package school.sptech;

public class Modelo {
    private Integer id;
    private String nome;

    public Modelo(Integer id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Modelo() {

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

    @Override
    public String toString() {
        return "Modelo{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                '}';
    }
}
