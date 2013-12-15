package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.text.BadLocationException;

import model.modtree.ModReferenceLeaf;
import model.modtree.ModTree;
import model.upk.UpkFile;
import ui.BrowseActionListener;
import ui.Constants;
import ui.MainFrame;
import util.unrealhex.HexStringLibrary;
import util.unrealhex.ReferenceUpdate;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Dialog implementation listing distinct references of a modfile tree model.
 * <p>
 * Allows to look up references in specifiable UPK files and to apply changes to
 * the document linked to the tree model.
 * 
 * @author XMS, Amineri
 */
@SuppressWarnings("serial")
public class ReferenceUpdateDialog extends JDialog {
	
	/**
	 * The reference to the current modfile tree structure.
	 */
	private ModTree modTree;
	
	/**
	 * Current destination upk, if it exists
	 */
	private UpkFile destUpk = null;
	
	/**
	 * The reference to the reference table.
	 */
	private JTable refTbl;
	
	/**
	 * Flag denoting whether an attempt to look up source references has been
	 * made.
	 */
	private boolean triedSrcLookup;

	/** The index of the Source Reference column. */
	private final int SOURCE_REF_COLUMN = 0;
	/** The index of the Virtual Function Flag column. */
	private final int VF_FLAG_COLUMN = 1;
	/** The index of the Reference Name column. */
	private final int REF_NAME_COLUMN = 2;
	/** The index of the Destination Reference column. */
	private final int DEST_REF_COLUMN = 3;
	
	/** Message string for null return value. */
	private final String NULL_RETURN_REF = "Null Return Ref";
	/** Error message string for missing reference name. */
	private final String NAME_NOT_FOUND = "name not found!";
	/** Error message string for missing reference. */
	private final String REF_NOT_FOUND = "ref not found!";
	
	/**
	 * Constructs a reference update dialog from the specified <code>ModTree</code> instance.<br>
	 * Contains a table listing all distinct references stored in the tree.
	 * @param modTree the modfile tree
	 */
	public ReferenceUpdateDialog(ModTree modTree) {
		super(MainFrame.getInstance(), "Update References", true);
		this.modTree = modTree;
		
		this.initComponents();
		
		// re-route default closing behavior to close() method
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
	 * Initializes and lays out the dialog's components.
	 */
	private void initComponents() {
		Container contentPane = this.getContentPane();
		
		contentPane.setLayout(new FormLayout("5px, 600px:g, 5px", "5px, f:300px:g, 0px, f:p, 0px, b:p, 5px"));
		
		// create table containing 
		final DefaultTableModel refTblMdl = new DefaultTableModel() {
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				switch (columnIndex) {
					case 1:
						return Boolean.class;
					default:
						return super.getColumnClass(columnIndex);
				}
			}
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		refTblMdl.setColumnIdentifiers(new Object[] { "Source Hex", "VF", "Reference Name", "Dest. Hex" });
		
		// extract distinct references from tree -- currently removed
//		Set<ModReferenceLeaf> refNodes = new HashSet<>(ReferenceUpdate.getReferences(this.modTree));
		// extract all references from tree (List, not Set)
		List<ModReferenceLeaf> refNodes = ReferenceUpdate.getReferences(this.modTree);
		// populate table model
		for (ModReferenceLeaf refNode : refNodes) {
			refTblMdl.addRow(new Object[]{
				refNode,
				refNode.isVirtualFunctionRef(),
				(refNode.isName() ) ? refNode.getTextNoTags() : null,
				null
				});
		}
		
		refTbl = new JTable(refTblMdl);
		refTbl.setAutoCreateRowSorter(true);
		refTbl.getTableHeader().setReorderingAllowed(false);
		refTbl.getTableHeader().setResizingAllowed(false);
		
		TableColumnModel refColMdl = refTbl.getColumnModel();
		
		// create a custom renderer for checkbox column to make it visually
		// consistent with other cells, this is really just a visual hack
		final TableCellRenderer delegate = refTbl.getDefaultRenderer(Boolean.class);
		DefaultTableCellRenderer booleanRenderer = new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
				JComponent comp = (JComponent) delegate.getTableCellRendererComponent(
						table, value, isSelected, hasFocus, row, column);
				comp.setOpaque(true);
				if (hasFocus) {
					JPanel panel = new JPanel(new BorderLayout());
					panel.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
					panel.add(comp, BorderLayout.CENTER);
					panel.setBackground(comp.getBackground());
					return panel;
				}
				return comp;
			}
		};
		refTbl.setDefaultRenderer(Boolean.class, booleanRenderer);
		
