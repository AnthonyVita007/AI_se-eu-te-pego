package scr;

import java.util.List;

public class CsvLogManager {
    //ATTRIBUTI
    private String csvFilePath; // Path al file CSV
    private List<double[]> featureVectors; // Lista di vettori delle features (input KNN)
    private List<double[]> actionVectors; // Lista di vettori delle azioni (output KNN)
    private CsvLogParser parser;

    //COSTRUTTORE
    public CsvLogManager(String csvFilePath) {
        this.csvFilePath = csvFilePath;
        this.parser = new CsvLogParser(csvFilePath);
    }

    //METODI
    //Metodo per estrarre da ogni riga del file il relativo features vector e action vector
    public void extractVectors() {
        double[] extractedFeatureVector = new double[23];
        double[] extractedActionVector = new double[4];

        parser.openFile();
        while(parser.nextSample(extractedFeatureVector, extractedActionVector)) {
            this.featureVectors.add(extractedFeatureVector);
            this.actionVectors.add(extractedActionVector);
        }
        parser.closeFile();
    }

    // Ottiene il vettore delle features all'indice specificato
    public double[] getFeatureVectorAtIndex(int index) {
        if (featureVectors == null) {
            throw new IllegalStateException("Feature vectors not initialized. Call extractVectors() first.");
        }
        if (index < 0 || index >= featureVectors.size()) {
            throw new IndexOutOfBoundsException("Invalid index for feature vector: " + index);
        }
        return featureVectors.get(index);
    }

    // Ottiene il vettore delle azioni all'indice specificato
    public double[] getActionVectorAtIndex(int index) {
        if (actionVectors == null) {
            throw new IllegalStateException("Feature vectors not initialized. Call extractVectors() first.");
        }
        if (index < 0 || index >= actionVectorsVectors.size()) {
            throw new IndexOutOfBoundsException("Invalid index for feature vector: " + index);
        }
        return actionVectors.get(index);

    // Restituisce tutte le feature vectors
    public List<double[]> getAllFeatureVectors() {
        return featureVectors;
    }

    // Restituisce tutte le action vectors
    public List<double[]> getAllActionVectors() {
        return actionVectors;
    }
}