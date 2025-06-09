package scr;
import java.util.*;

public class KnnController extends Controller{
    //ATTRIBUTI
    private double[][] featuresDataset;
    private double[][] actionsDataset;

    //COSTRUTTORE
    public KnnController(){
        super();
    }

    //METODI CREAZIONE E NORMALIZZAZIONE DATASET
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

    public void normalizeFeaturesDataset() {
        // Dichiarazione degli array contenenti i valori minimi e massimi per ogni feature
        double[] array_valori_min = {1.34, 1.89, 4.94, 2.58, 1.87, 2.56, 1.63, 3.33, 2.24}; // array contenente i valori minimi per ogni feature
        double[] array_valori_max = {99.50, 99.41, 98.93, 99.12, 98.58, 99.15, 93.50, 99.87, 98.40};  // array contenente i valori massimi per ogni feature

        // Normalizzazione di ogni feature utilizzando la formula: x_norm = (x-x_min)/(x_max - x_min)
        for (int i = 0; i < featuresDataset.length; i++) {           // per ogni vettore di feature
            for (int j = 0; j < featuresDataset[i].length; j++) {    // per ogni feature nel vettore
                double x = featuresDataset[i][j];
                double x_min = array_valori_min[j];
                double x_max = array_valori_max[j];

                // Applicazione della formula di normalizzazione
                // Controllo per evitare divisione per zero
                if (x_max != x_min) {
                    featuresDataset[i][j] = (x - x_min) / (x_max - x_min);
                } else {
                    featuresDataset[i][j] = 0; // caso in cui max = min, impostiamo il valore a 0
                }
            }
        }
    }

    //METODI PER LA STAMPA DEI DATASET
    public void printFeaturesDataset() {
        System.out.println("\n=== FEATURES DATASET ===");
        System.out.println("Dimensioni: " + featuresDataset.length + " vettori_di_features × " + featuresDataset[0].length + " num_features\n");

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
        System.out.println("Dimensioni: " + actionsDataset.length + " vettori_di_actions × " + actionsDataset[0].length + " num_actions\n");

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