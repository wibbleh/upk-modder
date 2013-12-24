package ui;

import java.awt.Shape;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.TabSet;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import static ui.Constants.TAB_SIZE;

/**
 * Current implementation fixes Java 7 bug with word wrapping.
 * Source : http://stackoverflow.com/questions/11000220/strange-text-wrapping-with-styled-text-in-jtextpane-with-java-7
 * @author Amineri
 */


class ModStyledEditorKit extends StyledEditorKit {
    private MyFactory factory;

	@Override
    public ViewFactory getViewFactory() {
        if (factory == null) {
            factory = new MyFactory();
        }
        return factory;
    }
}

class MyFactory implements ViewFactory {
	@Override
    public View create(Element elem) {
        String kind = elem.getName();
        if (kind != null) {
            if (kind.equals(AbstractDocument.ContentElementName)) {
                return new MyLabelView(elem);
            } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
                return new MyParagraphView(elem);
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

class MyParagraphView extends ParagraphView {

	public MyParagraphView(Element elem) {
		super(elem);
	}

	@Override
	public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
		super.removeUpdate(e, a, f);
		resetBreakSpots();
	}

	@Override
	public void insertUpdate(DocumentEvent e, Shape a, ViewFactory f) {
		super.insertUpdate(e, a, f);
		resetBreakSpots();
	}

	private void resetBreakSpots() {
		for(int i = 0; i < layoutPool.getViewCount(); i ++) {
			View v = layoutPool.getView(i);
			if(v instanceof MyLabelView) {
				((MyLabelView) v).resetBreakSpots();
			}
		}
	}
	
	/* hack to prevent line wrapping */
	@Override
	public void layout(int width, int height) {
		super.layout(Short.MAX_VALUE, height);
	}
	@Override
	public float getMinimumSpan(int axis) {
		return super.getPreferredSpan(axis);
	}

	// tab-stop code from http://java-sl.com/tip_default_tabstop_size.html
	@Override
	public float nextTabStop(float x, int tabOffset) {
		TabSet tabs = getTabSet();
		if(tabs == null) {
			// a tab every 72 pixels.
			return (float)(getTabBase() + (((int)x / TAB_SIZE + 1) * TAB_SIZE));
		}

		return super.nextTabStop(x, tabOffset);
	 }

}

class MyLabelView extends LabelView {

    boolean isResetBreakSpots=false;

    public MyLabelView(Element elem) {
        super(elem);
    }
	@Override
    public View breakView(int axis, int p0, float pos, float len) {
        if (axis == View.X_AXIS) {
            resetBreakSpots();
        }
        return super.breakView(axis, p0, pos, len);
    }

    public void resetBreakSpots() {
        isResetBreakSpots=true;
        removeUpdate(null, null, null);
        isResetBreakSpots=false;
   }

	@Override
    public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        super.removeUpdate(e, a, f);
    }

	@Override
    public void preferenceChanged(View child, boolean width, boolean height) {
        if (!isResetBreakSpots) {
            super.preferenceChanged(child, width, height);
        }
    }
}