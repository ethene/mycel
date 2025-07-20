package com.quantumresearch.mycel.infrastructure.api.system;

import android.content.Intent;

import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface AlarmListener {

	void onAlarm(Intent intent);
}
