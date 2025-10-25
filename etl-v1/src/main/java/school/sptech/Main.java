package school.sptech;

// !!!IMPORTANTE!!! - LEIA ANTES DE EXECUTAR:
/* Para esta primeira versão da ETL, o código está sendo executado localmente (considerando que não esteja rodando em uma EC2).
    Dito isso, é necessário que você verifique sua conexão com a AWS para que o código seja executado sem problemas, então abra o
    terminal do seu computador e use o comando aws configure para atualizar suas credenciais de acesso.
    O script utiliza essas credenciais para se conectar aos buckets, então sem credenciais não há como acessar.

    Ponto importante: A seleção dos buckets (para downloads e uploads) está sendo feita através do índice dos buckets na aws, mas tem um porém.
    Isso foi feito acessando os buckets da minha conta, então é importante que seja verificada a ordem dos seus buckets na aws.

    Para resumir o funcionamento do script: Acessa a AWS a partir das suas credenciais e baixa os arquivos do bucket raw,
    em seguida trata os dados, envia para o bucket trusted e exclui os downloads feitos no computador. Após isso, é realizado
    mais um tratamento para enfim enviar os arquivos para o bucket client e guardar os alertas no banco de dados como um histórico.
 */


// Importando as dependências necessárias
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.*;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;





public class Main {
    public static void main(String[] args) {

        // Estabelecendo conexão com a AWS
        S3Client s3Client = S3Client.builder().region(Region.US_EAST_1).build();

        baixarArquivos(s3Client, 1, "arquivos");
        tratarDadosRaw();
        enviarArquivos(s3Client, 2, "arquivos_tratados");

        baixarArquivos(s3Client, 2, "arquivos1");
        tratamentoClient();
        enviarArquivos(s3Client, 0, "arquivos_tratados1");

    }

