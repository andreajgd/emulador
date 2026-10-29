import java.util.LinkedHashMap;
import java.util.Map;

/**
 * El procesador: guarda los REGISTROS y las BANDERAS, y sabe ejecutar cada instrucción.
 */
public class Cpu {

    // Los registros: nombre -> valor.  Ej: "eax" -> 5
    public final Map<String, Integer> registros = new LinkedHashMap<>();

    // Las banderas
    public boolean zf;   // ¿el resultado dio cero?     (los números eran iguales)
    public boolean sf;   // ¿el resultado dio negativo?
    public boolean cf;   // ¿hubo acarreo / préstamo?   (comparación sin signo: el primero era menor)
    public boolean of;   // ¿hubo desborde con signo?

    public Cpu() {
        reiniciar();
    }

    /** Todos los registros y banderas en 0. */
    public void reiniciar() {
        for (String r : new String[]{"eax", "ebx", "ecx", "edx", "esi", "edi"}) {
            registros.put(r, 0);
        }
        zf = sf = cf = of = false;
    }

    // ------------------------------------------------------------ ejecutar

    /** Ejecuta el programa completo y muestra los registros después de cada instrucción. */
    public void correr(Programa p) {
        int pc = 0;       // "program counter": qué instrucción toca ahora
        int pasos = 0;

        while (pc < p.instrucciones.size()) {
            Instruccion ins = p.instrucciones.get(pc);
            Map<String, Integer> antes = new LinkedHashMap<>(registros);
            pasos++;

            System.out.println("\nPaso " + pasos + ":  " + ins.texto);
            String destino = ejecutar(ins);

            if (ins.nombre.startsWith("j")) {
                System.out.println(destino != null ? "  -> SÍ salta a '" + destino + "'" : "  -> NO salta");
            }
            System.out.println(mostrar(antes));

            // ¿Cuál instrucción sigue?
            if (destino == null) {
                pc = pc + 1;                          // la de abajo
            } else if (destino.equals("ret")) {
                break;                                // terminó
            } else {
                pc = p.etiquetas.get(destino);        // la de la etiqueta
            }

            if (pasos >= 1000) {
                throw new EmuladorError("Más de 1000 pasos: ¿ciclo infinito?");
            }
        }
        System.out.println("\nTerminó. Resultado en EAX = " + registros.get("eax"));
    }

    /**
     * Ejecuta UNA instrucción.
     * Devuelve:  null = seguir con la de abajo,  una etiqueta = saltar ahí,  "ret" = terminar.
     */
    public String ejecutar(Instruccion ins) {
        switch (ins.nombre) {
            case "mov":                                       // op1 = op2
                escribir(ins.op1, leer(ins.op2));
                return null;
            case "add":                                       // op1 = op1 + op2
                escribir(ins.op1, sumar(leer(ins.op1), leer(ins.op2)));
                return null;
            case "sub":                                       // op1 = op1 - op2
                escribir(ins.op1, restar(leer(ins.op1), leer(ins.op2)));
                return null;
            case "inc":                                       // op1 = op1 + 1
                escribir(ins.op1, sumar(leer(ins.op1), 1));
                return null;
            case "dec":                                       // op1 = op1 - 1
                escribir(ins.op1, restar(leer(ins.op1), 1));
                return null;
            case "cmp":                                       // resta SIN guardar: sólo cambian las banderas
                restar(leer(ins.op1), leer(ins.op2));
                return null;
            case "xor": {                                     // xor eax, eax  ->  eax = 0
                int r = leer(ins.op1) ^ leer(ins.op2);
                escribir(ins.op1, r);
                zf = (r == 0);
                sf = (r < 0);
                cf = of = false;
                return null;
            }
            case "jmp":                                       // salta siempre
                return ins.op1;
            case "ret":                                       // fin del programa
                return "ret";
            default:
                if (ins.nombre.startsWith("j")) {             // je, jne, jg, jge, jl, jle, jc...
                    return saltaria(ins.nombre) ? ins.op1 : null;
                }
                throw new EmuladorError("Línea " + ins.linea + ": instrucción no soportada '" + ins.nombre + "'");
        }
    }

    /** ¿El salto condicional se cumple? Lo decide mirando las banderas que dejó el último CMP. */
    public boolean saltaria(String salto) {
        switch (salto) {
            case "je":  case "jz":  return zf;                // iguales
            case "jne": case "jnz": return !zf;               // distintos
            case "jg":              return !zf && sf == of;   // mayor           (con signo)
            case "jge":             return sf == of;          // mayor o igual   (con signo)
            case "jl":              return sf != of;          // menor           (con signo)
            case "jle":             return zf || sf != of;    // menor o igual   (con signo)
            case "ja":              return !cf && !zf;        // mayor           (sin signo)
            case "jae": case "jnc": return !cf;               // mayor o igual   (sin signo)
            case "jb":  case "jc":  return cf;                // menor           (sin signo)
            case "jbe":             return cf || zf;          // menor o igual   (sin signo)
            default: throw new EmuladorError("Salto no soportado: '" + salto + "'");
        }
    }

    // ------------------------------------------------------------ leer y escribir operandos

    /** Un operando puede ser un registro (eax) o un número (5, -3, 0x1F). */
    public int leer(String op) {
        if (op == null) {
            throw new EmuladorError("Falta un operando");
        }
        if (registros.containsKey(op)) {
            return registros.get(op);
        }
        try {
            return (int) (long) Long.decode(op);
        } catch (NumberFormatException e) {
            throw new EmuladorError("'" + op + "' no es un registro ni un número");
        }
    }

    /** El destino siempre tiene que ser un registro. */
    public void escribir(String op, int valor) {
        if (!registros.containsKey(op)) {
            throw new EmuladorError("El destino tiene que ser un registro, no '" + op + "'");
        }
        registros.put(op, valor);
    }

    // ------------------------------------------------------------ suma y resta (ponen las banderas)

    private int sumar(int a, int b) {
        int r = a + b;
        long sinSigno = (a & 0xFFFFFFFFL) + (b & 0xFFFFFFFFL);
        cf = sinSigno > 0xFFFFFFFFL;                 // no cupo como número sin signo
        of = ((a ^ r) & (b ^ r)) < 0;                // dos del mismo signo dieron el signo contrario
        zf = (r == 0);
        sf = (r < 0);
        return r;
    }

    private int restar(int a, int b) {
        int r = a - b;
        cf = Integer.compareUnsigned(a, b) < 0;      // hubo que pedir prestado (a < b sin signo)
        of = ((a ^ b) & (a ^ r)) < 0;                // el resultado no cupo con signo
        zf = (r == 0);
        sf = (r < 0);
        return r;
    }

    // ------------------------------------------------------------ mostrar

    /** Los registros en una línea. Los que cambiaron llevan *. */
    public String mostrar(Map<String, Integer> antes) {
        StringBuilder sb = new StringBuilder("  ");
        for (String r : registros.keySet()) {
            boolean cambio = antes != null && !antes.get(r).equals(registros.get(r));
            sb.append(r.toUpperCase()).append("=").append(registros.get(r)).append(cambio ? "*" : "").append("  ");
        }
        sb.append("|  ZF=").append(zf ? 1 : 0).append(" SF=").append(sf ? 1 : 0)
          .append(" CF=").append(cf ? 1 : 0).append(" OF=").append(of ? 1 : 0);
        return sb.toString();
    }
}
