package com.vahab.caryar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.vahab.caryar.navigation.Screen
import com.vahab.caryar.ui.components.*
import com.vahab.caryar.ui.theme.*
import com.vahab.caryar.util.JalaliDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(navController: NavController) {
    val context = LocalContext.current
    val app = context.applicationContext as CarYarApp
    val repo = remember { Repository(app.database, context) }
    val scope = rememberCoroutineScope()

    val vehicles by repo.vehicles.collectAsState(initial = emptyList())
    val selectedId by repo.selectedVehicleId.collectAsState(initial = null)

    var currentVehicle by remember { mutableStateOf<com.vahab.caryar.data.Vehicle?>(null) }
    var latestService by remember { mutableStateOf<com.vahab.caryar.data.ServiceRecord?>(null) }
    var monthExpense by remember { mutableStateOf(0L) }
    var activeReminders by remember { mutableStateOf(0) }

    LaunchedEffect(vehicles, selectedId) {
        val vehicle = when {
            selectedId != null -> vehicles.find { it.id == selectedId }
            vehicles.isNotEmpty() -> vehicles.first()
            else -> null
        }
        currentVehicle = vehicle
        if (vehicle != null) {
            if (selectedId == null) repo.setSelectedVehicleId(vehicle.id)
            latestService = repo.getLatestService(vehicle.id)
            monthExpense = repo.sumExpensesMonth(vehicle.id, JalaliDate.currentYearMonth())
            activeReminders = repo.countActiveReminders(vehicle.id)
        } else {
            latestService = null
            monthExpense = 0
            activeReminders = 0
        }
    }

    Scaffold(
        topBar = {
            CarYarTopBar(title = "ماشین‌یار")
        },
        bottomBar = {
            BottomNavBar(navController, Screen.Dashboard.route)
        },
        containerColor = GraphiteBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "پرونده هوشمند خودرو",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(Modifier.height(16.dp))

            if (currentVehicle == null) {
                EmptyState(
                    message = "هنوز خودرویی ثبت نکرده‌اید.\nاول یک خودرو اضافه کنید تا داشبورد فعال شود.",
                    actionText = "افزودن خودرو",
                    onAction = { navController.navigate("vehicle_edit") }
                )
            } else {
                // Selected vehicle card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = currentVehicle!!.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = listOfNotNull(
                                currentVehicle!!.brand.takeIf { it.isNotBlank() },
                                currentVehicle!!.model.takeIf { it.isNotBlank() },
                                currentVehicle!!.year?.toString()
                            ).joinToString(" • "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = formatOdometer(currentVehicle!!.currentOdometer),
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Stats row
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        title = "هزینه این ماه",
                        value = if (monthExpense > 0) "%,d".format(monthExpense) else "۰",
                        subtitle = "تومان",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "یادآور فعال",
                        value = activeReminders.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Latest service
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("آخرین سرویس", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        Spacer(Modifier.height(8.dp))
                        if (latestService != null) {
                            Text(latestService!!.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Text(
                                "${JalaliDate.formatDisplay(latestService!!.dateJalali)} • ${formatOdometer(latestService!!.odometer)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        } else {
                            Text("هنوز سرویسی ثبت نشده", style = MaterialTheme.typography.bodyMedium, color = TextHint)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Quick actions
                Text("دسترسی سریع", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(Modifier.height(12.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickActionChip("ثبت هزینه", Icons.Default.Payments) {
                        navController.navigate("expense_edit")
                    }
                    QuickActionChip("ثبت تعمیر", Icons.Default.Build) {
                        navController.navigate("service_edit")
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickActionChip("ثبت کیلومتر", Icons.Default.Speed) {
                        // Simple dialog or navigate to edit vehicle for odometer
                        navController.navigate("vehicle_edit?id=${currentVehicle!!.id}")
                    }
                    QuickActionChip("یادآور جدید", Icons.Default.Notifications) {
                        navController.navigate("reminder_edit")
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.QuickActionChip(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = ElevatedSurface,
        modifier = Modifier
            .weight(1f)
            .height(52.dp)
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(text, style = MaterialTheme.typography.labelLarge, color = TextPrimary)
        }
    }
}

@Composable
fun BottomNavBar(navController: NavController, currentRoute: String) {
    NavigationBar(
        containerColor = DarkSurface,
        contentColor = TextPrimary
    ) {
        com.vahab.caryar.navigation.bottomNavItems.forEach { screen ->
            val selected = currentRoute == screen.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        if (selected) screen.selectedIcon else screen.icon,
                        contentDescription = screen.title
                    )
                },
                label = { Text(screen.title, style = MaterialTheme.typography.labelMedium) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AccentTeal,
                    selectedTextColor = AccentTeal,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = ElevatedSurface
                )
            )
        }
    }
}
