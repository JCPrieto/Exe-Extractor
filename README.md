# Exe Extractor #

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=JCPrieto_Exe-Extractor&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=JCPrieto_Exe-Extractor)

Esta aplicación la implementé en los primera años de la carrera y la subo a Github para que no se pierda.

La funcionalidad es sencilla: A partir de un instalador en formato .exe autoextraible o .msi, lo descomprime y lo
convierte en un archivo .zip.

La implementé hace tanto tiempo, que realmente no recuerdo la motivación que me llevó a hacerla.

Le he hecho 4 retoques esteticos para que se parezca a otras aplicaciones que he implementado mas recientemente,
refactorizado un poco el código, integrado con Maven y subido la versión de compilación a Java 21 ya que en su momento
la implementé en Java 5.

### Requisitos ###

* Java 21

### Ejecución ###

Después de generar el paquete con `mvn package`, el ZIP de `target/` incluye el jar, las dependencias y los lanzadores.
En Linux se puede ejecutar desde la carpeta descomprimida con:

```shell
./Exe\ Extractor.sh
```

El paquete tambien incluye `ExeExtractor.desktop` y `app-icon.png` para integracion con escritorios Linux. Si se quiere
que GNOME/Ubuntu Dock asocie correctamente el nombre y el icono de la aplicacion, copia o instala el `.desktop` junto al
icono en una ubicacion de aplicaciones del usuario y conserva `StartupWMClass=ExeExtractor`.

### Changelog ###

Consulta el historial de cambios en [`CHANGELOG.md`](CHANGELOG.md).

### Licencia ### 

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
