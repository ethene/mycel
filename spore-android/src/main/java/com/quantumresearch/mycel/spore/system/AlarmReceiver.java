package com.quantumresearch.mycel.spore.system;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.quantumresearch.mycel.spore.SporeApplication;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context ctx, Intent intent) {
		SporeApplication app =
				(SporeApplication) ctx.getApplicationContext();
		app.getSporeAppComponent().alarmListener().onAlarm(intent);
	}
}
