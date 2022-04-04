package com.rbiedrawa.app.core.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;
import org.quartz.impl.triggers.SimpleTriggerImpl;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import static java.util.Objects.requireNonNullElseGet;

@Slf4j
public abstract class RetryableJobBean extends QuartzJobBean {

    public abstract void executeJob(final JobExecutionContext context);

    @Override
    public final void executeInternal(final JobExecutionContext context) {
        try {
            executeJob(context);
        } catch (Throwable ex) {
            log.error(
                    "Job {} failed with exception. Rescheduling...",
                    context.getJobDetail().getKey(),
                    ex);
            reschedule(context);
        }
    }

    protected void reschedule(final JobExecutionContext context) {
        reschedule(context, null);
    }

    protected void reschedule(final JobExecutionContext context, Date nextStartTime) {
        final var trigger = Optional.of(context)
                .map(JobExecutionContext::getTrigger)
                .filter(SimpleTriggerImpl.class::isInstance)
                .map(SimpleTriggerImpl.class::cast)
                .orElseThrow(() -> new IllegalStateException("Only SimpleTrigger supported for rescheduling!"));

        final var timesTriggered = trigger.getTimesTriggered();

        log.debug("trigger: {} fired [{}] times", trigger.getFullName(), timesTriggered);

        trigger.setStartTime(requireNonNullElseGet(nextStartTime, () -> calculateNextAttemptDate(timesTriggered)));
        rescheduleJob(trigger, context);
    }

    private Date calculateNextAttemptDate(final int retryNumber) {
        final var timeToAddSec = ExponentialBackOff.calculate(2L, 2L, retryNumber);
        return Date.from(Instant.now().plusSeconds(timeToAddSec));
    }

    private void rescheduleJob(final SimpleTriggerImpl trigger, final JobExecutionContext context) {
        try {
            context.getScheduler().rescheduleJob(trigger.getKey(), trigger);
            log.info(
                    "Successfully rescheduled job {} using new trigger {} ",
                    context.getJobDetail().getKey(),
                    trigger.getKey());
        } catch (SchedulerException ex) {
            throw JobException.unableToRescheduleJob(trigger.getJobName(), ex);
        }
    }
}
