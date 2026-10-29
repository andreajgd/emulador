import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * El programa completo, listo para ejecutar:
 *   - la lista de instrucciones, en orden
 *   - dónde está cada etiqueta. Ej: "menor" -> 3  (la etiqueta menor: apunta a la instrucción 3)
 */
public class Programa {
    public final List<Instruccion> instrucciones = new ArrayList<>();
    public final Map<String, Integer> etiquetas = new HashMap<>();
}