package scr;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class CsvLogParser {

    //ATTRIBUTI
    private String csvFilePath; // Path al file CSV
    private BufferedReader reader; // Reader per leggere il file una riga alla volta

    // Dimensioni dei vettori (basate sulle feature/azioni scelte)
    private static int FEATURE_VECTOR_SIZE = 23; // speed, lateralSpeed, trackPosition
                                                    // track[0-18] (19 sensori)
                                                    // angleToTrackAxis
    private static int ACTION_VECTOR_SIZE = 6;   // action_accelerate, action_brake, action_clutch, action_gear,
                                                    // action_steering, action_focus
    private static int LINE_SIZE = FEATURE_VECTOR_SIZE + ACTION_VECTOR_SIZE; // numero di parametri separati da virgola presenti su ogni riga del file csv

    //COSTRUTTORE
    public CsvLogParser(String csvFilePath) {
        this.csvFilePath = csvFilePath;
    }

    //METODI
    // Apre il file CSV
    public void openFile() {
        try {
            reader = new BufferedReader(new FileReader(csvFilePath));
            // Salta l'header, se presente (modificare se il file non ha header)
            reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            reader = null;
        }
    }

    // Legge la prossima riga ed estrae i vettori
    // Restituisce true se ci sono altre righe, false se è finito il file.
    // Alla fine del metodo, features[] dovrà contenere tutti i valori assunti dalle features, 
    // e actions[] dovrà contenere tutti i valori assunti dalle actions
    public boolean nextSample(ArrayList<Double> features, ArrayList<Double> actions) {
            if (reader == null) return false;

            try {
                String line = reader.readLine();
                if (line == null) {
                    return false; // Fine del file
                }

                String[] tokens = line.split(",");
                if (tokens.length < LINE_SIZE) {
                    throw new IOException("Riga non valida: attesi almeno " + LINE_SIZE + " valori, trovati " + tokens.length);
                }

                // Pulisce i vettori ricevuti
                features.clear();
                actions.clear();

                // Carica le features (primi FEATURE_VECTOR_SIZE valori)
                for (int i = 0; i < FEATURE_VECTOR_SIZE; i++) {
                    features.add(Double.parseDouble(tokens[i].trim()));
                }

                // Carica le actions (ultimi ACTION_VECTOR_SIZE valori)
                for (int i = FEATURE_VECTOR_SIZE; i < LINE_SIZE; i++) {
                    actions.add(Double.parseDouble(tokens[i].trim()));
                }

                return true;

            } catch (IOException | NumberFormatException e) {
                e.printStackTrace();
                return false;
            }
        }

    // Chiude il file
    public void closeFile() {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        reader = null;
    }

    //GETTER
    public String getCsvFilePath() {
        return csvFilePath;
    }

    public static int getFeatureVectorSize() {
        return FEATURE_VECTOR_SIZE;
    }

    public static int getActionVectorSize() {
        return ACTION_VECTOR_SIZE;
    }

    public static int getLineSize() {
        return LINE_SIZE;
    }
}
