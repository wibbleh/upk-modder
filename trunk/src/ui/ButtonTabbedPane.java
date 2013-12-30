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

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;

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
	public ButtonTabComponent getTabComponentAt(int index) {
		return (ButtonTabComponent) super.getTabComponentAt(index);
	}
	
	@Override
	public void insertTab(String title, Icon icon, Component component,
			String tip, int index) {
		super.insertTab(title, icon, component, tip, index);
		
		// add 'Close' button to new tab
		ButtonTabComponent buttonTabComponent = new ButtonTabComponent(this);
		this.setTabComponentAt(index, buttonTabComponent);
	}
	
	@Override
	public void setForegroundAt(int index, Color color) {
		this.getTabComponentAt(index).getLabel().setForeground(color);
	}
	
	@Override
	public void setIconAt(int index, Icon icon) {
		this.getTabComponentAt(index).getLabel().setIcon(icon);
	}

	/**
	 * Sets the font at <code>index</code> to <code>font</code> which can be
	 * <code>null</code>, in which case the tab's font will default to the font
	 * of this <code>tabbedpane</code>.
	 * @param index the tab index where the foreground should be set
	 * @param font the font to be used in the tab
	 */
	public void setFontAt(int index, Font font) {
		this.getTabComponentAt(index).getLabel().setFont(font);
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
	        super(new BorderLayout());
	        
	        this.tabPane = tabPane;
	        
	        this.initComponents();
		}
	    
	    /**
	     * Creates and lays out this component's sub-components.
	     */
	    private void initComponents() {
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
	     * Returns the label of this button tab.
	     * @return the button tab label
	     */
	    public JLabel getLabel() {
	    	return (JLabel) this.getComponent(0);
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
