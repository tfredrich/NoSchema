# Repository Guidelines

## Project Structure & Module Organization
NoSchema is a Maven multi-module Java 17 project:
- `core/`: shared abstractions (`Entity`, `Identifier`, `UnitOfWork`) and core tests.
- `cassandra/`: Cassandra repository implementation, key/schema DSL, and integration-oriented tests.
- `gson-provider/`: `GsonObjectCodec` implementation and codec tests.
- `jackson-provider/`: `JacksonObjectCodec` implementation and codec tests.

Code lives under `src/main/java/...`; tests live under `src/test/java/...` in each module. Root `pom.xml` aggregates modules and common plugin config.

## Build, Test, and Development Commands
Use Maven from the repository root:
- `mvn clean verify`: full build, unit tests, source/javadoc artifacts, and verification lifecycle.
- `mvn test`: run tests across all modules.
- `mvn -pl core test`: run tests for one module only (replace `core` as needed).
- `mvn -pl cassandra -Dtest=KeyDefinitionParserTest test`: run a single test class.
- `mvn -DskipTests package`: build jars quickly without tests.

## Coding Style & Naming Conventions
- Java 17 (`maven-compiler-plugin` source/target set to `17`).
- Follow existing code style: tabs for indentation, K&R braces, concise Javadoc on public APIs.
- Package naming: lowercase (`com.strategicgains.noschema...`).
- Class names: PascalCase (`KeyDefinitionParser`); methods/fields: camelCase.
- Keep provider-specific code inside its provider module; shared contracts belong in `core`.

## Testing Guidelines
- Framework: JUnit 4 (`org.junit.Test`, `Assert.*`).
- Test classes should end with `Test` and mirror target package structure.
- Prefer focused behavioral names (`shouldCompareGreaterThan`) and keep fixtures minimal.
- Run `mvn test` before opening a PR; run module-scoped tests while iterating.

## Commit & Pull Request Guidelines
Recent history uses short, imperative, sentence-style commits (for example: `Updated jackson-provider with tests.`). Keep commits small and module-scoped when possible.

For pull requests:
- Explain what changed and why.
- Link related issues/tasks.
- List modules affected and test commands run.
- Include migration notes if API signatures or serialization behavior changed.
