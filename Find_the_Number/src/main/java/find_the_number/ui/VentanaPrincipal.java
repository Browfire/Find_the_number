package find_the_number.ui;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.UncheckedIOException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import find_the_number.aplicacion.Ranking;
import find_the_number.aplicacion.RepositorioPuntajes;
import find_the_number.dominio.Partida;
import find_the_number.dominio.Puntaje;

public final class VentanaPrincipal extends JFrame {
    private static final long serialVersionUID = 1L;
    private final CardLayout navegacion = new CardLayout();
    private final JPanel pantallas = new JPanel(navegacion);
    private final RepositorioPuntajes repositorio;
    private final PanelJuego juego;
    private final PanelPuntajes puntajes;

    public VentanaPrincipal(RepositorioPuntajes repositorio) {
        super("F1nd th3 Numb3r");
        this.repositorio = repositorio;
        juego = new PanelJuego(() -> mostrar("menu"), this::finalizar);
        puntajes = new PanelPuntajes(() -> mostrar("menu"));
        pantallas.add(new PanelMenu(this::jugar, this::mostrarPuntajes, () -> mostrar("instrucciones"),
                () -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING))), "menu");
        pantallas.add(juego, "juego");
        pantallas.add(puntajes, "puntajes");
        pantallas.add(new PanelInstrucciones(() -> mostrar("menu")), "instrucciones");
        setContentPane(pantallas);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evento) { juego.detener(); }
        });
        setMinimumSize(new Dimension(520, 360));
        pantallas.setPreferredSize(new Dimension(580, 400));
        pack();
        setLocationRelativeTo(null);
        mostrar("menu");
    }

    @Override
    public void dispose() {
        juego.detener();
        super.dispose();
    }

    private void mostrar(String pantalla) {
        juego.detener();
        navegacion.show(pantallas, pantalla);
    }

    private void jugar() {
        mostrar("juego");
        juego.iniciar(new Partida());
    }

    private void mostrarPuntajes() {
        try {
            puntajes.mostrar(new Ranking(repositorio).mejores());
            mostrar("puntajes");
        } catch (UncheckedIOException | IllegalArgumentException ex) {
            error(ex);
            mostrar("menu");
        }
    }

    private void finalizar(Partida partida) {
        try {
            Puntaje provisional = partida.puntaje("Jugador");
            Ranking ranking = new Ranking(repositorio);
            if (ranking.clasifica(provisional.puntos())) {
                String nombre = solicitarNombre();
                if (nombre != null) {
                    ranking.registrar(partida.puntaje(nombre));
                }
            } else {
                JOptionPane.showMessageDialog(this, "Juego terminado\nPuntaje obtenido: " + provisional.puntos());
            }
            puntajes.mostrar(ranking.mejores());
            mostrar("puntajes");
        } catch (UncheckedIOException | IllegalArgumentException ex) {
            error(ex);
            mostrar("menu");
        }
    }

    private String solicitarNombre() {
        String mensaje = "Puntaje alto obtenido!\nIngrese nombre";
        while (true) {
            String nombre = JOptionPane.showInputDialog(this, mensaje);
            if (nombre == null) {
                return null;
            }
            try {
                return Puntaje.normalizarJugador(nombre);
            } catch (IllegalArgumentException ex) {
                mensaje = ex.getMessage();
            }
        }
    }

    private void error(RuntimeException ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error de puntajes", JOptionPane.ERROR_MESSAGE);
    }
}