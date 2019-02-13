package br.com.zup.poc.simple.opencircuitbreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerOpenException;
import io.vavr.control.Try;
import org.junit.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleOpenedCircuitBreakerTest {

    @Test
    public void shold_not_call_function() {
        // Given
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .ringBufferSizeInClosedState(2)
                .waitDurationInOpenState(Duration.ofMillis(1000))
                .build();
        CircuitBreaker circuitBreaker = CircuitBreaker.of("testName", circuitBreakerConfig);

        // Simulate a failure attempt
        circuitBreaker.onError(0, new RuntimeException());

        // CircuitBreaker is still CLOSED, because 1 failure is allowed
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);

        // Simulate a failure attempt
        circuitBreaker.onError(0, new RuntimeException());

        // CircuitBreaker is OPEN, because the failure rate is above 50%
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        // When I decorate my function and invoke the decorated function
        Try<String> result = Try.of(CircuitBreaker.decorateCheckedSupplier(circuitBreaker, () -> "Hello"))
                .map(value -> value + " world");

        // Then the call fails, because CircuitBreaker is OPEN
        assertThat(result.isFailure()).isTrue();
        // Exception is CircuitBreakerOpenException
        assertThat(result.failed().get()).isInstanceOf(CircuitBreakerOpenException.class);
    }

}
