package serverAmministratore.services;

import serverAmministratore.beans.Casa;
import serverAmministratore.beans.ListaCase;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

// root della gerarchia fornita dal servizio
@Path("/case")
public class ListaCaseService {
    // Quando vuole inserirsi nel sistema, una casa deve comunicare al server: identificatore, indirizzo IP, numero di
    // porta sul quale è disponibile per comunicare con le altre case
    // Il server aggiunge una casa al suo elenco di case presenti nel conodminio solo se non esiste un’altra casa con lo
    // stesso identificatore
    // Se l’inserimento va a buon fine, il server amministratore restituisce alla casa la lista di case già presenti
    // nella rete, specificando per ognuno indirizzo IP e numero di porta per la comunicazione.
    @Path("add")
    @POST
    @Produces({"application/json"})
    @Consumes({"application/json"})
    public Response insert(Casa casa) {
        if (casa.getId() < 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("L'id di una casa non può essere un numero negativo").build();
        }

        if (casa.getPorta() < 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("La porta di una casa non può essere un numero negativo").build();
        }

        ListaCase listaCase = ListaCase.getInstance();

        // il controllo e l'aggiunta della casa devono essere operazioni sequenziali: non deve accadere che controllo
        // se una casa c'è, poi rilasciare il lock senza fare l'aggiunta, perché nel mentre potrei fare un'altra
        // operazione
        String added = listaCase.add(casa);

        switch (added) {
            case "ok":              // la casa non è nella lista ed è stata aggiunta
                return Response.ok(listaCase).build();
            case "id conflict":     // c'è già nella lista una casa con lo stesso id
                return Response.status(Response.Status.CONFLICT)
                        .entity("La casa ha lo stesso id di una casa già esistente nella rete").build();
            case "port conflict":   // la porta della casa è già in uso
                return Response.status(Response.Status.CONFLICT)
                        .entity("La casa ha la stessa porta utilizzata da un'altra casa nella rete").build();
            default:
                return null;
        }
    }

    // Una casa può richiedere esplicitamente di rimuoversi dalla rete. In questo caso, il server deve semplicemente
    // rimuoverla dall’elenco di case del condominio.
    @Path("remove/{idCasa}")
    @DELETE
    public Response delete(@PathParam("idCasa") int idCasa) {
        if (idCasa < 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("L'id di una casa non può essere un numero negativo").build();
        }

        ListaCase listaCase = ListaCase.getInstance();

        // il controllo se la casa c'è e la sua effettiva rimozione devono essere fatte nello stesso blocco synchronized
        if (listaCase.deleteById(idCasa)) { // la casa è stata trovata ed è stata rimossa
            return Response.ok(listaCase).build();
        } else { // la casa non è stata trovata
            return Response.status(Response.Status.NOT_FOUND).entity("La casa cercata non esiste").build();
        }
    }

    // Il server amministratore deve fornire dei metodi per ottenere l’elenco delle case presenti nella rete
    @GET
    @Produces({"application/json"})
    public Response getListaCase() {
        ListaCase listaCase = ListaCase.getInstance();

        if (listaCase.getListaCase().size() > 0) {
            return Response.ok(ListaCase.getInstance().getListaCase()).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("Non c'è alcuna casa nella rete").build();
        }
    }
}
