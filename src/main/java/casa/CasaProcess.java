package casa;

import casa.gson.Casa;
import casa.gson.ListaCase;
import casa.simulation_src_2019.SmartMeterSimulator;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import io.grpc.*;
import io.grpc.stub.StreamObserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class CasaProcess {
	// Una casa deve essere inizializzata specificando il suo identificatore, il numero di porta di ascolto per la
	// comunicazione tra case e l’indirizzo del server amministratore
	private static int id;
	private static final String ip = "localhost";
	private static int porta;
	private static final String serverIp = "localhost";
	private static final int serverPort = 8080;

	// per capire se diventare il coordinatore devo vedere quanti nella rete che ricevo dal server mi rispondono di esserci
	private static int inNetwork = 0;

	private static Integer ackCounter;				// conto quante case riesco a contattare quando faccio l'ingresso nella rete
	private static Object ackLock = new Object();	// serve perché il lock su un Integer da problemi
	// https://stackoverflow.com/questions/25733718/illegalmonitorstateexception-on-notify-when-synchronized-on-an-integer

	private static Integer goodbyeCounter;
	private static Object goodbyeLock = new Object();

	public static void main (String[] args) {
		// Una volta avviato, il processo casa deve registrarsi al condominio tramite il server amministratore.
		// contatto il server amministratore per ottenere la lista di case
		List<Casa> listaCasefFromServer = helloServer();

		// inserisco nel singleton della casa le informazioni di id, ip e porta (sono state accettate dal server)
		// non sono sincronizzati perché li scrivo solo adesso che sono l'unico thread attivo
		CasaInfo casaInfo = CasaInfo.getInstance();
		casaInfo.setId(id);
		casaInfo.setIp(ip);
		casaInfo.setPorta(porta);

		// Se l’inserimento va a buon fine (ovvero, non esistono altre case con lo stesso identificatore), la casa riceve
		// dal server l’elenco di case presenti nella rete peer-to-peer, in modo tale da poter presentarsi per entrare in
		// maniera decentralizzata nella rete.

		Server messageServer = null;

		// faccio partire il servizio di ricezione dei messaggi sulla porta della casa
		try {
			messageServer = ServerBuilder.forPort(porta).addService(new MessageServiceImpl()).build();
			messageServer.start();
			System.out.println("Il server per la ricezione dei messaggi è stato avviato sulla porta: " + porta + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}

		// inizializzo il counter degli ack con il numero di case che devo contattare per entrare nella rete
		// ancora sono l'unico thread attivo, quindi posso farlo in maniera non sincronizzata
		ackCounter = listaCasefFromServer.size() - 1;

		// contatto tutte le case restituite dal server per fare l'ingresso decentralizzato nella rete peer-to-peer
		enterNetwork(listaCasefFromServer);

		// Una volta avviato, il processo casa deve avviare il suo smart meter
		SmartMeter smartMeter = SmartMeter.getInstance();
		SmartMeterSimulator simulator = new SmartMeterSimulator(smartMeter);
		simulator.start();
		System.out.println("Lo smart meter è stato avviato.\n");

		// avvio il thread che si occupa di calcolare le statistiche locali
		StatisticheThread statsThread = new StatisticheThread();
		statsThread.start();

		System.out.println("Premere 'b' per richiedere il boost di corrente e premere 'q' per uscire dalla rete di case.\n");

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		while (true) {
			try {
				String input = br.readLine();

				switch (input) {
					case "b":
						System.out.println("Richiesta di corrente extra effettuata...");
						//extraPowerProcedure(simulator);
						CasaInfo.getInstance().extraPowerProcedure(simulator);
						break;
					case "q":
						System.out.println("Uscita della casa dalla rete in corso...");
						exitNetwork(statsThread, simulator, messageServer);
						break;
					default:
						System.out.println("La stringa inserita non corrisponde ad alcun comando disponibile.\n");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static List<Casa> helloServer() {
		// genero l'id della casa ed il suo numero di porta in maniera casuale
		id = generateId();
		porta = generatePort();
		System.out.println("Id: " + id + ", porta: " + porta + "\n");

		// creo un client Jersey per la comunicazione con il server amministratore
		Client client = Client.create();
		WebResource resource = client.resource("http://" + serverIp + ":" + serverPort + "/case/add");

		Gson gson = new Gson();
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		boolean conflict = true;		// dice se ci sono stati dei conflitti di id o di porta, quindi bisogna riprovare
		String responseMessage = "";	// conterrà la risposta ricevuta dal server
		int tentativi = 9;

		while (conflict) {
			if (tentativi == 0) {
				System.out.println("Numero massimo di tentativi raggiunto. Chiusura della casa in corso...\n");
				System.exit(0);
			}

			// costruisco il corpo del messaggio (contiene le informazioni della casa)
			Casa casa = new Casa(id, ip, porta);
			String body = gson.toJson(casa);

			System.out.println("Connessione con il server amministratore...");
			// provo a connettermi con il server per mandare la richiesta di inserimento
			try {
				ClientResponse response = resource.accept("application/json").type("application/json")
						.post(ClientResponse.class, body);
				int responseCode = response.getStatus();
				responseMessage = response.getEntity(String.class);

				switch (responseCode) {
					case 200:
						// l'inserimento è andato a buon fine
						System.out.println("L'inserimento della casa all'interno del server amministratore è andato a buon fine.\n");
						conflict = false;
						break;

					case 400:
						// non dovrebbe mai accadere
						System.out.println("Errore nella formulazione della richiesta! Codice d'errore HTTP: " + responseCode);
						System.out.println(responseMessage);

						switch (responseMessage) {
							case "L'id di una casa non può essere un numero negativo":
								id = generateId();
								System.out.println("Nuovo id: " + id + "\n");
								break;
							case "La porta di una casa non può essere un numero negativo":
								porta = generatePort();
								System.out.println("Nuova porta: " + porta + "\n");
								break;
						}

						tentativi--;

						break;

					case 409:
						System.out.println("Errore nei parametri della richiesta! Codice d'errore HTTP: " + responseCode);
						System.out.println(responseMessage);

						switch (responseMessage) {
							case "La casa ha lo stesso id di una casa già esistente nella rete":
								id = generateId();
								System.out.println("Nuovo id: " + id + "\n");
								break;
							case "La casa ha la stessa porta utilizzata da un'altra casa nella rete":
								porta = generatePort();
								System.out.println("Nuova porta: " + porta + "\n");
								break;
						}

						tentativi--;

						break;
				}
			} catch (ClientHandlerException e) {
				System.out.println(e);
				System.out.println("Problemi nella connessione con il server amministratore. Vuoi ripovare? (s/n)");

				checkInput(br);
			}
		}

		List<Casa> listaCase = gson.fromJson(responseMessage, ListaCase.class).getListaCase();
		List<Integer> listaId = new ArrayList<Integer>();

		for (Casa c: listaCase) {
			listaId.add(c.getId());
		}

		System.out.println("Lista delle case ricevuta dal SERVER: " + listaId + "\n");

		return listaCase;
	}

	private static int generateId() {
		Random rand = new Random();
		return rand.nextInt(100) + 1;
	}

	private static int generatePort() {
		int min = 1025;
		int max = 65535;
		Random rand = new Random();

		int result = 8080;

		// controllo che l'id della casa non sia lo stesso del server
		while (result == 8080) {
			result = rand.nextInt(max - min) + min;
		}

		return result;
	}

	private static void checkInput(BufferedReader br) {
		boolean correctInput = false;

		while (!correctInput) {
			try {
				String input = br.readLine();

				switch (input) {
					case "s":
						correctInput = true;
						break;
					case "n":
						correctInput = true;
						System.out.println("Il processo verrà chiuso...");
						System.exit(0);
						break;
					default:
						System.out.println("Inserire 's' per fare un'altra richiesta o 'n' per chiudere il programma. Riprovare.\n");
						break;
				}
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	private static void enterNetwork(List<Casa> listaCaseFromServer) {
		System.out.println("Inserimento nella rete peer-to-peer in corso...");

		for (Casa c: listaCaseFromServer) {
			if (id != c.getId()) {
				// scrivo alla casa per presentarmi
				helloRequest(c);
			} else {
				// io sono vivo, quindi sono nella lista delle case della rete
				CasaInfo.getInstance().addCasa(new Casa(id, ip, porta));
			}
		}

		// controllo se ho già ricevuto una risposta da tutti, altrimenti vado in wait
		synchronized (ackLock) {
			if (ackCounter > 0) {
				try {
					ackLock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("Ho contattato tutte le case nella rete.");
			}

			CasaInfo.getInstance().setReady(true);
			// con ready a true sblocco l'inserimento delle statistiche ricevute dalle altre case
		}

		// se nella lista di case che costruisco non c'è nessuno oltre a me allora divento io il coordinatore
		// nel mentre potrebbe essere arrivato qualcun altro, quindi per capire se sono il coordinatore devo contare
		// quanti mi hanno realmente risposto di quelli che ho contattato dal server
		// non rincronizzo su inNetwork, perché ho già ricevuto tutti i messaggi che potevano cambiare questo valore
		// (ho aspettato prima)
		if (inNetwork == 0) {
			CasaInfo.getInstance().setCoordinatore(true);
			System.out.println("Sono il COORDINATORE della rete di case.");
		}

		List<Integer> idList = new ArrayList<Integer>();

		for (Casa c: CasaInfo.getInstance().getListaCase()) {
			idList.add(c.getId());
		}

		System.out.println("Ingresso nella rete effettuato. Questa è la lista delle case: " + idList + "\n");
	}

	private static void helloRequest(Casa c) {
		// costruisco il canale di comunicazione con il servizio di ricezione dei messaggi offerto dalla casa
		final ManagedChannel channel = ManagedChannelBuilder.forTarget(c.getIp() + ":" + c.getPorta())
				.usePlaintext(true).build();

		// creo uno stub (classe generata dal proto)
		// uso una chiamata asincrona perché voglio fare queste comunicazioni in parallelo (aspetto poi le risposte)
		MessageServiceGrpc.MessageServiceStub stub = MessageServiceGrpc.newStub(channel);

		// creo la richiesta secondo la modalita' definita nel file proto
		MessageServiceOuterClass.HelloRequest request = MessageServiceOuterClass.HelloRequest.newBuilder()
				.setId(id)
				.setIp(ip)
				.setPorta(porta)
				.build();

		//System.out.println("Richiesta di HELLO:\n" + request);

		//invio e gestione handler asincroni
		stub.hello(request, new StreamObserver<MessageServiceOuterClass.HelloResponse>() {
			@Override
			public void onNext(MessageServiceOuterClass.HelloResponse helloResponse) {
				// cosa fa il client quando riceve l'ack dal server degli altri nodi
				// controllo il contenuto del messaggio: se true aggiungo la casa, se false non faccio nulla
				boolean response = helloResponse.getOk();

				if (response) {
					CasaInfo.getInstance().addCasa(c);

					// le richieste sono asincrone e sono gestite da thread differenti, quindi devo sincronizzare sull'
					// incremento della variabile
					// scopro che c'è un altro nodo nella rete: mi serve per capire se diventare coordinatore
					synchronized (ackLock) {
						inNetwork++;
					}
				} else {
					System.out.println("La casa " + c.getId() + "sta uscendo dalla rete.");
				}

				// ho ricevuto una risposta dalla casa contattata
				synchronized (ackLock) {
					ackCounter--;

					if (ackCounter == 0) {
						try {
							ackLock.notify();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}

			@Override
			public void onError(Throwable throwable) {
				// non riesco a contattare la casa, quindi probabilmente è già uscita dalla rete

				if (throwable.getMessage().equals("UNAVAILABLE")) {
					System.out.println("Non riesco a contattare la casa " + c.getId() + ", deve essere uscita dalla rete.");

					// ho comunque un'informazione sulla casa, quindi decremento il counter
					synchronized (ackLock) {
						ackCounter--;

						if (ackCounter == 0) {
							ackLock.notify();
						}
					}
				}
			}

			@Override
			public void onCompleted() {
				channel.shutdownNow();
			}
		});

		try {
			//you need this. otherwise the method will terminate before that answers from the server are received
			channel.awaitTermination(10, TimeUnit.SECONDS);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void exitNetwork(StatisticheThread statsThread, SmartMeterSimulator simulator, Server messageServer) {
		// salvo nel singleton che sto uscendo dalla rete
		// il set della variabile è già sincronizzato
		CasaInfo.getInstance().setQuitting(true);

		// controllo se la casa sta usando la risorsa, in questo caso devo aspettare finché finisce
		CasaInfo.getInstance().waitResourceRelease();

		// controllo se sono il coordinatore: in questo caso devo fare un'elezione
		// se però non c'è nessun altro nella rete, esco semplicemente
		// non serve sincronizzare, perché non posso diventare coordinatore se sto uscendo (quitting è a true)
		if (CasaInfo.getInstance().getCoordinatore()) {
			// mi metto in wait fino a che mi arriva una notify generata dall'invio delle statistiche globali al server
			/*synchronized (CasaInfo.getInstance().getLock()) {
				try {
					System.out.println("\nAspetto di inviare le statistiche globali al server prima di uscire...");
					CasaInfo.getInstance().getLock().wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("Statistiche globali inviate.\n");
				// mi sveglio dopo che ho inviato al server le statistiche globali
			}*/

			election();
			// quando esco non sono più il coordinatore
		}

		// dico al server amministratore che sto uscendo dalla rete di case
		// lo faccio subito, così se arriva una nuova casa non mi vede nella lista del server e non cerca di contattarmi
		exitRequestServer();

		// esco dalla rete solo se non sto inviando statistiche locali a nessuno
		System.out.println("Finisco di inviare le statistiche locali a tutti...");
		synchronized (CasaInfo.getInstance().getLocalFlag()) {
			// termino il simulatore ed i thread di produzione delle statistiche
			statsThread.stopThread();
			simulator.stopMeGently();

			// sono in quitting, quindi non aggiungo nessuno di nuovo. Al massimo qualcuno esce nel mentre, ma non è un problema
			// (fallirà la connessione)
			List<Casa> listaCaseCopy = CasaInfo.getInstance().getListaCase();

			// non sincronizzo, perché ancora non sono stati avviati i thread che leggono e modificano il counter
			goodbyeCounter = listaCaseCopy.size() - 1;	// - 1 perché ci sono io

			// contatto tutte le case della rete per avvisarle che sto uscendo
			for (Casa c : listaCaseCopy) {
				if (c.getId() != CasaInfo.getInstance().getId()) {
					goodbyeRequest(c);
				}
			}

			synchronized (goodbyeLock) {
				if (goodbyeCounter > 0) {
					try {
						goodbyeLock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

			// chiudo il mio server di comunicazione con le altre case
			messageServer.shutdown();
			// dico al server di cancellare la lista delle mie statistiche dal server
			removeStatsFromServer();
		}

		// ho avvisato tutte le case nella rete della mia uscita, quindi termino il processo
		System.out.println("L'uscita dalla rete è andata a buon fine.");
		System.out.println("Terminazione del processo...");

		/*try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/

		System.exit(0);
	}

	private static void election() {
		// sono il coordiantore e voglio uscire dalla rete, quindi chiedo a qualcun altro di divenare coordinatore al posto mio
		// cerco nella mia lista di case quella con id più alto (lavoro su una copia della lista: non devo sincronizzare)
		List<Casa> listaCaseOrdinata = CasaInfo.getInstance().getSortedListCase();
		int index = 0;		// indice nella lista delle case ordinate che corrisponde alla casa da contattare
		Casa possibleElected = listaCaseOrdinata.get(index);

		// se ci sono solo io nella lista di case, esco senza eleggere nessuno
		// non sincronizzo sulla lista di case perché sono in uscita, quindi nessuno si può aggiungere alla rete. Al massimo
		// si possono togliere, ma se sono 1 resto 1
		if (listaCaseOrdinata.size() - 1 == 0) {
			return;
		}

		// se sono io la casa con id più alto, allora devo contattare la seconda
		if (possibleElected.getId() == CasaInfo.getInstance().getId()) {
			index++;
			possibleElected = listaCaseOrdinata.get(index);
		}

		// contatto la casa per vedere se non sta uscendo dalla rete e può essere eletta
		boolean electionCompleted = electionRequest(possibleElected);

		// se la casa ha detto di no, chiedo alla casa dopo nella rete, fino a che non termina la lista
		while (!electionCompleted && index != listaCaseOrdinata.size() - 1) {
			index++;
			possibleElected = listaCaseOrdinata.get(index);
			System.out.println("Sto mandando il messaggio di election a " + possibleElected.getId());
			if (possibleElected.getId() != CasaInfo.getInstance().getId()) {
				electionCompleted = electionRequest(possibleElected);
			}
		}

		if (electionCompleted) {
			System.out.println("Ho eletto come nuovo COORDINATORE la casa " + listaCaseOrdinata.get(index).getId());
		}
		// se non ci sono più case nella lista a cui chiedere vuol dire che stanno tutte uscendo, allora esco anche io
		// senza problemi: il prossimo che entrerà nella rete diventerà automaticamente coordinatore

		/*try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/

		// terminata l'elezione, non sono più coordinatore, quindi cambio la mia variaible nel singleton
		// questo anche se non trovo un altro coordinatore. Tanto esco dalla lista di case, ce ne sarà un altro poi
		CasaInfo.getInstance().setCoordinatore(false);
	}

	private static boolean electionRequest(Casa c) {
		// System.out.println("Sto scrivendo alla casa " + c.getId() + " sulla porta " + c.getPorta());

		// costruisco il canale di comunicazione con il servizio di ricezione dei messaggi offerto dalla casa
		final ManagedChannel channel = ManagedChannelBuilder.forTarget(c.getIp() + ":" + c.getPorta())
				.usePlaintext(true).build();

		// creo un blocking stub (classe generata dal proto)
		// uso una chiamata sincrona perché voglio aspettare la risposta per vedere se devo eleggere qualcun altro
		MessageServiceGrpc.MessageServiceBlockingStub stub = MessageServiceGrpc.newBlockingStub(channel);

		// creo la richiesta secondo la modalita' definita nel file proto
		MessageServiceOuterClass.ElectionRequest request = MessageServiceOuterClass.ElectionRequest.newBuilder()
				.setId(CasaInfo.getInstance().getId())
				.build();

		System.out.println("Richiesta di ELECTION:\n" + request);

		boolean electionCompleted;

		// chiamo la procedura remota bloccante
		try {
			MessageServiceOuterClass.ElectionResponse response = stub.election(request);
			System.out.println("[SERVER CASA " + c.getId() + "] " + response.getId());

			electionCompleted = response.getCoordinator();

			// chiudo il canale
			//System.out.println("Chiusura del canale.\n");
			channel.shutdown();
		} catch (StatusRuntimeException e) {
			// può accadere che io non riesca a contattare l'altra casa perché sto pur sempre lavorando su una copia
			System.out.println(e);
			electionCompleted = false;

			// chiudo il canale
			//System.out.println("Chiusura del canale.\n");
			channel.shutdown();
		}

		return electionCompleted;
	}

	private static boolean exitRequestServer() {
		int id = CasaInfo.getInstance().getId();
		boolean exitCompleted = false;

		// creo un client Jersey per la comunicazione con il server amministratore
		Client client = Client.create();
		WebResource resource = client.resource("http://" + serverIp + ":" + serverPort + "/case/remove/" + Integer.toString(id));

		// provo a connettermi con il server per mandare la DELETE
		try {
			ClientResponse response = resource.delete(ClientResponse.class);
			int responseCode = response.getStatus();
			String responseMessage = response.getEntity(String.class);

			switch (responseCode) {
				case 200:       // la casa è stata correttamente rimossa dalla rete
					System.out.println("La richiesta di rimozione dal server è andata a buon fine.\n");
					exitCompleted = true;
					break;
				case 400:       // l'id specificato è un id negativo
					System.out.println("Errore nella richiesta! Codice d'errore HTTP: " + responseCode);
					System.out.println(responseMessage);
					System.out.println("");
					break;
				case 409:       // l'id specificato non corrisponde ad una casa esistente nella rete
					System.out.println("Errore nella richiesta! Codice d'errore HTTP: " + responseCode);
					System.out.println(responseMessage);
					System.out.println("");
					break;
			}
		} catch (ClientHandlerException e) {
			// non dovrebbe mai accadere, perché non mi aspetto fallimenti del server una volta che partito
			System.out.println(e);
			System.out.println("Problemi nella connessione con il server.\n");
		}

		return exitCompleted;
	}

	private static void goodbyeRequest(Casa c) {
		// System.out.println("Sto scrivendo alla casa " + c.getId() + " sulla porta " + c.getPorta());

		// costruisco il canale di comunicazione con il servizio di ricezione dei messaggi offerto dalla casa
		final ManagedChannel channel = ManagedChannelBuilder.forTarget(c.getIp() + ":" + c.getPorta())
				.usePlaintext(true).build();

		// creo un blocking stub (classe generata dal proto)
		// uso una chiamata sincrona perché voglio aspettare tutte le risposte prima di uscire dalla rete
		MessageServiceGrpc.MessageServiceStub stub = MessageServiceGrpc.newStub(channel);

		// creo la richiesta secondo la modalita' definita nel file proto
		MessageServiceOuterClass.GoodbyeRequest request = MessageServiceOuterClass.GoodbyeRequest.newBuilder()
				.setId(id)
				.build();

		//System.out.println("Richiesta di GOODBYE:\n" + request);
		stub.goodbye(request, new StreamObserver<MessageServiceOuterClass.GoodbyeResponse>() {
			@Override
			public void onNext(MessageServiceOuterClass.GoodbyeResponse goodbyeResponse) {
				// cosa fa il client quando riceve l'ack dal server degli altri nodi
				// non faccio nulla, semplicemente incremento il counter di ack in uscita

				// ho ricevuto una risposta dalla casa contattata
				synchronized (goodbyeLock) {
					goodbyeCounter--;

					if (goodbyeCounter == 0) {
						try {
							goodbyeLock.notify();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}

			@Override
			public void onError(Throwable throwable) {
				// non riesco a contattare una casa
				// non dovrebbe accadere, perché le uscite sono controllate, però è anche vero che sto lavorando su una copia

				synchronized (goodbyeLock) {
					goodbyeCounter--;

					if (goodbyeCounter == 0) {
						try {
							goodbyeLock.notify();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}

			@Override
			public void onCompleted() {
				channel.shutdownNow();
			}
		});

		try {
			//you need this. otherwise the method will terminate before that answers from the server are received
			channel.awaitTermination(10, TimeUnit.SECONDS);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void removeStatsFromServer() {
		int id = CasaInfo.getInstance().getId();

		// creo un client Jersey per la comunicazione con il server amministratore
		Client client = Client.create();
		WebResource resource = client.resource("http://" + serverIp + ":" + serverPort + "/stats/remove/" + Integer.toString(id));

		// provo a connettermi con il server per mandare la DELETE
		try {
			ClientResponse response = resource.delete(ClientResponse.class);
			int responseCode = response.getStatus();
			String responseMessage = response.getEntity(String.class);

			switch (responseCode) {
				case 200:       // la casa è stata correttamente rimossa dalla rete
					System.out.println("La richiesta di rimozione delle statistiche dal server è andata a buon fine.\n");
					break;
				case 400:       // l'id specificato è un id negativo
					System.out.println("Errore nella richiesta! Codice d'errore HTTP: " + responseCode);
					System.out.println(responseMessage);
					System.out.println("");
					break;
				case 404:       // la casa non esiste o non ha mai prodotto statistiche
					System.out.println("Errore nella richiesta! Codice d'errore HTTP: " + responseCode);
					System.out.println(responseMessage);
					System.out.println("");
					break;
			}
		} catch (ClientHandlerException e) {
			// non dovrebbe mai accadere, perché non mi aspetto fallimenti del server una volta che partito
			System.out.println(e);
			System.out.println("Problemi nella connessione con il server.\n");
		}
	}
}
