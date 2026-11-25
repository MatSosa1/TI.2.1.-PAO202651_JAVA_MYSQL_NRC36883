package ec.edu.monster.controladores;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ec.edu.monster.fakes.ServidorFalso;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * Navegador falso con Mockito: petición con parámetros, sesión con atributos reales (un mapa)
 * y registro de la vista o la redirección resultante.
 */
class NavegadorFalso {

    static final String CTX = "/web_apipaises_rest_java";

    final HttpServletRequest request = mock(HttpServletRequest.class);
    final HttpServletResponse response = mock(HttpServletResponse.class);
    final HttpSession session = mock(HttpSession.class);
    final Map<String, Object> atributosSesion = new HashMap<>();
    final Map<String, Object> atributos = new HashMap<>();
    String vista;
    String redireccion;
    boolean sesionInvalidada;

    NavegadorFalso() throws Exception {
        when(request.getContextPath()).thenReturn(CTX);
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession(true)).thenReturn(session);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute(anyString())).thenAnswer(i -> atributosSesion.get(i.getArgument(0, String.class)));
        doAnswer(i -> atributosSesion.put(i.getArgument(0), i.getArgument(1))).when(session).setAttribute(anyString(), org.mockito.ArgumentMatchers.any());
        doAnswer(i -> atributosSesion.remove(i.getArgument(0, String.class))).when(session).removeAttribute(anyString());
        doAnswer(i -> {
            sesionInvalidada = true;
            atributosSesion.clear();
            return null;
        }).when(session).invalidate();
        doAnswer(i -> atributos.put(i.getArgument(0), i.getArgument(1))).when(request).setAttribute(anyString(), org.mockito.ArgumentMatchers.any());
        when(request.getAttribute(anyString())).thenAnswer(i -> atributos.get(i.getArgument(0, String.class)));
        when(request.getRequestDispatcher(anyString())).thenAnswer(i -> {
            String ruta = i.getArgument(0, String.class);
            RequestDispatcher dispatcher = mock(RequestDispatcher.class);
            doAnswer(f -> {
                vista = ruta.substring(ruta.lastIndexOf('/') + 1);
                return null;
            }).when(dispatcher).forward(request, response);
            return dispatcher;
        });
        doAnswer(i -> {
            redireccion = i.getArgument(0, String.class);
            return null;
        }).when(response).sendRedirect(anyString());
    }

    NavegadorFalso conSesion() {
        atributosSesion.put("token", ServidorFalso.TOKEN);
        atributosSesion.put("nombre", "Administrador");
        return this;
    }

    NavegadorFalso sinSesion() {
        when(request.getSession(false)).thenReturn(null);
        return this;
    }

    NavegadorFalso parametro(String nombre, String valor) {
        when(request.getParameter(nombre)).thenReturn(valor);
        return this;
    }

    Object atributo(String nombre) {
        return atributos.get(nombre);
    }
}
