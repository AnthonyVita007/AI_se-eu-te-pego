package scr;

public class Utilities {
    public static void printMatrice(double[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            // Stampa il prefisso "row" seguito dal numero di riga con larghezza fissa (es. row0, row10...)
            System.out.printf("row%-3d ", i);
            for (int j = 0; j < matrix[i].length; j++) {
                // Stampa ciascun numero con due cifre decimali, larghezza 5 e virgola come separatore decimale
                System.out.printf("%5s", String.format("%.2f", matrix[i][j]).replace('.', ','));
                System.out.print("  ");
            }
            System.out.println();
        }
    }

    public static void printVettore(double[] vector) {
        for (int i = 0; i < vector.length; i++) {
            System.out.printf("%5s  ", String.format("%.2f", vector[i]).replace('.', ','));
        }
        System.out.println();
    }
}
