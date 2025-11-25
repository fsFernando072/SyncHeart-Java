package school.sptech;

import java.util.ArrayList;
import java.util.List;

public class Clinica {
    private Integer id;
    private String nome;
    private List<Log> listaLogs;

    public Clinica(Integer id, String nome) {
        this.id = id;
        this.nome = nome;
        this.listaLogs = new ArrayList<>();
    }

    public Clinica() {
        this.listaLogs = new ArrayList<>();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<Log> getListaLogs() {
        return listaLogs;
    }

    public void setListaLogs(List<Log> listaLogs) {
        this.listaLogs = listaLogs;
    }
}
