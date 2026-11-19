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
import com.vahab.caryar.data.Expense
import com.vahab.caryar.data.Repository
import com.vahab.caryar.navigation.Screen
import com.vahab.caryar.ui.components.*
import com.vahab.caryar.ui.theme.*
import com.vahab.caryar.util.JalaliDate
import kotlinx.coroutines.launch

@Composable
fun ExpensesScreen(navController: NavController) {
    val context = LocalContext.current
    val app = context.applicationContext as CarYarApp
    val repo = remember { Repository(app.database, context) }
    val scope = rememberCoroutineScope()

    val selectedId by repo.selectedVehicleId.collectAsState(initial = null)
    val expenses by if (selectedId != null) {
        repo.getExpenses(selectedId!!).collectAsState(initial = emptyList())
    } else remember { mutableStateOf(emptyList<Expense>()) }

    var monthSum by remember { mutableStateOf(0L) }
    var yearSum by remember { mutableStateOf(0L) }
    var totalSum by remember { mutableStateOf(0L) }
    var showDelete by remember { mutableStateOf<Expense?>(null) }

    LaunchedEffect(selectedId) {
        if (selectedId != null) {
            monthSum = repo.sumExpensesMonth(selectedId!!, JalaliDate.currentYearMonth())
            yearSum = repo.sumExpensesYear(selectedId!!, JalaliDate.currentYear())
            totalSum = repo.sumExpensesTotal(selectedId!!)
        }
    }

    Scaffold(
        topBar = {
            CarYarTopBar(title = "مدیریت هزینه‌ها", actions = {
                IconButton(onClick = { navController.navigate("expense_edit") }) {
                    Icon(Icons.Default.Add, null, tint = AccentTeal)
                }
            })
        },
        bottomBar = { BottomNavBar(navController, Screen.Expenses.route) },
        floatingActionButton = {
            if (selectedId != null) {
                FloatingActionButton(onClick = { navController.navigate("expense_edit") }, containerColor = AccentTeal) {
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
            else -> Column(Modifier.fillMaxSize().padding(padding)) {
                // Summary cards
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard("این ماه", "%,d".format(monthSum), "تومان", Modifier.weight(1f))
                    StatCard("امسال", "%,d".format(yearSum), "تومان", Modifier.weight(1f))
                }
                Text("مجموع کل: ${formatToman(totalSum)}", Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)

                if (expenses.isEmpty()) {
                    EmptyState("هزینه‌ای ثبت نشده.", "ثبت هزینه") { navController.navigate("expense_edit") }
                } else {
                    LazyColumn(
                        Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(expenses, key = { it.id }) { e ->
                            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = CardBackground), modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(14.dp)) {
                                    Text(e.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                    Text(e.category, style = MaterialTheme.typography.bodyMedium, color = AccentTeal)
                                    Text("${JalaliDate.formatDisplay(e.dateJalali)}  •  ${formatToman(e.amount)}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                    Row {
                                        TextButton(onClick = { navController.navigate("expense_edit?id=${e.id}") }) {
                                            Icon(Icons.Default.Edit, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("ویرایش")
                                        }
                                        TextButton(onClick = { showDelete = e }) {
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
    }

    showDelete?.let { e ->
        ConfirmDeleteDialog("حذف هزینه", "این هزینه حذف شود؟", {
            scope.launch { repo.deleteExpense(e); showDelete = null }
        }, { showDelete = null })
    }
}
