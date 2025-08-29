package syncheart.sptech;

public class TesteLogs {
    public static void main(String[] args) {
        ColetaDeDados dados = new ColetaDeDados();
        System.out.println("CPU: "+dados.getCpu());
        System.out.println("DISCO: "+dados.getDisco());
        System.out.println("RAM: "+dados.getRam());

        TratamentoDados alertas = new TratamentoDados();
        System.out.println("ALERTAS CPU: ");
        alertas.alertasCPU();

        System.out.println("ALERTAS RAM: ");
        alertas.alertasRAM();

        System.out.println("ALERTAS DISCO: ");
        alertas.alertasDisco();
    }
}
