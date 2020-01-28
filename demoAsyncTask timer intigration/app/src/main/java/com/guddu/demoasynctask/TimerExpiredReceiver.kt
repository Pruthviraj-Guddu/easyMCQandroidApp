package com.guddu.demoasynctask

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TimerExpiredReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
       //TODO: show ntification finished
        PrefUtil.setTimerState(MainActivity.TimeState.Stopped, context)
        PrefUtil.setAlarmSetTime(0,context)
    }
}
