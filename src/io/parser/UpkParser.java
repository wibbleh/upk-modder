package io.parser;

import io.model.upk.NameEntry;
import io.model.upk.ObjectEntry;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import model.upk.UpkHeader;

/**
 * Class featuring UPK file parsing capabilities.
 * 
 * @author XMS
 */
public class UpkParser {
	
	/**
	 * The UPK file reference.
	 */
	private File upkFile;
	
	/**
	 * The reader instance.
	 */
	private RandomAccessFile raf;
	
	/**
	 * Constructs an UPK parser from the specified UPK file.
	 * @param upkFile the file to parse
	 */
	public UpkParser(File upkFile) {
		this.upkFile = upkFile;
		
	}

	/**
	 * Parses the specified *.upk file's header.
	 * @param upkFile the *.upk file to parse
	 * @return the parsed header
	 * @throws IOException if an I/O error occurs
	 */
	public UpkHeader parseHeader() throws IOException {
		
		this.raf = new RandomAccessFile(upkFile, "r");
		this.raf.seek(0x19L);

		int nameListSize = raf.readInt();
		int nameListPos = raf.readInt();
		int objectListSize = raf.readInt();
		int objectListPos = raf.readInt();
		
		List<NameEntry> nameList = this.parseNameList(nameListPos, nameListSize);
		List<ObjectEntry> objectList = this.parseObjectList(objectListPos, objectListSize);
		
		this.raf.close();
		
		return new UpkHeader(nameList, nameListPos, objectList, objectListPos);
	}

	/**
	 * Parses the namelist entries located after the specified byte position. 
	 * @param nameListPos the byte position of the namelist
	 * @param nameListSize the expected number of namelist entries
	 * @return the namelist
	 * @throws IOException if an I/O error occurs
	 */
	private List<NameEntry> parseNameList(int nameListPos, int nameListSize) throws IOException {
		List<NameEntry> entryList = new ArrayList<NameEntry>(nameListSize);
		
		this.raf.seek(nameListPos);
		
		for (int i = 0; i < nameListSize; i++) {
			entryList.add(readNameEntry());
		}
		
		return entryList;
	}

	/**
	 * Reads a single namelist entry at the reader's current position.
	 * @return the namelist entry
	 * @throws IOException
	 */
	private NameEntry readNameEntry() throws IOException {
		int strLen = this.raf.readInt();
		byte[] strBuf = new byte[strLen];
		this.raf.read(strBuf);
		
		return new NameEntry(new String(strBuf));
	}

	/**
	 * Parses the objectlist entries located after the specified byte position. 
	 * @param objectListPos the byte position of the objectlist
	 * @param objectListSize the expected number of objectlist entries
	 * @return the objectlist
	 * @throws IOException if an I/O error occurs
	 */
	private List<ObjectEntry> parseObjectList(int objectListPos, int objectListSize) throws IOException {
		List<ObjectEntry> objectList = new ArrayList<ObjectEntry>(objectListSize);
		
		this.raf.seek(objectListPos);
		
		for (int i = 0; i < objectListSize; i++) {
			objectList.add(readObjectEntry());
		}
		
		return objectList;
	}

	/**
	 * Reads a single objectlist entry at the reader's current position.
	 * @return the objectlist entry
	 * @throws IOException if an I/O error occurs
	 */
	private ObjectEntry readObjectEntry() throws IOException {
		List<Integer> data = new ArrayList<Integer>();
		for (int i = 0; i < 11; i++) {
			data.add(this.raf.readInt());
		}
		int extraBytes = this.raf.readInt();
		data.add(extraBytes);
		for (int i = 0; i < 6 + extraBytes; i++) {
			data.add(this.raf.readInt());
		}
		
		return new ObjectEntry(data);
	}
	
}
