package io.model.moddocument;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import model.moddocument3.ModDocument;

/**
 *
 * @author Amineri
 */

/**
 * Performs the loading of a plain text file into a ModDocument
 * @author Amineri
 */

public class ModDocumentFileReader {
	
	private File source;
	
	public ModDocumentFileReader(File source)
	{
		if(!source.exists()) {
			return;
		}
		this.source = source;
	}
	
	public ModDocument parse() throws BadLocationException
	{
		ModDocument doc = new ModDocument();
		doc.getDefaultRootElement();
        String encoding = System.getProperty("file.encoding");
		AttributeSet as = null;
        try (Scanner s = new Scanner(source, encoding))
        {
            while(s.hasNext())
            {
				doc.insertString(doc.getLength(), s.nextLine() + "\n", as);
            }
            doc.insertUpdate(null, as);
        }
        catch (IOException x) 
        {
            System.out.println("caught exception: " + x);
        }
		return doc;
	}
}
