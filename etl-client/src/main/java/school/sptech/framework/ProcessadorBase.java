package school.sptech.framework;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public abstract class ProcessadorBase implements RequestHandler<S3Event, String> {

    protected final S3Client s3Client = S3Client.builder().build();
    protected final ObjectMapper jsonMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    protected final CsvMapper csvMapper = new CsvMapper();

    private static final String BUCKET_DESTINO = System.getenv("BUCKET_DESTINO");

    // --- MUDANÇA AQUI: Adicionei 'String folderName' nos parâmetros ---
    protected abstract Object processarDados(List<Map<String, String>> dadosBrutos, String folderName, LambdaLogger logger);

    @Override
    public String handleRequest(S3Event evento, Context contexto) {
        LambdaLogger logger = contexto.getLogger();

        try {
            for (var registro : evento.getRecords()) {
                String bucketOrigem = registro.getS3().getBucket().getName();

                // O S3 entrega o caminho completo: "Hospital_Sirio/dados.csv"
                String chaveOrigem = registro.getS3().getObject().getUrlDecodedKey();

                logger.log("Iniciando processamento: " + chaveOrigem);

                // --- LÓGICA PARA EXTRAIR A PASTA (CLÍNICA) ---
                String folderName = "Geral"; // Padrão caso esteja na raiz
                int indexBarra = chaveOrigem.indexOf("/");

                if (indexBarra != -1) {
                    // Pega tudo que está antes da primeira barra "/"
                    folderName = chaveOrigem.substring(0, indexBarra);
                }

                logger.log("Clínica identificada pela pasta: " + folderName);
                // -----------------------------------------------------

                List<Map<String, String>> dadosBrutos = lerCsvDoBucket(bucketOrigem, chaveOrigem);

                // Passamos o folderName para o filho
                Object resultado = processarDados(dadosBrutos, folderName, logger);

                if (resultado != null) {
                    // Se usar o método genérico, ele salva DENTRO da pasta da clínica também
                    String nomeArquivoFinal = folderName + "/" + chaveOrigem.substring(indexBarra + 1).replace(".csv", ".json");
                    salvarJsonGenerico(resultado, nomeArquivoFinal, logger);
                }
            }
        } catch (Exception e) {
            logger.log("ERRO FATAL: " + e.getMessage());
            e.printStackTrace();
            return "Erro: " + e.getMessage();
        }
        return "Sucesso";
    }

    // ... (Mantenha os métodos lerCsvDoBucket, lerJsonExistente, salvarJsonFixo iguais) ...
    // Vou repetir apenas o salvarJsonFixo para garantir que ele não mude nada errado:

    private List<Map<String, String>> lerCsvDoBucket(String bucket, String key) throws Exception {
        try (InputStream input = s3Client.getObject(GetObjectRequest.builder()
                .bucket(bucket).key(key).build())) {
            CsvSchema schema = CsvSchema.emptySchema().withHeader().withColumnSeparator(';');
            MappingIterator<Map<String, String>> it = csvMapper.readerFor(Map.class).with(schema).readValues(input);
            return it.readAll();
        }
    }

    protected <T> T lerJsonExistente(String chaveCompleta, Class<T> classeDto) {
        try {
            // A chaveCompleta já deve vir com a pasta (ex: "Hospital/dashboard.json")
            String chave = "client/" + chaveCompleta;
            try (InputStream input = s3Client.getObject(GetObjectRequest.builder()
                    .bucket(BUCKET_DESTINO)
                    .key(chave).build())) {
                return jsonMapper.readValue(input, classeDto);
            }
        } catch (Exception e) {
            return null;
        }
    }

    protected void salvarJsonFixo(Object dados, String chaveCompleta, LambdaLogger logger) {
        try {
            String json = jsonMapper.writeValueAsString(dados);
            String chave = "client/" + chaveCompleta;

            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(BUCKET_DESTINO)
                            .key(chave).build(),
                    RequestBody.fromString(json, StandardCharsets.UTF_8));

            logger.log("Arquivo salvo em: " + chave);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar JSON fixo: " + e.getMessage());
        }
    }

    private void salvarJsonGenerico(Object dados, String chaveComPasta, LambdaLogger logger) {
        // ... implementação padrão ...
    }
}