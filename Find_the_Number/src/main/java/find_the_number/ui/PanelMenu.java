package find_the_number.ui;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public final class PanelMenu extends JPanel {
    private static final long serialVersionUID = 1L;

    public PanelMenu(Runnable jugar, Runnable puntajes, Runnable instrucciones, Runnable salir) {
        super(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        JLabel titulo = new JLabel("F1nd th3 Numb3r", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(java.awt.Font.BOLD, 24f));
        add(titulo, BorderLayout.NORTH);
        JPanel acciones = new JPanel(new GridLayout(4, 1, 8, 8));
        agregar(acciones, "Jugar", "jugar", jugar);
        agregar(acciones, "Puntajes", "puntajes", puntajes);
        agregar(acciones, "Instrucciones", "instrucciones", instrucciones);
        agregar(acciones, "Salir", "salir", salir);
        JPanel centro = new JPanel(new GridBagLayout());
        centro.add(acciones);
        add(centro, BorderLayout.CENTER);
    }

    private void agregar(JPanel panel, String texto, String nombre, Runnable accion) {
        JButton boton = new JButton(texto);
        boton.setName(nombre);
        boton.addActionListener(evento -> accion.run());
        panel.add(boton);
    }
}