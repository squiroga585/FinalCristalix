package me.mariahernandez.cristalia.perfil

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Repositorio centralizado para el registro y gestión del historial de operaciones
 * y descargas realizadas por el usuario en las distintas secciones de la aplicación.
 * Sincroniza las operaciones tanto en memoria local mediante StateFlow como en Firebase Firestore.
 */
object HistorialRepositoryImpl {
    
    // Instancia diferida de Firebase Firestore para almacenar y consultar datos
    private val db: FirebaseFirestore? by lazy {
        try { FirebaseFirestore.getInstance() } catch (_: Exception) { null }
    }
    
    // Instancia diferida de Firebase Auth para asociar el historial al usuario autenticado
    private val auth: FirebaseAuth? by lazy {
        try { FirebaseAuth.getInstance() } catch (_: Exception) { null }
    }

    // Lista reactiva interna con las operaciones registradas
    private val _historial = MutableStateFlow<List<OperacionHistorial>>(emptyList())
    
    /** Flujo de lectura observable con la lista completa del historial y descargas del usuario. */
    val historial: StateFlow<List<OperacionHistorial>> = _historial.asStateFlow()

    /**
     * Registra un nuevo cálculo o descarga en el historial de la aplicación.
     * Crea un objeto OperacionHistorial con ID único y lo antepone a la lista actual.
     * Si el usuario inició sesión, persiste la entrada en la colección 'historial' de Firestore.
     *
     * @param titulo Nombre descriptivo del cálculo o simulación (ej. "NaCl (Densidad)").
     * @param descripcion Detalle numérico o de parámetros utilizados en la operación.
     * @param tipoEstructura Sigla de la red o tipo de estructura (ej. "FCC", "SC", "BCC").
     * @param badges Lista de etiquetas con resultados o métricas clave (ej. "ρ: 2.14 g/cm³").
     * @param esDescarga Indica si la operación corresponde a un modelo o archivo guardado/descargado.
     * @return El objeto OperacionHistorial creado y agregado.
     */
    fun agregarCalculo(
        titulo: String,
        descripcion: String,
        tipoEstructura: String = "FCC",
        badges: List<OperacionBadge> = emptyList(),
        esDescarga: Boolean = false
    ): OperacionHistorial {
        val nuevoId = UUID.randomUUID().toString().take(8)
        val nuevoItem = OperacionHistorial(
            id = nuevoId,
            titulo = titulo,
            descripcion = descripcion,
            tipoEstructura = tipoEstructura,
            badges = badges,
            esFavorito = esDescarga
        )

        // Se agrega la nueva entrada al inicio de la lista local
        _historial.update { actual ->
            listOf(nuevoItem) + actual.filterNot { it.id == nuevoId }
        }

        // Si hay usuario autenticado en Firebase, guardamos el elemento en Firestore
        val uid = auth?.currentUser?.uid
        if (uid != null && db != null) {
            try {
                db?.collection("users")
                    ?.document(uid)
                    ?.collection("historial")
                    ?.document(nuevoId)
                    ?.set(nuevoItem)
            } catch (_: Exception) {}
        }

        return nuevoItem
    }

    /**
     * Alterna el estado de guardado/descarga de un elemento específico en el historial.
     * Si estaba guardado lo desmarca, y viceversa, actualizando Firestore si corresponde.
     *
     * @param id Identificador único del cálculo a modificar.
     */
    fun toggleFavorito(id: String) {
        _historial.update { actual ->
            actual.map { item ->
                if (item.id == id) {
                    val actualizado = item.copy(esFavorito = !item.esFavorito)
                    val uid = auth?.currentUser?.uid
                    if (uid != null && db != null) {
                        try {
                            db?.collection("users")
                                ?.document(uid)
                                ?.collection("historial")
                                ?.document(id)
                                ?.set(actualizado)
                        } catch (_: Exception) {}
                    }
                    actualizado
                } else {
                    item
                }
            }
        }
    }

    /**
     * Carga de manera asíncrona las operaciones almacenadas en Firestore para el usuario activo.
     */
    suspend fun cargarHistorial() {
        val uid = auth?.currentUser?.uid ?: return
        if (db == null) return
        try {
            val snapshot = db?.collection("users")
                ?.document(uid)
                ?.collection("historial")
                ?.get()
                ?.await()
            val remotos = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(OperacionHistorial::class.java)
            } ?: emptyList()
            if (remotos.isNotEmpty()) {
                _historial.value = remotos
            }
        } catch (_: Exception) {}
    }
}
