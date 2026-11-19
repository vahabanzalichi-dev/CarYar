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
import com.vahab.caryar.data.Repository
import com.vahab.caryar.data.ServiceCategories
import com.vahab.caryar.data.ServiceRecord
import com.vahab.caryar.ui.components.*
import com.vahab.caryar.ui.theme.*
import com.vahab.caryar.util.JalaliDate
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceEditScreen(navController: NavController, serviceId: Long?) {
    val context = LocalContext.current
    val app = context.applicationContext as CarYarApp
    val repo = remember { Repository(app.database, context) }
    val scope = rememberCoroutineScope()

    val selectedId by repo.selectedVehicleId.collectAsState(initial = null)

    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ServiceCategories.list.first()) }
    var date by remember { mutableStateOf(JalaliDate.today()) }
    var odometer by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var workshop by remember { mutableStateOf("") }
    var parts by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var nextOdo by remember { mutableStateOf("") }
    var nextDate by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(serviceId != null) }

    LaunchedEffect(serviceId) {
        if (serviceId != null) {
            val s = app.database.serviceDao().getById(serviceId)
            if (s != null) {
                title = s.title
                category = s.category
                date = s.dateJalali
                odometer = s.odometer.toString()
                cost = if (s.cost > 0) s.cost.toString() else ""
                workshop = s.workshop
                parts = s.partsReplaced
                notes = s.notes
                nextOdo = s.nextServiceOdometer?.toString() ?: ""
                nextDate = s.nextServiceDateJalali ?: ""
            }
            isLoading = false
        } else {
            // prefill odometer from vehicle
            selectedId?.let { vid ->
                val v = repo.getVehicle(vid)
                if (v != null) odometer = v.currentOdometer.toString()
            }
        }
    }

    Scaffold(
        topBar = {
            CarYarTopBar(
                title = if (serviceId == null) "ثبت سرویس / تعمیر" else "ویرایش سرویس",
                onBack = { navController.popBackStack() }
            )
        },
        containerColor = GraphiteBlack
    ) { padding ->
        if (selectedId == null) {
            Box(Modifier.fillMaxSize().padding(padding)) {
                EmptyState("ابتدا خودرو انتخاب کنید.")
            }
            return@Scaffold
        }
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان *") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = textFieldColors())
            
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("دسته‌بندی") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = textFieldColors()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    ServiceCategories.list.forEach {
                        DropdownMenuItem(text = { Text(it) }, onClick = { category = it; expanded = false })
                    }
                }
            }

            OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("تاریخ شمسی (1403/08/20)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = textFieldColors())
            OutlinedTextField(value = odometer, onValueChange = { odometer = it.filter { c -> c.isDigit() } }, label = { Text("کیلومتر *") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = textFieldColors())
            OutlinedTextField(value = cost, onValueChange = { cost = it.filter { c -> c.isDigit() } }, label = { Text("هزینه (تومان)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = textFieldColors())
            OutlinedTextField(value = workshop, onValueChange = { workshop = it }, label = { Text("نام تعمیرگاه") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = textFieldColors())
            OutlinedTextField(value = parts, onValueChange = { parts = it }, label = { Text("قطعات تعویض‌شده") }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors())
            OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("توضیحات") }, modifier = Modifier.fillMaxWidth().height(80.dp), colors = textFieldColors())
            OutlinedTextField(value = nextOdo, onValueChange = { nextOdo = it.filter { c -> c.isDigit() } }, label = { Text("سرویس بعدی (کیلومتر)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = textFieldColors())
            OutlinedTextField(value = nextDate, onValueChange = { nextDate = it }, label = { Text("سرویس بعدی (تاریخ شمسی)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = textFieldColors())

            PrimaryButton(
                text = if (serviceId == null) "ذخیره" else "بروزرسانی",
                onClick = {
                    if (title.isBlank() || odometer.isBlank()) return@PrimaryButton
                    scope.launch {
                        val record = ServiceRecord(
                            id = serviceId ?: 0,
                            vehicleId = selectedId!!,
                            title = title.trim(),
                            category = category,
                            dateJalali = date.trim(),
                            odometer = odometer.toIntOrNull() ?: 0,
                            cost = cost.toLongOrNull() ?: 0,
                            workshop = workshop.trim(),
                            partsReplaced = parts.trim(),
                            notes = notes.trim(),
                            nextServiceOdometer = nextOdo.toIntOrNull(),
                            nextServiceDateJalali = nextDate.trim().ifBlank { null }
                        )
                        if (serviceId == null) repo.insertService(record)
                        else repo.updateService(record)
                        // update vehicle odometer if higher
                        val v = repo.getVehicle(selectedId!!)
                        if (v != null && record.odometer > v.currentOdometer) {
                            repo.updateVehicle(v.copy(currentOdometer = record.odometer, updatedAt = System.currentTimeMillis()))
                        }
                        navController.popBackStack()
                    }
                },
                enabled = title.isNotBlank() && odometer.isNotBlank()
            )
        }
    }
}
