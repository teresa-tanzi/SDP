package serverAmministratore.beans;

public class Statistica {
    private float valore;
    private long timestamp;

    // costruttore vuoto
    public Statistica() {}

    // costruttore con parametri
    public Statistica(float valore, int timestamp) {
        this.valore = valore;
        this.timestamp = timestamp;
    }

    public float getValore() {
        return valore;
    }

    public void setValore(float valore) {
        this.valore = valore;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public String toString() {
        return "{valore:" + valore + ",timestamp:" + timestamp +"}";
    }
}
