package com.guddu.basictimer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.os.CountDownTimer
import android.os.health.TimerStat
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object{
        fun setAlarm(context: Context, nowSeconds: Long, secondsRemaing: Long): Long{
            val wakeUpTime = (nowSeconds + secondsRemaing) * 1000
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,wakeUpTime,pendingIntent)
            PrefUtil.setAlarmSetTime(nowSeconds, context)
            return wakeUpTime

            //val pendingIntent : PendingIntent.getBroadcast(context,0)
        }

        fun removeAlarm(context: Context){
            val intent = Intent(context,TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            PrefUtil.setAlarmSetTime(0,context)

        }

        val nowSeconds :Long
        get() = Calendar.getInstance().timeInMillis / 1000

     }

    enum class TimeState{
        Stopped, Paused, Running
    }

    private lateinit var timer : CountDownTimer
    private var timerLengthSeconds : Long = 0L
    private var timeState = TimeState.Stopped
    private var secondsRemaining = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.setIcon(R.drawable.ic_timer)
        supportActionBar?.title = "    Timer"

        fab_start.setOnClickListener { v ->
            startTimer()
            timeState = TimeState.Running
            updateButtons()
        }
        fab_pause.setOnClickListener { v ->
            timer.cancel()
            timeState = TimeState.Paused
            updateButtons()
        }
        fab_stop.setOnClickListener { v ->
            timer.cancel()
            onTimerFinished()
        }
    }

    override fun onResume() {
        super.onResume()
        initTimer()
        removeAlarm(this)
        // TODO:  hide notification
    }

    override fun onPause() {
        super.onPause()
        if (timeState == TimeState.Running){
            timer.cancel()
            val wakeUpTimer = setAlarm(this, nowSeconds,secondsRemaining)
            //TODO: show notification
        }
        else if(timeState == TimeState.Paused){
            //TODO: show notification
        }
        PrefUtil.setPreviousLengthSeconds(timerLengthSeconds,this)
        PrefUtil.setSecondsRemaing(secondsRemaining,this)
        PrefUtil.setTimerState(timeState,this)
    }

    private fun initTimer(){
        timeState = PrefUtil.getTimerState(this)
        if(timeState==TimeState.Stopped)
            setNewTimerLength()
        else
            setPreviousTimerength()

        secondsRemaining = if(timeState == TimeState.Running || timeState == TimeState.Paused)
            PrefUtil.getSecondsRemaing(this)
        else
            timerLengthSeconds


        //done change seconds remainig where the background timer stopped
        val alarmSetTime = PrefUtil.getAlarmSetTime(this)
        if (alarmSetTime > 0)
            secondsRemaining -= nowSeconds - alarmSetTime

        //resume where we left off
        if(secondsRemaining <= 0)
            onTimerFinished() //this is called when the timer finished in the background
        else if (timeState == TimeState.Running)
            startTimer()
        updateButtons()
        updateCountDownUI()
    }

    private fun onTimerFinished(){
        timeState = TimeState.Stopped
        setNewTimerLength()
        progress_countDown.progress = 0
        PrefUtil.setSecondsRemaing(timerLengthSeconds,this)
        secondsRemaining = timerLengthSeconds
        updateButtons()
        updateCountDownUI()
    }
    private fun startTimer() {
        timeState = TimeState.Running
        timer = object : CountDownTimer(secondsRemaining * 1000, 1000) {


            override fun onFinish() = onTimerFinished()
            override fun onTick(millisUtilFinished: Long) {
                secondsRemaining = millisUtilFinished / 1000
                updateCountDownUI()
            }
        }.start()
    }
    private fun setNewTimerLength(){
        val lengthInMinuts = PrefUtil.getTimerLength(this)
        timerLengthSeconds = (lengthInMinuts * 60L)
        progress_countDown.max = timerLengthSeconds.toInt()
    }

    private fun setPreviousTimerength(){
        timerLengthSeconds = PrefUtil.getPreviousLengthSeconds(this)
        progress_countDown.max = timerLengthSeconds.toInt()
    }
    private fun updateCountDownUI(){
        val minutesUntilFinished = secondsRemaining / 60
        val secondsinMinutesUntillFinished = secondsRemaining - minutesUntilFinished * 60
        val secondStr = secondsinMinutesUntillFinished.toString()
        txt_countDown.text = "$minutesUntilFinished:${
        if(secondStr.length == 2)
            secondStr
        else "0" + secondStr}"
        progress_countDown.progress = (timerLengthSeconds - secondsRemaining).toInt()
    }

    private fun updateButtons(){
        when(timeState){
            TimeState.Running -> {
                fab_start.isEnabled = false
                fab_stop.isEnabled = true
                fab_pause.isEnabled = true
            }
            TimeState.Stopped ->{
                fab_start.isEnabled = true
                fab_pause.isEnabled = false
                fab_stop.isEnabled = false
            }
            TimeState.Paused ->{
                fab_start.isEnabled = true
                fab_pause.isEnabled = false
                fab_stop.isEnabled = true
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
