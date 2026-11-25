package ec.edu.monster.dobles;

import ec.edu.monster.modelo.Datos.ConteoRegion;
import ec.edu.monster.modelo.Datos.Informe;
import ec.edu.monster.modelo.Datos.Pais;
import ec.edu.monster.modelo.Datos.PromedioRegion;
import ec.edu.monster.modelo.Datos.ResultadoBusqueda;
import ec.edu.monster.modelo.Datos.ResultadoImportacion;
import ec.edu.monster.modelo.Datos.ResumenImportacion;
import ec.edu.monster.modelo.DatosSesion;
import ec.edu.monster.modelo.LoginServicio;
import ec.edu.monster.modelo.PaisServicio;
import ec.edu.monster.modelo.ReporteServicio;
import ec.edu.monster.modelo.Resultado;
import ec.edu.monster.modelo.ServicioException;
import java.util.ArrayList;
import java.util.List;

/** Servicio en memoria: registra las llamadas y responde lo preparado, o falla si se le pide. */
public class ServicioFalso implements LoginServicio, PaisServicio, ReporteServicio {

    public static final String TOKEN = "t".repeat(64);
    public static final Pais ECUADOR = new Pais("Ecuador", "Quito", "América", 18_001_000, "United States dollar (USD)", false);
    public static final Pais ALEMANIA = new Pais("Alemania", "Berlin", "Europa", 83_491_249, "Euro (EUR)", false);

    public final List<String> llamadas = new ArrayList<>();
    public String tokenRecibido;
    private ServicioException falla;
    public List<Pais> paises = new ArrayList<>(List.of(ALEMANIA, ECUADOR));

    public void fallarCon(String mensaje) {
        falla = new ServicioException(mensaje);
    }

    public void rechazarToken() {
        falla = new ServicioException(ServicioException.SESION_NO_VALIDA, true);
    }

    private void registrar(String llamada, String token) throws ServicioException {
        llamadas.add(llamada);
        tokenRecibido = token;
        if (falla != null) {
            throw falla;
        }
    }

    @Override
    public DatosSesion iniciarSesion(String usuario, String clave) throws ServicioException {
        registrar("login " + usuario + " " + clave, null);
        return new DatosSesion(TOKEN, "Administrador");
    }

    @Override
    public void cerrarSesion(String token) throws ServicioException {
        registrar("logout", token);
    }

    @Override
    public Resultado<List<Pais>> listar(String token) throws ServicioException {
        registrar("listar", token);
        return new Resultado<>("Países obtenidos correctamente", paises);
    }

    @Override
    public Resultado<ResultadoBusqueda> buscar(String token, String nombre, String capital, String region)
            throws ServicioException {
        registrar("buscar " + nombre + "|" + capital + "|" + region, token);
        List<Pais> encontrados = paises.stream()
                .filter(p -> region == null || region.isEmpty() || p.region().equals(region)).toList();
        String mensaje = (nombre == null || nombre.isBlank()) && (capital == null || capital.isBlank())
                && region != null && !region.isEmpty() ? "Filtro aplicado correctamente" : "Búsqueda realizada correctamente";
        return new Resultado<>(mensaje, new ResultadoBusqueda(encontrados, encontrados.size()));
    }

    @Override
    public Resultado<List<Pais>> consultarApi(String token, String nombre) throws ServicioException {
        registrar("consultarApi " + nombre, token);
        return new Resultado<>("Consulta en la API exitosa", List.of(ECUADOR));
    }

    @Override
    public Resultado<ResultadoImportacion> importar(String token, String consulta, String nombre)
            throws ServicioException {
        registrar("importar " + consulta + "|" + nombre, token);
        return new Resultado<>("País importado correctamente", new ResultadoImportacion(ECUADOR, "registrado"));
    }

    @Override
    public Resultado<ResumenImportacion> importarTodos(String token) throws ServicioException {
        registrar("importarTodos", token);
        return new Resultado<>("Importación finalizada", new ResumenImportacion(250, 240, 10));
    }

    @Override
    public Resultado<Pais> registrar(String token, String consulta, String nombre) throws ServicioException {
        registrar("registrar " + consulta + "|" + nombre, token);
        return new Resultado<>("País registrado correctamente", ECUADOR);
    }

    @Override
    public Resultado<Pais> actualizar(String token, String nombre) throws ServicioException {
        registrar("actualizar " + nombre, token);
        return new Resultado<>("País actualizado correctamente", ECUADOR);
    }

    @Override
    public Resultado<Object> eliminar(String token, String nombre) throws ServicioException {
        registrar("eliminar " + nombre, token);
        return new Resultado<>("País eliminado correctamente", null);
    }

    @Override
    public Resultado<Informe> generar(String token) throws ServicioException {
        registrar("informes", token);
        return new Resultado<>("Informes generados correctamente", new Informe(
                List.of(new ConteoRegion("América", 1), new ConteoRegion("Europa", 1)), ALEMANIA,
                List.of(new PromedioRegion("América", 18_001_000), new PromedioRegion("Europa", 83_491_249))));
    }
}
