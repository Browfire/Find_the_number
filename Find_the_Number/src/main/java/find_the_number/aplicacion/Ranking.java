package find_the_number.aplicacion;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import find_the_number.dominio.Puntaje;

public final class Ranking {
    private static final int LIMITE = 10;
    private final RepositorioPuntajes repositorio;
    private List<Puntaje> puntajes;

    public Ranking(RepositorioPuntajes repositorio) {
        this.repositorio = Objects.requireNonNull(repositorio);
        puntajes = ordenar(repositorio.cargar());
    }

    public List<Puntaje> mejores() {
        return puntajes;
    }

    public boolean clasifica(int puntos) {
        if (puntos < 0) {
            throw new IllegalArgumentException("Los puntos no pueden ser negativos");
        }
        return puntajes.size() < LIMITE || puntos > puntajes.get(LIMITE - 1).puntos();
    }

    public boolean registrar(Puntaje puntaje) {
        Objects.requireNonNull(puntaje);
        if (!clasifica(puntaje.puntos())) {
            return false;
        }
        List<Puntaje> nuevos = new ArrayList<>(puntajes);
        nuevos.add(puntaje);
        List<Puntaje> ordenados = ordenar(nuevos);
        repositorio.guardar(ordenados);
        puntajes = ordenados;
        return true;
    }

    private List<Puntaje> ordenar(List<Puntaje> entradas) {
        return entradas.stream().sorted(Comparator.comparingInt(Puntaje::puntos).reversed())
                .limit(LIMITE).toList();
    }
}