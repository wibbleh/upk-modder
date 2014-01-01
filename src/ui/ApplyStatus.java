package ui;

import java.awt.Color;
import java.awt.Font;

import javax.swing.Icon;

/**
 * Enumeration holding hex replacement apply states and corresponding GUI constants.
 * @author Amineri, XMS
 */
public enum ApplyStatus {
	/** Status applies recursively to both projects, project folders, and modfiles. */

	/** Precedence 1: Indicates an error when testing/applying. Error status superceded others. 
	 * If one file in a project has an error, then the project inherits ERROR*/
	APPLY_ERROR(new Color(191, 0, 0), Constants.TAB_PANE_FONT_ERROR, "ERROR", Constants.BULLET_ERROR_ICON),

	/** Precedence 2: Mixed status can occur in the following situations: 
	 *  A single file with multiple BEFORE/AFTER blocks has some blocks before and some after (but none are missing entirely)
	 *  A project/folder has some childen with different statuses, or any child with MIXED status */
	MIXED_STATUS(new Color(232, 118, 0), Constants.TAB_PANE_FONT_REVERTED, "Mixed Status", Constants.BULLET_MIXED_ICON),

	/** Indicates a missing UPK file association or unknown status. 
	  *  Also indicates a project / folder in which all files have the UNKNOWN status */
	UNKNOWN(Color.BLACK, Constants.TAB_PANE_FONT_REVERTED, "Unknown", Constants.BULLET_UNKNOWN_ICON),
			
	/** Indicates a mod file with applicable <i>BEFORE</i> blocks. 
	  *  Also indicates a project / folder in which all files have the BEFORE status */
	BEFORE_HEX_PRESENT(Color.BLACK, Constants.TAB_PANE_FONT_REVERTED, "Original Hex", Constants.BULLET_BEFORE_ICON),
	
	/** Indicates a mod file with revertable <i>AFTER</i> blocks. 
	  *  Also indicates a project / folder in which all files have the AFTER status */
	AFTER_HEX_PRESENT(Color.BLACK, Constants.TAB_PANE_FONT_APPLIED, "Hex Applied", Constants.BULLET_AFTER_ICON);

	/**
	 * The foreground color.
	 */
	private Color foreground;
	
	/**
	 * The font.
	 */
	private Font font;
	
	/**
	 * The tooltip text.
	 */
	private String tooltip;
	
	/**
	 * The icon.
	 */
	private Icon icon;

	/**
	 * Constructs an apply status element from the specified foreground
	 * color, font and tooltip text.
	 * @param foreground the foreground color
	 * @param font the font
	 * @param tooltip the tooltip text
	 */
	private ApplyStatus(Color foreground, Font font, String tooltip, Icon icon) {
		this.foreground = foreground;
		this.font = font;
		this.tooltip = tooltip;
		this.icon = icon;
	}

	/**
	 * Returns the foreground color.
	 * @return the foreground color
	 */
	public Color getForeground() {
		return foreground;
	}

	/**
	 * Returns the font.
	 * @return the font
	 */
	public Font getFont() {
		return font;
	}

	/**
	 * Returns the tooltip text.
	 * @return the tooltip text
	 */
	public String getToolTipText() {
		return tooltip;
	}

	/**
	 * Returns the icon.
	 * @return the icon
	 */
	public Icon getIcon() {
		return icon;
	}
	
}
