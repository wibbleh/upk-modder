package model.modelement3;

import java.util.HashMap;
import java.util.Map;

/**
 * Class for storing mod file context-specific flags.
 * @author Amineri, XMS
 */
// TODO: possibly rename class to avoid confusing it with a StyleContext implementation
public class ModContext {

	public enum ModContextType {
		FILE_HEADER,
		HEX_HEADER,
		HEX_CODE,
		VALID_CODE,
		BEFORE_HEX,
		AFTER_HEX;
		// TODO: add other object types, correct type identifier of INTEGER member
	}
	
	/**
	 * The map holding all context flags.
	 */
	private Map<ModContextType, Boolean> contextFlags;
	
	/**
	 * Creates a mod context instance with all flags initially set to <code>false</code>.
	 */
	public ModContext() {
		this.contextFlags = new HashMap<>();
		for (ModContextType type : ModContextType.values()) {
			contextFlags.put(type, false);
		}
	}
	
	/**
	 * Returns the context flag of the specified type.
	 * @param type the context flag type
	 * @return the context flag value
	 */
	public boolean getContextFlag(ModContextType type) {
		return this.contextFlags.get(type);
	}

	/**
	 * Sets the context flag of the specified type to the specified value.
	 * @param type the context flag type
	 * @param value the context flag value
	 */
	public void setContextFlag(ModContextType type, boolean value) {
		this.contextFlags.put(type, value);
	}
	
}
