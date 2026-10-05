package find_the_number.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class PartidaTest {
    @ParameterizedTest
    @CsvSource({"1234, 4, 0", "4321, 0, 4", "1243, 2, 2", "1567, 1, 0", "5678, 0, 0", "0123, 0, 3"})
    void comparaSinAcumularEstado(String entrada, int famas, int toques) {
        CodigoSecreto secreto = new CodigoSecreto("1234");
        ResultadoIntento resultado = secreto.comparar(entrada);
        assertEquals(famas, resultado.famas());
        assertEquals(toques, resultado.toques());
        assertEquals(resultado, secreto.comparar(entrada));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "123", "12345", "1123", "12a3", "12 3", "-123", "+123", " 123", "\uff11\uff12\uff13\uff14"})
    void unIntentoInvalidoNoConsumeTurnos(String entrada) {
        Partida partida = new Partida(new CodigoSecreto("1234"), () -> 0L);
        assertThrows(IllegalArgumentException.class, () -> partida.intentar(entrada));
        assertEquals(0, partida.turnos());
        assertTrue(partida.historial().isEmpty());
    }

    @Test
    void conservaCerosIniciales() {
        assertTrue(new CodigoSecreto("0123").comparar("0123").acierto());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 4, 10})
    void generaDigitosDistintosConLongitudValida(int longitud) {
        CodigoSecreto codigo = CodigoSecreto.generar(longitud, new Random(42));
        assertEquals(longitud, codigo.valor().length());
        assertEquals(longitud, codigo.valor().chars().distinct().count());
        assertEquals(codigo, CodigoSecreto.generar(longitud, new Random(42)));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 11})
    void rechazaLongitudesImposiblesSinEntrarEnUnBucle(int longitud) {
        assertThrows(IllegalArgumentException.class, () -> CodigoSecreto.generar(longitud, new Random()));
    }

    @Test
    void mideTiempoMonotonicoYCongelaLaVictoria() {
        AtomicLong reloj = new AtomicLong(5000000000L);
        Partida partida = new Partida(new CodigoSecreto("1234"), reloj::get);
        assertThrows(IllegalStateException.class, () -> partida.puntaje("Ana"));
        partida.intentar("1243");
        reloj.addAndGet(4200000000L);
        assertEquals(4L, partida.segundos());
        assertTrue(partida.intentar("1234").acierto());
        reloj.addAndGet(9000000000L);
        assertEquals(4L, partida.segundos());
        assertEquals(2, partida.turnos());
        assertTrue(partida.terminada());
        assertEquals(187500, partida.puntaje("Ana").puntos());
        assertThrows(IllegalStateException.class, () -> partida.intentar("1234"));
        assertThrows(UnsupportedOperationException.class, () -> partida.historial().clear());
    }

    @ParameterizedTest
    @CsvSource({"1, 0, 1500000, 00:00", "1, 4, 375000, 00:04", "3, 15, 33333, 00:15", "2, 125, 6000, 02:05", "1, 3600, 416, 60:00"})
    void calculaPuntosConUnSegundoMinimo(int turnos, long segundos, int puntos, String tiempo) {
        Puntaje puntaje = Puntaje.calcular("Ana", turnos, segundos);
        assertEquals(puntos, puntaje.puntos());
        assertEquals(tiempo, puntaje.tiempo());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "Ana\tOtra", "Ana\nOtra"})
    void rechazaNombresInvalidos(String nombre) {
        assertThrows(IllegalArgumentException.class, () -> Puntaje.normalizarJugador(nombre));
    }

    @Test
    void limitaNombresSinCortarUnicode() {
        assertEquals("ABCDEFGHIJ", Puntaje.normalizarJugador(" ABCDEFGHIJKLM "));
        assertEquals("ABCDEFGHI\ud83d\ude00", Puntaje.normalizarJugador("ABCDEFGHI\ud83d\ude00Extra"));
    }

    @ParameterizedTest
    @CsvSource({"0, 1", "1, -1", "-1, 1"})
    void rechazaDuracionesOTurnosInvalidos(int turnos, long segundos) {
        assertThrows(IllegalArgumentException.class, () -> Puntaje.calcular("Ana", turnos, segundos));
    }

    @Test
    void unPuntajeNoPuedeDesbordarseConDuracionesGrandes() {
        assertEquals(0, Puntaje.calcular("Ana", Integer.MAX_VALUE, Long.MAX_VALUE).puntos());
    }
}