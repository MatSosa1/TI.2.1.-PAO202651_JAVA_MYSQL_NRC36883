# Plan de desarrollo por fases: APIPaises (REST / Java / MySQL)

Fuentes del plan:
- el ERS y el ECUD (RQF-001 a RQF-010);
- los diagramas de PowerDesigner: clases `Servidor_APIPaises`, arquitectura MVC y secuencia de los cinco flujos;
- el enunciado del taller;
- el proyecto de referencia EurekaBank (`TG.1.1. PAO202651_SOAP_JAVA_CONBDD_NRC36883_GR04`), que guía la estructura y las interfaces de los cuatro clientes.

Reglas de trabajo:
- No se hacen commits ni push.
- Cada fase termina con un punto de control: resumen de lo hecho, cómo probarlo, dudas y propuestas para la fase siguiente, y confirmación manual antes de avanzar.

---

## 0. Hallazgo crítico antes de empezar: REST Countries v3.1 ya no funciona

El 24/11/2026 se probó `https://restcountries.com/v3.1/all` y `/v3.1/name/{nombre}`. Ambas URL redirigen y devuelven:

```json
{ "success": false, "data": null,
  "errors": [{ "message": "This API version has been deprecated ... migrate to our new version (v5)." }] }
```

Las versiones v1 a v4 se dieron de baja. La versión vigente es la **v5** y cambia tres cosas:

| Aspecto | v3.1 (ERS/ECUD/taller) | v5 (vigente) |
|---|---|---|
| Base URL | `https://restcountries.com/v3.1` | `https://api.restcountries.com/countries/v5` |
| Autenticación | ninguna | `Authorization: Bearer <API_KEY>`: requiere crear cuenta gratuita en restcountries.com |
| Todos los países | `/all?fields=...` (un solo arreglo) | `GET /countries/v5?limit=100&offset=N` paginado (≈249 países ⇒ 3 páginas), respuesta en `data.objects` + `data.meta` |
| Buscar por nombre | `/name/{nombre}` (404 si no hay) | `GET /countries/v5/name?q={nombre}` (subcadena en common/official/alternates/native; **no** busca traducciones) y `GET /countries/v5/names.translations?q={nombre}` para nombres en español |
| Nombre en español | `translations.spa.common` | `names.translations.spa.common` (respaldo: `names.common`) |
| Capital | `capital: ["Quito"]` | `capitals: [{ name, attributes: { primary } }]` |
| Región | `region` | `region` (Africa, Americas, Asia, Europe, Oceania, Antarctic) |
| Población | `population` | `population` |
| Moneda | `currencies: { USD: { name, symbol } }` | `currencies: [{ code, name, symbol }]` |
| Límites | sin límite | 20 peticiones cada 10 s (HTTP 429) y cuota mensual del plan gratuito (HTTP 403) |

**Impacto en el diseño:** todo el cambio se concentra en `ConsumoAPIPaises`. El ECUD9 ya lo define como el único lugar que conoce la API, así que controladores, DAO y clientes no cambian. La API key se guarda en la configuración del servidor y nunca se versiona.

### Decisiones tomadas (punto de control 1, 24/11/2026)

1. **API externa:** REST Countries **v5 con API key**. La key va en `config.properties`, que no se versiona. Debe crearse la cuenta antes de la Fase 3; las pruebas de la Fase 2 usan JSON de ejemplo.
2. **Importar todos:** se agrega `POST /api/Paises/importar/todos`, que importa en una sola transacción y devuelve `ResumenImportacion`. Los 4 clientes tienen un botón «Importar todos» en «Consultar país en la API».
3. **Token sin vencimiento**, según ECUD8: vale hasta el logout o el reinicio del servidor. Mensaje único: «Token inválido o sesión no activa».
4. **Proyectos:** se aprueban los nombres propuestos y `groupId` `ec.edu.monster`, compilando para **Java 21**.

---

## 1. Visión general de la solución

```
 Clientes (MVC cada uno)                Servidor RESTful (MVC)                 Externos
┌──────────────────────┐   HTTP+JSON   ┌───────────────────────────────┐  HTTPS  ┌──────────────────┐
│ CLICON  consola      │──────────────▶│ JAX-RS  /api                  │────────▶│ REST Countries v5│
│ CLIESC  Swing        │  Authorization│  TokenFilter → Controllers     │         └──────────────────┘
│ CLIWEB  JSP/Servlet  │   <token>     │  Services (Token, Hash, API)  │  JDBC   ┌──────────────────┐
│ CLIMOV  Android      │◀──────────────│  DAO (Usuario, Region, Pais)  │────────▶│ MySQL APIPaisesDB│
└──────────────────────┘ {estado,      └───────────────────────────────┘         └──────────────────┘
                          mensaje,datos}
```

