package ui.editor;

import java.awt.Color;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * Document class for the mod file format.
 * @author Amineri, XMS
 */
@SuppressWarnings("serial")
public class ModDocument extends DefaultStyledDocument {
	
	// TODO: link enum values to text format styles
	/**
	 * Enumeration holding branch types.
	 */
	private enum BranchContext {
		FILE_HEADER,
		BEFORE_HEX,
		AFTER_HEX,
		HEX_HEADER,
		HEX_CODE;
	}
	
	/**
	 * Enumeration holding leaf types.
	 */
	private enum LeafContext {
		ATTRIBUTE_NAME,
		ATTRIBUTE_VALUE,
		OTHER;
	}
	
	/**
	 * The version number of the document.
	 */
	private int fileVersion = -1;

	/**
	 * The filename of the UPK file associated with this document.
	 */
	private String upkName = "";

	/**
	 * The GUID of the UPK file associated with this document.
	 */
	private String guid = "";

	/**
	 * The name of the targeted UnrealScript function.
	 */
	private String functionName = "";
	
	/**
	 * The context flag denoting which branch (i.e. line) is currently in the
	 * process of being manipulated.
	 */
	private BranchContext branchContext;
	
	/**
	 * Context flag denoting the type of leaf element is currently in the
	 * process of being manipulated.
	 */
	private LeafContext leafContext;

	/**
	 * Creates a new ModDocument instance.
	 */
	public ModDocument() {
		super();
	}
	
	@Override
	public void insertString(int offs, String str, AttributeSet a)
			throws BadLocationException {
		
		// restore default text attributes
		StyleConstants.setForeground((MutableAttributeSet) a, Color.BLACK);
		StyleConstants.setItalic((MutableAttributeSet) a, false);
		
		// trim line breaks, spaces and tab characters from inserted string
		String line = str.trim();
		// check for keywords
		// TODO: maybe move context detection to separate method
		if (line.startsWith("[BEFORE_HEX]")) {
			this.branchContext = BranchContext.BEFORE_HEX;
		} else if (line.startsWith("[AFTER_HEX]")) {
			this.branchContext = BranchContext.AFTER_HEX;
		} else if (line.startsWith("[HEADER]")) {
			this.branchContext = BranchContext.HEX_HEADER;
		} else if (line.startsWith("[CODE]")) {
			this.branchContext = BranchContext.HEX_CODE;
		} else if (line.startsWith("//")) {
			// TODO: get style data from context enum
			StyleConstants.setForeground((MutableAttributeSet) a, new Color(63, 127, 95));
		}
		
		if (this.leafContext == LeafContext.ATTRIBUTE_VALUE) {
			StyleConstants.setForeground((MutableAttributeSet) a, new Color(0, 0, 192));
			StyleConstants.setItalic((MutableAttributeSet) a, true);
		}
		this.leafContext = LeafContext.OTHER;
		
		// check for comments
		int commentOffset = line.indexOf("//");
		if (commentOffset > 0) {
			// split at '//' if comment is after other content
			String pre = str.substring(0, commentOffset + 1);
			String post = str.substring(commentOffset + 1);
			
			this.insertString(offs, pre, a);
			this.insertString(offs + pre.length(), post, a);
		} else {
			// check for attribute values
			int equalsOffset = str.indexOf("=");
			if ((equalsOffset > 0) && (commentOffset < 0)) {
				// split at '=' if attribute/value pair is outside of comment
				String pre = str.substring(0, equalsOffset);
				String post = str.substring(equalsOffset + 1);
				
				this.leafContext = LeafContext.ATTRIBUTE_NAME;
				this.insertString(offs, pre, a);
				this.leafContext = LeafContext.OTHER;
				this.insertString(offs + pre.length(), "=", a);
				this.leafContext = LeafContext.ATTRIBUTE_VALUE;
				this.insertString(offs + pre.length() + 1, post, a);
			} else {
				// fall-back for default behavior
				super.insertString(offs, str, a);
			}
		}
		
	}
	
	// TODO: fix formatting on remove operations
	
    /**
     * Returns array of root elements.<br>
     * For <code>ModDocument</code> this will always be length 1 array.
     * @return
     */
	@Override
	public Element[] getRootElements() {
		return new Element[] { this.getDefaultRootElement() };
	}
	
	@Override
	protected Element createBranchElement(Element parent, AttributeSet a) {
		// create branch element depending on context
		switch (this.branchContext) {
			case FILE_HEADER:
				return new FileHeaderBranchElement(parent, a);
			case BEFORE_HEX:
				return new BeforeHexBranchElement(parent, a);
			case AFTER_HEX:
				return new AfterHexBranchElement(parent, a);
			case HEX_HEADER:
				return new HexHeaderBranchElement(parent, a);
			case HEX_CODE:
				return new HexCodeBranchElement(parent, a);
			default:
				// fall-back case
				return super.createBranchElement(parent, a);
		}
	}

	@Override
	protected Element createLeafElement(Element parent, AttributeSet a, int p0, int p1) {
		// create leaf element depending on context
		switch (this.leafContext) {
			case ATTRIBUTE_NAME:
				return new AttributeNameLeafElement(parent, a, p0, p1);
			case ATTRIBUTE_VALUE:
				return new AttributeValueLeafElement(parent, a, p0, p1);
			default:
				// fall-back case
				return super.createLeafElement(parent, a, p0, p1);
		}
	}
	
