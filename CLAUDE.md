# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project status

University workshop (ESPE, NRC 36883, "TI.2.1. Taller APIs PÚBLICAS en JAVA y MySQL") building **APIPaises**: a system that consumes the public REST Countries API, stores the countries in MySQL, and exposes them to several clients. All documentation is in Spanish; write code identifiers, messages, and docs in Spanish to match.

Work proceeds in phases defined in `05. DOCUMENTACION/PLAN_DESARROLLO.md`. Each phase ends with a checkpoint where the user confirms before the next one starts. Do not commit or push. Done so far:
- the database (`03. BDD`);
- the complete server (`04. SERVIDOR/WS_APIPAISES_JAVA_REST`), deployed on Payara `domain1`;
- the console client (`04. CLICON/CONSOLE_APIPAISES_JAVA_REST`);
- the Swing desktop client (`04. CLIESC/ESC_APIPAISES_JAVA_REST`);
- the JSP/Servlet web client (`04. CLIWEB/web_apipaises_rest_java`), deployed on `domain1`;
- the Android client (`04. CLIMOV/MOV_APIPAISES_JAVA_REST`).

Phase 8, the closing work, is done except one item. Its deliverables are in `05. DOCUMENTACION`:
- `MANUAL_JAVA_MYSQL_APIPAISES_SOSA.docx`, the installation and usage manual. It follows the reference `MANUAL_SOAP_JAVA_CONBDD_GR4.docx`: same cover, styles and code boxes. It embeds the full source of every project.
- `CORRECCIONES_ERS_ECUD.md`, the list of fixes still to apply by hand to the ERS, the ECUD and the PowerDesigner diagrams.

The one remaining item is the integral test of the 4 clients against the real REST Countries v5. It waits for the user's API key. So far every API success path has been verified only against a fake v5 server.

If code changes after this, the manual's code listings go stale. Edit the .docx directly (unzip `word/document.xml`, or use Word), then update its table of contents in Word. Word COM automation works on this machine (`New-Object -ComObject Word.Application`) and can also export a PDF to check the layout. LibreOffice is not installed. The UI of each client must mirror the EurekaBank reference project at `C:\IMarcusDev\Universidad\8vo_semestre\ArquitecturaSw_36883_GR04\PARCIAL_1\CONBDD\TG.1.1. PAO202651_SOAP_JAVA_CONBDD_NRC36883_GR04`.

Commands:
- Database: `cd "03. BDD" && docker compose up -d`. This starts MySQL 8.4 on port 3306 with `root`/`12345` and runs `APIPaisesDB.sql` and `init.sql`. `docker compose down -v` resets it.
- Server: run these from `04. SERVIDOR/WS_APIPAISES_JAVA_REST`:
  - `mvn clean package` builds the war;
  - `mvn test` runs all tests. `DaoIntegracionTest` needs the Docker DB and is skipped without it;
  - `mvn test -Dtest=ConsumoAPIPaisesTest` runs a single class.
- Console client: from `04. CLICON/CONSOLE_APIPAISES_JAVA_REST`, `mvn clean package` builds a shaded, runnable jar. Run it with `java -jar target/CONSOLE_APIPAISES_JAVA_REST.jar [baseUrl]`. Its tests live in `src/main/java/ec/edu/monster/pruebas`, the course convention copied from the reference console; `testSourceDirectory` points there and the compiler excludes it. To drive the console in a script, pipe its input with `printf '%s\n' 1 admin admin123 ... | java -jar ...`.
- Desktop client: from `04. CLIESC/ESC_APIPAISES_JAVA_REST`, `mvn clean package` builds a runnable jar and copies `cliente.properties` next to it; the server address is in that file. Tests are in `src/test/java`, which is what the reference desktop does, and use doubles in `dobles/`. The Swing look (`vista/Estilos`, `IndicadorEstado`) is copied from the EurekaBank reference. Keep it in sync rather than restyling.
- Web client: from `04. CLIWEB/web_apipaises_rest_java`, `mvn clean package` builds the war; deploy it with `asadmin deploy --force=true`. The app is at `http://localhost:8080/web_apipaises_rest_java/`. The CSS is the EurekaBank web CSS. Only `paises.css` is new, so extend that file rather than restyling. Tests are in `src/test/java` and use Mockito, a fake browser (`NavegadorFalso`) and a fake REST server (`fakes/ServidorFalso`).
- Android client, from `04. CLIMOV/MOV_APIPAISES_JAVA_REST`:
  - set `ANDROID_HOME=C:\Android`, then run `./gradlew.bat testDebugUnitTest assembleDebug`;
  - unit tests are JUnit 4 in `src/main/java/ec/edu/monster/pruebas`, the reference's convention; a sourceSet filter excludes them from the APK;
  - the server URL comes from `servidor=` in `gradle.properties` or from `-Pservidor=`;
  - emulator: `C:\Android\emulator\emulator.exe -avd "pixel_7_-_api_36_0" -no-window`, then `adb install -r` and `adb exec-out screencap -p` for screenshots;
  - the palette and styles come from the EurekaBank mobile app, renamed `ap_*`.
