# Exe Extractor #

Esta aplicación la implementé en los primera años de la carrera y la subo a Github para que no se pierda.

La funcionalidad es sencilla: A partir de un instalador en formato .exe autoextraible, lo descomprime y lo convierte en un archivo .zip.

La implementé hace tanto tiempo, que realmente no recuerdo la motivación que me llevó a hacerla.

Le he hecho 4 retoques esteticos para que se parezca a otras aplicaciones que he implementado mas recientemente, refactorizado un poco el código, integrado con Maven y subido la versión de compilación a Java 11 ya que en su momento la implementé en Java 5. 
En cualquier caso, en Java 8 debería de compilar ya que lo unico que he actualizado al refactorizar es el uso de 4 o 5 lambdas.

### Requisitos ###

* Java 21

### Changelog ###

* 2.0.2
  * Actualización de seguridad
  * Actualización a Java 21

* 2.0.1
  * Actualización de seguridad

* 2.0.0

    * Migracion a Java 11.
    * Integracion con Maven.
    * Look & Feel del sistema operativo en el que corrá.
    * Refactorización sencilla del código. 

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
