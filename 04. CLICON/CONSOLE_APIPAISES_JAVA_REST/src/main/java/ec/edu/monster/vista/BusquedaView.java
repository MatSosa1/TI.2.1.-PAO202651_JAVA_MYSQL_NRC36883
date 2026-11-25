package ec.edu.monster.vista;

import ec.edu.monster.controlador.PaisController;
import ec.edu.monster.controlador.ValidadorDatos;
import ec.edu.monster.modelos.dto.Respuesta;
import ec.edu.monster.modelos.dto.ResultadoBusqueda;
import ec.edu.monster.vista.utils.Formato;
import ec.edu.monster.vista.utils.MessagesUtils;
import ec.edu.monster.vista.utils.Tablas;

/**
 * ECUD5 Buscar Países: nombre y capital por coincidencia parcial y región de una lista
 * (Todas, América, Europa, Asia, África, Oceanía, Antártida). Los criterios se combinan.
 */
public class BusquedaView {

  private static final PaisController CONTROLLER = new PaisController();

  public static void show() {
    MessagesUtils.printTitle("BUSCAR PAISES");
    String nombre = MessagesUtils.readText("Nombre (Enter para omitir)");
    if (!valido(nombre))
      return;
    String capital = MessagesUtils.readText("Capital (Enter para omitir)");
    if (!valido(capital))
      return;
    String region = Tablas.elegirRegion();
    if (region == null)
      return;

    MessagesUtils.printMessage("Procesando...");
    Respuesta<ResultadoBusqueda> respuesta = CONTROLLER.buscar(nombre, capital, region);
    if (MenuView.revisarSesion(respuesta))
      return;
    if (!respuesta.esOk() || respuesta.getDatos() == null) {
      MessagesUtils.printError(respuesta.getMensaje());
      return;
    }

    ResultadoBusqueda resultado = respuesta.getDatos();
    if (resultado.paises() == null || resultado.paises().isEmpty()) {
      MessagesUtils.printMessage("No se encontraron países con esos criterios");
      return;
    }
    MessagesUtils.printMessage(respuesta.getMensaje());
    Tablas.paises(resultado.paises(), false);
    System.out.println(Formato.total(resultado.total()));
  }

  /** Un criterio vacío se omite; con texto no puede superar 100 caracteres. */
  private static boolean valido(String criterio) {
    if (criterio.length() > ValidadorDatos.MAX_NOMBRE) {
      MessagesUtils.printError(ValidadorDatos.NOMBRE_MUY_LARGO);
      return false;
    }
    return true;
  }
}
