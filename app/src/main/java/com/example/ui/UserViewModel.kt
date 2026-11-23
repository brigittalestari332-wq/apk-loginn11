package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.RegistrationDraft
import com.example.data.UserProfile
import com.example.data.UserRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data object NeedsRegistration : ProfileUiState
    data class Welcome(val profile: UserProfile) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

class UserViewModel(
    private val repository: UserRepository,
    private val currentUserId: String,
    private val userEmail: String,
    private val displayName: String,
    private val pendingDraft: RegistrationDraft?,
    private val onConsumeDraft: () -> Unit,
) : ViewModel() {

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

    private val _actionSuccess = MutableStateFlow<String?>(null)
    val actionSuccess: StateFlow<String?> = _actionSuccess.asStateFlow()

    private var sessionRecorded = false
    private var autoCreateAttempted = false

    val uiState: StateFlow<ProfileUiState> = repository.observeUserProfile(currentUserId)
        .onEach { profile ->
            if (profile == null) {
                if (pendingDraft != null && !autoCreateAttempted) {
                    autoCreateAttempted = true
                    autoSubmitPendingDraft(pendingDraft)
                }
            } else {
                if (pendingDraft != null) {
                    onConsumeDraft()
                }
                if (!sessionRecorded) {
                    sessionRecorded = true
                    viewModelScope.launch {
                        repository.recordLoginSession(currentUserId)
                    }
                }
            }
        }
        .map { profile ->
            if (profile == null) {
                if (pendingDraft != null && autoCreateAttempted && _isSubmitting.value) {
                    ProfileUiState.Loading
                } else {
                    ProfileUiState.NeedsRegistration
                }
            } else {
                ProfileUiState.Welcome(profile)
            }
        }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing user profile", rootError)
            emit(
                ProfileUiState.Error(
                    rootError.message ?: "Gagal memuat profil pengguna dari server."
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = ProfileUiState.Loading
        )

    private fun autoSubmitPendingDraft(draft: RegistrationDraft) {
        viewModelScope.launch {
            _isSubmitting.value = true
            sessionRecorded = true
            val result = repository.createUserProfile(
                fullName = draft.fullName,
                username = draft.username,
                email = userEmail,
                occupation = draft.occupation,
                city = draft.city,
            )
            _isSubmitting.value = false
            onConsumeDraft()
            result.onSuccess {
                _actionSuccess.value = "Registrasi berhasil! Akun Anda telah terdaftar."
            }.onFailure { err ->
                sessionRecorded = false
                _actionError.value = err.localizedMessage ?: "Gagal menyimpan registrasi."
            }
        }
    }

    fun submitRegistration(
        fullName: String,
        username: String,
        occupation: String,
        city: String,
    ) {
        if (_isSubmitting.value) return
        viewModelScope.launch {
            _isSubmitting.value = true
            _actionError.value = null
            sessionRecorded = true
            val result = repository.createUserProfile(
                fullName = fullName,
                username = username,
                email = userEmail,
                occupation = occupation,
                city = city,
            )
            _isSubmitting.value = false
            result.onSuccess {
                _actionSuccess.value = "Akun berhasil didaftarkan!"
            }.onFailure { err ->
                sessionRecorded = false
                _actionError.value = err.localizedMessage ?: "Gagal menyimpan data registrasi."
            }
        }
    }

    fun updateProfile(
        fullName: String,
        username: String,
        occupation: String,
        city: String,
        onComplete: () -> Unit = {},
    ) {
        if (_isSubmitting.value) return
        viewModelScope.launch {
            _isSubmitting.value = true
            _actionError.value = null
            val result = repository.updateUserProfile(
                userId = currentUserId,
                fullName = fullName,
                username = username,
                occupation = occupation,
                city = city,
            )
            _isSubmitting.value = false
            result.onSuccess {
                _actionSuccess.value = "Profil berhasil diperbarui."
                onComplete()
            }.onFailure { err ->
                _actionError.value = err.localizedMessage ?: "Gagal memperbarui profil."
            }
        }
    }

    fun clearMessages() {
        _actionError.value = null
        _actionSuccess.value = null
    }

    private companion object {
        const val TAG = "UserViewModel"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
