package serverAmministratore.beans;

public class Aggregato {
    private float mean;
    private float stDev;

    public Aggregato(float mean, float stDev) {
        this.mean = mean;
        this.stDev = stDev;
    }

    public float getMean() {
        return mean;
    }

    public void setMean(float mean) {
        this.mean = mean;
    }

    public float getStDev() {
        return stDev;
    }

    public void setStDev(float stDev) {
        this.stDev = stDev;
    }

    public String toString() {
        return "{media:" + mean + ",deviazioneStandard:" + stDev + "}";
    }
}
