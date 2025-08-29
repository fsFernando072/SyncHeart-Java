package syncheart.sptech;

import java.util.ArrayList;

public class ColetaDeDados {
    //Atributos
    private ArrayList<Double> cpu;
    private ArrayList<Double> ram;
    private ArrayList<Double> disco;

    //Métodos Setter no Construtor diretamente
    public ColetaDeDados() {
        this.cpu = capturaCpu();
        this.ram = capturaRam();
        this.disco = capturaDisco();
    }

    public ArrayList<Double> getCpu() {
        return cpu;
    }
    public ArrayList<Double> getRam() {
        return ram;
    }
    public ArrayList<Double> getDisco() {
        return disco;
    }

    //Métodos de Geração de Dados
    public ArrayList<Double> capturaCpu(){
        Marcapasso marcapasso = new Marcapasso();
        Integer totalModelos = marcapasso.getModelos().length;
        ArrayList<Double> cpu = new ArrayList<>();

        for(Integer i = 0; i < totalModelos; i++){
            cpu.add((double)((int)(Math.random() * 30)+1));
        }
        return cpu;
    }
    public ArrayList<Double> capturaRam(){
        Marcapasso marcapasso = new Marcapasso();
        Integer totalModelos = marcapasso.getModelos().length;
        ArrayList<Double> ram = new ArrayList<>();

        for(Integer i = 0; i < totalModelos; i++){
            ram.add((double)((int)(Math.random() * 50)+1));
        }
        return ram;
    }
    public ArrayList<Double> capturaDisco(){
        Marcapasso marcapasso = new Marcapasso();
        Integer totalModelos = marcapasso.getModelos().length;
        ArrayList<Double> disco = new ArrayList<>();

        for(Integer i = 0; i < totalModelos; i++){
            disco.add((double)((int)(Math.random() * 70)+1));
        }
        return disco;
    }
}
