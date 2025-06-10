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
        String CsvFilePath = "C://Users//vitaa//IdeaProjects//AI_se-eu-te-pego//src//dataset_definitivo.csv";

        CsvLogManager manager_file = new CsvLogManager(CsvFilePath);
        manager_file.extractVectors();

        KnnController AI_Controller = new KnnController();
        AI_Controller.createFeaturesDataset(manager_file.getFeatureVectors());
        AI_Controller.createActionsDataset(manager_file.getActionVectors());
        AI_Controller.normalizeFeaturesDataset();
        AI_Controller.printFeaturesDataset();

        double[] normalizedTorcsFeatureVector = {0.60, 0.50, 0.40, 0.60, 0.60, 0.60, 0.60, 0.60, 0.60};

        
        int[] kNearestIndices = AI_Controller.findKNearestNeighborIndices(normalizedTorcsFeatureVector, 3);

        System.out.println("\nIndici dei 3 vicini più prossimi:");
        for (int idx : kNearestIndices) {
            System.out.println("index: " + idx);
            Utilities.printVettore((AI_Controller.getFeaturesDataset())[idx]);
        }

        
        double[] meanAction = AI_Controller.meanActionOfKNeighbors(kNearestIndices);
        System.out.println("\nMedia delle azioni dei 3 vicini:");
        Utilities.printVettore(meanAction);
    }
}