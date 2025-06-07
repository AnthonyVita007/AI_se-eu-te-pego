package scr;

import java.util.List;

public class CsvLogManager {
    //ATTRIBUTI
    private String csvFilePath; // Path al file CSV
    private List<double[]> featureVectors; // Lista di vettori delle features (input KNN)
    private List<double[]> actionVectors; // Lista di vettori delle azioni (output KNN)

    //COSTRUTTORE
    public CsvLogManager(String csvFilePath) {
        this.csvFilePath = csvFilePath;
    }

    //METODI
    // Metodo per caricare il CSV in memoria
    public void loadCsv() {
        // TODO: implementazione
    }

    //Metodo per estrarre da ogni riga del file il relativo features vector e action vector
    //il metodo sfrutterà gli strumenti messi a disposzione dalla classe CsvLogParser

    // Ottiene il vettore delle features all'indice specificato
    public double[] getFeatureVectorAtIndex(int index) {
        // TODO: implementazione
        return null;
    }

    // Ottiene il vettore delle azioni all'indice specificato
    public double[] getActionVectorAtIndex(int index) {
        // TODO: implementazione
        return null;
    }

    // Restituisce tutte le feature vectors
    public List<double[]> getAllFeatureVectors() {
        return featureVectors;
    }

    // Restituisce tutte le action vectors
    public List<double[]> getAllActionVectors() {
        return actionVectors;
    }
}