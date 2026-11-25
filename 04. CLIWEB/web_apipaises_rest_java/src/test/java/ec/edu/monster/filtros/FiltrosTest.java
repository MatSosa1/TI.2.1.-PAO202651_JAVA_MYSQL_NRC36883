package ec.edu.monster.filtros;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

/** AuthFilter y CsrfFilter con peticiones falsas (Mockito). */
class FiltrosTest {

    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final HttpServletResponse response = mock(HttpServletResponse.class);
    private final HttpSession session = mock(HttpSession.class);
    private final FilterChain chain = mock(FilterChain.class);

    FiltrosTest() {
        when(request.getContextPath()).thenReturn("/web");
    }

    @Test
    void sinSesionVaAlLogin() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        new AuthFilter().doFilter(request, response, chain);
        verify(response).sendRedirect("/web/login");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void conTokenContinuaSinCache() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("token")).thenReturn("tok");
        new AuthFilter().doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
        verify(response).setHeader("Cache-Control", "no-store");
    }

    @Test
    void tokenEnBlancoNoCuenta() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("token")).thenReturn(" ");
        new AuthFilter().doFilter(request, response, chain);
        verify(response).sendRedirect("/web/login");
    }

    @Test
    void getDelLoginCreaElTokenCsrf() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getServletPath()).thenReturn("/login");
        when(request.getSession(true)).thenReturn(session);
        new CsrfFilter().doFilter(request, response, chain);
        verify(session).setAttribute(eq(CsrfFilter.SESSION_ATTRIBUTE), anyString());
        verify(chain).doFilter(request, response);
    }

    @Test
    void postConTokenCorrectoContinua() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getServletPath()).thenReturn("/paises");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(CsrfFilter.SESSION_ATTRIBUTE)).thenReturn("abc");
        when(request.getParameter(CsrfFilter.PARAMETER)).thenReturn("abc");
        new CsrfFilter().doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
    }

    @Test
    void postConTokenFalsoEs403() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getServletPath()).thenReturn("/paises");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(CsrfFilter.SESSION_ATTRIBUTE)).thenReturn("abc");
        when(request.getParameter(CsrfFilter.PARAMETER)).thenReturn("otro");
        new CsrfFilter().doFilter(request, response, chain);
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void loginVencidoMuestraUnFormularioNuevo() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getServletPath()).thenReturn("/login");
        when(request.getSession(false)).thenReturn(null);
        new CsrfFilter().doFilter(request, response, chain);
        verify(response).sendRedirect("/web/login");
        verify(chain, never()).doFilter(request, response);
    }
}
