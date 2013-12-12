package ui.dialogs;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import model.modtree.ModTree;
import ui.BrowseActionListener;
import ui.Constants;
import ui.MainFrame;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import java.util.List;
import model.upk.UpkFile;
import util.unrealhex.HexStringLibrary;
import util.unrealhex.ReferenceUpdate;

/**
 * TODO: API
 * 
 * @author XMS
 */
@SuppressWarnings("serial")
public class ReferenceUpdateDialog extends JDialog {
	
	private static UpkFile upkSrcFile;
	private static UpkFile upkDstFile;
	private static ReferenceUpdate updater ;

				/**
	 * The reference to the singleton instance of the reference update dialog.
	 */
	private static ReferenceUpdateDialog instance;
	
	/**
	 * The reference to the current modfile tree structure.
	 */
	private ModTree modTree;
	
	/**
	 * TODO: API
	 */
	private ReferenceUpdateDialog() {
		super(MainFrame.getInstance(), "Update References", true);
		
		this.initComponents();
		
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent evt) {
				close();
			}
		});
		
		// adjust dialog size
		this.pack();
		this.setResizable(false);
		// center dialog in main frame
		this.setLocationRelativeTo(MainFrame.getInstance());
	}

	/**
	 * TODO: API
	 * @return
	 */
	public static ReferenceUpdateDialog getInstance() {
		if (instance == null) {
			instance = new ReferenceUpdateDialog();
		}
		return instance;
	}
	
	/**
	 * TODO: API
	 */
	private void initComponents() {
		Container contentPane = this.getContentPane();
		
//		contentPane.setLayout(new FormLayout("5px, m:g, 5px", "5px, t:p, 5px, f:300px:g, 5px, b:p, 5px"));
		contentPane.setLayout(new FormLayout("5px, f:600px:g, 5px", "5px, t:p, 5px, f:300px:g, 5px, b:p, 5px"));

		// create panel containing update controls
		FormLayout controlLyt = new FormLayout("p:g, 5px, p:g", "p");
		controlLyt.setColumnGroups(new int[][] { { 1, 3 } });
		
		JPanel controlPnl = new JPanel(controlLyt);
		
		JButton sourceBtn = new JButton("Pick Source UPK...");
		final JButton destBtn = new JButton("Pick Destination UPK...");
		destBtn.setEnabled(false);

		controlPnl.add(sourceBtn, CC.xy(1, 1));
		controlPnl.add(destBtn, CC.xy(3, 1));
		
		// create table containing 
		final DefaultTableModel refTblMdl = new DefaultTableModel() {
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				switch (columnIndex) {
					case 0:
						return Boolean.class;
					default:
						return super.getColumnClass(columnIndex);
				}
			}
			@Override
			public boolean isCellEditable(int row, int column) {
				return (column == 0);
			}
		};
		refTblMdl.setColumnIdentifiers(new Object[] { "", "Before", "Name", "After" });
		
		JTable refTbl = new JTable(refTblMdl);
		refTbl.setAutoCreateRowSorter(true);
		refTbl.getTableHeader().setReorderingAllowed(false);
		
		TableColumn selCol = refTbl.getColumnModel().getColumn(0);
		selCol.setMaxWidth(selCol.getMinWidth());
		
		DefaultTableCellRenderer monoRenderer = new DefaultTableCellRenderer() {
			/** TODO: API */
			private Font monoFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
				Component comp = super.getTableCellRendererComponent(
						table, value, isSelected, hasFocus, row, column);
				if ("not found!".equals(value)) {
					comp.setForeground((isSelected) ? Color.cyan : Color.RED);
				} else {
					comp.setForeground((isSelected) ? Color.WHITE : Color.BLACK);
					comp.setFont(this.monoFont);
				}
				return comp;
			}
		};
		refTbl.getColumnModel().getColumn(1).setCellRenderer(monoRenderer);
		refTbl.getColumnModel().getColumn(3).setCellRenderer(monoRenderer);

		JScrollPane refScpn = new JScrollPane(refTbl,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		// create bottom panel containing 'OK' and 'Cancel' buttons
		FormLayout buttonLyt = new FormLayout("0px:g, p, 5px, p, 5px, p, 5px, p", "p");
		buttonLyt.setColumnGroups(new int[][] { { 2, 4 } });
		JPanel buttonPnl = new JPanel(buttonLyt);
		
		final JButton okayBtn = new JButton("OK");
		okayBtn.setEnabled(false);
		okayBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				apply();
				close();
			}
		});
		
		JButton cancelBtn = new JButton("Cancel");
		cancelBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				close();
			}
		});
		
		final JButton applyBtn = new JButton("Apply");
		applyBtn.setEnabled(false);
		applyBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				apply();
			}
		});

		final JButton testBtn = new JButton("Test");
		testBtn.setEnabled(false);
		testBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				test();
			}
		});

		buttonPnl.add(okayBtn, CC.xy(2, 1));
		buttonPnl.add(cancelBtn, CC.xy(4, 1));
		buttonPnl.add(testBtn, CC.xy(6, 1));
		buttonPnl.add(applyBtn, CC.xy(8, 1));
		
		// install listeners on source/destination buttons to populate/update the table
		sourceBtn.addActionListener(new BrowseActionListener(this, Constants.UPK_FILE_FILTER) {
			@Override
			protected void execute(File file) {
				// clear table
				refTblMdl.setRowCount(0);
				// populate table
				upkSrcFile = new UpkFile(file);
				updater = new ReferenceUpdate(modTree, modTree.getDocument(), upkSrcFile);
				List<Integer> sourceRefs = updater.getSourceReferences();
				List<String> sourceRefNames = updater.getReferenceNames();
				String sourceRefString = HexStringLibrary.convertIntToHexString(sourceRefs.get(0));
				for(int i = 0; i < updater.getSourceReferences().size(); i++) {
					sourceRefString = HexStringLibrary.convertIntToHexString(sourceRefs.get(i));
					refTblMdl.addRow(new Object[] { true, sourceRefString, sourceRefNames.get(i), null });
				}
				// check GUID
				if(!updater.verifySourceGUID()) {
					// put here the visual indicator of a GUID mismatch
				}
				// enable destination button
				destBtn.setEnabled(true);
			}
		});
		
		destBtn.addActionListener(new BrowseActionListener(this, Constants.UPK_FILE_FILTER) {
			@Override
			protected void execute(File file) {
				// TODO: read selected UPK, parse header, match references
				upkDstFile = new UpkFile(file);
				updater = new ReferenceUpdate(modTree, modTree.getDocument(), upkSrcFile, upkDstFile);
//				List<Integer> sourceRefs = updater.getSourceReferences();
//				List<String> sourceRefNames = updater.getReferenceNames();
				List<Integer> destRefs = updater.getDestReferences();
				List<Boolean> destRefErrs = updater.getDestRefErrors();
				// iterate table
				for (int row = 0; row < refTblMdl.getRowCount(); row++) {
//					String refBefore = (String) refTblMdl.getValueAt(row, 1);
//					String refName = (String) refTblMdl.getValueAt(row, 2);
					// TODO: do reference lookup
						String refAfter = HexStringLibrary.convertIntToHexString(destRefs.get(row));
						// update table
						if (destRefErrs.get(row)) {
							refTblMdl.setValueAt(false, row, 0);
						} else {
							refTblMdl.setValueAt(refAfter, row, 3);
						}
				}
				// enable 'OK' and 'Apply' buttons
				okayBtn.setEnabled(true);
				testBtn.setEnabled(true);
				applyBtn.setEnabled(true);
			}
		});
		
		// add everything to content pane
		contentPane.add(controlPnl, CC.xy(2, 2));
		contentPane.add(refScpn, CC.xy(2, 4));
		contentPane.add(buttonPnl, CC.xy(2, 6));
	}

	/**
	 * TODO: API
	 */
	private void apply() {
		// TODO -- currently applies all references -- check boxes don't do anything.
		updater.updateDocumentToValue();
	}
	
	/**
	 * TODO: API
	 */
	private void close() {
		// TODO: do some clean-up if necessary
		this.dispose();
	}

	/**
	 * TODO: API
	 */
	private void test() {
		if(updater.testUpdateDocumentToValue(false)) {
//				okayBtn.setEnabled(true);
//				applyBtn.setEnabled(true);
			// TODO : provide visual feedback that test was successful
		} else {
			// TODO: provide visual feedback that test failed
		}
	}

	/**
	 * Sets the modfile tree structure reference.
	 * @param modTree the modfile tree to set
	 */
	public void setModTree(ModTree modTree) {
		this.modTree = modTree;
	}

}
