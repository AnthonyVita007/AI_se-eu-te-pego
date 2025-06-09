package scr;
import java.util.*;

import static java.lang.Math.*;

public class KnnController extends Controller{
    //ATTRIBUTI
    private double[][] featuresDataset;
    private double[][] actionsDataset;
    // Attributi per la normalizzazione
    private static final double[] array_valori_min = {1.34, 1.89, 4.94, 2.58, 1.87, 2.56, 1.63, 3.33, 2.24};
    private static final double[] array_valori_max = {99.50, 99.41, 98.93, 99.12, 98.58, 99.15, 93.50, 99.87, 98.40};

    //---------------------------------------------------------------------------------------------------------
    //COSTRUTTORE
    public KnnController(){
        super();
    }

    //---------------------------------------------------------------------------------------------------------
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

    public double[] normalizeVector(double[] v) {
        double[] normalized = new double[v.length];
        for (int i = 0; i < v.length; i++) {
            double x = v[i];
            double x_min = array_valori_min[i];
            double x_max = array_valori_max[i];
            if (x_max != x_min) {
                normalized[i] = (x - x_min) / (x_max - x_min);
            } else {
                normalized[i] = 0;
            }
        }
        return normalized;
    }

    public void normalizeFeaturesDataset() {
        for (int i = 0; i < featuresDataset.length; i++) {
            featuresDataset[i] = normalizeVector(featuresDataset[i]);
        }
    }

    //---------------------------------------------------------------------------------------------------------
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
        Utilities.printMatrice(featuresDataset);
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
        Utilities.printMatrice(actionsDataset);
    }

    //---------------------------------------------------------------------------------------------------------
    //METODI CALCOLO DISTANZA EUCLIDEA TRA VETTORI E NEAREST-NEIGHBOR
    public double calculateEuclideanDistance_v1v2(double[] v1, double[] v2) {
        double sum = 0;
        for (int i = 0; i < v1.length; i++) {
            double diff = v1[i] - v2[i];
            sum += diff * diff;
        }
        return sqrt(sum);
    }

    public int findNearestNeighborIndex(double[] torcsFeatureVector) {
        int nearestIndex = -1;
        double minDistance = Double.MAX_VALUE;
        for (int i = 0; i < featuresDataset.length; i++) {
            double distance = calculateEuclideanDistance_v1v2(torcsFeatureVector, featuresDataset[i]);
            if (distance < minDistance) {
                minDistance = distance;
                nearestIndex = i;
            }
        }
        return nearestIndex;
    }

    //---------------------------------------------------------------------------------------------------------
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

    //---------------------------------------------------------------------------------------------------------
    //GETTER
    public double[][] getFeaturesDataset() {
        return featuresDataset;
    }

    public double[][] getActionsDataset() {
        return actionsDataset;
    }
}