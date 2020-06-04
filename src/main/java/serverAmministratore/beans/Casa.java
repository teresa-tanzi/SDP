package serverAmministratore.beans;

public class Casa {
    private int id;
    private String ip;
    private int porta;

    // costruttore vuoto
    public Casa() {}

    // costruttore con parametri
    public Casa(int id, String ip, int porta) {
        this.id = id;
        this.ip = ip;
        this.porta = porta;
    }

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
}
