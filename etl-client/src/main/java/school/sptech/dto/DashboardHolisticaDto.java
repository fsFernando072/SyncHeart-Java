package school.sptech.dto;

import java.util.List;

public class DashboardHolisticaDto {
    public Kpis kpis;
    public List<MatrizItem> matrizStress;
    public List<HotspotItem> hotspot;
    public Historico historico;
    public Capacidade capacidade;
    public SaudeBateria saudeBateria;
    public List<AlertaTriagem> filaTriagem;

    public DashboardHolisticaDto() {}

    public static class Kpis {
        public int total;
        public int atencao;
        public int critico;
        public Kpis() {}
        public Kpis(int total, int atencao, int critico) { this.total = total; this.atencao = atencao; this.critico = critico; }
    }

    public static class MatrizItem {
        public int x;
        public String y;
        public int v;
        public MatrizItem() {}
        public MatrizItem(int x, String y, int v) { this.x = x; this.y = y; this.v = v; }
    }

    public static class HotspotItem {
        public int id;
        public String label;
        public DataPoint data;
        public String cor;
        public HotspotItem() {}
        public HotspotItem(int id, String label, int x, int y, int r, String cor) {
            this.id = id; this.label = label; this.data = new DataPoint(x, y, r); this.cor = cor;
        }
    }

    public static class DataPoint {
        public int x, y, r;
        public DataPoint() {}
        public DataPoint(int x, int y, int r) { this.x = x; this.y = y; this.r = r; }
    }

    public static class Historico {
        public List<String> labels;
        public List<Integer> valores;
        public Historico() {}
        public Historico(List<String> labels, List<Integer> valores) { this.labels = labels; this.valores = valores; }
    }

    public static class Capacidade {
        public int ativos, totalSuportado;
        public Capacidade() {}
        public Capacidade(int ativos, int totalSuportado) { this.ativos = ativos; this.totalSuportado = totalSuportado; }
    }

    public static class SaudeBateria {
        public int saudavel, atencao, critico;
        public SaudeBateria() {}
        public SaudeBateria(int s, int a, int c) { this.saudavel = s; this.atencao = a; this.critico = c; }
    }

    public static class AlertaTriagem {
        public int id, idModelo;
        public String idDispositivo, tipo, texto, tempo, metrica, valor;
        public AlertaTriagem() {}
        public AlertaTriagem(int id, int idModelo, String uuid, String tipo, String texto, String tempo, String metrica, String valor) {
            this.id = id; this.idModelo = idModelo; this.idDispositivo = uuid; this.tipo = tipo;
            this.texto = texto; this.tempo = tempo; this.metrica = metrica; this.valor = valor;
        }
    }
}