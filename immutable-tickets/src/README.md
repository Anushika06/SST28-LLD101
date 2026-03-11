# Immutable Incident Ticket Refactoring

## Overview

The original implementation of **HelpLite Incident Tickets** allowed tickets to be modified after creation. This caused issues such as inconsistent audit logs, unsafe state changes, and scattered validation logic.

The system was refactored to make `IncidentTicket` an **immutable class** using the **Builder Pattern**, ensuring safer object creation and better maintainability.

---

# 1. IncidentTicket.java

## Problem

The original `IncidentTicket` class was **mutable**, which caused several issues:

* Fields were not `final`, allowing modification after object creation.
* Public setter methods allowed external code to change ticket state.
* Multiple constructors allowed creation of **partially initialized objects**.
* The `tags` list was returned directly, leaking the internal mutable state.
* `setTags()` stored external list references, allowing outside code to modify the ticket indirectly.
* Validation was not enforced during object creation.

These problems made the system **unsafe, unpredictable, and difficult to audit**.

## Improvements

The class was refactored into an **immutable design** with the following changes:

* The class was declared `final` to prevent subclass modification.
* All fields were made `private final`, ensuring they cannot change after creation. 
* All setter methods were removed.
* A **Builder Pattern** was introduced for controlled object creation.
* Required fields (`id`, `reporterEmail`, `title`) are enforced through the builder constructor.
* Optional fields are configured using fluent builder methods.
* Validation is performed inside `Builder.build()`.
* Defensive copying was implemented for the `tags` list:

```java
this.tags = Collections.unmodifiableList(new ArrayList<>(b.tags));
```

This ensures external code cannot modify the internal list. 

* A `toBuilder()` method was added to safely create modified copies of existing tickets.

---

# 2. TicketService.java

## Problem

The original service layer created tickets and then **mutated them afterward**, which violated immutability principles.

Example issues:

* Ticket fields were modified after creation using setters.
* Escalation and assignment methods directly changed the ticket object.
* Service methods changed internal ticket state rather than producing new instances.

This caused **unexpected state changes and poor auditability**.

## Improvements

The service was refactored to work with immutable objects.

* Tickets are now created using the **Builder Pattern**:

```java
IncidentTicket
    .builder(id, reporterEmail, title)
    .priority("MEDIUM")
    .source("CLI")
    .customerVisible(false)
    .addTag("NEW")
    .build();
```

* Instead of modifying an existing ticket, service methods now **create a new instance** using `toBuilder()`.

Example:

```java
return t.toBuilder()
        .priority("CRITICAL")
        .addTag("ESCALATED")
        .build();
```

This ensures the **original ticket remains unchanged**, maintaining immutability. 

---

# 3. TryIt.java

## Problem

The original demo showed how mutable tickets could be altered unexpectedly:

* Service methods modified the same ticket instance.
* External code could modify the `tags` list using `getTags()`.

This demonstrated how mutable objects can lead to **uncontrolled state changes**.

## Improvements

The demo was updated to verify immutability.

Key changes:

* Service methods now return **new ticket instances** after updates:

```java
t = service.assign(t, "agent@example.com");
t = service.escalateToCritical(t);
```

* Attempts to modify the `tags` list externally now throw an exception:

```java
tags.add("HACKED_FROM_OUTSIDE");
```

This results in an `UnsupportedOperationException`, confirming that the ticket is immutable. 

---

# 4. Validation.java

## Problem

In the original system, validation logic was **scattered across the service layer**, making it inconsistent and easy to miss required checks.

## Improvements

Validation is now **centralized and consistently used** inside the Builder.

Examples include:

* Ticket ID validation
* Email validation
* Priority validation
* SLA range validation

Builder validation example:

```java
Validation.requireTicketId(id);
Validation.requireEmail(reporterEmail, "reporterEmail");
Validation.requireRange(slaMinutes, 5, 7200, "slaMinutes");
```

This ensures **all validation occurs at the time of object creation**, preventing invalid tickets. 

---

# Final Outcome

After refactoring:

* `IncidentTicket` is now **fully immutable**.
* Tickets cannot be modified after creation.
* All updates create **new ticket instances**.
* Validation is centralized and enforced during object construction.
* Internal collections are protected using **defensive copying**.
* The system is safer, more predictable, and easier to maintain.

---
