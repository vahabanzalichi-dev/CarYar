package com.vahab.caryar.util

import java.util.Calendar
import java.util.Locale

/**
 * Simple Jalali (Persian) date utilities.
 * Format used throughout the app: yyyy/MM/dd
 */
object JalaliDate {

    fun today(): String {
        val g = Calendar.getInstance()
        return toJalali(g.get(Calendar.YEAR), g.get(Calendar.MONTH) + 1, g.get(Calendar.DAY_OF_MONTH))
    }

    fun currentYearMonth(): String {
        val today = today()
        return today.substring(0, 7) // yyyy/MM
    }

    fun currentYear(): String {
        return today().substring(0, 4)
    }

    fun toJalali(gy: Int, gm: Int, gd: Int): String {
        val g_d_m = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        var jy: Int
        if (gy > 1600) {
            jy = 979
            gy -= 1600
        } else {
            jy = 0
            gy -= 621
        }
        val gy2 = if (gm > 2) gy + 1 else gy
        var days = 365 * gy + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400 - 80 + gd + g_d_m[gm - 1]
        jy += 33 * (days / 12053)
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        val jm: Int
        val jd: Int
        if (days < 186) {
            jm = 1 + days / 31
            jd = 1 + days % 31
        } else {
            jm = 7 + (days - 186) / 30
            jd = 1 + (days - 186) % 30
        }
        return String.format(Locale.US, "%04d/%02d/%02d", jy, jm, jd)
    }

    fun fromJalali(jy: Int, jm: Int, jd: Int): Triple<Int, Int, Int> {
        val gy: Int
        val gm: Int
        val gd: Int
        val days: Int
        jy -= 979
        days = 365 * jy + jy / 33 * 8 + (jy % 33 + 3) / 4 + 78 + jd + if (jm < 7) (jm - 1) * 31 else (jm - 7) * 30 + 186
        gy = 1600 + 400 * (days / 146097)
        days %= 146097
        var leap = true
        if (days >= 36525) {
            days--
            gy += 100 * (days / 36524)
            days %= 36524
            if (days >= 365) days++ else leap = false
        }
        gy += 4 * (days / 1461)
        days %= 1461
        if (days >= 366) {
            leap = false
            days--
            gy += days / 365
            days %= 365
        }
        val sal_a = intArrayOf(0, 31, if (leap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var gmTemp = 0
        while (gmTemp < 13 && days >= sal_a[gmTemp]) {
            days -= sal_a[gmTemp]
            gmTemp++
        }
        gm = gmTemp
        gd = days + 1
        return Triple(gy, gm, gd)
    }

    fun formatDisplay(jalali: String): String {
        if (jalali.isBlank() || jalali.length < 8) return jalali
        return try {
            val parts = jalali.split("/")
            if (parts.size == 3) {
                "${parts[2]} ${monthName(parts[1].toInt())} ${parts[0]}"
            } else jalali
        } catch (e: Exception) {
            jalali
        }
    }

    fun monthName(month: Int): String {
        return when (month) {
            1 -> "فروردین"
            2 -> "اردیبهشت"
            3 -> "خرداد"
            4 -> "تیر"
            5 -> "مرداد"
            6 -> "شهریور"
            7 -> "مهر"
            8 -> "آبان"
            9 -> "آذر"
            10 -> "دی"
            11 -> "بهمن"
            12 -> "اسفند"
            else -> ""
        }
    }

    fun isValid(jalali: String): Boolean {
        return try {
            val p = jalali.split("/")
            if (p.size != 3) return false
            val y = p[0].toInt()
            val m = p[1].toInt()
            val d = p[2].toInt()
            y in 1300..1500 && m in 1..12 && d in 1..31
        } catch (e: Exception) {
            false
        }
    }
}
