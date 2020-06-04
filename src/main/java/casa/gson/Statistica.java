package casa.gson;

public class Statistica {
	private float valore;
	private long timestamp;

	public Statistica(float valore, long timestamp) {
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

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
}
