/**
 * Convierte el TEXTO del programa en un objeto Programa.
 * Lee línea por línea: quita comentarios (;), guarda etiquetas (menor:) y separa cada instrucción.
 */
public class Ensamblador {

    public static Programa ensamblar(String codigo) {
        Programa p = new Programa();
        String[] lineas = codigo.split("\n");

        for (int i = 0; i < lineas.length; i++) {
            String s = lineas[i];

            // 1. Quitar el comentario: todo lo que va después de ;
            int comentario = s.indexOf(';');
            if (comentario >= 0) {
                s = s.substring(0, comentario);
            }
            s = s.trim().toLowerCase();

            // 2. Si tiene etiqueta ("menor:"), anotar a qué instrucción apunta
            int dosPuntos = s.indexOf(':');
            if (dosPuntos >= 0) {
                String etiqueta = s.substring(0, dosPuntos).trim();
                p.etiquetas.put(etiqueta, p.instrucciones.size());
                s = s.substring(dosPuntos + 1).trim();
            }

            // 3. Si queda una instrucción, guardarla
            if (!s.isEmpty()) {
                p.instrucciones.add(leerInstruccion(s, i + 1));
            }
        }

        // 4. Revisar que cada salto vaya a una etiqueta que exista
        for (Instruccion ins : p.instrucciones) {
            if (ins.nombre.startsWith("j") && !p.etiquetas.containsKey(ins.op1)) {
                throw new EmuladorError("Línea " + ins.linea + ": la etiqueta '" + ins.op1 + "' no existe");
            }
        }
        return p;
    }

    /** "mov eax, 5" -> Instruccion(nombre="mov", op1="eax", op2="5") */
    public static Instruccion leerInstruccion(String s, int linea) {
        s = s.trim().toLowerCase();
        String[] partes = s.split(" ", 2);          // "mov"  y  "eax, 5"
        String nombre = partes[0];
        String op1 = null, op2 = null;
        if (partes.length > 1) {
            String[] ops = partes[1].split(",");    // "eax"  y  " 5"
            op1 = ops[0].trim();
            if (ops.length > 1) {
                op2 = ops[1].trim();
            }
        }
        return new Instruccion(nombre, op1, op2, linea, s);
    }
}
