package casa;

import casa.gson.Casa;
import casa.gson.Statistica;
import casa.simulation_src_2019.SmartMeterSimulator;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.*;

public class CasaInfo {
	private static CasaInfo instance;						// istanza del singleton
	private int id;											// id della casa
	private String ip;										// ip della casa
	private int porta;										// porta su cui la casa è in ascolto
	private List<Casa> listaCase;							// contiene tutte le case nella rete
	private Boolean coordinatore;							// dice se sono il coordinatore della rete
	private Object coordinatoreLock;
	private Boolean quitting;								// dice se sto uscendo dalla rete
	private Object quittingLock;
	private HashMap<Integer, Statistica> localStatsMap;		// mappa che contiene, per ogni casa, l'ultima statistica prodotta
	private HashMap<Integer, Boolean> ackMap;				// per ogni casa, memorizzo se mi è arrivato l'ack per il consenso
	private List<Object> boostInfo;							// informazioni relative all'algoritmo di consenso
	/* in posizione 0 memorizzo la stringa indica in che stato sono rispetto alla risorsa:
	 * - "neutral"
	 * - "requested"
	 * - "using"
	 * in posizione 1 memorizzo il timestamp indica a che tempo è stata richiesta la risorsa */
	private Object lock;									// oggetto utilizzato per sincronizzare
	private Boolean ready;									// ho ricevuto risposta da tutte le case all'ingresso
	private Object readyLock;
	private Object localFlag;								// prendo il lock su questo oggetto quando invio le statistiche

	// costruttore privato (deve essere chiamato solo dal singleton)
	private CasaInfo() {
		// valori default di quando viene creata una casa nuova
		coordinatore = false;
		quitting = false;
		ready = false;

		// creazione degli oggetti
		listaCase = new ArrayList<Casa>();
		localStatsMap = new HashMap<Integer, Statistica>();
		ackMap = new HashMap<Integer, Boolean>();
		lock = new Object();
		localFlag = new Object();
		coordinatoreLock = new Object();
		quittingLock = new Object();
		readyLock = new Object();

		// inizializzo le informazioni per il consenso
		boostInfo = new ArrayList<Object>();
		boostInfo.add(0, "neutral");
		boostInfo.add(1, (long)0);
	}

	//singleton
	public synchronized static CasaInfo getInstance() {
		if (instance == null)
			instance = new CasaInfo();
		return instance;
	}

	// non devo sincronizzare su id, ip e porta, perché li scrivo una volta (dal main thread, prima di farne altri che
	// possano leggere e poi non li cambio più)
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPorta() {
		return porta;
	}

	public void setPorta(int porta) {
		this.porta = porta;
	}

	// devo invece sincronizzare su coordinatore, waitingFor, using e quitting, perché può essere scritto e letto da
	// thread diversi: main thread (che crea CasaInfo con cordinatore false) e il thread del service grpc
	public Boolean getCoordinatore() {
		synchronized (coordinatoreLock) {
			return new Boolean(coordinatore);
		}
	}

	public void setCoordinatore(Boolean coordinatore) {
		synchronized (coordinatoreLock) {
			this.coordinatore = coordinatore;
		}
	}

	// devo sincronizzare? Quitting viene sia scritto (dal main thread) che letto (dal service grpc)
	public Boolean getQuitting() {
		synchronized (quittingLock) {
			return new Boolean(quitting);
		}
	}

	public void setQuitting(Boolean quitting) {
		synchronized (quittingLock) {
			this.quitting = quitting;
		}
	}

	// devo sincronizzare la lista delle case perché viene letta e scritta continuamente da thread diversi
	public List<Casa> getListaCase() {
		synchronized (listaCase) {
			// ritorno una copia della lista e non la lista vera e propria
			return new ArrayList<>(listaCase);
		}
	}

	// non dovrebbe essere mai chiamato
	/*public void setListaCase(List<Casa> listaCase) {
		synchronized (this.listaCase) {
			this.listaCase = listaCase;
		}
	}*/

	// devo sincronizzare sulla mappa di statistiche perché viene sia letta che scritta da thread diversi
	// ritorno una copia della mappa, se voglio modificarla devo usare il set
	public HashMap<Integer, Statistica> getLocalStatsMap() {
		synchronized (localStatsMap) {
			return new HashMap<>(localStatsMap);
		}
	}

	/*public void setLocalStatsMap(HashMap<Integer, Statistica> statsMap) {
		synchronized (this.localStatsMap) {
			this.localStatsMap = statsMap;
		}
	}*/

	public HashMap<Integer, Boolean> getAckMap() {
		synchronized (ackMap) {
			return new HashMap<>(ackMap);
		}
	}

