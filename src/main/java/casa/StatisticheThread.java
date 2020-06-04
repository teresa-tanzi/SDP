package casa;

import casa.gson.Casa;
import casa.gson.Statistica;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class StatisticheThread extends Thread {
	// osserva il buffer dello smart meter: quando viene risvegliato dalla notify che riempie lo smart meter, fa una copia
	// della finestra e vi calcola la statistica locale
	private boolean stopCondition;

	public StatisticheThread() {
		stopCondition = false;
	}

	public void run() {
		while (!stopCondition) {
			// calcola la statistica locale
			// il metodo chiamato sullo smart meter va in sleep fino a quando non viene risvegliato (ci sono 24 misurazioni
			// nel buffer)
			Statistica localStat = SmartMeter.getInstance().getLocalStat();

			// aggiungo la statistica che ho appena prodotto nella mia mappa di statistiche (metodo sincronizzato)
			CasaInfo.getInstance().insertLocalStat(CasaInfo.getInstance().getId(), localStat);

			// prendo il lock su statsFlag, quindi non posso uscire dalla rete mentre sono in questo blocco
			synchronized (CasaInfo.getInstance().getLocalFlag()) {
				// invia la statistica locale a tutte le case nella rete
				sendStatAll(localStat);

				/*try {
					Thread.sleep(15000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}*/

				// invio la statistica locale al server amministratore
				sendStatServer(localStat);
			}
		}

		System.out.println("Chiusura del thread delle statistiche in corso...");
	}

	private void sendStatAll(Statistica stat) {
		// ritorna una copia della lista di case: mentre mando le statistiche, qualcuna di queste potrebbe anche uscire
		// dalla rete (non è un problema)
		List<Casa> listaCase = CasaInfo.getInstance().getListaCase();

		// mando la statistica locale che ho appena calcolato a tutte le case nella rete
		for (Casa c : listaCase) {
			if (c.getId() != CasaInfo.getInstance().getId()) {
				sendStat(stat, c);
			}
		}
	}

	private void sendStat(Statistica stat, Casa c) {
		int myId = CasaInfo.getInstance().getId();

		// costruisco il canale di comunicazione con il servizio di ricezione dei messaggi offerto dalla casa
		final ManagedChannel channel = ManagedChannelBuilder.forTarget(c.getIp() + ":" + c.getPorta())
				.usePlaintext(true).build();

		// creo uno stub non bloccante (classe generata dal proto)
		// uso una chiamata asincrona perché non mi importa di ricevere un ack per proseguire con il lavoro
		MessageServiceGrpc.MessageServiceStub stub = MessageServiceGrpc.newStub(channel);

		// creo la richiesta secondo la modalita' definita nel file proto
		MessageServiceOuterClass.InvioStat request = MessageServiceOuterClass.InvioStat.newBuilder()
				.setId(myId)
				.setStat(MessageServiceOuterClass.InvioStat.Statistica.newBuilder()
						.setValore(stat.getValore())
						.setTimestamp(stat.getTimestamp()))
				.build();

		//System.out.println("Messaggio di INVIO STATISTICA:\n" + request);

		//invio e gestione handler asincroni
		stub.localStat(request, new StreamObserver<MessageServiceOuterClass.AckStat>() {
			@Override
			public void onNext(MessageServiceOuterClass.AckStat ackStat) {
				// cosa fa il client quando riceve l'ack dal server degli altri nodi
				// assumiamo che non ci siano fallimenti incontrollati, quindi non mi serve l'ack per sapere che il
				// messaggio è stato ricevuto
			}

			@Override
			public void onError(Throwable throwable) {
				// sto mandando messaggi su una copia della rete, quindi nel mentre alcune case potrebbero essere uscite
				// non è un problema
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

	private void sendStatServer(Statistica stat) {
		Gson gson = new Gson();
		int myId = CasaInfo.getInstance().getId();

		// creo un client Jersey per la comunicazione con il server amministratore
		Client client = Client.create();
		WebResource resource = client.resource("http://localhost:8080/stats/add/" + myId);

		String body = gson.toJson(stat);

		// provo a connettermi con il server per mandare la POST
		try {
			ClientResponse response = resource.type("application/json").post(ClientResponse.class, body);
			int responseCode = response.getStatus();
			String responseMessage = response.getEntity(String.class);

			switch (responseCode) {
				case 400:
					// non dovrebbe mai succedere perché non abbiamo comportamenti bizantini
					System.out.println("Errore nell'inserimento della statistica locale: " + responseMessage);
					break;
				case 409:
					// non dovrebbe mai succedere
					System.out.println("Errore nell'inserimento della statistica locale: " + responseMessage);
					break;
				case 200:
					//System.out.println("L'inserimento della statistica locale è andato a buon fine.");
			}
		} catch (ClientHandlerException e) {
			// non mi aspetto fallimenti del server: non dovrebbe mai accadere
			System.out.println(e);
			System.out.println("Problemi nella connessione con il server.\n");
		}
	}

	public void stopThread() {
		stopCondition = true;
	}
}
