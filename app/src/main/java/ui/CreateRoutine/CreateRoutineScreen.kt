package ui.CreateRoutine

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import data.Exercise
import data.RoutineExercise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoutineScreen(
    vm: CreateRoutineViewModel,
    onBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar mensajes del ViewModel
    LaunchedEffect(vm.message) {
        vm.message?.let {
            snackbarHostState.showSnackbar(it)
            vm.messageConsumed()
        }
    }

    // Ejecutar filtros cuando cambien los parámetros
    LaunchedEffect(vm.searchQuery, vm.selectedMuscle, vm.selectedType) {
        vm.filterExercises()
    }

    MaterialTheme(colorScheme = darkColorScheme()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Crear Rutina") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {

                // Pantalla vacía inicial
                AnimatedVisibility(
                    visible = vm.showEmptyState,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    EmptyRoutineScreen(vm)
                }

                // Pantalla de selección de ejercicios
                AnimatedVisibility(
                    visible = vm.showExerciseSelection,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    ExerciseSelectionContent(vm)
                }

                // Dialog para agregar sets/reps
                if (vm.showExerciseDialog) {
                    AddExerciseDialog(vm)
                }
            }
        }
    }
}

@Composable
fun EmptyRoutineScreen(vm: CreateRoutineViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.FitnessCenter,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Aún no has agregado ejercicios",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { vm.showAddExerciseScreen() }) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Agregar ejercicio")
        }
    }
}

@Composable
private fun ExerciseSelectionContent(vm: CreateRoutineViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Seleccionar ejercicios",
                style = MaterialTheme.typography.titleLarge
            )
            TextButton(onClick = { vm.finishExerciseSelection() }) {
                Text("Listo")
            }
        }

        ExerciseFilters(vm)

        if (vm.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            if (vm.filteredExercises.isEmpty()) {
                Text(
                    "No se encontraron ejercicios",
                    modifier = Modifier.padding(top = 32.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(vm.filteredExercises) { exercise ->
                        AvailableExerciseItem(exercise, vm)
                    }
                }
            }
        }

    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseFilters(vm: CreateRoutineViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Filtros", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = vm.searchQuery,
                onValueChange = { vm.searchQuery = it },
                label = { Text("Buscar ejercicio") },
                singleLine = true,
                trailingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterDropdown(
                    label = "Músculo",
                    selected = vm.selectedMuscle ?: "Todos",
                    items = vm.getUniqueMuscles(),
                    onSelect = { vm.selectedMuscle = it },
                    modifier = Modifier.weight(1f)
                )

                FilterDropdown(
                    label = "Tipo",
                    selected = vm.selectedType ?: "Todos",
                    items = vm.getUniqueTypes(),
                    onSelect = { vm.selectedType = it },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdown(
    label: String,
    selected: String,
    items: List<String>,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = modifier.menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Todos") },
                onClick = {
                    onSelect(null)
                    expanded = false
                }
            )
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onSelect(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun AvailableExerciseItem(exercise: Exercise, vm: CreateRoutineViewModel) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        onClick = { vm.openExerciseDialog(exercise) }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(exercise.name, style = MaterialTheme.typography.titleMedium)
            Text(
                "${exercise.muscle} • ${exercise.type}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Dificultad: ${exercise.difficulty} • Equipo: ${exercise.equipment}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AddExerciseDialog(vm: CreateRoutineViewModel) {
    Dialog(onDismissRequest = { vm.closeExerciseDialog() }) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Agregar ejercicio", style = MaterialTheme.typography.titleLarge)

                vm.currentExercise?.let { exercise ->
                    Text(exercise.name, style = MaterialTheme.typography.titleMedium)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = vm.sets,
                        onValueChange = { vm.sets = it },
                        label = { Text("Sets") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = vm.reps,
                        onValueChange = { vm.reps = it },
                        label = { Text("Reps") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = vm.restTime,
                        onValueChange = { vm.restTime = it },
                        label = { Text("Descanso (s)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(
                        onClick = { vm.closeExerciseDialog() },
                        modifier = Modifier.weight(1f)
                    ) { Text("Cancelar") }

                    Button(
                        onClick = { vm.addExerciseToRoutine() },
                        modifier = Modifier.weight(1f)
                    ) { Text("Agregar") }
                }
            }
        }
    }
}
