import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.FileWriter;
import java.io.IOException;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JFrame;
import java.util.Locale;

public class TorcsSCRManualDriver implements KeyListener {
    
    // Connessione UDP
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int serverPort = 3001;
    
    // Controlli tastiera
    private boolean upPressed = false;
    private boolean downPressed = false;
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean brakePressed = false;
    
    // File CSV con sensori estesi
    private FileWriter csvWriter;
    private String fileName;
    private int dataCount = 0;
    
    // Finestra invisibile per catturare input tastiera
    private JFrame hiddenFrame;
    
    // Parametri di guida (Actions)
    private double currentSteering = 0.0;
    private double currentAccel = 0.0;
    private double currentBrake = 0.0;
    private double currentClutch = 0.0;
    private int currentGear = 1;
    private boolean currentRestartRace = false;
    private int currentFocus = 360;
    
    // Dati sensori attuali per il cambio automatico
    private double currentRPM = 0.0;
    private double currentSpeedX = 0.0;
    private boolean running = true;
    
    // Controllo sampling rate
    private long lastSampleTime = 0;
    private static final long SAMPLE_INTERVAL_MS = 25; // 50ms = 20Hz
    
    // Parametri cambio automatico migliorati
    private static final double RPM_SHIFT_UP = 7000.0;
    private static final double RPM_SHIFT_DOWN = 4250.0;
    private static final int MAX_GEAR = 6;
    private static final int MIN_GEAR = 1;
    private long lastGearChangeTime = 0;
    private static final long GEAR_CHANGE_DELAY = 300; // Ridotto per essere più reattivo
    
    // Nuovi parametri per scalare basandosi sulla velocità
    private static final double[] SPEED_THRESHOLDS = {
        0.0,   // Gear -1 (retromarcia)
        0.0,   // Gear 0 (non usato)
        0.0,   // Gear 1: sempre permessa
        8.0,   // Gear 2: velocità minima 8 km/h
        20.0,  // Gear 3: velocità minima 20 km/h
        35.0,  // Gear 4: velocità minima 35 km/h
        50.0,  // Gear 5: velocità minima 50 km/h
        70.0   // Gear 6: velocità minima 70 km/h
    };
    
    public TorcsSCRManualDriver() {
        try {
            initializeNetwork();
            initializeKeyListener();
            initializeCSV();
            startDriving();
        } catch (Exception e) {
            System.err.println("Errore nell'inizializzazione: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void initializeNetwork() throws SocketException, UnknownHostException {
        socket = new DatagramSocket();
        socket.setSoTimeout(1000);
        serverAddress = InetAddress.getByName("localhost");
        System.out.println("=== TORCS SCR Manual Driver - CSV Writer ===");
        System.out.println("Tentativo di connessione a TORCS SCR su porta " + serverPort);
        System.out.println();
    }
    
    private void initializeKeyListener() {
        hiddenFrame = new JFrame();
        hiddenFrame.setSize(1, 1);
        hiddenFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        hiddenFrame.setAlwaysOnTop(true);
        hiddenFrame.addKeyListener(this);
        hiddenFrame.setFocusable(true);
        hiddenFrame.setVisible(true);
        hiddenFrame.requestFocus();
        
        System.out.println("CONTROLLI ATTIVI:");
        System.out.println("W: Accelera | S: Freno/Retro");
        System.out.println("A: Sinistra | D: Destra");
        System.out.println("SPAZIO: Freno | R: Reset | ESC: Esci");
        System.out.println("(Mantieni attiva la finestra piccola per i controlli)");
        System.out.println();
    }
    
    private void initializeCSV() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String timestamp = sdf.format(new Date());
            
            // File CSV con sensori estesi - ora include track[0] e track[18]
            fileName = "torcs_extended_" + timestamp + ".csv";
            csvWriter = new FileWriter(fileName);
            
            // Header CSV esteso con 7 sensori track (0,3,6,9,12,15,18)
            String header = "speed,lateralSpeed,trackPosition,trackEdge_0,trackEdge_3,trackEdge_6,trackEdge_9,trackEdge_12,trackEdge_15,trackEdge_18,angleToTrackAxis,action_accelerate,action_brake,action_clutch,action_gear,action_steering,action_focus\n";
            
            csvWriter.write(header);
            csvWriter.flush();
            
            System.out.println("File CSV creato: " + fileName);
            System.out.println("Sensori track inclusi: 0, 3, 6, 9, 12, 15, 18");
            System.out.println();
            
        } catch (IOException e) {
            System.err.println("Errore creazione CSV: " + e.getMessage());
        }
    }
    
