package school.sptech;

import java.util.ArrayList;
import java.util.List;

public class Clinica {
    private Integer id;
    private String nome;
    private List<Dispositivo> listaDispositivos;

    public Clinica(Integer id, String nome) {
        this.id = id;
        this.nome = nome;
        this.listaDispositivos = new ArrayList<>();
    }

    public Clinica() {
        this.listaDispositivos = new ArrayList<>();
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

    public List<Dispositivo> getListaDispositivos() {
        return listaDispositivos;
    }

    public void setListaDispositivos(List<Dispositivo> listaDispositivos) {
        this.listaDispositivos = listaDispositivos;
    }
}
