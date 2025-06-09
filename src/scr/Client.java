/**
 *
 */
package scr;

import java.util.StringTokenizer;
import scr.Controller.Stage;

/**
 * @author gruppo06
 *
 */
public class Client {
    public static void main(String[] args) {
        String CsvFilePath = "C://Users//vitaa//Desktop//testSampling.csv";

        CsvLogManager manager_file = new CsvLogManager(CsvFilePath);
        manager_file.extractVectors(); //estrae i vettori dal file salvandoli nelle arrayList apposite che ha come attributi

        KnnController AI_Controller = new KnnController();
        AI_Controller.createFeaturesDataset(manager_file.getFeatureVectors());
        AI_Controller.normalizeFeaturesDataset();
        AI_Controller.printFeaturesDataset();
    }
}