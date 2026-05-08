package com.parkingit.cloud.payments.infrastructure.adapter;

import com.parkingit.cloud.payments.domain.model.valueobjects.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.NoSuchElementException;

@Component
@AllArgsConstructor
@Slf4j
public class PaymentGatewayFactory {
    private final List<PaymentGatewayAdapter> adapters;

    public PaymentGatewayAdapter getAdapter(PaymentMethod method) {
        log.debug("[PaymentGatewayFactory] Getting adapter for method: {}", method);

        PaymentGatewayAdapter adapter = adapters.stream()
                .filter(a -> a.getMethod() == method)
                .findFirst()
                .orElseThrow(() -> {
                    log.error("[PaymentGatewayFactory] No adapter found for method: {}", method);
                    return new NoSuchElementException("No adapter for payment method: " + method);
                });

        log.debug("[PaymentGatewayFactory] Adapter found: {}", adapter.getClass().getSimpleName());
        return adapter;
    }

    public List<PaymentGatewayAdapter> getAllAdapters() {
        return adapters;
    }
}
