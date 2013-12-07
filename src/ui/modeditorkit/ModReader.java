package ui.modeditorkit;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;
 
import javax.swing.text.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import model.moddocument3.ModDocument;
 

/**
 *
 * @author Amineri
 */


public class ModReader {
 
    public static void read(ModDocument d, int pos, InputStream in) throws IOException, BadLocationException {
        if (!(d instanceof ModDocument)) {
            return;
        }

        String encoding = System.getProperty("file.encoding");
        AttributeSet as = null;
        try (Scanner s = new Scanner(in, encoding))
        {
            System.out.print("Reading modfile... ");
            while(s.hasNext())
            {
                d.insertString(d.getLength(), s.nextLine() + "\n", as);
            }
            d.insertUpdate(null, as);
        }
        catch (Throwable x) 
        {
            System.out.println("caught exception: " + x);
        }

//        ModDocument doc=(ModDocument)d;
//        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//        dbf.setIgnoringElementContentWhitespace(false);
// 
//        try {
//            //Using factory get an instance of document builder
//            javax.xml.parsers.DocumentBuilder dbXML = dbf.newDocumentBuilder();
// 
//            //parse using builder to get DOM representation of the XML file
//            org.w3c.dom.Document dom = dbXML.parse(in);
// 
//            NodeList pars=dom.getDocumentElement().getChildNodes();
//            int offs=pos;
//            for (int i=0; i<pars.getLength(); i++) {
//                Node par=pars.item(i);
//                offs+=writePar(doc, offs, par);
//            }
//
//        } catch(SAXException pce) {
//            pce.printStackTrace();
//            throw new IOException(pce.getMessage());
//        } catch(ParserConfigurationException pce) {
//
//            pce.printStackTrace();
//            throw new IOException(pce.getMessage());
//        }
    }
 
    public static int writePar(ModDocument doc, int pos, Node par) throws BadLocationException{
        int len=0;
        NodeList texts=par.getChildNodes();
        for (int i=0; i<texts.getLength(); i++) {
            Node text=texts.item(i);
            len+=writeText(doc, pos+len, text);
        }

//        doc.setParagraphAttributes(pos+len-1, 1, getParagraphAttributes(par), true);
 
        return len;
    }
 
    public static SimpleAttributeSet getParagraphAttributes(Node par) {
        SimpleAttributeSet res=new SimpleAttributeSet();
 
//        String v=par.getAttributes().getNamedItem(ModDocument.ATTR_NAME_ALIGN).getNodeValue();
//        StyleConstants.setAlignment(res, Integer.parseInt(v));
//        v=par.getAttributes().getNamedItem(ModDocument.ATTR_NAME_ABOWE).getNodeValue();
//        StyleConstants.setSpaceAbove(res, Float.parseFloat(v));
//        v=par.getAttributes().getNamedItem(ModDocument.ATTR_NAME_BELOW).getNodeValue();
//        StyleConstants.setSpaceBelow(res, Float.parseFloat(v));
//        v=par.getAttributes().getNamedItem(ModDocument.ATTR_NAME_LEFT).getNodeValue();
//        StyleConstants.setLeftIndent(res, Float.parseFloat(v));
//        v=par.getAttributes().getNamedItem(ModDocument.ATTR_NAME_RIGHT).getNodeValue();
//        StyleConstants.setRightIndent(res, Float.parseFloat(v));
//        v=par.getAttributes().getNamedItem(ModDocument.ATTR_NAME_LINE_SPACING).getNodeValue();
//        StyleConstants.setLineSpacing(res, Float.parseFloat(v));
 
        return res;
    }
 
    public static SimpleAttributeSet getCharacterAttributes(Node text) {
        SimpleAttributeSet res=new SimpleAttributeSet();
 
//        String v=text.getAttributes().getNamedItem(ModDocument.ATTR_NAME_FONT_SIZE).getNodeValue();
//        StyleConstants.setFontSize(res, Integer.parseInt(v));
//        v=text.getAttributes().getNamedItem(ModDocument.ATTR_NAME_FONT_FAMILY).getNodeValue();
//        StyleConstants.setFontFamily(res, v);
//        v=text.getAttributes().getNamedItem(ModDocument.ATTR_NAME_BOLD).getNodeValue();
//        StyleConstants.setBold(res, Boolean.parseBoolean(v));
//        v=text.getAttributes().getNamedItem(ModDocument.ATTR_NAME_ITALIC).getNodeValue();
//        StyleConstants.setItalic(res, Boolean.parseBoolean(v));
//        v=text.getAttributes().getNamedItem(ModDocument.ATTR_NAME_UNDERLINE).getNodeValue();
//        StyleConstants.setUnderline(res, Boolean.parseBoolean(v));
//        if (text.getAttributes().getNamedItem(ModDocument.ATTR_NAME_JUNDERLINE)!=null) {
//            v=text.getAttributes().getNamedItem(ModDocument.ATTR_NAME_JUNDERLINE).getNodeValue();
//            res.addAttribute(ModDocument.JAGGED_UDERLINE_ATTRIBUTE_NAME,Boolean.parseBoolean(v));
//        }
 
        return res;
    }
 
    public static int writeText(ModDocument doc, int pos, Node text) throws BadLocationException{
        if (text.getFirstChild()!=null) {
            String s=text.getFirstChild().getNodeValue();
            doc.insertString(pos, s, getCharacterAttributes(text));
            return s.length();
        }
 
        return 0;
    }

}
