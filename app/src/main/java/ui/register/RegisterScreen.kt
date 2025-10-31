package ui.register

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onRegistered: () -> Unit,
    vm: RegisterViewModel = viewModel()
) {
    val state by vm.ui.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.registered) {
        if (state.registered) onRegistered()
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            vm.messageConsumed()
        }
    }

    MaterialTheme(colorScheme = lightColorScheme()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = MaterialTheme.colorScheme.background
        ) { inner ->
            Box(
                modifier = Modifier
                    .padding(inner)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(0.85f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Crear cuenta",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    OutlinedTextField(
                        value = state.email,
                        onValueChange = vm::onEmailChange,
                        label = { Text("Correo electrónico") },
                        placeholder = { Text("ejemplo@correo.com") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = "Icono de correo")
                        },
                        isError = state.email.isNotEmpty() &&
                                !android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches(),
                        supportingText = {
                            if (state.email.isNotEmpty() &&
                                !android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()
                            ) {
                                Text("Correo inválido", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    )

                    var passwordVisible by remember { mutableStateOf(false) }
                    var confirmVisible by remember { mutableStateOf(false) }

                    OutlinedTextField(
                        value = state.password,
                        onValueChange = vm::onPasswordChange,
                        label = { Text("Contraseña") },
                        placeholder = { Text("••••••••") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = "Icono de contraseña")
                        },
                        trailingIcon = {
                            val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            val desc = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(icon, contentDescription = desc)
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = state.password.isNotEmpty() && state.password.length < 6,
                        supportingText = {
                            if (state.password.isNotEmpty() && state.password.length < 6) {
                                Text("Mínimo 6 caracteres", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    )


                    OutlinedTextField(
                        value = state.confirm,
                        onValueChange = vm::onConfirmChange,
                        label = { Text("Confirmar contraseña") },
                        placeholder = { Text("••••••••") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = "Icono de confirmación")
                        },
                        trailingIcon = {
                            val icon = if (confirmVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            val desc = if (confirmVisible) "Ocultar contraseña" else "Mostrar contraseña"
                            IconButton(onClick = { confirmVisible = !confirmVisible }) {
                                Icon(icon, contentDescription = desc)
                            }
                        },
                        visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = state.confirm.isNotEmpty() &&
                                state.password.isNotEmpty() &&
                                state.confirm != state.password,
                        supportingText = {
                            if (state.confirm.isNotEmpty() &&
                                state.password.isNotEmpty() &&
                                state.confirm != state.password
                            ) {
                                Text("Las contraseñas no coinciden", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    )


                    if (state.error != null) {
                        Text(state.error!!, color = MaterialTheme.colorScheme.error)
                    }

                    Button(
                        onClick = vm::submit,
                        enabled = !state.loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(if (state.loading) "Creando cuenta..." else "Crear cuenta")
                    }

                    TextButton(onClick = onBack) {
                        Text("Ya tengo una cuenta", color = MaterialTheme.colorScheme.primary)
                    }
                }

                if (state.loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
