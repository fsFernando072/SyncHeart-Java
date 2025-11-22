package school.sptech.implementacoes;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import school.sptech.dto.DashboardHolisticaDto;
import school.sptech.framework.ProcessadorBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProcessadorHolistica extends ProcessadorBase {
/*
    Classe de Processamento da Dashboard Holistica (César)
*/
    // Nome fixo do arquivo JSON que o Front-end vai ler
    private static final String NOME_ARQUIVO_FINAL = "dashboard_holistica.json";

    @Override
    protected Object processarDados(List<Map<String, String>> dadosBrutos, LambdaLogger logger) {

        // 1. Gera o DTO com os dados do momento atual (CSV que acabou de chegar)
        DashboardHolisticaDto dtoAtual = gerarDtoDoLote(dadosBrutos);

        // 2. Tenta ler o JSON histórico que já existe no bucket Client
        DashboardHolisticaDto dtoAntigo = lerJsonExistente(NOME_ARQUIVO_FINAL, DashboardHolisticaDto.class);

        // 3. Faz o Merge (Mistura o histórico antigo com o novo)
        if (dtoAntigo != null) {
            logger.log("Dashboard Holística antiga encontrada. Atualizando histórico...");

            // --- Merge do Histórico (Gráfico de Linha) ---
            List<String> labelsCombinadas = new ArrayList<>();
            if (dtoAntigo.historico != null) labelsCombinadas.addAll(dtoAntigo.historico.labels);
            labelsCombinadas.addAll(dtoAtual.historico.labels);

            List<Integer> valoresCombinados = new ArrayList<>();
            if (dtoAntigo.historico != null) valoresCombinados.addAll(dtoAntigo.historico.valores);
            valoresCombinados.addAll(dtoAtual.historico.valores);

            // Mantém apenas os últimos 20 pontos para o gráfico não ficar gigante
            int maxPontos = 20;
            int tamanho = labelsCombinadas.size();
            int inicio = Math.max(0, tamanho - maxPontos);

            dtoAtual.historico.labels = labelsCombinadas.subList(inicio, tamanho);
            dtoAtual.historico.valores = valoresCombinados.subList(inicio, tamanho);

            // --- Merge de KPIs ---


        } else {
            logger.log("Nenhum histórico encontrado. Criando novo arquivo dashboard_holistica.json.");
        }

        // 4. Salva o arquivo atualizado com o nome fixo
        salvarJsonFixo(dtoAtual, NOME_ARQUIVO_FINAL, logger);

        // Retorna null para avisar a Classe Mãe que já salvamos manualmente
        return null;
    }

    // Método auxiliar para converter o CSV cru no Objeto DTO
    private DashboardHolisticaDto gerarDtoDoLote(List<Map<String, String>> dadosBrutos) {
        DashboardHolisticaDto dto = new DashboardHolisticaDto();

        // Listas auxiliares
        List<DashboardHolisticaDto.AlertaTriagem> alertas = new ArrayList<>();
        List<DashboardHolisticaDto.MatrizItem> matriz = new ArrayList<>();
        List<Integer> historicoValores = new ArrayList<>();
        List<String> historicoLabels = new ArrayList<>();

        int countCritico = 0;
        int countAtencao = 0;
        int countSaudavel = 0;

        double somaCpu = 0;
        double somaRam = 0;

        int idCounter = 1; // Para gerar IDs únicos para a lista de alertas

        for (Map<String, String> linha : dadosBrutos) {
            try {
                // Converte Strings do CSV para números (Tratando virgula e ponto)
                double cpu = Double.parseDouble(linha.getOrDefault("CPU", "0").replace(",", "."));
                double ram = Double.parseDouble(linha.getOrDefault("RAM", "0").replace(",", "."));
                double bateria = Double.parseDouble(linha.getOrDefault("BATERIA", "0").replace(",", "."));
                String uuid = linha.getOrDefault("UUID", "N/A");
                String ts = linha.getOrDefault("TIMESTAMP", "");

                somaCpu += cpu;
                somaRam += ram;

                // Preenche listas do histórico
                historicoValores.add((int) cpu);
                // Pega só o horário (HH:mm:ss) do timestamp
                historicoLabels.add(ts.length() > 11 ? ts.substring(11, 19) : ts);

                // #### Vou Remover. Os dados de alerta devem vir do Jira ####
                // --- Regras de Negócio (Alertas) ---
                String tipo = null;
                String texto = null;
                String metrica = null;
                String valor = null;

                // Crítico: CPU > 90% ou Bateria < 15%
                if (cpu > 90 || bateria < 15) {
                    tipo = "critico";
                    texto = cpu > 90 ? "Falha Crítica de CPU" : "Bateria em Nível Crítico";
                    metrica = cpu > 90 ? "CPU" : "Bateria";
                    valor = cpu > 90 ? String.format("%.1f%%", cpu) : String.format("%.1f%%", bateria);
                    countCritico++;
                }
                // Atenção: RAM > 80%
                else if (ram > 80) {
                    tipo = "atencao";
                    texto = "Alta Demanda de Memória";
                    metrica = "RAM";
                    valor = String.format("%.1f%%", ram);
                    countAtencao++;
                } else {
                    countSaudavel++;
                }

                // Adiciona na fila de triagem (Limitado a 20 para não poluir)
                if (tipo != null && alertas.size() < 20) {
                    alertas.add(new DashboardHolisticaDto.AlertaTriagem(
                            idCounter++,
                            1, // ID Modelo fictício
                            uuid,
                            tipo,
                            texto,
                            "Agora",
                            metrica,
                            valor
                    ));
                }

            } catch (Exception e) {
                // Ignora linhas com erro de conversão
                continue;
            }
        }

        // --- Cálculos Finais e Preenchimento do DTO ---
        int total = dadosBrutos.size();
        double mediaCpu = total > 0 ? somaCpu / total : 0;
        double mediaRam = total > 0 ? somaRam / total : 0;

        // 1. KPIs
        dto.kpis = new DashboardHolisticaDto.Kpis(total, countAtencao, countCritico);

        // 2. Fila de Triagem
        dto.filaTriagem = alertas;

        // 3. Histórico (Momentâneo)
        dto.historico = new DashboardHolisticaDto.Historico(historicoLabels, historicoValores);

        // 4. Matriz de Stress (Simulação baseada na média do lote)
        matriz.add(new DashboardHolisticaDto.MatrizItem(1, "Média Geral", (int) mediaCpu));
        matriz.add(new DashboardHolisticaDto.MatrizItem(2, "Média Geral", (int) mediaRam));
        matriz.add(new DashboardHolisticaDto.MatrizItem(3, "Média Geral", 50)); // Disco fixo
        dto.matrizStress = matriz;

        // 5. Hotspot (Exemplo Visual)
        dto.hotspot = new ArrayList<>();
        if (countCritico > 0) {
            dto.hotspot.add(new DashboardHolisticaDto.HotspotItem(1, "Área Crítica", 50, 50, 30, "rgba(231, 76, 60, 0.7)"));
        }

        // 6. Capacidade e Saúde
        dto.capacidade = new DashboardHolisticaDto.Capacidade(total, 1000);
        dto.saudeBateria = new DashboardHolisticaDto.SaudeBateria(countSaudavel, countAtencao, countCritico);

        return dto;
    }
}