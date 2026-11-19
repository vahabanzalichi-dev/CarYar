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
import com.vahab.caryar.data.Reminder
import com.vahab.caryar.data.Repository
import com.vahab.caryar.navigation.Screen
import com.vahab.caryar.ui.components.*
import com.vahab.caryar.ui.theme.*
import com.vahab.caryar.util.JalaliDate
import kotlinx.coroutines.launch

@Composable
fun RemindersScreen(navController: NavController) {
    val context = LocalContext.current
    val app = context.applicationContext as CarYarApp
    val repo = remember { Repository(app.database, context) }
    val scope = rememberCoroutineScope()

    val selectedId by repo.selectedVehicleId.collectAsState(initial = null)
    val reminders by if (selectedId != null) {
        repo.getReminders(selectedId!!).collectAsState(initial = emptyList())
    } else remember { mutableStateOf(emptyList<Reminder>()) }

    var showDelete by remember { mutableStateOf<Reminder?>(null) }

    Scaffold(
        topBar = {
            CarYarTopBar(title = "یادآورها", actions = {
                IconButton(onClick = { navController.navigate("reminder_edit") }) {
                    Icon(Icons.Default.Add, null, tint = AccentTeal)
                }
            })
        },
        bottomBar = { BottomNavBar(navController, Screen.Reminders.route) },
        floatingActionButton = {
            if (selectedId != null) {
                FloatingActionButton(onClick = { navController.navigate("reminder_edit") }, containerColor = AccentTeal) {
                    Icon(Icons.Default.Add, null, tint = androidx.compose.ui.graphics.Color.White)
                }
            }
        },
        containerColor = GraphiteBlack
    ) { padding ->
        when {
            selectedId == null -> Box(Modifier.fillMaxSize().padding(padding)) {
                EmptyState("ابتدا یک خودرو انتخاب کنید.", "رفتن به خودروها") { navController.navigate(Screen.Vehicles.route) }
            }
            reminders.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding)) {
                EmptyState("یادآوری ثبت نشده.", "افزودن یادآور") { navController.navigate("reminder_edit") }
            }
            else -> {
                LazyColumn(
                    Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(reminders, key = { it.id }) { r ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                Row {
                                    Text(r.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                    if (!r.isActive) {
                                        Text("غیرفعال", style = MaterialTheme.typography.labelMedium, color = TextHint)
                                    }
                                }
                                Spacer(Modifier.height(6.dp))
                                if (!r.dueDateJalali.isNullOrBlank()) {
                                    Text("تاریخ: ${JalaliDate.formatDisplay(r.dueDateJalali)}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                }
                                if (r.dueOdometer != null) {
                                    Text("کیلومتر: ${formatOdometer(r.dueOdometer)}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                }
                                if (r.notes.isNotBlank()) {
                                    Text(r.notes, style = MaterialTheme.typography.bodyMedium, color = TextHint)
                                }
                                Row {
                                    TextButton(onClick = { navController.navigate("reminder_edit?id=${r.id}") }) {
                                        Icon(Icons.Default.Edit, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("ویرایش")
                                    }
                                    TextButton(onClick = { showDelete = r }) {
                                        Icon(Icons.Default.Delete, null, Modifier.size(16.dp), tint = ErrorRed); Spacer(Modifier.width(4.dp)); Text("حذف", color = ErrorRed)
                                    }
                                    TextButton(onClick = {
                                        scope.launch { repo.updateReminder(r.copy(isActive = !r.isActive)) }
                                    }) {
                                        Text(if (r.isActive) "غیرفعال کردن" else "فعال کردن")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    showDelete?.let { r ->
        ConfirmDeleteDialog("حذف یادآور", "این یادآور حذف شود؟", {
            scope.launch { repo.deleteReminder(r); showDelete = null }
        }, { showDelete = null })
    }
}
