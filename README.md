# Procesamiento secuencial y paralelo de textos con Java Threads

Actividad práctica 01 – Computación Paralela.

La aplicación analiza *Don Quijote de la Mancha* (Project Gutenberg, TXT) y obtiene:

1. Número total de palabras del documento.
2. Número de apariciones de una palabra específica (por defecto `Quijote`).
3. Número de palabras que contienen una secuencia de caracteres (por defecto `ción`).

La búsqueda no distingue mayúsculas de minúsculas. El análisis se hace primero de forma
secuencial y luego en paralelo con 2 y 4 hilos, usando directamente la clase `Thread`
(`start()` y `join()`), sin `ExecutorService`, `parallelStream()` ni otras herramientas de alto nivel.

## Requisitos

- JDK 17 o superior (probado con OpenJDK 21). Se necesita `javac`, no solo el JRE.

## Estructura

```
data/quijote.txt              Libro original (Project Gutenberg #2000)
data/fragmento.txt            Fragmento de 2000 caracteres (prueba con texto pequeño)
src/AnalizadorTexto.java      Recorre un rango del texto y cuenta (lógica común)
src/ProcesadorSecuencial.java Versión secuencial: un único hilo sobre todo el texto
src/HiloAnalizador.java       Subclase de Thread que analiza un bloque
src/ProcesadorParalelo.java   Divide el texto sin cortar palabras, start()/join() y combina
src/ResultadoAnalisis.java    Resultado parcial/total y su combinación
src/Main.java                 Carga el archivo, mide tiempos y muestra la tabla
resultados/resultados.csv     Tiempos promedio de la ejecución reportada en el informe
resultados/salida_consola.txt Salida completa de esa ejecución (con tiempos por repetición)
informe/informe.html          Informe de resultados
```

## Cómo ejecutar

Desde la raíz del repositorio, la forma rápida (compila y ejecuta):

```bash
./run.sh                                  # valores por defecto
./run.sh data/quijote.txt Sancho mente 10 # archivo, palabra, secuencia, repeticiones
./run.sh data/quijote.txt Quijote ción 10 # corrida usada en el informe
./run.sh data/fragmento.txt               # texto pequeño: el paralelo resulta más lento
```

O paso a paso:

```bash
# Compilar
javac -d out src/*.java

# Ejecutar con los valores por defecto (data/quijote.txt, "Quijote", "ción", 5 repeticiones)
java -cp out Main

# O indicando archivo, palabra, secuencia y número de repeticiones
java -cp out Main data/quijote.txt Sancho ción 10
```

Si el libro no está en `data/`, se puede descargar con:

```bash
curl -L -o data/quijote.txt https://www.gutenberg.org/ebooks/2000.txt.utf-8
```

## Metodología de medición

- El archivo se carga en memoria **antes** de medir; los tiempos solo incluyen el procesamiento
  (no la lectura del archivo ni la impresión en consola).
- La versión ampliada se genera repitiendo el texto original 100 veces en memoria.
- Antes de medir nada se hacen 20 rondas de calentamiento del JIT con todas las configuraciones, y
  3 ejecuciones más antes de cada configuración; ninguna se mide. Luego se hacen N ejecuciones medidas
  con `System.nanoTime()` (5 por defecto; el informe usa 10) y se reporta el promedio. En consola se
  muestran también los tiempos de cada repetición.
- La aceleración se calcula como `S = Ts / Tp`, donde `Ts` es el tiempo secuencial del mismo conjunto.
- En cada ejecución se verifica que el resultado paralelo sea idéntico al secuencial.

## Decisiones de diseño

- **Palabra**: secuencia máxima de letras (`Character.isLetter`), así se reconocen tildes y ñ.
- **División sin cortar palabras**: el texto se parte en bloques de igual tamaño y cada corte se
  desplaza hacia adelante mientras caiga sobre una letra, de modo que siempre queda en un separador.
- **Sin sincronización**: cada hilo escribe en su propio objeto de resultado; el hilo principal los
  lee después de `join()` y los suma.
