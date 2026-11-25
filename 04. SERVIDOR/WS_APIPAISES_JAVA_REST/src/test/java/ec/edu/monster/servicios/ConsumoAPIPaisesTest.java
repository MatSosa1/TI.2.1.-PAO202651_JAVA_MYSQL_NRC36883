package ec.edu.monster.servicios;

import static ec.edu.monster.servicios.RestCountriesFalso.ALEMANIA;
import static ec.edu.monster.servicios.RestCountriesFalso.AUSTRIA;
import static ec.edu.monster.servicios.RestCountriesFalso.ECUADOR;
import static ec.edu.monster.servicios.RestCountriesFalso.capital;
import static ec.edu.monster.servicios.RestCountriesFalso.moneda;
import static ec.edu.monster.servicios.RestCountriesFalso.pagina;
import static ec.edu.monster.servicios.RestCountriesFalso.pais;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.modelos.Mensajes;
import ec.edu.monster.modelos.dto.PaisDTO;
import ec.edu.monster.servicios.RestCountriesFalso.Peticion;
import ec.edu.monster.servicios.RestCountriesFalso.Respuesta;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ConsumoAPIPaisesTest {

    private static final String KEY = "rc_prueba";

    // ================================================================== Conversión del JSON (ECUD9, pasos 5 a 8)

    @Nested
    class Mapeo {

        private final ConsumoAPIPaises api = new ConsumoAPIPaises("http://no-se-usa", KEY, 15);

        @Test
        void convierteUnPaisCompleto() throws ApiExternaException {
            PaisDTO ecuador = api.mapear(pagina(false, ECUADOR)).get(0);

            assertEquals("Ecuador", ecuador.getNombre());
            assertEquals("Quito", ecuador.getCapital());
            assertEquals("América", ecuador.getRegion());
            assertEquals(18001000L, ecuador.getPoblacion());
            assertEquals("United States dollar (USD)", ecuador.getMoneda());
            assertFalse(ecuador.isGuardado());
        }

        @Test
        void usaElNombreEnEspanol() throws ApiExternaException {
            assertEquals("Alemania", api.mapear(pagina(false, ALEMANIA)).get(0).getNombre());
        }

        @Test
        void sinTraduccionUsaElNombreComun() throws ApiExternaException {
            String json = pagina(false, pais("Narnia", null, "[]", "\"Europe\"", "5", "[]"));
            assertEquals("Narnia", api.mapear(json).get(0).getNombre());
        }

        @Test
        void sinCapitalNiMonedaUsaLosTextosDelEcud() throws ApiExternaException {
            String antartida = pais("Antarctica", "Antártida", "[]", "\"Antarctic\"", "1000", "[]");
            PaisDTO pais = api.mapear(pagina(false, antartida)).get(0);

            assertEquals("Sin capital", pais.getCapital());
            assertEquals("Sin moneda", pais.getMoneda());
            assertEquals("Antártida", pais.getRegion());
        }

        @Test
        void tomaLaCapitalPrincipalAunqueNoSeaLaPrimera() throws ApiExternaException {
            String sudafrica = pais("South Africa", "Sudáfrica",
                    "[" + capital("Bloemfontein", false) + "," + capital("Pretoria", true) + "]",
                    "\"Africa\"", "60000000", "[" + moneda("ZAR", "South African rand") + "]");
            assertEquals("Pretoria", api.mapear(pagina(false, sudafrica)).get(0).getCapital());
        }

        @Test
        void sinCapitalPrincipalTomaLaPrimera() throws ApiExternaException {
            String bolivia = pais("Bolivia", "Bolivia",
                    "[" + capital("Sucre", false) + "," + capital("La Paz", false) + "]",
                    "\"Americas\"", "12000000", "[" + moneda("BOB", "Bolivian boliviano") + "]");
            assertEquals("Sucre", api.mapear(pagina(false, bolivia)).get(0).getCapital());
        }

        @Test
        void tomaLaPrimeraMoneda() throws ApiExternaException {
            String panama = pais("Panama", "Panamá", "[" + capital("Panama City", true) + "]", "\"Americas\"",
                    "4000000", "[" + moneda("PAB", "Panamanian balboa") + "," + moneda("USD", "United States dollar") + "]");
            assertEquals("Panamanian balboa (PAB)", api.mapear(pagina(false, panama)).get(0).getMoneda());
        }

        @Test
        void descartaRegionDesconocidaYSigueConLosDemas() throws ApiExternaException {
            String raro = pais("Atlantis", "Atlántida", "[]", "\"Underwater\"", "1", "[]");
            List<PaisDTO> paises = api.mapear(pagina(false, raro, ECUADOR));

            assertEquals(1, paises.size(), "ECUD9, alterno 4");
            assertEquals("Ecuador", paises.get(0).getNombre());
        }

        @Test
        void poblacionAusenteONegativaQuedaEnCero() throws ApiExternaException {
            String sinPoblacion = pais("Nowhere", "Ningunaparte", "[]", "\"Asia\"", "null", "[]");
            String negativa = pais("Minus", "Menos", "[]", "\"Asia\"", "-5", "[]");

            List<PaisDTO> paises = api.mapear(pagina(false, sinPoblacion, negativa));
            assertEquals(0L, paises.get(0).getPoblacion());
            assertEquals(0L, paises.get(1).getPoblacion());
        }

        @Test
        void recortaNombresDeMasDeCienCaracteres() throws ApiExternaException {
            String largo = pais("Largo", "L".repeat(140), "[]", "\"Asia\"", "1", "[]");
            assertEquals(100, api.mapear(pagina(false, largo)).get(0).getNombre().length());
        }

        @Test
        void jsonQueNoEsDeLaV5EsRespuestaNoValida() {
            // Respuesta actual de la v3.1 dada de baja
            String legado = "{\"success\":false,\"data\":null,\"errors\":[{\"message\":\"This API version has been deprecated.\"}]}";
            for (String json : List.of(legado, "[1,2,3]", "{no es json", "")) {
                ApiExternaException error = assertThrows(ApiExternaException.class, () -> api.mapear(json));
                assertEquals(ApiExternaException.Tipo.RESPUESTA_INVALIDA, error.getTipo(), json);
                assertEquals(Mensajes.API_RESPUESTA_INVALIDA, error.getMensajeUsuario());
                assertEquals(502, error.getCodigoHttp());
            }
        }

        @Test
        void traduceLasSeisRegiones() {
            assertEquals("América", ConsumoAPIPaises.traducirRegion("Americas"));
            assertEquals("Europa", ConsumoAPIPaises.traducirRegion("Europe"));
            assertEquals("Asia", ConsumoAPIPaises.traducirRegion("Asia"));
            assertEquals("África", ConsumoAPIPaises.traducirRegion("Africa"));
            assertEquals("Oceanía", ConsumoAPIPaises.traducirRegion("Oceania"));
            assertEquals("Antártida", ConsumoAPIPaises.traducirRegion("Antarctic"));
            assertNull(ConsumoAPIPaises.traducirRegion("Mars"));
            assertNull(ConsumoAPIPaises.traducirRegion(null));
        }

        @Test
        void seleccionaElPaisElegidoSinDistinguirTildes() {
            List<PaisDTO> paises = List.of(new PaisDTO("Perú", "Lima", "América", 1, "Sol (PEN)"),
                    new PaisDTO("Paraguay", "Asunción", "América", 1, "Guaraní (PYG)"));

            assertEquals("Perú", ConsumoAPIPaises.seleccionar(paises, "peru").getNombre());
            assertNull(ConsumoAPIPaises.seleccionar(paises, "Per"));
        }
    }

    // ================================================================== Peticiones HTTP (ECUD9, pasos 2 a 4)

    @Test
    void obtenerTodosRecorreLasPaginasYEnviaLaApiKey() throws Exception {
        try (RestCountriesFalso falso = new RestCountriesFalso(peticion -> "0".equals(peticion.parametros().get("offset"))
                ? Respuesta.ok(pagina(true, ECUADOR, ALEMANIA))
                : Respuesta.ok(pagina(false, AUSTRIA)))) {

            List<PaisDTO> paises = new ConsumoAPIPaises(falso.urlBase(), KEY, 5).obtenerTodos();

            assertEquals(List.of("Ecuador", "Alemania", "Austria"), paises.stream().map(PaisDTO::getNombre).toList());
            assertEquals(2, falso.recibidas().size());
            Peticion primera = falso.recibidas().get(0);
            assertEquals("/countries/v5", primera.ruta());
            assertEquals("Bearer " + KEY, primera.authorization());
            assertEquals("100", primera.parametros().get("limit"));
            assertEquals(ConsumoAPIPaises.CAMPOS, primera.parametros().get("response_fields"));
            assertEquals("2", falso.recibidas().get(1).parametros().get("offset"));
        }
    }

    @Test
    void obtenerPorNombreUsaElAgregadoName() throws Exception {
        try (RestCountriesFalso falso = new RestCountriesFalso(peticion -> Respuesta.ok(pagina(false, ALEMANIA)))) {
            List<PaisDTO> paises = new ConsumoAPIPaises(falso.urlBase(), KEY, 5).obtenerPorNombre("germany");

            assertEquals("Alemania", paises.get(0).getNombre());
            assertEquals(1, falso.recibidas().size());
            assertEquals("/countries/v5/name", falso.recibidas().get(0).ruta());
            assertEquals("germany", falso.recibidas().get(0).parametros().get("q"));
        }
    }

    @Test
    void obtenerPorNombreEnEspanolBuscaEnLasTraducciones() throws Exception {
        try (RestCountriesFalso falso = new RestCountriesFalso(peticion -> peticion.ruta().endsWith("/name")
                ? Respuesta.ok(pagina(false))
                // names.translations encuentra «alemania» en cualquier idioma; Austria no debe quedar
                : Respuesta.ok(pagina(false, ALEMANIA, AUSTRIA)))) {

            List<PaisDTO> paises = new ConsumoAPIPaises(falso.urlBase(), KEY, 5).obtenerPorNombre("alemania");

            assertEquals(List.of("Alemania"), paises.stream().map(PaisDTO::getNombre).toList());
            assertEquals("/countries/v5/names.translations", falso.recibidas().get(1).ruta());
        }
    }

    @Test
    void obtenerPaisTomaElElegidoDeLaConsulta() throws Exception {
        try (RestCountriesFalso falso = new RestCountriesFalso(peticion -> Respuesta.ok(pagina(false, ALEMANIA, AUSTRIA)))) {
            PaisDTO elegido = new ConsumoAPIPaises(falso.urlBase(), KEY, 5).obtenerPais("a", "austria");

            assertEquals("Austria", elegido.getNombre());
            assertEquals(1, falso.recibidas().size());
        }
    }

    @Test
    void obtenerPaisBuscaPorElNombreEnEspanolSiLaConsultaNoLoTrae() throws Exception {
        // Actualizar «Alemania»: el agregado name devuelve otros países, la traducción sí lo trae
        try (RestCountriesFalso falso = new RestCountriesFalso(peticion -> peticion.ruta().endsWith("/name")
                ? Respuesta.ok(pagina(false, AUSTRIA))
                : Respuesta.ok(pagina(false, ALEMANIA)))) {

            PaisDTO elegido = new ConsumoAPIPaises(falso.urlBase(), KEY, 5).obtenerPais(null, "Alemania");

            assertEquals("Alemania", elegido.getNombre());
            assertEquals("alemania", falso.recibidas().get(0).parametros().get("q").toLowerCase());
            assertEquals("/countries/v5/names.translations", falso.recibidas().get(1).ruta());
        }
    }

    @Test
    void obtenerPaisSinElElegidoEs404() throws Exception {
        try (RestCountriesFalso falso = new RestCountriesFalso(peticion -> Respuesta.ok(pagina(false, AUSTRIA)))) {
            ApiExternaException error = assertThrows(ApiExternaException.class,
                    () -> new ConsumoAPIPaises(falso.urlBase(), KEY, 5).obtenerPais("aus", "Australia"));
            assertEquals(ApiExternaException.Tipo.SIN_COINCIDENCIAS, error.getTipo());
        }
    }

    @Test
    void codificaEspaciosYTildesEnLaConsulta() throws Exception {
        try (RestCountriesFalso falso = new RestCountriesFalso(peticion -> Respuesta.ok(pagina(false, ECUADOR)))) {
            new ConsumoAPIPaises(falso.urlBase(), KEY, 5).obtenerPorNombre("  Perú del Sur ");
            assertEquals("Perú del Sur", falso.recibidas().get(0).parametros().get("q"));
        }
    }

    @Test
    void sinCoincidenciasEs404() throws Exception {
        try (RestCountriesFalso falso = new RestCountriesFalso(peticion -> Respuesta.ok(pagina(false)))) {
            ApiExternaException error = assertThrows(ApiExternaException.class,
                    () -> new ConsumoAPIPaises(falso.urlBase(), KEY, 5).obtenerPorNombre("zzz"));

            assertEquals(ApiExternaException.Tipo.SIN_COINCIDENCIAS, error.getTipo());
            assertEquals(Mensajes.SIN_COINCIDENCIAS_API, error.getMensajeUsuario());
            assertEquals(404, error.getCodigoHttp());
        }
    }

    @Test
    void codigosDeErrorDeLaApiSonNoDisponible() throws Exception {
        // 401 API key, 403 cuota agotada, 429 límite de peticiones, 500 error de la API
        for (int codigo : new int[] {401, 403, 429, 500}) {
            String cuerpo = "{\"errors\":[{\"message\":\"Fallo " + codigo + "\",\"code\":\"prueba\"}]}";
            try (RestCountriesFalso falso = new RestCountriesFalso(peticion -> new Respuesta(codigo, cuerpo, 0))) {
                ApiExternaException error = assertThrows(ApiExternaException.class,
                        () -> new ConsumoAPIPaises(falso.urlBase(), KEY, 5).obtenerTodos());

                assertEquals(ApiExternaException.Tipo.NO_DISPONIBLE, error.getTipo(), "código " + codigo);
                assertEquals(Mensajes.API_NO_DISPONIBLE, error.getMensajeUsuario());
                assertEquals(502, error.getCodigoHttp());
                assertTrue(error.getMessage().contains(String.valueOf(codigo)), "el detalle va al log");
            }
        }
    }

    @Test
    void tiempoAgotadoEsNoDisponible() throws Exception {
        try (RestCountriesFalso falso = new RestCountriesFalso(peticion -> new Respuesta(200, pagina(false, ECUADOR), 3000))) {
            ApiExternaException error = assertThrows(ApiExternaException.class,
                    () -> new ConsumoAPIPaises(falso.urlBase(), KEY, 1).obtenerPorNombre("ecuador"));
            assertEquals(ApiExternaException.Tipo.NO_DISPONIBLE, error.getTipo());
        }
    }

    @Test
    void sinConexionEsNoDisponible() throws IOException {
        int puertoLibre;
        try (ServerSocket socket = new ServerSocket(0)) {
            puertoLibre = socket.getLocalPort();
        }
        ConsumoAPIPaises api = new ConsumoAPIPaises("http://127.0.0.1:" + puertoLibre + "/countries/v5", KEY, 2);

        ApiExternaException error = assertThrows(ApiExternaException.class, api::obtenerTodos);
        assertEquals(ApiExternaException.Tipo.NO_DISPONIBLE, error.getTipo());
        assertNotNull(error.getCause());
    }

    @Test
    void listaCompletaVaciaEsRespuestaNoValida() throws Exception {
        try (RestCountriesFalso falso = new RestCountriesFalso(peticion -> Respuesta.ok(pagina(false)))) {
            ApiExternaException error = assertThrows(ApiExternaException.class,
                    () -> new ConsumoAPIPaises(falso.urlBase(), KEY, 5).obtenerTodos());
            assertEquals(ApiExternaException.Tipo.RESPUESTA_INVALIDA, error.getTipo());
        }
    }
}
