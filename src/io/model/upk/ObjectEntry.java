package io.model.upk;

/**
 * Model class for an objectlist entry.
 * 
 * @author XMS, Amineri
 */
public class ObjectEntry {

	/**
	 * The position of this ObjectEntry element within the upk.
	 */
	private int iObjectEntryPos;
	
	/**
	 * The object type of this entry. Reference to an ImportEntry.
	 */
	private int iType;

	/**
	 * The outer (or owner) Object. Is reference to an ObjectEntry.
	 */
	private int iOuter;

	/**
	 * The name of this entry. Is reference to a NameEntry.
	 */
	private int iNamePtr;

	/**
	 * The file position of the Entry. Measured in bytes.
	 */
	private int iUpkPos;

	/**
	 * The file size of the Entry. Measured in bytes
	 */
	private int iUpkSize;

	/**
	 * The contextualized name of the Entry. String.
	 */
	private String sName;

	/**
	 * Constructs an objectlist entry from the specified property identifier array.
	 * @param data the array of objectlist entry property identifiers
	 * @param filePosition the file position of the entry within the upkfile
	 */
	public ObjectEntry(int[] data, int filePosition) {
		this.parseData(data);
		this.iObjectEntryPos = filePosition;
	}

	/**
	 * Parses a list of integers into their corresponding object entry properties.
	 * @param data the array of object entry property identifiers
	 */
	private void parseData(int[] data) {
		this.iType = data[0];
        this.iOuter = data[2];
        this.iNamePtr = data[3];
        this.iUpkSize = data[8];
        this.iUpkPos = data[9];
		// TODO: parse/store other values, create getters and other convenience methods (e.g. getNameListIndex(), etc.)
	}

    /**
     * Sets the Entry's name
     * @param name
     */
	public void setName(String name) {
		this.sName = name;
	}

	/**
	 * Returns name of entry
	 * @return String
	 */
	public String getName() {
		return this.sName;
	}

	/**
	 * Returns name of entry as index to namelist
	 * @return int
	 */
	public int getNameIdx() {
		return this.iNamePtr;
	}

	/**
	 * Returns outer (or owner) of entry as index to objectlist
	 * @return int
	 */
	public int getOuterIdx() {
		return this.iOuter;
	}

	/**
	 * Returns upk file position of object entry describes
	 * @return int
	 */
	public int getUpkPos() {
		return this.iUpkPos;
	}

	/**
	 * Sets the local cached copy of the upk file position the object entry describes 
	 * WARNING -- this does not update the file itself
	 * @param pos the new position
	 */
	public void setUpkPos(int pos) {
		this.iUpkPos = pos;
	}
	
	/**
	 * Returns upk file size of object entry describes
	 * @return int
	 */
	public int getUpkSize() {
		return this.iUpkSize;
	}
	
	/**
	 * Sets the local cached copy of the upk file size the object entry describes
	 * WARNING -- this does not update the file itself
	 * @param size the new size
	 */
	public void setUpkSize(int size) {
		this.iUpkSize = size;
	}
	
	/**
	 * Returns the object type of this entry.
	 * @return the object type
	 */
	public int getType() {
		return this.iType;
	}
	
	
	/**
	 * Sets the local cached copy of the object type
	 * WARNING -- this does not update the file itself
	 * @param type the new type
	 */
	public void setType(int type) {
		this.iType = type;
	}
	
	/**
	 * Sets the position of the ObjectEntry object within the upk.
	 * @return the file position of the object entry itself
	 */
	public int getObjectEntryPos() {
		return this.iObjectEntryPos;
	}
//	word 0 - 0 for class objects, negative value defining type for variables and functions -- value is index in import table
//	word 1 - 0 for variables, reference to parent class for classes
//	word 2 - reference (objectlist index) to owner -- 0 if no owner
//	word 3 - index into namelist table from step 1 -- name of variable
//	words 4, 5 - appear to always be 0
//	word 6 - property flags
//	word 7 - 04 00 07 00 
//	word 8 - file size of object referenced to in word 9
//	word 9 - file pointer to function in upk (location of header/script for functions) -- for variable points to variable buffer 
//	word 10 -- appears to always be 0 for script objects
//	word 11 - 0 for script objects, number of additional words (beyond 16) for others
//	words 12-17 - appear to always be 0 for script objects -- used for art asset content
//	words ## - additional words depending on value of word 11

}
