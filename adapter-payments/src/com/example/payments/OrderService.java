package com.example.payments;

import java.util.Map;
import java.util.Objects;

public class OrderService {

    private final Map<String, PaymentGateway> gateways;

    public OrderService(Map<String, PaymentGateway> gateways) {
        this.gateways = Objects.requireNonNull(gateways, "gateways");
    }

    public String charge(String provider, String customerId, int amountCents) {

        Objects.requireNonNull(provider, "provider");

        PaymentGateway gateway = gateways.get(provider);

        if (gateway == null) {
            throw new IllegalArgumentException("Unknown provider: " + provider);
        }

        return gateway.charge(customerId, amountCents);
    }
}