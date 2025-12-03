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
    public String criarTicket(String summary, String descriptionDetails, String nomeClinica) throws IOException, InterruptedException {
        String jsonPayload = buildIssueJson(summary, descriptionDetails, nomeClinica);
        return enviarRequisicao(jsonPayload);
    }

    public Map<String, String> buscarTicketsAbertosPorClinica(String nomeClinica) {
        String clinicaLabel = nomeClinica.replaceAll(" ", "_");

        // JQL: Busca tickets no projeto, com a label da clínica, que NÃO estão resolvidos/fechados
        String jql = String.format(
                "project = \"%s\" AND labels = \"%s\" AND status = \"Open\" AND description ~ \"Componente: Offline \"",
                jiraProjectKey,
                clinicaLabel
        );

        String encodedJql = Base64.getUrlEncoder().encodeToString(jql.getBytes(StandardCharsets.UTF_8));

        // Endpoint de busca da API do Jira
        URI uri = URI.create(jiraUrl + "/rest/api/3/search/jql");

        Map<String, String> ticketsDoJira = new HashMap<>();

        try {
            String authString = jiraUsername + ":" + jiraApiToken;
            String authHeader = "Basic " + Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));

            String requestBody = String.format(
                    "{ \"jql\": \"%s\", \"fields\": [\"description\", \"id\", \"key\"], \"maxResults\": 200 }",
                    jql.replace("\"", "\\\"")
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", authHeader)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();

            if (statusCode == 200) {
                String responseBody = response.body();

                // 1. Divide a resposta em Issues
                String[] issues = responseBody.split("\"key\":");

                // O primeiro elemento (issues[0]) é o cabeçalho, os demais são os issues.
                for (int i = 1; i < issues.length; i++) {
                    String issueBlock = issues[i];

                    // Extrai a Key do Ticket
                    String ticketKey = issueBlock.substring(1, issueBlock.indexOf('\"', 1));

                    String uuidPrefix = "Dispositivo_UUID: ";
                    if (issueBlock.contains(uuidPrefix)) {
                        String uuidSection = issueBlock.substring(issueBlock.indexOf(uuidPrefix) + uuidPrefix.length());

                        // O UUID é a primeira sequência de caracteres antes de uma quebra de linha ou espaço
                        String dispositivoUuid = uuidSection.split("\n")[0].split("\"")[0].trim();

                        // Remove quebras de linha/espaços adicionais
                        if (dispositivoUuid.contains("\\n")) {
                            dispositivoUuid = dispositivoUuid.substring(0, dispositivoUuid.indexOf("\\n"));
                        }

                        // Adiciona ao mapa
                        ticketsDoJira.put(dispositivoUuid, ticketKey);
                    }
                }

            } else {
                System.err.println("❌ FALHA na consulta JQL. Código: " + statusCode);
                System.err.println("   Resposta do Jira: " + response.body());
            }

        } catch (HttpTimeoutException e) {
            System.err.println("❌ ERRO DE CONEXÃO: Tempo limite (Timeout) atingido ao consultar Jira.");
        } catch (IOException | InterruptedException e) {
            System.err.println("❌ ERRO GERAL na requisição HTTP para consulta JQL: " + e.getMessage());
        }

        return ticketsDoJira;
    }

    public void fecharTicket(String issueKey) {
        final String TRANSITION_ID = "31";

        String jsonPayload = String.format(
                "{\"transition\": {\"id\": \"%s\"}}",
                TRANSITION_ID
        );

        try {
            String authString = jiraUsername + ":" + jiraApiToken;
            String authHeader = "Basic " + Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));

            URI uri = URI.create(jiraUrl + "/rest/api/3/issue/" + issueKey + "/transitions");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/json")
                    .header("Authorization", authHeader)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();

            // Transição bem-sucedida retorna 204 No Content
            if (statusCode == 204) {
                System.out.println("✅ SUCESSO: Ticket do Jira transicionado (fechado): " + issueKey);
            } else {
                System.err.println("❌ FALHA na transição do ticket " + issueKey + ". Código: " + statusCode);
                System.err.println("   Resposta do Jira: " + response.body());
            }

        } catch (HttpTimeoutException e) {
            System.err.println("❌ ERRO DE CONEXÃO: Tempo limite (Timeout) atingido ao tentar fechar ticket.");
        } catch (IOException | InterruptedException e) {
            System.err.println("❌ ERRO GERAL na requisição HTTP (fechar ticket): " + e.getMessage());
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
    private String enviarRequisicao(String jsonPayload) throws IOException, InterruptedException {
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

                if (responseBody.contains("\"key\":")) {
                    String keyField = responseBody.substring(responseBody.indexOf("\"key\":") + 7);
                    String issueKey = keyField.substring(0, keyField.indexOf("\"")).trim();
                    return issueKey;
                }
                // Retorna um valor de falha se a Key não for encontrada
                return "KEY_NAO_ENCONTRADA";
            } else {
                System.err.println("❌ FALHA na API do Jira. Código: " + statusCode);
                System.err.println("   Resposta do Jira: " + responseBody);
                throw new IOException("Falha ao criar ticket no Jira. Código: " + statusCode);
            }

        } catch (HttpTimeoutException e) {
            System.err.println("❌ ERRO DE CONEXÃO: Tempo limite (Timeout) atingido ao tentar acessar Jira.");
            throw e;
        } catch (IOException | InterruptedException e) {
            System.err.println("❌ ERRO GERAL na requisição HTTP para o Jira: " + e.getMessage());
            throw e;
        }
    }
}