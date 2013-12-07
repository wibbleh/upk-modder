package model.modelement3;

import model.modelement3.ModContextType;
/**
 *
 * @author Amineri
 */


public enum ModContextType
{
	FILEHEADER(0),
        HEADER(1),
        CODE(2),
	VALIDCODE(3),
	BEFOREHEX(4),
        AFTERHEX(5),
        NUMCONTEXTS(6);
	// TODO: add other object types, correct type identifier of INTEGER member
	
	/**
	 * The type identifier.
	 */
	private int context;
	
	private ModContextType(int context) {
		this.context = context;
	}
	
	/**
	 * Returns the type identifier of this object type
	 * @return the type identifier
	 */
	public int getIndex() {
		return this.context;
	}
}
