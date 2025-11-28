package school.sptech;

import java.time.LocalDateTime;

public class Log {
    private LocalDateTime timestamp;
    private String id;
    private Boolean arritmia;
    private Double cpu;
    private Double ram;
    private Double disco;
    private Double bateria;
    private Integer tarefas;
    private String listaTarefas;

    public Log(LocalDateTime timestamp, String id, Boolean arritmia, Double cpu, Double ram, Double disco, Double bateria, Integer tarefas, String listaTarefas) {
        this.timestamp = timestamp;
        this.id = id;
        this.arritmia = arritmia;
        this.cpu = cpu;
        this.ram = ram;
        this.disco = disco;
        this.bateria = bateria;
        this.tarefas = tarefas;
        this.listaTarefas = listaTarefas;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getArritmia() {
        return arritmia;
    }

    public void setArritmia(Boolean arritmia) {
        this.arritmia = arritmia;
    }

    public Double getCpu() {
        return cpu;
    }

    public void setCpu(Double cpu) {
        this.cpu = cpu;
    }

    public Double getRam() {
        return ram;
    }

    public void setRam(Double ram) {
        this.ram = ram;
    }

    public Double getDisco() {
        return disco;
    }

    public void setDisco(Double disco) {
        this.disco = disco;
    }

    public Double getBateria() {
        return bateria;
    }

    public void setBateria(Double bateria) {
        this.bateria = bateria;
    }

    public Integer getTarefas() {
        return tarefas;
    }

    public void setTarefas(Integer tarefas) {
        this.tarefas = tarefas;
    }

    public String getListaTarefas() {
        return listaTarefas;
    }

    public void setListaTarefas(String listaTarefas) {
        this.listaTarefas = listaTarefas;
    }

    @Override
    public String toString() {
        return "Log{" +
                "timestamp='" + timestamp + '\'' +
                ", id='" + id + '\'' +
                ", arritmia=" + arritmia +
                ", cpu=" + cpu +
                ", ram=" + ram +
                ", disco=" + disco +
                ", bateria=" + bateria +
                ", tarefas=" + tarefas +
                ", listaTarefas='" + listaTarefas + '\'' +
                '}';
    }
}
