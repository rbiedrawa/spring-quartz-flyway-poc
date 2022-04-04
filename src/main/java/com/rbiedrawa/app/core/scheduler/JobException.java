package com.rbiedrawa.app.core.scheduler;


public class JobException extends RuntimeException {

    public JobException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public static JobException unableToScheduleNewJob(String jobName, Throwable cause) {
        return new JobException(String.format("Unable to schedule new job %s", jobName), cause);
    }

    public static JobException unableToRescheduleJob(String jobName, Throwable cause) {
        return new JobException(String.format("Unable to reschedule job %s", jobName), cause);
    }
}
