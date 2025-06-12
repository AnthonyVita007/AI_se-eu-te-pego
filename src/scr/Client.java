package scr;

import java.util.StringTokenizer;
import scr.Controller.Stage;

public class Client {

    private static int UDP_TIMEOUT = 10000;
    private static int port;
    private static String host;
    private static String clientId;
    private static boolean verbose;
    private static int maxEpisodes;
    private static int maxSteps;
    private static Stage stage;
    private static String trackName;

    public static void main(String[] args) {
        // Parsing dei parametri da terminale e dei dati di connessione
        parseParameters(args);

        // Caricamento e normalizzazione dataset
        prepareDatasets();

        // Istanziazione del Controller AI
        KnnController driver = new KnnController(14);
        driver.setStage(stage);
        driver.setTrackName(trackName);

        // Preparazione socket di comunicazione
        SocketHandler mySocket = new SocketHandler(host, port, verbose);

        // Gestione del ciclo di episodi di gara
        runEpisodes(driver, mySocket);

        // Shutdown finale
        driver.shutdown();
        mySocket.close();
        System.out.println("Client shutdown.");
        System.out.println("Bye, bye!");
    }

    private static void prepareDatasets() {
        //estrazione dei Features vectors e degli Action vectors dal file
        String CsvFilePath = "15_giri_clean.csv";
        CsvLogManager manager_file = new CsvLogManager(CsvFilePath);
        manager_file.extractVectors();

        //creazione del dataset delle features e sua successiva normalizzazione
        DatasetsManager.createFeaturesDataset(manager_file.getFeatureVectors());
        double[] array_valori_max = DatasetsManager.getFeatureMaxValues();
        double[] array_valori_min = DatasetsManager.getFeatureMinValues();
        DatasetsManager.defineMaxMinForNormalization(array_valori_max, array_valori_min);
        DatasetsManager.normalizeFeaturesDataset();
        DatasetsManager.printFeaturesDataset();

        //creazione del dataset delle actions
        DatasetsManager.createActionsDataset(manager_file.getActionVectors());
    }

    private static void runEpisodes(KnnController driver, SocketHandler mySocket) {
        String inMsg;
        long curEpisode = 0;
        boolean shutdownOccurred = false;

        String initStr = buildInitString(driver);

        do {
            identifyWithServer(mySocket, initStr);

            long currStep = 0;
            while (true) {
                inMsg = mySocket.receive(UDP_TIMEOUT);

                if (inMsg != null) {
                    if (inMsg.contains("***shutdown***")) {
                        shutdownOccurred = true;
                        System.out.println("Server shutdown!");
                        break;
                    }
                    if (inMsg.contains("***restart***")) {
                        driver.reset();
                        if (verbose)
                            System.out.println("Server restarting!");
                        break;
                    }

                    Action action = new Action();
                    if (currStep < maxSteps || maxSteps == 0){
                        action = driver.control(new MessageBasedSensorModel(inMsg));
                        System.out.println(action.toString());
                    }
                    else
                        action.restartRace = true;

                    currStep++;
                    mySocket.send(action.toString());
                } else {
                    System.out.println("Server did not respond within the timeout");
                }
            }
        } while (++curEpisode < maxEpisodes && !shutdownOccurred);
    }

    private static String buildInitString(Controller driver) {
        float[] angles = driver.initAngles();
        StringBuilder initStr = new StringBuilder(clientId + "(init");
        for (float angle : angles) {
            initStr.append(" ").append(angle);
        }
        initStr.append(")");
        return initStr.toString();
    }

    private static void identifyWithServer(SocketHandler mySocket, String initStr) {
        String inMsg;
        do {
            mySocket.send(initStr);
            inMsg = mySocket.receive(UDP_TIMEOUT);
        } while (inMsg == null || !inMsg.contains("***identified***"));
    }

    private static void parseParameters(String[] args) {
        port = 3001;
        host = "localhost";
        clientId = "SCR";
        verbose = false;
        maxEpisodes = 1;
        maxSteps = 0;
        stage = Stage.UNKNOWN;
        trackName = "unknown";

        for (int i = 1; i < args.length; i++) {
            StringTokenizer st = new StringTokenizer(args[i], ":");
            String entity = st.nextToken();
            String value = st.nextToken();
            switch (entity) {
                case "port":
                    port = Integer.parseInt(value);
                    break;
                case "host":
                    host = value;
                    break;
                case "id":
                    clientId = value;
                    break;
                case "verbose":
                    if (value.equals("on"))
                        verbose = true;
                    else if (value.equals("off"))
                        verbose = false;
                    else {
                        System.out.println(entity + ":" + value + " is not a valid option");
                        System.exit(0);
                    }
                    break;
                case "stage":
                    stage = Stage.fromInt(Integer.parseInt(value));
                    break;
                case "trackName":
                    trackName = value;
                    break;
                case "maxEpisodes":
                    maxEpisodes = Integer.parseInt(value);
                    if (maxEpisodes <= 0) {
                        System.out.println(entity + ":" + value + " is not a valid option");
                        System.exit(0);
                    }
                    break;
                case "maxSteps":
                    maxSteps = Integer.parseInt(value);
                    if (maxSteps < 0) {
                        System.out.println(entity + ":" + value + " is not a valid option");
                        System.exit(0);
                    }
                    break;
            }
        }
    }
}
