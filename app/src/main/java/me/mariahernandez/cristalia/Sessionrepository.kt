package me.mariahernandez.cristalia

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Contrato de repositorio para gestionar el estado de sesión del usuario en la aplicación.
 * Define las operaciones requeridas para verificar si la sesión está activa, obtener datos del usuario
 * y controlar el inicio y cierre de sesión.
 */
interface SessionRepository {
    /** Devuelve true si el usuario tiene una sesión activa en la aplicación. */
    fun estaLogueado(): Boolean

    /** Retorna el nombre o correo del usuario logueado actualmente. */
    fun obtenerNombreUsuario(): String?

    /** Inicia la sesión de usuario y notifica a la aplicación el cambio de estado. */
    fun iniciarSesion(usuario: String)

    /** Cierra la sesión activa del usuario y limpia los datos en memoria y en Firebase. */
    fun cerrarSesion()

    /** Flujo de estado observable que emite true si hay sesión activa y false en caso contrario. */
    val estadoSesion: StateFlow<Boolean>
}

/**
 * Implementación principal del repositorio de sesión.
 * Gestiona la autenticación del usuario mediante Firebase Auth y mantiene un estado local
 * en memoria para preservar la sesión navegando entre diferentes pantallas de la app.
 */
object SessionRepositoryImpl : SessionRepository {
    
    // Instancia de Firebase Auth obtenida de forma segura
    private val auth: FirebaseAuth? = try {
        FirebaseAuth.getInstance()
    } catch (_: Exception) {
        null
    }

    // Almacena localmente el nombre de usuario de la sesión activa
    private var usuarioNombreLocal: String? = null

    // MutableStateFlow interno para emitir reactivamente el estado de sesión
    private val _estadoSesion = MutableStateFlow(comprobarSesion())
    
    // Flujo público de solo lectura para ser observado por los ViewModels y la interfaz UI
    override val estadoSesion: StateFlow<Boolean> = _estadoSesion.asStateFlow()

    /** Verifica si existe un usuario autenticado en Firebase o en el almacenamiento local. */
    private fun comprobarSesion(): Boolean {
        val firebaseUser = auth?.currentUser
        if (firebaseUser != null) return true
        return !usuarioNombreLocal.isNullOrBlank()
    }

    /** Retorna si hay sesión activa y actualiza el StateFlow si difiere del estado actual. */
    override fun estaLogueado(): Boolean {
        val result = comprobarSesion()
        if (_estadoSesion.value != result) {
            _estadoSesion.value = result
        }
        return result
    }

    /** Retorna el nombre formateado o correo electrónico del usuario actualmente autenticado. */
    override fun obtenerNombreUsuario(): String? {
        val firebaseUser = auth?.currentUser
        if (firebaseUser != null) {
            val name = firebaseUser.displayName
            if (!name.isNullOrBlank()) return name
            val email = firebaseUser.email
            if (!email.isNullOrBlank()) return email
        }
        return usuarioNombreLocal
    }

    /** Guarda el usuario en memoria y actualiza la sesión a activa (true). */
    override fun iniciarSesion(usuario: String) {
        usuarioNombreLocal = usuario
        _estadoSesion.value = true
    }

    /** Realiza el deslogueo en Firebase Auth y limpia las variables locales. */
    override fun cerrarSesion() {
        try {
            auth?.signOut()
        } catch (_: Exception) {}
        usuarioNombreLocal = null
        _estadoSesion.value = false
    }
}

/**
 * Implementación falsa del repositorio mantenida por retrocompatibilidad o pruebas unitarias.
 * Representa un estado donde nunca hay sesión iniciada.
 */
object SessionRepositoryFalso : SessionRepository {
    override fun estaLogueado(): Boolean = false
    override fun obtenerNombreUsuario(): String? = null
    override fun iniciarSesion(usuario: String) {}
    override fun cerrarSesion() {}
    override val estadoSesion: StateFlow<Boolean> = MutableStateFlow(false)
}