- **Servidor:** Java 21 sobre Payara 7 (Jakarta EE 10 API, igual que la referencia) con Jakarta REST (JAX-RS) y DAO con JDBC puro (`mysql-connector-j`). Gson parsea REST Countries, como pide el taller, y JSON-B, incluido en Payara, serializa las respuestas.
- **Convenciones:** `groupId` `ec.edu.monster` y paquetes en español, igual que en la referencia. Nombres de proyecto propuestos:

  | Carpeta | Proyecto |
  |---|---|
  | `04. SERVIDOR` | `WS_APIPAISES_JAVA_REST` |
  | `04. CLICON` | `CONSOLE_APIPAISES_JAVA_REST` |
  | `04. CLIESC` | `ESC_APIPAISES_JAVA_REST` |
  | `04. CLIWEB` | `web_apipaises_rest_java` |
  | `04. CLIMOV` | `MOV_APIPAISES_JAVA_REST` |

- **Contrato único:** todos los clientes consumen el mismo contrato (sección 3) y reutilizan literalmente los mensajes del ECUD.

---

## FASE 0: Planificación (esta fase)

- [x] Lectura de ERS, ECUD, diagramas (clases, arquitectura, secuencia) y enunciado.
- [x] Análisis del proyecto de referencia (servidor y 4 clientes).
- [x] Verificación de la API externa: se detectó la baja de la v3.1.
- [x] Verificación del entorno:

  | Herramienta | Estado |
  |---|---|
  | JDK | 21.0.12 |
  | Maven | 3.9.11 |
  | Docker | 29.9 |
  | Payara 7 | `C:\payara7` |
  | Android SDK | `C:\Android` |
  | Cliente `mysql` | no está en el PATH; la base se levanta con Docker, como en la referencia |

- [x] Este documento.

**Punto de control 0:** decisiones abiertas (al final del documento).

---

## FASE 1: Base de datos (`02. MER`, `03. BDD`)

**Objetivo:** dejar `APIPaisesDB` lista para el servidor, con un solo script de creación y uno de carga, como recomienda el ERS.

Entregables:
1. `03. BDD/APIPaisesDB.sql`: creación de la base y las tablas:
   - `Regiones`
     - `IdRegion INT AUTO_INCREMENT PK`
     - `Nombre VARCHAR(20) UNIQUE NOT NULL`
   - `Paises`
     - `Nombre VARCHAR(100) PK`
     - `Capital VARCHAR(100) NOT NULL`
     - `IdRegion INT NOT NULL FK → Regiones`
     - `Poblacion BIGINT NOT NULL CHECK (Poblacion >= 0)`
     - `Moneda VARCHAR(100) NOT NULL`
   - `Usuarios`
     - `IdUsuario INT AUTO_INCREMENT PK`
     - `Usuario VARCHAR(50) UNIQUE NOT NULL`
     - `ClaveHash CHAR(64) NOT NULL` (SHA-256 en hex)
     - `Nombre VARCHAR(100) NOT NULL`
   - Juego de caracteres `utf8mb4`, InnoDB (para las transacciones de importación) e índices en `Paises(IdRegion)` y `Paises(Poblacion)`.
2. `03. BDD/init.sql`: carga de las 6 regiones (América, Europa, Asia, África, Oceanía, Antártida) y de los usuarios de prueba, con la clave ya en hash SHA-256.
3. `03. BDD/docker-compose.yml`: contenedor MySQL 8 que ejecuta ambos scripts al iniciar, igual que en la referencia.
4. `02. MER/`: modelo entidad-relación de las 3 tablas. PowerDesigner es manual; yo dejo la especificación lista para dibujarlo o un diagrama en texto si lo prefieres.
5. `.gitignore` en la raíz, basado en el de la referencia: `target/`, `build/`, configuración con secretos, `local.properties`. Los `.oob` **no** se ignoran porque hoy ya están versionados.

**Verificación:**
- `docker compose up -d` termina sin errores.
- Las consultas de comprobación devuelven 6 regiones y los usuarios cargados.
- Insertar un país duplicado falla por la clave primaria.
- Insertar una población negativa falla por el CHECK.

**Estado (24/11/2026): completada y verificada** con MySQL 8.4 en Docker:
- [x] 3 tablas con collation `utf8mb4_0900_ai_ci`.
- [x] 6 regiones con tildes correctas.
- [x] Usuarios `admin`/`admin123` y `msosa`/`msosa`, con hash de 64 caracteres.
- [x] La búsqueda parcial encuentra `quito` → Quito y `america` → América.
- [x] Las restricciones rechazan lo que deben:

  | Caso | Error |
  |---|---|
  | Nombre duplicado, aunque cambie la tilde (`Perú`/`peru`) | 1062 |
  | Población negativa | 3819 |
  | Región inexistente | 1452 |
  | Borrar una región que tiene países | 1451 |

**Punto de control 1.**

---

## FASE 2: Servidor, núcleo (modelo, DAO y servicios)

Proyecto Maven `04. SERVIDOR/WS_APIPAISES_JAVA_REST` (`war`). Clases tomadas del diagrama de clases:

