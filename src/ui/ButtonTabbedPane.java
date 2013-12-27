package ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import static ui.Constants.*;

/**
 * Custom tabbed pane featuring a 'Close' button in its tabs.
 * @author XMS
 */
@SuppressWarnings("serial")
public class ButtonTabbedPane extends JTabbedPane {
	
	// TODO: focus traversal on tabs is dodgy, investigate

	/**
	 * Creates a tabbed pane featuring a 'Close' button in its tabs.
	 * @param tabPlacement the placement for the tabs relative to the content
	 * @param tabLayoutPolicy the policy for laying out tabs when all tabs will not fit on one run
	 */
	public ButtonTabbedPane(int tabPlacement, int tabLayoutPolicy) {
		super(tabPlacement, tabLayoutPolicy);
	}
	
	@Override
	public void addTab(String title, Component component) {
		super.addTab(title, component);
		// add 'Close' button to new tab
		ButtonTabComponent buttonTabComponent = new ButtonTabComponent(this);
		this.setTabComponentAt(this.getTabCount() - 1, buttonTabComponent);
		buttonTabComponent.getComponent(0).setFont(TAB_PANE_FONT_UNKNOWN);
	}
	
	@Override
	public void setForegroundAt(int index, Color color) {
		ButtonTabComponent comp = (ButtonTabComponent) this.getTabComponentAt(index);
		comp.getComponent(0).setForeground(color);
	}
	
	public void setFontAt(int index, Font font) {
		ButtonTabComponent comp = (ButtonTabComponent) this.getTabComponentAt(index);
		comp.getComponent(0).setFont(font);
	}
	
	/**
	 * Component to be used inside tabbed pane tabs.<br>
	 * Based on a <a href="http://docs.oracle.com/javase/tutorial/uiswing/examples/components/TabComponentsDemoProject/src/components/ButtonTabComponent.java">Java Tutorials Code Sample</a>
	 * @author XMS
	 */
	private class ButtonTabComponent extends JPanel {
		
		/**
		 * The reference to the parent tabbed pane.
		 */
		private JTabbedPane tabPane;
	 
	    /**
	     * Creates a panel containing the tab title and a 'Close' button
	     * @param tabPane the reference to the parent tabbed pane
	     */
	    public ButtonTabComponent(final JTabbedPane tabPane) {
	        super(new BorderLayout(0, 0));
	        
	        this.tabPane = tabPane;

			// make component transparent
			this.setOpaque(false);
			
			// create label, make it display its corresponding tab title as text
			JLabel label = new JLabel() {
				@Override
				public String getText() {
					int index = tabPane.indexOfTabComponent(
							ButtonTabComponent.this);
					if (index != -1) {
						return tabPane.getTitleAt(index);
					}
					return null;
				}
			};

			this.add(label, BorderLayout.CENTER);
			TabButton tabButton = new TabButton();
			
			JToolBar tb = new JToolBar();
			tb.add(tabButton);
			
			this.add(tabButton, BorderLayout.EAST);
		}

	    /**
	     * 'Close' button for tabs.
	     * @author XMS
	     */
		private class TabButton extends JButton implements ActionListener {
			
			/**
			 * Constructs a 'Close' button.
			 */
			public TabButton() {
				int size = 16;
				this.setPreferredSize(new Dimension(size, size));

				// configure visuals
				this.setToolTipText("Close this document");
				this.setFocusable(false);
				
				// install action listener to close tab on click
				this.addActionListener(this);
			}

			@Override
			public void actionPerformed(ActionEvent evt) {
				int i = tabPane.indexOfTabComponent(ButtonTabComponent.this);
				if (i != -1) {
					tabPane.remove(i);
				}
			}

			// paint the cross
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setStroke(new BasicStroke(2));
				g2.setColor(Color.DARK_GRAY);
				int delta = 6;
				g2.drawLine(delta, delta,
						getWidth() - delta, getHeight() - delta);
				g2.drawLine(getWidth() - delta, delta,
						delta, getHeight() - delta);
				g2.dispose();
			}
			
		}
		
	}

}
