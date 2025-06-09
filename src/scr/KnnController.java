package scr;
import java.lang.reflect.Array;
import java.util.*;

public class KnnController extends Controller{
    //ATTRIBUTI
    private double[][] featuresDataset;
    private double[][] actionsDataset;

    //COSTRUTTORE
    public KnnController() {
        super();
    }

    //METODI CREAZIONE DATASET
    public void createFeaturesDataset(ArrayList<ArrayList<Double>> featureVectors){
        // Inizializza la matrice con le dimensioni appropriate:
        // - numero di righe = numero di feature vectors
        // - numero di colonne = dimensione di ogni feature vector
        int numRows = featureVectors.size();
        int numCols = featureVectors.get(0).size(); // assumiamo che tutti i vettori abbiano la stessa dimensione

        // Crea la matrice
        featuresDataset = new double[numRows][numCols];

        // Copia i dati dalla ArrayList alla matrice
        for (int i = 0; i < numRows; i++) {
            ArrayList<Double> currentVector = featureVectors.get(i);
            for (int j = 0; j < numCols; j++) {
                featuresDataset[i][j] = currentVector.get(j);
            }
        }
    }

    public void createActionsDataset(ArrayList<ArrayList<Double>> actionVectors){
        // Inizializza la matrice con le dimensioni appropriate:
        // - numero di righe = numero di action vectors
        // - numero di colonne = dimensione di ogni action vector
        int numRows = actionVectors.size();
        int numCols = actionVectors.get(0).size(); // assumiamo che tutti i vettori abbiano la stessa dimensione

        // Crea la matrice
        actionsDataset = new double[numRows][numCols];

        // Copia i dati dalla ArrayList alla matrice
        for (int i = 0; i < numRows; i++) {
            ArrayList<Double> currentVector = actionVectors.get(i);
            for (int j = 0; j < numCols; j++) {
                actionsDataset[i][j] = currentVector.get(j);
            }
        }
    }

    //METODI PER LA STAMPA DEI DATASET

    public void printFeaturesDataset() {
        System.out.println("\n=== FEATURES DATASET ===");
        System.out.println("Dimensioni: " + featuresDataset.length + " righe × " + featuresDataset[0].length + " colonne\n");

        // Stampa gli indici delle colonne
        System.out.print("       "); // Spazio per l'indice della riga
        for (int j = 0; j < featuresDataset[0].length; j++) {
            System.out.printf("col%-4d ", j);
        }
        System.out.println("\n");

        // Stampa la matrice riga per riga
        for (int i = 0; i < featuresDataset.length; i++) {
            System.out.printf("row%-3d ", i); // Indice della riga
            for (int j = 0; j < featuresDataset[i].length; j++) {
                System.out.printf("%.2f  ", featuresDataset[i][j]);
            }
            System.out.println(); // Nuova riga
        }
    }

    public void printActionsDataset() {
        System.out.println("\n=== ACTIONS DATASET ===");
        System.out.println("Dimensioni: " + actionsDataset.length + " righe × " + actionsDataset[0].length + " colonne\n");

        // Stampa gli indici delle colonne
        System.out.print("       "); // Spazio per l'indice della riga
        for (int j = 0; j < actionsDataset[0].length; j++) {
            System.out.printf("col%-4d ", j);
        }
        System.out.println("\n");

        // Stampa la matrice riga per riga
        for (int i = 0; i < actionsDataset.length; i++) {
            System.out.printf("row%-3d ", i); // Indice della riga
            for (int j = 0; j < actionsDataset[i].length; j++) {
                System.out.printf("%.2f  ", actionsDataset[i][j]);
            }
            System.out.println(); // Nuova riga
        }
    }

    //METODI EREDITATI DA "Controller" DI CUI SI DEVE FARE OVERRIDE
    @Override
    public Action control(SensorModel sensors) {
        // Da implementare
        return null;
    }

    @Override
    public void reset() {
        // Da implementare
    }

    @Override
    public void shutdown() {
        // Da implementare
    }
}