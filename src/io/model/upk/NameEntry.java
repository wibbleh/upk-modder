package io.model.upk;

/**
 * Model class for a namelist entry.
 * 
 * @author XMS
 */
// TODO: do namelist entries feature additional properties? if not, a simple List<String> will suffice for the name list, no need for this class
// Amineri: there are technically 8 bytes of flags following the string but they are identical for every name in every upk I've examined
//			currently the parser just skips past those 8 bytes, so changing to List<String> should be okay
public class NameEntry{
	
	/**
	 * The name string of this entry.
	 */
	private String name;
	
	/**
	 * Constructs a namelist entry from the specified name string.
	 * @param name the name
	 */
	public NameEntry(String name) {
		this.name = name;
	}
	
	/**
	 * Returns the name of this entry.
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

}
