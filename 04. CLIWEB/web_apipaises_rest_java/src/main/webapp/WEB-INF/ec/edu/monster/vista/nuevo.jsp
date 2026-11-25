<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%--
  ECUD4 Registrar País. La muestra NuevoPaisController (GET/POST /paises/nuevo).
  Atributos: consulta (texto consultado), resultados (países de la API), error.
  El usuario consulta, elige un país de los resultados y presiona «Registrar»;
  «Cancelar» vuelve a la tabla sin enviar nada (ECUD4 alterno 6).
--%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="activeNav" value="paises"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Nuevo país | APIPaises</title>
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
            <h1 class="text-heading-xl">Nuevo país</h1>
            <p class="text-body">Consulte el país en la API REST Countries y regístrelo</p>
        </div>

        <%-- Pasos 2 y 3: nombre a consultar --%>
        <form class="op-card op-card--wide" method="get" action="${ctx}/paises/nuevo" data-procesando>
            <div class="search-row">
                <div class="field">
                    <label class="field__label" for="consulta">Nombre del país</label>
                    <input class="field__input" type="text" id="consulta" name="consulta" maxlength="100"
                           value="${fn:escapeXml(consulta)}" placeholder="Ej.: ecuador" autocomplete="off" required autofocus>
                </div>
                <button class="btn btn--secondary" type="submit">Consultar</button>
            </div>
            <span class="procesando" role="status">Procesando...</span>
        </form>

        <c:if test="${not empty error}">
            <p class="alert alert--error" role="alert"><c:out value="${error}"/></p>
        </c:if>

        <%-- Pasos 4 y 5: resultados y «Registrar» --%>
        <c:if test="${not empty resultados}">
            <form class="op-card op-card--wide" method="post" action="${ctx}/paises/nuevo" data-procesando>
                <%@ include file="partials/csrf.jspf" %>
                <input type="hidden" name="consulta" value="${fn:escapeXml(consulta)}">
                <div class="history__scroll">
                    <table class="history__table">
                        <thead>
                        <tr>
                            <th scope="col"><span class="visually-hidden">Elegir</span></th>
                            <th scope="col">Nombre</th>
                            <th scope="col">Capital</th>
                            <th scope="col">Región</th>
                            <th scope="col" class="history__amount">Población</th>
                            <th scope="col">Moneda</th>
                            <th scope="col">Guardado</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="p" items="${resultados}" varStatus="fila">
                            <tr>
                                <td><input type="radio" name="nombre" id="pais-${fila.index}"
                                           value="${fn:escapeXml(p.nombre)}"<c:if test="${fila.first}"> checked</c:if>></td>
                                <td><label for="pais-${fila.index}"><c:out value="${p.nombre}"/></label></td>
                                <td><c:out value="${p.capital}"/></td>
                                <td><c:out value="${p.region}"/></td>
                                <td class="history__amount"><c:out value="${p.poblacionFormateada}"/></td>
                                <td><c:out value="${p.moneda}"/></td>
                                <td><span class="tag ${p.guardado ? 'tag--si' : 'tag--no'}">${p.guardado ? 'Sí' : 'No'}</span></td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
                <div class="op-actions">
                    <span class="procesando" role="status">Procesando...</span>
                    <a class="btn btn--secondary" href="${ctx}/paises">Cancelar</a>
                    <button class="btn btn--primary" type="submit">Registrar</button>
                </div>
            </form>
        </c:if>

        <c:if test="${empty resultados}">
            <div class="op-actions op-card--wide">
                <a class="btn btn--secondary" href="${ctx}/paises">Cancelar</a>
            </div>
        </c:if>
    </main>
</div>
<script src="${ctx}/js/procesando.js"></script>
</body>
</html>