    // Função que baixa os arquivos do bucket
    public static void baixarArquivos(S3Client s3Client, Integer indiceBucket, String nomePasta) {
        System.out.println("Baixando os arquivos do bucket...");
        try {
            // Lista todos os buckets do usuário e guarda em uma lista, em seguida seleciona o primeiro bucket
            // (índice 0) para fazer o download dos arquivos
            ListBucketsResponse response = s3Client.listBuckets();
            List<Bucket> bucketList = response.buckets();
            ListObjectsV2Request req = ListObjectsV2Request.builder().bucket(bucketList.get(indiceBucket).name()).build();
            ListObjectsV2Response res = s3Client.listObjectsV2(req);


            // Cria um diretório localmente chamado "arquivos". O diretório irá guardar os arquivos baixados.
            Path pasta = Paths.get(nomePasta);
            Files.createDirectories(pasta);


            // Lista todos os arquivos do bucket e faz o download dentro da pasta arquivos.
            for (S3Object arquivo : res.contents()) {

                Path destino = pasta.resolve(arquivo.key());

                S3TransferManager transferManager = S3TransferManager.builder()
                        .build();

                DownloadFileRequest downloadFileRequest = DownloadFileRequest.builder()
                        .getObjectRequest(b -> b.bucket(bucketList.get(indiceBucket).name()).key(arquivo.key()))
                        .destination(destino)
                        .build();

                FileDownload download = transferManager.downloadFile(downloadFileRequest);
                download.completionFuture().join();


            }

        }
        catch (S3Exception erro) {
            System.out.println(erro.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        catch (IOException erro) {
            System.out.println("Ocorreu um erro ao criar a pasta");
            erro.printStackTrace();
            System.exit(1);
        }

        System.out.println("Arquivos baixados com sucesso");
    }

    // Função para o tratamento dos arquivos baixados
    public static void tratarDadosRaw() {
        System.out.println("Iniciando tratamento dos dados...");

        File pasta = new File("arquivos");
        File[] arquivos = pasta.listFiles();

        // Cria um novo diretório chamado "arquivos_tratados" para conter a modificação dos arquivos
        try {
            Path pastaTratada = Paths.get("arquivos_tratados");
            Files.createDirectories(pastaTratada);
        }
        catch (IOException erro) {
            System.out.println("Erro ao criar a pasta");
            erro.printStackTrace();
            System.exit(1);
        }

        // Lista todos os arquivos na pasta arquivos e os modifica, enviando a nova versão para o diretório arquivos_tratados
        // Remove o diretório arquivos no final
        if (arquivos != null) {
            for (File arquivo : arquivos) {
                try {
                    FileInputStream inputStream = new FileInputStream("arquivos/" + arquivo.getName());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    OutputStream outputStream = new FileOutputStream("arquivos_tratados/" + arquivo.getName());
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
                    String line;
                    List<Log> logs = new ArrayList<>();

                    // Ignora a primeira linha do arquivo (ou seja, o header)
                    reader.readLine();

                    // Guarda os dados do arquivo em uma lista e cria um objeto Log com os dados
                    while ((line = reader.readLine()) != null) {
                        String[] dados = line.split(",");

                        String timestamp = dados[0];
                        String id = dados[1];
                        Boolean arritmia = Boolean.parseBoolean(dados[2]);
                        Double cpu = Double.parseDouble(dados[3]);
                        Double ram = Double.parseDouble(dados[4]);
                        Double disco = Double.parseDouble(dados[5]);
                        Double bateria = Double.parseDouble(dados[6]);
                        Integer tarefas = Integer.parseInt(dados[7]);
                        String listaTarefas = dados[8];

                        Log log = new Log(timestamp, id, arritmia, cpu, ram, disco, bateria, tarefas, listaTarefas);
                        logs.add(log);
                    }

                    // Formata os dados e escreve em um novo arquivo
                    try {
                        writer.append("TIMESTAMP;UUID;ARRITMIA;CPU;RAM;DISCO;BATERIA;QTD_TAREFAS;TAREFAS\n");

                        for (Log log : logs) {
                            writer.write(String.format("%s;%s;%b;%.1f;%.1f;%.1f;%.1f;%d;%s\n", log.getTimestamp(),
                                    log.getId(), log.getArritmia(), log.getCpu(), log.getRam(), log.getDisco(), log.getBateria(),
                                    log.getTarefas(), log.getListaTarefas()).replace(",", "."));
                        }
                    }
                    catch (IOException erro) {
                        System.out.println("Erro ao escrever o arquivo");
                        erro.printStackTrace();
                    }
                    finally {
                        try {
                            writer.close();
                            reader.close();
                            outputStream.close();
                            inputStream.close();
                        }
                        catch (IOException erro) {
                            System.out.println("Erro ao fechar o arquivo");
                            erro.printStackTrace();
                            System.exit(1);
                        }
                    }



                }
                catch (IOException erro) {
                    System.out.println("Falha ao abrir o arquivo");
                    erro.printStackTrace();
                    System.exit(1);
                }
            }
        }
        else {
            System.out.println("Diretório não encontrado.");
            System.exit(1);
        }

        System.out.println("Fim do tratamento");
        removerPasta("arquivos");

    }

    // Função para o tratamento final dos dados
    public static void tratamentoClient() {
        System.out.println("Iniciando tratamento final dos dados...");

        File pasta = new File("arquivos1");
        File[] arquivos = pasta.listFiles();

        // Cria um novo diretório chamado "arquivos_tratados" para conter a modificação dos arquivos
        try {
            Path pastaTratada = Paths.get("arquivos_tratados1");
            Files.createDirectories(pastaTratada);
        }
        catch (IOException erro) {
            System.out.println("Erro ao criar a pasta");
            erro.printStackTrace();
            System.exit(1);
        }

        // Lista todos os arquivos na pasta arquivos e os modifica, enviando a nova versão para o diretório arquivos_tratados
        // Remove o diretório arquivos no final
        if (arquivos != null) {
            for (File arquivo : arquivos) {
                try {
                    FileInputStream inputStream = new FileInputStream("arquivos1/" + arquivo.getName());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    OutputStream outputStream = new FileOutputStream("arquivos_tratados1/" + arquivo.getName());
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
                    String line;
                    List<Log> logs = new ArrayList<>();

                    // Ignora a primeira linha do arquivo (ou seja, o header)
                    reader.readLine();

                    // Guarda os dados do arquivo em uma lista e cria um objeto Log com os dados
                    while ((line = reader.readLine()) != null) {
                        String[] dados = line.split(";");

                        String timestamp = dados[0];
                        String id = dados[1];
                        Boolean arritmia = Boolean.parseBoolean(dados[2]);
                        Double cpu = Double.parseDouble(dados[3]);
                        Double ram = Double.parseDouble(dados[4]);
                        Double disco = Double.parseDouble(dados[5]);
                        Double bateria = Double.parseDouble(dados[6]);
                        Integer tarefas = Integer.parseInt(dados[7]);
                        String listaTarefas = dados[8];

                        Log log = new Log(timestamp, id, arritmia, cpu, ram, disco, bateria, tarefas, listaTarefas);
                        logs.add(log);

                        DatabaseConfiguration databaseConfiguration = new DatabaseConfiguration();
                        JdbcTemplate template = new JdbcTemplate(databaseConfiguration.getDataSource());

                        List<Parametro> listaParametros = template.query("""
                                SELECT metrica, condicao, limiar_valor from ModelosAlertaParametros mp
                                WHERE mp.modelo_id = (SELECT modelo_id FROM Dispositivos WHERE dispositivo_uuid LIKE ?)
                                """, new BeanPropertyRowMapper<>(Parametro.class),  "%" + log.getId() + "%");


                        for (Parametro parametro : listaParametros) {
                            if (parametro.getMetrica().equals("CPU") && log.getCpu() >= parametro.getLimiarValor()) {
                                salvarHistorico(template, log, "CPU", "CPU excedeu o limite");
                                enviarAlertas("CPU excedeu o limite");
                            }
                            else if (parametro.getMetrica().equals("RAM") && log.getRam() >= parametro.getLimiarValor()) {
                                salvarHistorico(template, log, "RAM", "RAM excedeu o limite");
                                enviarAlertas("RAM excedeu o limite");
                            }
                            else if (parametro.getMetrica().equals("Bateria") && log.getBateria() <= parametro.getLimiarValor()) {
                                salvarHistorico(template, log, "Bateria", "Bateria fraca");
                                enviarAlertas("Bateria fraca");
                            }
                            else if (parametro.getMetrica().equals("Disco") && log.getDisco() >= parametro.getLimiarValor()) {
                                salvarHistorico(template, log, "Disco", "Disco excedeu o limite");
                                enviarAlertas("Disco excedeu o limite");
                            }
                        }


                    }

                    // Formata os dados e escreve em um novo arquivo
                    try {
                        writer.append("TIMESTAMP;UUID;ARRITMIA;CPU;RAM;DISCO;BATERIA;QTD_TAREFAS;TAREFAS\n");

                        for (Log log : logs) {
                            writer.write(String.format("%s;%s;%b;%.1f;%.1f;%.1f;%.1f;%d;%s\n", log.getTimestamp(),
                                    log.getId(), log.getArritmia(), log.getCpu(), log.getRam(), log.getDisco(), log.getBateria(),
                                    log.getTarefas(), log.getListaTarefas()).replace(",", "."));
                        }
                    }
                    catch (IOException erro) {
                        System.out.println("Erro ao escrever o arquivo");
                        erro.printStackTrace();
                    }
                    finally {
                        try {
                            writer.close();
                            reader.close();
                            outputStream.close();
                            inputStream.close();
                        }
                        catch (IOException erro) {
                            System.out.println("Erro ao fechar o arquivo");
                            erro.printStackTrace();
                            System.exit(1);
                        }
                    }



                }
                catch (IOException erro) {
                    System.out.println("Falha ao abrir o arquivo");
                    erro.printStackTrace();
                    System.exit(1);
                }
            }
        }
        else {
            System.out.println("Diretório não encontrado.");
            System.exit(1);
        }

        System.out.println("Fim do tratamento");
        removerPasta("arquivos1");

    }

    // Função para remover os diretórios
    public static void removerPasta(String nomePasta) {
        System.out.println("Excluindo diretório " + nomePasta + "...");

        File pasta = new File(nomePasta);
        File[] arquivos = pasta.listFiles();

        if (arquivos != null) {
            for (File arquivo : arquivos) {
                arquivo.delete();
            }
        }

        pasta.delete();

        System.out.println("Diretório excluído com sucesso");
    }

    // Função para enviar os arquivos para um bucket
    public static void enviarArquivos(S3Client s3Client, Integer indiceBucket, String nomePasta) {
        System.out.println("Iniciando envio dos arquivos...");

        // Lista todos os arquivos do diretório arquivos_tratados e guarda em uma lista
        S3TransferManager transferManager = S3TransferManager.builder().build();
        File pasta = new File(nomePasta);
        File[] arquivos = pasta.listFiles();

        ListBucketsResponse response = s3Client.listBuckets();
        List<Bucket> bucketList = response.buckets();

        // Seleciona os arquivos na lista e envia para o bucket trusted
        if (arquivos != null) {
            for (File arquivo : arquivos) {
                UploadFileRequest uploadFileRequest = UploadFileRequest.builder()
                        .putObjectRequest(b -> b.bucket(bucketList.get(indiceBucket).name()).key(arquivo.getName()))
                        .source(Paths.get(nomePasta + "/" + arquivo.getName()))
                        .build();

                FileUpload fileUpload = transferManager.uploadFile(uploadFileRequest);

                fileUpload.completionFuture().join();
            }
        }
        else {
            System.out.println("Diretório não encontrado");
        }


        // Exclui o diretório arquivos_tratados após o envio
        System.out.println("Envio de arquivos concluído com sucesso");
        removerPasta(nomePasta);

    }

    public static void enviarAlertas(String descricao) {
        try {
            JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
            Dotenv dotenv = Dotenv.load();
            String tokenJira = dotenv.get("JIRA_API_TOKEN");

            URI jiraServerUri = new URI("https://sptech-team-tll0v8wj.atlassian.net");
            try (JiraRestClient restClient = factory.createWithBasicHttpAuthentication(jiraServerUri, "davi.ssilva@sptech.school", tokenJira)) {

                IssueInputBuilder issueBuilder = new IssueInputBuilder("AAC", 10036L);
                issueBuilder.setSummary("Alerta detectado");
                issueBuilder.setDescription("Descrição da tarefa: {" + descricao + "}");
                IssueInput issueInput = issueBuilder.build();

                restClient.getIssueClient().createIssue(issueInput).claim();
            }
        }
        catch (URISyntaxException erro) {
            erro.printStackTrace();
        }
        catch (IOException erro) {
            erro.printStackTrace();
        }
    }

    public static void salvarHistorico(JdbcTemplate template, Log log, String componente, String mensagem) {
        String sqlInsert = """
                                        INSERT INTO Alertas (dispositivo_id, tipo_alerta, mensagem, detectado_em)
                                                      values((SELECT dispositivo_id FROM Dispositivos WHERE dispositivo_uuid LIKE ?), ?, ?, ?)
                                        """;

        template.update(sqlInsert, log.getId(), componente, mensagem, LocalDate.now());
    }

}
