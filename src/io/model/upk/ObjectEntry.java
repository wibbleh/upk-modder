package io.model.upk;

import java.util.List;

/**
 * Model class for an objectlist entry.
 * 
 * @author XMS
 */
public class ObjectEntry {

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

        public ObjectEntry(List<Integer> data) {
		this.parseData(data);
	}

	/**
	 * Parses a list of integers into their corresponding object entry properties.
	 * @param data the list of object entry property identifiers
	 */
	private void parseData(List<Integer> data) {
		this.iType = data.get(0);
                this.iOuter = data.get(2);
                this.iNamePtr = data.get(3);
                this.iUpkPos = data.get(9);
                this.iUpkSize = data.get(8);
		// TODO: parse/store other values, create getters and other convenience methods (e.g. getNameListIndex(), etc.)
	}

        /**
         * Sets the Entry's name
         * @param name
         */
        public void setName(String name)
        {
            this.sName = name;
        }

        /**
         * Returns name of entry
         * @return String
         */
        public String getName()
        {
            return this.sName;
        }

        /**
         * Returns name of entry as index to namelist
         * @return int
         */
        public int getNameIdx()
        {
            return this.iNamePtr;
        }

        /**
         * Returns outer (or owner) of entry as index to objectlist
         * @return int
         */
        public int getOuterIdx()
        {
            return this.iOuter;
        }
        
        /**
         * Returns upk file position of object entry describes
         * @return int
         */
        public int getUpkPos()
        {
            return this.iUpkPos;
        }
        
        /**
         * Returns upk file size of object entry describes
         * @return int
         */
        public int getUpkSize()
        {
            return this.iUpkSize;
        }
	
	/**
	 * Returns the object type of this entry.
	 * @return the object type
	 */
	public int getType() {
		return this.iType;
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
