package ui;

import javax.swing.event.DocumentEvent;
import javax.swing.text.DefaultStyledDocument;

/**
 *
 * @author Amineri
 */


public class ModStyledDocument extends DefaultStyledDocument {
	
	// TODO : override to prevent change updates from being sent to ModTree's
	@Override
	protected void fireChangedUpdate(DocumentEvent e) {
		
	}
	
}
