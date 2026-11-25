<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
  Menú principal (inicio después del login). Lo muestra MenuController (GET /menu).
  Una tarjeta por opción del ECUD1 paso 7, con el mismo diseño que el menú de EurekaBank.
--%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="activeNav" value="inicio"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Menú | APIPaises</title>
    <link rel="stylesheet" href="${ctx}/css/apipaises.css">
    <link rel="stylesheet" href="${ctx}/css/menu.css">
</head>
<body>
<%@ include file="partials/header.jspf" %>

<div class="app-body">
    <%@ include file="partials/sidebar.jspf" %>

    <main class="content">
        <div class="content__title">
            <h1 class="text-heading-xl">Bienvenido, <c:out value="${sessionScope.nombre}"/></h1>
            <p class="text-body">Seleccione una opción</p>
        </div>

        <div class="cards">
            <a class="card" href="${ctx}/paises">
                <span class="card__icon" aria-hidden="true">P</span>
                <span class="card__title">Países</span>
                <span class="card__text">Liste, registre, actualice y elimine los países guardados</span>
            </a>
            <a class="card" href="${ctx}/consulta">
                <span class="card__icon" aria-hidden="true">C</span>
                <span class="card__title">Consultar en la API</span>
                <span class="card__text">Consulte un país en REST Countries y guárdelo en la base local</span>
            </a>
            <a class="card" href="${ctx}/busqueda">
                <span class="card__icon" aria-hidden="true">B</span>
                <span class="card__title">Buscar países</span>
                <span class="card__text">Busque por nombre, capital o región</span>
            </a>
            <a class="card" href="${ctx}/informes">
                <span class="card__icon" aria-hidden="true">I</span>
                <span class="card__title">Informes</span>
                <span class="card__text">Países por región, país más poblado y promedio poblacional</span>
            </a>
        </div>

        <p class="alert alert--info">Este cliente consume el servicio REST desarrollado en Java.</p>
    </main>
</div>
</body>
</html>
