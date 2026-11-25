<%-- session="false": aquí no se crea sesión, así la redirección nunca lleva ;jsessionid=... --%>
<%@ page pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Página de bienvenida: la aplicación empieza en el inicio de sesión. --%>
<c:redirect url="/login"/>