- Payara 7 is at `C:\payara7` and the Android SDK at `C:\Android`.
- Deploy: start the domain with `/c/payara7/bin/asadmin.bat start-domain domain1`, then deploy with `/c/payara7/bin/asadmin.bat deploy --force=true <path to war>`. The base URL is `http://localhost:8080/WS_APIPAISES_JAVA_REST/api`.
- End-to-end tests: run `bash pruebas/pruebas_api.sh` against the deployed server. Add `API_EXTERNA=si` when a REST Countries key is configured. `pruebas/pruebas_api.http` is for manual requests.
- To change `restcountries.url` on Payara, `create-system-properties` rejects values that contain `:`. Create the property first, then run `asadmin set "servers.server.server.system-property.restcountries.url.value=<url>"`.
- Inside Payara, MySQL SSL fails (`unknown_ca`), so the default JDBC URL uses `sslMode=DISABLED&allowPublicKeyRetrieval=true`.

Server API contract: `05. DOCUMENTACION/PLAN_DESARROLLO.md` §3. Every response is `{estado, mensaje, datos}`, and the token goes raw in the `Authorization` header. ECUD messages are in `modelos/Mensajes.java`. Reuse them verbatim in the clients.

**REST Countries v3.1 is dead.** Every call returns a "deprecated" error. The server uses **v5** instead: base `https://api.restcountries.com/countries/v5`, an API key sent as `Authorization: Bearer`, paginated `data.objects`, `capitals[]` objects and `currencies[]` arrays. Only `servicios/ConsumoAPIPaises` knows the external API. The key goes in `src/main/resources/config.properties`, which is gitignored; copy `config.example.properties` to create it. A system property or environment variable overrides the file.

Folder map:

| Folder | Deliverable |
|---|---|
| `01. UML/` | ERS (IEEE 830 requirements), ECUD (detailed use cases), PowerDesigner diagrams |
| `02. MER/` | Entity-relationship model |
| `03. BDD/` | MySQL scripts (the ERS recommends one creation script `APIPaisesDB.sql` and one seed script `init.sql`) |
| `04. SERVIDOR/` | Central RESTful server |
| `04. CLICON/`, `04. CLIESC/`, `04. CLIWEB/`, `04. CLIMOV/` | Console, desktop, web, and mobile (Android 7.0 / API 24+) clients |
| `05. DOCUMENTACION/` | Final documentation |

## Source-of-truth documents

- `Taller Práctico Consumo de APIs Públicas en Java con MySQL aplicando el Patrón MVC.docx`: the professor's assignment. It contains a minimal single-app Swing example (`modelo`/`controlador`/`vista` packages, `ConsumoAPI` with `HttpURLConnection`, `PaisDAO`, Gson 2.10.1, mysql-connector-j 8.3.0, Maven, JDK 17+) plus the activities: lookup by name (`/v3.1/name/{nombre}`), full CRUD, JTable listing, region filter, statistics reports.
- `01. UML/01. ERS/ERS_JAVA_MYSQL_APIPAISES_SOSA.docx` and `01. UML/02. ECUD/ECUD_JAVA_MYSQL_APIPAISES_SOSA.docx`: the student's own design. It expands the assignment into an SOA system. **Follow these when implementing.**
- `JAVA_MYSQL_APIPAISES_SOSA.sws`: PowerDesigner 16.5 workspace that references the `.prj`/`.oom` diagram models (use case, activity, sequence, class, MVC architecture). These files are generated by the tool, so don't hand-edit them. Each `.oob` is the backup copy of its `.oom`.

