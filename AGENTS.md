# Repository Guidelines

## Project Structure & Module Organization

- `src/main/java/` holds the application source (package `es.jklabs` and subpackages).
- `src/assembly/cfg.xml` defines the Maven assembly used to build a distributable zip.
- `Exe Extractor.sh` and `Exe Extractor.bat` are convenience launchers for the packaged app.
- `target/` is Maven output (jars, libs, and the assembled zip); do not edit by hand.

## Build, Test, and Development Commands

- `mvn clean package` builds the jar and assembles a distributable zip with dependencies.
- `mvn -q clean package` does the same with quieter output for CI/logs.
- `mvn -B verify` is the CI-equivalent command (build + tests) used in GitHub Actions.
- `java -jar target/ExeExtractor.jar` runs the GUI after building.
- `./Exe\ Extractor.sh` or `Exe Extractor.bat` runs the packaged app on Unix/Windows.

## Coding Style & Naming Conventions

- Java 21 is required (see `pom.xml`); prefer standard Java conventions.
- Indentation is 4 spaces; keep line wrapping readable and consistent.
- Packages are lowercase (`es.jklabs...`); classes use PascalCase; constants use `UPPER_SNAKE_CASE`.
- No formatter or linter is configured; keep changes minimal and consistent with existing style.

## Testing Guidelines

- Automated tests live under `src/test/java` and are named `*Test.java`.
- Each time we create a new class or a new public method, add its corresponding unit test.
- When modifying a method, review existing tests and update them if behavior changes.
- Use `mvn test` to run tests and note results in PRs.

## Commit & Pull Request Guidelines

- Git history favors short, descriptive subjects (e.g., `Bump ...`, `Update ...`).
- Keep commits focused; prefer imperative, single-line subjects.
- PRs should include: a brief summary, how you tested (`mvn clean package`, manual GUI run), and any relevant
  screenshots if UI behavior changes.
- When preparing a release, bump `pom.xml` version and add an entry in `CHANGELOG.md` with the date and highlights.
- Release automation: Release workflow runs after successful `CI` completion on pushes to `master`, builds
  distributables, and publishes GitHub release artifacts using `v<project.version>`.

## Configuration & Runtime Notes

- The app is a Swing GUI that extracts self-extracting `.exe` installers into `.zip` files.
- Output ZIP filename is configurable via `src/main/resources/app.properties` key `app.output.zip.name`.
- Ensure the target runtime has a JRE/JDK 21; Java 8+ may work but is not guaranteed.
