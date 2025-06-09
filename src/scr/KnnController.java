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

    //METODI
    private void createFeaturesDataset(ArrayList<ArrayList<Double>> featureVectors){
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

    private void createActionsDataset(ArrayList<ArrayList<Double>> actionVectors){
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