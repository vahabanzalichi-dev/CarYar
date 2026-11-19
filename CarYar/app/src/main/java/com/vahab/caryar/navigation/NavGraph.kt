package com.vahab.caryar.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vahab.caryar.ui.screens.*

sealed class Screen(val route: String, val title: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    data object Dashboard : Screen("dashboard", "داشبورد", Icons.Outlined.Home, Icons.Filled.Home)
    data object Vehicles : Screen("vehicles", "خودروها", Icons.Outlined.DirectionsCar, Icons.Filled.DirectionsCar)
    data object Services : Screen("services", "تعمیرات", Icons.Outlined.Build, Icons.Filled.Build)
    data object Expenses : Screen("expenses", "هزینه‌ها", Icons.Outlined.Payments, Icons.Filled.Payments)
    data object Reminders : Screen("reminders", "یادآورها", Icons.Outlined.Notifications, Icons.Filled.Notifications)
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Vehicles,
    Screen.Services,
    Screen.Expenses,
    Screen.Reminders
)

@Composable
fun CarYarNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }
        composable(Screen.Vehicles.route) {
            VehiclesScreen(navController = navController)
        }
        composable(
            route = "vehicle_edit?id={id}",
            arguments = listOf(navArgument("id") {
                type = NavType.LongType
                defaultValue = -1L
            })
        ) { backStack ->
            val id = backStack.arguments?.getLong("id") ?: -1L
            VehicleEditScreen(navController = navController, vehicleId = if (id > 0) id else null)
        }
        composable(Screen.Services.route) {
            ServicesScreen(navController = navController)
        }
        composable(
            route = "service_edit?id={id}",
            arguments = listOf(navArgument("id") {
                type = NavType.LongType
                defaultValue = -1L
            })
        ) { backStack ->
            val id = backStack.arguments?.getLong("id") ?: -1L
            ServiceEditScreen(navController = navController, serviceId = if (id > 0) id else null)
        }
        composable(Screen.Expenses.route) {
            ExpensesScreen(navController = navController)
        }
        composable(
            route = "expense_edit?id={id}",
            arguments = listOf(navArgument("id") {
                type = NavType.LongType
                defaultValue = -1L
            })
        ) { backStack ->
            val id = backStack.arguments?.getLong("id") ?: -1L
            ExpenseEditScreen(navController = navController, expenseId = if (id > 0) id else null)
        }
        composable(Screen.Reminders.route) {
            RemindersScreen(navController = navController)
        }
        composable(
            route = "reminder_edit?id={id}",
            arguments = listOf(navArgument("id") {
                type = NavType.LongType
                defaultValue = -1L
            })
        ) { backStack ->
            val id = backStack.arguments?.getLong("id") ?: -1L
            ReminderEditScreen(navController = navController, reminderId = if (id > 0) id else null)
        }
    }
}
