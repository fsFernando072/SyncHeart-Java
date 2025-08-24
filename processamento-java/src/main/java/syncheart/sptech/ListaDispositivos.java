package syncheart.sptech;

import java.util.ArrayList;
import java.util.Scanner;

public class ListaDispositivos {
    public static ArrayList<String> exibirDispositivos(){
        MarcaPasso dispositivo = new MarcaPasso();
        String valor = "";
        ArrayList<String> dispositivos = new ArrayList<>();

        for(int i = 0; i < dispositivo.modelos.length; i++){
            valor = "Modelo: "+dispositivo.modelos[i] + " CPU: " +dispositivo.cpu[i].toString() + " RAM: "+
                    dispositivo.ram[i].toString() + " Bateria: "+dispositivo.bateria[i].toString() + " Disco: "+
                    dispositivo.disco[i].toString();
            dispositivos.add(valor);
        }
        return dispositivos;
    }
    public static void main(String[] args) {
        Scanner leitor = new Scanner(System.in);
        System.out.println("""
                1 - Verificar Todos os Dispositivos
                2 - Verificar um Dispositivo
                3 - Sair
                """);
        System.out.println("Digite o que Gostaria:");
        Integer resposta = leitor.nextInt();

        if(resposta == 1){
            for(int i = 0; i < exibirDispositivos().size(); i++){
                System.out.println(exibirDispositivos().get(i));
            }
        } else if (resposta == 2) {
            System.out.println("Digite o Número do Dispositivo:");
            Integer resposta2 = leitor.nextInt();
            System.out.println(exibirDispositivos().get(resposta2));
        }else{
            System.out.println("Saindo...");
        }
    }
}
