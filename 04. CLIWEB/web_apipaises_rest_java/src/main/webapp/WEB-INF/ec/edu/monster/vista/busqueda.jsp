<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%--
  ECUD5 Buscar Países. La muestra BusquedaController (GET /busqueda).
  Atributos: nombre, capital, region, regiones, resultados, total, mensaje, error.
  Al cambiar la región el formulario se envía solo (ECUD6); sin JavaScript se usa «Buscar».
--%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="activeNav" value="busqueda"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Buscar países | APIPaises</title>
    <link rel="stylesheet" href="${ctx}/css/apipaises.css">
    <link rel="stylesheet" href="${ctx}/css/menu.css">
    <link rel="stylesheet" href="${ctx}/css/operacion.css">
    <link rel="stylesheet" href="${ctx}/css/paises.css">
</head>
<body>
<%@ include file="partials/header.jspf" %>

<div class="app-body">
    <%@ include file="partials/sidebar.jspf" %>

    <main class="content">
        <div class="content__title">
            <h1 class="text-heading-xl">Buscar países</h1>
            <p class="text-body">Busque por nombre, capital o región</p>
        </div>

        <form class="op-card op-card--wide" method="get" action="${ctx}/busqueda" data-procesando>
            <input type="hidden" name="buscar" value="1">
            <div class="search-grid">
                <div class="field">
                    <label class="field__label" for="nombre">Nombre</label>
                    <input class="field__input" type="text" id="nombre" name="nombre" maxlength="100"
                           value="${fn:escapeXml(nombre)}" placeholder="Ej.: ecua" autocomplete="off">
                </div>
                <div class="field">
                    <label class="field__label" for="capital">Capital</label>
                    <input class="field__input" type="text" id="capital" name="capital" maxlength="100"
                           value="${fn:escapeXml(capital)}" placeholder="Ej.: quito" autocomplete="off">
                </div>
                <div class="field">
                    <label class="field__label" for="region">Región</label>
                    <select class="field__input field__select" id="region" name="region" data-autoenviar>
                        <option value=""<c:if test="${empty region}"> selected</c:if>>Todas</option>
                        <c:forEach var="r" items="${regiones}">
                            <option value="${fn:escapeXml(r)}"<c:if test="${region eq r}"> selected</c:if>><c:out value="${r}"/></option>
                        </c:forEach>
                    </select>
                </div>
                <button class="btn btn--primary" type="submit">Buscar</button>
            </div>
            <span class="procesando" role="status">Procesando...</span>
        </form>

        <c:if test="${not empty error}">
            <p class="alert alert--error" role="alert"><c:out value="${error}"/></p>
        </c:if>

        <c:if test="${not empty total}">
            <section class="op-card op-card--wide" aria-label="Resultados de la búsqueda">
                <div class="toolbar">
                    <p class="total">
                        <c:choose>
                            <c:when test="${empty resultados}">No se encontraron países con esos criterios</c:when>
                            <c:otherwise><c:out value="${total}"/></c:otherwise>
                        </c:choose>
                    </p>
                    <span class="tag tag--si"><c:out value="${mensaje}"/></span>
                </div>
                <c:if test="${not empty resultados}">
                    <div class="history__scroll">
                        <table class="history__table">
                            <thead>
                            <tr>
                                <th scope="col">Nombre</th>
                                <th scope="col">Capital</th>
                                <th scope="col">Región</th>
                                <th scope="col" class="history__amount">Población</th>
                                <th scope="col">Moneda</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="p" items="${resultados}">
                                <tr>
                                    <td><c:out value="${p.nombre}"/></td>
                                    <td><c:out value="${p.capital}"/></td>
                                    <td><c:out value="${p.region}"/></td>
                                    <td class="history__amount"><c:out value="${p.poblacionFormateada}"/></td>
                                    <td><c:out value="${p.moneda}"/></td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:if>
            </section>
        </c:if>
    </main>
</div>
<script src="${ctx}/js/procesando.js"></script>
</body>
</html>
