package school.sptech;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Log {
    private LocalDateTime timestamp;
    private String uuid;
    private Boolean arritmia;
    private Double valorCpu;
    private Double valorRam;
    private Double valorDisco;
    private Double valorBateria;
    private Integer totalTarefas;
    private List<String> listaTarefas;

    public Log(LocalDateTime timestamp, String uuid, Boolean arritmia, Double valorCpu, Double valorRam, Double valorDisco,
               Double valorBateria, Integer totalTarefas) {
        this.timestamp = timestamp;
        this.uuid = uuid;
        this.arritmia = arritmia;
        this.valorCpu = valorCpu;
        this.valorRam = valorRam;
        this.valorDisco = valorDisco;
        this.valorBateria = valorBateria;
        this.totalTarefas = totalTarefas;
        listaTarefas = new ArrayList<>();
    }

    public void adicionarTarefa(String tarefas) {
        listaTarefas.add(tarefas);
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Boolean getArritmia() {
        return arritmia;
    }

    public void setArritmia(Boolean arritmia) {
        this.arritmia = arritmia;
    }

    public Double getValorCpu() {
        return valorCpu;
    }

    public void setValorCpu(Double valorCpu) {
        this.valorCpu = valorCpu;
    }

    public Double getValorRam() {
        return valorRam;
    }

    public void setValorRam(Double valorRam) {
        this.valorRam = valorRam;
    }

    public Double getValorDisco() {
        return valorDisco;
    }

    public void setValorDisco(Double valorDisco) {
        this.valorDisco = valorDisco;
    }

    public Double getValorBateria() {
        return valorBateria;
    }

    public void setValorBateria(Double valorBateria) {
        this.valorBateria = valorBateria;
    }

    public Integer getTotalTarefas() {
        return totalTarefas;
    }

    public void setTotalTarefas(Integer totalTarefas) {
        this.totalTarefas = totalTarefas;
    }

    public List<String> getListaTarefas() {
        return listaTarefas;
    }

    public void setListaTarefas(List<String> listaTarefas) {
        this.listaTarefas = listaTarefas;
    }

    @Override
    public String toString() {
        return "school.sptech.Log{" +
                "timestamp=" + timestamp +
                ", uuid='" + uuid + '\'' +
                ", arritmia=" + arritmia +
                ", valorCpu=" + valorCpu +
                ", valorRam=" + valorRam +
                ", valorDisco=" + valorDisco +
                ", valorBateria=" + valorBateria +
                ", totalTarefas=" + totalTarefas +
                ", listaTarefas=" + listaTarefas +
                '}';
    }
}
