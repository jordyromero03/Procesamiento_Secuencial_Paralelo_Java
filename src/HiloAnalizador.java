/**
 * Hilo que analiza un bloque del documento.
 *
 * Extiende directamente java.lang.Thread (lo que pide la práctica). Cada hilo trabaja
 * sobre su propio rango [inicio, fin) y guarda su resultado en un campo propio, así
 * no comparte nada mutable con los demás y no hace falta sincronización.
 */
public class HiloAnalizador extends Thread {

    private final String texto;
    private final int inicio;
    private final int fin;
    private final String palabra;
    private final String secuencia;
    private ResultadoAnalisis resultado = ResultadoAnalisis.VACIO;

    public HiloAnalizador(String nombre, String texto, int inicio, int fin, String palabra, String secuencia) {
        super(nombre);
        this.texto = texto;
        this.inicio = inicio;
        this.fin = fin;
        this.palabra = palabra;
        this.secuencia = secuencia;
    }

    @Override
    public void run() {
        resultado = AnalizadorTexto.analizar(texto, inicio, fin, palabra, secuencia);
    }

    /** Solo debe leerse después de join(): join() garantiza que vemos lo que escribió run(). */
    public ResultadoAnalisis getResultado() {
        return resultado;
    }
}
