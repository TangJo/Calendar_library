package com.streamax.calendar.utils

/**
 * Created by pingguo on 2018/11/24.
 */
interface ICalendarUtil {
    /**
     *  是否为闰年
     *  @param  year
     *  @return
     */
    fun isLeapYear(year:Int):Boolean


    /**
     * 得到某月有多少天数
     *
     * @param isLeapyear 是否为闰年
     * @param month
     * @return
     */
    fun getDaysOfMonth(isLeapyear:Boolean,month:Int):Int


    /**
     * 指定某年中的某月的第一天是星期几
     *
     * @param year
     * @param month
     * @return
     */
    fun getWeekdayOfMonth(year:Int,month:Int):Int;
}