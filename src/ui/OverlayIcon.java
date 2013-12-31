package ui;

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Class to support overlaying of status icons.
 * Based on code from : https://www.java.net//node/678566
 * @author Amineri
 */

// @XMTS I'm not sure if there is a better way to implement this so that the Nimbus icons can be retained
// I'm unable to convert the Nimbus icons to ImageIcons to use this overlay example I found
public class OverlayIcon extends ImageIcon
{

	private Icon base;
	private Icon overlay;

	/**
	 * Default constructor that creates a non-overlaid base icon
	 * @param base the base icon
	 */
	public OverlayIcon(Icon base) {
		super(((ImageIcon) base).getImage());
		
		this.base = base;
		this.overlay = null;
	}
	
	/**
	 * Constructor that creates a composite icon of base + overlay
	 * @param base the base icon
	 * @param overlay the overlay icon
	 */
	public OverlayIcon(Icon base, Icon overlay) {
		this(base);
		this.overlay = overlay;
	}

	/**
	 * Sets the overlay icon to the supplied icon
	 * @param overlay the new overlay icon
	 */
	public void setOverlay(ImageIcon overlay) {
		this.overlay = overlay;
	}

	/**
	 * Remove the overlay icon 
	 */
	public void removeOverlay() {
		this.overlay = null;
	}

	/**
	 * Overridden method to paint a second icon in the lower left corner.
	 * The overlay icon is assumed to be 10x10 pixels and the base 16x16.
	 * @param c
	 * @param g
	 * @param x
	 * @param y
	 */
	@Override
	public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
		base.paintIcon(c, g, x, y);
		if(overlay != null) {
			overlay.paintIcon(c, g, x, y+7);
		}
	}
}
