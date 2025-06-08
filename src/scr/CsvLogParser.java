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
    private static final int FEATURE_VECTOR_SIZE = 100; // speed, angleToTrackAxis, lateralSpeed, rpm, gear_sensor,
                                                        //trackPosition, currentLapTime, damage, distanceFromStartLine, 
                                                        //distanceRaced, fuelLevel, lastLapTime, z, zSpeed, trackEdge_[0-18],
                                                        //focus_[0-4], wheelSpinVel_[0-3]
    private static final int ACTION_VECTOR_SIZE = 100;   // action_accelerate, action_brake, action_clutch, action_gear,
                                                        //action_steering, action_restartRace, action_focus

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
                if (tokens.length < 49) {
                    throw new IOException("Riga non valida: attesi almeno 49 valori, trovati " + tokens.length);
                }

                // Pulisce i vettori ricevuti
                features.clear();
                actions.clear();

                // Carica le features (primi 42 valori)
                for (int i = 0; i < 42; i++) {
                    features.add(Double.parseDouble(tokens[i].trim()));
                }

                // Carica le actions (ultimi 7 valori)
                for (int i = 42; i < 49; i++) {
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
}
