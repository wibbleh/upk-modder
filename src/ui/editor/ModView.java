package ui.editor;

import javax.swing.text.Element;
import javax.swing.text.PlainView;

/**
 * TODO: API
 * 
 * @author XMS
 */
public class ModView extends PlainView {

	/**
	 * The style context instance.
	 */
	private ModContext context;

	/**
	 * TODO: API
	 * @param context 
	 * @param elem
	 */
	public ModView(ModContext context, Element elem) {
		super(elem);
		this.context = context;
	}
	
}