| Paquete | Clases |
|---|---|
| `modelos.entidades` | `Usuario`, `Region`, `Pais` |
| `modelos.dto` | `PaisDTO` (con `guardado`), `RespuestaEstructurada<T>` (`ok(...)`, `error(...)`), `LoginRequest`, `LoginResponse`, `SolicitudPais` (`consulta`, `nombre`), `ResumenImportacion`, `ConteoRegion`, `PromedioRegion`, `InformeEstadistico` |
| `config` | `Configuracion`: lee `config.properties` (URL, usuario y clave de MySQL; URL base, API key y timeout de REST Countries). Las variables de entorno tienen prioridad. Se versiona solo `config.example.properties` |
| `dao` | `ConexionBD`, `UsuarioDAO`, `RegionDAO`, `PaisDAO`: SQL con `PreparedStatement`; búsqueda parcial sin distinguir mayúsculas; importación por lotes dentro de una sola transacción |
| `servicios` | `HashUtil` (SHA-256 en hex), `TokenService`, `ConsumoAPIPaises` |

`TokenService`:
- El token es SHA-256 de usuario + hora + valor aleatorio de `SecureRandom`, es decir, 64 caracteres hex.
- Los tokens activos viven en un `ConcurrentHashMap` en memoria.

`ConsumoAPIPaises` (ECUD9 adaptado a v5):
- Usa `HttpClient` con timeout de 15 s y Gson.
- `obtenerTodos()` recorre las 3 páginas.
- `obtenerPorNombre()` consulta primero el agregado `name` y, si no hay coincidencias, `names.translations`.
- Mapeo de campos:
  - nombre: el nombre en español, o el común si no hay traducción;
  - capital: la que tiene `primary`, si no la primera, o «Sin capital»;
  - región: traducida al español;
  - moneda: «`name` (`code`)», o «Sin moneda».
- Errores tipados: API no disponible (timeout, 5xx, 401/403/429) ⇒ 502; sin coincidencias ⇒ 404; JSON inválido ⇒ 502.

**Pruebas JUnit 5 sin red ni base de datos:**
- `HashUtil`: vector conocido de SHA-256.
- `TokenService`: genera, valida e invalida.
- Mapeo de `ConsumoAPIPaises` sobre JSON de ejemplo de la v5 en `src/test/resources`, incluidos los casos sin capital, sin moneda, sin traducción y región desconocida.
- `RespuestaEstructurada`.

**Verificación:** `mvn -q test` en verde.

**Estado (24/11/2026): completada y verificada.** `mvn clean package` construye `target/WS_APIPAISES_JAVA_REST.war`, que empaqueta solo Gson y mysql-connector-j. Corren **48 pruebas en verde**:

| Clase de prueba | Qué cubre | Pruebas |
|---|---|---|
| `ConsumoAPIPaisesTest` | Mapeo v5: nombre en español o común, capital principal, «Sin capital», «Sin moneda», región desconocida descartada, recorte a 100 caracteres, JSON de la v3.1 dada de baja ⇒ respuesta no válida | 13 |
| `ConsumoAPIPaisesTest` con `RestCountriesFalso` (servidor HTTP local) | Paginación, API key en `Authorization`, búsqueda por traducción, 401/403/429/500, tiempo agotado y sin conexión ⇒ 502, sin coincidencias ⇒ 404 | 9 |
| `DaoIntegracionTest` (contra el MySQL de Docker; se omite si no está) | Login con el hash de `init.sql`, regiones, mantenimiento, duplicado, búsqueda parcial y con comodines escapados, importación con rollback total e informes con las 6 regiones y desempate por nombre | 10 |
| `HashUtilTest`, `TokenServiceTest`, `ConfiguracionTest`, `TextosTest`, `RespuestaEstructuradaTest` | Servicios, configuración y utilidades | 16 |

Las pruebas de integración usan países con el prefijo `ZZ Prueba` y los borran al terminar.

**Punto de control 2.**

---

## FASE 3: Servidor, API REST (controladores, filtro y despliegue)

| Pieza | Responsabilidad |
|---|---|
| `controladores.ApplicationConfig` | `@ApplicationPath("api")` |
| `controladores.LoginController` (`/Login`) | `POST login`, `POST logout` |
| `controladores.PaisesController` (`/Paises`) | listar, buscar, consultarExterno, importar, importarTodos, registrar, actualizar, eliminar |
| `controladores.ReportesController` (`/Reportes`) | `GET` informes |
| `filtros.TokenFilter` (`ContainerRequestFilter`) | Exige un token válido en todo menos `Login/login`. Responde 401 con «Token inválido o sesión no activa» |
| `vista.JsonResponseView` | La «vista» del servidor según el diagrama de arquitectura: convierte la `RespuestaEstructurada` y el código HTTP en un `Response` JSON |
| `vista.ErrorInesperadoMapper` (`ExceptionMapper`) | Responde 500 con «Ocurrió un error inesperado, intente nuevamente» sin exponer trazas (ECUD10) |

**Contrato final:** sección 3.

