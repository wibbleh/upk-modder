package ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.PlainDocument;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import model.modtree.ModOperandNode;
import model.modtree.ModTree;
import model.upk.UpkFile;

import org.bounce.text.LineNumberMargin;

import util.unrealhex.HexSearchAndReplace;

/**
 * The basic component inside the tabbed pane.
 * @author XMS
 */
public class ModTab extends JSplitPane {

	/**
	 * The modfile editor instance.
	 */
	private JEditorPane modEditor;

	/**
	 * The modfile tree structure.
	 */
	private ModTree modTree;

	/**
	 * The modfile associated with this tab.
	 */
	private File modFile;
	
	// TODO: consolidate message, font, color into class?
	/**
	 * The current update message.
	 */
	private String updateMessage = "no modfile loaded";
	
	/**
	 * The current update message font.
	 */
	private Font updateFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
	
	/**
	 * The current update background color.
	 */
	private Color updateBGColor  = new Color(214, 217, 223);

	/**
	 * Flag indicating whether the mod is applied or not.
	 */
	private boolean modIsApplied = false;
	
	/**
	 * Flag indicating whether the mod can be applied or not (if errors or not).
	 */
	private boolean modCanBeApplied = false;

	/**
	 * The UPK file associated with this tab.
	 */
	// is now reflected from/stored in the modTree to enable ref name display in tree view
//	private UpkFile upkFile;  

	/**
	 * Creates a new tab with an empty editor.
	 */
	public ModTab() {
		this(null);
	}

	/**
	 * Creates a new tab from the specified modfile reference.
	 * @param modFile the modfile to parse
	 */
	public ModTab(File modFile) {
		super(JSplitPane.HORIZONTAL_SPLIT);
		this.modFile = modFile;
		
		try {
			this.initComponents();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates and lays out the components of the tab.
	 * @param modFile
	 */
	private void initComponents() throws Exception {
		// create right-hand editor pane
		modEditor = new JEditorPane();
		modEditor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

		// install editor kit
		modEditor.setEditorKit(new StyledEditorKit() {
			@Override
			public ViewFactory getViewFactory() {
				return new ViewFactory() {
			        public View create(Element elem) {
			            String kind = elem.getName();
			            if (kind != null) {
			                if (kind.equals(AbstractDocument.ContentElementName)) {
			                    return new LabelView(elem);
			                } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
			                	return new ParagraphView(elem) {
			                    	/* hack to prevent line wrapping */
			                    	@Override
									public void layout(int width, int height) {
										super.layout(Short.MAX_VALUE, height);
									}
			                    	@Override
									public float getMinimumSpan(int axis) {
										return super.getPreferredSpan(axis);
									}
			                    };
			                } else if (kind.equals(AbstractDocument.SectionElementName)) {
			                    return new BoxView(elem, View.Y_AXIS);
			                } else if (kind.equals(StyleConstants.ComponentElementName)) {
			                    return new ComponentView(elem);
			                } else if (kind.equals(StyleConstants.IconElementName)) {
			                    return new IconView(elem);
			                }
			            }
			            // default to text display
			            return new LabelView(elem);
			        }
			    };
			}
		});
		
		// read provided file, if possible
		if (modFile != null) {
			modEditor.read(new FileInputStream(modFile), modFile);
		}

		// wrap editor in scroll pane
		JScrollPane modEditorScpn = new JScrollPane(modEditor,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		modEditorScpn.setRowHeaderView(new LineNumberMargin(modEditor));
		modEditorScpn.setPreferredSize(new Dimension(650, 600));
		
		Document modDocument = modEditor.getDocument();
		modDocument.putProperty(PlainDocument.tabSizeAttribute, 4);

		// create tree view of right-hand mod editor
		modTree = new ModTree(modDocument);
		final JTree modElemTree = new JTree(modTree.getRoot()); // draw from ModTree
		JScrollPane modElemTreeScpn = new JScrollPane(modElemTree,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		modElemTreeScpn.setPreferredSize(new Dimension());
		
		// configure look and feel of tree view
		modElemTree.setRootVisible(false);
//		modElemTree.setShowsRootHandles(false);
//		modElemTree.putClientProperty("JTree.lineStyle", "Angled");
		
		// display alternate operand text info for opened ModOperandNodes
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
			@Override
			public Component getTreeCellRendererComponent(JTree tree,
					Object value, boolean sel, boolean expanded, boolean leaf,
					int row, boolean hasFocus) {
				if ((value instanceof ModOperandNode) && expanded) {
					value = ((ModOperandNode) value).toString(expanded);
				}
				Component comp = super.getTreeCellRendererComponent(tree, value, sel, expanded,
						leaf, row, hasFocus);
				comp.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
				return comp;
			}
		};
		renderer.setLeafIcon(null);
		renderer.setClosedIcon(null);
		renderer.setOpenIcon(null);
		modElemTree.setCellRenderer(renderer);
			
		// install document listener to refresh tree on changes to the document
		modDocument.addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent evt) {
				this.updateTree(evt);
			}
			@Override
			public void insertUpdate(DocumentEvent evt) {
				this.updateTree(evt);
			}
			@Override
			public void changedUpdate(DocumentEvent evt) {
				this.updateTree(evt);
			}
			/** Updates the tree views on document changes */
			private void updateTree(DocumentEvent evt) {
				// reset mod tree
				((DefaultTreeModel) modElemTree.getModel()).setRoot(
						modTree.getRoot());

//				// expand tree
//				for (int i = 0; i < modElemTree.getRowCount(); i++) {
//					modElemTree.expandRow(i);
//				}
			}
		});

//		// expand tree
//		for (int i = 0; i < modElemTree.getRowCount(); i++) {
//			modElemTree.expandRow(i);
//		}
		
