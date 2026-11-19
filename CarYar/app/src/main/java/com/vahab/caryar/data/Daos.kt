package com.vahab.caryar.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<Vehicle>>

    @Query("SELECT * FROM vehicles WHERE id = :id")
    suspend fun getById(id: Long): Vehicle?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vehicle: Vehicle): Long

    @Update
    suspend fun update(vehicle: Vehicle)

    @Delete
    suspend fun delete(vehicle: Vehicle)

    @Query("SELECT COUNT(*) FROM vehicles")
    suspend fun count(): Int
}

@Dao
interface ServiceDao {
    @Query("SELECT * FROM services WHERE vehicleId = :vehicleId ORDER BY dateJalali DESC, id DESC")
    fun getByVehicle(vehicleId: Long): Flow<List<ServiceRecord>>

    @Query("SELECT * FROM services WHERE vehicleId = :vehicleId ORDER BY dateJalali DESC LIMIT 1")
    suspend fun getLatest(vehicleId: Long): ServiceRecord?

    @Query("SELECT * FROM services WHERE id = :id")
    suspend fun getById(id: Long): ServiceRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(service: ServiceRecord): Long

    @Update
    suspend fun update(service: ServiceRecord)

    @Delete
    suspend fun delete(service: ServiceRecord)

    @Query("SELECT * FROM services WHERE vehicleId = :vehicleId AND nextServiceOdometer IS NOT NULL AND nextServiceOdometer <= :currentOdo")
    suspend fun getDueByOdometer(vehicleId: Long, currentOdo: Int): List<ServiceRecord>
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE vehicleId = :vehicleId ORDER BY dateJalali DESC, id DESC")
    fun getByVehicle(vehicleId: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getById(id: Long): Expense?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense): Long

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Query("SELECT SUM(amount) FROM expenses WHERE vehicleId = :vehicleId AND dateJalali LIKE :yearMonth || '%'")
    suspend fun sumByMonth(vehicleId: Long, yearMonth: String): Long?

    @Query("SELECT SUM(amount) FROM expenses WHERE vehicleId = :vehicleId AND dateJalali LIKE :year || '%'")
    suspend fun sumByYear(vehicleId: Long, year: String): Long?

    @Query("SELECT SUM(amount) FROM expenses WHERE vehicleId = :vehicleId")
    suspend fun sumTotal(vehicleId: Long): Long?

    @Query("SELECT category, SUM(amount) as total FROM expenses WHERE vehicleId = :vehicleId GROUP BY category")
    suspend fun sumByCategory(vehicleId: Long): List<CategorySum>
}

data class CategorySum(
    val category: String,
    val total: Long
)

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE vehicleId = :vehicleId ORDER BY isActive DESC, dueDateJalali ASC")
    fun getByVehicle(vehicleId: Long): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE isActive = 1")
    fun getAllActive(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getById(id: Long): Reminder?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: Reminder): Long

    @Update
    suspend fun update(reminder: Reminder)

    @Delete
    suspend fun delete(reminder: Reminder)

    @Query("SELECT COUNT(*) FROM reminders WHERE vehicleId = :vehicleId AND isActive = 1")
    suspend fun countActive(vehicleId: Long): Int

    @Query("SELECT * FROM reminders WHERE isActive = 1 AND dueOdometer IS NOT NULL AND dueOdometer <= :currentOdo")
    suspend fun getDueByOdometer(currentOdo: Int): List<Reminder>
}
