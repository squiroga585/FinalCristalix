package me.mariahernandez.cristalia.perfil

/**
 * Representa una etiqueta o distintivo dentro de una tarjeta de operación (ej. "NC: 6:6" o "ρ: 2.14 g/cm³").
 * Incluye un constructor sin parámetros con valores por defecto para ser deserializable por Firestore.
 */
data class OperacionBadge(
    val etiqueta: String = "",
    val valor: String = ""
)

/**
 * Modelo de datos que representa un cálculo o simulación registrado en el historial del perfil.
 *
 * @param id Identificador único del cálculo.
 * @param titulo Nombre descriptivo del cálculo o estructura.
 * @param descripcion Resumen de los parámetros de entrada o resultados.
 * @param tipoEstructura Tipo de red cristalina ("FCC", "BCC", "SC", "HCP", "HUECO", etc.).
 * @param badges Lista de insignias con métricas destacadas del cálculo.
 * @param esFavorito Indica si el cálculo está marcado como guardado/descargado por el usuario.
 */
data class OperacionHistorial(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val tipoEstructura: String = "FCC",
    val badges: List<OperacionBadge> = emptyList(),
    val esFavorito: Boolean = false
)

/**
 * Pestañas disponibles en la pantalla de Perfil.
 */
enum class TabPerfil {
    /** Pestaña que filtra y muestra las operaciones guardadas o descargadas. */
    DESCARGAS,
    /** Pestaña que muestra la totalidad de cálculos registrados en la sesión. */
    HISTORIAL
}

/**
 * Estado UI completo que observa la pantalla de Perfil (PerfilScreen).
 *
 * @param nombreUsuario Nombre del investigador actualmente autenticado.
 * @param tabSeleccionado Pestaña activa en la pantalla (DESCARGAS o HISTORIAL).
 * @param historial Lista completa de cálculos registrados.
 */
data class PerfilUiState(
    val nombreUsuario: String = "Usuario",
    val tabSeleccionado: TabPerfil = TabPerfil.DESCARGAS,
    val historial: List<OperacionHistorial> = emptyList()
) {
    /** Número total de cálculos realizados durante la sesión. */
    val totalCalculos: Int get() = historial.size

    /** Número total de cálculos o modelos descargados/guardados. */
    val totalDescargas: Int get() = historial.count { it.esFavorito }

    /** Lista filtrada conteniendo solo los elementos marcados como guardados/descargados. */
    val descargasFiltradas: List<OperacionHistorial> get() = historial.filter { it.esFavorito }
}
