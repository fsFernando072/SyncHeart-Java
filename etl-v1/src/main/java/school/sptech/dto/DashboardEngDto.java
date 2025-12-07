package school.sptech.dto;

import java.util.ArrayList;
import java.util.List;

public class DashboardEngDto {
    public KpiEng kpiEng;
    public DashBateria dashBateria;
    public DashCpuRam dashCpuRam;
    public DashDisco dashDisco;

    public DashboardEngDto() {}

    public static class KpiEng {
        public Double valorBateria;
        public Double valorCpu;
        public Double valorRam;
        public Double valorDisco;
        public KpiEng(){}
        public KpiEng(Double b, Double c, Double r, Double d) {this.valorBateria = b; this.valorCpu = c; this.valorRam = r; this.valorDisco = d;}
    }

    public static class DashBateria {
        public List<Integer> valores;
        public List<Integer> projecao;
        public List<String> labels;
        public DashBateria(){this.valores = new ArrayList<>(); this.projecao = new ArrayList<>(); this.labels = new ArrayList<>();}
    }

    public static class DashCpuRam {
        public List<Double> cpu;
        public List<Double> ram;
        public List<String> labels;
        public DashCpuRam(){this.cpu = new ArrayList<>(); this.ram = new ArrayList<>(); this.labels = new ArrayList<>();}
    }

    public static class DashDisco {
        public List<Double> disco;
        public List<String> labels;
        public DashDisco(){this.disco = new ArrayList<>(); this.labels = new ArrayList<>();}
    }
}
