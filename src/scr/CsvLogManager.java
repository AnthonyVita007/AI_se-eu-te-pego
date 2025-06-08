package scr;

import java.util.ArrayList;
import java.util.List;

public class CsvLogManager {
    //ATTRIBUTI
    private String csvFilePath; // Path al file CSV
    private ArrayList<ArrayList<Double>> featureVectors; // Lista di vettori delle features (input KNN)
    private ArrayList<ArrayList<Double>> actionVectors; // Lista di vettori delle azioni (output KNN)
    private CsvLogParser parser;

    //COSTRUTTORE
    public CsvLogManager(String csvFilePath) {
        this.csvFilePath = csvFilePath;
        this.parser = new CsvLogParser(csvFilePath);
        this.featureVectors = new ArrayList<ArrayList<Double>>();
        this.actionVectors = new ArrayList<ArrayList<Double>>();
    }

    //METODI
    //Metodo per estrarre da ogni riga del file il relativo features vector e action vector
    public void extractVectors() {
        ArrayList<Double> extractedFeatureVector = new ArrayList<Double>();
        ArrayList<Double> extractedActionVector = new ArrayList<Double>();

        parser.openFile();
        while(parser.nextSample(extractedFeatureVector, extractedActionVector)) {
            // Copia i valori in nuovi oggetti prima di aggiungerli
            this.featureVectors.add(new ArrayList<>(extractedFeatureVector));
            this.actionVectors.add(new ArrayList<>(extractedActionVector));

            // Pulisce i vettori temporanei per il prossimo giro
            extractedFeatureVector.clear();
            extractedActionVector.clear();
        }
        parser.closeFile();
    }


    // Ottiene il vettore delle features all'indice specificato
    public ArrayList<Double> getFeatureVectorAtIndex(int index) {
        if (featureVectors == null) {
            throw new IllegalStateException("Feature vectors not initialized. Call extractVectors() first.");
        }
        if (index < 0 || index >= featureVectors.size()) {
            throw new IndexOutOfBoundsException("Invalid index for feature vector: " + index);
        }
        return featureVectors.get(index);
    }

    // Ottiene il vettore delle azioni all'indice specificato
    public ArrayList<Double> getActionVectorAtIndex(int index) {
        if (actionVectors == null) {
            throw new IllegalStateException("Feature vectors not initialized. Call extractVectors() first.");
        }
        if (index < 0 || index >= actionVectors.size()) {
            throw new IndexOutOfBoundsException("Invalid index for feature vector: " + index);
        }
        return actionVectors.get(index);
    }

    // Restituisce tutte le feature vectors
    public ArrayList<ArrayList<Double>> getAllFeatureVectors() {
        return featureVectors;
    }

    // Restituisce tutte le action vectors
    public ArrayList<ArrayList<Double>> getAllActionVectors() {
        return actionVectors;
    }
}