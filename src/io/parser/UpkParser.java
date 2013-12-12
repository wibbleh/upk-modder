package io.parser;

import io.model.upk.ImportEntry;
import io.model.upk.NameEntry;
import io.model.upk.ObjectEntry;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import model.upk.UpkHeader;
import util.unrealhex.HexStringLibrary;

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
	
	byte[] buf = new byte[256];
	int strLen;
	long namePosition;
	String temp;
	String encoding = System.getProperty("file.encoding");
	
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
        
		int numInts = 6;
		byte[] bytes = new byte[numInts * 4];
		raf.read(bytes);
		
		ByteBuffer buf = ByteBuffer.wrap(bytes);
		buf.order(ByteOrder.LITTLE_ENDIAN);

		int[] ints = new int[numInts];
		buf.asIntBuffer().get(ints);
		
		long startTime = System.currentTimeMillis();
		System.out.print("Parsing name list... ");
		List<NameEntry> nameList = this.parseNameList(ints[1], ints[0]);
		System.out.print(" done, took " + (System.currentTimeMillis() - startTime) + "ms\nParsing object list...");
		startTime = System.currentTimeMillis();
		List<ObjectEntry> objectList = this.parseObjectList(ints[3], ints[2]);
		System.out.print(" done, took " + (System.currentTimeMillis() - startTime) + "ms\nParsing import list...");
		startTime = System.currentTimeMillis();
		List<ImportEntry> importList = this.parseImportList(ints[5], ints[4]);
		System.out.println(" done, took " + (System.currentTimeMillis() - startTime) + "ms");
        
        this.raf.seek(0x45L);
        byte[] aGUID = new byte[16];
        this.raf.read(aGUID);
		
		this.raf.close();

		return new UpkHeader(nameList, ints[1], objectList, ints[3], importList, ints[5], aGUID);
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
		namePosition = nameListPos;
		for (int i = 0; i < nameListSize; i++) {
			entryList.add(readNameEntry());
//			raf.skipBytes(8);	// consume 8 extra flag bytes (2 words)
		}
		
		return entryList;
	}

	/**
	 * Reads a single namelist entry at the reader's current position.
	 * @return the namelist entry
	 * @throws IOException
	 */
	private NameEntry readNameEntry() throws IOException {
//		int strLen = Integer.reverseBytes(this.raf.readInt());
//		byte[] strBuf = new byte[strLen - 1];	// omit termination character 0x00
//		this.raf.read(strBuf);
//		this.raf.skipBytes(1);	// skip termination character
//		
//		return new NameEntry(new String(strBuf));
		
		raf.seek(namePosition);
		raf.read(buf);
		strLen = HexStringLibrary.byteArrayToInt(buf);
		namePosition += (strLen + 12);
		temp = new String(buf);
		return new NameEntry(temp.substring(4, strLen+3));
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
		objectList.add(null);   // extra object so count starts at 1
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
		// read first 17 integers that are guaranteed to be there 
		int numInts = 17;
		byte[] bytes = new byte[numInts * 4];
		raf.read(bytes);
		// wrap read bytes in buffer
		ByteBuffer buf = ByteBuffer.wrap(bytes);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		// extract little endian integers from buffer
		int[] ints = new int[numInts];
		buf.asIntBuffer().get(ints);
		
		// eleventh item represents number of extra bytes to read
		int numExtraInts = ints[11];
		byte[] extraBytes = new byte[numExtraInts * 4];
		raf.read(extraBytes);
		// wrap read extra bytes in buffer
		buf = ByteBuffer.wrap(extraBytes);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		// init data array, copy the 17 guaranteed ints into it
		int[] data = new int[numInts + numExtraInts];
		System.arraycopy(ints, 0, data, 0, numInts);
		// extract extra ints into data array tail
		buf.asIntBuffer().get(data, numInts, numExtraInts);
		
		return new ObjectEntry(data);
	}
	
	/**
	 * Parses the importlist entries located after the specified byte position. 
	 * @param objectListPos the byte position of the importlist
	 * @param objectListSize the expected number of importlist entries
	 * @return the importlist
	 * @throws IOException if an I/O error occurs
	 */
	private List<ImportEntry> parseImportList(int importListPos, int importListSize) throws IOException {
		List<ImportEntry> importList = new ArrayList<ImportEntry>(importListSize);
		
		this.raf.seek(importListPos);
		importList.add(null);  // export import entry so count starts at 1
		for (int i = 0; i < importListSize; i++) {
			importList.add(readImportEntry());
		}
		
		return importList;
	}

	/**
	 * Reads a single importlist entry at the reader's current position.
	 * @return the importlist entry
	 * @throws IOException if an I/O error occurs
	 */
	private ImportEntry readImportEntry() throws IOException {
//		List<Integer> data = new ArrayList<Integer>();
//		for (int i = 0; i < 7; i++) {
//			data.add(this.raf.readInt());
//		}
		
		int numInts = 7;
		byte[] bytes = new byte[numInts * 4];
		raf.read(bytes);
		
		ByteBuffer buf = ByteBuffer.wrap(bytes);
		buf.order(ByteOrder.LITTLE_ENDIAN);

		int[] data = new int[numInts];
		buf.asIntBuffer().get(data);
		
		return new ImportEntry(data);
	}
	
}
