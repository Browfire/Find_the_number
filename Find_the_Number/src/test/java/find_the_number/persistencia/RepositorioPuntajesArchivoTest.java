package find_the_number.persistencia;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import find_the_number.aplicacion.Ranking;
import find_the_number.aplicacion.RepositorioPuntajes;
import find_the_number.dominio.Puntaje;

class RepositorioPuntajesArchivoTest {
    @TempDir
    Path directorio;

    @ParameterizedTest
    @CsvSource({"1100, true", "150, true", "100, false", "50, false"})
    void clasificaConComparacionEstricta(int puntos, boolean clasifica) {
        assertEquals(clasifica, new Ranking(repositorioConDiezPuntajes()).clasifica(puntos));
    }

    @ParameterizedTest
    @CsvSource({"1100, 0", "550, 5", "150, 9"})
    void insertaInclusoElDecimoPuesto(int puntos, int posicion) {
        var repositorio = repositorioConDiezPuntajes();
        Ranking ranking = new Ranking(repositorio);
        assertTrue(ranking.registrar(puntaje("Nuevo", puntos)));
        List<Puntaje> tabla = new Ranking(repositorio).mejores();
        assertEquals(10, tabla.size());
        assertEquals(puntaje("Nuevo", puntos), tabla.get(posicion));
        if (posicion < 9) {
            assertEquals("Jugador" + posicion, tabla.get(posicion + 1).jugador());
        }
        for (int actual = 1; actual < tabla.size(); actual++) {
            assertTrue(tabla.get(actual - 1).puntos() >= tabla.get(actual).puntos());
        }
        assertThrows(UnsupportedOperationException.class, () -> ranking.mejores().clear());
    }

    @Test
    void noClasificarDejaElArchivoIntacto() throws IOException {
        var repositorio = repositorioConDiezPuntajes();
        byte[] original = Files.readAllBytes(directorio.resolve("puntajes.txt"));
        assertFalse(new Ranking(repositorio).registrar(puntaje("Nuevo", 50)));
        assertArrayEquals(original, Files.readAllBytes(directorio.resolve("puntajes.txt")));
    }

