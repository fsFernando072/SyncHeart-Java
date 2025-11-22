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
    // Configurado para não falhar se o JSON tiver campos que o Java não conhece
    protected final ObjectMapper jsonMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    protected final CsvMapper csvMapper = new CsvMapper();

    // Variável de ambiente configurada na Lambda
    private static final String BUCKET_DESTINO = System.getenv("BUCKET_DESTINO");

    // --- Método Abstrato de Implementação ---
    protected abstract Object processarDados(List<Map<String, String>> dadosBrutos, LambdaLogger logger);

    @Override
    public String handleRequest(S3Event evento, Context contexto) {
        LambdaLogger logger = contexto.getLogger();

        try {
            for (var registro : evento.getRecords()) {
                String bucketOrigem = registro.getS3().getBucket().getName();
                String chaveOrigem = registro.getS3().getObject().getUrlDecodedKey();

                logger.log("Iniciando processamento: " + chaveOrigem);

                // 1. Lê o CSV
                List<Map<String, String>> dadosBrutos = lerCsvDoBucket(bucketOrigem, chaveOrigem);

                // 2. Chama a implementação do filho
                Object resultado = processarDados(dadosBrutos, logger);

                // 3. Se o filho retornar algo (e não tiver salvo manualmente), a mãe salva
                if (resultado != null) {
                    salvarJsonGenerico(resultado, chaveOrigem, logger);
                }
            }
        } catch (Exception e) {
            logger.log("ERRO FATAL: " + e.getMessage());
            e.printStackTrace();
            return "Erro: " + e.getMessage();
        }
        return "Sucesso";
    }

    private List<Map<String, String>> lerCsvDoBucket(String bucket, String key) throws Exception {
        try (InputStream input = s3Client.getObject(GetObjectRequest.builder()
                .bucket(bucket).key(key).build())) {

            // Configuração para o arquivo TRUSTED (Ponto e Vírgula)
            CsvSchema schema = CsvSchema.emptySchema().withHeader().withColumnSeparator(';');

            MappingIterator<Map<String, String>> it = csvMapper
                    .readerFor(Map.class)
                    .with(schema)
                    .readValues(input);

            return it.readAll();
        }
    }

    // Método para os filhos buscarem o JSON antigo
    protected <T> T lerJsonExistente(String nomeArquivoFixo, Class<T> classeDto) {
        try {
            String chave = "client/" + nomeArquivoFixo;
            try (InputStream input = s3Client.getObject(GetObjectRequest.builder()
                    .bucket(BUCKET_DESTINO)
                    .key(chave).build())) {
                return jsonMapper.readValue(input, classeDto);
            }
        } catch (Exception e) {
            // Se der 404 (NoSuchKey), retorna nulo. É a primeira vez que roda.
            return null;
        }
    }

    // Método para os filhos salvarem com nome fixo
    protected void salvarJsonFixo(Object dados, String nomeArquivoFixo, LambdaLogger logger) {
        try {
            String json = jsonMapper.writeValueAsString(dados);
            String chave = "client/" + nomeArquivoFixo;

            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(BUCKET_DESTINO)
                            .key(chave).build(),
                    RequestBody.fromString(json, StandardCharsets.UTF_8));

            logger.log("Arquivo atualizado e salvo: " + chave);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar JSON fixo: " + e.getMessage());
        }
    }

    // Método padrão (salva com o mesmo nome do CSV trocando extensão)
    private void salvarJsonGenerico(Object dados, String nomeOriginal, LambdaLogger logger) {
        try {
            String json = jsonMapper.writeValueAsString(dados);
            String nomeDestino = "client/" + nomeOriginal.replace(".csv", ".json");

            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(BUCKET_DESTINO)
                            .key(nomeDestino).build(),
                    RequestBody.fromString(json, StandardCharsets.UTF_8));
            logger.log("Arquivo salvo: " + nomeDestino);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar JSON genérico: " + e.getMessage());
        }
    }
}