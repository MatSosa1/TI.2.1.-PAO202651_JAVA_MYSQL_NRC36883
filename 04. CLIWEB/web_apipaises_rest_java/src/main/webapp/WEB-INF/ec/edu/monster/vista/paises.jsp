<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%--
  ECUD4 Gestionar Países y ECUD6 Filtrar. La muestra PaisesController (GET/POST /paises).
  Atributos:
    paises, total, region (filtro activo, "" = todas), regiones, mensaje / error (flash)
    confirmarEliminar -> nombre del país a eliminar: se muestra la confirmación
  Todo valor dinámico se escapa (c:out / fn:escapeXml).
--%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="activeNav" value="paises"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Países | APIPaises</title>
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
            <h1 class="text-heading-xl">Países</h1>
            <p class="text-body">Países guardados en la base de datos local</p>
        </div>

        <c:url var="urlVolver" value="/paises"><c:if test="${not empty region}"><c:param name="region" value="${region}"/></c:if></c:url>

        <c:choose>
            <%-- ===== ECUD4 Eliminar pasos 2 y 3: confirmación (sin JavaScript) ===== --%>
            <c:when test="${not empty confirmarEliminar}">
                <section class="op-dialog" aria-labelledby="confirmar-title">
                    <h2 id="confirmar-title" class="op-dialog__title">Eliminar país</h2>
                    <p class="op-dialog__text">¿Eliminar el país <strong><c:out value="${confirmarEliminar}"/></strong>?</p>
                    <form class="op-form" method="post" action="${ctx}/paises" data-procesando>
                        <%@ include file="partials/csrf.jspf" %>
                        <input type="hidden" name="accion" value="eliminar">
                        <input type="hidden" name="paso" value="confirmar">
                        <input type="hidden" name="nombre" value="${fn:escapeXml(confirmarEliminar)}">
                        <input type="hidden" name="region" value="${fn:escapeXml(region)}">
                        <div class="op-actions">
                            <span class="procesando" role="status">Procesando...</span>
                            <a class="btn btn--secondary" href="${urlVolver}">Volver</a>
                            <button class="btn btn--danger" type="submit">Eliminar</button>
                        </div>
                    </form>
                </section>
            </c:when>

            <%-- ===== Tabla de países ===== --%>
            <c:otherwise>
                <c:if test="${not empty mensaje}">
                    <p class="alert alert--success" role="status"><c:out value="${mensaje}"/></p>
                </c:if>
                <c:if test="${not empty error}">
                    <p class="alert alert--error" role="alert"><c:out value="${error}"/></p>
                </c:if>

                <section class="op-card op-card--wide" aria-label="Países guardados">
                    <div class="toolbar">
                        <p class="total">
                            <c:choose>
                                <c:when test="${empty paises and empty region}">No hay países registrados. Use «Consultar en la API» para guardarlos</c:when>
                                <c:when test="${empty paises}">No se encontraron países en esa región</c:when>
                                <c:otherwise><c:out value="${total}"/></c:otherwise>
                            </c:choose>
                        </p>
                        <div class="row-actions">
                            <span class="procesando" role="status">Procesando...</span>
                            <a class="btn btn--primary" href="${ctx}/paises/nuevo">Nuevo</a>
                        </div>
                    </div>

                    <c:if test="${not empty paises}">
                        <div class="history__scroll">
                            <table class="history__table">
                                <thead>
                                <tr>
                                    <th scope="col">Nombre</th>
                                    <th scope="col">Capital</th>
                                    <th scope="col">Región</th>
                                    <th scope="col" class="history__amount">Población</th>
                                    <th scope="col">Moneda</th>
                                    <th scope="col" class="history__amount">Acciones</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach var="p" items="${paises}">
                                    <tr>
                                        <td><c:out value="${p.nombre}"/></td>
                                        <td><c:out value="${p.capital}"/></td>
                                        <td><c:out value="${p.region}"/></td>
                                        <td class="history__amount"><c:out value="${p.poblacionFormateada}"/></td>
                                        <td><c:out value="${p.moneda}"/></td>
                                        <td>
                                            <div class="row-actions">
                                                <%-- ECUD4 Actualizar: el servidor vuelve a consultar la API --%>
                                                <form class="inline-form" method="post" action="${ctx}/paises" data-procesando>
                                                    <%@ include file="partials/csrf.jspf" %>
                                                    <input type="hidden" name="accion" value="actualizar">
                                                    <input type="hidden" name="nombre" value="${fn:escapeXml(p.nombre)}">
                                                    <input type="hidden" name="region" value="${fn:escapeXml(region)}">
                                                    <button class="btn btn--secondary btn--small" type="submit">Actualizar</button>
                                                </form>
                                                <form class="inline-form" method="post" action="${ctx}/paises">
                                                    <%@ include file="partials/csrf.jspf" %>
                                                    <input type="hidden" name="accion" value="eliminar">
                                                    <input type="hidden" name="paso" value="revisar">
                                                    <input type="hidden" name="nombre" value="${fn:escapeXml(p.nombre)}">
                                                    <input type="hidden" name="region" value="${fn:escapeXml(region)}">
                                                    <button class="btn btn--secondary btn--small" type="submit">Eliminar</button>
                                                </form>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:if>

                    <%-- ECUD6: lista de filtros en la parte inferior (ERS 9.1.1); filtra al elegir --%>
                    <nav class="filtros" aria-label="Filtrar por región">
                        <span class="field__label">Filtrar por región</span>
                        <div class="filtros__lista">
                            <a class="chip${empty region ? ' chip--active' : ''}" href="${ctx}/paises"<c:if test="${empty region}"> aria-current="true"</c:if>>Todas</a>
                            <c:forEach var="r" items="${regiones}">
                                <c:url var="urlRegion" value="/paises"><c:param name="region" value="${r}"/></c:url>
                                <a class="chip${region eq r ? ' chip--active' : ''}" href="${urlRegion}"<c:if test="${region eq r}"> aria-current="true"</c:if>><c:out value="${r}"/></a>
                            </c:forEach>
                        </div>
                    </nav>
                </section>
            </c:otherwise>
        </c:choose>
    </main>
</div>
<script src="${ctx}/js/procesando.js"></script>
</body>
</html>
