package ec.edu.monster;

import ec.edu.monster.modelos.http.ConfigHTTP;
import ec.edu.monster.vista.LoginView;
import ec.edu.monster.vista.MenuView;
import ec.edu.monster.vista.utils.MessagesUtils;

/**
 * Cliente de consola de APIPaises. Uso: java -jar CONSOLE_APIPAISES_JAVA_REST.jar [url del servidor].
 * La url por defecto está en cliente.properties.
 */
public class Main {

  public static void main(String[] args) {
    MessagesUtils.prepararConsola();
    if (args.length > 0)
      ConfigHTTP.setBaseUrl(args[0]);

    // Arte de inicio
    MessagesUtils.printMonster();
    MessagesUtils.esperar(2);

    // Inicio de sesión y menú; al cerrar sesión (o si el token deja de valer) se vuelve al inicio de sesión
    while (LoginView.userHasLogin()) {
      MenuView.show();
    }

    MessagesUtils.printMessage("Hasta Luego!");
    MessagesUtils.esperar(1);
  }
}