		DefaultTableCellRenderer monoRenderer = new DefaultTableCellRenderer() {
			/** Reference to monospaced font. */
			private Font monoFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
				if (value instanceof ModReferenceLeaf) {
					int refValue = ((ModReferenceLeaf) value).getRefValue();
					if (!((ModReferenceLeaf) value).isName()) {
						value = HexStringLibrary.convertIntToHexString(
								refValue).trim();
					} else {
						value = (triedSrcLookup) ? REF_NOT_FOUND : null;
					}
				}
				Component comp = super.getTableCellRendererComponent(
						table, value, isSelected, hasFocus, row, column);
				if (REF_NOT_FOUND.equals(value) || NULL_RETURN_REF.equals(value)) {
					comp.setForeground((isSelected) ? Color.CYAN : Color.RED);
					comp.setFont(comp.getFont().deriveFont(Font.ITALIC));
				} else {
					comp.setForeground((isSelected) ? Color.WHITE : Color.BLACK);
					comp.setFont(this.monoFont);
				}
				return comp;
			}
		};
		
		DefaultTableCellRenderer errorRenderer = new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
				Component comp = super.getTableCellRendererComponent(
						table, value, isSelected, hasFocus, row, column);
				if (NAME_NOT_FOUND.equals(value)) {
					comp.setForeground((isSelected) ? Color.CYAN : Color.RED);
					comp.setFont(comp.getFont().deriveFont(Font.ITALIC));
				} else {
					comp.setForeground((isSelected) ? Color.WHITE : Color.BLACK);
				}
				return comp;
			}
		};
		
		refColMdl.getColumn(SOURCE_REF_COLUMN).setCellRenderer(monoRenderer);
		refColMdl.getColumn(SOURCE_REF_COLUMN).setMinWidth(88);
		refColMdl.getColumn(SOURCE_REF_COLUMN).setMaxWidth(88);
		refColMdl.getColumn(VF_FLAG_COLUMN).setMaxWidth(24);
		refColMdl.getColumn(REF_NAME_COLUMN).setCellRenderer(errorRenderer);
		refColMdl.getColumn(DEST_REF_COLUMN).setCellRenderer(monoRenderer);
		refColMdl.getColumn(DEST_REF_COLUMN).setMinWidth(94);
		refColMdl.getColumn(DEST_REF_COLUMN).setMaxWidth(94);
		
		JScrollPane refScpn = new JScrollPane(refTbl,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		JPanel refPnl = new JPanel(new BorderLayout(5, 5));
		refPnl.setBorder(BorderFactory.createTitledBorder("Reference Table"));
		refPnl.add(refScpn, BorderLayout.CENTER);

		// create panel containing update controls
		FormLayout controlLyt = new FormLayout("p:g, 5px, p:g", "f:p");
		controlLyt.setColumnGroups(new int[][] { { 1, 3 } });
		JPanel controlPnl = new JPanel(controlLyt);
		
		// create sub-panel containing source-specific controls
		JPanel sourcePnl = new JPanel(new FormLayout("p:g", "p, 5px, p, 2px"));
		sourcePnl.setBorder(BorderFactory.createTitledBorder("Source References"));
		
		JButton lookupSrcBtn = new JButton("Look Up Source References");
		lookupSrcBtn.setToolTipText("Prompts selection of a UPK file and tries to look up missing reference names and hex values");
		lookupSrcBtn.setEnabled(this.hasSourceRefs());
		final JButton hexToNamesBtn = new JButton("Convert Hex References to Names");
		hexToNamesBtn.setToolTipText("Converts all hex references in the editor to their named counterparts");
		hexToNamesBtn.setEnabled(false);
		
		sourcePnl.add(lookupSrcBtn, CC.xy(1, 1));
		sourcePnl.add(hexToNamesBtn, CC.xy(1, 3));
		
		// create sub-panel containing destination-specific controls
		JPanel destPnl = new JPanel(new FormLayout("p:g", "p, 5px, p"));
		destPnl.setBorder(BorderFactory.createTitledBorder("Destination References"));

		final JButton lookupDestBtn = new JButton("Look Up Destination Reference Values");
		lookupDestBtn.setToolTipText("Prompts selection of a UPK file and tries to look up hex values for all named references");
		lookupDestBtn.setEnabled(!this.hasSourceRefs());
		final JButton srcToDestBtn = new JButton("Convert Source Refs to Destination Refs");
		srcToDestBtn.setToolTipText("Converts all source references in the editor to their corresponding destination hex values");
		srcToDestBtn.setEnabled(false);

		destPnl.add(lookupDestBtn, CC.xy(1, 1));
		destPnl.add(srcToDestBtn, CC.xy(1, 3));

		// install action listeners
		lookupSrcBtn.addActionListener(new BrowseActionListener(this, Constants.UPK_FILE_FILTER) {
			@Override
			protected void execute(File file) {
				boolean res = lookUpSourceReferences(file);
				hexToNamesBtn.setEnabled(res);
				lookupDestBtn.setEnabled(res);
				triedSrcLookup = true;
			}
		});
		
		lookupDestBtn.addActionListener(new BrowseActionListener(this, Constants.UPK_FILE_FILTER) {
			@Override
			protected void execute(File file) {
				boolean res = lookUpDestinationReferences(file);
				srcToDestBtn.setEnabled(res || !hasSourceRefs());
			}
		});
		
		hexToNamesBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				try {
					// TODO: implement better error handling
					convertToRefNames();
				} catch(BadLocationException ex) {
					Logger.getLogger(ReferenceUpdateDialog.class.getName()).log(Level.SEVERE, null, ex);
				}
				
			}
		});

		srcToDestBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				try {
					// TODO: implement better error handling
					convertToRefDestValues();
				} catch(BadLocationException ex) {
					Logger.getLogger(ReferenceUpdateDialog.class.getName()).log(Level.SEVERE, null, ex);
				}
				
			}
		});

		controlPnl.add(sourcePnl, CC.xy(1, 1));
		controlPnl.add(destPnl, CC.xy(3, 1));
		
		// create bottom panel containing 'Close' button
		JPanel buttonPnl = new JPanel(new FormLayout("0px:g, r:p", "p"));
		
		final JButton closeBtn = new JButton("Close");
		closeBtn.setEnabled(true);
		closeBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				close();
			}
		});
		
		buttonPnl.add(closeBtn, CC.xy(2, 1));
		
		// add everything to content pane
		contentPane.add(refPnl, CC.xy(2, 2));
		contentPane.add(controlPnl, CC.xy(2, 4));
		contentPane.add(buttonPnl, CC.xy(2, 6));
	}

	/**
	 * Determines if the dialogue has any valid source reference values.
	 * @return <code>true</code> if dialogue has any non-null reference values,
	 *  <code>false</code> otherwise
	 */
	private boolean hasSourceRefs() {
		ModReferenceLeaf leaf;
		for (int row = 0; row < refTbl.getRowCount(); row++) {
			leaf = (ModReferenceLeaf) refTbl.getValueAt(row, SOURCE_REF_COLUMN);
			if (!leaf.isName() && (leaf.getRefValue() != 0)) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Converts all source reference names present into the already determined values.
	 * Assumes replacement text is value if value is nonzero, otherwise assumes replacing name.
	 * @return <code>true</code> if reference replacement processed without errors,
	 *  <code>false</code> otherwise
	 */
	private boolean convertToRefDestValues() throws BadLocationException {
		boolean res = true;
		String findString;
		// since there are a lot of updates coming, disable the modTree updating temporarily
		// will perform a single refresh at the end
		modTree.disableUpdating();
		
		// the amount that the document offset has been adjusted
		// tracking it this way is MUCH faster than trying to wait for the ModTree to update after each insertion
		int offsetIncrease = 0;
		
		// replace GUID with destination upk GUID info
		offsetIncrease += ReferenceUpdate.replaceGUID(modTree, destUpk);
				
		// iterate table rows
		for (int row = 0; row < refTbl.getRowCount(); row++) {
			// get reference from table
			ModReferenceLeaf refNode = (ModReferenceLeaf) refTbl.getValueAt(row, SOURCE_REF_COLUMN);
			String refName = refTbl.getValueAt(row, REF_NAME_COLUMN).toString();
			String refDestValue = refTbl.getValueAt(row, DEST_REF_COLUMN).toString();
			// extract reference name from UPK file
			if(!refDestValue.isEmpty() && !refName.isEmpty() && !refDestValue.equals(REF_NOT_FOUND)) {
				if(refNode.isName()) {
					findString = ReferenceUpdate.tagReference(refName, refNode);
				} else {
					findString = HexStringLibrary.convertIntToHexString(refNode.getRefValue()).trim();
				}
				// TODO: improve error handling
				// replace the string, retaining total offset change in file.
				offsetIncrease += ReferenceUpdate.replaceRefStringInDocument(
						findString + " ",
						refDestValue,
						refNode,
						offsetIncrease
				);
			}
			
		}
		// force modTree refresh from document and re-enable updating
		modTree.forceRefreshFromDocument();
		modTree.enableUpdating();
		return res;
	}

	/**
	 * Converts all source reference values present into the already determined names.
	 * @return <code>true</code> if reference replacement processed without errors,
	 *  <code>false</code> otherwise
	 */
	private boolean convertToRefNames() throws BadLocationException {
		boolean res = true;
		// since there are a lot of updates coming, disable the modTree updating temporarily
		// will perform a single refresh at the end
		modTree.disableUpdating();
		
		// the amount that the document offset has been adjusted
		// tracking it this way is MUCH faster than trying to wait for the ModTree to update after each insertion
		int offsetIncrease = 0;

		// replace GUID with "unknown"
		offsetIncrease += ReferenceUpdate.replaceGUID(modTree, null);
				
		// iterate table rows
		for (int row = 0; row < refTbl.getRowCount(); row++) {
			// get reference from table
			ModReferenceLeaf refNode = (ModReferenceLeaf) refTbl.getValueAt(row, SOURCE_REF_COLUMN);
			String refName = refTbl.getValueAt(row, REF_NAME_COLUMN).toString();
			// extract reference name from UPK file
			if(!refNode.isName() && !refName.equals(REF_NOT_FOUND) && !refName.isEmpty()) {
				// add reference tags to name
				refName = ReferenceUpdate.tagReference(refName, refNode);
				// TODO: improve error handling
				// replace the string, retaining total offset change in file.
				offsetIncrease += ReferenceUpdate.replaceRefStringInDocument(
						HexStringLibrary.convertIntToHexString(refNode.getRefValue()).trim(),
						refName,
						refNode,
						offsetIncrease
				);
				refTbl.setValueAt(REF_NOT_FOUND, row, SOURCE_REF_COLUMN);
			}
		}
		// force modTree refresh from document and re-enable updating
		modTree.forceRefreshFromDocument();
		modTree.enableUpdating();
		return res;
	}

	/**
	 * Looks up reference names from the specified file and inserts them into
	 * the reference table.
	 * @param file the UPK file to parse
	 * @return <code>true</code> if reference lookup processed without errors,
	 *  <code>false</code> otherwise
	 */
	private boolean lookUpSourceReferences(File file) {
		// init return value
		// TODO: maybe replace with error code of some sort
		boolean res = true;
		
		// parse UPK file
		// TODO: implement progress monitoring for upk parsing
		UpkFile upkFile = new UpkFile(file);
		// extract GUID
		byte[] guid = upkFile.getHeader().getGUID();
		// compare file GUID with tree GUID
		if (!this.modTree.getGuid().trim().equals(HexStringLibrary.convertByteArrayToHexString(guid).trim())) {
			// show warning message
			int option = JOptionPane.showConfirmDialog(this, "Mismatching GUIDs detected. Continue anyway?",
					"GUID Mismatch", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (option == JOptionPane.NO_OPTION) {
				// abort
				return false;
			}
		}
		
		// iterate table rows
		for (int row = 0; row < refTbl.getRowCount(); row++) {
			// get reference from table
			ModReferenceLeaf refNode = (ModReferenceLeaf) refTbl.getValueAt(row, SOURCE_REF_COLUMN);
			// extract reference name from UPK file
			String refName;
			if(!refNode.isName()) {
				// check for null return value
				if(refNode.getRefValue() == 0) {
					refName = NULL_RETURN_REF;
				} else { // valid value, search UpkFile for name
					refName = (refNode.isVirtualFunctionRef()) ?
							upkFile.getVFRefName(refNode.getRefValue()) :
							upkFile.getRefName(refNode.getRefValue());
					// check whether lookup failed
					if (refName.isEmpty()) {
						res = false;
						// replace name string with error message
						refName = NAME_NOT_FOUND;
					}
				}
				// update table row
				refTbl.setValueAt(refName, row, REF_NAME_COLUMN);
			} else {
				// check whether the table row contains a named reference instead
				Object value = refTbl.getValueAt(row, REF_NAME_COLUMN);
				if (value instanceof String) {
					refName = (String) value;
					// try reverse lookup of contextualized reference name
					int refValue = (refNode.isVirtualFunctionRef()) ?
							upkFile.findVFRefByName(refName) :
							upkFile.findRefByName(refName);
					refNode.setRefValue(refValue);
				}
			}
		}
		
		return res;
	}
	
	/**
	 * Looks up reference values from the specified file and inserts them into
	 * the reference table.
	 * @param file the UPK file to parse
	 * @return <code>true</code> if reference lookup processed without errors,
	 *  <code>false</code> otherwise
	 */
	private boolean lookUpDestinationReferences(File file) {
		// init return value
		boolean res = true;
		
		// parse UPK file
		// TODO: implement progress monitoring for upk parsing
		UpkFile upkFile = new UpkFile(file);
		
		// store destination upk for later GUID updating if it is applied
		destUpk = upkFile; 
		
		// iterate table rows
		for (int row = 0; row < refTbl.getRowCount(); row++) {
			// get reference from table
			ModReferenceLeaf refNode = (ModReferenceLeaf) refTbl.getValueAt(row, SOURCE_REF_COLUMN);
			// get reference name from table
			String refName = (String) refTbl.getValueAt(row, REF_NAME_COLUMN);
			// get virtual function flag from table
			boolean vfRef = (Boolean) refTbl.getValueAt(row, VF_FLAG_COLUMN);
			// look up reference value in UPK file
			int refValue = (vfRef) ? upkFile.findVFRefByName(refName) : upkFile.findRefByName(refName);
			// check whether lookup failed
			String refStr = HexStringLibrary.convertIntToHexString(refValue);
			if(vfRef) {
				if (refValue < 0) {
					res = false;
					refStr = REF_NOT_FOUND;
				}
			} else {
				if (refValue == 0) {
					if(!refNode.isName() && refNode.getRefValue() == 0) { // is null return reference
						refStr = HexStringLibrary.convertIntToHexString(0);
					} else { // lookup failed
						res = false;
						// replace ref string with error message
						refStr = REF_NOT_FOUND;
					}
				}
			}
			// update table row
			refTbl.setValueAt(refStr, row, DEST_REF_COLUMN);
		}
		
		return res;
	}
	
	/**
	 * Disposes the dialog.
	 */
	private void close() {
		// TODO: do some clean-up if necessary
		this.dispose();
	}

}
