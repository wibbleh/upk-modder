package io.model.upk;

import java.util.List;

/**
 * Model class for an objectlist entry.
 * 
 * @author XMS
 */
public class ObjectEntry {

	/**
	 * The object type of this entry.
	 */
	private ObjectType type;

	public ObjectEntry(List<Integer> data) {
		this.parseData(data);
	}

	/**
	 * Parses a list of integers into their corresponding object entry properties.
	 * @param data the list of object entry property identifiers
	 */
	private void parseData(List<Integer> data) {
		this.type = ObjectType.getType(data.get(0));
		// TODO: parse/store other values, create getters and other convenience methods (e.g. getNameListIndex(), etc.)
	}
	
	/**
	 * Returns the object type of this entry.
	 * @return the object type
	 */
	public ObjectType getType() {
		return this.type;
	}
	
//	word 0 - 0 for class objects, negative value defining type for variables and functions -- functions have value -387 = 7D FE FF FF
//	word 1 - 0 for variables, reference to parent class for classes
//	word 2 - reference (objectlist index) to owner -- 0 if no owner
//	word 3 - index into namelist table from step 1 -- name of variable
//	words 4, 5 - appear to always be 0
//	word 6 - property flags
//	word 7 - 04 00 07 00 
//	word 8 - file size of object referenced to in word 10
//	word 9 - file pointer to function in upk (location of header/script for functions) -- for variable points to variable buffer 
//	word 10 -- appears to always be 0 for script objects
//	word 11 - 0 for script objects, number of additional words (beyond 16) for others
//	words 12-17 - appear to always be 0 for script objects -- used for art asset content
//	words ## - additional words depending on value of word 11

}
