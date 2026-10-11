/**
 * Resultado (parcial o total) del análisis de un texto.
 * Es inmutable: cada hilo produce el suyo y luego se combinan sumando.
 */
public record ResultadoAnalisis(long totalPalabras, long aparicionesPalabra, long palabrasConSecuencia) {

    public static final ResultadoAnalisis VACIO = new ResultadoAnalisis(0, 0, 0);

    /** Combina dos resultados parciales: como los conteos son sumas, basta con sumar campo a campo. */
    public ResultadoAnalisis combinar(ResultadoAnalisis otro) {
        return new ResultadoAnalisis(
                totalPalabras + otro.totalPalabras,
                aparicionesPalabra + otro.aparicionesPalabra,
                palabrasConSecuencia + otro.palabrasConSecuencia);
    }
}
