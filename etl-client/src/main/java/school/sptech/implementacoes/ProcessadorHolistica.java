package school.sptech.implementacoes;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import school.sptech.dto.DashboardHolisticaDto;
import school.sptech.framework.ProcessadorBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProcessadorHolistica extends ProcessadorBase {

    // Define o nome fixo do arquivo dentro da pasta da clínica
    private static final String NOME_ARQUIVO_PADRAO = "dashboard_holistica.json";

    @Override
    protected Object processarDados(List<Map<String, String>> dadosBrutos, String nomeClinica, LambdaLogger logger) {

        // Validação de segurança: se o lote vier vazio, aborta.
        if (dadosBrutos == null || dadosBrutos.isEmpty()) {
            return null;
        }

        // 1. DEFINIÇÃO DO CAMINHO
        // O nomeClinica vem automaticamente da pasta do S3 Trusted (processado pela Classe Mãe)
        // Exemplo de resultado: "Hospital_Sirio/dashboard_holistica.json"
        String caminhoArquivo = nomeClinica + "/" + NOME_ARQUIVO_PADRAO;

        logger.log("Iniciando atualização da dashboard para: " + nomeClinica);

        // 2. GERAÇÃO DO DTO
        // Processa o CSV recebido para calcular o estado atual do sistema
        DashboardHolisticaDto dtoAtual = gerarDtoDoLote(dadosBrutos);

        // 3. RECUPERAÇÃO DO HISTÓRICO (SE EXISTIR)
        // Tenta ler o JSON que já existe na pasta específica da clínica
        DashboardHolisticaDto dtoAntigo = lerJsonExistente(caminhoArquivo, DashboardHolisticaDto.class);

        // 4. LÓGICA DE MERGE (MISTURA ANTIGO + NOVO)
        if (dtoAntigo != null) {
            logger.log("Histórico encontrado. Realizando merge de dados...");

            // --- Merge do Gráfico de Histórico ---

            // Prepara lista de Labels (Datas/Horas)
            List<String> labelsCombinadas = new ArrayList<>();
            if (dtoAntigo.historico != null && dtoAntigo.historico.labels != null) {
                labelsCombinadas.addAll(dtoAntigo.historico.labels);
            }
            labelsCombinadas.addAll(dtoAtual.historico.labels);

            // Prepara lista de Valores
            List<Integer> valoresCombinados = new ArrayList<>();
            if (dtoAntigo.historico != null && dtoAntigo.historico.valores != null) {
                valoresCombinados.addAll(dtoAntigo.historico.valores);
            }
            valoresCombinados.addAll(dtoAtual.historico.valores);

            // Corte: Mantém apenas os últimos 20 pontos para não sobrecarregar o gráfico
            int maxPontos = 20;
            int tamanhoTotal = labelsCombinadas.size();
            int inicioCorte = Math.max(0, tamanhoTotal - maxPontos);

            // Atualiza o DTO Atual com a lista combinada e cortada
            dtoAtual.historico.labels = labelsCombinadas.subList(inicioCorte, tamanhoTotal);
            dtoAtual.historico.valores = valoresCombinados.subList(inicioCorte, tamanhoTotal);

            // KPIs, Matriz e Alertas mantêm-se os do "dtoAtual" (estado real-time)

        } else {
            logger.log("Nenhum histórico anterior encontrado. Criando novo arquivo.");
        }

        // 5. SALVAMENTO
        // Salva o arquivo atualizado na pasta correta do bucket Client
        salvarJsonFixo(dtoAtual, caminhoArquivo, logger);

        // Retorna null para sinalizar à classe mãe que o salvamento já foi feito manualmente
        return null;
    }

    /*
     Método auxiliar que converte o CSV Bruto
      no objeto estruturado DashboardHolisticaDto.
     */
    private DashboardHolisticaDto gerarDtoDoLote(List<Map<String, String>> dadosBrutos) {
        DashboardHolisticaDto dto = new DashboardHolisticaDto();

        // Listas auxiliares para preencher o DTO
        List<DashboardHolisticaDto.AlertaTriagem> alertas = new ArrayList<>();
        List<DashboardHolisticaDto.MatrizItem> matriz = new ArrayList<>();
        List<Integer> historicoValores = new ArrayList<>();
        List<String> historicoLabels = new ArrayList<>();
        List<DashboardHolisticaDto.HotspotItem> hotspots = new ArrayList<>();

        // Contadores e Acumuladores
        int countCritico = 0;
        int countAtencao = 0;
        int countSaudavel = 0;

        double somaCpu = 0;
        double somaRam = 0;

        int idCounter = 1; // Contador para gerar IDs sequenciais nos alertas

        for (Map<String, String> linha : dadosBrutos) {
            try {
                // Conversão segura de String para Double (trata vírgula e ponto)
                double cpu = Double.parseDouble(linha.getOrDefault("CPU", "0").replace(",", "."));
                double ram = Double.parseDouble(linha.getOrDefault("RAM", "0").replace(",", "."));
                double bateria = Double.parseDouble(linha.getOrDefault("BATERIA", "0").replace(",", "."));

                String uuid = linha.getOrDefault("UUID", "N/A");
                String timestamp = linha.getOrDefault("TIMESTAMP", "");

                // Acumula para médias
                somaCpu += cpu;
                somaRam += ram;

                // Adiciona ao histórico (para o gráfico de linha)
                historicoValores.add((int) cpu);

                // Formata o timestamp para pegar apenas a hora (HH:mm:ss)
                String labelHora = timestamp.length() > 11 ? timestamp.substring(11, 19) : timestamp;
                historicoLabels.add(labelHora);

                // --- Regras de Negócio (Definição de Status) ---
                // ************ Vou remover, alertas vão ser definidos no JIRA ************
                String tipoAlerta = null;
                String textoAlerta = null;
                String metricaAlerta = null;
                String valorAlerta = null;

                // Regra Crítica: CPU acima de 20% OU Bateria abaixo de 15%
                if (cpu > 90 || bateria < 15) {
                    tipoAlerta = "critico";

                    if (cpu > 90) {
                        textoAlerta = "Falha Crítica de CPU";
                        metricaAlerta = "CPU";
                        valorAlerta = String.format("%.1f%%", cpu);
                    } else {
                        textoAlerta = "Bateria em Nível Crítico";
                        metricaAlerta = "Bateria";
                        valorAlerta = String.format("%.1f%%", bateria);
                    }
                    countCritico++;
                }
                // Regra Atenção: RAM acima de 80%
                else if (ram > 80) {
                    tipoAlerta = "atencao";
                    textoAlerta = "Alta Demanda de Memória";
                    metricaAlerta = "RAM";
                    valorAlerta = String.format("%.1f%%", ram);
                    countAtencao++;
                }
                // Saudável
                else {
                    countSaudavel++;
                }

                // Se houver alerta, adiciona à lista (limitado a 10 para não poluir o JSON)
                if (tipoAlerta != null && alertas.size() < 10) {
                    alertas.add(new DashboardHolisticaDto.AlertaTriagem(
                            idCounter++,
                            1, // ID Modelo (pode ser dinâmico se vier no CSV)
                            uuid,
                            tipoAlerta,
                            textoAlerta,
                            "Agora", // Tempo relativo
                            metricaAlerta,
                            valorAlerta
                    ));
                }

            } catch (NumberFormatException e) {

            }
        }

        // --- Cálculos Finais ---
        int totalRegistros = dadosBrutos.size();
        double mediaCpu = totalRegistros > 0 ? somaCpu / totalRegistros : 0;
        double mediaRam = totalRegistros > 0 ? somaRam / totalRegistros : 0;

        // 1. Preenchimento de KPIs
        dto.kpis = new DashboardHolisticaDto.Kpis(totalRegistros, countAtencao, countCritico);

        // 2. Fila de Triagem (Lista de Alertas)
        dto.filaTriagem = alertas;

        // 3. Histórico (Inicialmente com os dados do lote atual)
        dto.historico = new DashboardHolisticaDto.Historico(historicoLabels, historicoValores);

        // 4. Matriz de Stress (Simulação baseada na média do lote)
        // Assume valores de 0 a 100 para posicionamento na matriz
        matriz.add(new DashboardHolisticaDto.MatrizItem(1, "Média Geral", (int) mediaCpu));
        matriz.add(new DashboardHolisticaDto.MatrizItem(2, "Média Geral", (int) mediaRam));
        matriz.add(new DashboardHolisticaDto.MatrizItem(3, "Média Geral", 50)); // Disco fixo em 50% por enquanto
        dto.matrizStress = matriz;

        // 5. Hotspot (Exemplo Visual: Cria bolha vermelha se houver muitos críticos)
        if (countCritico > 0) {
            hotspots.add(new DashboardHolisticaDto.HotspotItem(
                    1,
                    "Área Crítica",
                    50, 50, 30, // Posição X, Y e Raio
                    "rgba(231, 76, 60, 0.7)" // Cor Vermelha
            ));
        }
        dto.hotspot = hotspots;

        // 6. Capacidade e Saúde
        dto.capacidade = new DashboardHolisticaDto.Capacidade(totalRegistros, 1000); // 1000 é um valor de exemplo para capacidade máxima
        dto.saudeBateria = new DashboardHolisticaDto.SaudeBateria(countSaudavel, countAtencao, countCritico);

        return dto;
    }
}