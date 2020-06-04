package amministratore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Amministratore {
    // per connettersi al server, bisogna conoscere l'IP e la porta del servizio
    private static final String ipAddress = "localhost";
    private static final int port = 8080;

    public static void main (String [] args) {
        System.out.println("Benvenuto amministratore.");

        boolean continua = true;

        while (continua) {
            System.out.println("Quale informazione vuoi ottenere?");
            System.out.println("");
            System.out.println("1. Elenco delle case presenti nella rete.");
            System.out.println("2. Ultime n statistiche di una casa.");
            System.out.println("3. Ultime n statistiche globali.");
            System.out.println("4. Deviazione standard e media delle ultime n statistiche di una casa.");
            System.out.println("5. Deviazione standard e media delle ultime n statistiche globali.");
            System.out.println("");
            System.out.println("Seleziona l'informazione che vuoi ottenere digitando il numero corrispondente.");

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            boolean correctInput = false;
            int input = 0;

            while (!correctInput) {
                try {
                    input = Integer.parseInt(br.readLine());

                    if (input < 1 || input > 5) {
                        System.out.println("Il numero inserito non corrisponde ad una voce del menu. Riprovare.");
                    } else {
                        System.out.println("Hai selezionato il numero: " + input);
                        correctInput = true;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Il valore inserito non è un numero. Riprovare.");
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }

            Client client = Client.create();
            String resourceStr = "http://" + ipAddress + ":" + port;

            switch (input) {
                case 1:
                    // elenco delle case presenti nella rete
                    resourceStr = resourceStr + "/case";

                    break;

                case 2:
                    // ultime n statistiche relative ad una specifica casa
                    int n2 = 0;
                    int id2 = 0;
                    n2 = getCorrectN(br);
                    id2 = getCorrectId(br);

                    resourceStr = resourceStr + "/stats/get/" + n2 + "/" + id2;

                    break;

                case 3:
                    // ultime n statistiche globali
                    int n3 = 0;
                    n3 = getCorrectN(br);

                    resourceStr = resourceStr + "/stats/get/" + n3;

                    break;

                case 4:
                    // deviazione standard e media delle ultime n statistiche di una casa
                    int n4 = 0;
                    int id4 = 0;
                    n4 = getCorrectN(br);
                    id4 = getCorrectId(br);

                    resourceStr = resourceStr + "/stats/getAggregate/" + n4 + "/" + id4;

                    break;

                case 5:
                    // deviazione standard e media delle ultime n statistiche globali
                    int n5 = 0;
                    n5 = getCorrectN(br);

                    resourceStr = resourceStr + "/stats/getAggregate/" + n5;

                    break;
            }

            WebResource resource = client.resource(resourceStr);
            System.out.println("Sto effettuando la richiesta: " + resource);

            try {
                ClientResponse response = resource.accept("application/json").get(ClientResponse.class);

                if (response.getStatus() != 200) {
                    System.out.println("Errore nella richiesta! Codice d'errore HTTP: " + response.getStatus());

                    System.out.println(response.getEntity(String.class));
                } else {
                    String output = response.getEntity(String.class);

                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    JsonParser jp = new JsonParser();
                    JsonElement je = jp.parse(output);
                    String prettyJsonString = gson.toJson(je);

                    System.out.println("[SERVER]: ");
                    System.out.println(prettyJsonString);
                }

                System.out.println("");
                System.out.println("Vuoi fare un'altra analisi? (s/n)");
            } catch (ClientHandlerException e) {
                // eccezione descritta in maniera più concisa
                System.out.println(e.getMessage());
                System.out.println("Problemi nella connessione con il server. Vuoi ripovare? (s/n)");
            }

            String continuaStr = "";
            boolean correctContinua = false;

            while (!correctContinua) {
                try {
                    continuaStr = br.readLine();

                    switch (continuaStr) {
                        case "s":
                            correctContinua = true;
                            break;
                        case "n":
                            correctContinua = true;
                            continua = false;
                            System.out.println("Arrivederci amministratore.");
                            break;
                        default:
                            System.out.println("Inserire 's' per fare un'altra richiesta o 'n' per chiudere il programma. Riprovare.");
                    }
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    private static int getCorrectN(BufferedReader br) {
        boolean correctN = false;
        int n = 0;

        while (!correctN) {
            System.out.println("Quante statistiche ti interessano?");

            try {
                n = Integer.parseInt(br.readLine());

                // non posso chiedere 0 o un numero negativo di statistiche
                if (n < 1) {
                    System.out.println("Bisogna richiedere almeno una statistica. Riprovare.");
                } else {
                    correctN = true;
                }
            } catch (NumberFormatException e) {
                System.out.println("Il valore inserito non è un numero. Riprovare.");
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        return n;
    }

    private static int getCorrectId(BufferedReader br) {
        boolean correctId = false;
        int id = 0;

        while (!correctId) {
            System.out.println("Qual è l'id della casa che ti interessa?");

            try {
                id = Integer.parseInt(br.readLine());

                // l'id è un numero positivo
                if (id < 0) {
                    System.out.println("L'id di una casa deve essere un numero positivo. Riprovare.");
                } else {
                    correctId = true;
                }
            } catch (NumberFormatException e) {
                System.out.println("L'id di una casa deve essere un valore numerico. Riprovare.");
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        return id;
    }
}
