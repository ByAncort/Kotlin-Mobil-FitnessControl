package ui.login

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    vm: LoginViewModel = viewModel()
) {
    val state by vm.ui.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Animación de rebote INFINITO
    val infiniteTransition = rememberInfiniteTransition(label = "infinite_bounce")
    val bounceScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 800,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fast_bounce"
    )
    LaunchedEffect(state.loggedIn) {
        if (state.loggedIn) onLoginSuccess()
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            vm.messageConsumed()
        }
    }

    MaterialTheme(colorScheme = darkColorScheme()) {
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
                    // 🔹 Icono con animación de rebote INFINITO
                    val scaleAnimation by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.5f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "scale"
                    )

                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .scale(scaleAnimation)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.surface
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            modifier = Modifier.size(70.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }


                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "Bienvenido",
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    OutlinedTextField(
                        value = state.email,
                        onValueChange = vm::onEmailChange,
                        label = { Text("Correo electrónico") },
                        placeholder = { Text("ejemplo@correo.com") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { /* Acción al presionar siguiente, por ejemplo, mover el foco */ }
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Icono de correo"
                            )
                        },
                        isError = state.email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches(),
                        supportingText = {
                            if (state.email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
                                Text("Correo inválido", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    )

                    var passwordVisible by remember { mutableStateOf(false) }

                    OutlinedTextField(
                        value = state.password,
                        onValueChange = vm::onPasswordChange,
                        label = { Text("Contraseña") },
                        placeholder = { Text("••••••••") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { /* Acción al presionar “Done”, por ejemplo, enviar formulario */ }
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Icono de contraseña"
                            )
                        },
                        trailingIcon = {
                            val visibilityIcon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            val description = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = visibilityIcon, contentDescription = description)
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = state.password.isNotEmpty() && state.password.length < 6,
                        supportingText = {
                            if (state.password.isNotEmpty() && state.password.length < 6) {
                                Text("La contraseña debe tener al menos 6 caracteres", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    )


                    if (state.error != null) {
                        Text(
                            text = state.error!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))


                    Button(
                        onClick = vm::submit,
                        enabled = !state.loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (state.loading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .padding(end = 8.dp)
                                )
                                Text("Ingresando...")
                            }
                        } else {
                            Text("Ingresar")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = onRegister,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            "Crear cuenta",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
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