package br.com.zup.poc.simple.closedcircuitbreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.vavr.CheckedFunction0;
import io.vavr.control.Try;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleCircuitBreakerTest {

    @Test
    public void should_chain_function_successfuly() {
        // Given
        CircuitBreaker circuitBreaker = CircuitBreaker.ofDefaults("testName");

        // When I decorate my function
        CheckedFunction0<String> decoratedSupplier = CircuitBreaker
                .decorateCheckedSupplier(circuitBreaker, () -> "This can be any method which returns: 'Hello");

        // and chain an other function with map
        Try<String> result = Try.of(decoratedSupplier)
                .map(value -> value + " world'");

        // Then the Try Monad returns a Success<String>, if all functions ran successfully.
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).isEqualTo("This can be any method which returns: 'Hello world'");
    }
}
