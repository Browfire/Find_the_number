package find_the_number;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

@SuppressWarnings("serial")
public class MenuPrincipal extends JFrame {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MenuPrincipal frame = new MenuPrincipal();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	

	/**
	 * Create the frame.
	 */
	public MenuPrincipal() {
		super("F1ND TH3 NUMBE3R");
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 399, 247);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JButton btnJugar = new JButton("Jugar");
		btnJugar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			VentanaJuego comienzo=new VentanaJuego();
			comienzo.setVisible(true);
			dispose();
			}
		});
		btnJugar.setBounds(136, 72, 120, 23);
		contentPane.add(btnJugar);
		
		JButton btnPuntajes = new JButton("Puntajes");
		btnPuntajes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new MostrarPuntaje().setVisible(true);
				dispose();
			}
		});
		btnPuntajes.setBounds(136, 140, 120, 23);
		contentPane.add(btnPuntajes);
		
		JButton btnInstrucciones = new JButton("Instrucciones");
		btnInstrucciones.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new Instrucciones().setVisible(true);
				dispose();
			}
		});
		btnInstrucciones.setBounds(136, 106, 120, 23);
		contentPane.add(btnInstrucciones);
		
		JButton btnSalir = new JButton("Salir");
		btnSalir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			System.exit(0);
			}
		});
		btnSalir.setBounds(136, 174, 120, 23);
		contentPane.add(btnSalir);
		
		JLabel lblTitle = new JLabel("F1nd th3 Numb3r");
		lblTitle.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 18));
		lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitle.setBounds(121, 11, 164, 23);
		contentPane.add(lblTitle);
		setLocationRelativeTo(null);
	}
}
