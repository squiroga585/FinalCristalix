package me.mariahernandez.cristalia.huecos.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.mariahernandez.cristalia.SessionRepository
import me.mariahernandez.cristalia.SessionRepositoryImpl
import me.mariahernandez.cristalia.celdaunitaria.EventoUi
import me.mariahernandez.cristalia.perfil.HistorialRepositoryImpl
import me.mariahernandez.cristalia.perfil.OperacionBadge

data class DetalleHuecosUiState(
    /** Datos de la estructura elegida; null mientras no se ha cargado o si el id no existe. */
    val info: InfoHuecos? = null
)

//                                       sesion          tipo           valor por defecto
class DetalleHuecosViewModel(
    private val session: SessionRepository = SessionRepositoryImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleHuecosUiState())
    val uiState: StateFlow<DetalleHuecosUiState> = _uiState.asStateFlow()

    // Eventos de una sola vez (login / descargar). Se reutiliza EventoUi de celdaunitaria.
    private val _eventos = Channel<EventoUi>(Channel.BUFFERED)
    val eventos = _eventos.receiveAsFlow()

    /** Carga los datos de la opción tocada en el menú (sc, bcc, fcc, nacl...). */
    fun cargar(opcionId: String) {
        _uiState.update { it.copy(info = infoHuecos(opcionId)) }
    }

    /** Si hay sesión descarga el GLB; si no, manda al login. */
    fun onDescargarClick(archivoGlb: String) {
        viewModelScope.launch {
            if (session.estaLogueado()) {
                val tituloInfo = _uiState.value.info?.etiquetaGlb ?: "Huecos 3D"
                val textoNc = _uiState.value.info?.textoNc ?: "3D"
                HistorialRepositoryImpl.agregarCalculo(
                    titulo = "Huecos ($tituloInfo)",
                    descripcion = "Visualización 3D guardada/descargada",
                    tipoEstructura = "HUECO",
                    badges = listOf(OperacionBadge("Coordinación", textoNc)),
                    esDescarga = true
                )
                _eventos.send(EventoUi.Descargar(archivoGlb))
            } else {
                _eventos.send(EventoUi.IrALogin)
            }
        }
    }
}