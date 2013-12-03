package io.parser;

import io.model.upk.NameEntry;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;

import model.upk.UpkFile;
import model.upk.UpkHeader;

import org.junit.Test;

/**
 * Test class for upk parsing
 * 
 * @author XMS
 */
public class UpkParserTest extends TestCase {

	@Test
	public void testHeaderParsing() {
		
		File file = new File("test/resources/XComGame.upk");
		
		UpkFile upkFile = new UpkFile(file);
		UpkHeader header = upkFile.getHeader();
		
		List<NameEntry> nameList = header.getNameList();
		
		assertEquals("/ package/gfxAnchoredMessageMgr/AnchoredMessageMgr", nameList.get(0).getName());
		assertEquals("ZPlane", nameList.get(nameList.size() - 1).getName());
		
		assertEquals(50730, header.getObjectListSize());
		
	}
	
}
