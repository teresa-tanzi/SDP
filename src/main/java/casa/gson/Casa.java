package casa.gson;

public class Casa implements Comparable<Casa> {
    private int id;
    private String ip;
    private int porta;

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

    @Override
    public int compareTo(Casa o) {
        return id - o.getId();
    }
}