	@Override
	protected AbstractElement createDefaultRoot() {
		// grabs a write-lock for this initialization and abandon it during
		// initialization so in normal operation we can detect an illegitimate
		// attempt to mutate attributes
		this.writeLock();

		this.branchContext = BranchContext.FILE_HEADER;
		this.leafContext = LeafContext.OTHER;

		BranchElement root = new SectionElement();
		BranchElement branch = (BranchElement) this.createBranchElement(root, null);
		branch.replace(0, 0, new Element[] { this.createLeafElement(branch, null, 0, 1) });
		root.replace(0, 0, new Element[] { branch });

		this.writeUnlock();

		return root;
	}
	
	/**
	 * Returns the file version number.
	 * @return the version number
	 */
	public int getFileVersion() {
		return this.fileVersion;
	}

	/**
	 * Sets the file version number
	 * @param fileVersion the version number to set.
	 */
	public void setFileVersion(int fileVersion) {
		this.fileVersion = fileVersion;
	}

	/**
	 * Returns the filename of the UPK file associated with this document.
	 * @return the UPK filename
	 */
	public String getUpkName() {
		return this.upkName;
	}

	/**
	 * Sets the filename of the UPK file associated with this document.
	 * @param upkName the filename to set
	 */
	public void setUpkName(String upkName) {
		this.upkName = upkName;
	}

	/**
	 * Returns the GUID of the UPK file associated with this document.
	 * @return the GUID
	 */
	public String getGuid() {
		return this.guid;
	}

	/**
	 * Sets the GUID of the UPK file associated with this document.
	 * @param guid the GUID to set
	 */
	public void setGuid(String guid) {
		this.guid = guid;
	}

	/**
	 * Returns the name of the targeted UnrealScript function.
	 * @return the function name
	 */
	public String getFunctionName() {
		return this.functionName;
	}

	/**
	 * Sets the name of the targeted UnrealScript function.
	 * @param functionName the function name
	 */
	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}
	
	/**
	 * Generic super-class for all branch elements.
	 * @author XMS
	 */
	private class ModBranchElement extends BranchElement {

		public ModBranchElement(Element parent, AttributeSet attributes) {
			super(parent, attributes);
		}
		
		@Override
		public String toString() {
			return "" + this.getClass().getSimpleName() + "(" + this.getName() + ") "
					+ this.getStartOffset() + "," + this.getEndOffset() + "\n";
		}
		
	}
	
	/**
	 * TODO: API
	 * @author XMS
	 */
	private class FileHeaderBranchElement extends ModBranchElement {

		public FileHeaderBranchElement(Element parent, AttributeSet attributes) {
			super(parent, attributes);
		}
		
	}
	
	/**
	 * TODO: API
	 * @author XMS
	 */
	private class BeforeHexBranchElement extends ModBranchElement {

		public BeforeHexBranchElement(Element parent, AttributeSet attributes) {
			super(parent, attributes);
		}
		
	}
	
	/**
	 * TODO: API
	 * @author XMS
	 */
	private class AfterHexBranchElement extends ModBranchElement {

		public AfterHexBranchElement(Element parent, AttributeSet attributes) {
			super(parent, attributes);
		}
		
	}
	
	/**
	 * TODO: API
	 * @author XMS
	 */
	private class HexHeaderBranchElement extends ModBranchElement {

		public HexHeaderBranchElement(Element parent, AttributeSet attributes) {
			super(parent, attributes);
		}
		
	}
	
	/**
	 * TODO: API
	 * @author XMS
	 */
	private class HexCodeBranchElement extends ModBranchElement {

		public HexCodeBranchElement(Element parent, AttributeSet attributes) {
			super(parent, attributes);
		}
		
	}
	
	/**
	 * Generic super-class for all leaf elements.
	 * @author XMS
	 */
	private class ModLeafElement extends LeafElement {

		public ModLeafElement(Element parent, AttributeSet a,
				int offs0, int offs1) {
			super(parent, a, offs0, offs1);
		}
		
		@Override
		public String toString() {
			 return "" + this.getClass().getSimpleName() + "(" + this.getName() + ") " 
					 + this.getStartOffset() + "," + this.getEndOffset() + "\n";
		}
		
	}

	/**
	 * TODO: API
	 * @author XMS
	 */
	private class AttributeNameLeafElement extends ModLeafElement {

		public AttributeNameLeafElement(Element parent, AttributeSet a,
				int offs0, int offs1) {
			super(parent, a, offs0, offs1);
		}
		
		// TODO: override TreeNode methods to link together attribute value and attribute name elements
		
	}

	/**
	 * TODO: API
	 * @author XMS
	 */
	private class AttributeValueLeafElement extends ModLeafElement {

		public AttributeValueLeafElement(Element parent, AttributeSet a,
				int offs0, int offs1) {
			super(parent, a, offs0, offs1);
		}
		
		// TODO: override TreeNode methods to link together attribute value and attribute name elements
		
	}
	
}