    private void startDriving() {
        // Thread per ricevere dati dai sensori
        Thread receiveThread = new Thread(() -> {
            byte[] buffer = new byte[2048];
            
            while (running) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    
                    String sensorData = new String(packet.getData(), 0, packet.getLength());
                    
                    // Aggiorna i valori di controllo prima di registrare
                    updateControlValues();
                    
                    // Registra i valori estesi ogni 50ms
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastSampleTime >= SAMPLE_INTERVAL_MS) {
                        recordExtendedData(sensorData);
                        lastSampleTime = currentTime;
                    }
                    
                    // Costruisce e invia il comando azione (sempre, per mantenere responsività)
                    String actionCommand = buildActionCommand();
                    sendAction(actionCommand);
                    
                } catch (SocketTimeoutException e) {
                    // Timeout normale, continua
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Errore ricezione dati: " + e.getMessage());
                    }
                }
            }
        });
        
        receiveThread.setDaemon(true);
        receiveThread.start();
        
        // Invio primo messaggio per iniziare la connessione
        try {
            String initMsg = "SCRManualDriver(init)";
            sendMessage(initMsg);
            System.out.println("Messaggio di inizializzazione inviato: " + initMsg);
            
            Thread.sleep(100);
            
            String altInitMsg = "(init SCRManualDriver)";
            sendMessage(altInitMsg);
            System.out.println("Messaggio alternativo inviato: " + altInitMsg);
            
            System.out.println("Connessione inizializzata. Inizia a guidare!");
        } catch (IOException | InterruptedException e) {
            System.err.println("Errore invio inizializzazione: " + e.getMessage());
        }
    }
    
