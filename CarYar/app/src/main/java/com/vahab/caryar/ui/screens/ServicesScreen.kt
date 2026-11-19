package com.vahab.caryar.ui.screens

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.vahab.caryar.CarYarApp
import com.vahab.caryar.data.Repository
import com.vahab.caryar.data.ServiceRecord
import com.vahab.caryar.navigation.Screen
import com.vahab.caryar.ui.components.*
import com.vahab.caryar.ui.theme.*
import com.vahab.caryar.util.JalaliDate
import kotlinx.coroutines.launch

@Composable
fun ServicesScreen(navController: NavController) {
    val context = LocalContext.current
    val app = context.applicationContext as CarYarApp
    val repo = remember { Repository(app.database, context) }
    val scope = rememberCoroutineScope()

    val selectedId by repo.selectedVehicleId.collectAsState(initial = null)
    val services by if (selectedId != null) {
        repo.getServices(selectedId!!).collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(emptyList<ServiceRecord>()) }
    }
    var showDelete by remember { mutableStateOf<ServiceRecord?>(null) }

    Scaffold(
        topBar = {
            CarYarTopBar(
                title = "تعمیرات و سرویس",
                actions = {
                    IconButton(onClick = { navController.navigate("service_edit") }) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = AccentTeal)
                    }
                }
            )
        },
        bottomBar = { BottomNavBar(navController, Screen.Services.route) },
        floatingActionButton = {
            if (selectedId != null) {
                FloatingActionButton(
                    onClick = { navController.navigate("service_edit") },
                    containerColor = AccentTeal
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White)
                }
            }
        },
        containerColor = GraphiteBlack
    ) { padding ->
        when {
            selectedId == null -> {
                Box(Modifier.fillMaxSize().padding(padding)) {
                    EmptyState("ابتدا یک خودرو انتخاب یا ثبت کنید.", "رفتن به خودروها") {
                        navController.navigate(Screen.Vehicles.route)
                    }
                }
            }
            services.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding)) {
                    EmptyState("هنوز سرویسی ثبت نشده.", "ثبت اولین سرویس") {
                        navController.navigate("service_edit")
                    }
                }
            }
            else -> {
                LazyColumn(
                    Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(services, key = { it.id }) { s ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                Text(s.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                Text(s.category, style = MaterialTheme.typography.bodyMedium, color = AccentTeal)
                                Spacer(Modifier.height(6.dp))
                                Text("${JalaliDate.formatDisplay(s.dateJalali)}  •  ${formatOdometer(s.odometer)}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                if (s.cost > 0) {
                                    Text(formatToman(s.cost), style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                }
                                Row {
                                    TextButton(onClick = { navController.navigate("service_edit?id=${s.id}") }) {
                                        Icon(Icons.Default.Edit, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("ویرایش")
                                    }
                                    TextButton(onClick = { showDelete = s }) {
                                        Icon(Icons.Default.Delete, null, Modifier.size(16.dp), tint = ErrorRed); Spacer(Modifier.width(4.dp)); Text("حذف", color = ErrorRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    showDelete?.let { s ->
        ConfirmDeleteDialog("حذف سرویس", "این رکورد حذف شود؟", {
            scope.launch { repo.deleteService(s); showDelete = null }
        }, { showDelete = null })
    }
}
