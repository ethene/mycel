package com.quantumresearch.mycel.spore.api.system;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation for injecting the executor for long-running IO tasks that should
 * run without sleeping. Also used for annotating methods that should run on
 * this executor.
 * <p>
 * The contract of this executor is that tasks may be run concurrently, and
 * submitting a task will never block. Tasks must not run indefinitely. Tasks
 * submitted during shutdown are discarded.
 */
@Qualifier
@Target({FIELD, METHOD, PARAMETER})
@Retention(RUNTIME)
public @interface WakefulIoExecutor {
}