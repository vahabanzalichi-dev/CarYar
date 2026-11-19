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
import com.vahab.caryar.data.Expense
import com.vahab.caryar.data.ExpenseCategories
import com.vahab.caryar.data.Repository
import com.vahab.caryar.ui.components.*
import com.vahab.caryar.ui.theme.*
import com.vahab.caryar.util.JalaliDate
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEditScreen(navController: NavController, expenseId: Long?) {
    val context = LocalContext.current
    val app = context.applicationContext as CarYarApp
    val repo = remember { Repository(app.database, context) }
    val scope = rememberCoroutineScope()
    val selectedId by repo.selectedVehicleId.collectAsState(initial = null)

    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(JalaliDate.today()) }
    var odometer by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ExpenseCategories.list.first()) }
    var notes by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(expenseId) {
        if (expenseId != null) {
            val e = app.database.expenseDao().getById(expenseId)
            if (e != null) {
                title = e.title
                amount = e.amount.toString()
                date = e.dateJalali
                odometer = e.odometer?.toString() ?: ""
                category = e.category
                notes = e.notes
            }
        }
    }

    Scaffold(
        topBar = {
            CarYarTopBar(
                title = if (expenseId == null) "ثبت هزینه" else "ویرایش هزینه",
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
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان *") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = textFieldColors())
            OutlinedTextField(value = amount, onValueChange = { amount = it.filter { c -> c.isDigit() } }, label = { Text("مبلغ (تومان) *") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = textFieldColors())
            OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("تاریخ شمسی") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = textFieldColors())
            OutlinedTextField(value = odometer, onValueChange = { odometer = it.filter { c -> c.isDigit() } }, label = { Text("کیلومتر (اختیاری)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = textFieldColors())

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = category, onValueChange = {}, readOnly = true,
                    label = { Text("دسته‌بندی") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = textFieldColors()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    ExpenseCategories.list.forEach {
                        DropdownMenuItem(text = { Text(it) }, onClick = { category = it; expanded = false })
                    }
                }
            }

            OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("توضیحات") }, modifier = Modifier.fillMaxWidth().height(80.dp), colors = textFieldColors())

            PrimaryButton(
                text = if (expenseId == null) "ذخیره هزینه" else "بروزرسانی",
                onClick = {
                    if (title.isBlank() || amount.isBlank()) return@PrimaryButton
                    scope.launch {
                        val e = Expense(
                            id = expenseId ?: 0,
                            vehicleId = selectedId!!,
                            title = title.trim(),
                            amount = amount.toLongOrNull() ?: 0,
                            dateJalali = date.trim(),
                            odometer = odometer.toIntOrNull(),
                            category = category,
                            notes = notes.trim()
                        )
                        if (expenseId == null) repo.insertExpense(e) else repo.updateExpense(e)
                        navController.popBackStack()
                    }
                },
                enabled = title.isNotBlank() && amount.isNotBlank()
            )
        }
    }
}
