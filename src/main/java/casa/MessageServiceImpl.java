package casa;

import casa.gson.Casa;
import casa.gson.Statistica;
import io.grpc.stub.StreamObserver;

public class MessageServiceImpl extends MessageServiceGrpc.MessageServiceImplBase {
	@Override
	public void hello(MessageServiceOuterClass.HelloRequest request, StreamObserver<MessageServiceOuterClass.HelloResponse> responseObserver) {
		//super.hello(request, responseObserver);
		// la richiesta contiene le informazioni di una casa
		System.out.println("\nHo ricevuto il seguente messaggio di HELLO:\n" + request);

		// se sto uscendo dalla rete, allora non aggiungo la casa alla mia lista e le dico di non aggiungere me
		// se invece non sto uscendo, allora aggiungo la casa nella lista
		// faccio questi controlli in addCasa, perché devo sincronizzare sulla variabile quitting del singleton.
		boolean ack = CasaInfo.getInstance().addCasa(new Casa(request.getId(), request.getIp(), request.getPorta()));

		// costruisco la risposta
		MessageServiceOuterClass.HelloResponse response = MessageServiceOuterClass.HelloResponse.newBuilder()
				.setOk(ack)
				.setId(CasaInfo.getInstance().getId())	// non ho problemi di sincronizzazione sull'id
				.build();

		/*try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/

		System.out.println("Invio la risposta al messaggio di HELLO:\n" + response);

		// passo la risposta nello stream
		responseObserver.onNext(response);

		// completo e finisco la comunicazione
		responseObserver.onCompleted();
	}

	@Override
	public void localStat(MessageServiceOuterClass.InvioStat request, StreamObserver<MessageServiceOuterClass.AckStat> responseObserver) {
		// super.localStat(request, responseObserver);
		// la richiesta contiene una statistica locale
		// System.out.println("\nHo ricevuto il seguente messaggio di LOCAL STAT:\n" + request);

		// unwrap del messaggio
		int remoteId = request.getId();
		Statistica localStat = new Statistica(request.getStat().getValore(), request.getStat().getTimestamp());

		// aggiungo la statistica locale alla mia mappa di statistiche (metodo sincronizzato)
		CasaInfo.getInstance().insertLocalStat(remoteId, localStat);

		// costruisco la risposta
		MessageServiceOuterClass.AckStat response = MessageServiceOuterClass.AckStat.newBuilder()
				.setId(CasaInfo.getInstance().getId())
				.setAck(true)
				.build();

		// System.out.println("Invio la risposta al messaggio di LOCAL STAT:\n" + response);

		// passo la risposta nello stream
		responseObserver.onNext(response);

		// completo e finisco la comunicazione
		responseObserver.onCompleted();
	}

	@Override
	public void extraPower(MessageServiceOuterClass.ExtraPowerRequest request, StreamObserver<MessageServiceOuterClass.ExtraPowerResponse> responseObserver) {
		//super.extraPower(request, responseObserver);
		System.out.println("\nHo ricevuto il seguente messaggio di EXTRA POWER:\n" + request);

		// unwrap del messaggio
		int remoteId = request.getId();
		long remoteTime = request.getTime();

		// controllo se per me va bene che l'altro usi la risorsa
		// questa funzione va in wait se le condizione non sono soddifìsfatte, quindi sono sicuro che quando la funzione
		// termina posso rilasciare la risorsa
		boolean letResource = CasaInfo.getInstance().letResource(remoteTime);

		// costruisco la risposta
		MessageServiceOuterClass.ExtraPowerResponse response = MessageServiceOuterClass.ExtraPowerResponse.newBuilder()
				.setId(CasaInfo.getInstance().getId())
				.setOk(true)
				.build();

		System.out.println("Invio la risposta al messaggio di EXTRA POWER:\n" + response);

		// passo la risposta nello stream
		responseObserver.onNext(response);

		// completo e finisco la comunicazione
		responseObserver.onCompleted();
	}

	@Override
	public void election(MessageServiceOuterClass.ElectionRequest request, StreamObserver<MessageServiceOuterClass.ElectionResponse> responseObserver) {
		//super.election(request, responseObserver);
		System.out.println("\nHo ricevuto il seguente messaggio di ELECTION:\n" + request);

		// controllo se posso diventare il coordinatore (devo sincronizzare su quitting che potrebbe essere scritto dal
		// main thread)
		boolean success = CasaInfo.getInstance().iAmElected();

		// costruisco la risposta
		MessageServiceOuterClass.ElectionResponse response = MessageServiceOuterClass.ElectionResponse.newBuilder()
				.setCoordinator(success)
				.setId(CasaInfo.getInstance().getId())
				.build();

		System.out.println("Invio la risposta di ELECTION: " + response);
		System.out.println("");

		// passo la risposta nello stream
		responseObserver.onNext(response);

		// completo e finisco la comunicazione
		responseObserver.onCompleted();
	}

	@Override
	public void goodbye(MessageServiceOuterClass.GoodbyeRequest request, StreamObserver<MessageServiceOuterClass.GoodbyeResponse> responseObserver) {
		//super.goodbye(request, responseObserver);
		System.out.println("\nHo ricevuto il seguente messaggio di GOODBYE:\n" + request);

		// rimuovo la casa che mi ha fatto la richiesta dalla mia lista delle case
		CasaInfo.getInstance().removeCasaById(request.getId());

		// costruisco la risposta
		MessageServiceOuterClass.GoodbyeResponse response = MessageServiceOuterClass.GoodbyeResponse.newBuilder()
				.setOk(true)
				.setId(CasaInfo.getInstance().getId())
				.build();

		System.out.println("Invio la risposta di GOODBYE: " + response + "\n");

		// passo la risposta nello stream
		responseObserver.onNext(response);

		// completo e finisco la comunicazione
		responseObserver.onCompleted();
	}
}
