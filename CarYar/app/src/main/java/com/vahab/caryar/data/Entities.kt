package com.vahab.caryar.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val brand: String = "",
    val model: String = "",
    val year: Int? = null,
    val trim: String = "",
    val plateNumber: String = "",
    val currentOdometer: Int = 0,
    val purchaseDate: String = "", // Jalali yyyy/MM/dd
    val purchasePrice: Long? = null,
    val notes: String = "",
    val imageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "services",
    foreignKeys = [
        ForeignKey(
            entity = Vehicle::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("vehicleId")]
)
data class ServiceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val title: String,
    val category: String,
    val dateJalali: String, // yyyy/MM/dd
    val odometer: Int,
    val cost: Long = 0,
    val workshop: String = "",
    val partsReplaced: String = "",
    val notes: String = "",
    val nextServiceOdometer: Int? = null,
    val nextServiceDateJalali: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = Vehicle::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("vehicleId")]
)
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val title: String,
    val amount: Long, // تومان
    val dateJalali: String,
    val odometer: Int? = null,
    val category: String,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = Vehicle::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("vehicleId")]
)
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val title: String,
    val dueDateJalali: String? = null, // yyyy/MM/dd
    val dueOdometer: Int? = null,
    val notes: String = "",
    val isActive: Boolean = true,
    val isTriggered: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// Categories
object ServiceCategories {
    val list = listOf(
        "تعویض روغن",
        "فیلتر روغن",
        "فیلتر هوا",
        "فیلتر کابین",
        "فیلتر سوخت",
        "لنت ترمز",
        "باتری",
        "لاستیک",
        "تسمه",
        "سیستم خنک‌کننده",
        "تعمیر موتور",
        "تعمیر گیربکس",
        "برق خودرو",
        "سایر تعمیرات"
    )
}

object ExpenseCategories {
    val list = listOf(
        "سوخت",
        "تعمیرات",
        "سرویس",
        "قطعات",
        "بیمه",
        "معاینه فنی",
        "جریمه",
        "پارکینگ",
        "کارواش",
        "عوارض",
        "سایر هزینه‌ها"
    )
}
