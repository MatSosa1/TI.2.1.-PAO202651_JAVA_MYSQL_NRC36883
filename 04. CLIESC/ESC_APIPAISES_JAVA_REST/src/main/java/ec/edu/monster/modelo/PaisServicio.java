package ec.edu.monster.modelo;

import ec.edu.monster.modelo.Datos.Pais;
import ec.edu.monster.modelo.Datos.ResultadoBusqueda;
import ec.edu.monster.modelo.Datos.ResultadoImportacion;
import ec.edu.monster.modelo.Datos.ResumenImportacion;
import java.util.List;

/** Operaciones sobre países: ECUD2 a ECUD6. */
public interface PaisServicio {

    Resultado<List<Pais>> listar(String token) throws ServicioException;

    Resultado<ResultadoBusqueda> buscar(String token, String nombre, String capital, String region)
            throws ServicioException;

    Resultado<List<Pais>> consultarApi(String token, String nombre) throws ServicioException;

    Resultado<ResultadoImportacion> importar(String token, String consulta, String nombre) throws ServicioException;

    Resultado<ResumenImportacion> importarTodos(String token) throws ServicioException;

    Resultado<Pais> registrar(String token, String consulta, String nombre) throws ServicioException;

    Resultado<Pais> actualizar(String token, String nombre) throws ServicioException;

    Resultado<Object> eliminar(String token, String nombre) throws ServicioException;
}
