package com.krishiai.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.krishiai.app.data.model.User
import com.krishiai.app.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : AuthRepository {

    override fun getCurrentUser(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser == null) {
                trySend(null)
            } else {
                firestore.collection("users").document(firebaseUser.uid)
                    .get()
                    .addOnSuccessListener { document ->
                        val user = document.toObject(User::class.java)
                        trySend(user)
                    }
                    .addOnFailureListener {
                        trySend(User(uid = firebaseUser.uid, email = firebaseUser.email ?: ""))
                    }
            }
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun login(email: String, password: String): Result<User> = runCatching {
        val authResult = auth.signInWithEmailAndPassword(email, password).await()
        val uid = authResult.user?.uid ?: throw Exception("Authentication UID is null")
        val documentSnapshot = firestore.collection("users").document(uid).get().await()
        documentSnapshot.toObject(User::class.java) ?: throw Exception("User profile not found in database")
    }

    override suspend fun signup(user: User, password: String): Result<User> = runCatching {
        val authResult = auth.createUserWithEmailAndPassword(user.email, password).await()
        val uid = authResult.user?.uid ?: throw Exception("Auth registration UID is null")
        val userWithUid = user.copy(uid = uid)
        firestore.collection("users").document(uid).set(userWithUid).await()
        userWithUid
    }

    override suspend fun logout(): Result<Unit> = runCatching {
        auth.signOut()
    }

    override suspend fun resetPassword(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email).await()
    }

    override suspend fun uploadProfilePhoto(bytes: ByteArray): Result<String> = runCatching {
        val uid = auth.currentUser?.uid ?: UUID.randomUUID().toString()
        val ref = storage.reference.child("profile_photos/$uid.jpg")
        ref.putBytes(bytes).await()
        ref.downloadUrl.await().toString()
    }

    override suspend fun getUserProfile(uid: String): Result<User> = runCatching {
        val document = firestore.collection("users").document(uid).get().await()
        document.toObject(User::class.java) ?: throw Exception("Profile does not exist")
    }
}
