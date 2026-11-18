package me.mariahernandez.cristalia.perfil

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repositorio para la interacción directa con la colección 'favoritos' de Firebase Firestore.
 * Proporciona métodos suspendibles para guardar, eliminar y consultar elementos guardados por el usuario.
 */
class FavoritosRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /** Retorna el Identificador Único de Usuario (UID) en Firebase Auth, o null si no está logueado. */
    private val userId: String?
        get() = auth.currentUser?.uid

    /**
     * Almacena una operación marcada como favorita/descarga en el perfil del usuario en Firestore.
     * @param item Objeto OperacionHistorial a guardar.
     */
    suspend fun guardarFavorito(item: OperacionHistorial) {
        val uid = userId ?: return
        try {
            db.collection("users")
                .document(uid)
                .collection("favoritos")
                .document(item.id)
                .set(item)
                .await()
        } catch (_: Exception) {}
    }

    /**
     * Elimina un elemento de la colección de favoritos en Firestore usando su ID.
     * @param itemId ID único del elemento a remover.
     */
    suspend fun eliminarFavorito(itemId: String) {
        val uid = userId ?: return
        try {
            db.collection("users")
                .document(uid)
                .collection("favoritos")
                .document(itemId)
                .delete()
                .await()
        } catch (_: Exception) {}
    }

    /**
     * Recupera todos los elementos guardados como favoritos del usuario activo desde Firestore.
     * @return Lista de objetos OperacionHistorial guardados.
     */
    suspend fun obtenerFavoritos(): List<OperacionHistorial> {
        val uid = userId ?: return emptyList()
        return try {
            val snapshot = db.collection("users")
                .document(uid)
                .collection("favoritos")
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(OperacionHistorial::class.java)?.copy(esFavorito = true)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
