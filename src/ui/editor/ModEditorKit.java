package ui.editor;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

/**
 * TODO: API
 * 
 * @author XMS
 */
@SuppressWarnings("serial")
public class ModEditorKit extends StyledEditorKit {
	
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
		super.getViewFactory();
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

        public View create(Element elem) {
            String kind = elem.getName();
            // TODO: use different identifiers to determine what type of view should be created
            if (kind != null) {
                if (kind.equals(AbstractDocument.ContentElementName)) {
                    return new LabelView(elem);
                } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
                    return new ParagraphView(elem) {
                    	/* hack to prevent line wrapping */
                    	@Override
						public void layout(int width, int height) {
							super.layout(Short.MAX_VALUE, height);
						}
                    	@Override
						public float getMinimumSpan(int axis) {
							return super.getPreferredSpan(axis);
						}
                    };
                } else if (kind.equals(AbstractDocument.SectionElementName)) {
                    return new BoxView(elem, View.Y_AXIS);
                } else if (kind.equals(StyleConstants.ComponentElementName)) {
                    return new ComponentView(elem);
                } else if (kind.equals(StyleConstants.IconElementName)) {
                    return new IconView(elem);
                }
            }

            // default to text display
            return new LabelView(elem);
        }

    }
	
}
