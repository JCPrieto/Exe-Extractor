# Changelog

## 2.2.0 - 2026-02-09

- Added: detection of generated output file based on placeholder, new files, and fallback to newest file.
- Added: configurable output ZIP name through `app.output.zip.name`.
- Added: collision-safe output naming (`Exe.zip`, `Exe-1.zip`, `Exe-2.zip`, ...).
- Added: unit tests for output name normalization, collision handling, and generated file detection.
- Fixed: `InterruptedException` handling in update check by restoring thread interruption state.
- Changed: Maven Compiler Plugin upgraded to `3.15.0`.
- Docs: SonarCloud quality gate badge added to `README.md`.

## 2.1.1

- Validaciones de rutas y ejecucion mediante `ProcessBuilder` con errores controlados.

## 2.1.0

- Comprobacion de nuevas versiones desde GitHub y enlace de descarga en el menu.
- Version y metadatos movidos a `app.properties` con filtrado de recursos Maven.
- Ajustes de empaquetado (nombre final del jar/zip) y scripts de lanzamiento.
- Pruebas unitarias basicas para la logica de versiones.
- Workflow de release en GitHub Actions.

## 2.0.2

- Actualización de seguridad.
- Actualización a Java 21.

## 2.0.1

- Actualización de seguridad.

## 2.0.0

- Migracion a Java 11.
- Integracion con Maven.
- Look & Feel del sistema operativo en el que corrá.
- Refactorización sencilla del código.
