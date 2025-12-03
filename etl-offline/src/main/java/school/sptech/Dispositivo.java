package school.sptech;

import java.time.LocalDateTime;

public class Dispositivo {
    private Integer id;
    private Integer modelo_id;
    private String uuid;
    private LocalDateTime ultima_atualizacao;

    public Dispositivo() {
    }

    public Dispositivo(Integer id, Integer modelo_id, String uuid, LocalDateTime ultima_atualizacao) {
        this.id = id;
        this.modelo_id = modelo_id;
        this.uuid = uuid;
        this.ultima_atualizacao = ultima_atualizacao;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getModelo_id() {
        return modelo_id;
    }

    public void setModelo_id(Integer modelo_id) {
        this.modelo_id = modelo_id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public LocalDateTime getUltima_atualizacao() {
        return ultima_atualizacao;
    }

    public void setUltima_atualizacao(LocalDateTime ultima_atualizacao) {
        this.ultima_atualizacao = ultima_atualizacao;
    }
}
