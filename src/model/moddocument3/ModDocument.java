package model.moddocument3;

import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.PlainDocument;
import javax.swing.text.Segment;

import model.modelement3.ModElement;
import model.modelement3.ModRootElement;

/**
 * Document class for the mod file format.
 * @author Amineri, XMS
 */
@SuppressWarnings("serial")
public class ModDocument extends PlainDocument {
	
	/**
	 * The document's root element.
	 */
    private ModRootElement rootElement;
    
    /**
	 * The array of the document's root elements. Always contains only a single
	 * element.
	 */
	private final ModRootElement[] rootElements = new ModRootElement[1];

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
	 * Refreshes contexts and re-parses tokens after an insert operation.<br>
	 * <i>Parameters <code>chng</code> and <code>attr</code> currently unused.</i>
	 * @param chng the change event describing the edit
	 * @param attr the set of attributes for the inserted text
	 */
	@Override
	public void insertUpdate(AbstractDocument.DefaultDocumentEvent chng, AttributeSet attr) {
		if (this.rootElement != null) {
			this.rootElement.reorganizeAfterInsertion();
		}
	}

    /**
     * Refreshes contexts and re-parses tokens after a remove operation.<br>
     * <i>Parameter <code>chng</code> currently unused.</i>
     * @param chng the change event describing the edit
     */
    @Override
	public void removeUpdate(AbstractDocument.DefaultDocumentEvent chng) {
		if (this.rootElement != null) {
			this.rootElement.reorganizeAfterDeletion();
		}
	}

    /**
     * Returns the length of the document measured in characters.
     * @return the document length
     */
	@Override
	public int getLength() {
		return (this.rootElement == null) ? 0 : this.rootElement.getEndOffset();
    }

	/**
	 * Removes <code>length</code> characters starting at position
	 * <code>offset</code>.
	 * @param offset the starting offset measured from the beginning of the document
	 * @param length the number of characters to remove
	 */
	@Override
	public void remove(int offset, int length) {
		if (this.rootElement == null) {
			return;
		}
		this.rootElement.remove(offset, length);
	}

    /**
	 * Inserts <code>string at </code>position</code> offset.<br>
	 * <i>Parameter <code>as</code> currently is unused.</i><p>
	 * Requires
	 * <code>{@link ModDocument#insertUpdate(DefaultDocumentEvent, AttributeSet) insertUpdate}</code>
	 * call to reset contexts and parse Unreal Engine bytecode.
	 * @param offset the starting measured from the beginning of the document
	 * @param string the string to insert; does nothing with null/empty strings
	 * @param as the attributes for the inserted content (currently unused)
	 * @see
	 */
	@Override
	public void insertString(int offset, String string, AttributeSet as) {
		if (this.rootElement == null) {
			return;
		}
		this.rootElement.insertString(offset, string, as);
	}

    /**
	 * Retrieves a sequence of text of the specified <code>length</code> from
	 * the document starting at position <code>offset</offset>.
	 * @param offset the starting offset
	 * @param length the number of characters to retrieve
	 * @return the text
	 */
	@Override
	public String getText(int offset, int length) {
		if (this.rootElement == null) {
			return "";
		}
		return this.rootElement.getText(offset, length);
	}

    /**
     * Retrieves a sequence of text of the specified <code>length</code> from
	 * the document starting at position <code>offset</offset>.<br>
     * Result is returned in parameter <code>segment</code>.
     * @param offset the starting offset
     * @param length the number of characters to retrieve
     * @param segment the <code>Segment</code> object to retrieve the text into
     */
	@Override
	public void getText(int offset, int length, Segment segment) {
		if (this.rootElement == null) {
			return;
		}
		this.rootElement.getText(offset, length, segment);
	}

    /**
     * Returns array of root elements.<br>
     * For <code>ModDocument</code> this will always be length 1 array.
     * @return
     */
	@Override
	public ModElement[] getRootElements() {
		if (this.rootElements[0] == null) {
			// lazily instantiate root element
			this.getDefaultRootElement();
		}
		return this.rootElements;
	}

	/**
	 * Returns the root element of the document.
	 * @return the root element
	 */
	@Override
	public ModElement getDefaultRootElement() {
		if (this.rootElement == null) {
			// lazily instantiate root element
			this.rootElement = new ModRootElement(this);
			this.rootElements[0] = this.rootElement;
		}
		return this.rootElement;
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

}
