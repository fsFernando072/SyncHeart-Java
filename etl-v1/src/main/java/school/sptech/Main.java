package school.sptech;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.*;

public class Main {
    public static void main(String[] args) {

        S3Client s3Client = S3Client.builder().region(Region.US_EAST_1).build();

        baixarArquivos(s3Client);
        tratarDados();
        enviarArquivos(s3Client);

    }

    public static void baixarArquivos(S3Client s3Client) {
        System.out.println("Baixando os arquivos do bucket...");
        try {
            ListBucketsResponse response = s3Client.listBuckets();
            List<Bucket> bucketList = response.buckets();
            ListObjectsV2Request req = ListObjectsV2Request.builder().bucket(bucketList.get(0).name()).build();
            ListObjectsV2Response res = s3Client.listObjectsV2(req);

            Path pasta = Paths.get("arquivos");
            Files.createDirectories(pasta);

            for (S3Object arquivo : res.contents()) {

                Path destino = pasta.resolve(arquivo.key());

                S3TransferManager transferManager = S3TransferManager.builder()
                        .build();

                DownloadFileRequest downloadFileRequest = DownloadFileRequest.builder()
                        .getObjectRequest(b -> b.bucket(bucketList.getFirst().name()).key(arquivo.key()))
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

    public static void tratarDados() {
        System.out.println("Iniciando tratamento dos dados...");

        File pasta = new File("arquivos");
        File[] arquivos = pasta.listFiles();

        try {
            Path pastaTratada = Paths.get("arquivos_tratados");
            Files.createDirectories(pastaTratada);
        }
        catch (IOException erro) {
            System.out.println("Erro ao criar a pasta");
            erro.printStackTrace();
            System.exit(1);
        }

        if (arquivos != null) {
            for (File arquivo : arquivos) {
                try {
                    FileInputStream inputStream = new FileInputStream("arquivos/" + arquivo.getName());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    OutputStream outputStream = new FileOutputStream("arquivos_tratados/" + arquivo.getName());
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
                    String line;
                    List<Log> logs = new ArrayList<>();

                    reader.readLine();

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

    public static void enviarArquivos(S3Client s3Client) {
        System.out.println("Iniciando envio dos arquivos...");

        S3TransferManager transferManager = S3TransferManager.builder().build();
        File pasta = new File("arquivos_tratados");
        File[] arquivos = pasta.listFiles();

        ListBucketsResponse response = s3Client.listBuckets();
        List<Bucket> bucketList = response.buckets();

        if (arquivos != null) {
            for (File arquivo : arquivos) {
                UploadFileRequest uploadFileRequest = UploadFileRequest.builder()
                        .putObjectRequest(b -> b.bucket(bucketList.get(1).name()).key(arquivo.getName()))
                        .source(Paths.get("arquivos_tratados/" + arquivo.getName()))
                        .build();

                FileUpload fileUpload = transferManager.uploadFile(uploadFileRequest);

                fileUpload.completionFuture().join();
            }
        }
        else {
            System.out.println("Diretório não encontrado");
        }

        System.out.println("Envio de arquivos concluído com sucesso");
        removerPasta("arquivos_tratados");

    }

}
