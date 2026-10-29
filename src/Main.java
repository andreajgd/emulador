import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * El menú: pide qué hacer y usa Ensamblador y Cpu.
 *    javac -encoding UTF-8 -d out src/*.java
 *    java -cp out Main
 */
public class Main {

    static final Scanner teclado = new Scanner(System.in);
    static final Cpu cpu = new Cpu();

    // El ejemplo de clase: int acceso(int edad) { if (edad >= 18) return 1; else return 0; }
    static final String EJEMPLO_EDAD =
            "cmp edi, 18      ; compara la edad con 18\n" +
            "jl menor         ; si es menor que 18, ir a 'menor'\n" +
            "mov eax, 1       ; si no: EAX = 1 (puede entrar)\n" +
            "ret\n" +
            "menor:\n" +
            "xor eax, eax     ; EAX = 0 (no puede entrar)\n" +
            "ret\n";

    public static void main(String[] args) throws UnsupportedEncodingException {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, "UTF-8"));   // acentos en Windows
        System.out.println("=== EMULADOR DE ENSAMBLADOR ===");
        System.out.println(cpu.mostrar(null));

        while (true) {
            System.out.println("\n1. Dar valor a un registro");
            System.out.println("2. Ejecutar una instrucción");
            System.out.println("3. Escribir un programa");
            System.out.println("4. Ejemplo: edad >= 18");
            System.out.println("5. Reiniciar registros");
            System.out.println("0. Salir");
            String opcion = pedir("Opción: ");

            try {
                switch (opcion) {
                    case "1": darValor(); break;
                    case "2": unaInstruccion(); break;
                    case "3": cpu.correr(Ensamblador.ensamblar(leerPrograma())); break;
                    case "4": ejemploEdad(); break;
                    case "5": cpu.reiniciar(); System.out.println(cpu.mostrar(null)); break;
                    case "0": return;
                    default: System.out.println("Escribe un número del 0 al 5.");
                }
            } catch (EmuladorError e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    /** Opción 1: "edi" y "18" -> EDI = 18 */
    static void darValor() {
        String r = pedir("Registro (eax, ebx, ecx, edx, esi, edi): ").toLowerCase();
        cpu.escribir(r, cpu.leer(pedir("Valor: ")));
        System.out.println(cpu.mostrar(null));
    }

    /** Opción 2: ejecuta una sola línea sobre los registros actuales y los muestra. */
    static void unaInstruccion() {
        Instruccion ins = Ensamblador.leerInstruccion(pedir("Instrucción: "), 1);
        if (ins.nombre.startsWith("j")) {        // solo no hay adónde saltar: se dice si saltaría
            boolean si = ins.nombre.equals("jmp") || cpu.saltaria(ins.nombre);
            System.out.println(si ? "  -> SÍ saltaría a '" + ins.op1 + "'" : "  -> NO saltaría");
            return;
        }
        Map<String, Integer> antes = new LinkedHashMap<>(cpu.registros);
        cpu.ejecutar(ins);
        System.out.println(cpu.mostrar(antes));
    }

    /** Opción 3: lee líneas hasta una línea vacía. */
    static String leerPrograma() {
        System.out.println("Escribe el programa (línea vacía = terminar):");
        StringBuilder codigo = new StringBuilder();
        String linea;
        while (!(linea = pedir("  | ")).isEmpty()) {
            codigo.append(linea).append("\n");
        }
        return codigo.toString();
    }

    /** Opción 4: pide la edad, la pone en EDI y corre el ejemplo. */
    static void ejemploEdad() {
        System.out.println("\n" + EJEMPLO_EDAD);
        cpu.reiniciar();
        cpu.escribir("edi", cpu.leer(pedir("Edad: ")));
        cpu.correr(Ensamblador.ensamblar(EJEMPLO_EDAD));
        System.out.println(cpu.registros.get("eax") == 1 ? "Puede entrar." : "No puede entrar.");
    }

    static String pedir(String texto) {

        System.out.print(texto);
        if (!teclado.hasNextLine()) {
            System.exit(0);
        }
        return teclado.nextLine().trim();
    }
}
