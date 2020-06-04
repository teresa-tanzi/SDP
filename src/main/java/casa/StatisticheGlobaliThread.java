package casa;

import casa.gson.Statistica;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import java.util.HashMap;
import java.util.Map;

public class StatisticheGlobaliThread extends Thread {
	HashMap<Integer, Statistica> statsMapCopy;

	public StatisticheGlobaliThread(Map<Integer, Statistica> statsMapCopy) {
		this.statsMapCopy = new HashMap<>(statsMapCopy);
	}

	public void run() {
		// metodo sincronizzato che aspetta fino a quando la mappa delle statistiche locali non è piena
		Statistica globalStat = computeGlobalStat(statsMapCopy);

		// Ogni casa deve periodicamente stampare a schermo il consumo complessivo del condominio
		System.out.println("Nuova statistica GLOBALE: " + globalStat.getValore() + ", " + globalStat.getTimestamp());

		// Le case devono coordinarsi per inviare periodicamente al Server Amministratore il consumo complessivo del
		// condominio (statistica globale)
		// se sono il coordinatore, faccio l'upload delle globali sul server amministratore
		// non sincronizzo: basta che al momento in cui inizio sono il coordinatore. Quando esco dalla rete ed eleggo un
		// altro coordinatore, mi assicuro di aver finito l'upload delle statistiche globali prima

		// se sono il coordinatore, allora invio le statistiche globali al server
		CasaInfo.getInstance().sendGlobalStatServer(globalStat);
	}

	// lavora su una copia della mappa di statistiche, quindi non ho porblemi di sincronizzazione
	private Statistica computeGlobalStat(HashMap<Integer, Statistica> statsMap) {
		float globalValue = 0;
		int sumTimestamp = 0;
		int counter = 0;

		for (HashMap.Entry me: statsMap.entrySet()) {
			globalValue += ((Statistica)me.getValue()).getValore();
			sumTimestamp += ((Statistica)me.getValue()).getTimestamp();
			counter++;
		}

		Statistica globalStat = new Statistica(globalValue, sumTimestamp/counter);

		return globalStat;
	}
}
