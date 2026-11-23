package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.WorkOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.auth.onGoogleSignInClicked
import com.example.data.ProfileValidation
import com.example.data.RegistrationDraft
import com.example.ui.components.OceanHeroBanner
import com.example.ui.components.OceanPrimaryButton
import com.example.ui.components.SamudraTextField

enum class AuthTab {
    LOGIN,
    REGISTER,
}

@Composable
fun AuthScreen(
    onPrepareRegistrationDraft: (RegistrationDraft?) -> Unit,
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var selectedTab by rememberSaveable { mutableStateOf(AuthTab.LOGIN) }
    var isLoading by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }

    // Registration form fields
    var fullName by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var occupation by rememberSaveable { mutableStateOf("") }
    var city by rememberSaveable { mutableStateOf("") }
    var showValidationErrors by remember { mutableStateOf(false) }

    val fullNameError = if (showValidationErrors) ProfileValidation.validateFullName(fullName) else null
    val usernameError = if (showValidationErrors) ProfileValidation.validateUsername(username) else null
    val occupationError = if (showValidationErrors) ProfileValidation.validateOccupation(occupation) else null
    val cityError = if (showValidationErrors) ProfileValidation.validateCity(city) else null

    fun triggerGoogleLogin() {
        authError = null
        onPrepareRegistrationDraft(null)
        isLoading = true
        onGoogleSignInClicked(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {
                isLoading = false
                onAuthSuccess()
            },
            onAuthError = { msg ->
                isLoading = false
                authError = msg
            },
            scope = scope,
            onAuthCancelled = {
                isLoading = false
            },
        )
    }

    fun triggerGoogleRegistration() {
        authError = null
        showValidationErrors = true
        val hasError = listOf(
            ProfileValidation.validateFullName(fullName),
            ProfileValidation.validateUsername(username),
            ProfileValidation.validateOccupation(occupation),
            ProfileValidation.validateCity(city),
        ).any { it != null }
        if (hasError) return

        onPrepareRegistrationDraft(
            RegistrationDraft(
                fullName = fullName.trim(),
                username = username.trim(),
                occupation = occupation.trim(),
                city = city.trim(),
            )
        )
        isLoading = true
        onGoogleSignInClicked(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {
                isLoading = false
                onAuthSuccess()
            },
            onAuthError = { msg ->
                isLoading = false
                onPrepareRegistrationDraft(null)
                authError = msg
            },
            scope = scope,
            onAuthCancelled = {
                isLoading = false
                onPrepareRegistrationDraft(null)
            },
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        OceanHeroBanner(
            title = stringResource(R.string.app_name),
            subtitle = stringResource(R.string.app_tagline),
        )

        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AuthSegmentedSwitcher(
                selectedTab = selectedTab,
                onSelectTab = { tab ->
                    selectedTab = tab
                    authError = null
                },
            )

            if (authError != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                        )
                        Text(
                            text = authError ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
            }

            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "auth_tab_transition",
            ) { tab ->
                when (tab) {
                    AuthTab.LOGIN -> LoginCard(
                        isLoading = isLoading,
                        onLoginClick = ::triggerGoogleLogin,
                        onSwitchToRegister = {
                            selectedTab = AuthTab.REGISTER
                            authError = null
                        },
                    )
                    AuthTab.REGISTER -> RegisterCard(
                        fullName = fullName,
                        onFullNameChange = { fullName = it },
                        username = username,
                        onUsernameChange = { username = it },
                        occupation = occupation,
                        onOccupationChange = { occupation = it },
                        city = city,
                        onCityChange = { city = it },
                        fullNameError = fullNameError,
                        usernameError = usernameError,
                        occupationError = occupationError,
                        cityError = cityError,
                        isLoading = isLoading,
                        onRegisterClick = ::triggerGoogleRegistration,
                        onSwitchToLogin = {
                            selectedTab = AuthTab.LOGIN
                            authError = null
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthSegmentedSwitcher(
    selectedTab: AuthTab,
    onSelectTab: (AuthTab) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            SegmentPill(
                label = stringResource(R.string.tab_login),
                icon = Icons.AutoMirrored.Rounded.Login,
                selected = selectedTab == AuthTab.LOGIN,
                onClick = { onSelectTab(AuthTab.LOGIN) },
                testTag = "tab_login_button",
                modifier = Modifier.weight(1f),
            )
            SegmentPill(
                label = stringResource(R.string.tab_register),
                icon = Icons.Rounded.PersonAdd,
                selected = selectedTab == AuthTab.REGISTER,
                onClick = { onSelectTab(AuthTab.REGISTER) },
                testTag = "tab_register_button",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SegmentPill(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(containerColor)
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun LoginCard(
    isLoading: Boolean,
    onLoginClick: () -> Unit,
    onSwitchToRegister: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.login_headline),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.login_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    FeatureBullet(
                        icon = Icons.Rounded.VerifiedUser,
                        title = "Autentikasi Google Terpercaya",
                        subtitle = "Masuk sekali ketuk tanpa repot mengingat kata sandi.",
                    )
                    FeatureBullet(
                        icon = Icons.Rounded.CloudDone,
                        title = "Data Profil Tersimpan Aman",
                        subtitle = "Identitas akun tersinkronisasi langsung dengan Cloud Firestore.",
                    )
                }
            }

            OceanPrimaryButton(
                text = stringResource(R.string.login_google_button),
                onClick = onLoginClick,
                isLoading = isLoading,
                leadingIcon = Icons.AutoMirrored.Rounded.Login,
                testTag = "login_google_button",
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Pengguna baru?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(
                    onClick = onSwitchToRegister,
                    modifier = Modifier.testTag("switch_to_register_link"),
                ) {
                    Text(
                        text = "Registrasi di sini",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun RegisterCard(
    fullName: String,
    onFullNameChange: (String) -> Unit,
    username: String,
    onUsernameChange: (String) -> Unit,
    occupation: String,
    onOccupationChange: (String) -> Unit,
    city: String,
    onCityChange: (String) -> Unit,
    fullNameError: String?,
    usernameError: String?,
    occupationError: String?,
    cityError: String?,
    isLoading: Boolean,
    onRegisterClick: () -> Unit,
    onSwitchToLogin: () -> Unit,
) {
    val focusManager = LocalFocusManager.current

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.register_headline),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.register_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            SamudraTextField(
                value = fullName,
                onValueChange = onFullNameChange,
                label = stringResource(R.string.field_fullname),
                placeholder = "Contoh: Nabila Putri",
                leadingIcon = Icons.Rounded.Person,
                errorText = fullNameError,
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                testTag = "register_fullname_input",
            )

            SamudraTextField(
                value = username,
                onValueChange = onUsernameChange,
                label = stringResource(R.string.field_username),
                placeholder = "Contoh: nabila_laut",
                leadingIcon = Icons.Rounded.AlternateEmail,
                errorText = usernameError,
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                testTag = "register_username_input",
            )

            SamudraTextField(
                value = occupation,
                onValueChange = onOccupationChange,
                label = stringResource(R.string.field_occupation),
                placeholder = "Contoh: Mahasiswa / Desainer",
                leadingIcon = Icons.Rounded.WorkOutline,
                errorText = occupationError,
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                testTag = "register_occupation_input",
            )

            SamudraTextField(
                value = city,
                onValueChange = onCityChange,
                label = stringResource(R.string.field_city),
                placeholder = "Contoh: Surabaya",
                leadingIcon = Icons.Rounded.LocationCity,
                errorText = cityError,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        onRegisterClick()
                    }
                ),
                testTag = "register_city_input",
            )

            Spacer(modifier = Modifier.height(4.dp))

            OceanPrimaryButton(
                text = stringResource(R.string.register_google_button),
                onClick = onRegisterClick,
                isLoading = isLoading,
                leadingIcon = Icons.Rounded.Badge,
                testTag = "register_google_button",
            )

            Text(
                text = "Setelah menekan tombol di atas, pilih akun Google Anda untuk menyelesaikan pendaftaran.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Sudah terdaftar?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(
                    onClick = onSwitchToLogin,
                    modifier = Modifier.testTag("switch_to_login_link"),
                ) {
                    Text(
                        text = "Masuk di sini",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureBullet(
    icon: ImageVector,
    title: String,
    subtitle: String,
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(20.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
