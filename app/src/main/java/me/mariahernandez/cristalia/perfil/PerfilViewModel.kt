package me.mariahernandez.cristalia.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.mariahernandez.cristalia.SessionRepositoryImpl

sealed interface PerfilEventoUi {
    data object Volver : PerfilEventoUi
    data object CerrarSesion : PerfilEventoUi
}

class PerfilViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    private val _eventos = Channel<PerfilEventoUi>(Channel.BUFFERED)
    val eventos = _eventos.receiveAsFlow()

    init {
        viewModelScope.launch {
            HistorialRepositoryImpl.historial.collect { lista ->
                _uiState.update { it.copy(historial = lista) }
            }
        }
        viewModelScope.launch {
            HistorialRepositoryImpl.cargarHistorial()
        }
    }

    fun inicializarUsuario(usuarioRaw: String?) {
        val nombreSession = SessionRepositoryImpl.obtenerNombreUsuario()
        val usuarioElegido = if (!usuarioRaw.isNullOrBlank()) usuarioRaw else nombreSession
        if (!usuarioElegido.isNullOrBlank()) {
            val nombreLimpio = usuarioElegido.trim()
            val nombreFormateado = if (nombreLimpio.contains("@")) {
                nombreLimpio.substringBefore("@")
                    .replace(".", " ")
                    .replace("_", " ")
                    .capitalizeWords()
            } else {
                nombreLimpio.replace("_", " ").capitalizeWords()
            }
            _uiState.update { it.copy(nombreUsuario = nombreFormateado) }
        }
    }

    private fun String.capitalizeWords(): String =
        split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }

    fun seleccionarTab(tab: TabPerfil) {
        _uiState.update { it.copy(tabSeleccionado = tab) }
    }

    fun toggleFavorito(id: String) {
        HistorialRepositoryImpl.toggleFavorito(id)
    }

    fun onVolverClick() {
        viewModelScope.launch {
            _eventos.send(PerfilEventoUi.Volver)
        }
    }

    fun onCerrarSesionClick() {
        viewModelScope.launch {
            _eventos.send(PerfilEventoUi.CerrarSesion)
        }
    }
}
