package data.repository

import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import ui.profile.UserProfile

class ProfileRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val usersCollection = firestore.collection("users")
    private val storageRef = storage.reference.child("profile_photos")

    /**
     * Obtiene el perfil completo del usuario desde Firestore
     */
    suspend fun getUserProfile(uid: String): UserProfile? {
        return try {
            val doc = usersCollection.document(uid).get().await()
            if (doc.exists()) {
                UserProfile(
                    uid = uid,
                    email = doc.getString("email") ?: "",
                    displayName = doc.getString("displayName"),
                    phone = doc.getString("phone"),
                    weight = doc.getDouble("weight"),
                    height = doc.getLong("height")?.toInt(),
                    photoUri = doc.getString("photoUri")
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Actualiza el perfil del usuario en Firestore
     */
    suspend fun updateUserProfile(profile: UserProfile): Boolean {
        return try {
            val data = hashMapOf<String, Any?>(
                "email" to profile.email,
                "displayName" to profile.displayName,
                "phone" to profile.phone,
                "weight" to profile.weight,
                "height" to profile.height,
                "photoUri" to profile.photoUri,
                "updatedAt" to Timestamp.now()
            )

            usersCollection.document(profile.uid)
                .set(data, SetOptions.merge())
                .await()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Sube una foto de perfil a Firebase Storage y retorna la URL
     */
    suspend fun uploadProfilePhoto(uid: String, photoUri: Uri): String? {
        return try {
            val fileName = "profile_$uid.jpg"
            val photoRef = storageRef.child(fileName)

            // Subir el archivo
            photoRef.putFile(photoUri).await()

            // Obtener la URL de descarga
            val downloadUrl = photoRef.downloadUrl.await()
            downloadUrl.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Elimina la foto de perfil del usuario
     */
    suspend fun deleteProfilePhoto(uid: String): Boolean {
        return try {
            val fileName = "profile_$uid.jpg"
            val photoRef = storageRef.child(fileName)
            photoRef.delete().await()

            // Actualizar Firestore para remover la URL
            usersCollection.document(uid)
                .update("photoUri", null)
                .await()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Crea un perfil inicial cuando el usuario se registra
     */
    suspend fun createUserProfile(uid: String, email: String?, displayName: String? = null): Boolean {
        return try {
            val profile = hashMapOf(
                "email" to email,
                "displayName" to displayName,
                "createdAt" to Timestamp.now(),
                "updatedAt" to Timestamp.now()
            )

            usersCollection.document(uid).set(profile).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}