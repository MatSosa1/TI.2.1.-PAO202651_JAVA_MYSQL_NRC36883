package ec.edu.monster.modelos.dto;

/**
 * Cuerpo de POST /api/Paises y POST /api/Paises/importar: el texto que el usuario
 * consultó en la API y el nombre del país que eligió de los resultados (ECUD2, ECUD4).
 */
public class SolicitudPais {

    private String consulta;
    private String nombre;

    public SolicitudPais() {
    }

    public SolicitudPais(String consulta, String nombre) {
        this.consulta = consulta;
        this.nombre = nombre;
    }

    public String getConsulta() {
        return consulta;
    }

    public void setConsulta(String consulta) {
        this.consulta = consulta;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
