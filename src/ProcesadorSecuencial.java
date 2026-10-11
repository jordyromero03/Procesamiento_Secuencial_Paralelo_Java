/**
 * Implementación secuencial: un único flujo de ejecución recorre todo el documento.
 */
public final class ProcesadorSecuencial {

    private ProcesadorSecuencial() {
    }

    public static ResultadoAnalisis procesar(String texto, String palabra, String secuencia) {
        return AnalizadorTexto.analizar(texto, 0, texto.length(), palabra, secuencia);
    }
}
