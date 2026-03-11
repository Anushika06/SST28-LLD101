# Adapter Pattern Refactoring — Payment Gateways

## Overview

The system processes payments through multiple third-party SDKs such as **FastPay** and **SafeCash**.
However, each provider exposes a **different API**, making it difficult for the main service (`OrderService`) to interact with them uniformly.

The system was refactored using the **Adapter Design Pattern** so that the application interacts with a **single interface (`PaymentGateway`)**, while adapters translate requests to the provider-specific SDK APIs.

---
# 1. OrderService.java

## Problem (Original Code)

`OrderService` originally contained provider-specific logic and had to know how each payment provider worked. This created tight coupling between the service layer and payment SDKs.

It also relied on provider identifiers to determine which payment logic to execute.

## Improvement

`OrderService` now depends only on the **`PaymentGateway` interface**.

```java
PaymentGateway gateway = gateways.get(provider);
return gateway.charge(customerId, amountCents);
```

The service simply selects a gateway from the registry and calls the common method. 

### Benefits

* Removes provider-specific logic
* Follows **Open/Closed Principle**
* New payment providers can be added without modifying `OrderService`

---

# 2. PaymentGateway.java

## Problem (Original Design)

There was no unified interface for payment providers, so the service had to interact directly with different SDK methods.

## Improvement

A common interface was introduced:

```java
String charge(String customerId, int amountCents);
```

All adapters implement this interface. 

### Benefits

* Provides a **standard contract for all payment providers**
* Decouples business logic from third-party SDKs

---

# 3. FastPayClient.java

## Problem

The FastPay SDK provides its own method:

```java
payNow(String custId, int amountCents)
```

This method does not match the application's payment interface. 

## Improvement

A **FastPayAdapter** was created to translate the application interface into the SDK method call.

---

# 4. FastPayAdapter.java

## Improvement

This adapter wraps the FastPay SDK and implements the `PaymentGateway` interface.

```java
return client.payNow(customerId, amountCents);
```

It converts the generic `charge()` call into the FastPay API call. 

### Benefits

* Isolates FastPay-specific logic
* Allows FastPay to be used through the common interface

---

# 5. SafeCashClient.java

## Problem

SafeCash uses a **two-step workflow**:

1. Create payment
2. Confirm payment

```java
SafeCashPayment payment = client.createPayment(amountCents, customerId);
return payment.confirm();
```



This workflow does not match the application's single-method payment call.

## Improvement

The workflow is hidden inside an adapter.

---

# 6. SafeCashAdapter.java

## Improvement

This adapter converts the SafeCash multi-step process into a single method defined by the `PaymentGateway` interface.

```java
SafeCashPayment payment = client.createPayment(amountCents, customerId);
return payment.confirm();
```

The adapter hides SDK complexity from the application. 

### Benefits

* Simplifies payment logic
* Keeps SDK-specific logic out of business code

---

# 7. SafeCashPayment.java

## Purpose

Represents the SafeCash payment transaction object and provides a `confirm()` method that finalizes the payment.

```java
public String confirm()
```

The adapter calls this internally. 

---

# 8. App.java

## Improvement

The application registers payment adapters in a **gateway registry**:

```java
gateways.put("fastpay", new FastPayAdapter(new FastPayClient()));
gateways.put("safecash", new SafeCashAdapter(new SafeCashClient()));
```

Then `OrderService` uses this registry to select the correct gateway. 

### Benefits

* Easy to configure providers
* New providers can be added without modifying existing service logic

---

# Final Outcome

After refactoring:

* `OrderService` depends only on the **PaymentGateway interface**
* SDK-specific logic is handled by **Adapters**
* FastPay and SafeCash APIs are unified behind a single interface
* New payment providers can be added **without modifying business logic**

