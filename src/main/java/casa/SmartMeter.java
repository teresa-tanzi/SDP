package casa;

import casa.gson.Statistica;
import casa.simulation_src_2019.Buffer;
import casa.simulation_src_2019.Measurement;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SmartMeter implements Buffer {
	private ArrayList<Measurement> bufferMisurazioni;
	private static SmartMeter instance;

	private SmartMeter() {
		// cosa devo fare la prima volta che viene chiamato il costruttore?
		bufferMisurazioni = new ArrayList<Measurement>();
	}

	//singleton
	public synchronized static SmartMeter getInstance() {
		if (instance == null)
			instance = new SmartMeter();
		return instance;
	}

	public synchronized ArrayList<Measurement> getBufferMisurazioni() {
		// restituisco una copia del buffer
		return new ArrayList<>(bufferMisurazioni);
	}

	@Override
	public void addMeasurement(Measurement m) {
		// devo sincronizzare perché qui aggiungo misurazioni, ma un altro thread consuma le misurazioni
		synchronized (bufferMisurazioni) {
			bufferMisurazioni.add(m);

			// sveglio il consumatore quando il buffer raggiunge le dimensioni della finestra
			if (bufferMisurazioni.size() == 24) {
				bufferMisurazioni.notify();
			}
		}
	}

	public Statistica getLocalStat() {
		List<Measurement> window;

		// devo sincronizzare, perché nel mentre che lo leggo potrebbe essere scritto
		synchronized (bufferMisurazioni) {
			// se il buffer ha meno di 24 misurazioni, mi metto in attesa, perché non posso calcolare la statistica
			if (bufferMisurazioni.size() < 24) {
				try {
					bufferMisurazioni.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			// faccio una copia del buffer (che userò per calcolare la statistica)
			window = new ArrayList<Measurement>(bufferMisurazioni);

			// cancello i primi 12 elementi del buffer (overlap del 50%)
			bufferMisurazioni.subList(0, 12).clear();
		}

		// fuori dal synchronized, perché lavoro sulla copia della lista
		// calcolo la statistica locale
		return computeLocalStat(window);
	}

	private Statistica computeLocalStat(List<Measurement> window) {
		float partialSum = 0;
		int counter = 0;

		for (Measurement m: window) {
			partialSum += m.getValue();
			counter++;
		}

		// divisioni intere
		float value = partialSum / counter;
		long timestamp = System.currentTimeMillis() - computeMidnightMilliseconds();
		Statistica stat = new Statistica(value, timestamp);

		// System.out.println("Nuova statistica LOCALE: " + stat.getValore() + ", " + stat.getTimestamp() + "\n");

		return stat;
	}

	private long computeMidnightMilliseconds(){
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTimeInMillis();
	}
}