		// wrap tree and editor in split pane
		this.setLeftComponent(modEditorScpn);
		this.setRightComponent(modElemTreeScpn);
		this.setOneTouchExpandable(true);

		// initially hide right-hand tree view
		this.setSize(1000, 0);
		this.setDividerLocation(1.0);
		
	}

	/**
	 * Saves the editor's contents to the file associated with this tab.
	 */
	public void saveFile() {
		try {
			this.getEditor().write(new OutputStreamWriter(new FileOutputStream(this.modFile)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Searches the associated UPK file for the byte data of the <code>BEFORE</code>
	 * block(s) and overwrites it using the byte data of the <code>AFTER</code> block(s).
	 * @XTMS -- the key here is that there can be multiple non-adjacent before/after blocks
	 * see AIAddNewObjectives@XGStrategyAI.upk_mod in the sample project
	 *      -- a few lines at the end of the function are changed, as well as the header
	 */
	public void applyChanges() {
		try {
			if(this.searchAndReplace(
					HexSearchAndReplace.consolidateBeforeHex(this.modTree, this.getUpkFile()),
					HexSearchAndReplace.consolidateAfterHex(this.modTree, this.getUpkFile()))
					) {
				this.setUpdateMessage("AFTER Hex Installed");
				this.setUpdateBackgroundColor(new Color(255, 255, 0));
				this.modIsApplied = true;
			};
		} catch(IOException ex) {
			Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
			this.setUpdateMessage("File error " + ex);
			this.setUpdateBackgroundColor(new Color(255, 128, 128));
		}
	}

	/**
	 * Searches the associated UPK file for the byte data of the <code>AFTER</code>
	 * block(s) and overwrites it using the byte data of the <code>BEFORE</code> block(s).
	 */
	public void revertChanges() {
		try {
			if(this.searchAndReplace(
					HexSearchAndReplace.consolidateAfterHex(this.modTree, this.getUpkFile()),
					HexSearchAndReplace.consolidateBeforeHex(this.modTree, this.getUpkFile()))
					) {
				this.setUpdateMessage("BEFORE Hex Installed");
				this.setUpdateBackgroundColor(new Color(128, 255, 128));
				this.modIsApplied = false;
			}
		} catch(IOException ex) {
			Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
			this.setUpdateMessage("File error " + ex);
			this.setUpdateBackgroundColor(new Color(255, 128, 128));
		}
	}
	
	/**
	 * Searches the associated UPK file for the provided byte pattern and
	 * overwrites it using the provided replacement bytes.
	 * @param patterns the byte pattern to search for
	 * @param replacements the bytes to replace the search pattern with
	 * @Return true if S&R was successful, false otherwise
	 */
	private boolean searchAndReplace(List<byte[]> patterns, List<byte[]> replacements) throws IOException {
		// perform error checking first
		long[] filePositions = testBeforeAndAfterBlocks(patterns, replacements);
		if(filePositions == null) {
			return false;
		}

		// everything matches, time to make the change(s)
		for(int i = 0 ; i < filePositions.length; i++) {
			HexSearchAndReplace.applyHexChange(replacements.get(i), this.getUpkFile(), filePositions[i]);
		}
		return true;
	}
	
	public void setUpdateStatus(boolean checkBothDirections) {
		if(this.modTree == null) {
			this.setUpdateMessage("No file data");
			this.setUpdateBackgroundColor(new Color(255, 128, 128));
			this.modIsApplied = false;
			this.modCanBeApplied = false;
			return;
		}
		if(this.getUpkFile() == null) {
			this.setUpdateMessage("No upk present");
			this.setUpdateBackgroundColor(new Color(255, 128, 128));
			this.modIsApplied = false;
			this.modCanBeApplied = false;
			return;
		}
		List<byte[]> beforeHex = HexSearchAndReplace.consolidateBeforeHex(this.modTree, this.getUpkFile());
		if(beforeHex.isEmpty()) {
			this.setUpdateMessage("No/empty BEFORE Blocks");
			this.setUpdateBackgroundColor(new Color(255, 128, 128));
			this.modIsApplied = false;
			this.modCanBeApplied = false;
			return;
		}
		List<byte[]> afterHex =	HexSearchAndReplace.consolidateAfterHex(this.modTree, this.getUpkFile());
		if(afterHex.isEmpty()) {
			this.setUpdateMessage("No/empty AFTER Blocks");
			this.setUpdateBackgroundColor(new Color(255, 128, 128));
			this.modIsApplied = false;
			this.modCanBeApplied = false;
			return;
		}
		try {
			if(checkBothDirections) {
				if (testBeforeAndAfterBlocks(beforeHex, afterHex) != null) {
					this.setUpdateMessage("BEFORE Hex Installed");
					this.setUpdateBackgroundColor(new Color(128, 255, 128));
					this.modIsApplied = false;
					this.modCanBeApplied = true;
				} else if (testBeforeAndAfterBlocks(afterHex, beforeHex) != null) {
					this.setUpdateMessage("AFTER Hex Installed");
					this.setUpdateBackgroundColor(new Color(128, 255, 128));
					this.modIsApplied = true;
					this.modCanBeApplied = true;
				} else {
					modIsApplied = false;
				}
			} else { // check only based on the current status
				if(this.modIsApplied) {
					if (testBeforeAndAfterBlocks(afterHex, beforeHex) != null) {
						this.setUpdateMessage("AFTER Hex Installed");
						this.setUpdateBackgroundColor(new Color(128, 255, 128));
						this.modIsApplied = true;
						this.modCanBeApplied = true;
					}						
				} else {
					if (testBeforeAndAfterBlocks(beforeHex, afterHex) != null) {
						this.setUpdateMessage("BEFORE Hex Installed");
						this.setUpdateBackgroundColor(new Color(128, 255, 128));
						this.modIsApplied = false;
						this.modCanBeApplied = true;
					}						
				}
			}
			
		} catch(IOException ex) {
			Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
			this.setUpdateMessage("File error " + ex);
			this.setUpdateBackgroundColor(new Color(255, 128, 128));
		}
	}
	
	private long[] testBeforeAndAfterBlocks(List<byte[]> patterns, List<byte[]> replacements) throws IOException {
		// perform simple error checking first
		// check for same number of blocks
		if(patterns.size() != replacements.size()) {
			this.setUpdateMessage("Block count mismatch");
			this.setUpdateBackgroundColor(new Color(255, 128, 128));
			this.modCanBeApplied = false;
			return null;
		}
		// check each block has same number of bytes
		long[] filePositions = new long[patterns.size()];
		for (int i = 0; i < patterns.size() ; i++) {
			if(patterns.get(i).length != replacements.get(i).length) {
				this.setUpdateMessage("Block " + i + " bytecount mismatch");
				this.setUpdateBackgroundColor(new Color(255, 128, 128));
				this.modCanBeApplied = false;
				return null;
			}
		}
		// try and find each pattern blocks position
		for(int j = 0; j < patterns.size() ; j ++) {
			long filePos = HexSearchAndReplace.findFilePosition(patterns.get(j), this.getUpkFile(), this.modTree);
			if(filePos == -1) {
				this.setUpdateMessage("Block " + j + " not found");
				this.setUpdateBackgroundColor(new Color(255, 128, 128));
				this.modCanBeApplied = false;
				return null;
			} else {
				filePositions[j]= filePos;
			}
		}
		return filePositions;
	}
	
	/**
	 * Returns the modfile editor instance of this tab.
	 * @return the editor
	 */
	public JEditorPane getEditor() {
		return modEditor;
	}

	/**
	 * Returns the <code>ModTree</code> instance of this tab.
	 * @return the <code>ModTree</code>
	 */
	public ModTree getTree() {
		return modTree;
	}

	/**
	 * Returns the file associated with this tab.
	 * @return the file
	 */
	public File getModFile() {
		return modFile;
	}
	
	/**
	 * Sets the modfile associated with this tab.
	 * @param modFile the modfile to set
	 */
	public void setModFile(File modFile) {
		this.modFile = modFile;
	}
	
	/**
	 * Returns the UPK file associated with this tab.
	 * @return the UPK file
	 */
	public UpkFile getUpkFile() {
		return this.modTree.getSourceUpk();
//		return upkFile;
	}

	/**
	 * Sets the UPK file associated with this tab.
	 * @param upkFile the upk file to set
	 */
	public void setUpkFile(UpkFile upkFile) {
		this.modTree.setSourceUpk(upkFile);
//		this.upkFile = upkFile;
	}

	private String getUpdateMessage() {
		return this.updateMessage;
	}

	private Font getUpdateFont() {
		return this.updateFont;
	}

	private Color getUpdateBackgroundColor() {
		return this.updateBGColor;
	}
	
	private void setUpdateMessage(String msg) {
		this.updateMessage = msg;
	}

	private void setUpdateFont(Font font) {
		this.updateFont = font;
	}

	private void setUpdateBackgroundColor(Color color) {
		this.updateBGColor = color;
	}

	public boolean modIsApplied() {
		return this.modIsApplied;
	}
	
	public boolean modCanBeApplied() {
		return this.modCanBeApplied;
	}
	
}