**Verificación:**
- Despliegue en `C:\payara7`: `asadmin deploy` del `.war`.
- Script `05. DOCUMENTACION/pruebas_api.http` (o una colección de Postman) que recorre todos los flujos y alternos del ECUD: 401, 404, 409, 400, 502 simulado con una URL inválida, y respuesta 201 al registrar.

**Estado (24/11/2026): completada y verificada.**

Despliegue:
- El servidor quedó desplegado en `domain1` de Payara 7 y responde en `http://localhost:8080/WS_APIPAISES_JAVA_REST/api`.
- Las demás aplicaciones del dominio no se tocaron.

Pruebas:
- **52 pruebas JUnit en verde**: las 48 de la Fase 2, 3 nuevas de `obtenerPais` y 1 de `TokenFilter`.
- **Pruebas de punta a punta** con `pruebas/pruebas_api.sh`, que verifica código HTTP, estado y mensaje:
  - **Sin API key: 30 de 30.** Login y sus alternos, token, 404/405, listar, buscar, filtrar, región no válida, validaciones, 502 en todo lo que consulta REST Countries, informes y logout.
  - **Con una REST Countries v5 simulada: 39 de 39.** Se apuntó Payara a la simulación con propiedades del sistema temporales y se probó todo el ciclo: consultar (404 y 200), importar y reimportar (registrado y actualizado), `guardado=true`, registrar (201), duplicado (409), actualizar por nombre en español mediante las traducciones, eliminar, importar todos e informes con las 6 regiones.
  - Al terminar se borraron los datos de prueba y las propiedades temporales.
- `pruebas/pruebas_api.http` queda para pruebas manuales desde VS Code o IntelliJ.

Ajustes que salieron en esta fase:
- **SSL con MySQL.** Dentro de Payara, la JVM presenta el certificado del dominio y MySQL lo rechaza (`unknown_ca`). La URL JDBC por defecto lleva `sslMode=DISABLED&allowPublicKeyRetrieval=true`, porque la base es local en Docker.
- **Propiedades del sistema en Payara.** `asadmin create-system-properties` no acepta valores con `:`, aunque vayan escapados. Para cambiar `restcountries.url` primero se crea la propiedad y luego se usa `asadmin set "servers.server.server.system-property.restcountries.url.value=<url>"`. La API key, que no lleva `:`, se puede crear directamente: `asadmin create-system-properties restcountries.apiKey=<key>`.

**Punto de control 3.** Con el servidor congelado, empiezan los clientes.

---

## FASES 4 a 7: Clientes

Base común a los cuatro clientes, que replica la referencia:
- `controlador` / `modelo(s)` / `vista` separados.
- Validación previa al envío (RNF de entrada del ERS).
- El token se guarda en la sesión y viaja en `Authorization`.
- Ante un 401: se borra la sesión, se muestra «Su sesión no es válida. Inicie sesión nuevamente.» y se vuelve al login.
- Sin conexión: «No se pudo conectar con el servidor. Verifique la red e intente nuevamente.»
- Población con separador de miles.
- URL del servidor configurable.
- Pruebas con un servidor falso, como `ServidorFalso` y `FakeSoapServer` en la referencia.

Menú común (ECUD1, paso 7): **Países · Consultar país en la API · Buscar países · Informes · Cerrar sesión**.

Diferencias de interfaz respecto a EurekaBank:
- No hay selector de perfil Empleado/Cliente porque hay un único actor.
- El sello de servicio cambia de «SOAP / Java» a **«REST / Java»**.
- La marca cambia de «EB / EurekaBank» a **«AP / APIPaises»**.

### FASE 4: Cliente de consola (`04. CLICON/CONSOLE_APIPAISES_JAVA_REST`)

Guía: `CONSOLE_EUREKABANK_JAVA_SOAP`.

- **`vista.utils.MessagesUtils` reutilizado:**
  - consola en UTF-8 (`chcp 65001`);
  - arte ASCII de inicio;
  - títulos entre separadores `=====`;
  - menús numerados con «Seleccione una opcion»;
  - clave oculta con `Console.readPassword`;
  - confirmación `(S/N)`;
  - «Procesando...»;
  - tablas de texto con ` | ` y `-+-`.
- **Errores:** prefijo `ERROR:`, como exige el ECUD, en lugar del `[!]` de la referencia.
- **Vistas:**
  - `LoginView`
  - `MenuView`
  - `PaisesView`: lista con opciones Nuevo, Actualizar y Eliminar
  - `ConsultaApiView`: consultar y guardar, más «Importar todos»
  - `BusquedaView`: nombre, capital y región elegida de una lista numerada
  - `InformesView`: tres tablas de texto, sin gráfico (ECUD7)
- **HTTP y JSON:** `modelos.http` con `HttpClient` y Gson; `ConfigHTTP` guarda la sesión.

**Estado (24/11/2026): completada y verificada.**

