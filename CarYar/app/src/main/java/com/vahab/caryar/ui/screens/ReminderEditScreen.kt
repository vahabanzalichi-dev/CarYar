package com.vahab.caryar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.vahab.caryar.CarYarApp
import com.vahab.caryar.data.Reminder
import com.vahab.caryar.data.Repository
import com.vahab.caryar.ui.components.*
import com.vahab.caryar.ui.theme.*
import com.vahab.caryar.util.JalaliDate
import kotlinx.coroutines.launch

@Composable
fun ReminderEditScreen(navController: NavController, reminderId: Long?) {
    val context = LocalContext.current
    val app = context.applicationContext as CarYarApp
    val repo = remember { Repository(app.database, context) }
    val scope = rememberCoroutineScope()
    val selectedId by repo.selectedVehicleId.collectAsState(initial = null)

    var title by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var dueOdo by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(true) }

    LaunchedEffect(reminderId) {
        if (reminderId != null) {
            val r = app.database.reminderDao().getById(reminderId)
            if (r != null) {
                title = r.title
                dueDate = r.dueDateJalali ?: ""
                dueOdo = r.dueOdometer?.toString() ?: ""
                notes = r.notes
                isActive = r.isActive
            }
        }
    }

    Scaffold(
        topBar = {
            CarYarTopBar(
                title = if (reminderId == null) "یادآور جدید" else "ویرایش یادآور",
                onBack = { navController.popBackStack() }
            )
        },
        containerColor = GraphiteBlack
    ) { padding ->
        if (selectedId == null) {
            Box(Modifier.fillMaxSize().padding(padding)) { EmptyState("ابتدا خودرو انتخاب کنید.") }
            return@Scaffold
        }
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان یادآور *") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = textFieldColors())
            OutlinedTextField(value = dueDate, onValueChange = { dueDate = it }, label = { Text("تاریخ هدف (شمسی - اختیاری)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = textFieldColors())
            OutlinedTextField(value = dueOdo, onValueChange = { dueOdo = it.filter { c -> c.isDigit() } }, label = { Text("کیلومتر هدف (اختیاری)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = textFieldColors())
            OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("توضیحات") }, modifier = Modifier.fillMaxWidth().height(80.dp), colors = textFieldColors())
            
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("فعال", color = TextPrimary, modifier = Modifier.weight(1f))
                Switch(checked = isActive, onCheckedChange = { isActive = it }, colors = SwitchDefaults.colors(checkedTrackColor = AccentTeal))
            }

            Text("حداقل یکی از تاریخ یا کیلومتر را وارد کنید.", style = MaterialTheme.typography.bodyMedium, color = TextHint)

            PrimaryButton(
                text = if (reminderId == null) "ذخیره یادآور" else "بروزرسانی",
                onClick = {
                    if (title.isBlank()) return@PrimaryButton
                    if (dueDate.isBlank() && dueOdo.isBlank()) return@PrimaryButton
                    scope.launch {
                        val r = Reminder(
                            id = reminderId ?: 0,
                            vehicleId = selectedId!!,
                            title = title.trim(),
                            dueDateJalali = dueDate.trim().ifBlank { null },
                            dueOdometer = dueOdo.toIntOrNull(),
                            notes = notes.trim(),
                            isActive = isActive
                        )
                        if (reminderId == null) repo.insertReminder(r) else repo.updateReminder(r)
                        navController.popBackStack()
                    }
                },
                enabled = title.isNotBlank() && (dueDate.isNotBlank() || dueOdo.isNotBlank())
            )
        }
    }
}
