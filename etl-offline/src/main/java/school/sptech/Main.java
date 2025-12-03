package school.sptech;

// Importando as dependências necessárias
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;


public class Main {
    public static void main(String[] args) {
        enviarAlertasOfflineJira();
    }

    public static void enviarAlertasOfflineJira() {
        JiraTicketCreator jiraTicketCreator = new JiraTicketCreator();

        System.out.println("--- Iniciando verificação de dispositivos offline e gerenciamento de tickets ---");

        List<Clinica> listaClinicas = listarClinicas();

        for (Clinica clinica : listaClinicas) {
            System.out.println(String.format("\nProcessando clínica: %s (ID: %d)", clinica.getNome(), clinica.getId()));

            try {
                // 1. BUSCAR TICKETS ABERTOS NO JIRA PARA ESTA CLÍNICA
                // Mapa: [Dispositivo UUID] -> [Jira Issue Key]
                Map<String, String> ticketsAbertosNoJira = jiraTicketCreator.buscarTicketsAbertosPorClinica(clinica.getNome());

                System.out.println(String.format("   Tickets abertos encontrados no Jira para %s: %d", clinica.getNome(), ticketsAbertosNoJira.size()));

                // 2. Listar Dispositivos DESATUALIZADOS (> 5 minutos offline)
                List<Dispositivo> desatualizados = listarDispositivosDesatualizados(clinica);

                // PROCESSAMENTO 1: Abrir tickets para dispositivos que estão offline
                for (Dispositivo dispositivo : desatualizados) {
                    // Só cria se NÃO houver um ticket aberto no mapa consultado
                    if (!ticketsAbertosNoJira.containsKey(dispositivo.getUuid())) {
                        String summary = "ALERTA DISPOSITIVO OFFLINE";
                        String description = String.format(
                                """
                                Dispositivo_UUID: %s
                                Modelo_ID: %s
                                Componente: Offline
                                Horário: %s
                                """,
                                dispositivo.getUuid(),
                                dispositivo.getModelo_id(),
                                dispositivo.getUltima_atualizacao()
                        );

                        // CHAMA O MÉTODO E CAPTURA A KEY RETORNADA
                        String newTicketKey = jiraTicketCreator.criarTicket(summary, description, clinica.getNome());

                        // Adiciona a key ao mapa TEMPORÁRIO (para evitar duplicatas na mesma execução)
                        ticketsAbertosNoJira.put(dispositivo.getUuid(), newTicketKey);
                        System.out.println(String.format("🚨 Ticket ABERTO no Jira: %s para o dispositivo: %s", newTicketKey, dispositivo.getUuid()));
                    }
                }

                // 3. Listar Dispositivos ATUALIZADOS (online nos últimos 5 minutos)
                List<Dispositivo> atualizados = listarDispositivosAtualizados(clinica);

                // PROCESSAMENTO 2: Fechar tickets para dispositivos que voltaram ao normal
                for (Dispositivo dispositivo : atualizados) {
                    // Se o dispositivo ATUALIZADO tinha um ticket em aberto no Jira
                    if (ticketsAbertosNoJira.containsKey(dispositivo.getUuid())) {
                        String ticketKey = ticketsAbertosNoJira.get(dispositivo.getUuid());

                        // CHAMA O MÉTODO DE FECHAMENTO
                        jiraTicketCreator.fecharTicket(ticketKey);

                        // Remove da lista temporária
                        ticketsAbertosNoJira.remove(dispositivo.getUuid());
                        System.out.println(String.format("✅ RESOLVIDO: Ticket FECHADO (%s) para o dispositivo: %s", ticketKey, dispositivo.getUuid()));
                    }
                }

            } catch (IOException e) {
                System.err.println("❌ ERRO de rede ou API ao processar a clínica " + clinica.getNome() + ": " + e.getMessage());
            } catch (InterruptedException e) {
                System.err.println("❌ Processamento interrompido na clínica " + clinica.getNome() + ": " + e.getMessage());
                Thread.currentThread().interrupt();
                break; // Interrompe o loop principal após uma InterruptedException
            }
        }
        System.out.println("\n--- Verificação e gerenciamento de tickets concluídos ---");
    }

    public static List<Clinica> listarClinicas() {
        DatabaseConfiguration databaseConfiguration = new DatabaseConfiguration();
        JdbcTemplate template = new JdbcTemplate(databaseConfiguration.getDataSource());

        List<Clinica> listaClinicas = template.query(
                """
                SELECT clinica_id as id, nome_fantasia as nome FROM Clinicas
                """, new BeanPropertyRowMapper<>(Clinica.class)
        );

        return listaClinicas;
    }

    public static List<Dispositivo> listarDispositivosDesatualizados(Clinica clinica) {
        DatabaseConfiguration databaseConfiguration = new DatabaseConfiguration();
        JdbcTemplate template = new JdbcTemplate(databaseConfiguration.getDataSource());

        LocalDateTime limiteTempo = LocalDateTime.now().minusMinutes(6);
        String limiteTempoFormatado = limiteTempo.toString().replace("T", " ");

        List<Dispositivo> listaDispositivos = template.query(
                """
                SELECT 
                    d.dispositivo_id as id,
                    d.dispositivo_uuid as uuid,
                    d.modelo_id,
                    d.ultima_atualizacao
                FROM Dispositivos d
                INNER JOIN Modelos m ON m.modelo_id = d.modelo_id
                INNER JOIN Clinicas c ON m.clinica_id = c.clinica_id
                WHERE c.clinica_id = ? 
                AND d.ultima_atualizacao < ?
                """, new BeanPropertyRowMapper<>(Dispositivo.class), clinica.getId(), limiteTempoFormatado
        );

        return listaDispositivos;
    }

    public static List<Dispositivo> listarDispositivosAtualizados(Clinica clinica) {
        DatabaseConfiguration databaseConfiguration = new DatabaseConfiguration();
        JdbcTemplate template = new JdbcTemplate(databaseConfiguration.getDataSource());

        LocalDateTime limiteTempo = LocalDateTime.now().minusMinutes(6);
        String limiteTempoFormatado = limiteTempo.toString().replace("T", " ");

        List<Dispositivo> listaDispositivos = template.query(
                """
                SELECT
                    d.dispositivo_id as id,
                    d.dispositivo_uuid as uuid,
                    d.modelo_id,
                    d.ultima_atualizacao
                FROM Dispositivos d
                INNER JOIN Modelos m ON m.modelo_id = d.modelo_id
                INNER JOIN Clinicas c ON m.clinica_id = c.clinica_id
                WHERE c.clinica_id = ?
                AND d.ultima_atualizacao >= ?
                """, new BeanPropertyRowMapper<>(Dispositivo.class), clinica.getId(), limiteTempoFormatado
        );

        return listaDispositivos;
    }

}
