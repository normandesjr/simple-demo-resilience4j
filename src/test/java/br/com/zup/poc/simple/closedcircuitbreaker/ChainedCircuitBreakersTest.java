package br.com.zup.poc.simple.closedcircuitbreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.vavr.CheckedFunction0;
import io.vavr.CheckedFunction1;
import io.vavr.control.Try;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ChainedCircuitBreakersTest {

    @Test
    public void should_chain_function_successfuly() {
        // Given
        CircuitBreaker circuitBreaker = CircuitBreaker.ofDefaults("testName");
        CircuitBreaker anotherCircuitBreaker = CircuitBreaker.ofDefaults("anotherTestName");

        // When I create a Supplier and a Function which are decorated by different CircuitBreakers
        CheckedFunction0<String> decoratedSupplier = CircuitBreaker
                .decorateCheckedSupplier(circuitBreaker, () -> "Hello");

        CheckedFunction1<String, String> decoratedFunction = CircuitBreaker
                .decorateCheckedFunction(anotherCircuitBreaker, (input) -> input + " world");

        // and I chain a function with map
        Try<String> result = Try.of(decoratedSupplier)
                .mapTry(decoratedFunction::apply);

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).isEqualTo("Hello world");
    }
}
