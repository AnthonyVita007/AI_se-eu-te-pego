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

        double[] torcsFeatureVector = {72.29,27.69,88.51,13.02,19.67,98.21,65.23,50.6,23.58};
        double[] normalizedTorcsFeatureVector = {0.60, 0.50, 0.40, 0.60, 0.60, 0.60, 0.60, 0.60, 0.60};
        int nearestNeighbourIndex = AI_Controller.findNearestNeighborIndex(normalizedTorcsFeatureVector);

        System.out.println("\n Normalized Torcs Vector");
        Utilities.printVettore(normalizedTorcsFeatureVector);

        System.out.println("\n Nearest Neighbor");
        System.out.println("index: " + nearestNeighbourIndex);
        Utilities.printVettore((AI_Controller.getFeaturesDataset())[nearestNeighbourIndex]);
    }
}