package com.quantumresearch.mycel.app.util;

import com.quantumresearch.mycel.spore.api.FormatException;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import static com.quantumresearch.mycel.spore.util.ValidationUtils.checkRange;
import static com.quantumresearch.mycel.app.api.autodelete.AutoDeleteConstants.MAX_AUTO_DELETE_TIMER_MS;
import static com.quantumresearch.mycel.app.api.autodelete.AutoDeleteConstants.MIN_AUTO_DELETE_TIMER_MS;
import static com.quantumresearch.mycel.app.api.autodelete.AutoDeleteConstants.NO_AUTO_DELETE_TIMER;

@Immutable
@NotNullByDefault
public class ValidationUtils {

	public static long validateAutoDeleteTimer(@Nullable Long timer)
			throws FormatException {
		if (timer == null) return NO_AUTO_DELETE_TIMER;
		checkRange(timer, MIN_AUTO_DELETE_TIMER_MS, MAX_AUTO_DELETE_TIMER_MS);
		return timer;
	}
}
