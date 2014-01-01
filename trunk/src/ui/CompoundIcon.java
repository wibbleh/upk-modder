package ui;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

/**
 * Icon class for overlaying multiple icons.
 * @author XMS
 */
public class CompoundIcon implements Icon {
	
	/** The array of delegate icons to paint. */
	private Icon[] delegates;
	/** The compound icon width. */
	private int iconWidth = 0;
	/** The compound icon height. */
	private int iconHeight = 0;

	/**
	 * Creates a compound icon from the specified delegate icons. Icons are
	 * painted in the order they are specified.
	 * @param delegates the delegate icons to paint.
	 */
	public CompoundIcon(Icon... delegates) {
		this.delegates = delegates;
		// find maximum width and height
		for (Icon delegate : delegates) {
			if (delegate != null) {
				iconWidth = Math.max(iconWidth, delegate.getIconWidth());
				iconHeight = Math.max(iconHeight, delegate.getIconHeight());
			}
		}
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		for (Icon delegate : delegates) {
			if (delegate != null) {
				delegate.paintIcon(c, g, x, y);
			}
		}
	}

	@Override
	public int getIconWidth() {
		return iconWidth;
	}

	@Override
	public int getIconHeight() {
		return iconHeight;
	}
	
}