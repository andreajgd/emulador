/**
 * Un error en el programa que escribió el usuario. Ej: "la etiqueta 'fin' no existe".
 * Hereda de RuntimeException para no tener que poner "throws" en cada método:
 * el error sube solo hasta el menú (Main), que lo atrapa con try/catch y muestra el mensaje.
 */
public class EmuladorError extends RuntimeException {
    public EmuladorError(String mensaje) {
        super(mensaje);
    }
}
