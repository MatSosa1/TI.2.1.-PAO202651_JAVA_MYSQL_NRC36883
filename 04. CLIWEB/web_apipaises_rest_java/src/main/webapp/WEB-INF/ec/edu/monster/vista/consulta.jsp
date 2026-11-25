<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%--
  ECUD3 Consultar País en la API, ECUD2 Importar e «Importar todos».
  La muestra ConsultaController (GET/POST /consulta). Atributos:
    nombre (texto consultado), resultados, mensaje / error
    confirmar = guardar       -> confirmación de ECUD2 (consulta, pais)
    confirmar = importarTodos -> confirmación de la importación total (consulta)
--%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="activeNav" value="consulta"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Consultar en la API | APIPaises</title>
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
            <h1 class="text-heading-xl">Consultar en la API</h1>
            <p class="text-body">Consulte un país en REST Countries y guárdelo en la base de datos local</p>
        </div>

        <c:url var="urlVolver" value="/consulta"><c:if test="${not empty consulta}"><c:param name="nombre" value="${consulta}"/></c:if></c:url>

        <c:choose>
            <%-- ===== ECUD2 pasos 2 y 3: confirmación ===== --%>
            <c:when test="${confirmar eq 'guardar'}">
                <section class="op-dialog" aria-labelledby="confirmar-title">
                    <h2 id="confirmar-title" class="op-dialog__title">Guardar país</h2>
                    <p class="op-dialog__text">¿Guardar el país <strong><c:out value="${pais}"/></strong> en la base de datos local?</p>
                    <form class="op-form" method="post" action="${ctx}/consulta" data-procesando>
                        <%@ include file="partials/csrf.jspf" %>
                        <input type="hidden" name="accion" value="guardar">
                        <input type="hidden" name="paso" value="confirmar">
                        <input type="hidden" name="consulta" value="${fn:escapeXml(consulta)}">
                        <input type="hidden" name="nombre" value="${fn:escapeXml(pais)}">
                        <div class="op-actions">
                            <span class="procesando" role="status">Procesando...</span>
                            <a class="btn btn--secondary" href="${urlVolver}">Volver</a>
                            <button class="btn btn--primary" type="submit">Guardar</button>
                        </div>
                    </form>
                </section>
            </c:when>

            <%-- ===== Importar todos: confirmación ===== --%>
            <c:when test="${confirmar eq 'importarTodos'}">
                <section class="op-dialog" aria-labelledby="confirmar-title">
                    <h2 id="confirmar-title" class="op-dialog__title">Importar todos los países</h2>
                    <p class="op-dialog__text">¿Importar todos los países desde la API REST Countries?
                        Los nuevos se registran y los que ya existen se actualizan.</p>
                    <form class="op-form" method="post" action="${ctx}/consulta" data-procesando>
                        <%@ include file="partials/csrf.jspf" %>
                        <input type="hidden" name="accion" value="importarTodos">
                        <input type="hidden" name="paso" value="confirmar">
                        <input type="hidden" name="consulta" value="${fn:escapeXml(consulta)}">
                        <div class="op-actions">
                            <span class="procesando" role="status">Procesando... (puede tardar unos segundos)</span>
                            <a class="btn btn--secondary" href="${urlVolver}">Volver</a>
                            <button class="btn btn--primary" type="submit">Importar</button>
                        </div>
                    </form>
                </section>
            </c:when>

            <%-- ===== Consulta y resultados ===== --%>
            <c:otherwise>
                <form class="op-card op-card--wide" method="get" action="${ctx}/consulta" data-procesando>
                    <div class="search-row">
                        <div class="field">
                            <label class="field__label" for="nombre">Nombre del país</label>
                            <input class="field__input" type="text" id="nombre" name="nombre" maxlength="100"
                                   value="${fn:escapeXml(nombre)}" placeholder="Ej.: ecuador" autocomplete="off" required autofocus>
                        </div>
                        <button class="btn btn--primary" type="submit">Consultar</button>
                    </div>
                    <span class="procesando" role="status">Procesando...</span>
                </form>

                <c:if test="${not empty mensaje}">
                    <p class="alert alert--success" role="status"><c:out value="${mensaje}"/></p>
                </c:if>
                <c:if test="${not empty error}">
                    <p class="alert alert--error" role="alert"><c:out value="${error}"/></p>
                </c:if>

                <section class="op-card op-card--wide" aria-label="Resultados de la API">
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
                                    <th scope="col">Guardado</th>
                                    <th scope="col" class="history__amount">Acción</th>
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
                                        <td><span class="tag ${p.guardado ? 'tag--si' : 'tag--no'}">${p.guardado ? 'Sí' : 'No'}</span></td>
                                        <td>
                                            <div class="row-actions">
                                                <form class="inline-form" method="post" action="${ctx}/consulta">
                                                    <%@ include file="partials/csrf.jspf" %>
                                                    <input type="hidden" name="accion" value="guardar">
                                                    <input type="hidden" name="paso" value="revisar">
                                                    <input type="hidden" name="consulta" value="${fn:escapeXml(nombre)}">
                                                    <input type="hidden" name="nombre" value="${fn:escapeXml(p.nombre)}">
                                                    <button class="btn btn--primary btn--small" type="submit">Guardar</button>
                                                </form>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:if>
                    <div class="toolbar">
                        <p class="total">Importe todos los países de REST Countries a la base local de una sola vez.</p>
                        <form class="inline-form" method="post" action="${ctx}/consulta">
                            <%@ include file="partials/csrf.jspf" %>
                            <input type="hidden" name="accion" value="importarTodos">
                            <input type="hidden" name="paso" value="revisar">
                            <input type="hidden" name="consulta" value="${fn:escapeXml(nombre)}">
                            <button class="btn btn--secondary" type="submit">Importar todos</button>
                        </form>
                    </div>
                </section>
            </c:otherwise>
        </c:choose>
    </main>
</div>
<script src="${ctx}/js/procesando.js"></script>
</body>
</html>
