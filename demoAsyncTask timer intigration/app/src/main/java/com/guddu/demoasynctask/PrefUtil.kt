package com.guddu.demoasynctask

import android.content.Context
import android.preference.PreferenceManager

class PrefUtil {
    companion object{
        fun getTimerLength(context: Context): Int{
            //placeholder
            return 1
        }
        private const val PRESERVE_TIMER_LENGTH_SECONDS_ID ="com.guddu.basictimer.previous_timer_length"

        fun getPreviousLengthSeconds(context: Context):Long{
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getLong(PRESERVE_TIMER_LENGTH_SECONDS_ID, 0)
        }
        fun setPreviousLengthSeconds(seconds: Long, context: Context){
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putLong(PRESERVE_TIMER_LENGTH_SECONDS_ID,seconds)
            editor.apply()
        }
        private const val TIMER_STATE_ID = "com.guddu.basictimer.timer_state"

        fun getTimerState(context: Context): MainActivity.TimeState{
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val ordinal = preferences.getInt(TIMER_STATE_ID,0)
            return MainActivity.TimeState.values()[ordinal]
        }
        fun setTimerState(state: MainActivity.TimeState, context: Context){
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            val ordinal = state.ordinal
            editor.putInt(TIMER_STATE_ID, ordinal)
            editor.apply()
        }

        private const val SECONDS_REMAINING ="com.guddu.basictimer.seconds_remaining"

        fun getSecondsRemaing(context: Context):Long{
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getLong(SECONDS_REMAINING, 0)
        }
        fun setSecondsRemaing(seconds: Long, context: Context){
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putLong(SECONDS_REMAINING,seconds)
            editor.apply()
        }
        private const val ALARM_SET_TIME_ID = "com.guddu.basictimer.background_time"
        fun getAlarmSetTime(context: Context):Long{
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getLong(ALARM_SET_TIME_ID,0)
        }
        fun setAlarmSetTime(time : Long,context: Context){
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putLong(ALARM_SET_TIME_ID, time)
            editor.apply()
        }
    }
}