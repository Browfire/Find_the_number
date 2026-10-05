# Find the Number

Juego de escritorio Java Swing para descubrir un codigo de cuatro cifras
sin digitos repetidos mediante pistas de toques y famas.

## Requisitos

- JDK 21. Comprobar con `java -version` y `javac -version`.
- Apache Maven 3.9 o posterior. Comprobar con `mvn -version`.
- Entorno de escritorio para mostrar las ventanas Swing.

Configurar `JAVA_HOME` para que apunte al JDK 21 y agregar Maven al `PATH`.
Las fuentes se guardan en UTF-8; usar tambien esa codificacion en el editor.

## Compilar

Desde esta carpeta, donde se encuentra `pom.xml`:

```powershell
mvn clean package
```

Si la terminal esta en la raiz del repositorio, entrar primero con
`cd Find_the_Number`.

El resultado es `target/find-the-number-1.0.0-SNAPSHOT.jar`.
La compilacion no requiere Eclipse ni otras configuraciones del IDE.
Este comando tambien ejecuta las pruebas automatizadas antes de empaquetar.

## Pruebas

Para ejecutar solo las pruebas, desde esta carpeta:

```powershell
mvn test
```

Las pruebas usan JUnit 5 y archivos temporales; no abren ventanas ni modifican el archivo de puntajes del proyecto.

La suite cubre toques y famas, entradas validas e invalidas, ceros iniciales,
calculo de puntos, formato del tiempo y clasificacion e insercion del ranking.
Tambien verifica archivos ausentes, vacios, corruptos, errores de acceso,
nombres en UTF-8 y conservacion de los datos ante errores.
Las pruebas Swing se ejecutan en el hilo de eventos y verifican ingreso por
boton o Enter, escritura y pegado de cuatro digitos, victoria, abandono,
reinicio y parada del temporizador.
Actualmente hay 80 casos activos, sin pruebas deshabilitadas. Los informes
se generan en `target/surefire-reports/`.

Para verificar tambien el empaquetado en un entorno sin escritorio:

```powershell
mvn "-Djava.awt.headless=true" clean verify
```

GitHub Actions ejecuta este comando con Java 21 en Windows y Linux.
La verificacion local se realizo en Windows; el resultado remoto del CI debe
comprobarse en GitHub despues de publicar los cambios.

## Ejecutar

Desde la misma carpeta:

```powershell
java -jar target/find-the-number-1.0.0-SNAPSHOT.jar
```

El punto de entrada del JAR es `find_the_number.Inicio`.

## Reglas de Partida

El codigo tiene cuatro digitos distintos y puede comenzar con cero. Un intento
invalido no consume turnos. La comparacion no conserva estado entre llamadas.
El campo de entrada acepta solo digitos del 0 al 9 y hasta cuatro caracteres,
tanto al escribir como al pegar; los cambios invalidos se rechazan completos.
La longitud exacta y los digitos repetidos se validan tambien al enviar.

La partida mide el tiempo con `System.nanoTime()` y lo congela al ganar.
El temporizador Swing solo refresca la pantalla y se detiene al abandonar,
ganar o cerrar la ventana. Cada nueva partida comienza desde cero.

Los puntos se calculan como `1500000 / turnos / max(1, segundos)`, con division
entera. Una victoria antes del primer segundo conserva `00:00` y usa un segundo
minimo solo para calcular puntos. Los puntajes historicos no se recalculan.

Los nombres se validan en cada solicitud, se recortan los espacios de los
extremos y se limita la longitud a diez caracteres Unicode. Cancelar omite
el guardado y muestra el ranking, sin interrumpir la aplicacion.

## Puntajes

Por defecto se utiliza `puntaje.txt` en el directorio desde el que se ejecuta
la aplicacion. Al lanzar los comandos anteriores desde esta carpeta, se usa
el ranking local de esta carpeta, si existe. Los puntajes de cada usuario no
se versionan: en una copia nueva del repositorio el ranking comienza vacio.
Los datos locales existentes se conservan. Para elegir otra ubicacion:

```powershell
java "-Dfindthenumber.puntajes=C:\Datos\FindTheNumber\puntaje.txt" -jar target/find-the-number-1.0.0-SNAPSHOT.jar
```

El archivo es texto UTF-8: cada fila contiene jugador, turnos, tiempo `MM:SS`
y puntos, separados por tabuladores. Los turnos deben ser positivos y los
puntos no negativos. Los nombres no pueden estar vacios ni contener caracteres
de control. Los minutos pueden tener mas de dos cifras.

