package syncheart.sptech;

public class Ordenacao {
    public static void SelectionSort(int[] v){
        for(int i = 0; i < v.length-1; i ++){
            for(int j = i+1; j < v.length; j++){
                if(v[j] < v[i]){
                    //Trocar instruções
                    //Toda vez que encontra algum valor menor do qu está no i, ele troca
                    int aux = v[i];
                    v[i] = v[j];
                    v[j] = aux;
                }
            }
        }
    }

    public static void SelectionSortOtimizado(int[] v){
        int indiceMenor;

        for(int i = 0; i < v.length-1; i++){
            indiceMenor = i;

            //Diferente do outro que troca, este vê qual é menor e coloca em uma variável que será
            //Atribuída ao vetor apenas no final
            for(int j = i+1; j < v.length; j++){
                if(v[j] < v[indiceMenor]){
                    indiceMenor = j;
                }
            }
            //Troca vetor na posição i com vetor na posição indiceMenor
            if(i != indiceMenor){
                int aux = v[i];
                v[i] = v[indiceMenor];
                v[indiceMenor] = aux;
            }
        }
    }

    int pesqBin(int x, int[] v){
        int inicio = 0;
        int fim = v.length-1;
        int meio = (inicio+fim)/2;

        while(inicio<= fim){
            if(x == v[meio]){
                return meio;
            }else {
                if(x > v[meio]){
                    inicio = meio+1;
                }else{
                    fim = meio-1;
                }
            }
        }
        return -1;
    }

    public static void bubbleSort(int[] v){
        for(int i = 0; i < v.length-1;i++){
            for(int j = 1; j < v.length-i;j++){
                if(v[j - 1] > v[j]){
                    Integer aux = v[j];
                    v[j] = v[j-1];
                    v[j -1] = aux;
                }
            }
        }
    }

    public static void main(String[] args) {
        int[] vetor = {6, 5, 4, 3, 2, 1};
        SelectionSort(vetor);

        for (int num : vetor){
            System.out.println(num);
        }

        System.out.println("_______________________");

        int[] vetor2 = {10,9,8,7,6,5,4,3,2,1};
        SelectionSortOtimizado(vetor2);

        for(int num : vetor2){
            System.out.println(num);
        }

        System.out.println("_______________________");

        int[] vetor3 = {1,4,6,8,2,9};
        bubbleSort(vetor3);

        for(int num : vetor3){
            System.out.println(num);
        }
    }
}
