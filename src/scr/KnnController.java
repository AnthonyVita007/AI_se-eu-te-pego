package scr;
import java.util.*;

import static java.lang.Math.*;

public class KnnController extends Controller{
    //ATTRIBUTI
    private double[][] featuresDataset;
    private double[][] actionsDataset;
    // Attributi per la normalizzazione
    private static final double[] array_valori_min = {0.0,-32.5307,-0.719544,-1.0,-1.0,-1.0,-1.0,-1.0,-0.338155};
    private static final double[] array_valori_max = {282.467,31.5179,1.1072,200.0,200.0,200.0,200.0,200.0,0.350809};

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

    public double[] normalizeFeatureVector(double[] v) {
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
            featuresDataset[i] = normalizeFeatureVector(featuresDataset[i]);
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

    public int[] findKNearestNeighborIndices(double[] torcsFeatureVector, int k) {
        // Lista per accumulare distanza e indice
        List<double[]> distanceIndexList = new ArrayList<>();
        for (int i = 0; i < featuresDataset.length; i++) {
            double distance = calculateEuclideanDistance_v1v2(torcsFeatureVector, featuresDataset[i]);
            distanceIndexList.add(new double[]{distance, i});
        }
        // Ordina per distanza crescente
        distanceIndexList.sort(Comparator.comparingDouble(a -> a[0]));

        // Estrai i primi k indici
        int[] nearestIndices = new int[min(k, distanceIndexList.size())];
        for (int i = 0; i < nearestIndices.length; i++) {
            nearestIndices[i] = (int)distanceIndexList.get(i)[1];
        }
        return nearestIndices;
    }

    public double[] meanActionOfKNeighbors(int[] neighborIndices) {
        if (actionsDataset == null || neighborIndices.length == 0) return null;
        int actionLen = actionsDataset[0].length;
        double[] mean = new double[actionLen];
        for (int idx : neighborIndices) {
            for (int j = 0; j < actionLen; j++) {
                mean[j] += actionsDataset[idx][j];
            }
        }
        for (int j = 0; j < actionLen; j++) {
            mean[j] /= neighborIndices.length;
        }
        return mean;
    }

    //---------------------------------------------------------------------------------------------------------
    //METODI EREDITATI DA "Controller" DI CUI SI DEVE FARE OVERRIDE
    @Override
    public Action control(SensorModel sensors) {
        //COSTRUZIONE DEL VETTORE DELLE FEATURE PROVENIENTE DA TORCS
        double[] torcsFeatureVector = new double[9];

        torcsFeatureVector[0] = sensors.getSpeed();
        torcsFeatureVector[1] = sensors.getLateralSpeed();
        torcsFeatureVector[2] = sensors.getTrackPosition();

        double[] track = sensors.getTrackEdgeSensors();
        torcsFeatureVector[3] = track[2];
        torcsFeatureVector[4] = track[5];
        torcsFeatureVector[5] = track[8];
        torcsFeatureVector[6] = track[11];
        torcsFeatureVector[7] = track[14];

        torcsFeatureVector[8] = sensors.getAngleToTrackAxis();

        //COSTRUZIONE DELL'ACTION CHE VERRA' INVIATA A TORCS
        //normalizzazione del vettore delle feature proveniente da TORCS
        this.normalizeFeatureVector(torcsFeatureVector);

        //troviamo gli indici dei K Nearest Neighbors nel dataset delle features
        int[] nearestNeighborsIndeces = this.findKNearestNeighborIndices(torcsFeatureVector, 3);

        //troviamo le actions corrispondenti ai 3 Neighbors compiute dal pilota esperto
        //e ne facciamo la media
        double[] controlli = this.meanActionOfKNeighbors(nearestNeighborsIndeces);

        //creiamo un oggetto action da ritornare a TORCS
        Action action = new Action();
        action.accelerate = controlli[0];
        action.brake = controlli[1];
        action.clutch = controlli[2];
        action.gear = (int) Math.round(controlli[3]);
        action.steering = controlli[4];
        action.focus = (int) controlli[5];

        return action;
    }

    @Override
    public void reset() {
        System.out.println("La gara sta per ricominciare");
    }

    @Override
    public void shutdown() {
        System.out.println("Spegnimento");
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