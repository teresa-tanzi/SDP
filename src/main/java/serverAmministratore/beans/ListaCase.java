package serverAmministratore.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ListaCase {
    @XmlElement(name="case")
    private List<Casa> listaCase;
    private static ListaCase instance;

    private ListaCase() {
        listaCase = new ArrayList<Casa>();
    }

    //singleton
    public synchronized static ListaCase getInstance() {
        if (instance == null)
            instance = new ListaCase();
        return instance;
    }

    public synchronized List<Casa> getListaCase() {
        return new ArrayList<>(listaCase);
    }

    /*public void setListaCase(List<Casa> listaCase) {
        this.listaCase = listaCase;
    }*/

    // aggiungo una casa (id, ip, #porta) alla lista delle case
    public synchronized String add(Casa c){
        // controllo se la casa che voglio inserire ha lo stesso id o la stessa porta di una casa che c'è già e, in tal
        // caso, ritorno un errore, altrimenti aggiungo la casa e ritorno un messaggio di ok
        if (isIn(c.getId())) {
            return "id conflict";
        } else if (isPortInUse(c.getPorta())) {
            return "port conflict";
        } else {
            listaCase.add(c);
            return "ok";
        }
    }

    // cerco una casa nella lista conoscendo solo il suo id
    // non sincronizzo, perché chiamo sempre da metodi sincronizzati
    private Casa getById(int id){
        for (Casa c: listaCase) {
            if (c.getId() == id) {
                return c;
            }
        }

        return null;
    }

    // rimuovo una casa dalla lista conoscendo il suo id
    public synchronized boolean deleteById(int id) {
        // controllo se c'è una casa con quel id: se c'è, ritorno true e cancello la casa, se no ritorno false
        boolean canDelete = isIn(id);
        Casa c = getById(id);

        if (canDelete) {
            listaCase.remove(c);
        }

        return canDelete;
    }

    // cerco se una casa è già presente nella lista conoscendo il suo id
    // non sincronizzo perché tanto chiamo sempre da metodi che sono già sincronizzati
    private boolean isIn(int id) {
        boolean isIn = false;

        for (Casa c: listaCase) {
            if (c.getId() == id) {
                isIn = true;
            }
        }

        return isIn;
    }

    // controllo se una porta è già in utilizzo da una qualche casa nella rete
    // non sincronizzo perché chiamo da metodi che sono già sincronizzati
    private boolean isPortInUse(int port) {
        boolean portInUse = false;

        for (Casa c: listaCase) {
            if (c.getPorta() == port) {
                portInUse = true;
            }
        }

        return portInUse;
    }
}
