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
import com.vahab.caryar.data.Vehicle
import com.vahab.caryar.ui.components.*
import com.vahab.caryar.ui.theme.*
import com.vahab.caryar.util.JalaliDate
import kotlinx.coroutines.launch

@Composable
fun VehicleEditScreen(navController: NavController, vehicleId: Long?) {
    val context = LocalContext.current
    val app = context.applicationContext as CarYarApp
    val repo = remember { Repository(app.database, context) }
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var trim by remember { mutableStateOf("") }
    var plate by remember { mutableStateOf("") }
    var odometer by remember { mutableStateOf("") }
    var purchaseDate by remember { mutableStateOf(JalaliDate.today()) }
    var purchasePrice by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(vehicleId != null) }

    LaunchedEffect(vehicleId) {
        if (vehicleId != null) {
            val v = repo.getVehicle(vehicleId)
            if (v != null) {
                name = v.name
                brand = v.brand
                model = v.model
                year = v.year?.toString() ?: ""
                trim = v.trim
                plate = v.plateNumber
                odometer = v.currentOdometer.toString()
                purchaseDate = v.purchaseDate.ifBlank { JalaliDate.today() }
                purchasePrice = v.purchasePrice?.toString() ?: ""
                notes = v.notes
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CarYarTopBar(
                title = if (vehicleId == null) "افزودن خودرو" else "ویرایش خودرو",
                onBack = { navController.popBackStack() }
            )
        },
        containerColor = GraphiteBlack
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator(color = AccentTeal)
            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("نام دلخواه خودرو *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors()
                )
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("برند") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors()
                )
                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("مدل") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors()
                )
                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it.filter { c -> c.isDigit() }.take(4) },
                    label = { Text("سال تولید") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = textFieldColors()
                )
                OutlinedTextField(
                    value = trim,
                    onValueChange = { trim = it },
                    label = { Text("تیپ") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors()
                )
                OutlinedTextField(
                    value = plate,
                    onValueChange = { plate = it },
                    label = { Text("شماره پلاک (اختیاری)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors()
                )
                OutlinedTextField(
                    value = odometer,
                    onValueChange = { odometer = it.filter { c -> c.isDigit() } },
                    label = { Text("کیلومتر فعلی *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = textFieldColors()
                )
                OutlinedTextField(
                    value = purchaseDate,
                    onValueChange = { purchaseDate = it },
                    label = { Text("تاریخ خرید (مثال: 1402/05/15)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors()
                )
                OutlinedTextField(
                    value = purchasePrice,
                    onValueChange = { purchasePrice = it.filter { c -> c.isDigit() } },
                    label = { Text("قیمت خرید (تومان)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = textFieldColors()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("توضیحات") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    colors = textFieldColors()
                )

                Spacer(Modifier.height(8.dp))
                PrimaryButton(
                    text = if (vehicleId == null) "ذخیره خودرو" else "بروزرسانی",
                    onClick = {
                        if (name.isBlank()) return@PrimaryButton
                        val odo = odometer.toIntOrNull() ?: 0
                        scope.launch {
                            if (vehicleId == null) {
                                val id = repo.insertVehicle(
                                    Vehicle(
                                        name = name.trim(),
                                        brand = brand.trim(),
                                        model = model.trim(),
                                        year = year.toIntOrNull(),
                                        trim = trim.trim(),
                                        plateNumber = plate.trim(),
                                        currentOdometer = odo,
                                        purchaseDate = purchaseDate.trim(),
                                        purchasePrice = purchasePrice.toLongOrNull(),
                                        notes = notes.trim()
                                    )
                                )
                                repo.setSelectedVehicleId(id)
                            } else {
                                val existing = repo.getVehicle(vehicleId)
                                if (existing != null) {
                                    repo.updateVehicle(
                                        existing.copy(
                                            name = name.trim(),
                                            brand = brand.trim(),
                                            model = model.trim(),
                                            year = year.toIntOrNull(),
                                            trim = trim.trim(),
                                            plateNumber = plate.trim(),
                                            currentOdometer = odo,
                                            purchaseDate = purchaseDate.trim(),
                                            purchasePrice = purchasePrice.toLongOrNull(),
                                            notes = notes.trim(),
                                            updatedAt = System.currentTimeMillis()
                                        )
                                    )
                                }
                            }
                            navController.popBackStack()
                        }
                    },
                    enabled = name.isNotBlank()
                )
            }
        }
    }
}

@Composable
fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AccentTeal,
    unfocusedBorderColor = DividerColor,
    focusedLabelColor = AccentTeal,
    unfocusedLabelColor = TextHint,
    cursorColor = AccentTeal,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary
)