Construcción:
- `mvn clean package` genera `target/CONSOLE_APIPAISES_JAVA_REST.jar`, un jar ejecutable con Gson incluido.
- Se ejecuta con `java -jar target/CONSOLE_APIPAISES_JAVA_REST.jar [url]`. La URL por defecto está en `src/main/resources/cliente.properties` y también se puede pasar con `-Dservidor.url`.
- Las pruebas van en `src/main/java/ec/edu/monster/pruebas`, como en la consola de referencia, y quedan fuera del jar.

Pruebas:
- **20 pruebas JUnit en verde** con `ServidorFalso`. Cubren URL, método, token sin prefijo, cuerpo JSON, codificación de tildes y espacios, 401 como sesión no válida, sin red o tiempo agotado ⇒ «No se pudo conectar…», respuesta ilegible ⇒ el cliente no se detiene, espera larga para «Importar todos», y además validaciones y formato.
- **Sesión real con datos guardados**, contra el servidor desplegado y la REST Countries v5 simulada:
  - login fallido y login correcto;
  - consultar «ecuador» y guardarlo: la columna «Guardado» pasa a «Sí»;
  - importar todos: 7 registrados y 1 actualizado;
  - filtrar por Europa (el filtro se mantiene) y eliminar con confirmación;
  - quitar el filtro, buscar «ecua» e informes en 3 tablas;
  - cerrar sesión y salir.
- **Servidor apagado:** muestra «ERROR: No se pudo conectar con el servidor…».
- Al terminar se borraron los datos de prueba.

**Punto de control 4.**

### FASE 5: Cliente de escritorio Swing (`04. CLIESC/ESC_APIPAISES_JAVA_REST`)

Guía: `ESC_EUREKABANK_JAVA_SOAP`.

- **`Estilos`:** misma paleta (`PRIMARIO #1F4E79`, `FONDO #F4F6F8`, `ERROR #B91C1C`…), las mismas fuentes y las fábricas `botonPrimario`, `botonSecundario`, `botonNavegacion`, `panelTarjeta` y `entrada`.
- **`LoginFrame` (640×480):**
  - título «APIPaises» y subtítulo «Países del mundo · Servicio REST / Java»;
  - campos Usuario y Contraseña, con «Ingresar» alineado a la derecha;
  - barra de menús Archivo/Ayuda;
  - `IndicadorEstado` («Servicio disponible» o «Servicio no disponible», con un punto de color y consulta periódica).
- **`PrincipalFrame` (940×640):**
  - encabezado con la marca «AP», el sello «REST / Java», el nombre del usuario y «Cerrar sesión»;
  - barra lateral «MENÚ»: Inicio, Países, Consultar en la API, Buscar países, Informes;
  - `CardLayout` para cambiar de pantalla;
  - barra inferior «Sesión activa · Servicio disponible».
- **Paneles:**

  | Panel | Contenido |
  |---|---|
  | `InicioPanel` | tarjetas de acceso rápido |
  | `PaisesPanel` | `JTable` (el «DataGrid»), filtro por región en la parte inferior, total y botones Nuevo/Actualizar/Eliminar |
  | `ConsultaApiPanel` | campo de búsqueda, tabla con la columna «Guardado», Guardar e Importar todos |
  | `BusquedaPanel` | Nombre, Capital y lista de regiones |
  | `InformesPanel` | dos tablas, tarjeta del país más poblado y **gráfico de barras dibujado con Graphics2D**, sin librerías externas |

- **Concurrencia y estructura:** `SwingWorker` (`enSegundoPlano`) para no congelar la interfaz, composición en `App` e interfaces `*Vista` para probar los controladores con dobles.

**Estado (24/11/2026): completada y verificada.**

Construcción:
- `mvn clean package` genera `target/ESC_APIPAISES_JAVA_REST.jar`, un jar ejecutable con Gson incluido.
- Junto al jar queda `target/cliente.properties` (`ws.host`, `ws.port`, `ws.context`): se edita para apuntar a otro servidor sin recompilar. También se puede pasar con `-Dcliente.config=`.
- El archivo se llama `cliente.properties` porque `.gitignore` excluye todo `config.properties`.

Arquitectura, igual que la referencia:
- `App` como raíz de composición;
- interfaces `Vistas.*`;
- controladores con `Sesion` y una base `ControladorConSesion` que, ante un 401, cierra la sesión y vuelve al login con «Su sesión no es válida…»;
- servicios detrás de las interfaces `LoginServicio`, `PaisServicio` y `ReporteServicio`, implementadas por `ServiciosRest` (Gson y `java.net.http`).

Interfaz:
- `Estilos` e `IndicadorEstado` copiados de EurekaBank, con la misma paleta y fuentes.
- `LoginFrame` y `PrincipalFrame` con el mismo diseño: marca «AP», sello «REST / Java», menú lateral con la opción activa resaltada y línea «Sesión activa · Servicio disponible».
- La ventana principal mide 1100×760 en lugar de 940×640 de la referencia, para que las tablas de cinco o seis columnas y los informes quepan sin barras de desplazamiento.
- Las pantallas:
  - **Inicio:** tarjetas de acceso.
  - **Países:** tabla, filtros por región abajo que filtran al instante, y Nuevo (diálogo para consultar en la API y registrar), Actualizar y Eliminar con confirmación.
  - **Consultar en la API:** tabla con «Guardado», Guardar con confirmación e Importar todos.
  - **Buscar países:** nombre, capital y región; la región filtra al instante.
  - **Informes:** dos tablas con las 6 regiones, tarjeta del país más poblado y gráfico de barras con `Graphics2D`.
