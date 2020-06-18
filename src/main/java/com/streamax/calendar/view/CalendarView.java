package com.streamax.calendar.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.streamax.calendar.R;
import com.streamax.calendar.listener.OnItemClickListener;
import com.streamax.calendar.listener.OnSlideListener;
import com.streamax.calendar.utils.CalendarUtils;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by pingguo on 2018/11/25.
 */

public class CalendarView extends View implements View.OnTouchListener
        , GestureDetector.OnGestureListener, ICalendarView {
    private final static String TAG = CalendarView.class.getSimpleName();
    private Date mSelectedStartDate;
    private Date mSelectedEndDate;
    // 当前日历显示的月
    private Date mCurDate;
    // 今天的日期
    private Date mTodayDate;
    // 手指按下状态时临时日期
    private Date mDownDate;
    // 日历显示的第一个日期和最后一个日期
    private Date mShowFirstDate, mShowLastDate;
    // 按下的格子索引
    private int downIndex;
    private Calendar mCalendar;
    private Surface mSurface;
    // 日历显示数字
    private int[] mDateArr = new int[42];
    // 当前显示的日历起始的索引
    private int mCurStartIndex, mCurEndIndex;
    //给控件设置监听事件
    private OnItemClickListener onItemClickListener;
    private GestureDetector mGestureDetector;

    private CalendarUtils calendarUtils = new CalendarUtils();


    public CalendarView(Context context) {
        super(context);
        init(null, 0);
    }

    public CalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CalendarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyle) {
        mCurDate = mSelectedStartDate = mSelectedEndDate = mTodayDate = new Date();
        mCalendar = Calendar.getInstance();
        mCalendar.setTime(mCurDate);
        mSurface = new Surface();
        mSurface.density = getResources().getDisplayMetrics().density;
        setOnTouchListener(this);

        mGestureDetector = new GestureDetector(this);
        initViewAttrs(attrs, defStyle);
    }

    private void initViewAttrs(AttributeSet attrs, int defStyle) {
        final TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.CalendarView, defStyle, 0);

        mViewWidthRatio = array.getFloat(R.styleable.CalendarView_viewWidthRatio, 1f);
        mViewHeightRatio = array.getFloat(R.styleable.CalendarView_viewHeightRatio, 0.4f);

        mSurface.mMainBgColor = array.getColor(R.styleable.CalendarView_mainBgColor, Color.WHITE);
        mSurface.mWeekBgColor = array.getColor(R.styleable.CalendarView_weekBgColor, Color.BLACK);

        mSurface.mWeekCircleColor = array.getColor(R.styleable.CalendarView_weekCircleColor, Color.TRANSPARENT);

        mSurface.mWeekTextColor = array.getColor(R.styleable.CalendarView_weekTextColor, Color.BLACK);
        mSurface.mWeekTextSize = array.getDimensionPixelSize(R.styleable.CalendarView_weekTextSize, 36);

        mSurface.mMonthTextSize = array.getDimensionPixelSize(R.styleable.CalendarView_monthTextSize, 36);

        mSurface.mBorderLineColor = array.getColor(R.styleable.CalendarView_borderLineColor, Color.parseColor("#CCCCCC"));
        mSurface.mTodayNumberColor = array.getColor(R.styleable.CalendarView_todayNumberColor, Color.RED);
        mSurface.mCellSelectedColor = array.getColor(R.styleable.CalendarView_selectedColor, Color.parseColor("#99CCFF"));
        mSurface.isCellBgFill = array.getBoolean(R.styleable.CalendarView_isFill, false);
        mSurface.isCircle = array.getBoolean(R.styleable.CalendarView_isCircle, false);
        mSurface.defaultMonthTextColor = array.getColor(R.styleable.CalendarView_defaultMonthColor, Color.BLACK);
        mSurface.inVisibleMonthTextColor = array.getColor(R.styleable.CalendarView_invisibleMonthColor, Color.parseColor("#CCCCCC"));
        mSurface.mGpsMarkerColor = array.getColor(R.styleable.CalendarView_gpsMarkerColor, Color.RED);

        mSurface.isDayBgCircle = array.getBoolean(R.styleable.CalendarView_dayBgCircle, false);
        mSurface.mLockLineMarkerColor = array.getColor(R.styleable.CalendarView_lockVideoColor, Color.YELLOW);
        mSurface.mNormalLineMarkerColor = array.getColor(R.styleable.CalendarView_normalVideoColor, Color.GREEN);
        mSurface.mAlarmLineMarkerColor = array.getColor(R.styleable.CalendarView_alarmVideoColor, Color.RED);

        array.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mSurface.mControlWidth = (int) (getResources().getDisplayMetrics().widthPixels * mViewWidthRatio);
        mSurface.mControlHeight = (int) (getResources().getDisplayMetrics().heightPixels * mViewHeightRatio);

        float height = getMeasuredHeight();

        Log.d("onMeasure", "mControlHeight = " + getResources().getDisplayMetrics().heightPixels
                + ", getMeasuredHeight = " + height + ", getHeight = " + getHeight());

        widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(mSurface.mControlWidth,
                View.MeasureSpec.EXACTLY);
        heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(mSurface.mControlHeight,
                View.MeasureSpec.EXACTLY);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            mSurface.init();
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw");
        //设置画板颜色
        setBackgroundColor(mSurface.mMainBgColor);
        drawFrame(canvas);
        drawWeekMsg(canvas);
        // 计算日期
        calculateDate();
        // 按下状态，选择状态背景色
        drawDownOrSelectedBg(canvas);
        drawCellBgByTodayIndex(canvas);
        super.onDraw(canvas);
    }


    /**
     * 绘制框
     *
     * @param canvas
     */
    private void drawFrame(Canvas canvas) {
        // 画框
        canvas.drawPath(mSurface.mBoxPath, mSurface.mBorderPaint);
    }

    /**
     * 根据当天日期绘制格子
     *
     * @param canvas
     */
    private void drawCellBgByTodayIndex(Canvas canvas) {
        // today index
        int todayIndex = -1;
        mCalendar.setTime(mCurDate);
        String curYearAndMonth = mCalendar.get(Calendar.YEAR) + String.valueOf(mCalendar.get(Calendar.MONTH));
        mCalendar.setTime(mTodayDate);
        String todayYearAndMonth = mCalendar.get(Calendar.YEAR) + String.valueOf(mCalendar.get(Calendar.MONTH));
        if (curYearAndMonth.equals(todayYearAndMonth)) {
            int todayNumber = mCalendar.get(Calendar.DAY_OF_MONTH);
            todayIndex = mCurStartIndex + todayNumber - 1;
        }
        setCellBgColor(canvas, todayIndex);
    }

    /**
     * 绘制星期信息
     *
     * @param canvas
     */
    private void drawWeekMsg(Canvas canvas) {
        // 星期
        float weekTextY = mSurface.mMonthHeight + mSurface.mWeekHeight * 3 / 4f;
        canvas.drawRect(0, 0, mSurface.mControlWidth, mSurface.mWeekHeight, mSurface.mWeekBgPaint);

        for (int i = 0; i < mSurface.weekTexts.length; i++) {


            Float[] margins = calculateLeftAndTopMarginOfWeek(i);
            float marginLeft = margins[0];
            float marginTop = margins[1];
            float cx = marginLeft + mSurface.mBorderWidth + mSurface.mCellWidth / 2;
            float cy = marginTop + mSurface.mCellHeight * 11 / 16 - mSurface.mBorderWidth;
            float radius = mSurface.mCellHeight / 4;
            canvas.drawCircle(cx, cy, radius, mSurface.mWeekCirclePaint);


            float weekTextX = i * mSurface.mCellWidth + (mSurface.mCellWidth - mSurface.mWeekPaint
                    .measureText(mSurface.weekTexts[i])) / 2f;
            canvas.drawText(mSurface.weekTexts[i], weekTextX, weekTextY, mSurface.mWeekPaint);

        }
    }

    /**
     * 设置日历Cell不同背景颜色
     *
     * @param canvas
     * @param todayIndex
     */
    private void setCellBgColor(Canvas canvas, int todayIndex) {
        for (int i = 0; i < 42; i++) {
            //默认日期颜色
            int color = mSurface.defaultMonthTextColor;
            if (isLastMonth(i) || isNextMonth(i)) {
                color = mSurface.inVisibleMonthTextColor;
            } else {
                //判断是否有视频录像或者gps
                boolean isHasNormalVideo = isHasNormalVideoRecord(i);
                boolean isHasAlarmVideo = isHasAlarmVideoRecord(i);
                boolean isHasLockVideo = isHasLockVideoRecord(i);

                boolean isHasGps = isHasGpsMarker(i);

                drawMarkerBg(canvas, i, isHasNormalVideo, isHasAlarmVideo, isHasLockVideo, isHasGps);
            }
            if (todayIndex != -1 && i == todayIndex) {
                color = mSurface.mTodayNumberColor;
            }
            drawCellText(canvas, i, String.valueOf(mDateArr[i]), color);
        }
    }

    /**
     * 是否有正常录像标记
     *
     * @param index
     * @return
     */
    private boolean isHasNormalVideoRecord(int index) {
        return null != mVideoNormalRecordMap.get(mDateArr[index]) && mVideoNormalRecordMap.get(mDateArr[index]);
    }

    /**
     * 是否有报警录像标记
     *
     * @param index
     * @return
     */
    private boolean isHasAlarmVideoRecord(int index) {
        return null != mVideoAlarmRecordMap.get(mDateArr[index]) && mVideoAlarmRecordMap.get(mDateArr[index]);
    }

    /**
     * 是否有锁定录像标记
     *
     * @param index
     * @return
     */
    private boolean isHasLockVideoRecord(int index) {
        return null != mVideoLockRecordMap.get(mDateArr[index]) && mVideoLockRecordMap.get(mDateArr[index]);
    }

    /**
     * 是否有gps标记
     *
     * @param index
     * @return
     */
    private boolean isHasGpsMarker(int index) {
        return null != mGpsRecordMap.get(mDateArr[index]) && mGpsRecordMap.get(mDateArr[index]);
    }

    /**
     * 计算日期
     */
    private void calculateDate() {
        mCalendar.setTime(mCurDate);
        mCalendar.set(Calendar.DAY_OF_MONTH, 1);
        int dayInWeek = mCalendar.get(Calendar.DAY_OF_WEEK);
        Log.d(TAG, "day in week:" + dayInWeek);
        //当前月开始日期下标加1
        int monthStart = dayInWeek;
        //如果当前值为1，将其赋值为8
        if (monthStart == 1) {
            monthStart = 8;
        }
        //以日为开头减1，以星期一为开头减2
        monthStart -= 1;
        //可获取当前开始日期下标
        mCurStartIndex = monthStart;
        //将1号赋值为开始日期
        mDateArr[monthStart] = 1;
        // last month
        if (monthStart > 0) {
            mCalendar.set(Calendar.DAY_OF_MONTH, 0);
            int dayInmonth = mCalendar.get(Calendar.DAY_OF_MONTH);
            //将上一个月的日期赋值
            for (int i = monthStart - 1; i >= 0; i--) {
                mDateArr[i] = dayInmonth;
                dayInmonth--;
            }
            mCalendar.set(Calendar.DAY_OF_MONTH, mDateArr[0]);
        }
        mShowFirstDate = mCalendar.getTime();
        // this month
        mCalendar.setTime(mCurDate);
        mCalendar.add(Calendar.MONTH, 1);
        mCalendar.set(Calendar.DAY_OF_MONTH, 0);
        int monthDay = mCalendar.get(Calendar.DAY_OF_MONTH);
        //将本月开始之后到月末的日期赋值
        for (int i = 1; i < monthDay; i++) {
            mDateArr[monthStart + i] = i + 1;
        }
        mCurEndIndex = monthStart + monthDay;
        // next month 下一个月赋值
        for (int i = monthStart + monthDay; i < 42; i++) {
            mDateArr[i] = i - (monthStart + monthDay) + 1;
        }
        if (mCurEndIndex < 42) {
            // 显示了下一月的
            mCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        mCalendar.set(Calendar.DAY_OF_MONTH, mDateArr[41]);
        mShowLastDate = mCalendar.getTime();
    }

    /**
     * 绘制日期文本
     *
     * @param canvas
     * @param index
     * @param text
     * @param color
     */
    private void drawCellText(Canvas canvas, int index, String text, int color) {
        int x = getXByIndex(index);
        int y = getYByIndex(index);
        mSurface.mDatePaint.setColor(color);
        float cellY = mSurface.mMonthHeight + mSurface.mWeekHeight + (y - 1)
                * mSurface.mCellHeight + mSurface.mCellHeight * 3 / 4f;
        float cellX = (mSurface.mCellWidth * (x - 1))
                + (mSurface.mCellWidth - mSurface.mDatePaint.measureText(text)) / 2f;
        canvas.drawText(text, cellX, cellY, mSurface.mDatePaint);
    }

    /**
     * 绘制选中项背景填充颜色
     *
     * @param canvas
     * @param index
     * @param color
     */
    private void drawSelectedCellBg(Canvas canvas, int index, int color) {
        mSurface.mCellSelectedBgPaint.setColor(color);

        Float[] margins = calculateLeftAndTopMargin(index);
        float marginLeft = margins[0];
        float marginTop = margins[1];
        if (mSurface.isCircle) {
            float cx = marginLeft + mSurface.mBorderWidth + mSurface.mCellWidth / 2;
            float cy = marginTop + mSurface.mCellHeight * 5 / 8 - mSurface.mBorderWidth;
            float radius = mSurface.mCellHeight / 2 - 10;
            canvas.drawCircle(cx, cy, radius, mSurface.mCellSelectedBgPaint);
        } else {
            float right = marginLeft + mSurface.mCellWidth - mSurface.mBorderWidth;
            float bottom = marginTop + mSurface.mCellHeight - mSurface.mBorderWidth;
            canvas.drawRect(marginLeft, marginTop, right, bottom, mSurface.mCellSelectedBgPaint);
        }
    }

    /**
     * 绘制标记物，如录像视频、gps等
     *
     * @param canvas
     * @param index
     * @param isDrawNormalVideo
     * @param isDrawMapPoint
     * @param isDrawLockVideo
     * @desc isDrawVideoCircle&isDrawMapPoint与isDrawLine只可能存在一种情况
     */
    private void drawMarkerBg(Canvas canvas, int index, boolean isDrawNormalVideo, boolean isDrawAlarmVideo
            , boolean isDrawLockVideo, boolean isDrawMapPoint) {
        Float[] margins = calculateLeftAndTopMargin(index);
        float marginLeft = margins[0];
        float marginTop = margins[1];
        //绘制视频正常录像标记圆
        if (isDrawNormalVideo) {
            if (mSurface.isDayBgCircle) {
                drawCircle(mSurface.mVideoNormalRecordPaint, canvas, marginLeft, marginTop);
            } else {
                drawLine(mSurface.mNormalLineMarkerPaint, canvas, marginLeft, marginTop);
            }
        }

        //绘制报警录像标记圆
        if (isDrawAlarmVideo) {
            if (mSurface.isDayBgCircle) {
                drawCircle(mSurface.mVideoAlarmRecordPaint, canvas, marginLeft, marginTop);
            } else {
                drawLine(mSurface.mAlarmLineMarkerPaint, canvas, marginLeft, marginTop);
            }
        }

        //绘制日期下的线
        if (isDrawLockVideo) {
            if (mSurface.isDayBgCircle) {
                drawCircle(mSurface.mVideoLockRecordPaint, canvas, marginLeft, marginTop);
            } else {
                drawLine(mSurface.mLockLineMarkerPaint, canvas, marginLeft, marginTop);
            }
        }


        //绘制gps小点
        if (isDrawMapPoint) {
            int radius = 10;
            float cx = marginLeft + mSurface.mCellWidth / 4;
            float cy = marginTop + mSurface.mCellHeight / 4;
            canvas.drawCircle(cx, cy, radius, mSurface.mGpsPointPaint);
        }

    }

    private void drawLine(Paint paint, Canvas canvas, float marginLeft, float marginTop) {
        int marginLev = 40;
        int marginPor = 10;
        float left = marginLeft + marginLev;
        float top = marginTop + mSurface.mCellHeight + marginPor;
        float right = marginLeft + mSurface.mBorderWidth + mSurface.mCellWidth - marginLev;
        float bottom = marginTop + mSurface.mCellHeight + marginPor;
        paint.setStrokeWidth(20);  //设置横线粗细
        paint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawLine(left, top, right, bottom, paint);
    }

    private void drawCircle(Paint paint, Canvas canvas, float marginLeft, float marginTop) {
        float cx = marginLeft + mSurface.mBorderWidth + mSurface.mCellWidth / 2;
        float cy = marginTop + mSurface.mCellHeight * 5 / 8 - mSurface.mBorderWidth;
        float radius = mSurface.mCellHeight / 2 - 20;
        canvas.drawCircle(cx, cy, radius, paint);
    }

    /**
     * @param canvas
     */
    private void drawDownOrSelectedBg(Canvas canvas) {

        // selected bg color
        if (!mSelectedEndDate.before(mShowFirstDate)
                && !mSelectedStartDate.after(mShowLastDate)) {
            int[] selectionArr = new int[]{-1, -1};
            mCalendar.setTime(mCurDate);
            mCalendar.add(Calendar.MONTH, -1);
            findSelectedIndex(0, mCurStartIndex, mCalendar, selectionArr);
            if (selectionArr[1] == -1) {
                mCalendar.setTime(mCurDate);
                findSelectedIndex(mCurStartIndex, mCurEndIndex, mCalendar, selectionArr);
            }
            if (selectionArr[1] == -1) {
                mCalendar.setTime(mCurDate);
                mCalendar.add(Calendar.MONTH, 1);
                findSelectedIndex(mCurEndIndex, 42, mCalendar, selectionArr);
            }
            if (selectionArr[0] == -1) {
                selectionArr[0] = 0;
            }
            if (selectionArr[1] == -1) {
                selectionArr[1] = 41;
            }
            for (int i = selectionArr[0]; i <= selectionArr[1]; i++) {
                drawSelectedCellBg(canvas, i, mSurface.mCellSelectedColor);
                mCurrentSelectedDay = mDateArr[i];
                setOnClickData();
            }
        } else {
            drawSelectedCellBg(canvas, getIndexByDateArr(mCurrentSelectedDay), mSurface.mCellSelectedColor);
            setOnClickData();
        }
    }

    private void setOnClickData() {
        if (null == onItemClickListener) return;
        boolean isHasNormalVideo = mVideoNormalRecordMap.get(mCurrentSelectedDay) != null && mVideoNormalRecordMap.get(mCurrentSelectedDay);
        boolean isHasAlarmVideo = mVideoAlarmRecordMap.get(mCurrentSelectedDay) != null && mVideoAlarmRecordMap.get(mCurrentSelectedDay);
        boolean isHasLockVideo = mVideoLockRecordMap.get(mCurrentSelectedDay) != null && mVideoLockRecordMap.get(mCurrentSelectedDay);


        boolean isHasGps = mGpsRecordMap.get(mCurrentSelectedDay) != null && mGpsRecordMap.get(mCurrentSelectedDay);
        onItemClickListener.onClickData(isHasNormalVideo || isHasAlarmVideo || isHasLockVideo, isHasGps);
    }

    /**
     * 根据日期获取当前下标
     *
     * @param day
     * @return
     */
    private int getIndexByDateArr(int day) {
        int oneDayIndex = 0;
        for (int i = 0; i < mDateArr.length; i++) {
            if (isNextMonth(i) || isLastMonth(i)) continue;
            //处理当前月日期不存在情况
            if (1 == mDateArr[i]) {
                oneDayIndex = i;
            }
            if (day == mDateArr[i]) return i;
        }
        return oneDayIndex;
    }


    private int mCurrentSelectedDay = 1;

    /**
     * 查找选中的下标
     *
     * @param startIndex
     * @param endIndex
     * @param calendar
     * @param selectionArr
     */
    private void findSelectedIndex(int startIndex, int endIndex, Calendar calendar, int[] selectionArr) {
        for (int i = startIndex; i < endIndex; i++) {
            calendar.set(Calendar.DAY_OF_MONTH, mDateArr[i]);
            Date temp = calendar.getTime();
            // Log.d(TAG, "temp:" + temp.toLocaleString());
            if (temp.compareTo(mSelectedStartDate) == 0) {
                selectionArr[0] = i;
            }
            if (temp.compareTo(mSelectedEndDate) == 0) {
                selectionArr[1] = i;
                return;
            }
        }
    }

    /**
     * 是否为上一个月
     *
     * @param i
     * @return
     */
    private boolean isLastMonth(int i) {
        return i < mCurStartIndex;
    }

    /**
     * 是否为下一个月
     *
     * @param i
     * @return
     */
    private boolean isNextMonth(int i) {
        return i >= mCurEndIndex;
    }

    private int getXByIndex(int i) {
        return i % 7 + 1; // 1 2 3 4 5 6 7
    }

    private int getYByIndex(int i) {
        return i / 7 + 1; // 1 2 3 4 5 6
    }

    /**
     * 计算当前格子相对左上的相对距离
     *
     * @param index
     * @return
     */
    private Float[] calculateLeftAndTopMargin(int index) {
        int x = getXByIndex(index);
        int y = getYByIndex(index);
        float left = mSurface.mCellWidth * (x - 1) + mSurface.mBorderWidth;
        float top = mSurface.mMonthHeight + mSurface.mWeekHeight + (y - 1) * mSurface.mCellHeight + mSurface.mBorderWidth;
        return new Float[]{left, top};
    }

    /**
     * 计算当前格子相对左上的相对距离
     *
     * @param index
     * @return
     */
    private Float[] calculateLeftAndTopMarginOfWeek(int index) {
        int x = getXByWeekIndex(index);
        int y = getYByWeekIndex(index);
        float left = mSurface.mCellWidth * (x - 1) + mSurface.mBorderWidth;
        float top = mSurface.mWeekHeight + (y - 1) * mSurface.mCellHeight + mSurface.mBorderWidth;
        return new Float[]{left, top};
    }


    private int getXByWeekIndex(int i) {
        return i % 7 + 1; // 1 2 3 4 5 6 7
    }

    private int getYByWeekIndex(int i) {
        return i / 7; // 1 2 3 4 5 6
    }

    /**
     * 获得当前应该显示的年月
     *
     * @return
     */
    @Override
    public String getYearAndmonth() {
        mCalendar.setTime(mCurDate);
        int year = mCalendar.get(Calendar.YEAR);
        int month = mCalendar.get(Calendar.MONTH);
        return year + "-" + mSurface.monthText[month];
    }


    /**
     * 上一月
     *
     * @return
     */
    @Override
    public String clickLeftMonth() {
        mCalendar.setTime(mCurDate);
        mCalendar.add(Calendar.MONTH, -1);
        mCurDate = mCalendar.getTime();
        invalidate();
        return getYearAndmonth();
    }

    /**
     * 下一月
     *
     * @return
     */
    @Override
    public String clickRightMonth() {
        mCalendar.setTime(mCurDate);
        mCalendar.add(Calendar.MONTH, 1);
        mCurDate = mCalendar.getTime();
        invalidate();
        return getYearAndmonth();
    }

    //是否可见日期
    private boolean isVisisbleDate = true;

    /**
     * 根据坐标确定选择日期
     *
     * @param x
     * @param y
     */
    private void setSelectedDateByCoor(float x, float y) {
        if (y > mSurface.mMonthHeight + mSurface.mWeekHeight) {
            int m = (int) (Math.floor(x / mSurface.mCellWidth) + 1);
            int n = (int) (Math.floor((y - (mSurface.mMonthHeight + mSurface.mWeekHeight))
                    / mSurface.mCellHeight) + 1);
            downIndex = (n - 1) * 7 + m - 1;
            Log.d(TAG, "downIndex:" + downIndex);
            mCalendar.setTime(mCurDate);
            if (isLastMonth(downIndex)) {
                isVisisbleDate = false;
                mCalendar.add(Calendar.MONTH, -1);
            } else if (isNextMonth(downIndex)) {
                isVisisbleDate = false;
                mCalendar.add(Calendar.MONTH, 1);
            } else {
                mCalendar.set(Calendar.DAY_OF_MONTH, mDateArr[downIndex]);
            }
            mDownDate = mCalendar.getTime();
        }
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isVisisbleDate = true;
                setSelectedDateByCoor(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
                if (!isVisisbleDate) return false;
                if (mDownDate != null) {
                    mSelectedStartDate = mSelectedEndDate = mDownDate;
                    mDownDate = null;
                    invalidate();
                    //响应监听事件
                    if (null == onItemClickListener) return false;
                    //mDateArr[downIndex]
                    onItemClickListener.OnItemClick(mDateArr[downIndex], isHasLockVideoRecord(downIndex)
                            || isHasAlarmVideoRecord(downIndex) || isHasNormalVideoRecord(downIndex));
                }
                break;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    private OnSlideListener onSlideListener;

    @Override
    public void setOnSlideListener(OnSlideListener onSlideListener) {
        this.onSlideListener = onSlideListener;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Log.d(TAG, e.toString());
        if (null == onItemClickListener) return;

        onItemClickListener.OnLongPressClick(mSelectedStartDate);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (null == onSlideListener) return false;
        if (e1.getX() - e2.getX() > 120) {
            // 向左滑动
            onSlideListener.onLeftSlideListener();
            return true;
        } else if (e1.getX() - e2.getX() < -120) {
            // 向右滑动
            onSlideListener.onRigthSlideListener();
            return true;
        }
        return false;
    }

    @Override
    public void setMainBgColor(int colorId) {
        mSurface.mMainBgColor = colorId;
    }

    @Override
    public void setWeekArr(@NotNull String[] weekArr) {
        mSurface.setWeekText(weekArr);
    }

    @Override
    public void setMonthArr(@NotNull String[] monthArr) {
        mSurface.setMonthText(monthArr);
    }

    /**
     * 年
     *
     * @return
     */
    public int getYear() {
        return mCalendar.get(Calendar.YEAR);
    }

    /**
     * 月
     *
     * @return
     */
    public int getMonth() {
        return mCalendar.get(Calendar.MONTH) + 1;
    }

    @Override
    public int getDay() {
        return mCalendar.get(Calendar.DAY_OF_MONTH);
    }

    @Override
    public void setWeekTextColor(int colorId) {
        mSurface.mWeekTextColor = colorId;
    }

    @Override
    public void setTodayNumberColor(int colorId) {
        mSurface.mTodayNumberColor = colorId;
    }

    @Override
    public void setSelectedColor(int colorId, boolean isFill, boolean isCircle) {
        mSurface.mCellSelectedColor = colorId;
        mSurface.isCellBgFill = isFill;
        mSurface.isCircle = isCircle;
    }

    @Override
    public void setDefaultMonthColor(int colorId) {
        mSurface.defaultMonthTextColor = colorId;
    }

    @Override
    public void setInvisibleMonthColor(int colorId) {
        mSurface.inVisibleMonthTextColor = colorId;
    }


    @Override
    public void setWeekBgColor(int colorId) {
        mSurface.mWeekBgColor = colorId;
    }

    @Override
    public void setBorderLineColor(int colorId) {
        mSurface.mBorderLineColor = colorId;
    }

    private float mViewWidthRatio = 1f;
    private float mViewHeightRatio = 0.4f;

    @Override
    public void setViewDimension(float viewWidthRatio, float viewHeightRatio) {
        mViewWidthRatio = viewWidthRatio;
        mViewHeightRatio = viewHeightRatio;
        Log.d(TAG, "viewWidth is " + viewWidthRatio + ",viewHeight is " + viewHeightRatio);
    }

    @Override
    public void setWeekTextSize(int size) {

    }

    @Override
    public void setMonthTextSize(int size) {

    }

    //存储视频正常录像map
    private Map<Integer, Boolean> mVideoNormalRecordMap = new ConcurrentHashMap<>();
    //存储视频报警录像map
    private Map<Integer, Boolean> mVideoAlarmRecordMap = new ConcurrentHashMap<>();
    //锁定录像map
    private Map<Integer, Boolean> mVideoLockRecordMap = new ConcurrentHashMap<>();

    private Map<Integer, Boolean> mGpsRecordMap = new ConcurrentHashMap<>();


    @Override
    public void setVideoNormalRecordMap(@NotNull Map<Integer, Boolean> record) {
        mVideoNormalRecordMap = record;
    }

    @Override
    public void setVideoAlarmRecordMap(@NotNull Map<Integer, Boolean> record) {
        mVideoAlarmRecordMap = record;
    }


    @Override
    public void setVideoLockRecordMap(@NotNull Map<Integer, Boolean> record) {
        mVideoLockRecordMap = record;
    }

    @Override
    public void setGpsMarkerMap(@NotNull Map<Integer, Boolean> record) {
        mGpsRecordMap = record;
    }

    @Override
    public void setVideoRecordMarkerColor(int colorId) {
        mSurface.mAlarmLineMarkerColor = colorId;
    }

    @Override
    public void setGpsMarkerColor(int colorId) {
        mSurface.mGpsMarkerColor = colorId;
    }

    @Override
    public void setLineMarkerColor(int colorId) {
        mSurface.mNormalLineMarkerColor = colorId;
    }

    @Override
    public void jumpDate(int year, int month, int day) {
        month = calculateJumpMonth(month);
        day = calculateJumpDate(year, month, day);
        Date date = new Date(year - 1900, month - 1, day);

        mCalendar.setTime(date);
        mCurDate = mCalendar.getTime();
        this.mCurrentSelectedDay = day;
        mSelectedStartDate = mSelectedEndDate = mCurDate;
        invalidate();

        Log.d(TAG, "jumpDate is " + date.toString());
    }

    /**
     * 计算跳转月份
     *
     * @param month
     * @return
     */
    private int calculateJumpMonth(int month) {
        if (month < 1) {
            month = 1;
        } else if (month > 12) {
            month = 12;
        }
        return month;
    }

    /**
     * 计算跳转日期
     *
     * @param year
     * @param month
     * @param day
     * @return
     */
    private int calculateJumpDate(int year, int month, int day) {
        int dayOfMonth = calendarUtils.getDaysOfMonth(calendarUtils.isLeapYear(year), month);
        if (day < 1) {
            day = 1;
        } else if (day > dayOfMonth) {
            day = dayOfMonth;
        }
        return day;
    }

    @Override
    public int getDaysOfMonth(int year, int month) {
        return calendarUtils.getDaysOfMonth(calendarUtils.isLeapYear(year), month);
    }


    /**
     * 1. 布局尺寸
     * 2. 文字颜色，大小
     * 3. 当前日期的颜色，选择的日期颜色
     */
    private class Surface {
        private float density;
        // 整个控件的宽度
        private int mControlWidth;
        // 整个控件的高度
        private int mControlHeight;
        // 显示月的高度
        private float mMonthHeight;
        // 显示星期的高度
        private float mWeekHeight;
        // 日期方框宽度
        private float mCellWidth;
        // 日期方框高度
        private float mCellHeight;
        private float mBorderWidth;
        //画板颜色
        private int mMainBgColor = Color.parseColor("#FFFFFF");
        private int textColor = Color.BLACK;
        //星期文字颜色
        private int mWeekTextColor = Color.BLACK;
        //星期文字大小
        private int mWeekTextSize = 12;

        private int mMonthTextSize = 12;


        //默认日期颜色
        private int defaultMonthTextColor = Color.BLACK;
        //不可用日期颜色
        private int inVisibleMonthTextColor = Color.parseColor("#CCCCCC");
        //星期背景颜色
        private int mWeekBgColor = Color.BLACK;

        //星期圆颜色
        private int mWeekCircleColor = Color.TRANSPARENT;
        //边界线颜色
        private int mBorderLineColor = Color.parseColor("#CCCCCC");
        //当天日期颜色
        private int mTodayNumberColor = Color.RED;
        private int cellDownColor = Color.parseColor("#CCFFFF");
        private int mCellSelectedColor = Color.parseColor("#99CCFF");
        private Paint mBorderPaint;
        private Paint mMonthPaint;
        //绘制星期字体
        private Paint mWeekPaint;
        //绘制星期背景颜色
        private Paint mWeekBgPaint;

        //绘制星期背景圆
        private Paint mWeekCirclePaint;

        private Paint mDatePaint;
        //绘制选中日期背景
        private Paint mCellSelectedBgPaint;
        //绘制视频正常录像圆
        private Paint mVideoNormalRecordPaint;

        //绘制视频报警录像圆
        private Paint mVideoAlarmRecordPaint;

        //绘制视频锁定录像圆
        private Paint mVideoLockRecordPaint;
        //绘制gps小点
        private Paint mGpsPointPaint;
        //gps标记颜色
        private int mGpsMarkerColor = Color.RED;

        //绘制正常录像下标标记
        private Paint mNormalLineMarkerPaint;
        //正常录像标记颜色
        private int mNormalLineMarkerColor = Color.GREEN;


        //绘制报警录像下标标记
        private Paint mAlarmLineMarkerPaint;
        //报警录像标记颜色
        private int mAlarmLineMarkerColor = Color.RED;


        //绘制锁定录像下标标记
        private Paint mLockLineMarkerPaint;
        //锁定录像标记颜色
        private int mLockLineMarkerColor = Color.YELLOW;

        //是否填充
        private boolean isCellBgFill = false;
        //是否为圆
        private boolean isCircle = false;


        //日历背景是否画圆
        private boolean isDayBgCircle = false;

        // 边框路径
        private Path mBoxPath;
        private String[] weekTexts = {"S", "M", "T", "W", "T", "F", "S"};
        private String[] monthText = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};


        public void setWeekText(String[] weekTexts) {
            this.weekTexts = weekTexts;
        }

        public void setMonthText(String[] monthText) {
            this.monthText = monthText;
        }

        public void init() {
            float temp = mControlHeight / 7f;
            mMonthHeight = 0;//(float) ((temp + temp * 0.3f) * 0.6);
            //monthChangeWidth = monthHeight * 1.5f;
            mWeekHeight = (float) ((temp + temp * 0.3f) * 0.7);
            mCellHeight = (mControlHeight - mMonthHeight - mWeekHeight) / 6f;
            mCellWidth = mControlWidth / 7f;

            initBorderPaint();

            initMonthPaint();

            initWeekPaint();

            initDatePaint();

            initBoxPath();

            initCellSelectedBgPaint();

            initNormalVideoPaint();

            initAlarmVideoPaint();

            initLockVideoPaint();

            initGpsPaint();
            initNormalLineMarkerPaint();

            initAlarmLineMarkerPaint();

            initLockLineMarkerPaint();

            initWeekBgPaint();

            initWeekCirclePaint();
        }

        private void initBorderPaint() {
            mBorderPaint = new Paint();
            mBorderPaint.setColor(mBorderLineColor);
            mBorderPaint.setStyle(Paint.Style.STROKE);
            mBorderWidth = (float) (0.5 * density);
            // Log.d(TAG, "borderwidth:" + borderWidth);
            mBorderWidth = mBorderWidth < 1 ? 1 : mBorderWidth;
            mBorderPaint.setStrokeWidth(mBorderWidth);
        }

        private void initBoxPath() {
            mBoxPath = new Path();
            mBoxPath.rLineTo(mControlWidth, 0);
            mBoxPath.moveTo(0, mMonthHeight + mWeekHeight);
            mBoxPath.rLineTo(mControlWidth, 0);
            for (int i = 1; i < 6; i++) {
                mBoxPath.moveTo(0, mMonthHeight + mWeekHeight + i * mCellHeight);
                mBoxPath.rLineTo(mControlWidth, 0);
                mBoxPath.moveTo(i * mCellWidth, mMonthHeight);
                mBoxPath.rLineTo(0, mControlHeight - mMonthHeight);
            }
            mBoxPath.moveTo(6 * mCellWidth, mMonthHeight);
            mBoxPath.rLineTo(0, mControlHeight - mMonthHeight);
        }

        private void initMonthPaint() {
            mMonthPaint = new Paint();
            mMonthPaint.setColor(textColor);
            mMonthPaint.setAntiAlias(true);

//            float textSize = mCellHeight * 0.3f;
            float textSize = mMonthTextSize;
            Log.d(TAG, "text size:" + textSize);
            mMonthPaint.setTextSize(textSize);
            mMonthPaint.setTypeface(Typeface.DEFAULT_BOLD);
        }

        private void initWeekPaint() {
            //设置星期画板
            mWeekPaint = new Paint();
            mWeekPaint.setColor(mWeekTextColor);
            mWeekPaint.setAntiAlias(true);
//            float weekTextSize = mWeekHeight * 0.5f;
            float weekTextSize = mWeekTextSize;
            mWeekPaint.setTextSize(weekTextSize);
            mWeekPaint.setTypeface(Typeface.DEFAULT_BOLD);
        }

        private void initDatePaint() {
            mDatePaint = new Paint();
            mDatePaint.setColor(textColor);
            mDatePaint.setAntiAlias(true);
//            float cellTextSize = mCellHeight * 0.4f;
            float cellTextSize = mMonthTextSize;
            mDatePaint.setTextSize(cellTextSize);
            mDatePaint.setTypeface(Typeface.DEFAULT_BOLD);
        }

        private void initCellSelectedBgPaint() {
            mCellSelectedBgPaint = new Paint();
            mCellSelectedBgPaint.setAntiAlias(true);
            mCellSelectedBgPaint.setStyle(isCellBgFill ? Paint.Style.FILL : Paint.Style.STROKE);
            mCellSelectedBgPaint.setStrokeWidth(5);
            mCellSelectedBgPaint.setColor(mCellSelectedColor);
        }

        private void initWeekBgPaint() {
            mWeekBgPaint = new Paint();
            mWeekBgPaint.setAntiAlias(true);
            mWeekBgPaint.setStyle(Paint.Style.FILL);
            // 星期背景
            mWeekBgPaint.setColor(mWeekBgColor);
        }


        private void initWeekCirclePaint() {
            mWeekCirclePaint = new Paint();

            mWeekCirclePaint.setAntiAlias(true);
            mWeekCirclePaint.setStyle(Paint.Style.FILL);
            // 星期圆背景
            mWeekCirclePaint.setColor(mWeekCircleColor);
        }

        private void initNormalVideoPaint() {
            mVideoNormalRecordPaint = new Paint();
            mVideoNormalRecordPaint.setAntiAlias(true);
            mVideoNormalRecordPaint.setStyle(Paint.Style.FILL);
            mVideoNormalRecordPaint.setStrokeWidth(5);
            mVideoNormalRecordPaint.setColor(mNormalLineMarkerColor);
        }

        private void initAlarmVideoPaint() {
            mVideoAlarmRecordPaint = new Paint();
            mVideoAlarmRecordPaint.setAntiAlias(true);
            mVideoAlarmRecordPaint.setStyle(Paint.Style.FILL);
            mVideoAlarmRecordPaint.setStrokeWidth(5);
            mVideoAlarmRecordPaint.setColor(mAlarmLineMarkerColor);
        }

        private void initLockVideoPaint() {
            mVideoLockRecordPaint = new Paint();
            mVideoLockRecordPaint.setAntiAlias(true);
            mVideoLockRecordPaint.setStyle(Paint.Style.FILL);
            mVideoLockRecordPaint.setStrokeWidth(5);
            mVideoLockRecordPaint.setColor(mLockLineMarkerColor);
        }

        private void initGpsPaint() {
            mGpsPointPaint = new Paint();
            mGpsPointPaint.setAntiAlias(true);
            mGpsPointPaint.setStyle(Paint.Style.FILL);
            mGpsPointPaint.setColor(mGpsMarkerColor);
        }

        private void initNormalLineMarkerPaint() {
            mNormalLineMarkerPaint = new Paint();
            mNormalLineMarkerPaint.setAntiAlias(true);
            mNormalLineMarkerPaint.setStyle(Paint.Style.FILL);
            mNormalLineMarkerPaint.setColor(mNormalLineMarkerColor);
        }

        private void initAlarmLineMarkerPaint() {
            mAlarmLineMarkerPaint = new Paint();
            mAlarmLineMarkerPaint.setAntiAlias(true);
            mAlarmLineMarkerPaint.setStyle(Paint.Style.FILL);
            mAlarmLineMarkerPaint.setColor(mAlarmLineMarkerColor);
        }

        private void initLockLineMarkerPaint() {
            mLockLineMarkerPaint = new Paint();
            mLockLineMarkerPaint.setAntiAlias(true);
            mLockLineMarkerPaint.setStyle(Paint.Style.FILL);
            mLockLineMarkerPaint.setColor(mLockLineMarkerColor);
        }

    }
}
