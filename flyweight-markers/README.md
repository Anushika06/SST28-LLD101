# Flyweight Pattern Refactoring — GeoDash Map Markers

## Overview

The GeoDash CLI tool renders thousands of map markers.
In the original implementation, each `MapMarker` created its own `MarkerStyle` object even when multiple markers shared the same style configuration.

This caused **large memory overhead** due to thousands of duplicate style objects.

The system was refactored using the **Flyweight Design Pattern**, where:

* **Intrinsic state (shared)** → `MarkerStyle`
* **Extrinsic state (per marker)** → `lat`, `lng`, `label`

A `MarkerStyleFactory` is used to share style instances instead of creating new ones.

---

# 1. MapMarker.java

## Problem

Originally, every marker created its own style object:

```java
this.style = new MarkerStyle(shape, color, size, filled);
```

This meant **30,000 markers created 30,000 style objects**, even when styles were identical. 

### Issues

* High memory usage
* No reuse of identical styles
* Violates Flyweight principle

## Improvement

`MapMarker` was refactored to store:

* Extrinsic state → `lat`, `lng`, `label`
* Shared intrinsic state → `MarkerStyle`

The style object is now **obtained from `MarkerStyleFactory` instead of creating a new instance**, allowing markers with identical styles to share the same object.

---

# 2. MarkerStyle.java

## Problem

The style class was **mutable** and contained setter methods:

```java
setShape()
setColor()
setSize()
setFilled()
```

Mutable objects cannot safely be shared across multiple markers.

### Issues

* Shared state could be accidentally modified
* Not safe for Flyweight reuse

## Improvement

`MarkerStyle` was refactored to be an **immutable class**:

* Fields represent intrinsic style configuration
* Objects represent shared style data
* Style identity is represented as:

```
shape|color|size|filled
```

This makes style objects safe to share among thousands of markers. 

---

# 3. MarkerStyleFactory.java

## Problem

Although a factory existed, it **did not implement caching**.
It always returned a new style:

```java
return new MarkerStyle(shape, color, size, filled);
```

This defeated the purpose of Flyweight sharing. 

## Improvement

The factory was refactored to implement a **Flyweight cache**:

* A `Map<String, MarkerStyle>` stores style instances
* Styles are indexed using a unique key:

```
shape|color|size|F/O
```

When a style is requested:

* If it exists in the cache → return existing instance
* Otherwise → create, store, and return the new instance

This ensures **identical styles reuse the same object**. 

---

# 4. MapDataSource.java

## Problem

Originally, markers were created directly with style parameters:

```java
new MapMarker(lat, lng, label, shape, color, size, filled)
```

Since `MapMarker` internally created the style object, the data source indirectly caused **thousands of duplicate styles**. 

## Improvement

The marker creation pipeline was refactored to **use `MarkerStyleFactory`**.

Now style instances are obtained from the factory, ensuring that **identical style configurations reuse shared objects**.

---

# 5. MapRenderer.java

## Problem

The renderer itself was correct, but because style objects were duplicated, rendering thousands of markers caused unnecessary memory usage.

## Improvement

After Flyweight refactoring, markers now share style objects, so rendering large numbers of markers **uses far fewer style instances** while keeping the same output format. 

---

# 6. QuickCheck.java

## Purpose

This file verifies Flyweight sharing.

It counts unique style object identities:

```java
identities.add(System.identityHashCode(m.getStyle()));
```

### Before Refactor

Example output:

```
Markers: 20000
Unique styles: ~20000
```

### After Refactor

```
Markers: 20000
Unique styles: <= 96
```

(3 shapes × 4 colors × 4 sizes × 2 fill states)

This confirms that **styles are now shared instead of duplicated**. 

---

# 7. App.java

## Improvement

The application still loads and renders markers the same way:

```java
List<MapMarker> markers = ds.loadMarkers(n);
```

The Flyweight pattern improves **memory efficiency internally without changing external behavior**. 

---

# Final Outcome

After refactoring:

* Marker styles are **shared across markers**
* Style objects are **immutable**
* `MarkerStyleFactory` manages style reuse
* `MapMarker` stores only extrinsic state
* Memory usage is **significantly reduced**
* Rendering behavior remains unchanged

