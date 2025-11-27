package ui.ExploreRoutines

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import data.RoutineExercise
import data.WorkoutRoutine
import ui.CreateRoutine.CreateRoutineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreRoutinesScreen(
    vm: ExploreRoutinesViewModel,
    navController: NavController
) {
    val vm: ExploreRoutinesViewModel = viewModel()
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar mensajes del ViewModel
    LaunchedEffect(vm.error) {
        vm.error?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearError()
        }
    }

    MaterialTheme(colorScheme = lightColorScheme()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Explorar Rutinas",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    actions = {
                        IconButton(
                            onClick = { vm.refreshRoutines() },
                            enabled = !vm.isLoading
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Actualizar"
                            )
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
//            floatingActionButton = {
//                AnimatedVisibility(
//                    visible = !vm.isLoading && vm.error == null,
//                    enter = scaleIn() + fadeIn(),
//                    exit = scaleOut() + fadeOut()
//                ) {
//                    ExtendedFloatingActionButton(
//                        onClick = { navController.navigate("createRoutine") },
//                        icon = { Icon(Icons.Default.Add, "Crear rutina") },
//                        text = { Text("Crear Rutina") },
//                        containerColor = MaterialTheme.colorScheme.primary
//                    )
//                }
//            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                // Estado de carga
                AnimatedVisibility(
                    visible = vm.isLoading,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    LoadingRoutinesView()
                }

                // Estado de error
                AnimatedVisibility(
                    visible = vm.error != null && !vm.isLoading,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    ErrorRoutinesView(vm)
                }

                // Estado vacío
                AnimatedVisibility(
                    visible = vm.filteredRoutines.isEmpty() && vm.error == null && !vm.isLoading,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    EmptyRoutinesView(vm)
                }

                // Lista de rutinas
                AnimatedVisibility(
                    visible = vm.filteredRoutines.isNotEmpty() && !vm.isLoading,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    RoutinesListView(vm)
                }

                // Dialog de detalle de rutina
                if (vm.showRoutineDetail) {
                    RoutineDetailDialog(vm)
                }
            }
        }
    }
}

@Composable
fun LoadingRoutinesView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icono animado
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val scaleAnimation by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )

        Box(
            modifier = Modifier
                .size(120.dp)
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
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(50.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Buscando rutinas...",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Cargando las mejores rutinas para ti",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun ErrorRoutinesView(vm: ExploreRoutinesViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(50.dp),
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Error al cargar",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = vm.error ?: "Ha ocurrido un error inesperado",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { vm.refreshRoutines() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reintentar", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun EmptyRoutinesView(vm: ExploreRoutinesViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
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

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "No hay rutinas disponibles",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Sé el primero en crear una rutina y compartirla con la comunidad",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = { /* Navegar a crear rutina */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Crear Primera Rutina", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun RoutinesListView(vm: ExploreRoutinesViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header con contador y filtros
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Rutinas Disponibles",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${vm.filteredRoutines.size} rutina${if (vm.filteredRoutines.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            FilledTonalButton(
                onClick = { /* Abrir filtros avanzados */ }
            ) {
                Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Filtros")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Barra de búsqueda
        OutlinedTextField(
            value = vm.searchQuery,
            onValueChange = { vm.updateSearchQuery(it) },
            label = { Text("Buscar rutinas...") },
            placeholder = { Text("Ej: Rutina full body") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (vm.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { vm.updateSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de rutinas
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(
                items = vm.filteredRoutines,
                key = { it.id ?: 0 }
            ) { routine ->
                RoutineCard(
                    routine = routine,
                    onViewDetails = { vm.showRoutineDetails(routine) },
                    onStartRoutine = { vm.startRoutine(routine) },
                    onSaveRoutine = { vm.saveRoutine(routine) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineCard(
    routine: WorkoutRoutine,
    onViewDetails: () -> Unit,
    onStartRoutine: () -> Unit,
    onSaveRoutine: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        onClick = onViewDetails,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header con nombre y botón de favorito
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = routine.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    routine.description?.let { description ->
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = onSaveRoutine,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = "Guardar rutina")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Estadísticas de la rutina
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RoutineStatChip(
                    icon = Icons.Default.FitnessCenter,
                    label = "Unknown"
                )
                RoutineStatChip(
                    icon = Icons.Default.Schedule,
                    label = routine.duration ?: "Unknown"
                )
                RoutineStatChip(
                    icon = Icons.Default.List,
                    label = "${routine.exercises?.size ?: 0} ejercicios"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de acción
//            Button(
//                onClick = onStartRoutine,
//                modifier = Modifier.fillMaxWidth(),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = MaterialTheme.colorScheme.primary
//                )
//            ) {
//                Icon(Icons.Default.PlayArrow, contentDescription = "Iniciar rutina", modifier = Modifier.size(18.dp))
//                Spacer(modifier = Modifier.width(8.dp))
//                Text("Comenzar Rutina")
//            }
        }
    }
}

@Composable
fun RoutineStatChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun RoutineDetailDialog(vm: ExploreRoutinesViewModel) {
    Dialog(onDismissRequest = { vm.hideRoutineDetails() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Detalles de la Rutina",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { vm.hideRoutineDetails() }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar")
                        }
                    }

                    vm.selectedRoutine?.let { routine ->
                        Text(
                            routine.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        routine.description?.let { description ->
                            Text(
                                description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                HorizontalDivider()

                // Información de la rutina
                vm.selectedRoutine?.let { routine ->
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        InfoRow(
                            icon = Icons.Default.FitnessCenter,
                            label = "Dificultad: ${"Unknown"}"
                        )
                        InfoRow(
                            icon = Icons.Default.Schedule,
                            label = "Duración: ${routine.duration ?: "Unknown"}"
                        )
                        InfoRow(
                            icon = Icons.Default.List,
                            label = "Ejercicios: ${routine.exercises?.size ?: 0}"
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Lista de ejercicios
                    Text(
                        "Ejercicios incluidos:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        routine.exercises?.forEach { routineExercise ->
                            item {
                                ExerciseItem(routineExercise = routineExercise)
                            }
                        }
                    }
                }

                HorizontalDivider()

                // Botones de acción
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { vm.hideRoutineDetails() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cerrar")
                    }

//                    Button(
//                        onClick = {
//                            vm.selectedRoutine?.let { routine ->
//                                vm.startRoutine(routine)
//                            }
//                            vm.hideRoutineDetails()
//                        },
//                        modifier = Modifier.weight(1f)
//                    ) {
//                        Icon(Icons.Default.PlayArrow, null, Modifier.size(18.dp))
//                        Spacer(Modifier.width(4.dp))
//                        Text("Comenzar")
//                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseItem(routineExercise: RoutineExercise) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = routineExercise.exercise.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${routineExercise.sets} sets × ${routineExercise.reps} reps",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${routineExercise.restTime}s descanso",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}