- Barra de progreso mientras se procesa cada operación.

Pruebas:
- **30 pruebas JUnit en verde** en `src/test/java`, con dobles como en la referencia (`dobles/` y `pruebas/`). Cubren los controladores de todos los ECUD, los servicios REST (URL, método, token, cuerpo, codificación, 401 de login frente a 401 de token, errores y respuesta ilegible), la configuración, la sesión, los formatos y el estado del servidor.
- **Verificación visual con capturas reales de pantalla**, contra el servidor desplegado y la REST Countries simulada: login, inicio, países, filtro Europa, diálogo Nuevo, consulta en la API, búsqueda e informes.
- Al terminar se borraron los datos de prueba.

**Punto de control 5.**

### FASE 6: Cliente web (`04. CLIWEB/web_apipaises_rest_java`)

Guía: `web_eurekabank_soap_java`. JSP + Servlets en Jakarta EE 10, `war` desplegado en Payara.

- **CSS:** `apipaises.css` con los mismos *tokens*:
  - colores `--brand-900 #0b2545`, `--brand-500 #1d4e89`, `--gray-*`, `--danger-*` y `--success-*`;
  - fuente Inter con respaldo del sistema, sin CDN.

  Además `login.css`, `menu.css` y `operacion.css`.
- **Sello de servicio:** `badge__protocol--rest` (`--service-rest #b45309`) junto a `badge__platform--java`.
- **Vistas en `WEB-INF/ec/edu/monster/vista`:**
  - `login.jsp` (tarjeta centrada, sin selector de rol)
  - `menu.jsp`
  - `paises.jsp`
  - `consulta.jsp`
  - `busqueda.jsp`
  - `informes.jsp`
  - parciales `header.jspf`, `sidebar.jspf` y `csrf.jspf`
- **Controladores:** un servlet por pantalla, más `AuthFilter` y `CsrfFilter`.
- **Comunicación con el servidor:** `modelos/servicios` con `HttpClient` y Gson. El token se guarda en la `HttpSession`.
- **Gráfico de informes:** barras en SVG o CSS en línea, sin librerías externas.
- **Filtro por región:** se aplica al cambiar la región (JS mínimo que envía el formulario) y funciona también sin JS.

**Estado (24/11/2026): completada y verificada.**

Construcción y despliegue:
- `mvn clean package` genera `target/web_apipaises_rest_java.war`, desplegado en `domain1`: `http://localhost:8080/web_apipaises_rest_java/`.
- La dirección del servidor está en `src/main/resources/cliente.properties` (`ws.host`, `ws.port`, `ws.context`).

Igual que la web de EurekaBank:
- un servlet por pantalla;
- JSP en `WEB-INF/ec/edu/monster/vista`;
- parciales `header`, `sidebar` y `csrf`;
- `AuthFilter`, sin roles porque hay un solo tipo de usuario, y `CsrfFilter`;
- confirmaciones sin JavaScript (tarjeta `op-dialog` con Volver y Confirmar);
- Post/Redirect/Get con mensajes flash;
- un nuevo id de sesión al iniciar sesión;
- Cache-Control `no-store`.

CSS:
- Son los de EurekaBank: `apipaises.css` (antes `eurekabank.css`), `login.css`, `menu.css` y `operacion.css`, con los mismos tokens y la fuente Inter, sin CDN.
- El sello es «Servicio · REST · Java», con el color REST que ya traía la referencia.
- `paises.css` solo agrega lo que no existía: filtros por región, tarjeta ancha, búsqueda, informes y gráfico.

Pantallas:
- Login y Menú (tarjetas).
- **Países:** tabla, Actualizar y Eliminar por fila (con confirmación), Nuevo, y los filtros por región abajo.
- **Nuevo país:** consultar, elegir y Registrar; un duplicado muestra «Ya existe…» en la misma pantalla.
- **Consultar en la API:** «Guardado» Sí/No, Guardar con confirmación e Importar todos con confirmación.
- **Buscar:** la región se aplica al cambiarla.
- **Informes:** dos tablas, tarjeta del país más poblado y gráfico de barras SVG generado en el servidor.
- `js/procesando.js` (opcional, sin dependencias) muestra «Procesando...» mientras el servidor responde.
- Ante un 401 se borra la sesión y se vuelve a `/login?sesion=expirada`, que muestra «Su sesión no es válida…».

