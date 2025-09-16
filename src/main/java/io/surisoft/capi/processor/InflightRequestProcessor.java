package io.surisoft.capi.processor;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class InflightRequestProcessor implements Processor {

    private final AtomicInteger inflightRequests = new AtomicInteger(0);

    public InflightRequestProcessor(MeterRegistry meterRegistry) {
        Gauge.builder("capi.inflight.requests", inflightRequests, AtomicInteger::get)
                .description("Number of inflight requests in CAPI")
                .register(meterRegistry);
    }

    @Override
    public void process(Exchange exchange) {
        inflightRequests.incrementAndGet();
        exchange.getUnitOfWork().addSynchronization(new org.apache.camel.spi.Synchronization() {
            @Override
            public void onComplete(Exchange exchange) {
                inflightRequests.decrementAndGet();
            }

            @Override
            public void onFailure(Exchange exchange) {
                inflightRequests.decrementAndGet();
            }
        });
    }
}
