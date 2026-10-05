package find_the_number.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.Component;
import java.awt.Container;
import java.awt.datatransfer.StringSelection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import find_the_number.dominio.CodigoSecreto;
import find_the_number.dominio.Partida;
import find_the_number.dominio.Puntaje;

class PanelesTest {
    @ParameterizedTest
    @ValueSource(strings = {"12345", "-123", "+123", "12a3", "12 3", "12.3", "\uff11\uff12\uff13\uff14"})
    void bloqueaTextoInvalidoAlEscribirYPegar(String texto) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            PanelJuego panel = new PanelJuego(() -> {}, partida -> fail("No deberia finalizar"));
            JTextField entrada = (JTextField) buscar(panel, "entrada");
            entrada.replaceSelection(texto);
            assertEquals("", entrada.getText());
            entrada.setText("0123");
            entrada.selectAll();
            entrada.getTransferHandler().importData(entrada, new StringSelection(texto));
            assertEquals("0123", entrada.getText());
        });
    }

    @Test
    void limitaCuatroDigitosYPermiteReemplazarBorrarYPegarCerosIniciales() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            PanelJuego panel = new PanelJuego(() -> {}, partida -> fail("No deberia finalizar"));
            JTextField entrada = (JTextField) buscar(panel, "entrada");
            entrada.getTransferHandler().importData(entrada, new StringSelection("0123"));
            assertEquals("0123", entrada.getText());
            entrada.setCaretPosition(4);
            entrada.replaceSelection("4");
            assertEquals("0123", entrada.getText());
            entrada.select(1, 3);
            entrada.replaceSelection("98");
            assertEquals("0983", entrada.getText());
            entrada.select(1, 3);
            entrada.replaceSelection("789");
            assertEquals("0983", entrada.getText());
            entrada.select(0, 1);
            entrada.replaceSelection("");
            assertEquals("983", entrada.getText());
            entrada.selectAll();
            entrada.replaceSelection("0456");
            assertEquals("0456", entrada.getText());
            entrada.selectAll();
            entrada.replaceSelection("");
            assertEquals("", entrada.getText());
        });
    }

    @Test
    void iniciarAbandonarYVolverAJugarNoDejaTemporizadoresActivos() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            AtomicInteger retornos = new AtomicInteger();
            PanelJuego panel = new PanelJuego(retornos::incrementAndGet, partida -> fail("No deberia finalizar"));
            try {
                panel.iniciar(new Partida(new CodigoSecreto("1234"), () -> 0L));
                assertTrue(panel.temporizadorActivo());
                ((JButton) buscar(panel, "menu")).doClick(0);
                assertEquals(1, retornos.get());
                assertFalse(panel.temporizadorActivo());
                panel.iniciar(new Partida(new CodigoSecreto("5678"), () -> 0L));
                assertTrue(panel.temporizadorActivo());
                assertEquals(0, ((JTable) buscar(panel, "intentos")).getRowCount());
                assertEquals("00:00", ((JLabel) buscar(panel, "reloj")).getText());
            } finally { panel.detener(); }
        });
    }

    @Test
    void unErrorNoConsumeTurnosYLaVictoriaDetieneElJuegoUnaSolaVez() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            AtomicInteger victorias = new AtomicInteger();
            Partida partida = new Partida(new CodigoSecreto("1234"), () -> 0L);
            PanelJuego panel = new PanelJuego(() -> {}, resultado -> victorias.incrementAndGet());
            try {
                panel.iniciar(partida);
                JTextField entrada = (JTextField) buscar(panel, "entrada");
                JButton ingresar = (JButton) buscar(panel, "ingresar");
                JTable tabla = (JTable) buscar(panel, "intentos");
                entrada.setText("-123");
                ingresar.doClick(0);
                assertEquals(0, partida.turnos());
                assertEquals(0, tabla.getRowCount());
                assertFalse(((JLabel) buscar(panel, "error")).getText().isBlank());
                entrada.setText("1243");
                entrada.postActionEvent();
                assertEquals(1, tabla.getRowCount());
                assertEquals(2, tabla.getValueAt(0, 1));
                assertEquals(2, tabla.getValueAt(0, 2));
                assertFalse(tabla.isCellEditable(0, 0));
                entrada.setText("1234");
                ingresar.doClick(0);
                assertEquals(1, victorias.get());
                assertFalse(panel.temporizadorActivo());
                assertFalse(ingresar.isEnabled());
                assertFalse(entrada.isEditable());
                entrada.postActionEvent();
                assertEquals(1, victorias.get());
                panel.iniciar(new Partida(new CodigoSecreto("5678"), () -> 0L));
                assertTrue(ingresar.isEnabled());
                assertTrue(entrada.isEditable());
                assertEquals(0, tabla.getRowCount());
            } finally { panel.detener(); }
        });
    }

    @Test
    void elRankingSeActualizaSinAcumularFilas() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            AtomicInteger retornos = new AtomicInteger();
            PanelPuntajes panel = new PanelPuntajes(retornos::incrementAndGet);
            panel.mostrar(List.of(new Puntaje("Ana", 1, 4L, 375000)));
            JTable tabla = (JTable) buscar(panel, "puntajes");
            assertEquals(1, tabla.getRowCount());
            assertEquals("00:04", tabla.getValueAt(0, 2));
            assertEquals(Integer.class, tabla.getColumnClass(3));
            assertFalse(tabla.isCellEditable(0, 0));
            panel.mostrar(List.of());
            assertEquals(0, tabla.getRowCount());
            ((JButton) buscar(panel, "menu")).doClick(0);
            assertEquals(1, retornos.get());
        });
    }

    @Test
    void cargaLasInstruccionesDesdeElRecursoEmpaquetado() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            AtomicInteger retornos = new AtomicInteger();
            PanelInstrucciones panel = new PanelInstrucciones(retornos::incrementAndGet);
            JTextArea texto = (JTextArea) buscar(panel, "instrucciones");
            assertTrue(texto.getText().contains("Fama"));
            assertTrue(texto.getLineWrap());
            assertFalse(texto.isEditable());
            ((JButton) buscar(panel, "menu")).doClick(0);
            assertEquals(1, retornos.get());
        });
    }

    private Component buscar(Container raiz, String nombre) {
        for (Component componente : raiz.getComponents()) {
            if (nombre.equals(componente.getName())) {
                return componente;
            }
            if (componente instanceof Container contenedor) {
                Component encontrado = buscar(contenedor, nombre);
                if (encontrado != null) { return encontrado; }
            }
        }
        return null;
    }
}