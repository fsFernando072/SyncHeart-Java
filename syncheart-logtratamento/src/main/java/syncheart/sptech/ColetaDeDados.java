package syncheart.sptech;

import java.util.ArrayList;

public class ColetaDeDados {
    private ArrayList<Double> cpu;
    private ArrayList<Double> ram;
    private ArrayList<Double> disco;
    private String[] usuario;

    //Construtor
    public ColetaDeDados() {
        this.usuario = setUsuario();
        this.cpu = setCpu();
        this.ram = setRam();
        this.disco = setDisco();
    }

    //Getter
    public ArrayList<Double> getCpu() {
        return cpu;
    }
    public ArrayList<Double> getRam() {
        return ram;
    }
    public ArrayList<Double> getDisco() {
        return disco;
    }
    public String[] getUsuario() {
        return usuario;
    }

    //Métodos de Números Aleatórios
    public ArrayList<Double> setCpu(){
        ArrayList<Double> cpuPreenchimento = new ArrayList<>();
        for(int i = 0; i < usuario.length; i++){
            Double numeroAleatorio = (double)((int)(Math.random()*100));
            cpuPreenchimento.add(numeroAleatorio);
        }
        return cpuPreenchimento;
    }

    public ArrayList<Double> setRam(){
        ArrayList<Double> ramPreenchimento = new ArrayList<>();
        for(int i = 0; i < usuario.length; i++){
            Double numeroAleatorio = (double)((int)(Math.random()*100));
            ramPreenchimento.add(numeroAleatorio);
        }
        return ramPreenchimento;
    }

    public ArrayList<Double> setDisco(){
        ArrayList<Double> discoPreenchimento = new ArrayList<>();
        for(int i = 0; i < usuario.length; i++){
            Double numeroAleatorio = (double)((int)(Math.random()*100));
            discoPreenchimento.add(numeroAleatorio);
        }
        return discoPreenchimento;
    }

    public String[] setUsuario(){
        String[] usuarios = {"Guilherme Silva","Matheus Nascimento",
        "Sérgio Vinícius","Guilherme Barros","Gabriel Castilho","Carlos José","Ana Maria",
        "João Pedro","Miguel Pinto","Carla Peres","Bianca Lopes","Fernando Brandinho"};
        return usuarios;
    }
}