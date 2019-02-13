package br.com.zup.poc.simple.recoveringfromexception;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.vavr.CheckedFunction0;
import io.vavr.control.Try;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleRecoveryExceptionTest {

    @Test
    public void should_recover_from_any_exception() {
        // Given
        CircuitBreaker circuitBreaker = CircuitBreaker.ofDefaults("testName");

        // When I decorate my function and invoke the decorated function
        CheckedFunction0<String> checkedSupplier = CircuitBreaker.decorateCheckedSupplier(circuitBreaker, () -> {
            throw new RuntimeException("BAM!");
        });
        Try<String> result = Try.of(checkedSupplier)
                .recover(throwable -> "Hello Recovery");

        // Then the function should be a success, because the exception could be recovered
        assertThat(result.isSuccess()).isTrue();
        // and the result must match the result of the recovery function.
        assertThat(result.get()).isEqualTo("Hello Recovery");

    }

}
