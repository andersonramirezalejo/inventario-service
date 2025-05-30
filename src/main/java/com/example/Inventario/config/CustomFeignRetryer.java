package com.example.Inventario.config;

import feign.RetryableException;
import feign.Retryer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class CustomFeignRetryer implements Retryer {

    private static final Logger log = LoggerFactory.getLogger(CustomFeignRetryer.class);

    private final int maxAttempts;
    private final long backoffPeriod;
    private final long maxPeriod;
    int attempt;

    public CustomFeignRetryer() {
        this(3, 100, 1000);
    }

    public CustomFeignRetryer(int maxAttempts, long backoffPeriod, long maxPeriod) {
        this.maxAttempts = maxAttempts;
        this.backoffPeriod = backoffPeriod;
        this.maxPeriod = maxPeriod;
        this.attempt = 1;
    }

    @Override
    public void continueOrPropagate(RetryableException e) {
        if (attempt >= maxAttempts) {
            log.error("Excedido el número máximo de reintentos ({}) para la llamada a Feign. Propagando excepción.", maxAttempts);
            throw e;
        }

        long sleepMillis;
        if (e.retryAfter() != null) {
            sleepMillis = TimeUnit.SECONDS.toMillis(e.retryAfter());
        } else {
            // Implementa un "backoff" exponencial: 100ms, 200ms, 400ms...
            sleepMillis = (long) (backoffPeriod * Math.pow(2, attempt -1));
        }
        sleepMillis = Math.min(sleepMillis, maxPeriod);

        log.warn("Reintento #{} para la llamada a Feign debido a: {}. Esperando {} ms.", attempt, e.getMessage(), sleepMillis);
        attempt++;

        try {
            Thread.sleep(sleepMillis);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    @Override
    public Retryer clone() {
        return new CustomFeignRetryer(maxAttempts, backoffPeriod, maxPeriod);
    }
}
