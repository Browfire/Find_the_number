package find_the_number.ui;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public final class PanelInstrucciones extends JPanel {
    private static final long serialVersionUID = 1L;

    public PanelInstrucciones(Runnable volver) {
        super(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        JTextArea texto = new JTextArea(cargarTexto());
        texto.setName("instrucciones");
        texto.setEditable(false);
        texto.setLineWrap(true);
        texto.setWrapStyleWord(true);
        texto.setMargin(new Insets(12, 12, 12, 12));
        texto.setCaretPosition(0);
        add(new JScrollPane(texto), BorderLayout.CENTER);
        JButton menu = new JButton("Menu");
        menu.setName("menu");
        menu.addActionListener(evento -> volver.run());
        JPanel pie = new JPanel(new BorderLayout());
        pie.add(menu, BorderLayout.WEST);
        add(pie, BorderLayout.SOUTH);
    }

    private String cargarTexto() {
        try (var recurso = getClass().getResourceAsStream("/instrucciones.txt")) {
            if (recurso == null) {
                throw new IllegalStateException("No se encontro el recurso de instrucciones");
            }
            return new String(recurso.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudieron cargar las instrucciones", ex);
        }
    }
}