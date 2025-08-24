package syncheart.sptech;

import java.util.ArrayList;

public class TratamentoDados {
    public static ArrayList<String> alertas(){
        MarcaPasso dispositivos = new MarcaPasso();
        ArrayList<String> listaAlertas = new ArrayList<>();

        //For geral
            //For para verificar CPU
            for(int c = 0; c < dispositivos.cpu.length; c++){
                if (dispositivos.cpu[c] > 15){
                    listaAlertas.add(String.format("O Dispositivo %s está com o Consumo de CPU em %.1f %%",
                            dispositivos.modelos[c],dispositivos.cpu[c]));
                }
            }

            //For para verificar RAM
            for(int r = 0; r < dispositivos.ram.length; r++){
                if (dispositivos.ram[r] > 25){
                    listaAlertas.add(String.format("O Dispositivo %s está com o Consumo de RAM em %.1f %%",
                            dispositivos.modelos[r],dispositivos.ram[r]));
                }
            }

            //For para verificar Bateria
            for(int b = 0; b < dispositivos.bateria.length; b++){
                if (dispositivos.bateria[b] < 60){
                    listaAlertas.add(String.format("O Dispositivo %s está com a Bateria em %.1f %%",
                            dispositivos.modelos[b],dispositivos.bateria[b]));
                }
            }

            //For para verificar Disco
            for(int d = 0; d < dispositivos.disco.length; d++){
                if (dispositivos.disco[d] > 60){
                    listaAlertas.add(String.format("O Dispositivo %s está com o Consumo de Disco em %.1f %%",
                            dispositivos.modelos[d],dispositivos.disco[d]));
                }
        }
        return listaAlertas;
    }

    public static void main(String[] args) {
        for (int i = 0; i < alertas().size(); i++){
            System.out.println(alertas().get(i));
        }
        if(alertas().size() > 0){
            System.out.println("Contacte Urgentemente o Hospital sob os Portadores dos Modelos Listados!");
        }
    }
}
