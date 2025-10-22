package repository.Auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class FirebaseAuthDataSource(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    suspend fun signIn(email: String, pass: String): FirebaseUser? =
        suspendCancellableCoroutine { cont ->
            auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener { cont.resume(it.user) }
                .addOnFailureListener { cont.resume(null) }
        }


    suspend fun signUp(email: String, pass: String): FirebaseUser? =
        suspendCancellableCoroutine { cont ->
            auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener { cont.resume(it.user) }
                .addOnFailureListener { cont.resume(null) }
        }

    suspend fun sendPasswordReset(email: String): Boolean =
        suspendCancellableCoroutine { cont ->
            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener { cont.resume(true) }
                .addOnFailureListener { cont.resume(false) }
        }


    fun currentUser(): FirebaseUser? = auth.currentUser
    fun signOut() = auth.signOut()
}