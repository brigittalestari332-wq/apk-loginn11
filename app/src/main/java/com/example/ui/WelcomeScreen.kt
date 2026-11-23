package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.Waves
import androidx.compose.material.icons.rounded.WorkOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ProfileValidation
import com.example.data.UserProfile
import com.example.ui.components.OceanPrimaryButton
import com.example.ui.components.SamudraTextField
import com.example.ui.theme.OceanBright
import com.example.ui.theme.OceanDeep
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SeaTealContainer
import com.example.ui.theme.SeaTealSuccess
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun WelcomeScreen(
    profile: UserProfile,
    isSubmitting: Boolean,
    actionError: String?,
    actionSuccess: String?,
    onClearMessages: () -> Unit,
    onUpdateProfile: (fullName: String, username: String, occupation: String, city: String, onDone: () -> Unit) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(actionError, actionSuccess) {
        val message = actionError ?: actionSuccess
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            onClearMessages()
        }
    }

    val dateFormatter = remember {
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID"))
    }
    val registeredDateText = remember(profile.createdAt) {
        val ts = profile.createdAt ?: Timestamp.now()
        dateFormatter.format(ts.toDate())
    }
    val lastLoginText = remember(profile.lastLoginAt, profile.updatedAt, profile.createdAt) {
        val ts = profile.lastLoginAt ?: profile.updatedAt ?: profile.createdAt ?: Timestamp.now()
        dateFormatter.format(ts.toDate())
    }
    val initials = remember(profile.fullName) {
        profile.fullName
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotEmpty() }
            .take(2)
            .joinToString("") { it.first().uppercaseChar().toString() }
            .ifBlank { "S" }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Hero Welcome Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(OceanDeep, OceanPrimary, OceanBright)
                        )
                    )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_ocean_wave_hero_1795399885570),
                    contentDescription = stringResource(R.string.cd_hero_banner),
                    contentScale = ContentScale.Crop,
                    alpha = 0.28f,
                    modifier = Modifier.matchParentSize(),
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    OceanDeep.copy(alpha = 0.78f),
                                    OceanPrimary.copy(alpha = 0.62f),
                                    OceanDeep.copy(alpha = 0.90f),
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Waves,
                                contentDescription = stringResource(R.string.cd_app_logo),
                                tint = PureWhite,
                                modifier = Modifier.size(22.dp),
                            )
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge,
                                color = PureWhite,
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = PureWhite.copy(alpha = 0.16f),
                            modifier = Modifier.border(1.dp, PureWhite.copy(alpha = 0.3f), CircleShape),
                        ) {
                            IconButton(
                                onClick = onSignOut,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("top_logout_button"),
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Logout,
                                    contentDescription = stringResource(R.string.action_logout),
                                    tint = PureWhite,
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = PureWhite,
                            shadowElevation = 4.dp,
                            modifier = Modifier.size(64.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = initials,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = OceanPrimary,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = stringResource(R.string.welcome_greeting_prefix),
                                style = MaterialTheme.typography.bodyMedium,
                                color = PureWhite.copy(alpha = 0.85f),
                            )
                            Text(
                                text = profile.fullName.ifBlank { "Pengguna Samudra" },
                                style = MaterialTheme.typography.headlineMedium,
                                color = PureWhite,
                                modifier = Modifier.testTag("welcome_title"),
                            )
                            Surface(
                                shape = RoundedCornerShape(99.dp),
                                color = PureWhite.copy(alpha = 0.2f),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Verified,
                                        contentDescription = null,
                                        tint = PureWhite,
                                        modifier = Modifier.size(14.dp),
                                    )
                                    Text(
                                        text = "@${profile.username} • ${stringResource(R.string.welcome_badge)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = PureWhite,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Content body
            Column(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Status summary row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    StatHighlightCard(
                        title = stringResource(R.string.label_total_visits),
                        value = "${profile.loginCount}x",
                        subtitle = "Sesi tercatat",
                        modifier = Modifier.weight(1f),
                    )
                    StatHighlightCard(
                        title = stringResource(R.string.label_account_status),
                        value = stringResource(R.string.status_active),
                        subtitle = "Cloud Firestore",
                        badgeColor = SeaTealContainer,
                        badgeTextColor = SeaTealSuccess,
                        modifier = Modifier.weight(1f),
                    )
                }

                // Profile Details Card
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
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.section_profile_details),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            TextButton(
                                onClick = { showEditDialog = true },
                                modifier = Modifier.testTag("edit_profile_button"),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.action_edit_profile))
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        ProfileDetailRow(
                            icon = Icons.Rounded.Person,
                            label = stringResource(R.string.field_fullname),
                            value = profile.fullName,
                        )
                        ProfileDetailRow(
                            icon = Icons.Rounded.AlternateEmail,
                            label = stringResource(R.string.field_username),
                            value = "@${profile.username}",
                        )
                        ProfileDetailRow(
                            icon = Icons.Rounded.Email,
                            label = "Email Terdaftar",
                            value = profile.email,
                        )
                        ProfileDetailRow(
                            icon = Icons.Rounded.WorkOutline,
                            label = stringResource(R.string.field_occupation),
                            value = profile.occupation.ifBlank { "Anggota Samudra" },
                        )
                        ProfileDetailRow(
                            icon = Icons.Rounded.LocationCity,
                            label = "Kota Domisili",
                            value = profile.city.ifBlank { "Belum diatur" },
                        )
                        ProfileDetailRow(
                            icon = Icons.Rounded.CalendarMonth,
                            label = "Terdaftar Sejak",
                            value = registeredDateText,
                        )
                        ProfileDetailRow(
                            icon = Icons.Rounded.AccessTime,
                            label = "Masuk Terakhir",
                            value = lastLoginText,
                        )
                    }
                }

                // Bottom Sign Out Action
                OutlinedButton(
                    onClick = onSignOut,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("logout_button"),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.action_logout),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }

    if (showEditDialog) {
        EditProfileDialog(
            profile = profile,
            isSubmitting = isSubmitting,
            onDismiss = { showEditDialog = false },
            onSave = { fullName, username, occupation, city ->
                onUpdateProfile(fullName, username, occupation, city) {
                    showEditDialog = false
                }
            },
        )
    }
}

