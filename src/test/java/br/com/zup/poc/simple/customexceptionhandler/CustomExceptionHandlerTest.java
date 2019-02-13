package br.com.zup.poc.simple.customexceptionhandler;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.vavr.CheckedRunnable;
import io.vavr.control.Try;
import org.junit.Test;

import javax.xml.ws.WebServiceException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.Duration;

import static io.vavr.API.*;
import static org.assertj.core.api.Assertions.assertThat;
import static io.vavr.Predicates.instanceOf;

public class CustomExceptionHandlerTest {

    @Test
    public void should_ignore_ioexception_as_error() {
        // Given
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .ringBufferSizeInClosedState(2)
                .ringBufferSizeInHalfOpenState(2)
                .waitDurationInOpenState(Duration.ofMillis(1000))
                .recordFailure(throwable -> Match(throwable).of(
                        Case($(instanceOf(WebServiceException.class)), true),
                        Case($(), false)))
                .build();
        CircuitBreaker circuitBreaker = CircuitBreaker.of("testName", circuitBreakerConfig);

        // Simulate a failure attempt
        circuitBreaker.onError(0, new WebServiceException());
        // CircuitBreaker is still CLOSED, because 1 failure is allowed
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);

        //When
        CheckedRunnable checkedRunnable = CircuitBreaker.decorateCheckedRunnable(circuitBreaker, () -> {
            throw new SocketTimeoutException("BAM!");
        });
        Try result = Try.run(checkedRunnable);

        //Then
        assertThat(result.isFailure()).isTrue();
        // CircuitBreaker is still CLOSED, because SocketTimeoutException has not been recorded as a failure
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        assertThat(result.failed().get()).isInstanceOf(IOException.class);

    }
}