	public void setAckMap(HashMap<Integer, Boolean> ackMap) {
		synchronized (this.ackMap) {
			this.ackMap = ackMap;
		}
	}

	public String getBoostStatus() {
		synchronized (boostInfo) {
			return (String) boostInfo.get(0);
		}
	}

	public void setBoosStatus(String value) {
		synchronized (boostInfo) {
			boostInfo.set(0, value);
		}
	}

	public long getBoostRequestTime() {
		synchronized (boostInfo) {
			return (long) boostInfo.get(1);
		}
	}

	public void setBoostRequestTime(long time) {
		synchronized (boostInfo) {
			boostInfo.set(1, time);
		}
	}

	// mi serve l'oggetto vero e proprio per sincronizzare all'esterno
	public List<Object> getBoostInfo() {
		synchronized (boostInfo) {
			return boostInfo;
		}
	}

	// non sincronizzo, perché non viene mai scritto
	public Object getLock() {
		return lock;
	}

	// non dovrei mai poterlo modificare
	/*public void setLock(Object lock) {
		synchronized (this.lock) {
			this.lock = lock;
		}
	}*/

	public Boolean getReady() {
		synchronized (readyLock) {
			return ready;
		}
	}

	public void setReady(Boolean ready) {
		synchronized (readyLock) {
			this.ready = ready;
		}
	}

	public Object getLocalFlag() {
		synchronized (localFlag) {
			return localFlag;
		}
	}

	// aggiungo una nuova casa nella lista
	public boolean addCasa(Casa c) {
		synchronized (quittingLock) {
			if (quitting) {
				// se sto uscendo ritorno false e non faccio nulla
				return false;
			} else {
				// dentro al blocco syncrhonized: dovrebbe essere dentro se fosse: non passo la condizione (quindi quitting
				// = false), ma subito dopo va a true -> può accadere

				// quando aggiungo gli altri nodi all'ingresso nella rete sicuramente non sono in quitting
				synchronized (listaCase) {
					listaCase.add(c);
				}
				return true;
			}
		}
	}

	// aggiungo una nuova statistica locale nella mappa
	public void insertLocalStat(int id, Statistica stat) {
		synchronized (readyLock) {
			if (!ready) {
				// se non sono ancora entrato in rete, ignoro la richiesta
				return;
			}
		}
		// dovrei estendere il blocco synchronized solo nel caso in cui: valuto la condizione ed è false (quindi sono ready)
		// ma subito dopo cambio e divento non ready -> non può succedere mai

		Set<Integer> idInStatsMap = new HashSet<Integer>();
		Set<Integer> idInListaCase = new HashSet<Integer>();
		Map<Integer, Statistica> mapStatsCopy = null;

		synchronized (localStatsMap) {
			// se non c'è la chiave, la aggiungo
			if (!localStatsMap.containsKey(id)) {
				localStatsMap.put(id, stat);
			} else {	// altrimenti, sostituisco l'ultimo valore
				localStatsMap.replace(id, stat);
			}

			// controllo se gli id della lista di case sono un sottoinsieme degli id nella statsMap: potrei anche aver
			// aver ricevuto statistiche da case che nel mentre sono uscite dalla rete
			idInStatsMap = localStatsMap.keySet();

			synchronized (listaCase) {
				for (Casa c: listaCase) {
					idInListaCase.add(c.getId());
				}

				// il controllo posso farlo anche fuori dal synchronized: se una casa esce fa nulla, se una casa entra per
				// ora non la considero, ma la considero al giro dopo
				// non ci sono problemi di inconsistenza comunque
				if (idInStatsMap.containsAll(idInListaCase)) {
					mapStatsCopy = new HashMap<>(localStatsMap);

					// cancello tutti i dati dalla mappa delle statistiche per eviare di mandare valori ridondanti
					localStatsMap.clear();
				}
			}
		}

		// inizializzo la copia della mappa solo se ci sono le condizioni per calcolare la statistica globale
		if (mapStatsCopy != null) {
			// lancio il thread che calcola la globale su questa copia
			StatisticheGlobaliThread statisticheGlobaliThread = new StatisticheGlobaliThread(mapStatsCopy);
			statisticheGlobaliThread.start();
		}
	}

