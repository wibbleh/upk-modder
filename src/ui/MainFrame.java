package ui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

/**
 * The application's primary frame.
 * 
 * @author XMS
 */
@SuppressWarnings("serial")
public class MainFrame extends JFrame {

	/**
	 * The shared singleton instance of the application's main frame.
	 */
	public static MainFrame instance;
	
	/**
	 * Constructs the application's main frame.
	 * @param title the title string appearing in the frame's title bar
	 */
	private MainFrame(String title) {
		// instantiate frame
		// TODO: add application icon
		super(title);
		
		// create and lay out the frame's components
		this.initComponents();
		
		// make closing the main frame terminate the application
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
		// adjust frame size
		this.pack();
		// center frame in screen
		this.setLocationRelativeTo(null);
		// show frame
		this.setVisible(true);
	}
	
	/**
	 * Returns the singleton instance of the application's main frame.
	 * @return the main frame instance
	 */
	public static MainFrame getInstance() {
		if (instance == null) {
			instance = new MainFrame(Constants.APPLICATION_NAME + " " + Constants.VERSION_NUMBER + "");
		}
		return instance;
	}

	/**
	 * Creates and lays out the frame's components.
	 */
	private void initComponents() {
		// create and install menu bar
		this.setJMenuBar(this.createMenuBar());
		
		// configure content pane layout
		Container contentPane = this.getContentPane();
		contentPane.setLayout(new FormLayout(
				"5px, p:g, 2px, p, 5px, p:g, 2px, p, 5px",
				"5px, p, 2px, p, 5px, f:p:g, 5px, p, 5px"));
		
		// create text fields containing source and destination UPK file paths
		JTextField srcTtf = new JTextField();
		JTextField dstTtf = new JTextField();

		// create browse buttons and link them to text fields via client property
		JButton browseSrcBtn = new JButton("Browse...");
		browseSrcBtn.putClientProperty("textfield", srcTtf);
		JButton browseDstBtn = new JButton("Browse...");
		browseDstBtn.putClientProperty("textfield", dstTtf);

		// create and install listener for browse buttons
		ActionListener browseListener = new ActionListener() {
			/** The reference to the last selected file. */
			private File lastSelectedFile;
			@Override
			public void actionPerformed(ActionEvent evt) {
				// create and configure file chooser instance
				JFileChooser chooser = new JFileChooser(lastSelectedFile);
				chooser.setFileFilter(Constants.UPK_FILE_FILTER);
				chooser.setMultiSelectionEnabled(false);
				chooser.setAcceptAllFileFilterUsed(false);
				// show file selection dialog
				int res = chooser.showOpenDialog(MainFrame.this);
				if (res == JFileChooser.APPROVE_OPTION) {
					// get reference to clicked button from event
					JComponent button = (JComponent) evt.getSource();
					// extract referemce to textfield from button client property
					JTextField textField = (JTextField) button.getClientProperty("textfield");
					// set text field text to path of selected file
					File selectedFile = chooser.getSelectedFile();
					textField.setText(selectedFile.getPath());
					this.lastSelectedFile = selectedFile;
				}
			}
		};
		browseSrcBtn.addActionListener(browseListener);
		browseDstBtn.addActionListener(browseListener);
		
		// create text area for editing mod file hex contents
		// TODO: maybe use separate text areas for before/after blocks
		// TODO: use JEditorPane for formatted text
		JTextArea editorArea = new JTextArea("// here be hex\n00 5C FF");
		JScrollPane editorPane = new JScrollPane(editorArea);
		editorPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		editorPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		editorPane.setPreferredSize(new Dimension(640, 480));
		
		// create panel containing buttons with various functions
		// TODO: find more appropriate names (and possibly positions) for buttons
		FormLayout buttonLyt = new FormLayout(
				"0px:g, p, 5px, p, 5px, p",
				"p");
		buttonLyt.setColumnGroups(new int[][] { { 2, 4, 6 } });
		JPanel buttonPnl = new JPanel(buttonLyt);
		buttonPnl.add(new JButton("Mark References"), CC.xy(2, 1));
		buttonPnl.add(new JButton("Update References"), CC.xy(4, 1));
		buttonPnl.add(new JButton("Apply Hex Replacement"), CC.xy(6, 1));
		
		// add components to content pane
		contentPane.add(new JLabel("Source UPK"), CC.xyw(2, 2, 3));
		contentPane.add(new JLabel("Destination UPK"), CC.xyw(6, 2, 3));
		contentPane.add(srcTtf, CC.xy(2, 4));
		contentPane.add(browseSrcBtn, CC.xy(4, 4));
		contentPane.add(dstTtf, CC.xy(6, 4));
		contentPane.add(browseDstBtn, CC.xy(8, 4));
		contentPane.add(editorPane, CC.xyw(2, 6, 7));
		contentPane.add(buttonPnl, CC.xyw(2, 8, 7));
	}

	/**
	 * Creates and configures the main frame's menu bar.
	 * @return the menu bar
	 */
	private JMenuBar createMenuBar() {
		// create menu bar
		JMenuBar menuBar = new JMenuBar();
		
		// create file menu
		JMenu fileMenu = new JMenu("File");
		
		// create file menu items
		// TODO: add icons
		JMenuItem newItem = new JMenuItem("New");
		newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
		
		JMenuItem openItem = new JMenuItem("Open File...");
		openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		
		JMenuItem saveItem = new JMenuItem("Save File...");
		saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		saveItem.setEnabled(false);
		
		JMenuItem exportItem = new JMenuItem("Export...");
		exportItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
		exportItem.setEnabled(false);
		
		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				// dispose main frame (thereby terminating the application)
				MainFrame.this.dispose();
			}
		});
		
		// TODO: implement functionality for other items
		
		// add items to file menu
		fileMenu.add(newItem);
		fileMenu.add(openItem);
		fileMenu.add(saveItem);
		fileMenu.addSeparator();
		fileMenu.add(exportItem);
		fileMenu.addSeparator();
		fileMenu.add(exitItem);
		
		// create help menu
		JMenu helpMenu = new JMenu("Help");
		
		// create help menu items
		JMenuItem helpItem = new JMenuItem("Help");
		helpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		helpItem.setEnabled(false);
		
		JMenuItem aboutItem = new JMenuItem("About");
		aboutItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				MainFrame.this.showAboutDialog();
			}
		});
		
		// add items to help menu
		helpMenu.add(helpItem);
		helpMenu.addSeparator();
		helpMenu.add(aboutItem);
		
		// add menus to menu bar
		menuBar.add(fileMenu);
		menuBar.add(helpMenu);
		
		return menuBar;
	}
	
	/**
	 * Creates and displays the application's About dialog.
	 */
	private void showAboutDialog() {
		// TODO: prettify dialog
		JDialog aboutDlg = new JDialog(this, "About UPK Modder", true);
		
		Container contentPane = aboutDlg.getContentPane();
		
		JLabel aboutLbl = new JLabel(Constants.ABOUT_TEXT);
		aboutLbl.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		
		contentPane.add(aboutLbl);
		
		aboutDlg.pack();
		aboutDlg.setResizable(false);
		aboutDlg.setLocationRelativeTo(this);
		aboutDlg.setVisible(true);
	}
	
}
