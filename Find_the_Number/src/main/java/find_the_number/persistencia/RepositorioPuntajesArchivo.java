package find_the_number.persistencia;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import find_the_number.aplicacion.RepositorioPuntajes;
import find_the_number.dominio.Puntaje;

public final class RepositorioPuntajesArchivo implements RepositorioPuntajes {
    private final Path ruta;

    public RepositorioPuntajesArchivo(Path ruta) {
        this.ruta = Objects.requireNonNull(ruta).toAbsolutePath().normalize();
    }

    @Override
    public List<Puntaje> cargar() {
        List<Puntaje> puntajes = new ArrayList<>();
        try (BufferedReader lector = Files.newBufferedReader(ruta, StandardCharsets.UTF_8)) {
            String linea;
            int numeroLinea = 0;
            while ((linea = lector.readLine()) != null) {
                numeroLinea++;
                if (linea.isBlank()) {
                    continue;
                }
                String[] campos = linea.split("\t", -1);
                if (campos.length == 4 && campos[0].isBlank() && campos[1].equals("0")
                        && campos[2].equals("00:00") && campos[3].matches("0+")) {
                    continue;
                }
                try {
                    if (campos.length != 4 || !campos[2].matches("[0-9]{2,}:[0-5][0-9]")) {
                        throw new IllegalArgumentException("Se requieren cuatro campos y tiempo MM:SS");
                    }
                    String[] tiempo = campos[2].split(":");
                    long segundos = Math.addExact(Math.multiplyExact(Long.parseLong(tiempo[0]), 60L),
                            Long.parseLong(tiempo[1]));
                    puntajes.add(new Puntaje(campos[0], Integer.parseInt(campos[1]), segundos,
                            Integer.parseInt(campos[3])));
                } catch (IllegalArgumentException | ArithmeticException ex) {
                    throw new IllegalArgumentException("Puntaje invalido en " + ruta + ", linea " + numeroLinea, ex);
                }
            }
        } catch (NoSuchFileException ex) {
            return List.of();
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo leer el archivo de puntajes: " + ruta, ex);
        }
        return List.copyOf(puntajes);
    }

    @Override
    public void guardar(List<Puntaje> puntajes) {
        List<Puntaje> copia = List.copyOf(puntajes);
        try {
            Path directorio = ruta.getParent();
            Files.createDirectories(directorio);
            Path temporal = Files.createTempFile(directorio, "puntajes-", ".tmp");
            try {
                try (BufferedWriter escritor = Files.newBufferedWriter(temporal, StandardCharsets.UTF_8)) {
                    for (int posicion = 0; posicion < copia.size(); posicion++) {
                        if (posicion > 0) {
                            escritor.write('\n');
                        }
                        Puntaje puntaje = copia.get(posicion);
                        escritor.write(String.join("\t", puntaje.jugador(), String.valueOf(puntaje.turnos()),
                                puntaje.tiempo(), String.valueOf(puntaje.puntos())));
                    }
                }
                try {
                    Files.move(temporal, ruta, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } catch (AtomicMoveNotSupportedException ex) {
                    Files.move(temporal, ruta, StandardCopyOption.REPLACE_EXISTING);
                }
            } finally {
                Files.deleteIfExists(temporal);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo guardar el archivo de puntajes: " + ruta, ex);
        }
    }
}