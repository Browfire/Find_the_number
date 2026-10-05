package find_the_number.aplicacion;

import java.util.List;
import find_the_number.dominio.Puntaje;

public interface RepositorioPuntajes {
    List<Puntaje> cargar();
    void guardar(List<Puntaje> puntajes);
}