# Correcciones pendientes en ERS, ECUD y diagramas

Diferencias entre la documentación de diseño y el sistema implementado y verificado (24/11/2026). Cada punto indica dónde está el texto actual y por qué debe cambiar. Las líneas citadas son del texto de cada `.docx` (`word/document.xml`), por lo que sirven para encontrar el párrafo, no como número de página.

Documentos:

- ERS: `01. UML/01. ERS/ERS_JAVA_MYSQL_APIPAISES_SOSA.docx`.
- ECUD: `01. UML/02. ECUD/ECUD_JAVA_MYSQL_APIPAISES_SOSA.docx`.
- Diagramas: `01. UML/03. DIAGRAMAS` (PowerDesigner). Se corrigen en la herramienta y no a mano en los `.oom`.

## 1. REST Countries v3.1 dejó de funcionar (ERS y ECUD)

Hoy toda petición a `https://restcountries.com/v3.1/...` responde con un error de servicio obsoleto. El servidor usa la versión 5:

| Aspecto | Dice la documentación (v3.1) | Implementado (v5) |
|---|---|---|
| URL base | `https://restcountries.com/v3.1` | `https://api.restcountries.com/countries/v5` |
| Autenticación | ninguna | API key en `Authorization: Bearer <clave>` (cuenta gratuita) |
| Todos los países | `/v3.1/all?fields=...` | `GET /countries/v5?limit=100&offset=...`, paginado en `data.objects` con `meta.more` |
| Buscar por nombre | `/v3.1/name/{nombre}` | `/name?q=`, y si no hay resultados `/names.translations?q=` (para nombres en español como «Alemania») |
| Nombre | `translations.spa.common` / `name.common` | `names.translations.spa.common` / `names.common` |
| Capital | `capital[0]` | el objeto de `capitals[]` con `attributes.primary`, o el primero |
| Moneda | objeto `currencies` | lista `currencies[]` con `name` y `code` |
| Límites | no se mencionan | 20 peticiones cada 10 s (429) y cuota mensual (403); se responden con 502 «La API REST Countries no está disponible, intente más tarde» |

Dónde corregir:

- ERS, líneas 132, 244, 334, 388 y 740: las URL y la versión.
- ERS, línea 782: la referencia bibliográfica («REST Countries API v3.1»).
- ECUD2 Importar País (descripción, línea 126).
- ECUD3 Consultar País en la API (descripción y paso 5, líneas 178 y 206).
- ECUD9 Consumir API Pública: la descripción (línea 494) y los pasos 2, 5, 6 y 8 (líneas 519–526). Hay que agregar la API key, la paginación y la búsqueda alternativa por traducción.
- Agregar al ERS un requisito no funcional de configuración: la API key va en `src/main/resources/config.properties`, que no se versiona, o en una propiedad del sistema o una variable de entorno (`RESTCOUNTRIES_APIKEY`).

## 2. Restos de la versión .NET / SQL Server (ERS)

El ERS se adaptó de la versión .NET del taller. El sistema real es Java 21, JAX-RS, Payara 7 y MySQL 8.4.

| Línea ERS | Texto actual | Corrección |
|---|---|---|
| 120, 182 | Taller «Consumo de APIs Públicas en .NET / DOTNET con SQL Server…» | «…en Java con MySQL aplicando el Patrón MVC» y el nombre real del `.docx` del enunciado |
| 121, 123 y otras (12 apariciones de «SQL Server», 17 de «.NET») | SQL Server, .NET | MySQL, Java |
| 194, 197 | Referencias de EF Core y SQL Server 2022 | Referencias de JDBC / mysql-connector-j y MySQL 8.4 |
| 274 | «servidor ASP.NET Core y SQL Server» | «servidor Payara (Java 21) y MySQL 8.4 en Docker» |
| 608 | «Persistencia en SQL Server» | «Persistencia en MySQL» |
| 610 | «La cadena de conexión se configura en appsettings.json» | «…en `config.properties` (`db.url`, `db.usuario`, `db.clave`)» |
| 752, 756, 768 | `NVARCHAR(100)` | `VARCHAR(100)` (MySQL, `utf8mb4`) |
| 716 (rendimiento) | «…con una sola llamada a SaveChangesAsync dentro de una transacción» | «…en una sola transacción JDBC (`setAutoCommit(false)`, inserción por lotes, `commit` o `rollback`) en `PaisDAO.importarTodos`» |

El ECUD tiene 2 menciones de SQL Server y 4 de .NET; se corrigen con el mismo criterio.