private void recordExtendedData(String sensorData) {
    try {
        StringBuilder data = new StringBuilder();
        
        // === 11 VALORI SENSORI (invece di 9) ===
        
        // 1. Speed (da speedX)
        data.append(extractValue(sensorData, "speedX")).append(",");

        // 2. lateralSpeed (da speedY)
        data.append(extractValue(sensorData, "speedY")).append(",");
        
        // 3. DistanzaLineaCentrale (da trackPos)
        data.append(extractValue(sensorData, "trackPos")).append(",");
        
        // 4-10. Sensori traccia estesi (7 sensori: 0,3,6,9,12,15,18)
        String[] trackSensors = extractTrackSensorsArray(sensorData);
        
        // 4. SensoreEstremoSX (track[0])
        if (trackSensors.length > 0) {
            data.append(trackSensors[0]).append(",");
        } else {
            data.append("200,");
        }
        
        // 5. SensoreSX1 (track[3])
        if (trackSensors.length > 3) {
            data.append(trackSensors[3]).append(",");
        } else {
            data.append("200,");
        }
        
        // 6. SensoreSX2 (track[6])
        if (trackSensors.length > 6) {
            data.append(trackSensors[6]).append(",");
        } else {
            data.append("200,");
        }
        
        // 7. SensoreCentrale (track[9])
        if (trackSensors.length > 9) {
            data.append(trackSensors[9]).append(",");
        } else {
            data.append("200,");
        }
        
        // 8. SensoreDX1 (track[12])
        if (trackSensors.length > 12) {
            data.append(trackSensors[12]).append(",");
        } else {
            data.append("200,");
        }
        
        // 9. SensoreDX2 (track[15])
        if (trackSensors.length > 15) {
            data.append(trackSensors[15]).append(",");
        } else {
            data.append("200,");
        }
        
        // 10. SensoreEstremoDX (track[18])
        if (trackSensors.length > 18) {
            data.append(trackSensors[18]).append(",");
        } else {
            data.append("200,");
        }
        
        // 11. Angolo (da angle)
        data.append(extractValue(sensorData, "angle")).append(",");
        
        // === 6 VALORI AZIONI ===
        // 12. accelerate
        data.append(String.format(Locale.US, "%.6f", currentAccel)).append(",");
        
        // 13. brake
        data.append(String.format(Locale.US, "%.6f", currentBrake)).append(",");
        
        // 14. clutch
        data.append(String.format(Locale.US, "%.6f", currentClutch)).append(",");
        
        // 15. gear
        data.append(currentGear).append(",");

        // 16. steering
        data.append(String.format(Locale.US, "%.6f", currentSteering)).append(",");
        
        // 17. focus (ULTIMO VALORE - SENZA VIRGOLA)
        data.append(currentFocus).append("\n");
        
        csvWriter.write(data.toString());
        csvWriter.flush();
        
        // Aggiorna i valori per il cambio automatico
        try {
            currentRPM = Double.parseDouble(extractValue(sensorData, "rpm"));
            currentSpeedX = Double.parseDouble(extractValue(sensorData, "speedX"));
        } catch (NumberFormatException e) {
            // Mantieni valori precedenti
        }
        
        dataCount++;
        
        // Status update ogni 500 campioni
        if (dataCount % 500 == 0 && dataCount > 0) {
            System.out.println("Dati raccolti: " + dataCount + " campioni (17 valori per riga) - Marcia: " + currentGear + 
                             " - RPM: " + (int)currentRPM + " - Velocità: " + String.format("%.1f", Math.abs(currentSpeedX) * 3.6) + " km/h");
        }
        
    } catch (IOException e) {
        System.err.println("Errore scrittura CSV: " + e.getMessage());
    }
}
    
    private String buildActionCommand() {
        // Formato comando SCR
        return String.format("(accel %.3f)(brake %.3f)(gear %d)(steer %.3f)(clutch %.3f)(focus %d)(meta %d)",
                           currentAccel, currentBrake, currentGear, currentSteering, currentClutch, 
                           currentFocus, currentRestartRace ? 1 : 0);
    }
    
    private void updateControlValues() {
        // Gestione sterzo progressivo
        if (leftPressed && !rightPressed) {
            currentSteering = Math.min(1.0, currentSteering + 0.15);  // A = sinistra = valore positivo
        } else if (rightPressed && !leftPressed) {
            currentSteering = Math.max(-1.0, currentSteering - 0.15); // D = destra = valore negativo
        } else {
            // Ritorno graduale al centro
            if (Math.abs(currentSteering) > 0.05) {
                currentSteering *= 0.7;
            } else {
                currentSteering = 0.0;
            }
        }
        
        // Gestione accelerazione
        if (upPressed) {
            currentAccel = 1.0;
            currentBrake = 0.0;
        } else if (downPressed) {
            currentAccel = 0.0;
            currentBrake = 0.6;
        } else if (brakePressed) {
            currentAccel = 0.0;
            currentBrake = 1.0;
        } else {
            currentAccel = 0.0;
            currentBrake = 0.0;
        }
        
        // Reset restartRace dopo l'uso
        if (currentRestartRace) {
            currentRestartRace = false;
        }
        
        // CAMBIO AUTOMATICO MIGLIORATO
        updateImprovedAutomaticGear();
    }
    
    private void updateImprovedAutomaticGear() {
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - lastGearChangeTime < GEAR_CHANGE_DELAY) {
            return;
        }
        
        int oldGear = currentGear;
        double speedKmh = Math.abs(currentSpeedX) * 3.6; // Conversione da m/s a km/h
        
        // Gestione retromarcia
        if (downPressed && speedKmh < 3.0 && currentGear == 1) {
            currentGear = -1;
            System.out.println("Inserita retromarcia (velocità: " + String.format("%.1f", speedKmh) + " km/h)");
        } else if (currentGear == -1 && (upPressed || speedKmh > 3.0)) {
            currentGear = 1;
            System.out.println("Uscita da retromarcia");
        }
        
        // Cambio automatico solo se non in retromarcia
        if (currentGear > 0) {
            // SCALATA IN SU (accelerando)
            if (currentAccel > 0.1 && currentRPM > RPM_SHIFT_UP && currentGear < MAX_GEAR) {
                // Verifica che abbiamo velocità sufficiente per la marcia superiore
                if (speedKmh >= SPEED_THRESHOLDS[Math.min(currentGear + 1, SPEED_THRESHOLDS.length - 1)]) {
                    currentGear++;
                    System.out.println("Scalata su: Marcia " + currentGear + " (RPM: " + (int)currentRPM + 
                                     ", Velocità: " + String.format("%.1f", speedKmh) + " km/h)");
                }
            }
            
            // SCALATA IN GIÙ
        if (currentGear > MIN_GEAR) {
            boolean shouldDownshift = false;
            String reason = "";

            double speedThresholdForCurrentGear = SPEED_THRESHOLDS[currentGear];
            double speedThresholdForLowerGear = SPEED_THRESHOLDS[Math.max(currentGear - 1, 1)];

            // 1. RPM troppo bassi e accelerazione richiesta
        if (currentRPM < RPM_SHIFT_DOWN && currentAccel > 0.1) {
            shouldDownshift = true;
            reason = "RPM bassi durante accelerazione: " + (int)currentRPM;
        }

        // 2. Velocità sotto soglia minima della marcia attuale (con isteresi)
        if (speedKmh < speedThresholdForCurrentGear - 3.0) {
            shouldDownshift = true;
            reason = "Velocità bassa per marcia " + currentGear + ": " + String.format("%.1f", speedKmh) + " km/h";
        }

        // 3. Velocità sufficiente per una marcia inferiore (previene stallo in marcia alta)
        if (speedKmh < speedThresholdForLowerGear + 5.0 && currentGear > 2) {
            shouldDownshift = true;
            reason = "Velocità compatibile con marcia inferiore";
        }

        // 4. Frenata forte
        if (currentBrake > 0.5 && speedKmh < speedThresholdForCurrentGear) {
            shouldDownshift = true;
            reason = "Frenata forte";
        }

        if (shouldDownshift) {
            currentGear = Math.max(MIN_GEAR, currentGear - 1);
            System.out.println("Scalata giù: Marcia " + currentGear + " (" + reason + ")");
        }
}
            
            // Protezione per velocità molto bassa: forza prima marcia
            if (speedKmh < 5.0 && currentGear > 1) {
                currentGear = 1;
                System.out.println("Forzata prima marcia (velocità molto bassa: " + String.format("%.1f", speedKmh) + " km/h)");
            }
        }
        
        // Limita il range delle marce
        currentGear = Math.max(-1, Math.min(MAX_GEAR, currentGear));
        
        // Aggiorna il tempo dell'ultimo cambio se è avvenuto
        if (oldGear != currentGear) {
            lastGearChangeTime = currentTime;
        }
    }
    
    private void sendAction(String action) throws IOException {
        sendMessage(action);
    }
    
    private void sendMessage(String message) throws IOException {
        byte[] data = message.getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, serverPort);
        socket.send(packet);
    }
    
    private String extractValue(String data, String key) {
        try {
            String searchPattern = "(" + key + " ";
            int start = data.indexOf(searchPattern);
            if (start == -1) return "0";
            
            start += searchPattern.length();
            int end = data.indexOf(")", start);
            if (end == -1) return "0";
            
            return data.substring(start, end).trim();
        } catch (Exception e) {
            return "0";
        }
    }
    
    private String[] extractTrackSensorsArray(String data) {
        try {
            String trackPattern = "(track ";
            int start = data.indexOf(trackPattern);
            if (start != -1) {
                start += trackPattern.length();
                int end = data.indexOf(")", start);
                if (end != -1) {
                    String trackData = data.substring(start, end).trim();
                    return trackData.split("\\s+");
                }
            }
        } catch (Exception e) {
            // Fallback
        }
        return new String[0];
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                upPressed = true;
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                downPressed = true;
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                leftPressed = true;
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                rightPressed = true;
                break;
            case KeyEvent.VK_SPACE:
                brakePressed = true;
                break;
            case KeyEvent.VK_R:
                resetCar();
                break;
            case KeyEvent.VK_ESCAPE:
                shutdown();
                break;
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                upPressed = false;
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                downPressed = false;
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                leftPressed = false;
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                rightPressed = false;
                break;
            case KeyEvent.VK_SPACE:
                brakePressed = false;
                break;
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
        // Non utilizzato
    }
    
    private void resetCar() {
        try {
            currentRestartRace = true;
            sendMessage("(meta 1)");
            currentGear = 1;
            System.out.println("Reset inviato alla macchina - Marcia resettata a 1");
        } catch (IOException e) {
            System.err.println("Errore reset: " + e.getMessage());
        }
    }
    
    public void shutdown() {
        System.out.println("\nChiusura in corso...");
        running = false;
        
        try {
            if (csvWriter != null) {
                csvWriter.close();
                System.out.println("File salvato: " + fileName);
                System.out.println("Totale campioni raccolti: " + dataCount);
            }
            
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            
            if (hiddenFrame != null) {
                hiddenFrame.dispose();
            }
            
            System.exit(0);
            
        } catch (IOException e) {
            System.err.println("Errore chiusura: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== TORCS SCR Manual Driver - CSV Writer ===");
        System.out.println();
        
        new TorcsSCRManualDriver();
        
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.out.println("Programma terminato");
        }
    }
}