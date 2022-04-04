package com.rbiedrawa.app.todos;

import com.rbiedrawa.app.core.scheduler.JobException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TodoService {
    private final Scheduler scheduler;

    public String schedule(TodoDto todo) {
        final var jobDetail = JobBuilder.newJob(TodoJob.class)
                .withIdentity("todoJob-" + todo.getId(), "todoGroup")
                .withDescription("Todo odd/even job")
                .usingJobData(new JobDataMap(Map.of("todoId", todo.getId())))
                .storeDurably(false)
                .build();
        final var trigger = createNextTrigger(jobDetail, ZonedDateTime.now().plusSeconds(2));
        schedule(jobDetail, trigger);
        return jobDetail.getKey().toString();
    }

    private void schedule(JobDetail jobDetail, Trigger nextTrigger) {
        try {
            scheduler.scheduleJob(jobDetail, nextTrigger);
            log.info("Job {} successfully scheduled with trigger {}", jobDetail, nextTrigger);
        } catch (SchedulerException ex) {
            throw JobException.unableToScheduleNewJob(jobDetail.getKey().toString(), ex);
        }
    }

    private Trigger createNextTrigger(JobDetail jobDetail, ZonedDateTime triggerStartTime) {
        final var jobKey = jobDetail.getKey();
        return TriggerBuilder.newTrigger()
                .withIdentity(jobKey.getName(), jobKey.getGroup())
                .withDescription(jobDetail.getDescription())
                .forJob(jobDetail)
                .startAt(Date.from(triggerStartTime.toInstant()))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();
    }
}