@Composable
private fun StatHighlightCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    badgeColor: androidx.compose.ui.graphics.Color? = null,
    badgeTextColor: androidx.compose.ui.graphics.Color? = null,
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (badgeColor != null && badgeTextColor != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = badgeColor,
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleLarge,
                        color = badgeTextColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                    )
                }
            } else {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ProfileDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
            modifier = Modifier.size(40.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun EditProfileDialog(
    profile: UserProfile,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSave: (fullName: String, username: String, occupation: String, city: String) -> Unit,
) {
    var fullName by remember { mutableStateOf(profile.fullName) }
    var username by remember { mutableStateOf(profile.username) }
    var occupation by remember { mutableStateOf(profile.occupation) }
    var city by remember { mutableStateOf(profile.city) }
    var showValidation by remember { mutableStateOf(false) }

    val fullNameError = if (showValidation) ProfileValidation.validateFullName(fullName) else null
    val usernameError = if (showValidation) ProfileValidation.validateUsername(username) else null
    val occupationError = if (showValidation) ProfileValidation.validateOccupation(occupation) else null
    val cityError = if (showValidation) ProfileValidation.validateCity(city) else null

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = stringResource(R.string.action_edit_profile),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SamudraTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = stringResource(R.string.field_fullname),
                    leadingIcon = Icons.Rounded.Person,
                    errorText = fullNameError,
                    testTag = "edit_fullname_input",
                )
                SamudraTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = stringResource(R.string.field_username),
                    leadingIcon = Icons.Rounded.AlternateEmail,
                    errorText = usernameError,
                    testTag = "edit_username_input",
                )
                SamudraTextField(
                    value = occupation,
                    onValueChange = { occupation = it },
                    label = stringResource(R.string.field_occupation),
                    leadingIcon = Icons.Rounded.WorkOutline,
                    errorText = occupationError,
                    testTag = "edit_occupation_input",
                )
                SamudraTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = stringResource(R.string.field_city),
                    leadingIcon = Icons.Rounded.LocationCity,
                    errorText = cityError,
                    imeAction = ImeAction.Done,
                    testTag = "edit_city_input",
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    showValidation = true
                    val invalid = listOf(
                        ProfileValidation.validateFullName(fullName),
                        ProfileValidation.validateUsername(username),
                        ProfileValidation.validateOccupation(occupation),
                        ProfileValidation.validateCity(city),
                    ).any { it != null }
                    if (!invalid) {
                        onSave(fullName.trim(), username.trim(), occupation.trim(), city.trim())
                    }
                },
                enabled = !isSubmitting,
                modifier = Modifier.testTag("save_profile_button"),
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSubmitting,
            ) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}