	private void checkForGlobal() {
		Set<Integer> idInStatsMap = new HashSet<Integer>();
		Set<Integer> idInListaCase = new HashSet<Integer>();
		Map<Integer, Statistica> mapStatsCopy = null;

		synchronized (localStatsMap) {
			// controllo se gli id della lista di case sono un sottoinsieme degli id nella statsMap: potrei anche aver
			// aver ricevuto statistiche da case che nel mentre sono uscite dalla rete
			idInStatsMap = localStatsMap.keySet();

			synchronized (listaCase) {
				for (Casa c: listaCase) {
					idInListaCase.add(c.getId());
				}
			}

			// il controllo posso farlo anche fuori dal synchronized: al massimo ho delle statistiche più aggiornate (ma di
			// sicuro non le perdo), se una casa esce fa nulla, se una casa entra per ora non la considero, ma la considero
			// al giro dopo
			// non ci sono problemi di inconsistenza comunque
			if (idInStatsMap.containsAll(idInListaCase)) {
				mapStatsCopy = new HashMap<>(localStatsMap);

				// cancello tutti i dati dalla mappa delle statistiche per eviare di mandare valori ridondanti
				localStatsMap.clear();
			}
		}

		if (mapStatsCopy != null) {
			// lancio il thread che calcola la globale su questa copia
			StatisticheGlobaliThread statisticheGlobaliThread = new StatisticheGlobaliThread(mapStatsCopy);
			statisticheGlobaliThread.start();
		}
	}

	public void sendGlobalStatServer(Statistica globalStat) {
		// sincronizzo su coordinatore perché non voglio che cambi mentre sta svolgendo il suo compito da coordinatore
		synchronized (coordinatoreLock) {
			// se sono il coordinatore, invio le statistiche globali al server
			if (!coordinatore) {
				return;
			}
			// dovrei farlo dentro al blocco se: valuto la condizione e finisco nell'else, quindi sono il coordinatore,
			// ma subito dopo non sono più il coordinatore -> succede solo se sono il coordinatore e sto uscendo dalla
			// rete (dipende da quando fermo il thread di produzione delle statistiche)

			// tengo tutto insieme perché non voglio che il coordinatore cambi in alcun modo intanto che si fa l'invio

			Gson gson = new Gson();

			// creo un client Jersey per la comunicazione con il server amministratore
			Client client = Client.create();
			WebResource resource = client.resource("http://localhost:8080/stats/add/");

			String body = gson.toJson(globalStat);

			// provo a connettermi con il server per mandare la POST
			try {
				ClientResponse response = resource.type("application/json").post(ClientResponse.class, body);
				int responseCode = response.getStatus();
				String responseMessage = response.getEntity(String.class);

				switch (responseCode) {
					case 400:
						// non dovrebbe mai succedere perché non abbiamo comportamenti bizantini
						System.out.println("Errore nell'inserimento della statistica globale: " + responseMessage);
						break;
					case 409:
						// non dovrebbe mai succedere
						System.out.println("Errore nell'inserimento della statistica globale: " + responseMessage);
						break;
					case 200:
						//System.out.println("L'inserimento della statistica globale è andato a buon fine.");
						break;
				}

				// se il coordinatore sta aspettando di uscire dalla rete, lo sveglio
				/*synchronized (CasaInfo.getInstance().getLock()) {
					CasaInfo.getInstance().getLock().notify();
				}*/
			} catch (ClientHandlerException e) {
				System.out.println(e);
				System.out.println("Problemi nella connessione con il server.\n");
			}
		}
	}

