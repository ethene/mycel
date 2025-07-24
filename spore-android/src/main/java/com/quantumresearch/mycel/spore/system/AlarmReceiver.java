package com.quantumresearch.mycel.spore.system;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.quantumresearch.mycel.spore.BrambleApplication;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context ctx, Intent intent) {
		BrambleApplication app =
				(BrambleApplication) ctx.getApplicationContext();
		app.getBrambleAppComponent().alarmListener().onAlarm(intent);
	}
}
