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
    private static final long SAMPLE_INTERVAL_MS = 25; // 25ms = 40Hz
    
    // Parametri cambio automatico migliorati
    private static final double RPM_SHIFT_UP = 7000.0;
    private static final double RPM_SHIFT_DOWN = 4250.0;
    private static final int MAX_GEAR = 6;
    private static final int MIN_GEAR = 1;
    private long lastGearChangeTime = 0;
    private static final long GEAR_CHANGE_DELAY = 300;
    
    // Parametri per scalare basandosi sulla velocità
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
            
            fileName = "torcs_extended_" + timestamp + ".csv";
            csvWriter = new FileWriter(fileName);
            
            // Header CSV con tutti i 19 sensori track (0-18)
            StringBuilder header = new StringBuilder("speed,lateralSpeed,trackPosition,");
            // Aggiungi tutti i sensori track
            for (int i = 0; i < 19; i++) {
                header.append("trackEdge_").append(i).append(",");
            }
            header.append("angleToTrackAxis,action_accelerate,action_brake,action_clutch,action_gear,action_steering,action_focus\n");
            
            csvWriter.write(header.toString());
            csvWriter.flush();
            
            System.out.println("File CSV creato: " + fileName);
            System.out.println("Sensori track inclusi: da 0 a 18");
            System.out.println();
            
        } catch (IOException e) {
            System.err.println("Errore creazione CSV: " + e.getMessage());
        }
    }
    
    private void startDriving() {
        Thread receiveThread = new Thread(() -> {
            byte[] buffer = new byte[2048];
            
            while (running) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    
                    String sensorData = new String(packet.getData(), 0, packet.getLength());
                    
                    updateControlValues();
                    
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastSampleTime >= SAMPLE_INTERVAL_MS) {
                        recordExtendedData(sensorData);
                        lastSampleTime = currentTime;
                    }
                    
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
            
            // Speed, lateralSpeed, trackPosition
            data.append(extractValue(sensorData, "speedX")).append(",");
            data.append(extractValue(sensorData, "speedY")).append(",");
            data.append(extractValue(sensorData, "trackPos")).append(",");
            
            // Estrai tutti i sensori track da 0 a 18
            String[] trackSensors = extractTrackSensorsArray(sensorData);
            for (int i = 0; i < 19; i++) {
                if (i < trackSensors.length) {
                    data.append(trackSensors[i]);
                } else {
                    data.append("200"); // valore di default se il sensore non è disponibile
                }
                data.append(",");
            }
            
            // Angolo e azioni
            data.append(extractValue(sensorData, "angle")).append(",");
            data.append(String.format(Locale.US, "%.6f", currentAccel)).append(",");
            data.append(String.format(Locale.US, "%.6f", currentBrake)).append(",");
            data.append(String.format(Locale.US, "%.6f", currentClutch)).append(",");
            data.append(currentGear).append(",");
            data.append(String.format(Locale.US, "%.6f", currentSteering)).append(",");
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
                System.out.println("Dati raccolti: " + dataCount + " campioni (30 valori per riga) - Marcia: " + currentGear + 
                                 " - RPM: " + (int)currentRPM + " - Velocità: " + String.format("%.1f", Math.abs(currentSpeedX) * 3.6) + " km/h");
            }
            
        } catch (IOException e) {
            System.err.println("Errore scrittura CSV: " + e.getMessage());
        }
    }
    
    private String buildActionCommand() {
        return String.format("(accel %.3f)(brake %.3f)(gear %d)(steer %.3f)(clutch %.3f)(focus %d)(meta %d)",
                           currentAccel, currentBrake, currentGear, currentSteering, currentClutch, 
                           currentFocus, currentRestartRace ? 1 : 0);
    }
    
    private void updateControlValues() {
        // Gestione sterzo progressivo
        if (leftPressed && !rightPressed) {
            currentSteering = Math.min(1.0, currentSteering + 0.15);
        } else if (rightPressed && !leftPressed) {
            currentSteering = Math.max(-1.0, currentSteering - 0.15);
        } else {
            if (Math.abs(currentSteering) > 0.05) {
                currentSteering *= 0.7;
            } else {
                currentSteering = 0.0;
            }
        }
        
        // Gestione accelerazione e freno
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
        
        if (currentRestartRace) {
            currentRestartRace = false;
        }
        
        updateImprovedAutomaticGear();
    }
    
    private void updateImprovedAutomaticGear() {
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - lastGearChangeTime < GEAR_CHANGE_DELAY) {
            return;
        }
        
        int oldGear = currentGear;
        double speedKmh = Math.abs(currentSpeedX) * 3.6;
        
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
            // Scalata in su
            if (currentAccel > 0.1 && currentRPM > RPM_SHIFT_UP && currentGear < MAX_GEAR) {
                if (speedKmh >= SPEED_THRESHOLDS[Math.min(currentGear + 1, SPEED_THRESHOLDS.length - 1)]) {
                    currentGear++;
                    System.out.println("Scalata su: Marcia " + currentGear + " (RPM: " + (int)currentRPM + 
                                     ", Velocità: " + String.format("%.1f", speedKmh) + " km/h)");
                }
            }
            
            // Scalata in giù
            if (currentGear > MIN_GEAR) {
                boolean shouldDownshift = false;
                String reason = "";
                
                double speedThresholdForCurrentGear = SPEED_THRESHOLDS[currentGear];
                double speedThresholdForLowerGear = SPEED_THRESHOLDS[Math.max(currentGear - 1, 1)];
                
                if (currentRPM < RPM_SHIFT_DOWN && currentAccel > 0.1) {
                    shouldDownshift = true;
                    reason = "RPM bassi durante accelerazione: " + (int)currentRPM;
                }
                
                if (speedKmh < speedThresholdForCurrentGear - 3.0) {
                    shouldDownshift = true;
                    reason = "Velocità bassa per marcia " + currentGear + ": " + String.format("%.1f", speedKmh) + " km/h";
                }
                
                if (speedKmh < speedThresholdForLowerGear + 5.0 && currentGear > 2) {
                    shouldDownshift = true;
                    reason = "Velocità compatibile con marcia inferiore";
                }
                
                if (currentBrake > 0.5 && speedKmh < speedThresholdForCurrentGear) {
                    shouldDownshift = true;
                    reason = "Frenata forte";
                }
                
                if (shouldDownshift) {
                    currentGear = Math.max(MIN_GEAR, currentGear - 1);
                    System.out.println("Scalata giù: Marcia " + currentGear + " (" + reason + ")");
                }
            }
            
            // Protezione per velocità molto bassa
            if (speedKmh < 5.0 && currentGear > 1) {
                currentGear = 1;
                System.out.println("Forzata prima marcia (velocità molto bassa: " + String.format("%.1f", speedKmh) + " km/h)");
            }
        }
        
        currentGear = Math.max(-1, Math.min(MAX_GEAR, currentGear));
        
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
            // cattura eccezione
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