## 3. Universidades y el campo «área» (ERS y ECUD)

Están fuera del alcance del taller de Java y no se implementaron.

- ERS, líneas 143–144: alcance, «código de país de las universidades» y «Edición manual de las universidades».
- ERS, líneas 237 y 381: la función «importar universidades» en la descripción del usuario.
- ERS, línea 655: «La población y el área se muestran con separador de miles» queda solo como «La población…».
- ERS, línea 731: la validación del área, si se menciona.
- ECUD: el cuerpo ya no tiene Universidades; tiene diez casos, ECUD1 a ECUD10. Pero los tres índices del inicio están desactualizados:
  - el de contenido lista «10.ECUD_IMPORTAR_CONSULTAR_UNIVERSIDADES» y «11.ECUD_CONSUMIR_API_UNIVERSIDADES»;
  - el de ilustraciones y el de tablas tienen las entradas 10 y 11 de Universidades;
  - además usan nombres viejos: «ECUD_IMPORTAR_PAISES», «ECUD_BUSCAR_FILTRAR_PAISES» en un solo caso, «ECUD_GENERAR_REPORTES» y «ECUD_CONSUMIR_API_REST_COUNTRIES».

  Basta con actualizar los campos en Word: seleccionar todo, F9, «Actualizar toda la tabla».
- Diagramas: quitar Universidades de los casos de uso (`01_CASOS_USO`) si aparece.

## 4. Versiones de herramientas (ERS)

- ERS, línea 256: «NetBeans 31» no existe. Corregir a «NetBeans 21 o superior», como pide el enunciado.
- Unificar el JDK: el enunciado pide JDK 17+ y el ERS/ECUD Java 21. Se implementó con **Java 21** en el servidor y en los clientes de escritorio, consola y web. El cliente Android compila con el JDK de Android Studio, con `minSdk 24` y `compileSdk 36`.
- Agregar las versiones reales:
  - Payara Server 7 con Jakarta EE 11;
  - MySQL 8.4 en Docker;
  - Gson 2.10.1;
  - mysql-connector-j 8.3.0;
  - JUnit 5.11 (Mockito 5.20 en el cliente web; JUnit 4 en Android).

## 5. Endpoint nuevo: importar todos los países (ERS, ECUD y diagramas)

Se acordó en el punto de control 1. El ECUD2 ya menciona importar todos, pero el contrato del ERS no tiene la ruta.

- Agregar `POST /api/Paises/importar/todos`. No tiene cuerpo y responde 200 `{recibidos, registrados, actualizados}` con el mensaje «Importación finalizada»; en caso de error, 502 o 500.
- ECUD2: separar el flujo «Importar todos» del de importar un país (con `POST /Paises/importar` y cuerpo `{consulta, nombre}`). Cada cliente tiene un botón u opción «Importar todos» que pide confirmación.
- Diagrama de secuencia `Consultar_Importar_Pais`: agregar el mensaje `importarTodos()` del controlador a `ConsumoAPIPaises.obtenerTodos()` y a `PaisDAO.importarTodos()`.

## 6. Vencimiento del token (ECUD)

Hay una contradicción entre dos casos de uso:

- ECUD2, alterno 1 (línea 160): «si el token no está registrado o pasaron más de 15 minutos sin uso… mensaje «Token inválido o sesión expirada»».
- ECUD8, alterno 3 (línea 483) y el resto del ECUD: el token vive hasta el cierre de sesión o el reinicio del servidor, y el mensaje es «Token inválido o sesión no activa».

En el punto de control 1 se decidió que el token **no vence**. Hay que corregir el ECUD2 para que diga «si el token no está registrado» y use el mensaje «Token inválido o sesión no activa» (`Mensajes.TOKEN_INVALIDO`).

## 7. Detalles de comportamiento que conviene documentar (ERS y ECUD)

- **Informes sin países (ECUD7, alterno 2):** el servidor responde estado ERROR con «No hay países registrados para generar informes» y **HTTP 200**, no 404, porque no es un recurso inexistente. Las seis regiones aparecen siempre en los informes, con conteo y promedio 0 si no tienen países. El empate del país más poblado se resuelve por nombre.
- **Registrar (ECUD4):** responde **201**; actualizar y eliminar responden 200. Un nombre duplicado responde 409.
- **ECUD10 Respuesta estructurada:**
  - una ruta inexistente responde 404 y un método no permitido 405, con la estructura `{estado, mensaje, datos}`;
  - con estado ERROR, `datos` va como `null`;
  - mensajes agregados en `Mensajes.java`: «Datos no válidos», «Recurso no encontrado» y «Método no permitido».
