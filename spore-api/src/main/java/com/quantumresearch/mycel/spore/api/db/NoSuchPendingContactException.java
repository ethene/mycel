package com.quantumresearch.mycel.spore.api.db;

/**
 * Thrown when a database operation is attempted for a pending contact that is
 * not in the database. This exception may occur due to concurrent updates and
 * does not indicate a database error.
 */
public class NoSuchPendingContactException extends DbException {
}
