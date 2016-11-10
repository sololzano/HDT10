/**
@author Luis Diego Sierra, Diego López, Carlos Solórzano
*/
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;


public class MainFrame extends JFrame {

	private JPanel contentPane;
	private String path;
    private ArrayList<String[]> matrix;

    /**
	 * Crea el frame.
	 */
	private MainFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 500);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel pnlMain = new JPanel();
		pnlMain.setBackground(Color.WHITE);
		contentPane.add(pnlMain, BorderLayout.CENTER);
		pnlMain.setLayout(null);
		

        GraphDbManager dbManager = GraphDbManager.getInstance();

		JButton btnComunicacionEntrePersonas = new JButton("Comunicacion");
		btnComunicacionEntrePersonas.setBounds(141, 287, 140, 25);
		pnlMain.add(btnComunicacionEntrePersonas);
        btnComunicacionEntrePersonas.addActionListener(event->{
			
		} );
		
		JPanel pnlCorreos = new JPanel();
		pnlCorreos.setBounds(307, 139, 463, 170);
		pnlMain.add(pnlCorreos);
		pnlCorreos.setLayout(null);
		
		JLabel lblCorreosEnviados = new JLabel("CORREOS ENVIADOS");
		lblCorreosEnviados.setBounds(156, 12, 150, 15);
		pnlCorreos.add(lblCorreosEnviados);
		
		JPanel pnlSeleccion = new JPanel();
		pnlSeleccion.setBackground(Color.WHITE);
		pnlSeleccion.setBounds(141, 39, 180, 85);
		pnlCorreos.add(pnlSeleccion);
		pnlSeleccion.setLayout(null);
		
		JLabel lblOrigen = new JLabel("Origen");
		lblOrigen.setBounds(12, 12, 70, 15);
		pnlSeleccion.add(lblOrigen);
		
		JComboBox<String> cmbOrigen = new JComboBox<>();
		cmbOrigen.setBounds(12, 39, 70, 24);
		for(int i = 0; i < 14; i++){
			cmbOrigen.addItem("Per "+ String.valueOf(i + 1));
		}
		pnlSeleccion.add(cmbOrigen);
		
		JLabel lblDestino = new JLabel("Destino");
		lblDestino.setBounds(94, 12, 70, 15);
		pnlSeleccion.add(lblDestino);
		
		JComboBox<String> cmbDestino = new JComboBox<>();
		cmbDestino.setBounds(94, 39, 70, 24);
		cmbDestino.addItem("-");
		for(int i = 0; i < 14; i++){
			cmbDestino.addItem("Per "+ String.valueOf(i + 1));
		}
		pnlSeleccion.add(cmbDestino);
		
		JButton btnConsultar = new JButton("Consultar");
		btnConsultar.setBounds(173, 136, 117, 25);
		btnConsultar.addActionListener(event->{
				});
		pnlCorreos.add(btnConsultar);
		
		JButton btnMetrica = new JButton("Metrica");
		btnMetrica.setBounds(12, 287, 117, 25);
		pnlMain.add(btnMetrica);
        btnMetrica.addActionListener(event->{
            dbManager.showPageRank();
        });
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 324, 758, 121);
		pnlMain.add(scrollPane);
		
		JPanel panel = new JPanel();
		panel.setBounds(12, 139, 269, 136);
		pnlMain.add(panel);
		panel.setLayout(null);
		
		JButton btnVisualizarGrafoDe = new JButton("Grafo con relaciones");
		btnVisualizarGrafoDe.setBounds(12, 25, 245, 25);
		panel.add(btnVisualizarGrafoDe);
        btnVisualizarGrafoDe.addActionListener(event->{
			dbManager.dbToGraph();
		} );
		
		JButton btnRelacionesConMas = new JButton("6 correos o mas");
		btnRelacionesConMas.setBounds(12, 62, 245, 25);
		panel.add(btnRelacionesConMas);
        btnRelacionesConMas.addActionListener(event->{
            dbManager.sixOrMoreMails();
        });
		
		JButton btnGrafoSimplificado = new JButton("Grafo simplificado");
		btnGrafoSimplificado.setBounds(12, 99, 245, 25);
		panel.add(btnGrafoSimplificado);
        btnGrafoSimplificado.addActionListener(e -> {
            dbManager.simplifyGraph();
        });
		
		JLabel lblVisual = new JLabel("VISUAL");
		lblVisual.setBounds(12, 12, 70, 15);
		panel.add(lblVisual);

        final JButton btnCargarArchivoCsv = new JButton("Cargar archivo CSV a Neo4j");
        btnCargarArchivoCsv.addActionListener(event-> {

            /* Seleccion de archivos */
            JFileChooser fc = new JFileChooser();
            fc.setCurrentDirectory(new java.io.File("./src"));
            fc.setDialogTitle("Seleccione su archivo");
            fc.setFileFilter(new FileNameExtensionFilter("CSV files(.csv)", "csv"));
            if(fc.showOpenDialog(btnCargarArchivoCsv) == JFileChooser.APPROVE_OPTION){
                path = fc.getSelectedFile().getAbsolutePath();

                try {
                    Stream<String> stream = Files.lines(Paths.get(path));
                    matrix = new ArrayList<>();
                    stream.forEach((string)->
                            matrix.add(string.split(";"))
                    );

                    
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
        btnCargarArchivoCsv.setBounds(268, 90, 250, 25);
        pnlMain.add(btnCargarArchivoCsv);
	}
	
	/**
	 * Ejecuta la aplicacion.
	 */
	public static void main(String[] args) {
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame frame = new MainFrame();
					frame.setVisible(true);
					frame.setLocationRelativeTo(null);
					frame.setResizable(false);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
