package io.model.upk;

import java.util.List;

/**
 * Enumeration holding
 *
 * @author Amineri
 */
public class ImportEntry{
	/**
	 * The package of this entry.
	 */
	private int iPackage;

	/**
	 * The class of this entry.
	 */
        private int iClass;

        /**
	 * The outer/owner type of this entry.
	 */
        private int iOuter;

        /**
	 * The name type of this entry.
	 */
	private int iName;

	/**
	 * The contextualized name of the Entry. String.
	 */
        private String sName;

        public ImportEntry(List<Integer> data) {
		this.parseData(data);
	}

	/**
	 * Parses a list of integers into their corresponding object entry properties.
	 * @param data the list of object entry property identifiers
	 */
	private void parseData(List<Integer> data) {
		this.iPackage = data.get(0);
                this.iClass = data.get(2);
                this.iOuter = data.get(4);
                this.iName = data.get(5);
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
	 * Returns the package of this entry.
	 * @return integer index to namelist
	 */
	public int getPackageIdx() 
        {
            return this.iPackage;
	}
        
	/**
	 * Returns the class of this entry.
	 * @return integer index to namelist
	 */
	public int getClassIdx() 
        {
            return this.iClass;
	}
        
	/**
	 * Returns the outer/owner of this entry.
	 * @return integer index to importlist
	 */
	public int getOuterIdx() 
        {
            return this.iOuter;
	}
        
	/**
	 * Returns the name of this entry.
	 * @return integer index to namelist
	 */
	public int getNameIdx() 
        {
            return this.iName;
	}
        
//    word 0 -- package, index to namelist
//    word 1 -- always 0 for unrealscript entries
//    word 2 -- class, index to namelist
//    word 3 -- always 0 for unrealscript entries 
//    word 4 -- outer/owner, index to importlist
//    word 5 -- name, index to namelist
//    word 6 -- always 0 for unrealscript entries

}
