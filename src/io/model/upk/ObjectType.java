package io.model.upk;

/**
 * Enumeration holding objectlist entry types.
 *  
 * @author XMS
 */
// TODO: review whether this enumeration is actually useful to have ;)
// there are certain general names of things (e.g. "function" that would be useful to know.
// however the "function" is an import table entry so its value changes from upk to upk
public enum ObjectType {
	
	CLASS(0), // "Core:ClassProperty@Core"
	FUNCTION(-387), // "Core:Function@Core"
	INTEGER(9001);  // "Core:IntProperty@Core"
					// "Core:FloatProperty@Core"
					// "Core:BoolProperty@Core" 
	// TODO: add other object types, correct type identifier of INTEGER member
	
	/**
	 * The type identifier.
	 */
	private int identifier;
	
	private ObjectType(int identifier) {
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
	public static ObjectType getType(int identifier) {
		for (ObjectType type : ObjectType.values()) {
			if (type.getIdentifier() == identifier) {
				return type;
			}
		}
		throw new IllegalArgumentException("Unknown type identifier.");
	}
}
