package syncheart.sptech;

import jdk.swing.interop.SwingInterOpUtils;

import java.util.Scanner;

public class TesteLogs {
    public static void main(String[] args) {
        Scanner leitor = new Scanner(System.in);
        Scanner leitorString = new Scanner(System.in);
        Boolean sair = false;
        TratamentoDados tratamento = new TratamentoDados();
        while (!sair) {
            System.out.println("Escolha uma opção abaixo:");
            int opcao = leitor.nextInt();

            if (opcao == 1) {
                System.out.println(tratamento.getAlertaCPU());
                System.out.println(tratamento.getAlertaRAM());
                System.out.println(tratamento.getAlertaDisco());
            }
            else if (opcao == 2) {
                System.out.println("Escolha uma opção abaixo:");
                String grauEscolhido = leitorString.nextLine();
                String escolhaFormatada = grauEscolhido.toUpperCase();
                if (escolhaFormatada.equals("A")) {
                    for (int i = 0; i < tratamento.getAlertaCPU().toArray().length; i++) {
                        if (tratamento.getAlertaCPU().get(i).charAt(0) == 'A'){
                            System.out.println(tratamento.getAlertaCPU().get(i));
                        }
                    }
                    for (int i = 0; i < tratamento.getAlertaRAM().toArray().length; i++) {
                        if (tratamento.getAlertaRAM().get(i).charAt(0) == 'A'){
                            System.out.println(tratamento.getAlertaRAM().get(i));
                        }
                    }
                    for (int i = 0; i < tratamento.getAlertaDisco().toArray().length; i++) {
                        if (tratamento.getAlertaDisco().get(i).charAt(0) == 'A'){
                            System.out.println(tratamento.getAlertaDisco().get(i));
                        }
                    }
                }
                else if (escolhaFormatada.equals("B")) {
                    for (int i = 0; i < tratamento.getAlertaCPU().toArray().length; i++) {
                        if (tratamento.getAlertaCPU().get(i).charAt(0) == 'B'){
                            System.out.println(tratamento.getAlertaCPU().get(i));
                        }
                    }
                    for (int i = 0; i < tratamento.getAlertaRAM().toArray().length; i++) {
                        if (tratamento.getAlertaRAM().get(i).charAt(0) == 'B'){
                            System.out.println(tratamento.getAlertaRAM().get(i));
                        }
                    }
                    for (int i = 0; i < tratamento.getAlertaDisco().toArray().length; i++) {
                        if (tratamento.getAlertaDisco().get(i).charAt(0) == 'B'){
                            System.out.println(tratamento.getAlertaDisco().get(i));
                        }
                    }
                }
                else if (escolhaFormatada.equals("C")) {
                    for (int i = 0; i < tratamento.getAlertaCPU().toArray().length; i++) {
                        if (tratamento.getAlertaCPU().get(i).charAt(0) == 'C'){
                            System.out.println(tratamento.getAlertaCPU().get(i));
                        }
                    }
                    for (int i = 0; i < tratamento.getAlertaRAM().toArray().length; i++) {
                        if (tratamento.getAlertaRAM().get(i).charAt(0) == 'C'){
                            System.out.println(tratamento.getAlertaRAM().get(i));
                        }
                    }
                    for (int i = 0; i < tratamento.getAlertaDisco().toArray().length; i++) {
                        if (tratamento.getAlertaDisco().get(i).charAt(0) == 'C'){
                            System.out.println(tratamento.getAlertaDisco().get(i));
                        }
                    }
                }
                else {
                    System.out.println("Grau inválido");
                }
            }
            if (opcao == 3) {
                for (int i = 0; i < tratamento.getAlertaCritico().toArray().length; i++) {
                    System.out.println(tratamento.getAlertaCritico().get(i));
                }
            }
            if (opcao == 4) {
                sair = true;
            }
        }
    }
}