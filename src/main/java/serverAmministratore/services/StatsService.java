package serverAmministratore.services;

import serverAmministratore.beans.Aggregato;
import serverAmministratore.beans.MapStats;
import serverAmministratore.beans.Statistica;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/stats")
public class StatsService {
    // il server riceverà sia statistiche globali (relative all’intero condominio)
    @Path("add")
    @POST
    @Consumes({"application/json"})
    public Response insertGlobal(Statistica s) {
        if (s.getTimestamp() < 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Il timestamp di una statistica non può essere un numero negativo").build();
        }

        if (s.getValore() < 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Il valore di una statstica non può essere un numero negativo").build();
        }

        boolean conflict = MapStats.getInstance().addGlobal(s);

        if (conflict) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("La statistica è già presente nella lista delle statistiche globali").build();
        } else {
            return Response.ok().build();
        }
    }

    // che locali (di ogni zona casa)
    @Path("add/{idCasa}")
    @POST
    @Consumes({"application/json"})
    public Response insert(Statistica s, @PathParam("idCasa") int idCasa) {
        if (idCasa < 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("L'id di una casa non può essere un numero negativo").build();
        }

        if (s.getTimestamp() < 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Il timestamp di una statistica non può essere un numero negativo").build();
        }

        if (s.getValore() < 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Il valore di una statistica non può essere un numero negativo").build();
        }

        boolean conflict = MapStats.getInstance().addLocal(s, idCasa);

        if (conflict) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("La statistica è già presente nella lista delle statistiche di questa casa").build();
        } else {
            return Response.ok().build();
        }
    }

    // Il server amministratore deve fornire dei metodi per ottenere le ultime n statistiche (con timestamp) relative ad
    // una specifica casa
    @Path("get/{n}/{idCasa}")
    @GET
    @Produces({"application/json"})
    public Response getStats(@PathParam("n") int n, @PathParam("idCasa") int idCasa) {
        // il controllo di queste cose però è già fatto lato client
        if (idCasa < 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("L'id di una casa non può essere un numero negativo").build();
        }

        if (n < 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Non puoi chiedere un numero negativo di statitiche").build();
        }

        MapStats mapStats = MapStats.getInstance();
        List<Statistica> statList = mapStats.getMostRecentStats(n, idCasa);

        // controllo se la casa richiesta esiste o ha inviato mai delle statistiche
        if (statList != null) { // la casa c'è
            return Response.ok(statList).build();
        } else { // la casa con quel id non è stata trovata
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("La casa cercata non esiste o non ha mai inviato statistiche").build();
        }
    }

    // le ultime n statistiche (con timestamp) condominiali
    @Path("get/{n}")
    @GET
    @Produces({"application/json"})
    public Response getStatsGlobal(@PathParam("n") int n) {
        if (n < 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Non puoi chiedere un numero negativo di statitiche").build();
        }

        MapStats mapStats = MapStats.getInstance();
        List<Statistica> statsList = mapStats.getMostRecentGlobalStats(n);

        if (statsList.size() > 0) {
            return Response.ok(statsList).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("Non sono mai state inviate statistiche globali").build();
        }
    }

    // la deviazione standard e media delle ultime n statistiche prodotte da una specifica casa
    @Path("getAggregate/{n}/{idCasa}")
    @GET
    @Produces({"application/json"})
    public Response getAggregate(@PathParam("n") int n, @PathParam("idCasa") int idCasa) {
        if (idCasa < 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("L'id di una casa non può essere un numero negativo").build();
        }

        if (n < 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Non posso calcolare un aggregato su un numero negativo di statitiche").build();
        }

        MapStats mapStats = MapStats.getInstance();
        List<Statistica> statsList = mapStats.getMostRecentStats(n, idCasa);

        if (statsList != null) { // la casa c'è, quindi ha per forza inviato delle statistiche
            //System.out.println(mapStats.toString());
            //System.out.println(statsList.toString());

            float mean = mean(statsList);
            float stDev = stDev(statsList);
            Aggregato aggregate = new Aggregato(mean, stDev);

            return Response.ok(aggregate).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("La casa cercata non esiste o non ha mai inviato statistiche").build();
        }
    }

    // la deviazione standard e media delle ultime n statistiche complessive condominiali
    @Path("getAggregate/{n}")
    @GET
    @Produces({"application/json"})
    public Response getAggregateGlobal(@PathParam("n") int n) {
        if (n < 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Non puoi chiedere un numero negativo di statitiche").build();
        }

        MapStats mapStats = MapStats.getInstance();
        List<Statistica> statsList = mapStats.getMostRecentGlobalStats(n);

        if (statsList.size() > 0) {
            float mean = mean(statsList);
            float stDev = stDev(statsList);

            Aggregato aggregate = new Aggregato(mean, stDev);

            return Response.ok(aggregate).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Non sono mai state inviate statistiche globali").build();
        }
    }

    // rimuovo una lista di statistiche dalla mappa (lo faccio nel caso una casa esca dalla rete)
    @Path("remove/{idCasa}")
    @DELETE
    public Response removeStats(@PathParam("idCasa") int idCasa) {
        if (idCasa < 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("L'id di una casa non può essere un numero negativo").build();
        }

        MapStats mapStats = MapStats.getInstance();
        boolean removed = mapStats.remove(idCasa);

        if (removed) {
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("La casa non esiste o non ha mai mandato statistiche").build();
        }
    }

    // calcolo la media di un array di statistiche
    private float mean(List<Statistica> statsList) {
        float partialSum = 0;
        int counter = 0;

        for (Statistica s: statsList) {
            partialSum += s.getValore();
            counter++;
        }

        return partialSum / counter;
    }

    // calcolo la deviazione standard di un array di statistiche
    private float stDev(List<Statistica> statsList) {
        float mean = mean(statsList);
        float partialSum = 0;
        int counter = 0;

        System.out.println(statsList);

        for (Statistica s: statsList) {
            //partialSum += Math.pow((s.getValore() - mean), 2);
            partialSum += (s.getValore() - mean) * (s.getValore() - mean);

            counter++;
        }

        // tolgo il -1 a denominatore perché da problemi quando ho un solo elemento nella lista
        return (float)Math.sqrt((float)partialSum / counter);
    }
}
