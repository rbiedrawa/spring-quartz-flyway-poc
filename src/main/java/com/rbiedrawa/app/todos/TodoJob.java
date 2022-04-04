package com.rbiedrawa.app.todos;

import com.rbiedrawa.app.core.scheduler.RetryableJobBean;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;

@Slf4j
public class TodoJob extends RetryableJobBean {

    @Override
    public void executeJob(JobExecutionContext context) {
        final var dataMap = context.getMergedJobDataMap();
        final var todoId = dataMap.getLong("todoId");

        if(isEven(todoId)) {
            log.info("Todo [{}] is even😇", todoId);
        } else {
            log.error("Todo [{}] is odd - simulating failure😈", todoId);
            throw new RuntimeException(String.format("Todo %s is odd", todoId));
        }
    }

    boolean isEven(long num) { return ((num % 2) == 0); }
}
