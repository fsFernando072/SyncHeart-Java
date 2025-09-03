package syncheart.sptech;

public class TesteLogs {
    public static void main(String[] args) {
        ColetaDeDados dados = new ColetaDeDados();
        System.out.println("CPU: "+dados.getCpu());
        System.out.println("DISCO: "+dados.getDisco());
        System.out.println("RAM: "+dados.getRam());

        TratamentoDados tramento = new TratamentoDados();
        tramento.alertasCPU();
        tramento.alertasRAM();
        tramento.alertasDisco();
    }
}