    @Test
    void unArchivoAusenteNoSeCreaHastaElPrimerGuardado() {
        Path archivo = directorio.resolve("nuevo/puntajes.txt");
        var repositorio = new RepositorioPuntajesArchivo(archivo);
        Ranking ranking = new Ranking(repositorio);
        assertTrue(ranking.mejores().isEmpty());
        assertFalse(Files.exists(archivo));
        assertTrue(ranking.registrar(puntaje("Nuevo", 0)));
        assertEquals(List.of(puntaje("Nuevo", 0)), repositorio.cargar());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "\n\n", " \t0\t00:00\t000\n \t0\t00:00\t000"})
    void admiteArchivoVacioYPlantillasAntiguas(String contenido) throws IOException {
        Path archivo = directorio.resolve("puntajes.txt");
        Files.writeString(archivo, contenido);
        var repositorio = new RepositorioPuntajesArchivo(archivo);
        assertTrue(repositorio.cargar().isEmpty());
        assertTrue(new Ranking(repositorio).registrar(puntaje("Nuevo", 0)));
        assertEquals(1, repositorio.cargar().size());
    }

    @Test
    void ordenaUnRankingIncompletoYConservaEmpates() {
        var repositorio = new RepositorioPuntajesArchivo(directorio.resolve("puntajes.txt"));
        repositorio.guardar(List.of(puntaje("Bajo", 100), puntaje("Primero", 200), puntaje("Segundo", 200)));
        Ranking ranking = new Ranking(repositorio);
        ranking.registrar(puntaje("Nuevo", 200));
        assertEquals(List.of("Primero", "Segundo", "Nuevo", "Bajo"),
                ranking.mejores().stream().map(Puntaje::jugador).toList());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Ana\t1\t00:01", "Ana\t1\t00:01\t100\textra", "Ana\tabc\t00:01\t100",
        "Ana\t0\t00:01\t100", "Ana\t1\t00:01\tabc", "Ana\t1\t00:01\t-100",
        "Ana\t1\t00:99\t100", " \t1\t00:01\t100", "Ana\t1\t99999999999999999999:00\t100"
    })
    void informaFilasCorruptasSinSobrescribirlas(String contenido) throws IOException {
        Path archivo = directorio.resolve("puntajes.txt");
        Files.writeString(archivo, contenido);
        var repositorio = new RepositorioPuntajesArchivo(archivo);
        var error = assertThrows(IllegalArgumentException.class, () -> new Ranking(repositorio));
        assertTrue(error.getMessage().contains("linea 1"));
        assertEquals(contenido, Files.readString(archivo));
    }

    @Test
    void informaErroresDeAcceso() {
        var repositorio = new RepositorioPuntajesArchivo(directorio);
        assertThrows(UncheckedIOException.class, repositorio::cargar);
    }

    @Test
    void unErrorDeGuardadoNoModificaElArchivoQueBloqueaLaRuta() throws IOException {
        Path bloqueo = directorio.resolve("bloqueo");
        Files.writeString(bloqueo, "Conservar");
        var repositorio = new RepositorioPuntajesArchivo(bloqueo.resolve("puntajes.txt"));
        assertThrows(UncheckedIOException.class, () -> repositorio.guardar(List.of(puntaje("Nuevo", 100))));
        assertEquals("Conservar", Files.readString(bloqueo));
    }

    @Test
    void guardaEnUtf8SinTemporalesResidualesYPreservaPuntosHistoricos() throws IOException {
        Path archivo = directorio.resolve("puntajes.txt");
        var repositorio = new RepositorioPuntajesArchivo(archivo);
        Puntaje historico = new Puntaje("Jos\u00e9", 3, 3601L, 777);
        repositorio.guardar(List.of(historico));
        assertEquals(List.of(historico), repositorio.cargar());
        assertEquals("Jos\u00e9\t3\t60:01\t777", Files.readString(archivo));
        try (var archivos = Files.list(directorio)) {
            assertEquals(1L, archivos.count());
        }
    }

    @Test
    void leerNoReescribeYRankingLimitaSoloLaVista() throws IOException {
        var repositorio = new RepositorioPuntajesArchivo(directorio.resolve("puntajes.txt"));
        var entradas = new ArrayList<Puntaje>();
        for (int puntos = 1; puntos <= 12; puntos++) {
            entradas.add(puntaje("Jugador" + puntos, puntos));
        }
        repositorio.guardar(entradas);
        byte[] original = Files.readAllBytes(directorio.resolve("puntajes.txt"));
        Ranking ranking = new Ranking(repositorio);
        assertEquals(10, ranking.mejores().size());
        assertEquals(12, ranking.mejores().getFirst().puntos());
        assertEquals(3, ranking.mejores().getLast().puntos());
        assertArrayEquals(original, Files.readAllBytes(directorio.resolve("puntajes.txt")));
    }

    @Test
    void rechazaUnaCodificacionInvalidaSinModificarElArchivo() throws IOException {
        Path archivo = directorio.resolve("puntajes.txt");
        byte[] original = {(byte) 0xc3, (byte) 0x28};
        Files.write(archivo, original);
        assertThrows(UncheckedIOException.class, () -> new RepositorioPuntajesArchivo(archivo).cargar());
        assertArrayEquals(original, Files.readAllBytes(archivo));
    }

    @Test
    void cargaUnaVezYNoCambiaLaVistaSiElGuardadoFalla() {
        AtomicInteger lecturas = new AtomicInteger();
        RepositorioPuntajes repositorio = new RepositorioPuntajes() {
            public List<Puntaje> cargar() {
                lecturas.incrementAndGet();
                return List.of(puntaje("Anterior", 100));
            }
            public void guardar(List<Puntaje> entradas) {
                throw new UncheckedIOException(new IOException("Fallo simulado"));
            }
        };
        Ranking ranking = new Ranking(repositorio);
        assertTrue(ranking.clasifica(200));
        assertThrows(UncheckedIOException.class, () -> ranking.registrar(puntaje("Nuevo", 200)));
        assertEquals(List.of(puntaje("Anterior", 100)), ranking.mejores());
        assertEquals(1, lecturas.get());
    }

    private RepositorioPuntajesArchivo repositorioConDiezPuntajes() {
        var repositorio = new RepositorioPuntajesArchivo(directorio.resolve("puntajes.txt"));
        var entradas = new ArrayList<Puntaje>();
        for (int posicion = 0; posicion < 10; posicion++) {
            entradas.add(puntaje("Jugador" + posicion, 1000 - posicion * 100));
        }
        repositorio.guardar(entradas);
        return repositorio;
    }

    private Puntaje puntaje(String jugador, int puntos) {
        return new Puntaje(jugador, 1, 1L, puntos);
    }
}