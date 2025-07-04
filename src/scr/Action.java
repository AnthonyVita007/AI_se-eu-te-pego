package scr;

public class Action {

    public double accelerate = 0; // 0..1
    public double brake = 0; // 0..1
    public double clutch = 0; // 0..1
    public int gear = 0; // -1..6
    public double steering = 0; // -1..1
    public boolean restartRace = false;
    public int focus = 360;
    
    public String toString() {
        limitValues();
        return "(accel " + accelerate + ") " + "(brake " + brake + ") " + "(clutch " + clutch + ") " + "(gear " + gear
                + ") " + "(steer " + steering + ") " + "(meta " + (restartRace ? 1 : 0) + ") " + "(focus " + focus // ML
                + ")";
    }

    public void limitValues() {
        accelerate = Math.max(0, Math.min(1, accelerate));
        brake = Math.max(0, Math.min(1, brake));
        clutch = Math.max(0, Math.min(1, clutch));
        steering = Math.max(-1, Math.min(1, steering));
        gear = Math.max(-1, Math.min(6, gear));

    }
}
