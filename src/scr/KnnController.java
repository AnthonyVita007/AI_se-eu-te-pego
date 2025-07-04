package scr;
import java.util.*;

import static java.lang.Math.*;

public class KnnController extends Controller{
    // ATTRIBUTI
    private double[][] featuresDataset = DatasetsManager.getFeaturesDataset();
    private double[][] actionsDataset = DatasetsManager.getActionsDataset();

    // Attributo per il numero di vicini (k)
    private int k; // valore di default

    // Parametri per gestione azioni
    private double steeringSmoothingAlpha = 0.2; // Più vicino a 1 = più reattivo, più vicino a 0 = più dolce
    private double previousSteering = 0.0; // Memorizza la sterzata precedente
    private double maxSteeringDelta = 0.065; // Variazione massima consentita per step (prova valori 0.03-0.1)

    final int[] gearUp = {7000, 7500, 8000, 8500, 9000, 0};
    final int[] gearDown = {0, 2500, 3000, 3500, 4000, 4500};

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
        return Math.sqrt(sum);
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
    private double[] weightedMeanActionOfKNeighbors(List<NeighborInfo> neighbors, double angleToTrackAxis) {
        if (actionsDataset == null || neighbors.isEmpty()) return null;
        int actionLen = actionsDataset[0].length;
        double[] weightedSum = new double[actionLen];
        double weightSum = 0.0;
        double epsilon = 1e-8; // per evitare divisione per zero

        // Calcola la media pesata tra i vicini
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

        // CORREZIONE STERZATA (indice 4)
        weightedSum[4] *= computeSteeringCorrectionFactor(angleToTrackAxis);
        return weightedSum;
    }

    /* Calcola un fattore di correzione per la sterzata in base all'angolo rispetto all'asse della pista.
     Più l'auto è allineata (angolo vicino a 0), più la sterzata viene smorzata.*/
    private double computeSteeringCorrectionFactor(double angleToTrackAxis) {
        final double MIN_CORRECTION = 0.1; // non azzera mai completamente la sterzata
        // diminuendo il max angle è più sensibile
        double maxAngle = 0.20;
        double normalized = Math.min(1.0, Math.abs(angleToTrackAxis) / maxAngle);
        // Fattore: se angolo 0 -> MIN_CORRECTION, se angolo max -> 1
        return MIN_CORRECTION + (1.0 - MIN_CORRECTION) * normalized;
    }

    public int getGear(SensorModel sensors) {
        int gear = sensors.getGear();
        double rpm = sensors.getRPM();

        // se la marcia è 0 (N) o -1 (R) ritorna 1
        if (gear < 1) {
            return 1;
        }
        // controlla se gli rpm della macchina sono maggiori di quelli suggeriti
        // per far avanzare di marcia
        if (gear < 6 && rpm >= gearUp[gear - 1]) {
            return gear + 1;
        } else // controlla se gli rpm della macchina sono minori di quelli suggeriti
            // per far scendere di marcia
            if (gear > 1 && rpm <= gearDown[gear - 1]) {
                return gear - 1;
            } else // altrimenti mantieni quella attuale
            {
                return gear;
            }
    }

    //---------------------------------------------------------------------------------------------------------
    // METODI EREDITATI DA "Controller" DI CUI SI DEVE FARE OVERRIDE
    @Override
    public Action control(SensorModel sensors) {
        // COSTRUZIONE DEL VETTORE DELLE FEATURE PROVENIENTE DA TORCS
        double[] torcsFeatureVector = new double[23];
        // Primi 3 valori base
        torcsFeatureVector[0] = sensors.getSpeed();
        torcsFeatureVector[1] = sensors.getLateralSpeed();
        torcsFeatureVector[2] = sensors.getTrackPosition();

        // Inserimento di tutti i 19 sensori track
        double[] track = sensors.getTrackEdgeSensors();
        for (int i = 0; i < 19; i++) {
            torcsFeatureVector[i + 3] = track[i];  // Riempie le posizioni da 3 a 21
        }

        // Angolo rispetto all'asse della pista (ultima feature)
        torcsFeatureVector[20] = sensors.getAngleToTrackAxis();

        // NORMALIZZAZIONE DEL VETTORE DELLE FEATURE PROVENIENTE DA TORCS
        torcsFeatureVector = DatasetsManager.normalizeFeatureVector(torcsFeatureVector);

        // TROVIAMO I K NEAREST NEIGHBORS (usando l'attributo di classe k)
        List<NeighborInfo> neighbors = this.findKNearestNeighborsWithDistances(torcsFeatureVector, this.k);

        // CALCOLA LA MEDIA PESATA DELLE AZIONI DEI VICINI
        double[] controlli = this.weightedMeanActionOfKNeighbors(neighbors, sensors.getAngleToTrackAxis());

        // CREA UN OGGETTO ACTION DA RITORNARE A TORCS
        Action action = new Action();

        action.brake = controlli[1];

        //GESTIONE ACCELERAZIONE
        action.accelerate = controlli[0];

        action.clutch = controlli[2];

        //GESTIONE GEAR
        action.gear = getGear(sensors);

        //GESTIONE STERZATA
        // --- Smoothing e limitazione della sterzata ---
        double rawSteering = controlli[4];

        // Applica smoothing esponenziale
        double smoothedSteering = steeringSmoothingAlpha * rawSteering + (1 - steeringSmoothingAlpha) * previousSteering;
        // Limita la variazione massima per step
        double delta = smoothedSteering - previousSteering;
        if (Math.abs(delta) > maxSteeringDelta) {
            if (delta > 0)
                smoothedSteering = previousSteering + maxSteeringDelta;
            else
                smoothedSteering = previousSteering - maxSteeringDelta;
        }
        action.steering = smoothedSteering;

        //STAMPE DI DEBUG
        System.out.println("VETTORE TORCS");
        Utilities.printVettore(torcsFeatureVector);

        // Aggiorna la memoria della sterzata
        previousSteering = smoothedSteering;

        action.focus = (int) controlli[5];
        action.limitValues();

        return action;
    }

    @Override
    public void reset() {
        System.out.println("La gara sta per ricominciare");
        previousSteering = 0.0; // per evitare che i valori della gara passata influenzino quelli della gara attuale
    }

    @Override
    public void shutdown() {
        System.out.println("Spegnimento");
    }

    //---------------------------------------------------------------------------------------------------------
    // GETTER e SETTER
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