	public void extraPowerProcedure(SmartMeterSimulator simulator) {
		long time;

		// scrivo sul mio singleton che sto richiedendo la risorsa
		// se sto usando già la corrente extra, allora esco
		synchronized (boostInfo) {
			if (((String)boostInfo.get(0)).equals("using")) {
				System.out.println("Corrente extra già in uso.\n");
				return;
			}

			if (((String)boostInfo.get(0)).equals("requested")) {
				System.out.println("Hai già richiesto la corrente extra.\n");
				return;
			}

			// salvo lo stato a requested
			boostInfo.set(0, "requested");

			// salvo nel singleton il tempo della richiesta (nel caso mi arrivi un altro messaggio di richiesta)
			time = System.currentTimeMillis();
			boostInfo.set(1, time);
		}
		// non tengo il lock per tutta la proceuda, ma solo quando modifico le informazioni, altrimenti non portò mai leggere

		// faccio una copia della rete nel momento in cui avvio la procedura: so che dovrò aspettare l'ack solo dalle
		// case in questa copia (così non ci sono problemi se arriva qualcun altro nel mentre)
		// getListaCase è sincronizzato su listaCase
		//List<Casa> copiaListaCase = CasaInfo.getInstance().getListaCase();

		// ripulisco la ackmap
		synchronized (ackMap) {
			ackMap.clear();
		}

		/*try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/

		// se sono l'unica casa nella rete, non serve che io faccia richieste: uso semplicemente la risorsa
		// se siamo in due vale la stessa cosa: le risorse in gioco sono due
		// tra la copia e questo punto però ci potrebbe essere stato un ingresso nella rete: devo sincronizzare sulla lista
		// delle case per fare questo controllo
		synchronized (listaCase) {
			if (listaCase.size() <= 2) {
				//useBoost(simulator);
				BoostThread boostThread = new BoostThread(simulator);
				boostThread.start();
			} else {
				// mando una richiesta per utilizzare la risorsa condivisa a tutte le case nella lista
				for (Casa c : listaCase) {
					if (c.getId() != id) {
						// salvo nella mappa degli ack che aspetto un ack da questa casa (inizializzo tutto a false)
						insertInAckMap(c);

						// mando alla casa un messaggio per la richiesta del consenso
						extraPowerRequest(c, time, simulator);
					}
				}
			}
		}
	}

	private void extraPowerRequest(Casa c, long time, SmartMeterSimulator simulator) {
		// costruisco il canale di comunicazione con il servizio di ricezione dei messaggi offerto dalla casa
		final ManagedChannel channel = ManagedChannelBuilder.forTarget(c.getIp() + ":" + c.getPorta())
				.usePlaintext(true).build();

		// creo uno stub non bloccante (classe generata dal proto)
		// uso una chiamata asincrona perché le altre case potrebbero impiegarci del tempo a rispondermi
		MessageServiceGrpc.MessageServiceStub stub = MessageServiceGrpc.newStub(channel);

		// creo la richiesta secondo la modalita' definita nel file proto
		MessageServiceOuterClass.ExtraPowerRequest request = MessageServiceOuterClass.ExtraPowerRequest.newBuilder()
				.setId(id)
				.setResource("extra power")
				.setTime(time)
				.build();

		//System.out.println("Messaggio di EXTRA POWER:\n" + request);

		//invio e gestione handler asincroni
		stub.extraPower(request, new StreamObserver<MessageServiceOuterClass.ExtraPowerResponse>() {
			@Override
			public void onNext(MessageServiceOuterClass.ExtraPowerResponse extraPowerResponse) {
				// cosa fa il client quando riceve l'ack dal server degli altri nodi
				// modifica la mappa degli ack ed aggiunge che ha riceviuto un ack dalla casa con cui sta comunicando
				// controllo anche se ho ricevuto gli ack che sto aspettando
				boolean canBoost = ackArrived(c);

				if (canBoost) {
					useBoost(simulator);
				}
			}

			@Override
			public void onError(Throwable throwable) {
				// sto mandando messaggi su una copia della rete, quindi nel mentre alcune case potrebbero essere uscite
				// non è un problema. Se una casa è uscita, di sicuro non vuole usare la risorsa
				boolean canBoost = ackArrived(c);

				if (canBoost) {
					useBoost(simulator);
				}
			}

			@Override
			public void onCompleted() {
				channel.shutdownNow();
			}
		});
	}

	public void useBoost(SmartMeterSimulator simulator) {
		/*try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/

		// altrimenti stampo due volte il messaggio (accade quando sono da solo a chiedere il boost)
		// specifico che sto usando la risorsa
		synchronized (boostInfo) {
			if (!((String)boostInfo.get(0)).equals("requested")) {
				//System.out.println("Corrente extra già in uso.\n");
				return;
			}

			boostInfo.set(0, "using");
		}

		System.out.println("Sto usando la corrente extra...\n");

		// chiamo il metodo boost
		try {
			simulator.boost();
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println("\nHo finito di usare la corrente extra.\n");

		synchronized (boostInfo) {
			boostInfo.set(0, "neutral");
			boostInfo.set(1, (long)0);

			// avviso tutti quelli in coda che ho finito di usare la risorsa
			boostInfo.notifyAll();
		}
	}

	// aggiungo nella mappa degli ack le case alle quali mando il messaggio per il consenso
	public void insertInAckMap(Casa c) {
		int key = c.getId();

		// devo sincronizzare, perché sto modificando la mappa che verrà poi letta da un altro thread
		synchronized (ackMap) {
			// se la casa non c'è ancora, la aggiungo con associato il valore false (non ho ancora ricevuto l'ack)
			if (!ackMap.containsKey(key)) {
				ackMap.put(key, false);
			}
		}
	}

	// quando mi arriva un ack, lo salvo nella mappa
	public boolean ackArrived(Casa c) {
		// controllo anzitutto se sto aspettando di usare una risorsa
		synchronized (boostInfo) {
			if (!((String)boostInfo.get(0)).equals("requested")) {
				// se non sto aspettando di poter accedere ad una risorsa, allora esco semplicemente
				// ciò può accadere se sto usando una risorsa ma un altro la rilascia nel mentre
				return false;
			}
		}
		// dovrei stare dentro al synchronized se da "requested" posso andare in un altro valore dopo al controllo della
		// condizione -> succede solo se do il boost, ma è questo stesso thread che da il boost

		int key = c.getId();
		HashMap<Integer, Boolean> ackMapCopy;

		// devo sincronizzare, perché sto modificando la mappa che verrà poi letta da un altro thread
		synchronized (ackMap) {
			// deve essere una casa da cui mi sto aspettando una risposta
			if (ackMap.containsKey(key)) {
				ackMap.replace(key, true);
			}

			ackMapCopy = new HashMap<>(ackMap);
		}

		// controllo se il numero di ack è sufficiente a farmi usare la risorsa
		int numberOfAck = 0;

		for (Map.Entry<Integer, Boolean> entry : ackMapCopy.entrySet()) {
			if (entry.getValue() == true) {
				numberOfAck ++;
			}
		}

		// se il numero di ack che ho ricevuto è la dimensione della mappa -1, allora posso usare il boost
		if (numberOfAck >= ackMapCopy.size() - 1) {
			return true;
		} else {
			return false;
		}
	}

	/*public void clearAckMap() {
		synchronized (ackMap) {
			ackMap.clear();
		}
	}*/

	// controllo se per me va bene che qualcun altro usi la risorsa
	public boolean letResource(long time) {
		synchronized (boostInfo) {
			switch ((String)boostInfo.get(0)) {
				case "neutral":
					return true;
				case "requested":
					if ((long)boostInfo.get(1) >= time) {
						return true;
					} else {
						// se ho la precedenza sull'altro, aspetto fino a quando non finisco di usare la risorsa
						try {
							boostInfo.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					break;
				case "using":
					try {
						boostInfo.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					break;
			}
		}

		return true;
	}

	// chiamo questo metodo alla fine del boost per notificare che ora la risorsa è disponibile
	/*public void notifyForResource() {
		synchronized (boostInfo) {
			boostInfo.notifyAll();
		}
	}*/

	// se voglio uscire dalla rete, devo controllare se sto usando la risorsa e, nel caso, devo aspettare
	public void waitResourceRelease() {
		synchronized (boostInfo) {
			if (((String)boostInfo.get(0)).equals("using")) {
				System.out.println("In attesa del rilascio della risorsa...\n");
				try {
					boostInfo.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// viene svegliato dal notifyAll() che sveglia anche le risposte da inviare alle case

				// aspetto ancora qualche secondo perché gli ack vengano inviati, dopo di che esco dalla funzione
				/*try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}*/
			}
		}
	}

	// restituisco la lista di case ordinata per id (serve tutta la lista perché il primo potrebbe non essere disponibile)
	public List<Casa> getSortedListCase() {
		// get listaCase fa la copia della lista in maniera sincronizzata
		List<Casa> listaCase = this.getListaCase();

		Collections.sort(listaCase, Collections.reverseOrder());
		return listaCase;
	}

	// controllo se posso diventare il coordinatore
	public boolean iAmElected() {
		boolean success = false;

		// controllo se sto uscendo dalla rete
		synchronized (quittingLock) {
			if (!quitting) {
				// se non sto uscendo, allora posso diventare il nuovo coordinatore
				success = true;

				synchronized (coordinatoreLock) {
					coordinatore = true;
				}

				System.out.println("Sono il nuovo COORDINATORE della rete di case.\n");
			}
		}

		return success;
	}

	// rimuovo una casa dalla lista delle case
	public void removeCasaById(int idToRemove) {
		Casa casaToRemove = getCasaById(idToRemove);

		// se sto aspettando un ack dalla casa che vuole uscire per usare la risorsa, allora considero questo messaggio
		// come un ack
		if (casaToRemove != null) {
			ackArrived(casaToRemove);
		}

		// se sto aspettando una statistica locale da lui, ma sta uscendo, allora controllo se ho tutte lo statistiche
		// per calcolare la globale
		checkForGlobal();

		// rimuovo la casa dalla lista di case
		synchronized (listaCase) {
			if (casaToRemove != null) {
				listaCase.remove(casaToRemove);
			}
		}
	}

	// cerco una casa per id
	public Casa getCasaById(int id) {
		List<Casa> listaCaseCopy = getListaCase();
		Casa casa = null;

		for (Casa c: listaCaseCopy) {
			if (c.getId() == id) {
				casa = c;
			}
		}

		return casa;
	}
}