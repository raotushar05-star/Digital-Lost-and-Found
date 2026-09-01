package com.lostandfound.util;

import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class CaseNumberGenerator {

    private final AtomicLong sequence = new AtomicLong(System.currentTimeMillis() % 1_000_000);

    public String next() {
        int year = Year.now().getValue();
        long seq = sequence.incrementAndGet();
        return String.format("LF-%d-%06d", year, seq % 1_000_000);
    }
}
