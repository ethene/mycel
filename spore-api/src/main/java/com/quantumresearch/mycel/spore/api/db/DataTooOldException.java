package com.quantumresearch.mycel.spore.api.db;

/**
 * Thrown when the database uses an older schema than the current code and
 * cannot be migrated.
 */
public class DataTooOldException extends DbException {
}
