package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.MarkEmailRead
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.WorkOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ProfileValidation
import com.example.ui.components.OceanHeroBanner
import com.example.ui.components.OceanPrimaryButton
import com.example.ui.components.SamudraTextField

@Composable
fun CompleteRegistrationScreen(
    userEmail: String,
    initialFullName: String,
    isSubmitting: Boolean,
    errorMessage: String?,
    onSubmit: (fullName: String, username: String, occupation: String, city: String) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val suggestedUsername = remember(userEmail, initialFullName) {
        ProfileValidation.suggestUsernameFromEmail(userEmail, initialFullName)
    }

    var fullName by rememberSaveable { mutableStateOf(initialFullName) }
    var username by rememberSaveable { mutableStateOf(suggestedUsername) }
    var occupation by rememberSaveable { mutableStateOf("") }
    var city by rememberSaveable { mutableStateOf("") }
    var showValidation by remember { mutableStateOf(false) }

    val fullNameError = if (showValidation) ProfileValidation.validateFullName(fullName) else null
    val usernameError = if (showValidation) ProfileValidation.validateUsername(username) else null
    val occupationError = if (showValidation) ProfileValidation.validateOccupation(occupation) else null
    val cityError = if (showValidation) ProfileValidation.validateCity(city) else null

    fun submit() {
        showValidation = true
        val invalid = listOf(
            ProfileValidation.validateFullName(fullName),
            ProfileValidation.validateUsername(username),
            ProfileValidation.validateOccupation(occupation),
            ProfileValidation.validateCity(city),
        ).any { it != null }
        if (!invalid) {
            onSubmit(fullName.trim(), username.trim(), occupation.trim(), city.trim())
        }
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
            title = stringResource(R.string.complete_reg_headline),
            subtitle = stringResource(R.string.complete_reg_subtitle),
        )

        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MarkEmailRead,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Column {
                        Text(
                            text = "Akun Google Terhubung",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = userEmail.ifBlank { "Pengguna Google" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            if (errorMessage != null) {
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
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
            }

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
                    SamudraTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = stringResource(R.string.field_fullname),
                        leadingIcon = Icons.Rounded.Person,
                        errorText = fullNameError,
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        testTag = "complete_fullname_input",
                    )

                    SamudraTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = stringResource(R.string.field_username),
                        leadingIcon = Icons.Rounded.AlternateEmail,
                        errorText = usernameError,
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        testTag = "complete_username_input",
                    )

                    SamudraTextField(
                        value = occupation,
                        onValueChange = { occupation = it },
                        label = stringResource(R.string.field_occupation),
                        placeholder = "Contoh: Mahasiswa / Profesional",
                        leadingIcon = Icons.Rounded.WorkOutline,
                        errorText = occupationError,
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        testTag = "complete_occupation_input",
                    )

                    SamudraTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = stringResource(R.string.field_city),
                        placeholder = "Contoh: Jakarta",
                        leadingIcon = Icons.Rounded.LocationCity,
                        errorText = cityError,
                        imeAction = ImeAction.Done,
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                submit()
                            }
                        ),
                        testTag = "complete_city_input",
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OceanPrimaryButton(
                        text = stringResource(R.string.complete_reg_submit),
                        onClick = ::submit,
                        isLoading = isSubmitting,
                        leadingIcon = Icons.Rounded.CheckCircle,
                        testTag = "complete_registration_button",
                    )

                    OutlinedButton(
                        onClick = onSignOut,
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("cancel_registration_signout_button"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Ganti Akun Google")
                    }
                }
            }
        }
    }
}
