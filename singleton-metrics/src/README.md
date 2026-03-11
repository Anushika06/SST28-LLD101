# Singleton Refactoring Improvements

This project refactors the `MetricsRegistry` implementation into a **proper, thread-safe Singleton**.

---

# 1. MetricsRegistry.java

## Problem

The `MetricsRegistry` class was intended to be a Singleton but had multiple design flaws:

* The constructor was **public**, allowing external code to create multiple instances using `new MetricsRegistry()`.
* `getInstance()` used **lazy initialization without synchronization**, making it **not thread-safe**.
* The `INSTANCE` variable was **not declared `volatile`**, which could cause visibility issues in multi-threaded environments.
* Reflection could create additional instances by accessing the constructor.
* Serialization could produce a **new instance during deserialization**, breaking the Singleton property.

## Improvements

The following changes were implemented:

* The constructor was changed to **private** to prevent direct object creation.
* **Double-Checked Locking** was implemented in `getInstance()` to ensure **thread-safe lazy initialization**.
* The `INSTANCE` variable was declared **volatile** to prevent instruction reordering and ensure thread safety.
* A **constructor guard** was added to throw an exception if reflection attempts to create another instance.
* Implemented `readResolve()` to ensure **deserialization returns the existing singleton instance instead of creating a new one**.

These changes ensure that **only one instance of MetricsRegistry exists throughout the application lifecycle**.

---

# 2. MetricsLoader.java

## Problem

The loader created a new registry instance using:

```
new MetricsRegistry()
```

This violated the Singleton pattern because it bypassed the `getInstance()` method and created a separate registry object.

As a result, the metrics loaded from the properties file were stored in a **different registry instance** than the one used globally by the application.

## Improvements

The loader was updated to use the Singleton instance:

```
MetricsRegistry.getInstance()
```

This ensures that **all metrics are loaded into the same global registry instance used throughout the application**.

---

# 3. App.java

## Problem

The application printed two registry identities:

* Registry returned by the loader
* Registry returned by `getInstance()`

Due to the incorrect loader implementation, these identities were **different**, indicating multiple registry instances.

## Improvements

After fixing the loader and the singleton implementation:

* Both identities now match
* This confirms that **the same singleton instance is used throughout the application**.

---

# 4. ConcurrencyCheck.java

## Problem

The original singleton implementation was not thread-safe.
When multiple threads called `getInstance()` simultaneously, **multiple instances could be created**.

## Improvements

After implementing **Double-Checked Locking with volatile**, the concurrency test now shows:

```
Unique instances seen: 1
```

This confirms that the singleton is **thread-safe and only one instance is created across all threads**.

---

# 5. ReflectionAttack.java

## Problem

Reflection could bypass constructor access restrictions and create a second instance of `MetricsRegistry`.

This broke the Singleton guarantee.

## Improvements

A **constructor guard** was added:

```
if (INSTANCE != null) {
    throw new IllegalStateException("Singleton already initialized");
}
```

Now, reflection attempts to create another instance will **throw an exception**, preserving the singleton property.

---

# 6. SerializationCheck.java

## Problem

When the singleton instance was serialized and deserialized, Java created **a new object**, resulting in multiple instances.

## Improvements

The method `readResolve()` was implemented:

```
private Object readResolve() {
    return getInstance();
}
```

This ensures that **deserialization returns the existing singleton instance instead of creating a new one**.

---

# Final Result

After these improvements:

* Only **one instance of `MetricsRegistry` exists**
* The singleton is **thread-safe**
* Reflection cannot create additional instances
* Serialization preserves the singleton instance
* All components of the application use **the same global registry**

