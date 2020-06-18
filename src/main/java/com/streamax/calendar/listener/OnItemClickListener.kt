package com.streamax.calendar.listener

import java.util.*

/**
 * Created by Administrator on 2018/12/7 0007.
 */
interface OnItemClickListener {
    /**
     * 当前item点击事件
     * @param day 当前选中day
     */
    fun OnItemClick(day: Int,isHasVideo: Boolean)

    /**
     * 长按事件
     * @param date
     */
    fun OnLongPressClick(date: Date)

    fun onClickData(isHasVideo: Boolean, isHasGps: Boolean)
}