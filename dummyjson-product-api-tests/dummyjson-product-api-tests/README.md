# DummyJSON Product API Test Suite

Automated REST API test suite for the **DummyJSON Products** endpoint
(`https://dummyjson.com/docs/products`), built with **Java 21 (LTS)**,
**Maven**, **REST Assured 5.5.7**, and **JUnit 5 (Jupiter) 5.13.4**.

## What this covers

| Endpoint | Method | Test class |
|---|---|---|
| Get all products | `GET /products` | `GetAllProductsTests` |
| Get product by id | `GET /products/{id}` | `GetProductByIdTests` |
| Create a new product (valid + invalid payloads) | `POST /products/add` | `CreateProductTests` |
| Update an existing product | `PUT` / `PATCH /products/{id}` | `UpdateProductTests` |
| Delete a product | `DELETE /products/{id}` | `DeleteProductTests` |
| Total price of products with id 4–10 | n/a (utility + test) | `PriceCalculator` / `TotalPriceCalculationTests` |

Every test class contains both **positive** ("happy path") and **negative**
(error / edge-case) scenarios. Each response is validated against only the
fields requested for this exercise: **id, title, description, category,
price, brand** (the `Product` POJO intentionally ignores every other field
the live API returns, such as `discountPercentage`, `rating`, `stock`,
`tags`, `sku`, `reviews`, `meta`, `images`, etc.).

> **Note on DummyJSON's simulated write operations:** DummyJSON's `add`,
> `update`, and `delete` endpoints do **not** persist changes server-side —
> they simulate the operation and echo back a plausible response (e.g. a new
> `id` on create, the merged fields on update, `isDeleted`/`deletedOn` on
> delete). The tests are written against this **documented, real behavior**
> of the live API rather than assuming a stateful backend.

## Project structure

```
dummyjson-product-api-tests/
├── pom.xml
├── README.md
└── src/test/
    ├── java/com/dummyjson/
    │   ├── base/
    │   │   └── BaseTest.java              # shared RestAssured setup (base URI, specs)
    │   ├── models/
    │   │   ├── Product.java               # POJO: id, title, description, category, price, brand
    │   │   └── ProductsResponse.java      # wrapper: { products[], total, skip, limit }
    │   ├── utils/
    │   │   ├── ProductEndpoints.java      # centralized endpoint path constants
    │   │   └── PriceCalculator.java       # total-price-for-id-range utility
    │   └── tests/
    │       ├── GetAllProductsTests.java
    │       ├── GetProductByIdTests.java
    │       ├── CreateProductTests.java
    │       ├── UpdateProductTests.java
    │       ├── DeleteProductTests.java
    │       └── TotalPriceCalculationTests.java
    └── resources/
        └── junit-platform.properties
```

## Prerequisites

- **Java 21** (LTS) or later on your `PATH` (`java -version`)
- **Maven 3.9+** (`mvn -version`)
- Internet access (tests call the live `https://dummyjson.com` API)

## Running the tests

```bash
# unzip first
cd dummyjson-product-api-tests

# Run all tests
mvn clean test

# Run all tests AND generate the HTML report (recommended)
mvn clean verify
```

`testFailureIgnore` is enabled in `pom.xml` so the Maven build won't halt on
the first failing test — every test class runs to completion and all
results are captured in the report, then the build still reports the
overall failure count (and exits non‑zero) at the end.

### Running a single class or tag

```bash
# A single test class
mvn test -Dtest=GetAllProductsTests

# Only the price-calculation tests
mvn test -Dtest=TotalPriceCalculationTests
```

### Pointing at a different base URL

The base URL defaults to `https://dummyjson.com` and can be overridden
without touching any code:

```bash
mvn clean verify -Dapi.base.uri=https://your-mirror.example.com
```

## Test report

After `mvn clean verify` completes, open:

```
target/site/surefire-report.html
```

in a browser for a human-readable HTML report (pass/fail counts, per-class
breakdown, failure stack traces, timings).

Raw machine-readable XML/TXT results (one file per test class) are also
available under:

```
target/surefire-reports/
```

## The "total price of products 4–10" requirement

`com.dummyjson.utils.PriceCalculator` exposes:

```java
double calculateTotalPrice(int fromId, int toId);
PriceBreakdown calculateTotalPriceWithBreakdown(int fromId, int toId);
```

`TotalPriceCalculationTests` calls `calculateTotalPrice(4, 10)`, **prints**
the resulting total (and the individual prices) to stdout via
`System.out.printf`, and then **validates** that total against an
independently-computed sum (fetched directly via REST Assured in the test
itself) — so the calculation is cross-checked rather than just trusted.
Sample console output:

```
[Total Price] Sum of prices for product ids 4 to 10 = 123.45
[Total Price] Individual prices: [12.0, 9.99, ...]
```

## Design notes

- **`BaseTest`** sets `RestAssured.baseURI` once in a `@BeforeAll` and
  exposes a shared `RequestSpecification` (`Content-Type`/`Accept:
  application/json`) so every test class stays DRY.
- **Negative tests favor structural assertions** (status code ranges such
  as "< 500" or "4xx") over hard-coded exact messages where DummyJSON's
  exact error text isn't part of its documented contract — this keeps the
  suite resilient to minor wording changes on the server side while still
  catching real regressions (e.g. an endpoint starting to 500).
- **Jackson `@JsonIgnoreProperties(ignoreUnknown = true)`** on the `Product`
  POJO means the suite only ever asserts on the six required fields, even
  though the live API returns ~20 fields per product.
