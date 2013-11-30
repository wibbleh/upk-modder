package io.model.upk;

/**
 * Enumeration holding
 *  
 * @author XMS
 */
public enum ObjectType {
	
	CLASS(0),
	FUNCTION(-387),
	INTEGER(9001);
	// TODO: add other object types, correct type identifier of INTEGER member
	
	/**
	 * The type identifier.
	 */
	private int identifier;
	
	private ObjectType(int identifier) {
		this.identifier = identifier;
	}
	
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
