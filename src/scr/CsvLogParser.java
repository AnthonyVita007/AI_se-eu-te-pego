package scr;
import java.io.BufferedReader;

public class CsvLogParser {

    //ATTRIBUTI
    private String csvFilePath; // Path al file CSV
    private BufferedReader reader; // Reader per leggere il file una riga alla volta

    // Dimensioni dei vettori (basate sulle feature/azioni scelte)
    private static final int FEATURE_VECTOR_SIZE = 23; // angle,trackPos,speedX,rpm,gear,track[0-18]
    private static final int ACTION_VECTOR_SIZE = 4;   // accel,brake,gear_cmd,steer

    //COSTRUTTORE
    public CsvLogParser(String csvFilePath) {
        this.csvFilePath = csvFilePath;
    }

    //METODI
    // Apre il file CSV
    public void openFile() {
        // TODO: implementazione
    }

    // Legge la prossima riga ed estrae i vettori
    // Restituisce true se ci sono altre righe, false se è finito il file
    public boolean nextSample(double[] features, double[] actions) {
        // TODO: implementazione
        return false;
    }

    // Chiude il file
    public void closeFile() {
        // TODO: implementazione
    }
}
