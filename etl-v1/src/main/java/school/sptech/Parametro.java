package school.sptech;

public class Parametro {
    private String metrica;
    private String condicao;
    private Double limiarValor;
    private Integer duracaoMinutos;

    public Parametro(String metrica, String condicao, Double limiarValor, Integer duracaoMinutos) {
        this.metrica = metrica;
        this.condicao = condicao;
        this.limiarValor = limiarValor;
        this.duracaoMinutos = duracaoMinutos;
    }

    public Parametro() {

    }

    public String getMetrica() {
        return metrica;
    }

    public void setMetrica(String metrica) {
        this.metrica = metrica;
    }

    public String getCondicao() {
        return condicao;
    }

    public void setCondicao(String condicao) {
        this.condicao = condicao;
    }

    public Double getLimiarValor() {
        return limiarValor;
    }

    public void setLimiarValor(Double limiarValor) {
        this.limiarValor = limiarValor;
    }

    public Integer getDuracaoMinutos() {
        return duracaoMinutos;
    }

    public void setDuracaoMinutos(Integer duracaoMinutos) {
        this.duracaoMinutos = duracaoMinutos;
    }
}
