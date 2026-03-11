# Proxy Pattern Refactoring — CampusVault Reports

## Overview

The CampusVault CLI tool allows users to view internal reports.
In the original implementation, reports were accessed directly from disk without any control mechanism. This caused several issues such as lack of access control, repeated expensive file loading, and tight coupling between components.

The system was refactored using the **Proxy Design Pattern** to introduce:

* **Access control**
* **Lazy loading of reports**
* **Caching of loaded reports**
* **Better abstraction using interfaces**

---

# 1. ReportFile.java

## Problem

`ReportFile` handled both **file loading and report display** directly.

Every time `display()` was called, the report was loaded again from disk:

```java
String content = loadFromDisk();
```

This caused:

* Expensive disk operations on every call
* No caching of report content
* Tight coupling between clients and the concrete class.

## Improvement

The expensive loading logic was **moved to `RealReport`**, which acts as the **Real Subject** in the Proxy pattern.

`ReportFile` is no longer used by clients directly. Instead, the system now uses `ReportProxy` and `RealReport`.

---

# 2. RealReport.java

## Problem

In the original code, the real subject logic was not implemented and the class contained placeholder behavior.

## Improvement

`RealReport` now represents the **real object responsible for loading report data**.

The expensive disk loading happens in the constructor:

```java
this.content = loadFromDisk();
```

The report is loaded only once and then reused. 

This separates **expensive operations from client interaction**.

---

# 3. ReportProxy.java

## Problem

The original proxy implementation did not implement any real proxy behavior.
It simply created a new `RealReport` every time `display()` was called, which defeated the purpose of the proxy.

## Improvement

The proxy now performs the core responsibilities of the Proxy pattern:

### 1. Access Control

Before loading the report, the proxy checks whether the user has permission:

```java
if (!accessControl.canAccess(user, classification))
```

Unauthorized users are blocked from accessing restricted reports. 

---

### 2. Lazy Loading

The real report is created **only when it is first needed**:

```java
if (realReport == null) {
    realReport = new RealReport(reportId, title, classification);
}
```

This prevents unnecessary disk loading when the proxy is created. 

---

### 3. Caching

Once the real report is created, it is stored and reused for future requests through the same proxy.

```java
private RealReport realReport;
```

This avoids repeated expensive disk loads.

---

# 4. ReportViewer.java

## Problem

Originally, `ReportViewer` depended directly on the concrete class `ReportFile`, which made the system tightly coupled.

## Improvement

`ReportViewer` now depends on the **Report interface** instead of a concrete implementation:

```java
public void open(Report report, User user)
```

This allows the viewer to work with **both the proxy and real implementations** without modification. 

---

# 5. Report.java

## Problem

The interface existed but was not properly used in the original system.

## Improvement

`Report` is now the **common abstraction** used by both:

* `RealReport`
* `ReportProxy`

This ensures loose coupling between the client and implementation.

```java
void display(User user);
```



---

# 6. App.java

## Problem

The application previously created `ReportFile` objects directly, bypassing the proxy and allowing unrestricted access.

## Improvement

The application now creates **ReportProxy objects instead of ReportFile**:

```java
Report publicReport = new ReportProxy(...);
```

This ensures that **all report access passes through the proxy**, enabling access control and lazy loading. 

---

# 7. AccessControl.java

## Problem

The access control logic existed but was not integrated into the report access workflow.

## Improvement

The proxy now uses this class to enforce role-based permissions:

* `PUBLIC` → everyone can access
* `FACULTY` → faculty and admin
* `ADMIN` → admin only

```java
accessControl.canAccess(user, classification);
```



---

# 8. QuickCheck.java

## Improvement

This file demonstrates the correct proxy behavior:

* Access control enforcement
* Lazy loading
* Cached report usage on repeated calls.

Repeated calls to the same report show that **the report is loaded from disk only once**.

---

# Final Outcome

After refactoring:

* Clients interact only with the **Report abstraction**
* Access to reports is controlled by the **Proxy**
* Reports are **loaded lazily**
* Disk loading happens **only once per proxy instance**
* Unauthorized users are blocked from accessing restricted reports
* The system is **loosely coupled and more maintainable**
