package serverAmministratore.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class MapStats {
    @XmlElement(name="stats")
    private HashMap<Integer, List<Statistica>> mapStats;    // statistiche locali
    private List<Statistica> globalStats;                   // statistiche globali
    private static MapStats instance;

    private MapStats() {
        mapStats = new HashMap<Integer, List<Statistica>>();
        globalStats = new ArrayList<Statistica>();
    }

    //singleton
    public synchronized static MapStats getInstance() {
        if (instance == null)
            instance = new MapStats();
        return instance;
    }

    public HashMap<Integer, List<Statistica>> getMapStats() {
        synchronized (mapStats) {
            return new HashMap<>(mapStats);
        }
    }

    public List<Statistica> getGlobalStats() {
        synchronized (globalStats) {
            return new ArrayList<>(globalStats);
        }
    }

    // aggiungo una statistica conoscendo l'id della casa
    public boolean addLocal(Statistica stat, int idCasa) {
        boolean conflict = false;

        synchronized (mapStats) {
            // se la chiave già esiste, allora aggiungo semplicemente la statistica alla sua lista-valore
            if (mapStats.containsKey(idCasa)) {
                List<Statistica> oldList = mapStats.get(idCasa);

                // controllo se c'è già una statistica con lo stesso timestamp (identificatore della statistica)
                for (Statistica s: oldList) {
                    if (s.getTimestamp() == stat.getTimestamp()) {
                        conflict = true;
                    }
                }

                if (!conflict) {
                    oldList.add(stat);
                    mapStats.replace(idCasa, oldList);
                }
            } else {
                // altrimenti devo creare la coppia chiave-valore
                List<Statistica> listaStats = new ArrayList<Statistica>();
                listaStats.add(stat);

                mapStats.put(idCasa, listaStats);
            }
        }

        return conflict;
    }

    // aggiungo una statistica globale
    public boolean addGlobal(Statistica stat) {
        synchronized (globalStats) {
            boolean conflict = false;

            // controllo se non ci sono statistiche con lo stesso timestamp (lo considero come l'identificatore della
            // statistica)
            for (Statistica s: globalStats) {
                if (s.getTimestamp() == stat.getTimestamp()) {
                    conflict = true;
                }
            }

            if (!conflict) {
                globalStats.add(stat);
            }

            return conflict;
        }
    }

    // leggo le ultime n statistiche di una casa
    // non serve sincronizzare perché lavoro su una copia della mappa (che è sincronizzata)
    public List<Statistica> getMostRecentStats(int n, int idCasa) {
        HashMap<Integer, List<Statistica>> mapStatsCopy = getMapStats(); //copia di tutta la struttura dati

        // controllo se la casa cercata è nella lista
        if (mapStatsCopy.containsKey(idCasa)) { // la casa è nella lista, quindi ritorno le statistiche
            List<Statistica> statsList = mapStatsCopy.get(idCasa); // prendo solo la lista relativa alla casa che mi interessa (dalla copia)

            // se la lista è minore di n, allora ritorno tutta la lista
            List<Statistica> tail = statsList.subList(Math.max(statsList.size() - n, 0), statsList.size());
            return tail;
        } else {
            return null;
        }
    }

    // leggo le ultime n statistiche globali
    public List<Statistica> getMostRecentGlobalStats(int n) {
        List<Statistica> globalStatsCopy = getGlobalStats();

        // se la lista è minore di n, allora ritorno tutta la lista
        List<Statistica> tail = globalStatsCopy.subList(Math.max(globalStatsCopy.size() - n, 0), globalStatsCopy.size());
        return tail;
    }

    // rimuovo le statistiche di una casa passando l'id della casa
    public boolean remove(int idCasa) {
        synchronized (mapStats) {
            // controllo se la casa è nella lista: se c'è la rimuovo e ritorno tue, altrimenti ritorno false
            if (mapStats.containsKey(idCasa)) {
                mapStats.remove(idCasa);
                return true;
            } else {
                return false;
            }
        }
    }
}

