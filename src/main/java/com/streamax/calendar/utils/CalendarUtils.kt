package com.streamax.calendar.utils

import java.util.*

/**
 * Created by pingguo on 2018/11/24.
 */
class CalendarUtils : ICalendarUtil{
    override fun isLeapYear(year: Int): Boolean {
        if (year % 100 == 0 && year % 400 == 0) {
            return true
        } else if (year % 100 != 0 && year % 4 == 0) {
            return true
        }
        return false
    }
    override fun getDaysOfMonth(isLeapyear: Boolean, month: Int): Int {
        var daysOfMonth=30;
        when (month) {
            1, 3, 5, 7, 8, 10, 12 -> daysOfMonth = 31
            4, 6, 9, 11 -> daysOfMonth = 30
            2 -> if (isLeapyear) {
                daysOfMonth = 29
            } else {
                daysOfMonth = 28
            }
        }
        return daysOfMonth
    }

    override fun getWeekdayOfMonth(year: Int, month: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1)
        var dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
        return dayOfWeek
    }

}