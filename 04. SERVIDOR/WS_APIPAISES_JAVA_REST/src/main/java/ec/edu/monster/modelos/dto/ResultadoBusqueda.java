package ec.edu.monster.modelos.dto;

import ec.edu.monster.modelos.entidades.Pais;
import java.util.List;

/** Datos de GET /api/Paises/buscar: los países encontrados y el total (ECUD5 paso 7, ECUD6 paso 5). */
public class ResultadoBusqueda {

    private List<Pais> paises;
    private int total;

    public ResultadoBusqueda() {
    }

    public ResultadoBusqueda(List<Pais> paises) {
        this.paises = paises;
        this.total = paises.size();
    }

    public List<Pais> getPaises() {
        return paises;
    }

    public void setPaises(List<Pais> paises) {
        this.paises = paises;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