- **Token:** se acepta con o sin el prefijo `Bearer ` en `Authorization`.
- **Región «Antarctic»:** se traduce a Antártida, que está en el catálogo. Un país con una región fuera del catálogo se descarta y queda en el log (ECUD9, alterno 4).
- **Conexión a MySQL:** la URL JDBC lleva `sslMode=DISABLED&allowPublicKeyRetrieval=true`. Dentro de Payara la negociación SSL con el MySQL local falla con `unknown_ca`.
- **Configuración de los clientes:** se llama `cliente.properties` porque el `.gitignore` excluye `config.properties`, que es el archivo con la API key del servidor.
  - Consola: `servidor.url`, o el primer argumento del jar.
  - Escritorio: `ws.host`, `ws.port` y `ws.context`, junto al jar.
  - Web: los mismos valores, dentro del WAR.
  - Android: `servidor=` en `gradle.properties`, o `-Pservidor=` al compilar.

## 8. Diagrama de clases del servidor (`04_CLASES/Servidor_APIPaises.oom`)

El diagrama tiene las clases principales. Faltan las que se agregaron al implementar:

| Paquete | Clase | Responsabilidad |
|---|---|---|
| `config` | `Configuracion` | Lee la BD y la API: propiedad del sistema, luego variable de entorno, luego `config.properties`, luego el valor por defecto |
| `controladores` | `ApplicationConfig` | `@ApplicationPath("api")` |
| `controladores` | `ControladorBase` | Traduce las excepciones a la respuesta estructurada y su código HTTP |
| `modelos` | `Mensajes` | Textos del ECUD |
| `modelos.dto` | `SolicitudPais` | Cuerpo `{consulta, nombre}` de registrar e importar |
| `modelos.dto` | `ResultadoBusqueda` | `{paises, total}` |
| `modelos.dto` | `ResultadoImportacion` | `{pais, accion}` |
| `servicios` | `ApiExternaException` | Falla de la API con su tipo: `NO_DISPONIBLE` 502, `SIN_COINCIDENCIAS` 404, `RESPUESTA_INVALIDA` 502 |
| `utilidades` | `Textos` | Normaliza (sin tildes ni mayúsculas) y recorta textos |
| `vista` | `JsonResponseView` | Construye la respuesta JSON UTF-8 |
| `vista` | `ErrorInesperadoMapper` | `ExceptionMapper`: 404, 405 y 500 con la estructura uniforme |
| `filtros` | `TokenFilter` | Ya está; moverlo al paquete `filtros` |

Operaciones que faltan:

- `TokenService.usuarioDe(token)`;
- `ConsumoAPIPaises.obtenerPais(consulta, nombre)`, que elige el país pedido entre los resultados de la consulta;
- `LoginController.logout` recibe el encabezado `Authorization`, no el token suelto.

## 9. Diagrama de arquitectura y de casos de uso

- `05_ARQUITECTURA`: anotar que la API externa es REST Countries **v5** con API key. Agregar que la base corre en Docker (MySQL 8.4) y que los clientes se configuran con `cliente.properties`.
- `01_CASOS_USO`: agregar «Importar todos los países» como extensión de Importar País. Quitar los casos de Universidades (punto 3).
- `02_ACTIVIDAD/Iniciar_Sesion.oom` y `03_SECUENCIA/Consultar_Importar_Pais.oom` no tienen su copia `.oob`. PowerDesigner la crea al guardar de nuevo.

## 10. Interfaces de los clientes (opcional, ERS sección de interfaces)

Las interfaces siguen al proyecto de referencia EurekaBank (TG.1.1), como se pidió:

- **Consola:** menú numerado, tablas alineadas, mensajes de error con el prefijo `ERROR:`.
- **Escritorio:** ventana de 1100 × 760, con un menú lateral (Inicio, Países, Consultar en la API, Buscar países, Informes) y un indicador del estado del servidor.
- **Web:** las mismas secciones en un menú lateral, con confirmación antes de guardar o eliminar.
- **Android:** cuatro pestañas (Inicio, Países, API, Informes). La búsqueda y los filtros por región están dentro de Países, con filtros que se aplican al elegirlos.

Si el ERS describe otra distribución de pantallas, conviene actualizarla con estas capturas, que están en el manual (`MANUAL_JAVA_MYSQL_APIPAISES_SOSA.docx`).
