package casa.gson;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ListaCase {
    @SerializedName("case")
    private List<Casa> listaCase;

    public ListaCase(List<Casa> listaCase) {
        this.listaCase = listaCase;
    }

    public List<Casa> getListaCase() {
        return listaCase;
    }

    public void setListaCase(List<Casa> listaCase) {
        this.listaCase = listaCase;
    }
}
