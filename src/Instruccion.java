/**
 * UNA línea del programa ya separada en partes.
 * Ejemplo:  "mov eax, 5"  ->  nombre = "mov", op1 = "eax", op2 = "5"
 */
public class Instruccion {
    public final String nombre;   // la instrucción: mov, add, cmp, jge...
    public final String op1;      // primer operando (el destino), o null si no tiene
    public final String op2;      // segundo operando (la fuente), o null si no tiene
    public final int linea;       // en qué línea del programa estaba (para los errores)
    public final String texto;    // la línea tal como se escribió

    public Instruccion(String nombre, String op1, String op2, int linea, String texto) {
        this.nombre = nombre;
        this.op1 = op1;
        this.op2 = op2;
        this.linea = linea;
        this.texto = texto;
    }
}
