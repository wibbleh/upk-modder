package ui.modeditorkit;
import java.io.IOException;
import java.io.Writer;

import javax.swing.text.BadLocationException;

import model.moddocument3.ModDocument;
import model.modelement3.ModElement;
import model.modelement3.ModRootElement;

/**
 *
 * @author Amineri
 */


public class ModWriter {
 
    public static void write(ModDocument doc, int start, int len, Writer out) throws IOException, BadLocationException {
        out.write("<>");
        ModRootElement root= (ModRootElement) doc.getDefaultRootElement();
        int iStart=root.getElementIndex(start);
        int iEnd=root.getElementIndex(start+len);
 
        for (int i=iStart; i<=iEnd; i++) {
            ModElement par=root.getElement(i);
            writeElement(par, start, len, out);
        }
//        out.write("</"+ModDocument.TAG_NAME_DOCUMENT+">");
    }
 
    public static void writeElement(ModElement el, int start, int len, Writer out) throws IOException, BadLocationException {
//        out.write("<" +ModDocument.ATTR_NAME_ALIGN+"=\""+align+"\"");
//        float first=StyleConstants.getFirstLineIndent(par.getAttributes());
//        out.write(" "+ModDocument.ATTR_NAME_FIRST+"=\""+first+"\"");
//        float above=StyleConstants.getSpaceAbove(par.getAttributes());
//        out.write(" "+ModDocument.ATTR_NAME_ABOWE+"=\""+above+"\"");
//        float below=StyleConstants.getSpaceBelow(par.getAttributes());
//        out.write(" "+ModDocument.ATTR_NAME_BELOW+"=\""+below+"\"");
//        float left=StyleConstants.getLeftIndent(par.getAttributes());
//        out.write(" "+ModDocument.ATTR_NAME_LEFT+"=\""+left+"\"");
//        float right=StyleConstants.getRightIndent(par.getAttributes());
//        out.write(" "+ModDocument.ATTR_NAME_RIGHT+"=\""+right+"\"");
//        float ls=StyleConstants.getLineSpacing(par.getAttributes());
//        out.write(" "+ModDocument.ATTR_NAME_LINE_SPACING+"=\""+ls+"\"");
 
//        TabSet ts=StyleConstants.getTabSet(el.getAttributes());
//        if (ts!=null) {
//            throw new IOException("TabSet saving is not supported!");
//        }
 
//        out.write(">");
        //write children
        int iStart=el.getElementIndex(start);
        int iEnd=el.getElementIndex(start+len);
 
        for (int i=iStart; i<=iEnd; i++) {
            ModElement newEl=el.getElement(i);
			if(newEl.isLeaf()) {
	            writeToken(newEl, start, len, out);
			} else {
				writeElement(el, start, len, out);
			}
			
        }
 
//        out.write("</"+ModDocument.TAG_NAME_PAR+">");
    }
    
