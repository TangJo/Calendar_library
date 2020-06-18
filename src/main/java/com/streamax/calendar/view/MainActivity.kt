package com.streamax.calendar.view

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.streamax.calendar.R
import com.streamax.calendar.listener.OnItemClickListener
import com.streamax.calendar.listener.OnSlideListener
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), OnSlideListener, OnItemClickListener {
    val TAG: String = "MainActivity"

    override fun OnLongPressClick(date: Date) {
        Toast.makeText(this, date.toString(), Toast.LENGTH_SHORT).show()
    }

    override fun OnItemClick(currentDay: Int,isHasVideo: Boolean) {
        Log.d(TAG, currentDay.toString())
        setDate()
    }

    override fun onClickData(isHasVideo: Boolean, isHasGps: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onLeftSlideListener() {
        calendar_view.clickRightMonth()
        setDate()
    }

    override fun onRigthSlideListener() {
        calendar_view.clickLeftMonth()
        setDate()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        calendar_view.setOnItemClickListener(this)

        calendar_view.setOnSlideListener(this)

        val weeks = arrayOf("1", "2", "3", "4", "5", "6", "7")
//        calendar_view.setWeekArr(weeks)


        var months = arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12")
//        calendar_view.setMonthArr(months)

//        calendar_view.setMainBgColor(Color.RED)

        calendar_view.setWeekTextColor(Color.RED)

        calendar_view.setSelectedColor(Color.RED, false, true)

        calendar_view.setDefaultMonthColor(Color.BLACK)

        calendar_view.setInvisibleMonthColor(Color.GRAY)

        calendar_view.setWeekBgColor(Color.LTGRAY)

        calendar_view.setBorderLineColor(Color.TRANSPARENT)

        calendar_view.setViewDimension(1f, 0.4f)

        val videoRecodMap = mapOf<Int, Boolean>(2 to true, 23 to true, 10 to true)
        calendar_view.setVideoNormalRecordMap(videoRecodMap)

        val gpsRecordMap = mapOf<Int, Boolean>(9 to true, 19 to true, 11 to true, 30 to true, 2 to true)
        calendar_view.setGpsMarkerMap(gpsRecordMap)

        val localVideomap = mapOf<Int, Boolean>(15 to true, 21 to true, 29 to true)
        calendar_view.setVideoLockRecordMap(localVideomap)

        calendar_view.setLineMarkerColor(Color.GREEN)

        setDate()

        btn_up.setOnClickListener {
            var dateStr = calendar_view.clickLeftMonth()
            setDate()
        }

        calendar_view.getYearAndmonth()

        btn_next.setOnClickListener {
            //            calendar_view.clickRightMonth()

            calendar_view.jumpDate(2018, 8, 1)
            setDate()
        }
    }

    fun setDate() {
        tv_date.text = (calendar_view.getYear().toString() + "-"
                + String.format("%02d", calendar_view.getMonth()) + "-"
                + String.format("%02d", calendar_view.getDay()))
    }
}

