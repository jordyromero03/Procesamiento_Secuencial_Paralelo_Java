/**
 * Implementación paralela con la clase Thread (sin ExecutorService ni parallelStream).
 *
 * Pasos: 1) dividir el texto en bloques sin cortar palabras, 2) crear un hilo por bloque,
 * 3) start() a todos, 4) join() a todos, 5) combinar los resultados parciales.
 */
public final class ProcesadorParalelo {

    private ProcesadorParalelo() {
    }

    public static ResultadoAnalisis procesar(String texto, int numHilos, String palabra, String secuencia)
            throws InterruptedException {
        int[] limites = calcularLimites(texto, numHilos);

        HiloAnalizador[] hilos = new HiloAnalizador[numHilos];
        for (int i = 0; i < numHilos; i++) {
            hilos[i] = new HiloAnalizador("analizador-" + i, texto, limites[i], limites[i + 1], palabra, secuencia);
        }

        // Primero se arrancan todos para que trabajen a la vez...
        for (HiloAnalizador hilo : hilos) {
            hilo.start();
        }
        // ...y luego se espera a cada uno. Si se hiciera start()+join() dentro del mismo
        // bucle, los hilos correrían uno detrás de otro (secuencial disfrazado).
        for (HiloAnalizador hilo : hilos) {
            hilo.join();
        }

        ResultadoAnalisis total = ResultadoAnalisis.VACIO;
        for (HiloAnalizador hilo : hilos) {
            total = total.combinar(hilo.getResultado());
        }
        return total;
    }

    /**
     * Devuelve numHilos + 1 límites: el bloque i es [limites[i], limites[i+1]).
     *
     * Se parte en tamaños iguales y cada corte se corre hacia adelante mientras caiga
     * sobre una letra, de modo que siempre termina en un separador y ninguna palabra
     * queda partida entre dos bloques.
     */
    static int[] calcularLimites(String texto, int numHilos) {
        int longitud = texto.length();
        int[] limites = new int[numHilos + 1];
        limites[0] = 0;
        limites[numHilos] = longitud;

        for (int i = 1; i < numHilos; i++) {
            int corte = (int) ((long) longitud * i / numHilos);
            corte = Math.max(corte, limites[i - 1]);
            while (corte < longitud && Character.isLetter(texto.charAt(corte))) {
                corte++;
            }
            limites[i] = corte;
        }
        return limites;
    }
}
