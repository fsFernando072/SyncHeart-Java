package school.sptech;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        S3Client s3Client = S3Client.builder().region(Region.US_EAST_1).build();

        DatabaseConfiguration databaseConfiguration = new DatabaseConfiguration();
        JdbcTemplate template = new JdbcTemplate(databaseConfiguration.getDataSource());

        String destinationBucket = "s3-trusted-lab-202523112058";
        String sourceBucket = "s3-raw-lab-202523112057";

        try {
            ListBucketsResponse response = s3Client.listBuckets();
            List<Bucket> bucketList = response.buckets();
            ListObjectsV2Request req = ListObjectsV2Request.builder().bucket(bucketList.get(0).name()).build();
            ListObjectsV2Response res = s3Client.listObjectsV2(req);

            for (S3Object arquivo : res.contents()) {
                try {
                    GetObjectRequest objectRequest = GetObjectRequest.builder()
                            .bucket(sourceBucket)
                            .key(arquivo.key())
                            .build();

                    ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(objectRequest);

                    List<Log> logs = new ArrayList<>();
                    List<Clinica> clinicas = new ArrayList<>();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(s3Object));
                    String line;

                    reader.readLine();

                    while ((line = reader.readLine()) != null) {
                        String[] dados = line.split(",");

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


                        LocalDateTime timestamp = LocalDateTime.parse(dados[0], formatter);
                        String uuid = dados[1];
                        Boolean arritmia = Boolean.parseBoolean(dados[2]);
                        Double cpu = Double.parseDouble(dados[3]);
                        Double ram = Double.parseDouble(dados[4]);
                        Double disco = Double.parseDouble(dados[5]);
                        Double bateria = Double.parseDouble(dados[6]);
                        Integer tarefas = Integer.parseInt(dados[7]);
                        String listaTarefas = dados[8];

                        Log log = new Log(timestamp, uuid, arritmia, cpu, ram, disco, bateria, tarefas);
                        log.adicionarTarefa(listaTarefas);
                        logs.add(log);

                        Clinica c = template.queryForObject(
                                """
                                SELECT clinica_id as id, nome_fantasia as nome FROM Clinicas
                                WHERE clinica_id = (SELECT clinica_id FROM EquipesCuidado WHERE equipe_id = (SELECT
                                equipe_id FROM Dispositivos WHERE dispositivo_uuid = ?))
                                """, new BeanPropertyRowMapper<>(Clinica.class), log.getUuid()
                        );

                        Boolean repete = false;
                        for (Clinica clinica : clinicas) {
                            if (c.getNome().equals(clinica.getNome())) {
                                repete = true;

                                c = clinica;
                                break;
                            }
                        }

                        c.getListaLogs().add(log);

                        if (!repete) {
                            clinicas.add(c);
                        }

                    }

                    CsvWriter csvWriter = new CsvWriter();
                    ByteArrayOutputStream csvOutputStream = csvWriter.writeCsv(logs);
                    byte[] bytes = csvOutputStream.toByteArray();

                    for (Clinica c : clinicas) {
                        ListObjectsV2Request request = ListObjectsV2Request.builder()
                                .bucket(sourceBucket)
                                .prefix(c.getNome() + "/")
                                .maxKeys(1)
                                .build();
                    }

                    PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket("s3-trusted-lab-202523112058").key(logs.get(0).getTimestamp().toString() + ".csv").build();


                    s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));

                    try {
                        reader.close();
                    }
                    catch (IOException erro) {
                        System.out.println("Erro ao fechar o arquivo");
                        erro.printStackTrace();
                    }

                }
                catch (IOException erro) {
                    System.out.println("Falha ao abrir o arquivo");
                    erro.printStackTrace();
                }

            }
        }
        catch (S3Exception erro) {
            System.out.println(erro.awsErrorDetails().errorMessage());
        }
        finally {
            System.out.println("Sucesso");
        }

    }


}
