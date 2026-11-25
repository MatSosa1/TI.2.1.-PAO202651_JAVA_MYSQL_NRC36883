package ec.edu.monster.modelos.dto;

import java.util.List;

/** Países encontrados por Buscar o Filtrar y el total (ECUD5, ECUD6). */
public record ResultadoBusqueda(List<Pais> paises, int total) {
}
