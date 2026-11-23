package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth,
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("Pengguna harus masuk dengan Google sebelum mengakses Firestore.")
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Sesi pengguna berubah; menolak akses lintas pengguna." }
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(
                            UserProfile::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else {
                        null
                    }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    suspend fun createUserProfile(
        fullName: String,
        username: String,
        email: String,
        occupation: String,
        city: String,
    ): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}"
        return try {
            val uid = requireUserId()
            val cleanFullName = fullName.trim()
            val cleanUsername = username.trim()
            val cleanEmail = email.trim()
            val cleanOccupation = occupation.trim()
            val cleanCity = city.trim()

            ProfileValidation.validateFullName(cleanFullName)?.let { throw IllegalArgumentException(it) }
            ProfileValidation.validateUsername(cleanUsername)?.let { throw IllegalArgumentException(it) }
            ProfileValidation.validateOccupation(cleanOccupation)?.let { throw IllegalArgumentException(it) }
            ProfileValidation.validateCity(cleanCity)?.let { throw IllegalArgumentException(it) }
            require(cleanEmail.length in 3..120) { "Email akun Google tidak valid." }

            val docRef = db.collection("users").document(uid)
            docPath = docRef.path
            val payload = mapOf(
                "userId" to uid,
                "fullName" to cleanFullName,
                "username" to cleanUsername,
                "email" to cleanEmail,
                "occupation" to cleanOccupation,
                "city" to cleanCity,
                "loginCount" to 1L,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
                "lastLoginAt" to FieldValue.serverTimestamp(),
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(
        userId: String,
        fullName: String,
        username: String,
        occupation: String,
        city: String,
    ): Result<Unit> {
        var docPath = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Sesi pengguna berubah; menolak pembaruan lintas pengguna." }

            val cleanFullName = fullName.trim()
            val cleanUsername = username.trim()
            val cleanOccupation = occupation.trim()
            val cleanCity = city.trim()

            ProfileValidation.validateFullName(cleanFullName)?.let { throw IllegalArgumentException(it) }
            ProfileValidation.validateUsername(cleanUsername)?.let { throw IllegalArgumentException(it) }
            ProfileValidation.validateOccupation(cleanOccupation)?.let { throw IllegalArgumentException(it) }
            ProfileValidation.validateCity(cleanCity)?.let { throw IllegalArgumentException(it) }

            val docRef = db.collection("users").document(uid)
            docPath = docRef.path
            docRef.update(
                mapOf(
                    "fullName" to cleanFullName,
                    "username" to cleanUsername,
                    "occupation" to cleanOccupation,
                    "city" to cleanCity,
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun recordLoginSession(userId: String): Result<Unit> {
        var docPath = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Sesi pengguna berubah; menolak pembaruan sesi." }
            val docRef = db.collection("users").document(uid)
            docPath = docRef.path
            docRef.update(
                mapOf(
                    "loginCount" to FieldValue.increment(1L),
                    "lastLoginAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }
}
