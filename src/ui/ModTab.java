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
import javax.swing.text.AbstractDocument;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.TabSet;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.tree.DefaultTreeCellRenderer;

import model.modtree.ModGenericLeaf;
import model.modtree.ModOffsetLeaf;
import model.modtree.ModOperandNode;
import model.modtree.ModReferenceLeaf;
import model.modtree.ModStringLeaf;
import model.modtree.ModTree;
import model.modtree.ModTreeNode;
import model.upk.UpkFile;

import org.bounce.text.LineNumberMargin;
import static ui.Constants.TEXT_PANE_FONT;

import util.unrealhex.HexSearchAndReplace;

/**
 * The basic component inside the tabbed pane.
 * @author XMS
 */
@SuppressWarnings("serial")
public class ModTab extends JSplitPane {
	
	/**
	 * The logger.
	 */
	public static final Logger logger = Logger.getLogger(ModTab.class.getName());

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
	
	// TODO: deprecate message, font, color 
	/**
	 * The current update message.
	 */
	@Deprecated
	private String updateMessage = "no modfile loaded";
	
	/**
	 * The current update message font.
	 */
	@Deprecated
	private Font updateFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
	
	/**
	 * The current update background color.
	 */
	@Deprecated
	private Color updateBGColor  = new Color(214, 217, 223);

	/**
	 * Flag indicating whether the mod is applied or not.
	 */
	@Deprecated
	private boolean modIsApplied = false;
	
