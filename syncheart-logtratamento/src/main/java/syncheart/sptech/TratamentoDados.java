package syncheart.sptech;

import java.util.ArrayList;
import java.util.Collections;

public class TratamentoDados {
    private ArrayList<String> alertaCPU;
    private ArrayList<String> alertaRAM;
    private ArrayList<String> alertaDisco;
    private ArrayList<String> alertaCritico;

    public ArrayList<String> getAlertaCPU() {
        return alertaCPU;
    }

    public ArrayList<String> getAlertaRAM() {
        return alertaRAM;
    }

    public ArrayList<String> getAlertaDisco() {
        return alertaDisco;
    }

    public ArrayList<String> getAlertaCritico() {
        return alertaCritico;
    }

    public TratamentoDados() {
        alertaCPU = new ArrayList<>();
        alertaRAM = new ArrayList<>();
        alertaDisco = new ArrayList<>();
        alertaCritico = new ArrayList<>();
        filtrarCPU();
        filtrarRAM();
        filtrarDisco();
        ordernarAlertas();
    }

    public void filtrarCPU() {
        ColetaDeDados dados = new ColetaDeDados();
        for (int i = 0; i < dados.getCpu().toArray().length; i++) {
            if (dados.getCpu().get(i) > 40) {
                alertaCPU.add("A - " + dados.getUsuario()[i] + " -- CPU: " + dados.getCpu().get(i));
            } else if(dados.getCpu().get(i) > 30) {
                alertaCPU.add("B - " + dados.getUsuario()[i] + " -- CPU: " + dados.getCpu().get(i));
            } else if(dados.getCpu().get(i) > 20) {
                alertaCPU.add("C - " + dados.getUsuario()[i] + " -- CPU: " + dados.getCpu().get(i));
            }
        }
    }
    public void filtrarRAM() {
        ColetaDeDados dados = new ColetaDeDados();
        for (int i = 0; i < dados.getRam().toArray().length; i++) {
            if (dados.getRam().get(i) > 70) {
                alertaRAM.add("A - " + dados.getUsuario()[i] + " -- RAM: " + dados.getRam().get(i));
            } else if(dados.getCpu().get(i) > 60) {
                alertaRAM.add("B - " + dados.getUsuario()[i] + " -- RAM: " + dados.getRam().get(i));
            } else if(dados.getCpu().get(i) > 50) {
                alertaRAM.add("C - " + dados.getUsuario()[i] + " -- RAM: " + dados.getRam().get(i));
            }
        }
    }

    public void filtrarDisco() {
        ColetaDeDados dados = new ColetaDeDados();
        for (int i = 0; i < dados.getDisco().toArray().length; i++) {
            if (dados.getDisco().get(i) > 70) {
                alertaDisco.add("A - " + dados.getUsuario()[i] + " -- Disco: " + dados.getDisco().get(i));
            } else if(dados.getCpu().get(i) > 60) {
                alertaDisco.add("B - " + dados.getUsuario()[i] + " -- Disco: " + dados.getDisco().get(i));
            } else if(dados.getCpu().get(i) > 50) {
                alertaDisco.add("C - " + dados.getUsuario()[i] + " -- Disco: " + dados.getDisco().get(i));
            }
        }
    }

    public void ordernarAlertas() {
        ColetaDeDados dados = new ColetaDeDados();
        Collections.sort(alertaCPU);
        Collections.sort(alertaRAM);
        Collections.sort(alertaDisco);
        String[] nomes = dados.getUsuario();
        int indiceMaiorCPU = 0;
        int indiceMaiorRAM = 0;
        int indiceMaiorDisco = 0;

        for(int i = 0; i < dados.getCpu().toArray().length -1; i++){
            indiceMaiorCPU = i;

            for(int j = i+1; j < dados.getCpu().toArray().length; j++){
                if(dados.getCpu().get(j) > dados.getCpu().get(indiceMaiorCPU)){
                    indiceMaiorCPU = j;
                }
            }
        }
        for(int i = 0; i < dados.getRam().toArray().length -1; i++){
            indiceMaiorRAM = i;

            for(int j = i+1; j < dados.getRam().toArray().length; j++){
                if(dados.getRam().get(j) > dados.getRam().get(indiceMaiorRAM)){
                    indiceMaiorRAM = j;
                }
            }
        }
        for(int i = 0; i < dados.getDisco().toArray().length -1; i++){
            indiceMaiorDisco = i;

            for(int j = i+1; j < dados.getDisco().toArray().length; j++){
                if(dados.getDisco().get(j) > dados.getDisco().get(indiceMaiorDisco)){
                    indiceMaiorDisco = j;
                }
            }
        }
        alertaCritico.add("Usuário: " + dados.getUsuario()[indiceMaiorCPU] + " - CPU: " + dados.getCpu().get(indiceMaiorCPU));
        alertaCritico.add("Usuário: " + dados.getUsuario()[indiceMaiorRAM] + " - RAM: " + dados.getRam().get(indiceMaiorRAM));
        alertaCritico.add("Usuário: " + dados.getUsuario()[indiceMaiorDisco] + " - Disco: " + dados.getCpu().get(indiceMaiorDisco));
    }
}