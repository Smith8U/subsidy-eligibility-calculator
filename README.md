# Subsidy Eligibility Calculator

A ScalaFX desktop application that helps NGO and community-support workers assess which subsidies a low-income household may qualify for. The project demonstrates an object-oriented domain model, validated data entry, SQLite persistence, and an interactive JavaFX interface aligned with UN Sustainable Development Goal 1: No Poverty.

## Features

- Register households with validation for names, income, dependents, and disability status.
- Evaluate eligibility for housing, education, and food subsidies.
- View and delete saved household records.
- Browse subsidy descriptions, funding amounts, and eligibility rules.
- Persist records locally in SQLite so data survives application restarts.

## Tools used

- Scala 3.3.3
- SBT 1.9.9
- ScalaFX 21
- OpenJFX 21
- SQLite JDBC
- MUnit 1.0.0
- SLF4J NOP provider

## My contribution

This was an individual project. I designed and implemented the application, including the immutable household and subsidy domain model, generic validators, subsidy eligibility rules, ScalaFX navigation and screens, SQLite persistence, unit tests, and project documentation.

Key technical decisions included using a sealed `Subsidy` trait for subtype polymorphism, a generic `Validator[T]` abstraction for reusable validation, `Try` and `Using.resource` for safe database operations, and a dedicated observable UI state layer to connect immutable domain objects to the reactive interface.

## How to run

### Requirements

- Java 21
- SBT 1.9 or newer

### Compile and test

From the repository root:

```bash
sbt clean test
```

### Run the application

```bash
sbt run
```

The application creates `data/households.db` automatically when it first saves a household. The database is intentionally ignored by Git because it contains runtime data rather than source code.

## Sample output

After registering a household, the evaluation table displays results in this format:

```text
Household Head | Income (RM) | Dependents | Disability | Eligible Subsidies
Ahmad          | RM 2000.00  | 3          | No         | Public Housing Subsidy, B40 Education Grant
```

Eligibility rules implemented in the application:

- Public Housing Subsidy: income below RM 2,500 and more than two dependents.
- B40 Education Grant: income below RM 4,000 and at least one dependent.
- Basic Necessities Basket: income below RM 1,500 or a household member has a disability.

## Demo link

No public demo link is currently available. This is a local ScalaFX desktop application.

## License

No open-source license has been selected for this coursework portfolio repository. Please confirm university policy before adding one or granting reuse permission. Third-party library notices are listed in [`docs/third-party-licenses.md`](docs/third-party-licenses.md).

