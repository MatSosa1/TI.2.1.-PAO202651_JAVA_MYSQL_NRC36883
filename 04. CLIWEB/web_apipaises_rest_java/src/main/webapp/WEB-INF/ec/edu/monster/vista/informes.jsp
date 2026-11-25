<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
  ECUD7 Generar Informes Estadísticos. La muestra InformesController (GET /informes).
  Atributos: informe (tablas, país más poblado y barras del gráfico), mensaje, error, sinPaises.
  El gráfico es un SVG generado en el servidor: no usa librerías de JavaScript ni CDN.
--%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="activeNav" value="informes"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Informes | APIPaises</title>
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
            <h1 class="text-heading-xl">Informes</h1>
            <p class="text-body">Estadísticas de los países guardados en la base local</p>
        </div>

        <c:if test="${not empty error}">
            <p class="alert alert--error" role="alert"><c:out value="${error}"/></p>
            <c:if test="${sinPaises}">
                <%-- ECUD7 alterno 2: se sugiere guardar países --%>
                <p class="alert alert--info">Use <a href="${ctx}/consulta">«Consultar en la API»</a> para guardar países.</p>
            </c:if>
        </c:if>

        <c:if test="${not empty informe}">
            <p class="alert alert--success" role="status"><c:out value="${mensaje}"/></p>

            <div class="reports">
                <section class="op-card" aria-labelledby="conteo-title">
                    <h2 id="conteo-title" class="receipt__title">Países por región</h2>
                    <table class="history__table">
                        <thead>
                        <tr><th scope="col">Región</th><th scope="col" class="history__amount">Países</th></tr>
                        </thead>
                        <tbody>
                        <c:forEach var="conteo" items="${informe.paisesPorRegion}">
                            <tr><td><c:out value="${conteo.region}"/></td><td class="history__amount"><c:out value="${conteo.cantidad}"/></td></tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </section>

                <section class="op-card" aria-labelledby="promedio-title">
                    <h2 id="promedio-title" class="receipt__title">Promedio poblacional por región</h2>
                    <table class="history__table">
                        <thead>
                        <tr><th scope="col">Región</th><th scope="col" class="history__amount">Promedio de población</th></tr>
                        </thead>
                        <tbody>
                        <c:forEach var="p" items="${informe.promedioPorRegion}">
                            <tr><td><c:out value="${p.region}"/></td><td class="history__amount"><c:out value="${p.promedioFormateado}"/></td></tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </section>

                <section class="op-card stat" aria-labelledby="mayor-title">
                    <h2 id="mayor-title" class="receipt__title">País más poblado</h2>
                    <c:set var="mayor" value="${informe.paisMasPoblado}"/>
                    <p class="stat__name"><c:out value="${mayor.nombre}"/></p>
                    <p class="stat__detail"><c:out value="${mayor.poblacionFormateada}"/> habitantes · <c:out value="${mayor.region}"/></p>
                    <p class="stat__detail">Capital: <c:out value="${mayor.capital}"/></p>
                </section>

                <section class="op-card" aria-labelledby="grafico-title">
                    <h2 id="grafico-title" class="receipt__title">Cantidad de países por región</h2>
                    <svg class="chart" viewBox="0 0 ${informe.anchoGrafico} ${informe.altoGrafico}" role="img"
                         aria-label="Gráfico de barras: cantidad de países por región">
                        <line class="chart__axis" x1="0" y1="${informe.baseGrafico}" x2="${informe.anchoGrafico}" y2="${informe.baseGrafico}"/>
                        <c:forEach var="b" items="${informe.barras}">
                            <rect class="chart__bar" x="${b.x}" y="${b.y}" width="${b.ancho}" height="${b.alto}" rx="4">
                                <title><c:out value="${b.region}"/>: <c:out value="${b.cantidad}"/></title>
                            </rect>
                            <text class="chart__value" x="${b.centro}" y="${b.y - 8}"><c:out value="${b.cantidad}"/></text>
                            <text class="chart__label" x="${b.centro}" y="${informe.baseGrafico + 24}"><c:out value="${b.region}"/></text>
                        </c:forEach>
                    </svg>
                </section>
            </div>
        </c:if>
    </main>
</div>
</body>
</html>
