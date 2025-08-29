package syncheart.sptech;

import java.util.ArrayList;

public class TratamentoDados {
    //Atributos
    private ArrayList<Double> cpu;
    private ArrayList<Double> ram;
    private ArrayList<Double> disco;
    Marcapasso marcapasso = new Marcapasso();

    //Métodos Setter no Construtor diretamente
    public TratamentoDados(){
        ColetaDeDados dados = new ColetaDeDados();
        this.cpu = dados.getCpu();
        this.ram = dados.getRam();
        this.disco = dados.getDisco();
    }

    //Tratamento de CPU
    public void alertasCPU(){
        for (Integer i = 0; i < cpu.toArray().length; i++) {
            String modelo = marcapasso.getModelos()[i];

            if (cpu.indexOf(i) <= 0.25) {
                System.out.println("CPU inativa!!! No modelo: " + modelo);
            } else if (cpu.indexOf(i) >= 20) {
                System.out.println("CPU em Sobrecarga!!! No modelo: " + modelo);
            }
        }
    }

    //Tratamento de RAM
    public void alertasRAM(){
        for (Integer i = 0; i < ram.toArray().length; i++) {
            String modelo = marcapasso.getModelos()[i];

            if (ram.indexOf(i) >= 30) {
                System.out.println("Uso alto de RAM!!! No modelo: " + modelo);
            }
        }
    }

    //Tratamento de Disco
    public void alertasDisco(){
        for (Integer i = 0; i < disco.toArray().length; i++) {
            String modelo = marcapasso.getModelos()[i];

            if (disco.indexOf(i) >= 60) {
                System.out.println("Disco em 60%! No modelo: " + modelo);
            } else if (disco.indexOf(i) >= 80) {
                System.out.println("Disco em 80%, risco de perda de dados!!! No modelo: " + modelo);
            }
        }
    }
}