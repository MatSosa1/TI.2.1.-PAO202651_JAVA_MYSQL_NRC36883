<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%--
  Inicio de sesión (ECUD1). La muestra LoginController (GET/POST /login).
  Atributos: error (mensaje tras un intento fallido o sesión vencida), usuario (lo escrito;
  la clave nunca se devuelve). Mismo diseño que EurekaBank, sin selector de perfil.
--%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Iniciar sesión | APIPaises</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/apipaises.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/login.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/paises.css">
</head>
<body>
<main class="login-page">
    <section class="login-card" aria-labelledby="login-title">

        <div class="login-card__top">
            <span class="logo"><span class="logo__mark">AP</span>APIPaises</span>
            <%-- Sello del servicio: este cliente consume el servicio REST desarrollado en Java --%>
            <span class="badge" title="Servicio consumido: REST / Java">
                <span class="badge__label">Servicio</span>
                <span class="badge__protocol--rest">REST</span>
                <span class="badge__platform--java">Java</span>
            </span>
        </div>

        <div class="login-card__title">
            <h1 id="login-title" class="text-heading-xl">Iniciar sesión</h1>
            <p class="text-body">Ingrese sus credenciales para continuar</p>
        </div>

        <c:if test="${not empty error}">
            <p class="alert alert--error" role="alert"><c:out value="${error}"/></p>
        </c:if>

        <form class="login-form" method="post" action="${pageContext.request.contextPath}/login" data-procesando>
            <%@ include file="partials/csrf.jspf" %>
            <div class="field">
                <label class="field__label" for="usuario">Usuario</label>
                <input class="field__input" type="text" id="usuario" name="usuario"
                       value="${fn:escapeXml(usuario)}" placeholder="Ingrese su usuario"
                       autocomplete="username" required autofocus>
            </div>

            <div class="field">
                <label class="field__label" for="clave">Clave</label>
                <input class="field__input" type="password" id="clave" name="clave"
                       placeholder="Ingrese su clave" autocomplete="current-password" required>
            </div>

            <span class="procesando" role="status">Procesando...</span>
            <button class="btn btn--primary btn--block" type="submit">Ingresar</button>
        </form>
    </section>
</main>
<script src="${pageContext.request.contextPath}/js/procesando.js"></script>
</body>
</html>
