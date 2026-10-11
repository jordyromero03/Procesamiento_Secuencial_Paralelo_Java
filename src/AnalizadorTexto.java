import java.util.Locale;

/**
 * Lógica de análisis compartida por la versión secuencial y la paralela.
 *
 * Una "palabra" es una secuencia máxima de letras (Character.isLetter), así que
 * se reconocen letras con tilde y la ñ. Signos, números y espacios separan palabras.
 * La comparación ignora mayúsculas/minúsculas pasando cada palabra a minúsculas.
 */
public final class AnalizadorTexto {

    private AnalizadorTexto() {
    }

    /**
     * Analiza el fragmento texto[inicio, fin).
     *
     * @param palabraBuscada   palabra a contar, ya en minúsculas (ej. "quijote")
     * @param secuenciaBuscada secuencia a buscar dentro de las palabras, ya en minúsculas (ej. "ción")
     */
    public static ResultadoAnalisis analizar(String texto, int inicio, int fin,
                                             String palabraBuscada, String secuenciaBuscada) {
        long total = 0;
        long apariciones = 0;
        long conSecuencia = 0;

        int i = inicio;
        while (i < fin) {
            // Saltar separadores hasta el inicio de una palabra
            while (i < fin && !Character.isLetter(texto.charAt(i))) {
                i++;
            }
            if (i >= fin) {
                break;
            }
            // Avanzar hasta el final de la palabra
            int inicioPalabra = i;
            while (i < fin && Character.isLetter(texto.charAt(i))) {
                i++;
            }

            String palabra = texto.substring(inicioPalabra, i).toLowerCase(Locale.ROOT);
            total++;
            if (palabra.equals(palabraBuscada)) {
                apariciones++;
            }
            if (palabra.contains(secuenciaBuscada)) {
                conSecuencia++;
            }
        }
        return new ResultadoAnalisis(total, apariciones, conSecuencia);
    }
}
