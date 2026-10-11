import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Punto de entrada: carga el libro, ejecuta las pruebas de rendimiento y muestra la tabla.
 *
 * Uso: java -cp out Main [archivo] [palabra] [secuencia] [repeticiones]
 */
public class Main {

    private static final int[] HILOS = {1, 2, 4};
    private static final int FACTOR_AMPLIACION = 100;
    private static final int CALENTAMIENTO = 3;
    private static final int CALENTAMIENTO_INICIAL = 20;

    /** Una fila de la tabla de resultados. */
    private record Medicion(String conjunto, int hilos, double promedioMs, double aceleracion) {
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Path archivo = Path.of(args.length > 0 ? args[0] : "data/quijote.txt");
        String palabra = (args.length > 1 ? args[1] : "Quijote").toLowerCase(Locale.ROOT);
        String secuencia = (args.length > 2 ? args[2] : "ción").toLowerCase(Locale.ROOT);
        int repeticiones = args.length > 3 ? Integer.parseInt(args[3]) : 5;

        // El archivo se carga en memoria ANTES de medir: la lectura no entra en los tiempos.
        String original = Files.readString(archivo, StandardCharsets.UTF_8);
        String ampliado = original.repeat(FACTOR_AMPLIACION);

        System.out.printf("Archivo: %s%n", archivo);
        System.out.printf("Palabra buscada: \"%s\" | Secuencia buscada: \"%s\" | Repeticiones: %d%n",
                palabra, secuencia, repeticiones);
        System.out.printf("Procesadores disponibles para la JVM: %d%n%n",
                Runtime.getRuntime().availableProcessors());

        calentarJit(original, palabra, secuencia);

        List<Medicion> mediciones = new ArrayList<>();
        mediciones.addAll(evaluar("Original", original, palabra, secuencia, repeticiones));
        mediciones.addAll(evaluar("Ampliado", ampliado, palabra, secuencia, repeticiones));

        imprimirTabla(mediciones);
        guardarCsv(mediciones, Path.of("resultados", "resultados.csv"));
    }

    private static List<Medicion> evaluar(String nombre, String texto, String palabra, String secuencia,
                                          int repeticiones) throws InterruptedException {
        System.out.printf("=== Conjunto %s (%,d caracteres) ===%n", nombre, texto.length());

        ResultadoAnalisis referencia = ProcesadorSecuencial.procesar(texto, palabra, secuencia);
        System.out.printf("Total de palabras: %,d%n", referencia.totalPalabras());
        System.out.printf("Apariciones de \"%s\": %,d%n", palabra, referencia.aparicionesPalabra());
        System.out.printf("Palabras que contienen \"%s\": %,d%n", secuencia, referencia.palabrasConSecuencia());

        List<Medicion> filas = new ArrayList<>();
        double tiempoSecuencial = 0;
        for (int hilos : HILOS) {
            // Calentamiento: deja que el JIT compile el código antes de medir.
            for (int i = 0; i < CALENTAMIENTO; i++) {
                ejecutar(texto, hilos, palabra, secuencia);
            }

            long sumaNanos = 0;
            StringBuilder tiemposPorRepeticion = new StringBuilder();
            for (int i = 0; i < repeticiones; i++) {
                long inicio = System.nanoTime();
                ResultadoAnalisis resultado = ejecutar(texto, hilos, palabra, secuencia);
                long duracion = System.nanoTime() - inicio;
                sumaNanos += duracion;
                tiemposPorRepeticion.append(String.format(Locale.ROOT, " %.1f", duracion / 1_000_000.0));

                // La comprobación queda fuera del intervalo medido.
                if (!resultado.equals(referencia)) {
                    throw new IllegalStateException("Resultado con " + hilos + " hilos no coincide con el secuencial: "
                            + resultado + " vs " + referencia);
                }
            }

            double promedioMs = sumaNanos / (double) repeticiones / 1_000_000.0;
            // Se muestran los tiempos individuales para detectar repeticiones atípicas.
            System.out.printf(Locale.ROOT, "  %d hilo(s), tiempos (ms):%s -> promedio %.3f%n",
                    hilos, tiemposPorRepeticion, promedioMs);
            if (hilos == 1) {
                tiempoSecuencial = promedioMs;
            }
            filas.add(new Medicion(nombre, hilos, promedioMs, tiempoSecuencial / promedioMs));
        }
        System.out.println("Resultados paralelos coinciden con el secuencial: OK");
        System.out.println();
        return filas;
    }

    /**
     * Calentamiento general antes de cualquier medición. El JIT de Java empieza interpretando
     * el código y lo optimiza tras varias ejecuciones; sin esta fase la primera configuración
     * medida (la secuencial) sale más lenta y la aceleración queda artificialmente inflada.
     * Se usa el texto original porque es corto: el código compilado sirve igual para el ampliado.
     */
    private static void calentarJit(String texto, String palabra, String secuencia) throws InterruptedException {
        for (int i = 0; i < CALENTAMIENTO_INICIAL; i++) {
            for (int hilos : HILOS) {
                ejecutar(texto, hilos, palabra, secuencia);
            }
        }
    }

    /** Con 1 hilo se usa la versión secuencial pura; con más, la paralela. */
    private static ResultadoAnalisis ejecutar(String texto, int hilos, String palabra, String secuencia)
            throws InterruptedException {
        return hilos == 1
                ? ProcesadorSecuencial.procesar(texto, palabra, secuencia)
                : ProcesadorParalelo.procesar(texto, hilos, palabra, secuencia);
    }

    private static void imprimirTabla(List<Medicion> mediciones) {
        System.out.println("| Conjunto de datos | Hilos          | Tiempo promedio (ms) | Aceleración |");
        System.out.println("|-------------------|----------------|----------------------|-------------|");
        for (Medicion m : mediciones) {
            String hilos = m.hilos() == 1 ? "1 (secuencial)" : String.valueOf(m.hilos());
            System.out.printf(Locale.ROOT, "| %-17s | %-14s | %20.3f | %11.2f |%n",
                    m.conjunto(), hilos, m.promedioMs(), m.aceleracion());
        }
    }

    private static void guardarCsv(List<Medicion> mediciones, Path destino) throws IOException {
        StringBuilder csv = new StringBuilder("conjunto,hilos,tiempo_promedio_ms,aceleracion\n");
        for (Medicion m : mediciones) {
            csv.append(String.format(Locale.ROOT, "%s,%d,%.3f,%.2f%n",
                    m.conjunto(), m.hilos(), m.promedioMs(), m.aceleracion()));
        }
        Files.createDirectories(destino.getParent());
        Files.writeString(destino, csv.toString(), StandardCharsets.UTF_8);
        System.out.printf("%nResultados guardados en %s%n", destino);
    }
}
