package com.streamax.calendar.view

import com.streamax.calendar.listener.OnItemClickListener
import com.streamax.calendar.listener.OnSlideListener

/**
 * Created by pingguo on 2018/11/24.
 */
interface ICalendarView {

    /**
     * 设置控件宽度、高度占屏幕宽高比例
     * @param viewWidth
     */
    fun setViewDimension(viewWidthRatio: Float, viewHeightRatio: Float)


    /**
     * 设置主体颜色
     * @param colorId
     */
    fun setMainBgColor(colorId: Int)

    /**
     * 设置星期背景颜色
     *
     * @param colorId
     */
    fun setWeekBgColor(colorId: Int)


    /**
     * 设置星期字体颜色
     * @param colorId
     */
    fun setWeekTextColor(colorId: Int)

    /**
     * 设置星期文字大小
     */
    fun setWeekTextSize(size: Int)

    /**
     * 设置边界线颜色
     * @param colorId
     */
    fun setBorderLineColor(colorId: Int)

    /**
     * 设置当天文本颜色
     * @param colorId
     */
    fun setTodayNumberColor(colorId: Int)

    /**
     * 设置选中项颜色
     * @param colorId
     * @param isFill 是否填充
     * @param isCircle 是否显示圆
     */
    fun setSelectedColor(colorId: Int, isFill: Boolean, isCircle: Boolean)


    /**
     * 设置日期颜色
     */
    fun setDefaultMonthColor(colorId: Int)

    /**
     * 设置不可用日期颜色
     */
    fun setInvisibleMonthColor(colorId: Int)

    /**
     * 设置日期文本字体大小
     * @param size
     */
    fun setMonthTextSize(size: Int)

    /**
     * 设置视频录像标记颜色
     * @param colorId
     */
    fun setVideoRecordMarkerColor(colorId: Int)

    /**
     * 设置gps小点颜色
     * @param colorId
     */
    fun setGpsMarkerColor(colorId: Int)

    /**
     * 设置底下标记颜色
     * @param colorId
     */
    fun setLineMarkerColor(colorId: Int)

    /**
     * 设置星期字符数组
     * @param weekArr
     */
    fun setWeekArr(weekArr: Array<String>)


    /**
     * 设置于月份字符数组
     */
    fun setMonthArr(monthArr: Array<String>)

    /**
     * 获得当前应该显示的年月
     */
    fun getYearAndmonth(): String

    /**
     * 获取当前年
     */
    fun getYear(): Int

    /**
     * 获取当前月
     */
    fun getMonth(): Int

    /**
     * 获取当天日期
     */
    fun getDay(): Int


    /**
     * 获取当前月天数
     *
     */
    fun getDaysOfMonth(year: Int, month: Int): Int

    /**
     * 跳转指定日期
     * @param year 例如：2018
     * @param month 1-12
     * @param day 例如：10
     */
    fun jumpDate(year: Int, month: Int, day: Int)

    /**
     * 上一月
     * @return
     */
    fun clickLeftMonth(): String

    /**
     * 下一月
     * @return
     */
    fun clickRightMonth(): String

    /**
     * 设置视频正常录像数组
     * @param record
     */
    fun setVideoNormalRecordMap(record: Map<Int, Boolean>)

    /**
     * 设置视频报警录像数组
     * @param record
     */
    fun setVideoAlarmRecordMap(record: Map<Int, Boolean>)


    /**
     * 设置视频报警录像数组
     * @param record
     */
    fun setVideoLockRecordMap(record: Map<Int, Boolean>)


    /**
     * 设置gps标记数组
     * @param record
     */
    fun setGpsMarkerMap(record: Map<Int, Boolean>)


    /**
     * 给控件设置监听事件
     * @param listener
     */
    fun setOnItemClickListener(listener: OnItemClickListener)

    /**
     * 左右滑动监听
     * @param onSlideListener
     */
    fun setOnSlideListener(onSlideListener: OnSlideListener)


}