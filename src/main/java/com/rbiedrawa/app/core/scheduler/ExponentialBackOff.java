package com.rbiedrawa.app.core.scheduler;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ExponentialBackOff {

    // TODO: maybe add Jitter🤔
    public static long calculate(long interval, long exponentialRate, long retryNumber) {
        return interval * (long) Math.pow(exponentialRate, retryNumber);
    }
}
