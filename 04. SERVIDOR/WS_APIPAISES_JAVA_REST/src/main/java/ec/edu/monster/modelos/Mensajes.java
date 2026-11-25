package ec.edu.monster.modelos;

/**
 * Mensajes del sistema, copiados literalmente de las convenciones del ECUD.
 * Los clientes los muestran tal cual, así que no se deben reescribir.
 */
public final class Mensajes {

    public static final String LOGIN_EXITOSO = "Ingreso de sesión exitoso";
    public static final String CREDENCIALES_INCORRECTAS = "Credenciales incorrectas";
    public static final String SESION_CERRADA = "Sesión cerrada";
    public static final String TOKEN_INVALIDO = "Token inválido o sesión no activa";

    public static final String CONSULTA_API_EXITOSA = "Consulta en la API exitosa";
    public static final String SIN_COINCIDENCIAS_API = "No se encontraron países con ese nombre en la API";
    public static final String PAIS_IMPORTADO = "País importado correctamente";
    public static final String IMPORTACION_FINALIZADA = "Importación finalizada";

    public static final String PAISES_LISTADOS = "Países obtenidos correctamente";
    public static final String PAIS_REGISTRADO = "País registrado correctamente";
    public static final String PAIS_ACTUALIZADO = "País actualizado correctamente";
    public static final String PAIS_ELIMINADO = "País eliminado correctamente";
    public static final String PAIS_DUPLICADO = "Ya existe un país con ese nombre";
    public static final String PAIS_NO_EXISTE = "El país no existe";

    public static final String REGION_NO_VALIDA = "Región no válida";
    public static final String BUSQUEDA_REALIZADA = "Búsqueda realizada correctamente";
    public static final String FILTRO_APLICADO = "Filtro aplicado correctamente";

    public static final String INFORMES_GENERADOS = "Informes generados correctamente";
    public static final String SIN_PAISES_INFORMES = "No hay países registrados para generar informes";

    public static final String API_NO_DISPONIBLE = "La API REST Countries no está disponible, intente más tarde";
    public static final String API_RESPUESTA_INVALIDA = "La API REST Countries devolvió una respuesta no válida";
    public static final String SERVICIO_NO_DISPONIBLE = "Servicio no disponible, intente más tarde";
    public static final String ERROR_INESPERADO = "Ocurrió un error inesperado, intente nuevamente";

    /** Validaciones de entrada: las mismas que hacen los clientes antes de enviar. */
    public static final String INGRESE_USUARIO = "Ingrese el usuario";
    public static final String INGRESE_CLAVE = "Ingrese la clave";
    public static final String INGRESE_NOMBRE_PAIS = "Ingrese el nombre del país";
    public static final String NOMBRE_MUY_LARGO = "El nombre no puede superar 100 caracteres";

    /** Errores del protocolo HTTP (ruta, método o cuerpo), no previstos en el ECUD. */
    public static final String DATOS_NO_VALIDOS = "Los datos enviados no son válidos";
    public static final String RECURSO_NO_ENCONTRADO = "El recurso solicitado no existe";
    public static final String METODO_NO_PERMITIDO = "Operación no permitida en este recurso";

    private Mensajes() {
    }
}
