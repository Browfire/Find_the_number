package find_the_number.dominio;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.LongSupplier;

public final class Partida {
    private final CodigoSecreto secreto;
    private final LongSupplier reloj;
    private final long inicio;
    private final List<ResultadoIntento> historial = new ArrayList<>();
    private boolean terminada;
    private long fin;

    public Partida() {
        this(CodigoSecreto.generar(4, new Random()), System::nanoTime);
    }

    public Partida(CodigoSecreto secreto, LongSupplier reloj) {
        this.secreto = Objects.requireNonNull(secreto);
        this.reloj = Objects.requireNonNull(reloj);
        inicio = reloj.getAsLong();
    }

    public ResultadoIntento intentar(String entrada) {
        if (terminada) {
            throw new IllegalStateException("La partida ya termino");
        }
        ResultadoIntento resultado = secreto.comparar(entrada);
        historial.add(resultado);
        if (resultado.acierto()) {
            fin = reloj.getAsLong();
            terminada = true;
        }
        return resultado;
    }

    public long segundos() {
        long ahora = terminada ? fin : reloj.getAsLong();
        return Math.max(0L, ahora - inicio) / 1000000000L;
    }

    public boolean terminada() {
        return terminada;
    }

    public int turnos() {
        return historial.size();
    }

    public List<ResultadoIntento> historial() {
        return List.copyOf(historial);
    }

    public Puntaje puntaje(String jugador) {
        if (!terminada) {
            throw new IllegalStateException("La partida todavia no termina");
        }
        return Puntaje.calcular(jugador, turnos(), segundos());
    }
}