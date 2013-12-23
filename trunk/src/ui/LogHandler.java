package ui;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

/**
 * Handler implementation for re-directing log messages to a text component.
 * @author XMS
 */
public class LogHandler extends Handler {
	
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