Un archivo ausente o vacio representa un ranking vacio; no se crean jugadores
ficticios. El archivo y sus carpetas se crean al guardar el primer puntaje.
Las filas vacias y las antiguas filas de plantilla sin jugador y con valores
cero se ignoran. Se muestran hasta diez entradas ordenadas por puntos; los
empates conservan el orden existente. Si hay diez entradas, un nuevo puntaje
debe superar al ultimo para clasificar.

Si una fila tiene formato invalido o el archivo no es UTF-8, se informa el
error y no se sobrescribe el archivo. Los errores de acceso y guardado tambien
se muestran en la interfaz. No se eliminan automaticamente datos corruptos.

El guardado escribe primero en un archivo temporal del mismo directorio y
despues reemplaza el destino con un movimiento atomico cuando el sistema lo
admite; si no, usa un reemplazo normal. Los recursos se cierran automaticamente
y el temporal se elimina. No se coordina la escritura entre varias instancias
simultaneas de la aplicacion.

Para usar un ranking antiguo de `D:\puntaje.txt`, indicar esa ruta mediante
la propiedad anterior. No se copia ni modifica automaticamente.

## Estructura Actual

El repositorio contiene el proyecto Maven en `Find_the_Number/`, las reglas
comunes de exclusion en `.gitignore` y el CI en `.github/workflows/build.yml`.
Dentro del proyecto:

```text
pom.xml
README.md
src/main/java/find_the_number/Inicio.java
src/main/java/find_the_number/dominio/
src/main/java/find_the_number/aplicacion/
src/main/java/find_the_number/persistencia/
src/main/java/find_the_number/ui/
src/main/resources/instrucciones.txt
src/test/java/find_the_number/dominio/PartidaTest.java
src/test/java/find_the_number/persistencia/RepositorioPuntajesArchivoTest.java
src/test/java/find_the_number/ui/PanelesTest.java
```

`Inicio` es el unico punto de entrada y conecta las dependencias. El dominio
(`CodigoSecreto`, `ResultadoIntento`, `Partida` y `Puntaje`) no depende de Swing
ni de archivos. `Ranking` ordena y clasifica entradas inmutables mediante el
contrato `RepositorioPuntajes`; `RepositorioPuntajesArchivo` convierte el
formato y accede al disco.
La interfaz usa una ventana con `CardLayout` y layouts adaptables, sin
coordenadas absolutas. Las instrucciones son un recurso incluido en el JAR.

No hay fuentes activas en la antigua ruta `src/find_the_number/`: la copia
redundante de `Inicio.java` se retiro. Todas las fuentes y pruebas siguen la
estructura estandar de Maven. La salida `bin/` de la version antigua tambien
se retiro; Maven genera la salida actual en `target/`.

## Control de Versiones

Se deben incluir fuentes, pruebas, `pom.xml`, este README, recursos y el
workflow de CI. El `.gitignore` de la raiz se aplica a todo el repositorio y
excluye:

- `target/`, `bin/`, `out/` y archivos `.class`: salida regenerable.
- `.vscode/`, `.idea/`, `.settings/` y `.metadata/`: ajustes locales de editores.
- `.project`, `.classpath` y archivos `.iml`: metadatos locales del IDE.
- Los archivos de puntajes por defecto: datos locales de ejecucion.
- Logs, temporales, copias de respaldo y archivos auxiliares del sistema.

No se excluyen los recursos de texto del juego ni los archivos de CI.
Los metadatos de Eclipse/VS Code y los puntajes existentes pueden mantenerse
localmente, pero no forman parte del codigo compartido. Para importar el
proyecto en un IDE, usar `pom.xml` y JDK 21; la configuracion se puede regenerar.
Los puntajes y metadatos que ya estaban versionados se retiraron del indice
de Git sin borrar sus copias locales.

Antes de un commit, comprobar desde la raiz del repositorio:

```powershell
git status --short
git diff --check
```

Para reconstruir la salida de compilacion, usar `mvn clean verify` desde la
carpeta del proyecto. No hace falta versionar el JAR ni los informes de pruebas.

## Limitaciones

El repositorio local esta pensado para una sola instancia de la aplicacion;
no coordina escrituras simultaneas. Si el archivo se corrompe, se informa el
error y se preserva para repararlo manualmente, sin descartar filas en silencio.
