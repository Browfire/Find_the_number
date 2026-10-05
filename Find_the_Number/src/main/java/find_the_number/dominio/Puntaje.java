package find_the_number.dominio;

import java.util.Locale;

public record Puntaje(String jugador, int turnos, long segundos, int puntos) {
    public Puntaje {
        if (jugador == null || jugador.isBlank() || jugador.chars().anyMatch(Character::isISOControl)
                || turnos < 1 || segundos < 0 || puntos < 0) {
            throw new IllegalArgumentException("Datos de puntaje invalidos");
        }
    }

    public static Puntaje calcular(String jugador, int turnos, long segundos) {
        if (turnos < 1 || segundos < 0) {
            throw new IllegalArgumentException("Turnos o tiempo fuera de rango");
        }
        int puntos = (int) (1500000L / turnos / Math.max(1L, segundos));
        return new Puntaje(normalizarJugador(jugador), turnos, segundos, puntos);
    }

    public static String normalizarJugador(String jugador) {
        if (jugador == null || jugador.isBlank() || jugador.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Ingrese un nombre sin caracteres de control");
        }
        String nombre = jugador.strip();
        return nombre.substring(0, nombre.offsetByCodePoints(0, Math.min(10, nombre.codePointCount(0, nombre.length()))));
    }

    public static String formatearTiempo(long segundos) {
        if (segundos < 0) {
            throw new IllegalArgumentException("Tiempo fuera de rango");
        }
        return String.format(Locale.ROOT, "%02d:%02d", segundos / 60, segundos % 60);
    }

    public String tiempo() {
        return formatearTiempo(segundos);
    }
}