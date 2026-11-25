package ec.edu.monster.dao;

import ec.edu.monster.modelos.dto.ConteoRegion;
import ec.edu.monster.modelos.dto.PromedioRegion;
import ec.edu.monster.modelos.dto.ResumenImportacion;
import ec.edu.monster.modelos.entidades.Pais;
import ec.edu.monster.utilidades.Textos;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Acceso a la tabla Paises: mantenimiento (ECUD4), búsqueda y filtro (ECUD5, ECUD6),
 * importación (ECUD2) e informes (ECUD7).
 *
 * <p>Las comparaciones de texto las hace MySQL con la collation utf8mb4_0900_ai_ci, así
 * que no distinguen mayúsculas ni tildes.
 */
public class PaisDAO {

    /** Resultado de {@link #importar(Pais)}. */
    public static final String REGISTRADO = "registrado";
    public static final String ACTUALIZADO = "actualizado";

    private static final Logger LOG = Logger.getLogger(PaisDAO.class.getName());

    private static final String SELECT = "select p.Nombre, p.Capital, p.IdRegion, r.Nombre as Region,"
            + " p.Poblacion, p.Moneda from Paises p join Regiones r on r.IdRegion = p.IdRegion";
    private static final String ORDEN = " order by p.Nombre";

    private static final String EXISTE = "select 1 from Paises where Nombre = ?";
    private static final String EXISTE_BLOQUEO = "select 1 from Paises where Nombre = ? for update";
    private static final String NOMBRES = "select Nombre from Paises";
    private static final String CONTAR = "select count(*) from Paises";

    private static final String INSERTAR =
            "insert into Paises (Nombre, Capital, IdRegion, Poblacion, Moneda) values (?, ?, ?, ?, ?)";
    private static final String ACTUALIZAR =
            "update Paises set Capital = ?, IdRegion = ?, Poblacion = ?, Moneda = ? where Nombre = ?";
    /** Inserta o actualiza en una sola sentencia; evita fallar si dos nombres se consideran iguales por la collation. */
    private static final String INSERTAR_O_ACTUALIZAR =
            "insert into Paises (Nombre, Capital, IdRegion, Poblacion, Moneda) values (?, ?, ?, ?, ?) as nuevo"
            + " on duplicate key update Capital = nuevo.Capital, IdRegion = nuevo.IdRegion,"
            + " Poblacion = nuevo.Poblacion, Moneda = nuevo.Moneda";
    private static final String ELIMINAR = "delete from Paises where Nombre = ?";

    private static final String REGIONES = "select IdRegion, Nombre from Regiones";

    /** Incluye las regiones sin países (LEFT JOIN) y las ordena de mayor a menor (ECUD7, paso 4). */
    private static final String CONTAR_POR_REGION = "select r.Nombre as Region, count(p.Nombre) as Cantidad"
            + " from Regiones r left join Paises p on p.IdRegion = r.IdRegion"
            + " group by r.IdRegion, r.Nombre order by Cantidad desc, r.IdRegion";
    /** Empate de población: el primero por nombre (ECUD7, paso 5). */
    private static final String MAS_POBLADO = SELECT + " order by p.Poblacion desc, p.Nombre limit 1";
    /** Redondeado a entero; 0 en las regiones sin países (ECUD7, paso 6 y alterno 3). */
    private static final String PROMEDIO_POR_REGION =
            "select r.Nombre as Region, coalesce(round(avg(p.Poblacion)), 0) as Promedio"
            + " from Regiones r left join Paises p on p.IdRegion = r.IdRegion"
            + " group by r.IdRegion, r.Nombre order by r.IdRegion";

    private final ConexionBD conexionBD;

    public PaisDAO() {
        this(new ConexionBD());
    }

    public PaisDAO(ConexionBD conexionBD) {
        this.conexionBD = conexionBD;
    }

    // ------------------------------------------------------------------ Consultas

    /** Todos los países ordenados por nombre (ECUD4, Listar). */
    public List<Pais> listar() throws SQLException {
        try (Connection conexion = conexionBD.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(SELECT + ORDEN)) {
            return leerPaises(sentencia);
        }
    }

