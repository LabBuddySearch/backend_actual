package org.example.security;

import org.example.exception.RateLimitExceededException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SubmissionRateLimiter {

    private final ConcurrentHashMap<String, Deque<Long>> windowByUser = new ConcurrentHashMap<>();

    @Value("${application.rate-limit.submits-per-minute-per-user:30}")
    private int submitsPerMinutePerUser;

    public void check(String userEmail) {
        if (submitsPerMinutePerUser <= 0) {
            return;
        }
        long now = System.currentTimeMillis();
        long cutoff = now - 60_000L;
        Deque<Long> deque = windowByUser.computeIfAbsent(userEmail, k -> new ArrayDeque<>());
        synchronized (deque) {
            while (!deque.isEmpty() && deque.peekFirst() < cutoff) {
                deque.pollFirst();
            }
            if (deque.size() >= submitsPerMinutePerUser) {
                throw new RateLimitExceededException("Слишком много отправок решений. Попробуйте через минуту.");
            }
            deque.addLast(now);
        }
    }
}