	/**
	 * Flag indicating whether the mod can be applied or not (if errors or not).
	 */
	@Deprecated
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
		this(modFile, false);
	}

	/**
	 * Creates a new tab from the specified modfile reference.
	 * @param modFile the modfile to parse
	 * @param isTemplate specifies whether the file being opened is a template file
	 */
	public ModTab(File modFile, boolean isTemplate) {
		super(JSplitPane.HORIZONTAL_SPLIT);
		this.modFile = modFile;
		
		try {
			this.initComponents();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failure in ModTab component initiation: " + e);
		}
		if(isTemplate) {
			this.modFile = null;  // remove link to file
		}

	}
	
	/**
	 * Creates and lays out the components of the tab.
	 * @param modFile
	 */
	private void initComponents() throws Exception {
		// create right-hand editor pane
		modEditor = new JEditorPane();
		modEditor.setFont(TEXT_PANE_FONT);

		// install editor kit
		modEditor.setEditorKit(new StyledEditorKit() {
			@Override
			public ViewFactory getViewFactory() {
				return new ViewFactory() {
					@Override
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
									@Override
									public float nextTabStop(float x, int tabOffset) {
										TabSet tabs = getTabSet();
										if(tabs == null) {
											// a tab every 72 pixels.
											return (float)(getTabBase() + (((int)x / 18 + 1) * 18));
										}
 
										return super.nextTabStop(x, tabOffset);
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
		
		StyledDocument modDocument = (StyledDocument) modEditor.getDocument();
//		modDocument.putProperty(PlainDocument.tabSizeAttribute, 4);
//		 configure look-and-feel of StyledDocument
//		StyleContext sc = new StyleContext();
//		Style defaultStyle = sc.getStyle(StyleContext.DEFAULT_STYLE);
//		final Style mainStyle = sc.addStyle("MainStyle", defaultStyle);
//		StyleConstants.setLeftIndent(mainStyle, 16);
//		StyleConstants.setRightIndent(mainStyle, 16);
//		StyleConstants.setFirstLineIndent(mainStyle, 16);
//		StyleConstants.setFontFamily(mainStyle, "serif");
//		StyleConstants.setFontSize(mainStyle, 12);
//
//		final Style cwStyle = sc.addStyle("ConstantWidth", null);
//		StyleConstants.setFontFamily(cwStyle, "monospaced");
//		StyleConstants.setForeground(cwStyle, Color.green);
//
//		final Style heading2Style = sc.addStyle("Heading2", null);
//		StyleConstants.setForeground(heading2Style, Color.red);
//		StyleConstants.setFontSize(heading2Style, 16);
//		StyleConstants.setFontFamily(heading2Style, "serif");
//		StyleConstants.setBold(heading2Style, true);
//		StyleConstants.setLeftIndent(heading2Style, 4);
//		StyleConstants.setFirstLineIndent(heading2Style, 0);
//
//		modDocument.setLogicalStyle(0, mainStyle);
//		modDocument.setParagraphAttributes(0, modDocument.getLength(), heading2Style, false);

		// create tree view of right-hand mod editor
		modTree = new ModTree(modDocument);
//		final JTree modElemTree = new JTree(modTree.getRoot()); // draw from ModTree
		final JTree modElemTree = new JTree(modTree); // draw from ModTree
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
				if (((value instanceof ModOperandNode) && expanded )) {
					value = ((ModTreeNode) value).toString(expanded);
				} else if((value instanceof ModReferenceLeaf) 
						|| (value instanceof ModGenericLeaf) 
						|| (value instanceof ModStringLeaf) 
						|| (value instanceof ModOffsetLeaf)) {
					value = ((ModTreeNode) value).toString(true);
				}
				Component comp = super.getTreeCellRendererComponent(tree, value, sel, expanded,
						leaf, row, hasFocus);
				comp.setFont(TEXT_PANE_FONT);
				return comp;
			}
		};
		renderer.setLeafIcon(null);
		renderer.setClosedIcon(null);
		renderer.setOpenIcon(null);
		modElemTree.setCellRenderer(renderer);
			
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
			logger.log(Level.SEVERE, "Failure in modfile save: " + e);
		}
	}

	/**
	 * Searches the associated UPK file for the byte data of the <code>BEFORE</code>
	 * block(s) and overwrites it using the byte data of the <code>AFTER</code> block(s).
	 * @return true if changes applied successfully, false if not
	 * @XTMS -- the key here is that there can be multiple non-adjacent before/after blocks
	 * see AIAddNewObjectives@XGStrategyAI.upk_mod in the sample project
	 *      -- a few lines at the end of the function are changed, as well as the header
	 */
	public boolean applyChanges() {
		try {
			if(this.modTree.getAction().equals("")) { // default action of making changes to object
				if(this.modTree.getResizeAmount() == 0) {
					// basic search and replace without file backup
					if(this.searchAndReplace(
							HexSearchAndReplace.consolidateBeforeHex(this.modTree, this.getUpkFile()),
							HexSearchAndReplace.consolidateAfterHex(this.modTree, this.getUpkFile()))
							) {
						ModTab.logger.log(Level.INFO, "AFTER Hex Installed");
						return true;
					}
				} else {
					// advanced search and replace resizing function (many changes to upk)
					if(HexSearchAndReplace.resizeAndReplace(true, this.modTree, this.getUpkFile())) {
						ModTab.logger.log(Level.INFO, "Function resized and AFTER Hex Installed");
						return true;
					}
				}
			} else { // perform special action
				// TODO: replace within enumeration?
				if(this.getTree().getAction().equalsIgnoreCase("typechange")) {
					if(	HexSearchAndReplace.changeObjectType(true, modTree)) {
						ModTab.logger.log(Level.INFO, "Variable type changed to AFTER");
						return true;
					}
				}
			}
		} catch(IOException ex) {
			ModTab.logger.log(Level.SEVERE, "File error", ex);
		}
		return false;
	}

	/**
	 * Searches the associated UPK file for the byte data of the <code>AFTER</code>
	 * block(s) and overwrites it using the byte data of the <code>BEFORE</code> block(s).
	 * @return true if changes reverted successfully, false otherwise
	 */
	public boolean revertChanges() {
		try {
			if(this.modTree.getAction().equals("")) { // default action of making changes to object
				if(this.modTree.getResizeAmount() == 0) {
					// basic search and replace without file backup
					if(this.searchAndReplace(
							HexSearchAndReplace.consolidateAfterHex(this.modTree, this.getUpkFile()),
							HexSearchAndReplace.consolidateBeforeHex(this.modTree, this.getUpkFile()))
							) {
						ModTab.logger.log(Level.INFO, "BEFORE Hex Installed");
						return true;
					}
				} else {
					// advanced search and replace resizing function (many changes to upk)
					if(HexSearchAndReplace.resizeAndReplace(false, this.modTree, this.getUpkFile())) {
						ModTab.logger.log(Level.INFO, "Function resized and BEFORE Hex Installed");
						return true;
					}
				}
			} else { // perform special action
				// TODO: replace within enumeration?
				if(this.modTree.getAction().equalsIgnoreCase("typechange")) {
					if (HexSearchAndReplace.changeObjectType(false, modTree)) {
						ModTab.logger.log(Level.INFO, "Variable type reverted to BEFORE");
						return true;
					}
				}
			}
		} catch(IOException ex) {
			ModTab.logger.log(Level.SEVERE, "File error", ex);
		}
		return false;
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
	
	// TODO: Rewrite to return apply/revert status for setting file/project string attributes
	public void setUpdateStatus(boolean checkBothDirections) {
		if(this.modTree == null) {
			ModTab.logger.log(Level.SEVERE, "No ModFile");
			return;
		}
		if(this.getUpkFile() == null) {
			ModTab.logger.log(Level.SEVERE, "No upk present");
			return;
		}
		List<byte[]> beforeHex = HexSearchAndReplace.consolidateBeforeHex(this.modTree, this.getUpkFile());
		if(beforeHex.isEmpty()) {
			ModTab.logger.log(Level.INFO, "No/empty BEFORE Blocks");
			return;
		}
		List<byte[]> afterHex =	HexSearchAndReplace.consolidateAfterHex(this.modTree, this.getUpkFile());
		if(afterHex.isEmpty()) {
			ModTab.logger.log(Level.INFO, "No/empty AFTER Blocks");
			return;
		}
		try {
			if(checkBothDirections) {
				if (testBeforeAndAfterBlocks(beforeHex, afterHex) != null) {
					ModTab.logger.log(Level.INFO, "BEFORE Hex Installed");
				} else if (testBeforeAndAfterBlocks(afterHex, beforeHex) != null) {
					ModTab.logger.log(Level.INFO, "AFTER Hex Installed");
				} else {
					modIsApplied = false;
				}
			} else { // check only based on the current status
				if(this.modIsApplied) {
					if (testBeforeAndAfterBlocks(afterHex, beforeHex) != null) {
					ModTab.logger.log(Level.INFO, "AFTER Hex Installed");
						this.setUpdateMessage("AFTER Hex Installed");
					}						
				} else {
					if (testBeforeAndAfterBlocks(beforeHex, afterHex) != null) {
					ModTab.logger.log(Level.INFO, "BEFORE Hex Installed");
						this.setUpdateMessage("BEFORE Hex Installed");
					}						
				}
			}
			
		} catch(IOException ex) {
			ModTab.logger.log(Level.SEVERE, "File error", ex);
		}
	}
	
	private long[] testBeforeAndAfterBlocks(List<byte[]> patterns, List<byte[]> replacements) throws IOException {
		// perform simple error checking first
		// check for same number of blocks
		if(patterns.size() != replacements.size()) {
			ModTab.logger.log(Level.INFO, "Block count mismatch");
			return null;
		}
		// check each block has same number of bytes
		long[] filePositions = new long[patterns.size()];
		for (int i = 0; i < patterns.size() ; i++) {
			if(patterns.get(i).length != replacements.get(i).length) {
			ModTab.logger.log(Level.INFO, "Block " + i + " bytecount mismatch. FIND = " 
					+ patterns.get(i).length + ", REPLACE = " 
					+ replacements.get(i).length);
				return null;
			}
		}
		// try and find each pattern blocks position
		for(int j = 0; j < patterns.size() ; j ++) {
			long filePos = HexSearchAndReplace.findFilePosition(patterns.get(j), this.getUpkFile(), this.modTree);
			if(filePos == -1) {
				ModTab.logger.log(Level.INFO, "Block " + j + " FIND not found");
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