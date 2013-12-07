package ui.editor;

import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;

/**
 * Document class for the mod file format.
 * @author Amineri, XMS
 */
@SuppressWarnings("serial")
public class ModDocument extends PlainDocument {
	
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
	 * Creates a new ModDocument instance.
	 */
	public ModDocument() {
		super();
	}
	
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
		return new ModBranchElement(parent, a);
	}
	
	@Override
	protected Element createLeafElement(Element parent, AttributeSet a, int p0, int p1) {
        return new ModLeafElement(parent, a, p0, p1);
	}
	
	@Override
	protected void insertUpdate(DefaultDocumentEvent chng, AttributeSet attr) {
		// TODO: here would be a good place to refresh the document hierarchy, default implementation simply maps lines to leaves
		super.insertUpdate(chng, attr);
	}
	
	@Override
	protected void removeUpdate(DefaultDocumentEvent chng) {
		// TODO: here would be a good place to refresh the document hierarchy
		super.removeUpdate(chng);
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
	 * TODO: API
	 * 
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
	 * 
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
	
}
