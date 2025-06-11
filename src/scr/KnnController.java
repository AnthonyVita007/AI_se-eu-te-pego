package scr;
import java.util.*;

import static java.lang.Math.*;

public class KnnController extends Controller{
    // ATTRIBUTI
    private double[][] featuresDataset = DatasetsManager.getFeaturesDataset();
    private double[][] actionsDataset = DatasetsManager.getActionsDataset();

    // Attributo per il numero di vicini (k)
    private int k; // valore di default

    // Classe interna per rappresentare un vicino con indice e distanza
    private static class NeighborInfo {
        int index;
        double distance;

        NeighborInfo(int index, double distance) {
            this.index = index;
            this.distance = distance;
        }
    }

    //---------------------------------------------------------------------------------------------------------
    // COSTRUTTORE
    public KnnController(int numeroNeighbors){
        super();
        this.k = numeroNeighbors;
    }

    //---------------------------------------------------------------------------------------------------------
    // METODI CALCOLO DISTANZA EUCLIDEA TRA VETTORI E NEAREST-NEIGHBOR
    public double calculateEuclideanDistance_v1v2(double[] v1, double[] v2) {
        double sum = 0;
        for (int i = 0; i < v1.length; i++) {
            double diff = v1[i] - v2[i];
            sum += diff * diff;
        }
        return sqrt(sum);
    }

    // Restituisce una lista dei K vicini più prossimi, ciascuno con indice e distanza
    private List<NeighborInfo> findKNearestNeighborsWithDistances(double[] torcsFeatureVector, int k) {
        List<NeighborInfo> neighbors = new ArrayList<>();
        for (int i = 0; i < featuresDataset.length; i++) {
            double distance = calculateEuclideanDistance_v1v2(torcsFeatureVector, featuresDataset[i]);
            neighbors.add(new NeighborInfo(i, distance));
        }
        neighbors.sort(Comparator.comparingDouble(n -> n.distance));
        return neighbors.subList(0, Math.min(k, neighbors.size()));
    }

    // Calcola la media pesata delle azioni dei vicini (con pesi inversamente proporzionali alla distanza)
    private double[] weightedMeanActionOfKNeighbors(List<NeighborInfo> neighbors) {
        if (actionsDataset == null || neighbors.isEmpty()) return null;
        int actionLen = actionsDataset[0].length;
        double[] weightedSum = new double[actionLen];
        double weightSum = 0.0;
        double epsilon = 1e-8; // per evitare divisione per zero

        for (NeighborInfo neighbor : neighbors) {
            double weight = 1.0 / (neighbor.distance + epsilon);
            weightSum += weight;
            for (int j = 0; j < actionLen; j++) {
                weightedSum[j] += actionsDataset[neighbor.index][j] * weight;
            }
        }
        for (int j = 0; j < actionLen; j++) {
            weightedSum[j] /= weightSum;
        }
        return weightedSum;
    }

    //---------------------------------------------------------------------------------------------------------
    // METODI EREDITATI DA "Controller" DI CUI SI DEVE FARE OVERRIDE
    @Override
    public Action control(SensorModel sensors) {
        // COSTRUZIONE DEL VETTORE DELLE FEATURE PROVENIENTE DA TORCS
        double[] torcsFeatureVector = new double[9];
        torcsFeatureVector[0] = sensors.getSpeed();
        torcsFeatureVector[1] = sensors.getLateralSpeed();
        torcsFeatureVector[2] = sensors.getTrackPosition();

        double[] track = sensors.getTrackEdgeSensors();
        Utilities.printVettore(track);
        torcsFeatureVector[3] = track[3];
        torcsFeatureVector[4] = track[6];
        torcsFeatureVector[5] = track[9];
        torcsFeatureVector[6] = track[12];
        torcsFeatureVector[7] = track[15];

        torcsFeatureVector[8] = sensors.getAngleToTrackAxis();

        // NORMALIZZAZIONE DEL VETTORE DELLE FEATURE PROVENIENTE DA TORCS
        //torcsFeatureVector = DatasetsManager.normalizeFeatureVector(torcsFeatureVector);
        Utilities.printVettore(torcsFeatureVector);

        // TROVIAMO I K NEAREST NEIGHBORS (usando l'attributo di classe k)
        List<NeighborInfo> neighbors = this.findKNearestNeighborsWithDistances(torcsFeatureVector, this.k);

        // CALCOLA LA MEDIA PESATA DELLE AZIONI DEI VICINI
        double[] controlli = this.weightedMeanActionOfKNeighbors(neighbors);

        // CREA UN OGGETTO ACTION DA RITORNARE A TORCS
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
    // GETTER e SETTER
    public double[][] getFeaturesDataset() {
        return featuresDataset;
    }

    public double[][] getActionsDataset() {
        return actionsDataset;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        if (k > 0) {
            this.k = k;
        } else {
            throw new IllegalArgumentException("K deve essere maggiore di zero.");
        }
    }
}