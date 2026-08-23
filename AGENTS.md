# Repository Guidelines

## Project Structure & Module Organization

- `src/main/java/` holds the application source (package `es.jklabs` and subpackages).
- `src/assembly/cfg.xml` defines the Maven assembly used to build a distributable zip.
- `Exe Extractor.sh` and `Exe Extractor.bat` are convenience launchers for the packaged app.
- `ExeExtractor.desktop` and `src/main/resources/img/icons/app-icon.*` provide Linux desktop integration metadata
  and the application icon.
- `target/` is Maven output (jars, libs, and the assembled zip); do not edit by hand.

## Build, Test, and Development Commands

- `mvn clean package` builds the jar and assembles a distributable zip with dependencies.
- `mvn -q clean package` does the same with quieter output for CI/logs.
- `mvn verify` runs tests and generates the JaCoCo coverage report used by SonarQube (`target/site/jacoco/jacoco.xml`).
- `mvn -B verify` is the CI-equivalent command (build + tests + coverage report) used in GitHub Actions.
- `java -jar target/ExeExtractor.jar` runs the GUI after building.
- `./Exe\ Extractor.sh` or `Exe Extractor.bat` runs the packaged app on Unix/Windows.

## Coding Style & Naming Conventions

- Java 25 is required (see `pom.xml`); prefer standard Java conventions.
- Indentation is 4 spaces; keep line wrapping readable and consistent.
- Packages are lowercase (`es.jklabs...`); classes use PascalCase; constants use `UPPER_SNAKE_CASE`.
- Use `_` for intentionally unused lambda parameters, as supported by Java 25.
- Resolve localized or computed `java.util.logging` messages lazily with a `Supplier`; when logging an exception, use
  the overload that accepts both the `Throwable` and the message supplier.
- No formatter or linter is configured; keep changes minimal and consistent with existing style.

## Testing Guidelines

- Automated tests live under `src/test/java` and are named `*Test.java`.
- Each time we create a new class or a new public method, add its corresponding unit test.
- When modifying a method, review existing tests and update them if behavior changes.
- Use `mvn test` for a quick validation and `mvn verify` when you also need the JaCoCo coverage artifacts.
- `InicioTest` runs AWT in headless mode; validate Swing rendering and `Desktop`/`Taskbar` integration manually in a
  graphical environment when those areas change.
- Keep operating-system integrations behind the existing `UriBrowser` and `ApplicationTaskbar` abstractions so their
  supported and unsupported paths remain deterministic in headless tests.
- Keep extraction result handling separate from the `SwingWorker`; test process errors, missing output and successful
  moves through `finishExtraction` without creating Swing windows.

## Commit & Pull Request Guidelines

- Git history favors short, descriptive subjects (e.g., `Bump ...`, `Update ...`).
- Keep commits focused; prefer imperative, single-line subjects.
- PRs should include: a brief summary, how you tested (`mvn clean package`, manual GUI run), and any relevant
  screenshots if UI behavior changes.
- When preparing a release, bump `pom.xml` version and add an entry in `CHANGELOG.md` with the date and highlights.
- Release automation: after successful `CI` completion on trusted pushes to `master`, CI uploads the release
  artifacts and the Release workflow publishes them using `v<project.version>`.

## Configuration & Runtime Notes

- The app is a Swing GUI that extracts self-extracting installer packages (`.exe` and `.msi`) into `.zip` files.
- The executable jar uses the Java 25 no-argument `main()` method in `es.jklabs.ExeExtractor` so Linux desktop identity
  is configured before Swing initializes; `es.jklabs.Inicio` contains the main UI and delegates its entry point to
  `ExeExtractor`.
- Linux dock integration depends on `ExeExtractor.desktop`, `StartupWMClass=ExeExtractor`, the external `app-icon.png`,
  and the desktop identity properties configured by `es.jklabs.ExeExtractor` and the Unix launcher.
- Output ZIP filename is configurable via `src/main/resources/app.properties` key `app.output.zip.name`.
- Ensure the target runtime has a HotSpot-based JRE/JDK 25 (for example, Eclipse Temurin), since the packaged launchers
  enable compact object headers with a HotSpot option.
