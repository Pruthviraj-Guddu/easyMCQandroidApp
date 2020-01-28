@file:Suppress("DEPRECATION")

package com.guddu.demoasynctask

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Build.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONException
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.os.CountDownTimer
import android.os.health.TimerStat


import kotlinx.android.synthetic.main.activity_main.*
//import kotlinx.android.synthetic.main.content_main.*
import java.util.*

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    lateinit var context: Context
    var questionList : MutableList<Question> = ArrayList()
    var index= -1
    var score = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = this
        btn_next.isEnabled=false
        btn_next.alpha - 0.5.toFloat()
        startTimer()
        GetQuestion().execute()
    }
    @SuppressLint("SetTextI18n")

    fun updateQuestion(){
        val selected = rg_choice.checkedRadioButtonId
        if(selected==-1){
            Toast.makeText(this,"Please select an answer.",Toast.LENGTH_SHORT).show()
            return
        }
        if(index<questionList.size){
            when(selected){
                rb_choice1.id ->{
                    if(questionList[index].Answer ==1)
                        score++
                }
                rb_choice2.id ->{
                    if(questionList[index].Answer ==2)
                        score++
                }
                rb_choice3.id ->{
                    if(questionList[index].Answer ==3)
                        score++
                }
                rb_choice4.id ->{
                    if(questionList[index].Answer ==4)
                        score++
                }

            }
            index++
            if (index<questionList.size){
                tv_question.text = questionList[index].Question
                rb_choice1.text= questionList[index].Option1
                rb_choice2.text= questionList[index].Option2
                rb_choice3.text= questionList[index].Option3
                rb_choice4.text= questionList[index].Option4
                rg_choice.clearCheck()
                if ((index+1)==questionList.size)
                    btn_next.text="Finish"
            }else{
                val dialog = AlertDialog.Builder(context)
                dialog.setTitle("Result..")
                dialog.setMessage(" you have correctly answered " + score + " out of " +questionList.size + " Questions " )
                dialog.setPositiveButton("Close") { dialogInterface: DialogInterface, i: Int -> dialogInterface.dismiss()
                    finish()
                }
                dialog.show()
            }
        }
        override fun onResume() {
            super.onResume()
            initTimer()
            removeAlarm(this)
            // TODO:  hide notification
        }

        fun setAlarm(mainActivity: MainActivity, nowSeconds: Any, secondsRemaining: Long): Any {
            val wakeUpTime = (nowSeconds + secondsRemaing) * 1000
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,wakeUpTime,pendingIntent)
            PrefUtil.setAlarmSetTime(nowSeconds, context)
            return wakeUpTime

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


        fun startTimer() {
            timeState = TimeState.Running
            timer = object : CountDownTimer(secondsRemaining * 1000, 1000) {


                override fun onFinish() = onTimerFinished()
                override fun onTick(millisUtilFinished: Long) {
                    secondsRemaining = millisUtilFinished / 1000
                    updateCountDownUI()
                }
            }.start()
        }

        fun setNewTimerLength() {
            val lengthInMinuts = PrefUtil.getTimerLength(this)
            timerLengthSeconds = (lengthInMinuts * 60L)
            progress_countDown.max = timerLengthSeconds.toInt()
        }

        fun setPreviousTimerength() {
            timerLengthSeconds = PrefUtil.getPreviousLengthSeconds(this)
            progress_countDown.max = timerLengthSeconds.toInt()
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

        fun updateButtons() {
//            private fun updateButtons(){
//                when(timeState){
//                    TimeState.Running -> {
//                        fab_start.isEnabled = false
//                        fab_stop.isEnabled = true
//                        fab_pause.isEnabled = true
//                    }
//                    TimeState.Stopped ->{
//                        fab_start.isEnabled = true
//                        fab_pause.isEnabled = false
//                        fab_stop.isEnabled = false
//                    }
//                    TimeState.Paused ->{
//                        fab_start.isEnabled = true
//                        fab_pause.isEnabled = false
//                        fab_stop.isEnabled = true
//                    }
//                }
//            }
        }

        fun setNewTimerLength() {
            val lengthInMinuts = PrefUtil.getTimerLength(this)
            timerLengthSeconds = (lengthInMinuts * 60L)
            progress_countDown.max = timerLengthSeconds.toInt()
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
            //txt_countDown.text = "$minutesUntilFinished:${
            if(secondStr.length == 2)
                secondStr
            else "0" + secondStr}"
            progress_countDown.progress = (timerLengthSeconds - secondsRemaining).toInt()
        }

    private fun onTimerFinished() {
        timeState = TimeState.Stopped
        setNewTimerLength()
        progress_countDown.progress = 0
        PrefUtil.setSecondsRemaing(timerLengthSeconds,this)
        secondsRemaining = timerLengthSeconds
        updateButtons()
        updateCountDownUI()
    }

    private fun updateButtons() {
//        when(timeState){
//            TimeState.Running -> {
//                fab_start.isEnabled = false
//                fab_stop.isEnabled = true
//                fab_pause.isEnabled = true
//            }
//            TimeState.Stopped ->{
//                fab_start.isEnabled = true
//                fab_pause.isEnabled = false
//                fab_stop.isEnabled = false
//            }
//            TimeState.Paused ->{
//                fab_start.isEnabled = true
//                fab_pause.isEnabled = false
//                fab_stop.isEnabled = true
//            }
//        }
    }

    private fun updateCountDownUI() {
        val minutesUntilFinished = secondsRemaining / 60
        val secondsinMinutesUntillFinished = secondsRemaining - minutesUntilFinished * 60
        val secondStr = secondsinMinutesUntillFinished.toString()
//        txt_countDown.text = "$minutesUntilFinished:${
        if(secondStr.length == 2)
            secondStr
        else "0" + secondStr}"
//        progress_countDown.progress = (timerLengthSeconds - secondsRemaining).toInt()
    }
}


    @SuppressLint("StaticFieldLeak")
    internal inner class GetQuestion : AsyncTask<Void,Void,String>(){

        lateinit var progressDialog : ProgressDialog
        var hasInternet = false

        override fun onPreExecute() {
            super.onPreExecute()
            progressDialog = ProgressDialog(context)
            progressDialog.setMessage("Downloading Question")
            progressDialog.setCancelable(false)
            progressDialog.show()
            //println("Hello till downloading question")

        }
        @RequiresApi(VERSION_CODES.CUPCAKE)
        public override fun doInBackground(vararg params: Void?): String {
            if (isNetworkAvailable()){
                hasInternet = true
                val client = OkHttpClient()
                var url : String = "https://script.googleusercontent.com/macros/echo?user_content_key=1tgBN-ES-vsiLin8Lggs7R094sUSEWlBY3Lv7yLt0KnrexUuaTvreORsTenxGH0HaPDQ0rUkXVqmkc903P_gQrpXCbi98gcsm5_BxDlH2jW0nuo2oDemN9CCS2h10ox_1xSncGQajx_ryfhECjZEnBg4Wj9So2Q_mI0_S0Bm21-AGmcRnplmVaRcxvVzvCi9cnQQJegsnVb9TgJzPufw35cdv3aNHr6K&lib=MKMzvVvSFmMa3ZLOyg67WCThf1WVRYg6Z"
                var request = Request.Builder().url(url).build()
                var response = client.newCall(request).execute()
                println("quest down \n\n\n")
                println(response)
                return response.body?.string().toString()
            }
            else
            {
                //return ""
//                val duration = Toast.LENGTH_SHORT
//
//                val toast = Toast.makeText(applicationContext, "Cant connect Internet", duration)
//                toast.show()

//                this.progressDialog = ProgressDialog(context)
//                this.progressDialog.setMessage("No internet")
//                this.progressDialog.setCancelable(false)
//                this.progressDialog.show()
                return ""

            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            progressDialog.dismiss()

            if(hasInternet){
                try{
                    var resultArray = JSONArray(result)
                    println("***********resultArray*************")
                    println(resultArray)
                    for (i in 0 until resultArray.length()){
                        var currentObject = resultArray.getJSONObject(i)
                        var obj = Question()
                        //println("hello Worls")
                        obj.Question=currentObject.getString("Question")
                        obj.Option1=currentObject.getString("Option1")
                        obj.Option2=currentObject.getString("Option2")
                        obj.Option3=currentObject.getString("Option3")
                        obj.Option4=currentObject.getString("Option4")
                        obj.Answer=currentObject.getInt("Answer")
                        questionList.add(obj)



                    }
                    if(index==-1){
                        index++
                        tv_question.text = questionList[index].Question
                        rb_choice1.text= questionList[index].Option1
                        rb_choice2.text= questionList[index].Option2
                        rb_choice3.text= questionList[index].Option3
                        rb_choice4.text= questionList[index].Option4
                    }
                    btn_next.isEnabled=true
                    btn_next.alpha=1.toFloat()
                    btn_next.setOnClickListener {
                        updateQuestion()
                    }

                }catch (e: JSONException){

                }
            }

        }

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



    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }



}



