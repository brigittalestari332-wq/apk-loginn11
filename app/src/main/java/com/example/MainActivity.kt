package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.auth.attemptAutoSignIn
import com.example.auth.authStateFlow
import com.example.auth.signOut
import com.example.data.RegistrationDraft
import com.example.data.UserRepository
import com.example.ui.AuthScreen
import com.example.ui.CompleteRegistrationScreen
import com.example.ui.ProfileUiState
import com.example.ui.UserViewModel
import com.example.ui.WelcomeScreen
import com.example.ui.theme.SamudraTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SamudraTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val auth = Firebase.auth
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)

    var checkingAutoSignIn by remember { mutableStateOf(auth.currentUser == null) }
    var pendingDraft by remember { mutableStateOf<RegistrationDraft?>(null) }

    LaunchedEffect(Unit) {
        if (auth.currentUser == null) {
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = { checkingAutoSignIn = false },
                onUnauthenticated = { checkingAutoSignIn = false },
                scope = scope,
            )
        } else {
            checkingAutoSignIn = false
        }
    }

    val user = currentUser
    when {
        checkingAutoSignIn && user == null -> {
            OceanLoadingScreen(message = "Memeriksa sesi akun...")
        }
        user == null -> {
            AuthScreen(
                onPrepareRegistrationDraft = { draft -> pendingDraft = draft },
                onAuthSuccess = { /* AuthStateListener updates currentUser reactively */ },
            )
        }
        else -> {
            key(user.uid) {
                AuthenticatedAppContent(
                    user = user,
                    pendingDraft = pendingDraft,
                    onConsumeDraft = { pendingDraft = null },
                    onSignOut = {
                        pendingDraft = null
                        signOut(credentialManager = credentialManager, scope = scope)
                    },
                )
            }
        }
    }
}

@Composable
private fun AuthenticatedAppContent(
    user: FirebaseUser,
    pendingDraft: RegistrationDraft?,
    onConsumeDraft: () -> Unit,
    onSignOut: () -> Unit,
) {
    val uid = user.uid
    val userEmail = user.email.orEmpty()
    val displayName = user.displayName.orEmpty()

    val viewModel: UserViewModel = viewModel(
        key = uid,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                UserViewModel(
                    repository = UserRepository(db),
                    currentUserId = uid,
                    userEmail = userEmail,
                    displayName = displayName,
                    pendingDraft = pendingDraft,
                    onConsumeDraft = onConsumeDraft,
                )
            }
        },
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSubmitting by viewModel.isSubmitting.collectAsStateWithLifecycle()
    val actionError by viewModel.actionError.collectAsStateWithLifecycle()
    val actionSuccess by viewModel.actionSuccess.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is ProfileUiState.Loading -> {
            OceanLoadingScreen(message = "Memuat beranda Samudra...")
        }
        is ProfileUiState.NeedsRegistration -> {
            CompleteRegistrationScreen(
                userEmail = userEmail,
                initialFullName = displayName,
                isSubmitting = isSubmitting,
                errorMessage = actionError,
                onSubmit = { fullName, username, occupation, city ->
                    viewModel.submitRegistration(fullName, username, occupation, city)
                },
                onSignOut = onSignOut,
            )
        }
        is ProfileUiState.Welcome -> {
            WelcomeScreen(
                profile = state.profile,
                isSubmitting = isSubmitting,
                actionError = actionError,
                actionSuccess = actionSuccess,
                onClearMessages = viewModel::clearMessages,
                onUpdateProfile = { fullName, username, occupation, city, onDone ->
                    viewModel.updateProfile(fullName, username, occupation, city, onDone)
                },
                onSignOut = onSignOut,
            )
        }
        is ProfileUiState.Error -> {
            OceanErrorScreen(
                message = state.message,
                onSignOut = onSignOut,
            )
        }
    }
}

@Composable
private fun OceanLoadingScreen(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Waves,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp),
            )
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun OceanErrorScreen(
    message: String,
    onSignOut: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.widthIn(max = 460.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(42.dp),
                )
                Text(
                    text = "Gagal Memuat Profil",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = onSignOut,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("error_signout_button"),
                ) {
                    Text("Keluar ke Layar Login")
                }
            }
        }
    }
}