The `.docx` files are binary. Read them by unzipping `word/document.xml`, for example with Python `zipfile`. Set `PYTHONIOENCODING=utf-8` because the Windows console is cp1252 and chokes on Spanish characters and arrows.

## Target architecture (from ERS/ECUD)

- **SOA + MVC everywhere.** One RESTful server (Java 21, deployed on **Payara Server**, built in NetBeans) is the only component that talks to REST Countries (Java `HttpClient`, 15 s timeout) and to MySQL (through DAO classes). The four clients only call the server and never touch the DB or the external API. The server and each client are internally MVC.
- **Database `APIPaisesDB`**: tables `Usuarios` (password stored as SHA-256 hash; users are seeded by script, with no user-management UI), `Regiones` (catalog), and `Paises`. The country name (max 100 chars) is unique and acts as the key.
- **Country field mapping from REST Countries v3.1** (ERS §9.1.5):
  - nombre: `translations.spa.common`, falling back to `name.common`
  - capital: first entry of `capital[]`, or "Sin capital"
  - region: `region`, translated to América/Europa/Asia/África/Oceanía/Antártida and linked to `Regiones`
  - poblacion: `population` as BIGINT, ≥ 0
  - moneda: first currency formatted `"<name> (<ISO code>)"`, or "Sin moneda"

  Import requests only the needed fields, loads existing countries once, and saves inside a single transaction (target: about 250 countries in under 30 s).
- **Auth**: `POST /api/Login/login` checks the SHA-256 of the password and returns a token: SHA-256 of user + time + random value, kept in server memory. Every other request must carry it in the `Authorization` header. `POST /api/Login/logout` removes it.
- **Endpoints** (base `http://<servidor>:<puerto>/api`):
  - `GET/POST /Paises`
  - `PUT/DELETE /Paises/{nombre}`
  - `GET /Paises/buscar?nombre=&capital=&region=`
  - `POST /Paises/importar`
  - `GET /Paises/externo/{nombre}`: live lookup in REST Countries
  - `GET /Reportes`: count per region, most populous country, average population per region
- **Uniform response envelope** for every operation: `{ estado: "OK"|"ERROR", mensaje, datos }`. On error, `datos` is empty. HTTP codes are mapped by case:

  | Code | Case |
  |---|---|
  | 200 / 201 | success |
  | 400 | invalid data |
  | 401 | invalid token |
  | 404 | not found |
  | 409 | duplicate country name |
  | 502 | external API failure |
  | 500 | unexpected error |

  The exact user-facing message strings are listed in the ECUD conventions section ("Ingreso de sesión exitoso", "Credenciales incorrectas", "Ya existe un país con ese nombre", etc.). Reuse them verbatim.
- **Client-side validation** before sending:
  - user and password are not empty
  - the name is not empty and is at most 100 chars
  - the population is an integer ≥ 0
  - the region is picked from the list

  Console clients print errors prefixed with `ERROR:`.

## Known inconsistencies in the docs

The ERS/ECUD were adapted from an earlier .NET/SQL Server version of the same workshop, and leftovers remain. Do not implement them, and ask before resolving any conflict:

- References to SQL Server, EF Core, `SaveChangesAsync`, `NVARCHAR`, `appsettings.json`, and a ".NET con SQL Server" workshop title. The real stack is Java + MySQL.
- Universidades use cases (ECUD 10–11) and an "área" field. These are out of the Java assignment's scope.
- Tool versions differ between documents: the assignment says JDK 17+ and NetBeans 21+, while the ERS says Java 21 and "NetBeans 31".
- The assignment's sample schema (`api_paises.pais` with an auto-increment `id`) differs from the ERS design (`APIPaisesDB` with `Paises`/`Regiones`/`Usuarios` and the name as key). The ERS design is the intended one.
