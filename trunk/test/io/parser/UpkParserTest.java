package io.parser;

import io.model.upk.NameEntry;

import java.nio.file.Path;
import java.nio.file.Paths;
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
		
		Path path = Paths.get("test/resources/XComGame_EU_patch4.upk");
		
		UpkFile upkFile = new UpkFile(path);
		UpkHeader header = upkFile.getHeader();
		
		List<NameEntry> nameList = header.getNameList();
		
		assertEquals("/ package/gfxAnchoredMessageMgr/AnchoredMessageMgr", nameList.get(0).getName());
		assertEquals("ZPlane", nameList.get(nameList.size() - 1).getName());
		
		assertEquals(50730, header.getObjectListSize());
		
		assertEquals("Core:OnlineEventManager@GameEngine@Engine", header.importListStrings.get(1583));
		
		assertEquals("m_kWeapon@XGAbility_Targeted", header.objectListStrings.get(48104));
		assertEquals(4220694, header.getObjectList().get(48104).getObjectEntryPos());

		/*
		 * Tests for XComGame.upk ENEMY WITHIN RELEASE
		 */
		
		path = Paths.get("test/resources/XComGame_EW_release.upk");
		
		upkFile = new UpkFile(path);
		header = upkFile.getHeader();
		
		nameList = header.getNameList();
		
		assertEquals("/ package/gfxAnchoredMessageMgr/AnchoredMessageMgr", nameList.get(0).getName());
		assertEquals("ZUp", nameList.get(nameList.size() - 1).getName());
		
		assertEquals(56229, header.getObjectListSize());
		
		
	}
	
}
