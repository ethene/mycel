package com.quantumresearch.mycel.infrastructure.api.cleanup;

import com.quantumresearch.mycel.infrastructure.api.db.DatabaseComponent;
import com.quantumresearch.mycel.infrastructure.api.db.DbException;
import com.quantumresearch.mycel.infrastructure.api.db.Transaction;
import com.quantumresearch.mycel.infrastructure.api.sync.GroupId;
import com.quantumresearch.mycel.infrastructure.api.sync.MessageId;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.Collection;

/**
 * An interface for registering a hook with the {@link CleanupManager}
 * that will be called when a message's cleanup deadline is reached.
 */
@NotNullByDefault
public interface CleanupHook {

	/**
	 * Called when the cleanup deadlines of one or more messages are reached.
	 * <p>
	 * The callee is not required to delete the messages, but the hook won't be
	 * called again for these messages unless another cleanup timer is set (see
	 * {@link DatabaseComponent#setCleanupTimerDuration(Transaction, MessageId, long)}
	 * and {@link DatabaseComponent#startCleanupTimer(Transaction, MessageId)}).
	 */
	void deleteMessages(Transaction txn, GroupId g,
			Collection<MessageId> messageIds) throws DbException;
}
