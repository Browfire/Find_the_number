package find_the_number;

import java.nio.file.Path;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import find_the_number.persistencia.RepositorioPuntajesArchivo;
import find_the_number.ui.VentanaPrincipal;

public final class Inicio {
	private Inicio() { }

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			try {
				Path ruta = Path.of(System.getProperty("findthenumber.puntajes", "puntaje.txt"));
				new VentanaPrincipal(new RepositorioPuntajesArchivo(ruta)).setVisible(true);
			} catch (RuntimeException ex) {
				JOptionPane.showMessageDialog(null, ex.getMessage(), "Error de inicio", JOptionPane.ERROR_MESSAGE);
			}
		});
	}
}
