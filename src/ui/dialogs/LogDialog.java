package ui.dialogs;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import model.modtree.ModTree;

import org.bounce.text.LineNumberMargin;

import ui.Constants;
import ui.ModFileTabbedPane;
import ui.frames.MainFrame;
import ui.trees.ProjectTreeModel;
import util.unrealhex.ReferenceUpdate;

/**
 * Dialog implementation for the application's message log.
 * @author XMS
 */
@SuppressWarnings("serial")
public class LogDialog extends JDialog {
	
	/**
	 * Constructs a message 
	 * @param owner
	 */
	public LogDialog(Frame owner) {
		super(owner, "Message Log");
		
		this.setIconImage(((ImageIcon) Constants.HEX_SMALL_ICON).getImage());
		
		this.initComponents();
		
		this.pack();
		this.setMinimumSize(this.getSize());
		this.setLocationRelativeTo(owner);
	}
	
	/**
	 * Creates and lays out the 
	 */
	private void initComponents() {
		Container loggingCont = this.getContentPane();
		
		// create editor pane for storing log messages
		JEditorPane loggingEditor = new JEditorPane();
		loggingEditor.setDocument(new DefaultStyledDocument());
		loggingEditor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
		loggingEditor.setEditable(false);
		
		loggingEditor.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent evt) {
				try {
					// update status message text to show added line
					MainFrame.getInstance().setStatusMessage(
							evt.getDocument().getText(evt.getOffset(), evt.getLength()));
				} catch (BadLocationException e) {
					System.err.println("Error when writing to logger");
				}
			}
			
			@Override
			public void removeUpdate(DocumentEvent evt) { }
			@Override
			public void changedUpdate(DocumentEvent evt) { }
		});
		
		// install log handler on various loggers
		Handler logHandler = new LogHandler(loggingEditor);
		ModTree.logger.addHandler(logHandler);
		ModFileTabbedPane.logger.addHandler(logHandler);
		ReferenceUpdate.logger.addHandler(logHandler);
		ProjectTreeModel.logger.addHandler(logHandler);
		
		
		JScrollPane loggingScpn = new JScrollPane(loggingEditor,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		loggingScpn.setPreferredSize(new Dimension(480, 300));
		loggingScpn.setRowHeaderView(new LineNumberMargin(loggingEditor));
		
		loggingCont.add(loggingScpn);
	}
	
	/**
	 * Handler implementation for re-directing log messages to a text component.
	 * @author XMS
	 */
	private class LogHandler extends Handler {
		
		/**
		 * The document to append log messages to.
		 */
		private Document document;

		/**
		 * Creates a logging handler which feeds log messages from the specified
		 * loggers into the specified text component.
		 * @param txtComp the text component to receive log messages
		 */
		public LogHandler(JTextComponent txtComp) {
			this.document = txtComp.getDocument();
			this.setLevel(Level.ALL);
			this.setFormatter(new LogFormatter());
		}

		@Override
		public void publish(LogRecord record) {
			// format record
			String str = this.getFormatter().format(record) + "\n";
			
			// TODO: implement different styles for records depending on log level or attached throwables
			try {
				int offset = this.document.getLength();
				this.document.insertString(offset, str, null);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void flush() {
			// do nothing, don't need
		}

		@Override
		public void close() throws SecurityException {
			// do nothing, don't need
		}
		
		private class LogFormatter extends Formatter {

			@Override
			public String format(LogRecord record) {
				return String.format("[%1$tH:%1$tM:%1$tS] [%2$s] %3$s", new Date(record.getMillis()), record.getLevel(), record.getMessage());
			}
			
		}
		
	}
	
}
