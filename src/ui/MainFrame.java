package ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import javax.xml.stream.events.XMLEvent;

import org.bounce.text.LineNumberMargin;
import org.bounce.text.ScrollableEditorPanel;
import org.bounce.text.xml.XMLEditorKit;
import org.bounce.text.xml.XMLFoldingMargin;
import org.bounce.text.xml.XMLScanner;
import org.bounce.text.xml.XMLStyleConstants;

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
	 * The XML editor instance.
	 */
	private JEditorPane xmlEditor;

	/**
	 * The modfile editor instance.
	 */
	private JEditorPane modEditor;
	
	/**
	 * Constructs the application's main frame.
	 * @param title the title string appearing in the frame's title bar
	 */
	private MainFrame(String title) {
		// instantiate frame
		// TODO: add application icon
		super(title);
		
		// create and lay out the frame's components
		try {
			this.initComponents();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
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
	 * @throws IOException if an I/O error occurs
	 */
	private void initComponents() throws IOException {
		// create and install menu bar
		this.setJMenuBar(this.createMenuBar());
		
		// configure content pane layout
		Container contentPane = this.getContentPane();
		
		// create and configure left-hand xml editor
		this.xmlEditor = new JEditorPane();
		
		XMLEditorKit xmlKit = new XMLEditorKit();
		xmlKit.setAutoIndentation(true);
		xmlKit.setTagCompletion(true);
		
//		xmlKit.setAutoIndentation(true);
		xmlEditor.setEditorKit(xmlKit);
		xmlEditor.setEditable(false);
		xmlEditor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		
		ScrollableEditorPanel xmlPnl = new ScrollableEditorPanel(xmlEditor);
		JScrollPane xmlPane = new JScrollPane(xmlPnl);
		xmlPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		xmlPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		xmlPane.setPreferredSize(new Dimension(400, 600));

		JPanel rowHeader = new JPanel(new BorderLayout());
		rowHeader.add(new XMLFoldingMargin(xmlEditor), BorderLayout.EAST);
		rowHeader.add(new LineNumberMargin(xmlEditor), BorderLayout.WEST);
		xmlPane.setRowHeaderView(rowHeader);
		
		modEditor = new JEditorPane();
		modEditor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		// TODO: insert custom editor kit for modfile format
		
		JScrollPane modPane = new JScrollPane(modEditor);
		modPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		modPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		modPane.setPreferredSize(new Dimension(400, 600));
		
		// wrap editors in split pane with vertical divider
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, xmlPane, modPane);
		
		contentPane.add(splitPane);
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
		newItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				JEditorPane xmlEditor = MainFrame.this.xmlEditor;
				// clear XML editor contents
				xmlEditor.setText(null);
				// TODO: do some other clean-up tasks in the future, possibly prompt for confirmation
			}
		});
		
		JMenuItem openItem = new JMenuItem("Open File...");
		openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		openItem.addActionListener(new BrowseActionListener(this, Constants.XML_FILE_FILTER) {
			@Override
			protected void execute(File file) {
				try {
					// TODO: possibly prompt for confirmation
					JEditorPane xmlEditor = MainFrame.this.xmlEditor;
					xmlEditor.read(new FileReader(file), file);
					
					final PlainDocument xmlDocument = (PlainDocument) xmlEditor.getDocument();
					xmlDocument.putProperty(PlainDocument.tabSizeAttribute, 2);
					xmlDocument.putProperty(XMLEditorKit.ERROR_HIGHLIGHTING_ATTRIBUTE, new Boolean(true));
					
					xmlEditor.addCaretListener(new CaretListener() {
						@Override
						public void caretUpdate(CaretEvent evt) {
							int dot = evt.getDot();
							Element line = xmlDocument.getParagraphElement(dot);

							XMLScanner scanner;
							try {
								scanner = new XMLScanner(line.getDocument());
								scanner.setRange(line.getStartOffset(), line.getEndOffset());
								
								while (scanner.getEventType() != XMLEvent.END_ELEMENT) {
									scanner.scan();

									int startOffset = scanner.getStartOffset();
									int endOffset = scanner.getEndOffset();
									String text = xmlDocument.getText(startOffset, endOffset - startOffset);
									
									if (XMLStyleConstants.ELEMENT_NAME.equals(scanner.token)) {
										if (!"file".equals(text)) {
											break;
										}
									} else if (XMLStyleConstants.ATTRIBUTE_NAME.equals(scanner.token)) {
										if (!"name".equals(text)) {
											break;
										}
									} else if (XMLStyleConstants.ATTRIBUTE_VALUE.equals(scanner.token)) {
										JEditorPane modEditor = MainFrame.this.modEditor;
										// clear modfile editor
										modEditor.setText(null);
										// trim leading/trailing quotation marks
										String filename = text.replaceAll("^\"|\"$", "");
										// create file descriptor
										File file = new File(filename);
										// check whether file actually exists
										if (file.exists()) {
											// parse file into editor
											modEditor.read(new FileReader(file), file);
										} else {
											// show error text
											modEditor.setText("ERROR: File \'" + filename + "\' could not be found.");
										}
										// re-apply styling
										modEditor.getDocument().putProperty(PlainDocument.tabSizeAttribute, 2);
										
										break;
									}
								}
								
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						
					});
					
					xmlEditor.setEditable(true);
					// repaint parent (or rather great-grandparent) scroll pane to update row header
					xmlEditor.getParent().getParent().getParent().repaint();
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
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
		// TODO: prettify dialog... like, really, it's fugly
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
	
	/**
	 * Convenience implementation of the action listener interface for selecting a file and doing an operation on it.
	 * 
	 * @author XMS
	 */
	private abstract static class BrowseActionListener implements ActionListener {

		/**
		 * The shared reference to the last selected file.
		 */
		private static File lastSelectedFile;
		
		/**
		 * The parent component of the file selection dialog.
		 */
		private Component parent;

		/**
		 * The file filter used in the file selection dialog.
		 */
		private FileFilter filter;
		
		/**
		 * Constructs a browse action listener showing its file selection dialog
		 * atop the specified parent component.
		 * 
		 * @param parent
		 */
		public BrowseActionListener(Component parent, FileFilter filter) {
			this.parent = parent;
			this.filter = filter;
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			// create and configure file chooser instance
			JFileChooser chooser = new JFileChooser(getLastSelectedFile());
			chooser.setFileFilter(this.filter);
			chooser.setMultiSelectionEnabled(false);
			chooser.setAcceptAllFileFilterUsed(false);
			// show file selection dialog
			int res = chooser.showOpenDialog(this.parent);
			if (res == JFileChooser.APPROVE_OPTION) {
				File selectedFile = chooser.getSelectedFile();
				// execute operation
				this.execute(selectedFile);
				// store reference to selected file
				lastSelectedFile = selectedFile;
			}
		}
		
		/**
		 * Executes upon successfully seleting a file. Subclasses need to
		 * implement this to perform operations on the selected file.
		 * @param file the file to perform operations on
		 */
		protected abstract void execute(File file);
		
		/**
		 * Returns the reference to the last selected file.
		 * @return the last selected file
		 */
		public static File getLastSelectedFile() {
			return lastSelectedFile;
		}
		
	}
	
}
