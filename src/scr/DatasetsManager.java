package scr;
import java.util.ArrayList;

public class DatasetsManager {
    // ATTRIBUTI
    private static double[][] featuresDataset;
    private static double[][] actionsDataset;
    // Attributi per la normalizzazione
    private static double[] array_valori_min;
    private static double[] array_valori_max;

    //---------------------------------------------------------------------------------------------------------
    // METODI CREAZIONE DATASET
    public static void createFeaturesDataset(ArrayList<ArrayList<Double>> featureVectors){
        int numRows = featureVectors.size();
        int numCols = featureVectors.get(0).size(); // assumiamo che tutti i vettori abbiano la stessa dimensione
        featuresDataset = new double[numRows][numCols];
        for (int i = 0; i < numRows; i++) {
            ArrayList<Double> currentVector = featureVectors.get(i);
            for (int j = 0; j < numCols; j++) {
                featuresDataset[i][j] = currentVector.get(j);
            }
        }
    }

    public static void createActionsDataset(ArrayList<ArrayList<Double>> actionVectors){
        int numRows = actionVectors.size();
        int numCols = actionVectors.get(0).size();
        actionsDataset = new double[numRows][numCols];
        for (int i = 0; i < numRows; i++) {
            ArrayList<Double> currentVector = actionVectors.get(i);
            for (int j = 0; j < numCols; j++) {
                actionsDataset[i][j] = currentVector.get(j);
            }
        }
    }

    //---------------------------------------------------------------------------------------------------------
    // METODI NORMALIZZAZIONE DATASET

    public static double[] normalizeFeatureVector(double[] v) {
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

    public static void normalizeFeaturesDataset() {
        for (int i = 0; i < featuresDataset.length; i++) {
            featuresDataset[i] = normalizeFeatureVector(featuresDataset[i]);
        }
    }

    public static double[] getFeatureMaxValues() {
        if (featuresDataset == null || featuresDataset.length == 0) return null;
        int numFeatures = featuresDataset[0].length;
        double[] maxValues = new double[numFeatures];
        // Inizializza i massimi con il primo vettore
        for (int j = 0; j < numFeatures; j++) {
            maxValues[j] = featuresDataset[0][j];
        }
        // Scorre il dataset
        for (int i = 1; i < featuresDataset.length; i++) {
            for (int j = 0; j < numFeatures; j++) {
                if (featuresDataset[i][j] > maxValues[j]) {
                    maxValues[j] = featuresDataset[i][j];
                }
            }
        }
        return maxValues;
    }

    public static double[] getFeatureMinValues() {
        if (featuresDataset == null || featuresDataset.length == 0) return null;
        int numFeatures = featuresDataset[0].length;
        double[] minValues = new double[numFeatures];
        // Inizializza i minimi con il primo vettore
        for (int j = 0; j < numFeatures; j++) {
            minValues[j] = featuresDataset[0][j];
        }
        // Scorri il dataset
        for (int i = 1; i < featuresDataset.length; i++) {
            for (int j = 0; j < numFeatures; j++) {
                if (featuresDataset[i][j] < minValues[j]) {
                    minValues[j] = featuresDataset[i][j];
                }
            }
        }
        return minValues;
    }

    public static void defineMaxMinForNormalization(double[] array_valori_max, double[] array_valori_min) {
        DatasetsManager.array_valori_max = new double[array_valori_max.length];
        DatasetsManager.array_valori_min = new double[array_valori_min.length];

        for (int i = 0; i < DatasetsManager.array_valori_min.length; i++) {
            DatasetsManager.array_valori_min[i] = array_valori_min[i];
            DatasetsManager.array_valori_max[i] = array_valori_max[i];
        }
    }

    //---------------------------------------------------------------------------------------------------------
    // METODI PER LA STAMPA DEI DATASET
    public static void printFeaturesDataset() {
        System.out.println("\n=== FEATURES DATASET ===");
        System.out.println("Dimensioni: " + featuresDataset.length + " vettori_di_features × " + featuresDataset[0].length + " num_features\n");
        System.out.print("       ");
        for (int j = 0; j < featuresDataset[0].length; j++) {
            System.out.printf("col%-4d ", j);
        }
        System.out.println("\n");
        Utilities.printMatrice(featuresDataset);
    }

    public static void printActionsDataset() {
        System.out.println("\n=== ACTIONS DATASET ===");
        System.out.println("Dimensioni: " + actionsDataset.length + " vettori_di_actions × " + actionsDataset[0].length + " num_actions\n");
        System.out.print("       ");
        for (int j = 0; j < actionsDataset[0].length; j++) {
            System.out.printf("col%-4d ", j);
        }
        System.out.println("\n");
        Utilities.printMatrice(actionsDataset);
    }

    //---------------------------------------------------------------------------------------------------------
    // GETTER
    public static double[][] getFeaturesDataset() {
        return featuresDataset;
    }

    public static double[][] getActionsDataset() {
        return actionsDataset;
    }
}
