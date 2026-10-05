package find_the_number.ui;

import java.awt.BorderLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import find_the_number.dominio.Puntaje;

public final class PanelPuntajes extends JPanel {
    private static final long serialVersionUID = 1L;
    private final DefaultTableModel modelo = new DefaultTableModel(new Object[] {"Jugador", "Turnos", "Tiempo", "Puntaje"}, 0) {
        private static final long serialVersionUID = 1L;
        @Override
        public boolean isCellEditable(int fila, int columna) { return false; }
        @Override
        public Class<?> getColumnClass(int columna) { return columna == 1 || columna == 3 ? Integer.class : String.class; }
    };
    private final JLabel estado = new JLabel("Sin puntajes");

    public PanelPuntajes(Runnable volver) {
        super(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        JTable tabla = new JTable(modelo);
        tabla.setName("puntajes");
        tabla.setRowHeight(26);
        tabla.getTableHeader().setReorderingAllowed(false);
        DefaultTableCellRenderer centrado = new DefaultTableCellRenderer();
        centrado.setHorizontalAlignment(SwingConstants.CENTER);
        for (int columna = 0; columna < 4; columna++) {
            tabla.getColumnModel().getColumn(columna).setCellRenderer(centrado);
        }
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        JButton menu = new JButton("Menu");
        menu.setName("menu");
        menu.addActionListener(evento -> volver.run());
        JPanel pie = new JPanel(new BorderLayout(12, 0));
        pie.add(menu, BorderLayout.WEST);
        pie.add(estado, BorderLayout.CENTER);
        add(pie, BorderLayout.SOUTH);
    }

    public void mostrar(List<Puntaje> puntajes) {
        modelo.setRowCount(0);
        for (Puntaje puntaje : puntajes) {
            modelo.addRow(new Object[] {puntaje.jugador(), puntaje.turnos(), puntaje.tiempo(), puntaje.puntos()});
        }
        estado.setText(puntajes.isEmpty() ? "Sin puntajes" : puntajes.size() + " puntajes");
    }
}