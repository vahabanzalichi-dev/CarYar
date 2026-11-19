package com.vahab.caryar.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "caryar_prefs")

class Repository(private val db: AppDatabase, private val context: Context) {

    private val selectedVehicleKey = longPreferencesKey("selected_vehicle_id")

    // Vehicles
    val vehicles: Flow<List<Vehicle>> = db.vehicleDao().getAll()

    suspend fun getVehicle(id: Long) = db.vehicleDao().getById(id)
    suspend fun insertVehicle(v: Vehicle) = db.vehicleDao().insert(v)
    suspend fun updateVehicle(v: Vehicle) = db.vehicleDao().update(v)
    suspend fun deleteVehicle(v: Vehicle) = db.vehicleDao().delete(v)

    val selectedVehicleId: Flow<Long?> = context.dataStore.data.map { prefs ->
        prefs[selectedVehicleKey]
    }

    suspend fun setSelectedVehicleId(id: Long?) {
        context.dataStore.edit { prefs ->
            if (id == null) prefs.remove(selectedVehicleKey)
            else prefs[selectedVehicleKey] = id
        }
    }

    // Services
    fun getServices(vehicleId: Long) = db.serviceDao().getByVehicle(vehicleId)
    suspend fun getLatestService(vehicleId: Long) = db.serviceDao().getLatest(vehicleId)
    suspend fun insertService(s: ServiceRecord) = db.serviceDao().insert(s)
    suspend fun updateService(s: ServiceRecord) = db.serviceDao().update(s)
    suspend fun deleteService(s: ServiceRecord) = db.serviceDao().delete(s)

    // Expenses
    fun getExpenses(vehicleId: Long) = db.expenseDao().getByVehicle(vehicleId)
    suspend fun insertExpense(e: Expense) = db.expenseDao().insert(e)
    suspend fun updateExpense(e: Expense) = db.expenseDao().update(e)
    suspend fun deleteExpense(e: Expense) = db.expenseDao().delete(e)
    suspend fun sumExpensesMonth(vehicleId: Long, yearMonth: String) =
        db.expenseDao().sumByMonth(vehicleId, yearMonth) ?: 0L
    suspend fun sumExpensesYear(vehicleId: Long, year: String) =
        db.expenseDao().sumByYear(vehicleId, year) ?: 0L
    suspend fun sumExpensesTotal(vehicleId: Long) =
        db.expenseDao().sumTotal(vehicleId) ?: 0L
    suspend fun sumByCategory(vehicleId: Long) = db.expenseDao().sumByCategory(vehicleId)

    // Reminders
    fun getReminders(vehicleId: Long) = db.reminderDao().getByVehicle(vehicleId)
    fun getAllActiveReminders() = db.reminderDao().getAllActive()
    suspend fun insertReminder(r: Reminder) = db.reminderDao().insert(r)
    suspend fun updateReminder(r: Reminder) = db.reminderDao().update(r)
    suspend fun deleteReminder(r: Reminder) = db.reminderDao().delete(r)
    suspend fun countActiveReminders(vehicleId: Long) = db.reminderDao().countActive(vehicleId)
}