Pruebas:
- **34 pruebas JUnit + Mockito en verde** en `src/test/java`, como en la referencia. Cubren controladores con un navegador falso y el servidor REST simulado, filtros, cliente REST, formatos y el cálculo del gráfico.
- **Punta a punta en Payara: 34 de 34** con `curl`, contra el servidor y la REST Countries simulada. Cubre CSRF (incluido el 403 con token falso), login, consultar y guardar con confirmación, flash de una sola vez, importar todos, filtrar, actualizar sin perder el filtro, eliminar, registrar y duplicado, buscar, informes y logout.
- **Capturas con Edge headless** de las 10 pantallas, revisadas. Se agrandó el texto del gráfico.
- Al terminar se borraron los datos de prueba.

**Punto de control 6.**

### FASE 7: Cliente móvil Android (`04. CLIMOV/MOV_APIPAISES_JAVA_REST`)

Guía: `MOV_EUREKABANK_JAVA_SOAP`. Java, Gradle, `minSdk 24`, Material Components.

- **Paleta en `colors.xml`:** la de la referencia (`#344C74`, `#22365A`, `#E9EEF6`, `#F3F4F6`…).
- **`LoginActivity`:** tarjeta con `TextInputLayout`, botón principal e indicador de progreso circular, sin toggle de perfil.
- **`PrincipalActivity`:** `MaterialToolbar` con menú de sesión y `BottomNavigationView` con 4 pestañas:
  - Inicio
  - Países (incluye la búsqueda y los *chips* de región, la «lista de filtros en la parte inferior» del ERS)
  - API (consultar, guardar e importar todos)
  - Informes (tablas y gráfico de barras en una `View` propia)
- **Componentes:** `RecyclerView` con adaptadores, `SwipeRefreshLayout`, `BottomSheet` para confirmar y ver el detalle, y avisos en pantalla.
- **Red:**
  - `HttpURLConnection` con Gson, porque `java.net.http` no existe en Android 24;
  - `EjecutorTareas` saca las llamadas del hilo de interfaz;
  - la URL se define con `-Pservidor=http://10.0.2.2:8080/WS_APIPAISES_JAVA_REST/api/`;
  - `cleartextTraffic` habilitado solo para la red local.
- **Pruebas:** JUnit 4 en `pruebas`, con `TransporteFalso`.

**Estado (24/11/2026): completada y verificada en el emulador.**

Construcción:
- Con `gradlew assembleDebug` se genera `build/outputs/apk/debug/MOV_APIPAISES_JAVA_REST-debug.apk`.
- La URL del servidor está en `gradle.properties` (`servidor=`) o se pasa con `-Pservidor=`. Por defecto es `http://10.0.2.2:8080/WS_APIPAISES_JAVA_REST/api/`.
- Se usan las mismas versiones que la referencia: AGP 9.4.0, compileSdk 36, minSdk 24, Material 1.13.0 y Gson.

Igual que el móvil de EurekaBank:
- `AplicacionAPIPaises` como raíz de composición;
- `EjecutorTareas` para correr las llamadas en segundo plano;
- `SesionServicio`;
- `ControladorConSesion`, que borra la sesión ante un 401;
- `PestanaFragment`, que revisa la sesión al mostrarse y vuelve al login con «Su sesión no es válida…»;
- `NavegacionSesion` y `AjusteBordes`;
- los mismos colores, estilos y tema (renombrados `ap_*` y `Tema.APIPaises`).

Interfaz:
- Las ilustraciones de personajes de la referencia se reemplazaron por un globo terráqueo, en el logo y en el ícono de la app.
- **Login:** encabezado con degradado, tarjeta superpuesta, campos con ícono, clave oculta y progreso dentro del botón.
- **Principal:** barra superior con el usuario y «Cerrar sesión» (con confirmación), y 4 pestañas:
  - **Inicio:** saludo, tarjeta con los países guardados y opciones.
  - **Países:** búsqueda por nombre y capital, lista, chips de región abajo que filtran al instante, botón «Nuevo» (hoja para consultar en la API y registrar) y una hoja de detalle con Actualizar y Eliminar con confirmación. Aquí está «Buscar países», según el ERS 9.1.1.
  - **API:** consultar, estado «Guardado», Guardar con confirmación e Importar todos con confirmación.
  - **Informes:** tarjeta del país más poblado, gráfico de barras dibujado en un `Canvas` y tablas por región.
- Red con `HttpURLConnection` + Gson; `cleartextTraffic` está habilitado para la red local.

Pruebas:
- **23 pruebas JUnit 4 en verde** en `src/main/java/ec/edu/monster/pruebas` (excluidas del APK), con el servidor REST simulado: repositorio, controladores y formatos.
- **Recorrido en el emulador** Pixel 7, API 36, instalado con `sdkmanager` (emulador e imagen `google_apis_playstore;x86_64`), contra el servidor real de Payara y la REST Countries simulada:
  - login fallido con la clave borrada, y login correcto;
  - inicio;
  - consultar «ecuador» y guardarlo con confirmación (pasa a «Guardado»);
  - importar todos;
  - lista y filtro por Europa y por América;
  - detalle;
  - registrar un duplicado («Ya existe un país con ese nombre»);
  - eliminar con confirmación;
  - informes y cerrar sesión.