    public static void writeToken(ModElement token, int start, int len, Writer out) throws IOException, BadLocationException {
//        out.write("<" "+ModDocument.ATTR_NAME_FONT_SIZE+"=\""+fs+"\"");
//        String name=StyleConstants.getFontFamily(text.getAttributes());
//        out.write(" "+ModDocument.ATTR_NAME_FONT_FAMILY+"=\""+name+"\"");
//        boolean bold=StyleConstants.isBold(text.getAttributes());
//        out.write(" "+ModDocument.ATTR_NAME_BOLD+"=\""+bold+"\"");
//        boolean italic=StyleConstants.isItalic(text.getAttributes());
//        out.write(" "+ModDocument.ATTR_NAME_ITALIC+"=\""+italic+"\"");
//        boolean underline=StyleConstants.isUnderline(text.getAttributes());
//        out.write(" "+ModDocument.ATTR_NAME_UNDERLINE+"=\""+underline+"\"");
//        if (text.getAttributes().getAttribute(ModDocument.JAGGED_UDERLINE_ATTRIBUTE_NAME)!=null) {
//            boolean jUnderline=(Boolean)text.getAttributes().getAttribute(ModDocument.JAGGED_UDERLINE_ATTRIBUTE_NAME);
//            out.write(" "+ModDocument.ATTR_NAME_JUNDERLINE+"=\""+jUnderline+"\"");
//        }
// 
//        out.write(">");
        //write text

        int textStart=Math.max(start, token.getStartOffset());
        int textEnd=Math.min(start+len, token.getEndOffset());
        
        //NOTE: the String must be processed to replace e.g <> chars
        String s=token.getDocument().getText(textStart, textEnd-textStart);
//        s=escapeForXML(s);
        out.write(s);
 
//        out.write("</>");
    }
    
//    public static String escapeForXML(String src){
//        final StringBuilder res = new StringBuilder();
//        for (int i=0; i<src.length(); i++) {
//            char c =src.charAt(i);
//            if (c == '<') {
//                res.append(">");
//            }
//            else if (c == '&') {
//                res.append("&amp;");
//            }
//            else if (c == '\"') {
//                res.append("");
//            }
//            else if (c == '\t') {
//                addChar(9, res);
//            }
//            else if (c == '!') {
//                addChar(33, res);
//            }
//            else if (c == '#') {
//                addChar(35, res);
//            }
//            else if (c == '$') {
//                addChar(36, res);
//            }
//            else if (c == '%') {
//                addChar(37, res);
//            }
//            else if (c == '\'') {
//                addChar(39, res);
//            }
//            else if (c == '(') {
//                addChar(40, res);
//            }
//            else if (c == ')') {
//                addChar(41, res);
//            }
//            else if (c == '*') {
//                addChar(42, res);
//            }
//            else if (c == '+') {
//                addChar(43, res);
//            }
//            else if (c == ',') {
//                addChar(44, res);
//            }
//            else if (c == '-') {
//                addChar(45, res);
//            }
//            else if (c == '.') {
//                addChar(46, res);
//            }
//            else if (c == '/') {
//                addChar(47, res);
//            }
//            else if (c == ':') {
//                addChar(58, res);
//            }
//            else if (c == ';') {
//                addChar(59, res);
//            }
//            else if (c == '=') {
//                addChar(61, res);
//            }
//            else if (c == '?') {
//                addChar(63, res);
//            }
//            else if (c == '@') {
//                addChar(64, res);
//            }
//            else if (c == '[') {
//                addChar(91, res);
//            }
//            else if (c == '\\') {
//                addChar(92, res);
//            }
//            else if (c == ']') {
//                addChar(93, res);
//            }
//            else if (c == '^') {
//                addChar(94, res);
//            }
//            else if (c == '_') {
//                addChar(95, res);
//            }
//            else if (c == '`') {
//                addChar(96, res);
//            }
//            else if (c == '{') {
//                addChar(123, res);
//            }
//            else if (c == '|') {
//                addChar(124, res);
//            }
//            else if (c == '}') {
//                addChar(125, res);
//            }
//            else if (c == '~') {
//                addChar(126, res);
//            }
//            else if (c == '\n') {
//                addChar(10, res);
//            }
//            else {
//                res.append(c);
//            }
//        }
//        return res.toString();
//    }
 
    private static void addChar(Integer aIdx, StringBuilder aBuilder){
        String padding = "";
        if( aIdx <= 9 ){
            padding = "00";
        }
        else if( aIdx <= 99 ){
            padding = "0";
        }
        else {
            //no prefix
        }
        String number = padding + aIdx.toString();
        aBuilder.append("&#").append(number).append(";");
    }
 
//    public static String unescapeForXML(String src){
//        final StringBuilder res = new StringBuilder(src);
//        int i=res.indexOf("&");
//        while (i>=0) {
//            String s=res.substring(i);
//            if (s.startsWith("<")) {
//                res.replace(i,i+4,">");
//            }
//            else if (s.startsWith("&amp;")) {
//                res.replace(i,i+5,"&");
//            }
//            else if (s.startsWith("&qout;")) {
//                res.replace(i,i+6,"\"");
//            }
//            else if (s.startsWith("&#")) {
//                int charEnd=res.indexOf(";", i);
//                if (charEnd>=0) {
//                    String cStr=res.substring(i+2, charEnd);
//                    char c=(char)Integer.parseInt(cStr);
//                    res.replace(i, charEnd+1, c+"");
//                }
//            }
// 
//            i=res.indexOf("&", i+1);
//        }
//
//        return res.toString();
//    }
 
}
