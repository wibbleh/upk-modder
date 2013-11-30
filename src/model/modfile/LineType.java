package model.modfile;

/**
 * Enumerates lines needing special attention when resolving jump offsets
 * @author Amineri
 */


public enum LineType
{
        ELSE_JUMP(0),
        WHILE_IF(1),
        WHILE_JUMP(2),
        CONTINUE_JUMP(3),
        BREAK_JUMP(4);
        
    	/**
	 * The type identifier.
	 */
	private int identifier;
        
	private LineType(int identifier) {
		this.identifier = identifier;
	}

        /**
	 * Returns the type identifier of this object type
	 * @return the type identifier
	 */
	public int getIdentifier() {
		return this.identifier;
	}
	
	/**
	 * Returns the object type that corresponds to the specified type identifier
	 * @param identifier the type identifier
	 * @return the object type
	 */
	public static LineType getType(int identifier) {
		for (LineType type : LineType.values()) {
			if (type.getIdentifier() == identifier) {
				return type;
			}
		}
		throw new IllegalArgumentException("Unknown type identifier.");
	}

}
