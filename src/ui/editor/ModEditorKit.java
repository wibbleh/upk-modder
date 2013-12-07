package ui.editor;

import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

/**
 * TODO: API
 * 
 * @author XMS
 */
@SuppressWarnings("serial")
public class ModEditorKit extends DefaultEditorKit {
	
	/**
	 * The view factory instance.
	 */
	private ModViewFactory factory;
	
	/**
	 * The style context instance.
	 */
	private ModContext context;

	/**
	 * TODO: API
	 */
	public ModEditorKit() {
		super();
		this.factory = new ModViewFactory();
		this.context = new ModContext();
	}
	
	@Override
	public ViewFactory getViewFactory() {
		return this.factory;
	}
	
	@Override
	public Document createDefaultDocument() {
		return new ModDocument();
	}
	
	/**
	 * TODO: API
	 * 
	 * @author XMS
	 */
	private class ModViewFactory implements ViewFactory {

		@Override
		public View create(Element elem) {
			return new ModView(context, elem);
		}
		
	}
	
}
