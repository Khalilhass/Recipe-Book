package com.example.recipe_book.data.remote

import com.example.recipe_book.data.remote.dto.UserDto
import com.example.recipe_book.util.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val usersCollection get() = firestore.collection(Constants.FIRESTORE_USERS_COLLECTION)

    suspend fun register(email: String, password: String, userDto: UserDto): String {
        val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val user = authResult.user
            ?: throw IllegalStateException("Firebase did not return a uid after registration")
        try {
            usersCollection.document(user.uid).set(userDto).await()
            return user.uid
        } catch (e: Exception) {
            // Rollback: don't leave an orphaned Auth account with no Firestore
            // profile behind — e.g. if the device goes offline mid-registration.
            user.delete().await()
            throw e
        }
    }

    suspend fun updateProfilePhoto(uid: String, photoUrl: String) {
        usersCollection.document(uid).update("photoUrl", photoUrl).await()
    }

    suspend fun login(email: String, password: String): String {
        val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        return authResult.user?.uid
            ?: throw IllegalStateException("Firebase did not return a uid after login")
    }

    fun logout() {
        firebaseAuth.signOut()
    }

    fun getCurrentUserId(): String? = firebaseAuth.currentUser?.uid

    suspend fun getUserProfile(uid: String): UserDto {
        val snapshot = usersCollection.document(uid).get().await()
        return snapshot.toObject(UserDto::class.java)
            ?: throw NoSuchElementException("User profile not found for uid=$uid")
    }
}
