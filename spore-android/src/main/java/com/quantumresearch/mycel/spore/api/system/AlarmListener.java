package com.quantumresearch.mycel.spore.api.system;

import android.content.Intent;

import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface AlarmListener {

	void onAlarm(Intent intent);
}