- **Ajustes tras revisar las capturas:**
  - el aviso de confirmación quedó anclado sobre las pestañas;
  - el chip de región activo usa el color primario;
  - el texto junto a «Importar todos» es más corto.
- Al terminar se borraron los datos de prueba y se apagó el emulador.

**Punto de control 7.**

---

## FASE 8: Cierre

- Prueba integral: los 4 clientes contra el mismo servidor, con la matriz de casos del ECUD.
- Actualizar `CLAUDE.md` con los comandos reales de compilación, despliegue y pruebas.
- Lista de diferencias para corregir en ERS y ECUD: la v5, los restos de .NET/SQL Server y Universidades, la versión de NetBeans y el endpoint de importar todos.
- Opcional: insumos para el manual y la presentación en `05. DOCUMENTACION`.

**Estado (24/11/2026):** completada salvo la prueba integral con la API real, que espera la API key.

- Manual: `MANUAL_JAVA_MYSQL_APIPAISES_SOSA.docx`, de 379 páginas, con la estructura del manual de referencia. Incluye el código completo, las capturas y la salida de las pruebas.
- Correcciones para ERS, ECUD y diagramas: `CORRECCIONES_ERS_ECUD.md`.
- `CLAUDE.md` actualizado.
- Prueba de punta a punta del servidor contra la API v5 falsa: 39/39.

**Punto de control 8 (final).**

---

## 3. Contrato REST del servidor (base `http://<servidor>:8080/WS_APIPAISES_JAVA_REST/api`)

Todas las respuestas tienen la forma `{ "estado": "OK" | "ERROR", "mensaje": "…", "datos": … }`. Con estado ERROR, `datos` va como `null`.

| Método y ruta | Token | Cuerpo / parámetros | Éxito | Errores |
|---|---|---|---|---|
| `POST /Login/login` | no | `{usuario, clave}` | 200 `{token, usuario}` | 401, 500 |
| `POST /Login/logout` | sí | — | 200 «Sesión cerrada» | 401 |
| `GET /Paises` | sí | — | 200 `[Pais]` ordenada por nombre | 401, 500 |
| `GET /Paises/buscar` | sí | `?nombre=&capital=&region=` | 200 `{paises, total}`, con el mensaje «Filtro aplicado…» si solo llega la región | 400 región, 401 |
| `GET /Paises/externo/{nombre}` | sí | — | 200 `[PaisDTO con guardado]` | 400, 404, 502, 401 |
| `POST /Paises/importar` | sí | `{consulta, nombre}` | 200 `{pais, accion: "registrado" \| "actualizado"}` | 400, 404, 502, 500 |
| `POST /Paises/importar/todos` | sí | — | 200 `{recibidos, registrados, actualizados}` | 502, 500 |
| `POST /Paises` | sí | `{consulta, nombre}` | **201** «País registrado correctamente» | 409, 404, 502 |
| `PUT /Paises/{nombre}` | sí | — (vuelve a consultar la API) | 200 «País actualizado correctamente» | 404, 502 |
| `DELETE /Paises/{nombre}` | sí | — | 200 «País eliminado correctamente» | 404 |
| `GET /Reportes` | sí | — | 200 `InformeEstadistico` | ERROR «No hay países registrados…», 401 |

Notas:
- Un objeto `Pais` en JSON tiene la forma `{nombre, capital, region, poblacion, moneda}`; el id de la región no se expone.
- Todas las respuestas incluyen `"datos"`; con estado ERROR va como `null`.
- Una ruta inexistente responde 404 y un método no permitido 405, ambos con la estructura uniforme.
- En `GET /Reportes`, las 6 regiones aparecen siempre: las que no tienen países, con conteo y promedio 0.
- El país más poblado se elige por población descendente; un empate se resuelve por nombre.

---

## 4. Riesgos y mitigaciones

| Riesgo | Mitigación |
|---|---|
| Sin API key de REST Countries v5 no se puede consultar ni importar | Crear la cuenta gratuita antes de la Fase 3. Las pruebas de la Fase 2 usan JSON de ejemplo y no dependen de la red |
| Límite de 20 peticiones cada 10 s y cuota mensual | La importación total hace solo 3 peticiones. Un 429 o 403 se traduce a 502 «La API REST Countries no está disponible, intente más tarde» |
| Buscar «Alemania» no encuentra coincidencias en el agregado `name` | Si no hay resultados, se consulta `names.translations?q=` y se filtra por `spa` |
| «Actualizar» vuelve a buscar por el nombre guardado, que está en español | El mismo respaldo por traducciones, emparejando por el nombre en español exacto |
| La v5 podría devolver «Antarctic» u otra región no registrada | El país se descarta y queda en el log, como indica ECUD9, alterno 4 |
| El emulador de Android no ve `localhost` | Usar `10.0.2.2`, o la IP de la LAN en un dispositivo físico |
