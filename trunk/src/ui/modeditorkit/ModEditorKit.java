package ui.modeditorkit;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Element;
import javax.swing.text.PlainView;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

import model.moddocument3.ModDocument;
import model.modelement3.ModContext;
import model.modelement3.ModElement;

/**
 *
 * @author Amineri
 */


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

//	@Override
//	public String getContentType() {
//		return "text/upkmod";
//	}

	@Override
	public ViewFactory getViewFactory()  {
		return this.factory;
	}

//	@Override
//	public Action[] getActions() {
//	    return TextAction.augmentList(super.getActions(), new Action[] {new MyUnderlineAction()});
//	}
//
//	public static class MyUnderlineAction extends StyledTextAction {
// 
//        public MyUnderlineAction() {
//            super("jagged-underline");
//        }
// 
//		@Override
//        public void actionPerformed(ActionEvent e) {
//            JEditorPane editor = getEditor(e);
//            if (editor != null) {
//                ModDocument doc=(ModDocument)editor.getDocument();
//                int start=editor.getSelectionStart();
//                int end=editor.getSelectionEnd();
//                if (start!=end) {
//                    if (start>end) {
//                        int tmp=start;
//                        start=end;
//                        end=tmp;
//                    }
// 
////                    doc.setJaggedUnderline(start, end);
//                }
//
//            }
//        }
//    }

//	@Override
//	public Caret createCaret() {
//		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//	}

	@Override
	public ModDocument createDefaultDocument() {
		ModDocument mdoc = new ModDocument();
		mdoc.getDefaultRootElement();
		return mdoc;
//		return new ModDocument();
	}

	public void read(InputStream in, ModDocument doc, int pos) throws IOException, BadLocationException {
		ModReader.read(doc, pos, in);
}

	public void write(Writer out, ModDocument doc, int pos, int len) throws IOException, BadLocationException {
        ModWriter.write(doc, pos, len, out);
	}

	public void read(Reader in, ModDocument doc, int pos) throws IOException, BadLocationException {
        BufferedReader br=new BufferedReader(in);
        String s=br.readLine();
        StringBuilder buff=new StringBuilder();
        while (s!=null) {
            buff.append(s);
            s=br.readLine();
        }
 
        ModReader.read(doc, pos, new ByteArrayInputStream(buff.toString().getBytes()));
	}

    static class ModViewFactory implements ViewFactory {
		@Override
        public View create(Element elem) {
			ModElement modElem = (ModElement) elem;

//			String kind = modElem.getName();
//            if (kind != null) {
				// TODO -- implement Mod highlight style names
//                if (kind.equals(AbstractDocument.ContentElementName)) {
//                    return new MyLabelView(elem);
//                } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
//                    return new ParagraphView(elem);
//                } else if (kind.equals(AbstractDocument.SectionElementName)) {
//                    return new BoxView(elem, View.Y_AXIS);
//                } else if (kind.equals(StyleConstants.ComponentElementName)) {
//                    return new ComponentView(elem);
//                } else if (kind.equals(StyleConstants.IconElementName)) {
//                    return new IconView(elem);
//                }
//				return null;
//
//            }
			return new PlainView(modElem);
//            return new ModLabelView(modElem);
        }
    }
}
