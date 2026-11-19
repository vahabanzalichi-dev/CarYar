package com.vahab.caryar.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.vahab.caryar.CarYarApp
import com.vahab.caryar.data.Repository
import com.vahab.caryar.data.Vehicle
import com.vahab.caryar.navigation.Screen
import com.vahab.caryar.ui.components.*
import com.vahab.caryar.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun VehiclesScreen(navController: NavController) {
    val context = LocalContext.current
    val app = context.applicationContext as CarYarApp
    val repo = remember { Repository(app.database, context) }
    val scope = rememberCoroutineScope()

    val vehicles by repo.vehicles.collectAsState(initial = emptyList())
    val selectedId by repo.selectedVehicleId.collectAsState(initial = null)
    var showDeleteDialog by remember { mutableStateOf<Vehicle?>(null) }

    Scaffold(
        topBar = {
            CarYarTopBar(
                title = "پرونده خودرو",
                actions = {
                    IconButton(onClick = { navController.navigate("vehicle_edit") }) {
                        Icon(Icons.Default.Add, contentDescription = "افزودن", tint = AccentTeal)
                    }
                }
            )
        },
        bottomBar = { BottomNavBar(navController, Screen.Vehicles.route) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("vehicle_edit") },
                containerColor = AccentTeal,
                contentColor = androidx.compose.ui.graphics.Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "افزودن خودرو")
            }
        },
        containerColor = GraphiteBlack
    ) { padding ->
        if (vehicles.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding)) {
                EmptyState(
                    message = "هنوز خودرویی ثبت نشده است.",
                    actionText = "افزودن اولین خودرو",
                    onAction = { navController.navigate("vehicle_edit") }
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(vehicles, key = { it.id }) { vehicle ->
                    VehicleCard(
                        vehicle = vehicle,
                        isSelected = vehicle.id == selectedId,
                        onClick = {
                            scope.launch { repo.setSelectedVehicleId(vehicle.id) }
                        },
                        onEdit = { navController.navigate("vehicle_edit?id=${vehicle.id}") },
                        onDelete = { showDeleteDialog = vehicle }
                    )
                }
            }
        }
    }

    showDeleteDialog?.let { vehicle ->
        ConfirmDeleteDialog(
            title = "حذف خودرو",
            message = "آیا از حذف «${vehicle.name}» مطمئن هستید؟ تمام سوابق مرتبط نیز حذف می‌شوند.",
            onConfirm = {
                scope.launch {
                    repo.deleteVehicle(vehicle)
                    if (selectedId == vehicle.id) {
                        val remaining = vehicles.filter { it.id != vehicle.id }
                        repo.setSelectedVehicleId(remaining.firstOrNull()?.id)
                    }
                    showDeleteDialog = null
                }
            },
            onDismiss = { showDeleteDialog = null }
        )
    }
}

@Composable
fun VehicleCard(
    vehicle: Vehicle,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) ElevatedSurface else CardBackground
        ),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        vehicle.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        listOfNotNull(vehicle.brand, vehicle.model, vehicle.year?.toString())
                            .filter { it.isNotBlank() }
                            .joinToString(" • "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                if (isSelected) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AccentTeal.copy(alpha = 0.2f)
                    ) {
                        Text(
                            "فعال",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = AccentTeal
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                formatOdometer(vehicle.currentOdometer),
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("ویرایش")
                }
                TextButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp), tint = ErrorRed)
                    Spacer(Modifier.width(4.dp))
                    Text("حذف", color = ErrorRed)
                }
            }
        }
    }
}
