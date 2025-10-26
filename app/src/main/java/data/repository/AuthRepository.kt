package data.repository

import data.model.User
import data.remote.datasource.FirebaseAuthDataSource

class AuthRepository(
    private val ds: FirebaseAuthDataSource = FirebaseAuthDataSource(),
    private val profileRepo: ProfileRepository = ProfileRepository()
) {
    suspend fun login(email: String, pass: String): User? {
        val fu = ds.signIn(email, pass) ?: return null
        return User(uid = fu.uid, email = fu.email)
    }

    suspend fun signUp(email: String, pass: String, displayName: String? = null): User? {
        val fu = ds.signUp(email, pass) ?: return null

        // Crear perfil en Firestore
        profileRepo.createUserProfile(
            uid = fu.uid,
            email = fu.email,
            displayName = displayName
        )

        return User(uid = fu.uid, email = fu.email, displayName = displayName)
    }

    suspend fun sendPasswordReset(email: String): Boolean {
        return ds.sendPasswordReset(email)
    }

    fun logout() = ds.signOut()

    fun currentUser(): User? = ds.currentUser()?.let { User(it.uid, it.email) }
}