    /**
     * Países que cumplen todos los criterios con valor: nombre y capital por coincidencia
     * parcial, región por igualdad. Sin criterios devuelve todos (ECUD5, alterno 3).
     */
    public List<Pais> buscar(String nombre, String capital, String region) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT);
        List<String> valores = new ArrayList<>();
        String union = " where ";
        if (!Textos.vacio(nombre)) {
            sql.append(union).append("p.Nombre like ? escape '!'");
            valores.add(contiene(nombre));
            union = " and ";
        }
        if (!Textos.vacio(capital)) {
            sql.append(union).append("p.Capital like ? escape '!'");
            valores.add(contiene(capital));
            union = " and ";
        }
        if (!Textos.vacio(region)) {
            sql.append(union).append("r.Nombre = ?");
            valores.add(region.trim());
        }
        sql.append(ORDEN);

        try (Connection conexion = conexionBD.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql.toString())) {
            for (int i = 0; i < valores.size(); i++) {
                sentencia.setString(i + 1, valores.get(i));
            }
            return leerPaises(sentencia);
        }
    }

    public boolean existe(String nombre) throws SQLException {
        try (Connection conexion = conexionBD.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(EXISTE)) {
            sentencia.setString(1, nombre);
            try (ResultSet fila = sentencia.executeQuery()) {
                return fila.next();
            }
        }
    }

    /**
     * Claves de comparación ({@link Textos#clave}) de los nombres guardados, para marcar
     * qué países de la API ya están en la base (ECUD3, paso 6).
     */
    public Set<String> clavesGuardadas() throws SQLException {
        try (Connection conexion = conexionBD.obtenerConexion()) {
            return clavesGuardadas(conexion);
        }
    }

    public int contar() throws SQLException {
        try (Connection conexion = conexionBD.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(CONTAR);
                ResultSet fila = sentencia.executeQuery()) {
            return fila.next() ? fila.getInt(1) : 0;
        }
    }

    // ------------------------------------------------------------------ Mantenimiento

    /**
     * Registra un país nuevo (ECUD4, Registrar).
     *
     * @throws IllegalArgumentException si la región no está en el catálogo
     * @throws java.sql.SQLIntegrityConstraintViolationException si ya existe un país con ese nombre
     */
    public void registrar(Pais pais) throws SQLException {
        try (Connection conexion = conexionBD.obtenerConexion()) {
            int idRegion = idRegionObligatoria(conexion, pais.getRegion());
            try (PreparedStatement sentencia = conexion.prepareStatement(INSERTAR)) {
                llenarInsercion(sentencia, pais, idRegion);
                sentencia.executeUpdate();
            }
        }
    }

    /**
     * Actualiza capital, región, población y moneda del país con ese nombre (ECUD4, Actualizar).
     *
     * @return filas encontradas: 0 si el país no existe
     * @throws IllegalArgumentException si la región no está en el catálogo
     */
    public int actualizar(Pais pais) throws SQLException {
        try (Connection conexion = conexionBD.obtenerConexion()) {
            int idRegion = idRegionObligatoria(conexion, pais.getRegion());
            try (PreparedStatement sentencia = conexion.prepareStatement(ACTUALIZAR)) {
                sentencia.setString(1, pais.getCapital());
                sentencia.setInt(2, idRegion);
                sentencia.setLong(3, pais.getPoblacion());
                sentencia.setString(4, pais.getMoneda());
                sentencia.setString(5, pais.getNombre());
                return sentencia.executeUpdate();
            }
        }
    }

    /** @return filas eliminadas: 0 si el país no existe (ECUD4, Eliminar). */
    public int eliminar(String nombre) throws SQLException {
        try (Connection conexion = conexionBD.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(ELIMINAR)) {
            sentencia.setString(1, nombre);
            return sentencia.executeUpdate();
        }
    }

    // ------------------------------------------------------------------ Importación (ECUD2)

    /**
     * Guarda un país dentro de una transacción: lo registra si no existe o actualiza sus datos
     * si ya existe (ECUD2, paso 7).
     *
     * @return {@link #REGISTRADO} o {@link #ACTUALIZADO}
     */
    public String importar(Pais pais) throws SQLException {
        try (Connection conexion = conexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                int idRegion = idRegionObligatoria(conexion, pais.getRegion());
                boolean existe;
                try (PreparedStatement sentencia = conexion.prepareStatement(EXISTE_BLOQUEO)) {
                    sentencia.setString(1, pais.getNombre());
                    try (ResultSet fila = sentencia.executeQuery()) {
                        existe = fila.next();
                    }
                }
                try (PreparedStatement sentencia = conexion.prepareStatement(INSERTAR_O_ACTUALIZAR)) {
                    llenarInsercion(sentencia, pais, idRegion);
                    sentencia.executeUpdate();
                }
                conexion.commit();
                return existe ? ACTUALIZADO : REGISTRADO;
            } catch (SQLException | RuntimeException e) {
                conexion.rollback();
                throw e;
            }
        }
    }

    /**
     * Importa todos los países en una sola transacción: carga una vez los nombres existentes,
     * envía todas las filas en un lote y confirma al final. Si algo falla se deshace todo y
     * ningún país queda importado a medias (ECUD2, alterno 5).
     */
    public ResumenImportacion importarTodos(List<Pais> paises) throws SQLException {
        try (Connection conexion = conexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                Map<String, Integer> regiones = regionesPorClave(conexion);
                Set<String> existentes = clavesGuardadas(conexion);

                // Un solo registro por nombre (la clave primaria no distingue tildes ni mayúsculas)
                Map<String, Pais> unicos = new LinkedHashMap<>();
                for (Pais pais : paises) {
                    unicos.putIfAbsent(Textos.clave(pais.getNombre()), pais);
                }

                int registrados = 0;
                int actualizados = 0;
                try (PreparedStatement sentencia = conexion.prepareStatement(INSERTAR_O_ACTUALIZAR)) {
                    for (Map.Entry<String, Pais> entrada : unicos.entrySet()) {
                        Pais pais = entrada.getValue();
                        Integer idRegion = regiones.get(Textos.clave(pais.getRegion()));
                        if (idRegion == null) {
                            LOG.warning("País omitido por región desconocida: " + pais.getNombre());
                            continue;
                        }
                        llenarInsercion(sentencia, pais, idRegion);
                        sentencia.addBatch();
                        if (existentes.add(entrada.getKey())) {
                            registrados++;
                        } else {
                            actualizados++;
                        }
                    }
                    sentencia.executeBatch();
                }
                conexion.commit();
                return new ResumenImportacion(paises.size(), registrados, actualizados);
            } catch (SQLException | RuntimeException e) {
                conexion.rollback();
                throw e;
            }
        }
    }

    // ------------------------------------------------------------------ Informes (ECUD7)

    public List<ConteoRegion> contarPorRegion() throws SQLException {
        List<ConteoRegion> conteos = new ArrayList<>();
        try (Connection conexion = conexionBD.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(CONTAR_POR_REGION);
                ResultSet fila = sentencia.executeQuery()) {
            while (fila.next()) {
                conteos.add(new ConteoRegion(fila.getString("Region"), fila.getInt("Cantidad")));
            }
        }
        return conteos;
    }

    /** País con mayor población, o null si no hay países. */
    public Pais paisMasPoblado() throws SQLException {
        try (Connection conexion = conexionBD.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(MAS_POBLADO)) {
            List<Pais> paises = leerPaises(sentencia);
            return paises.isEmpty() ? null : paises.get(0);
        }
    }

    public List<PromedioRegion> promedioPorRegion() throws SQLException {
        List<PromedioRegion> promedios = new ArrayList<>();
        try (Connection conexion = conexionBD.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(PROMEDIO_POR_REGION);
                ResultSet fila = sentencia.executeQuery()) {
            while (fila.next()) {
                promedios.add(new PromedioRegion(fila.getString("Region"), fila.getLong("Promedio")));
            }
        }
        return promedios;
    }

    // ------------------------------------------------------------------ Apoyo

    private static List<Pais> leerPaises(PreparedStatement sentencia) throws SQLException {
        List<Pais> paises = new ArrayList<>();
        try (ResultSet fila = sentencia.executeQuery()) {
            while (fila.next()) {
                Pais pais = new Pais(fila.getString("Nombre"), fila.getString("Capital"),
                        fila.getString("Region"), fila.getLong("Poblacion"), fila.getString("Moneda"));
                pais.setIdRegion(fila.getInt("IdRegion"));
                paises.add(pais);
            }
        }
        return paises;
    }

    private static void llenarInsercion(PreparedStatement sentencia, Pais pais, int idRegion) throws SQLException {
        sentencia.setString(1, pais.getNombre());
        sentencia.setString(2, pais.getCapital());
        sentencia.setInt(3, idRegion);
        sentencia.setLong(4, Math.max(0, pais.getPoblacion()));
        sentencia.setString(5, pais.getMoneda());
    }

    private static int idRegionObligatoria(Connection conexion, String region) throws SQLException {
        int id = RegionDAO.idPorNombre(conexion, region);
        if (id <= 0) {
            throw new IllegalArgumentException("Región no válida: " + region);
        }
        return id;
    }

    private static Set<String> clavesGuardadas(Connection conexion) throws SQLException {
        Set<String> claves = new HashSet<>();
        try (PreparedStatement sentencia = conexion.prepareStatement(NOMBRES);
                ResultSet fila = sentencia.executeQuery()) {
            while (fila.next()) {
                claves.add(Textos.clave(fila.getString(1)));
            }
        }
        return claves;
    }

    private static Map<String, Integer> regionesPorClave(Connection conexion) throws SQLException {
        Map<String, Integer> regiones = new HashMap<>();
        try (PreparedStatement sentencia = conexion.prepareStatement(REGIONES);
                ResultSet fila = sentencia.executeQuery()) {
            while (fila.next()) {
                regiones.put(Textos.clave(fila.getString("Nombre")), fila.getInt("IdRegion"));
            }
        }
        return regiones;
    }

    /** Patrón LIKE «contiene», escapando los comodines que escriba el usuario. */
    private static String contiene(String texto) {
        String escapado = texto.trim().replace("!", "!!").replace("%", "!%").replace("_", "!_");
        return "%" + escapado + "%";
    }
}
