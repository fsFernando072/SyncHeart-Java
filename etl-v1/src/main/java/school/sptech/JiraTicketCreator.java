package school.sptech;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class JiraTicketCreator {
    // VARIÁVEIS DE CONFIGURAÇÃO
    private final String jiraUrl;
    private final String jiraUsername;
    private final String jiraApiToken;
    private final String jiraProjectKey;
    private final String jiraIssueTypeName;
    private final String jiraPriorityName;
    private final String jiraAssigneeEmail;

    private final HttpClient httpClient;

    public JiraTicketCreator() {
        // Leitura das configurações
        this.jiraUrl = Dotenv.load().get("JIRA_URL");
        this.jiraUsername = Dotenv.load().get("JIRA_EMAIL");
        this.jiraApiToken = Dotenv.load().get("JIRA_API_TOKEN");
        this.jiraProjectKey = Dotenv.load().get("JIRA_PROJECT_KEY");
        this.jiraIssueTypeName = Dotenv.load().get("JIRA_ISSUE_TYPE_NAME");
        this.jiraPriorityName = Dotenv.load().get("JIRA_PRIORITY_NAME");
        this.jiraAssigneeEmail = Dotenv.load().get("JIRA_EMAIL"); // O assignee é definido pelo email/username

        // Verificações de configuração obrigatória
        Objects.requireNonNull(jiraUrl, "JIRA_URL não pode ser nulo.");
        Objects.requireNonNull(jiraUsername, "JIRA_USERNAME não pode ser nulo.");
        Objects.requireNonNull(jiraApiToken, "JIRA_API_TOKEN não pode ser nulo.");
        Objects.requireNonNull(jiraProjectKey, "JIRA_PROJECT_KEY não pode ser nulo.");
        Objects.requireNonNull(jiraIssueTypeName, "JIRA_ISSUE_TYPE_NAME não pode ser nulo.");
        Objects.requireNonNull(jiraPriorityName, "JIRA_PRIORITY_NAME não pode ser nulo.");


        // Configuração do Cliente HTTP com timeout de 10 segundos
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }


    /**
     * Filtra a lista e cria um ticket no Jira para cada Alerta CRÍTICO.
     */
    public void criarTickets(List<Log> alertas, Integer idModelo, Double captura, int valor, String componente, String nomeClinica) {

        System.out.println("--- Iniciando verificação de alertas para o Jira ---");

        alertas.sort(Comparator.comparing(Log::getTimestamp));


        LocalDateTime inicio = null;
        LocalDateTime fim = null;

        for (Log log : alertas) {

            if (inicio == null) {
                inicio = log.getTimestamp();
                fim = log.getTimestamp();
            }
            else {
                Duration diferenca = Duration.between(fim, log.getTimestamp());

                if (diferenca.getSeconds() > 10) {
                    inicio = log.getTimestamp();
                }

                fim = log.getTimestamp();
            }

            Duration duracaoAlerta = Duration.between(inicio, fim);
            Long diferencaTotal = duracaoAlerta.toSeconds();

            int diferencaTotalInt = Math.toIntExact(diferencaTotal);


            if (diferencaTotalInt > valor) {
                try {
                    String summary = String.format("%s ATINGIU %s%%", componente, captura);
                    String duracaoAlertaFormatado = duracaoAlerta.toString().replace("PT", " ");
                    String inicioFormatado = inicio.toString().replace("T", " ");
                    String fimFormatado = fim.toString().replace("T", " ");

                    String description = String.format(
                            """
                            Dispositivo_UUID: %s
                            Modelo_ID: %d
                            Componente: %s
                            Duração: %s
                            Valor detectado: %s%%
                            Horário: %s - %s
                            """,
                            log.getUuid(), idModelo, componente, duracaoAlertaFormatado, captura, inicioFormatado, fimFormatado
                    );

                    String jsonPayload = buildIssueJson(summary, description, nomeClinica);
                    enviarRequisicao(jsonPayload);
                }
                catch (Exception e) {
                    System.out.println("Ocorreu um erro ao processar o alerta!");
                    e.printStackTrace();
                }

            }


        }

        System.out.println("--- Verificação de alertas concluída ---");
    }

    public void criarTickets(List<Log> alertas, Integer idModelo, Double captura, String componente, String nomeClinica) {

        alertas.sort(Comparator.comparing(Log::getTimestamp));

        for (Log log : alertas) {

                String summary = String.format("Disco ATINGIU %s%%", captura);

                try {
                    String description = String.format(
                            """
                            Dispositivo_UUID: %s
                            Modelo_ID: %d
                            Componente: %s
                            Valor detectado: %s%%
                            Horário: %s
                            """,
                            log.getUuid(), idModelo,  componente, log.getDisco(), log.getTimestamp()
                    );

                    String jsonPayload = buildIssueJson(summary, description, nomeClinica);
                    enviarRequisicao(jsonPayload);
                }
                catch (Exception e) {
                    System.out.println("Ocorreu um erro ao processar o alerta!");
                    e.printStackTrace();
                }
        }
    }

    public void criarTickets(List<Log> alertas, Integer idModelo, Double captura, String nomeClinica) {
        System.out.println("--- Iniciando verificação de alertas para o Jira ---");

        alertas.sort(Comparator.comparing(Log::getTimestamp));

        for (Log log : alertas) {

                String summary = String.format("Bateria ATINGIU %s%%", captura);

                try {
                    String description = String.format(
                            """
                            Dispositivo_UUID: %s
                            Modelo_ID: %d
                            Componente: %s
                            Valor detectado: %s%%
                            Horário: %s
                            """,
                            log.getUuid(), idModelo, "Bateria", log.getBateria(), log.getTimestamp()
                    );

                    String jsonPayload = buildIssueJson(summary, description, nomeClinica);
                    enviarRequisicao(jsonPayload);
                }
                catch (Exception e) {
                    System.out.println("Ocorreu um erro ao processar o alerta!");
                    e.printStackTrace();
                }

        }
    }

    // --- MÉTODOS PRIVADOS DE SUPORTE ---

    /**
     * Monta o JSON (Payload) para o Issue no formato ADF, incluindo campos obrigatórios.
     */
    private String buildIssueJson(String summary, String descriptionDetails, String nomeClinica) {
        String safeSummary = summary.replace("\"", "\\\"");
        String adfDescription = createAdfDescription(descriptionDetails);

        String jsonTemplate =
                "{" +
                        "\"fields\": {" +
                        "\"project\": {\"key\": \"%s\"}," +
                        "\"summary\": \"%s\"," +
                        "\"description\": %s," +
                        "\"issuetype\": {\"name\": \"%s\"}," +
                        "\"priority\": {\"name\": \"%s\"}," +
                        "\"labels\": [\"%s\"]" +
                        "}" +
                        "}";

        return String.format(
                jsonTemplate,
                jiraProjectKey,
                safeSummary,
                adfDescription, // adfDescription é um JSON e é inserido sem aspas adicionais
                jiraIssueTypeName,
                jiraPriorityName,
                nomeClinica.replaceAll(" ", "_")
        );
    }

    /**
     * Converte uma string de texto simples (com quebras de linha) para o formato JSON ADF exigido pelo Jira.
     */
    private String createAdfDescription(String simpleText) {
        // 1. Escapa caracteres que podem quebrar o JSON (principalmente aspas duplas e barras invertidas)
        String escapedText = simpleText
                .replace("\\", "\\\\") // Escapa barras invertidas
                .replace("\"", "\\\""); // Escapa aspas duplas

        // 2. Divide o texto por quebras de linha duplas (\n\n) para criar parágrafos separados no ADF
        String[] paragraphs = escapedText.split("\n\n");

        StringBuilder contentBuilder = new StringBuilder();

        for (String paragraph : paragraphs) {
            String cleanParagraph = paragraph.trim();

            if (!cleanParagraph.isEmpty()) {
                // Dentro do parágrafo, substitui quebras de linha simples restantes (\n) por \n dentro da string JSON,
                // que o Jira ADF renderiza como uma quebra de linha.
                String safeContent = cleanParagraph.replace("\n", "\\n");

                // Adiciona cada parágrafo como um bloco de 'paragraph' no ADF
                contentBuilder.append(String.format(
                        ",{\"type\": \"paragraph\", \"content\": [{\"type\": \"text\", \"text\": \"%s\"}]}",
                        safeContent
                ));
            }
        }

        // Remove a vírgula inicial (se houver conteúdo) e monta a estrutura final
        String finalContent = contentBuilder.length() > 0 ? contentBuilder.substring(1) : "";

        // Estrutura mínima do ADF (Atlassian Document Format)
        return String.format(
                "{\"type\": \"doc\", \"version\": 1, \"content\": [%s]}",
                finalContent.isEmpty() ? "{\"type\": \"paragraph\", \"content\": [{\"type\": \"text\", \"text\": \"Sem descrição.\"}]}" : finalContent
        );
    }


    /**
     * Envia a requisição HTTP POST para a API do Jira para criar um Issue.
     */
    private void enviarRequisicao(String jsonPayload) {
        try {
            // Cria o cabeçalho de autenticação Basic com o username e API Token
            String authString = jiraUsername + ":" + jiraApiToken;
            String authHeader = "Basic " + Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));

            URI uri = URI.create(jiraUrl + "/rest/api/3/issue");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/json")
                    .header("Authorization", authHeader)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            String responseBody = response.body();

            if (statusCode >= 200 && statusCode < 300) {
                System.out.println("✅ SUCESSO: Ticket do Jira criado. Status HTTP: " + statusCode);
            } else {
                System.err.println("❌ FALHA na API do Jira. Código: " + statusCode);
                System.err.println("   Resposta do Jira: " + responseBody);
            }

        } catch (HttpTimeoutException e) {
            System.err.println("❌ ERRO DE CONEXÃO: Tempo limite (Timeout) atingido ao tentar acessar Jira.");
        } catch (IOException | InterruptedException e) {
            System.err.println("❌ ERRO GERAL na requisição HTTP para o Jira: " + e.getMessage());
        }
    }
}