package find_the_number.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import find_the_number.dominio.Partida;
import find_the_number.dominio.Puntaje;
import find_the_number.dominio.ResultadoIntento;

public final class PanelJuego extends JPanel {
    private static final long serialVersionUID = 1L;
    private final JTextField entrada = new JTextField(8);
    private final JLabel reloj = new JLabel("00:00", SwingConstants.RIGHT);
    private final JLabel error = new JLabel(" ");
    private final JButton ingresar = new JButton("Ingresar");
    private final DefaultTableModel modelo = new DefaultTableModel(new Object[] {"Numero", "Toque", "Fama"}, 0) {
        private static final long serialVersionUID = 1L;
        @Override
        public boolean isCellEditable(int fila, int columna) { return false; }
        @Override
        public Class<?> getColumnClass(int columna) { return columna == 0 ? String.class : Integer.class; }
    };
    private final JTable tabla = new JTable(modelo);
    private final Timer temporizador = new Timer(200, evento -> actualizarTiempo());
    private final Consumer<Partida> finalizar;
    private Partida partida;

    public PanelJuego(Runnable volver, Consumer<Partida> finalizar) {
        super(new BorderLayout(12, 12));
        this.finalizar = finalizar;
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        entrada.setName("entrada");
        ((AbstractDocument) entrada.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass filtro, int posicion, String texto, AttributeSet atributos)
                    throws BadLocationException {
                replace(filtro, posicion, 0, texto, atributos);
            }

            @Override
            public void replace(FilterBypass filtro, int posicion, int longitud, String texto, AttributeSet atributos)
                    throws BadLocationException {
                String reemplazo = texto == null ? "" : texto;
                String actual = filtro.getDocument().getText(0, filtro.getDocument().getLength());
                String nuevo = new StringBuilder(actual).replace(posicion, posicion + longitud, reemplazo).toString();
                if (nuevo.matches("[0-9]{0,4}")) {
                    super.replace(filtro, posicion, longitud, reemplazo, atributos);
                }
            }
        });
        entrada.setHorizontalAlignment(SwingConstants.CENTER);
        entrada.addActionListener(evento -> ingresar());
        ingresar.setName("ingresar");
        ingresar.addActionListener(evento -> ingresar());
        reloj.setName("reloj");
        error.setName("error");
        tabla.setName("intentos");
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        controles.add(new JLabel("Ingrese numero"));
        controles.add(entrada);
        controles.add(ingresar);
        JPanel cabecera = new JPanel(new BorderLayout(8, 8));
        cabecera.add(controles, BorderLayout.CENTER);
        cabecera.add(reloj, BorderLayout.EAST);
        add(cabecera, BorderLayout.NORTH);
        tabla.setRowHeight(24);
        tabla.getTableHeader().setReorderingAllowed(false);
        DefaultTableCellRenderer centrado = new DefaultTableCellRenderer();
        centrado.setHorizontalAlignment(SwingConstants.CENTER);
        for (int columna = 0; columna < 3; columna++) {
            tabla.getColumnModel().getColumn(columna).setCellRenderer(centrado);
        }
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        JButton menu = new JButton("Menu");
        menu.setName("menu");
        menu.addActionListener(evento -> { detener(); volver.run(); });
        JPanel pie = new JPanel(new BorderLayout(12, 0));
        pie.add(menu, BorderLayout.WEST);
        pie.add(error, BorderLayout.CENTER);
        add(pie, BorderLayout.SOUTH);
    }

    public void iniciar(Partida partida) {
        detener();
        this.partida = partida;
        modelo.setRowCount(0);
        entrada.setText("");
        entrada.setEditable(true);
        ingresar.setEnabled(true);
        error.setText(" ");
        actualizarTiempo();
        temporizador.start();
        SwingUtilities.invokeLater(entrada::requestFocusInWindow);
    }

    public void detener() {
        temporizador.stop();
    }

    boolean temporizadorActivo() {
        return temporizador.isRunning();
    }

    private void actualizarTiempo() {
        if (partida != null) {
            reloj.setText(Puntaje.formatearTiempo(partida.segundos()));
        }
    }

    private void ingresar() {
        if (partida == null || partida.terminada() || !ingresar.isEnabled()) {
            return;
        }
        ResultadoIntento resultado;
        try {
            resultado = partida.intentar(entrada.getText());
        } catch (IllegalArgumentException ex) {
            error.setText(ex.getMessage());
            entrada.selectAll();
            return;
        }
        modelo.addRow(new Object[] {resultado.numero(), resultado.toques(), resultado.famas()});
        tabla.scrollRectToVisible(tabla.getCellRect(modelo.getRowCount() - 1, 0, true));
        entrada.setText("");
        error.setText(" ");
        actualizarTiempo();
        if (resultado.acierto()) {
            detener();
            ingresar.setEnabled(false);
            entrada.setEditable(false);
            finalizar.accept(partida);
        } else {
            entrada.requestFocusInWindow();
        }
    }
}