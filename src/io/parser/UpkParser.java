package io.parser;

import io.model.upk.ImportEntry;
import io.model.upk.NameEntry;
import io.model.upk.ObjectEntry;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import static model.modtree.ModTree.logger;

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
	 * The buffer size.
	 */
	private int bufSize = 10240;
	
	/**
	 * Constructs an UPK parser from the specified UPK file.
	 * @param upkFile the file to parse
	 */
	public UpkParser(File upkFile) {
		this.upkFile = upkFile;
	}

	/**
	 * Parses the specified *.upk file's header.
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
		List<NameEntry> nameList = this.parseNameList(ints[1], ints[0]);
		logger.log(Level.FINE, "Parsed name list, took " + (System.currentTimeMillis() - startTime) + "ms");
		startTime = System.currentTimeMillis();
		List<ObjectEntry> objectList = this.parseObjectList(ints[3], ints[2]);
		logger.log(Level.FINE, "Parsed object list, took " + (System.currentTimeMillis() - startTime) + "ms");
		startTime = System.currentTimeMillis();
		List<ImportEntry> importList = this.parseImportList(ints[5], ints[4]);
		logger.log(Level.FINE, "Parsed import list, took " + (System.currentTimeMillis() - startTime) + "ms");
        
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
		// init namelist
		List<NameEntry> entryList = new ArrayList<>(nameListSize);
		
		// init double buffer
		byte[] buf = new byte[this.bufSize * 2];
		this.raf.seek(nameListPos);
		this.raf.read(buf);
		
		ByteBuffer byteBuf = ByteBuffer.wrap(buf);
		byteBuf.order(ByteOrder.LITTLE_ENDIAN);
		for (int i = 0; i < nameListSize; i++) {
			// extract name entry from buffer
			entryList.add(this.readNameEntry(byteBuf));

			// check whether we reached into back buffer range
			int position = byteBuf.position();
			if (position > this.bufSize) {
				// rewind buffer position
				byteBuf.position(position - this.bufSize);
				// copy back buffer to front buffer range
				System.arraycopy(buf, this.bufSize, buf, 0, this.bufSize);
				// read new bytes into back buffer
				this.raf.read(buf, this.bufSize, this.bufSize);
			}
		}
		
		return entryList;
	}

	/**
	 * Reads a single namelist entry at the specified byte buffer's current position.
	 * @param byteBuf the byte buffer to extract the namelist entry from
	 * @return the namelist entry
	 */
	private NameEntry readNameEntry(ByteBuffer byteBuf) {
		// extract string length from first 4 bytes
		int strLen = byteBuf.getInt();
		
		// extract string (without termination character)
		byte[] strBuf = new byte[strLen - 1];
		byteBuf.get(strBuf);
		
		// skip termination character and 8 extra flag bytes
		byteBuf.position(byteBuf.position() + 9);
		
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
		// init objectlist
		List<ObjectEntry> objectList = new ArrayList<>(objectListSize);
		objectList.add(null);   // add dummy element so count starts at 1
		
		// init double buffer
		byte[] buf = new byte[this.bufSize * 2];
		this.raf.seek(objectListPos);
		this.raf.read(buf);

		ByteBuffer byteBuf = ByteBuffer.wrap(buf);
		byteBuf.order(ByteOrder.LITTLE_ENDIAN);
		for (int i = 0; i < objectListSize; i++) {
			// extract object entry from buffer
			objectList.add(this.readObjectEntry(byteBuf, (int) raf.getFilePointer()));

			// check whether we reached into back buffer range
			int position = byteBuf.position();
			if (position > this.bufSize) {
				// rewind buffer position
				byteBuf.position(position - this.bufSize);
				// copy back buffer to front buffer range
				System.arraycopy(buf, this.bufSize, buf, 0, this.bufSize);
				// read new bytes into back buffer
				this.raf.read(buf, this.bufSize, this.bufSize);
			}
		}
		
		return objectList;
	}

	/**
	 * Reads a single objectlist entry at the specified byte buffer's current position.
	 * @param byteBuf the byte buffer to extract the objectlist entry from
	 * @return the objectlist entry
	 */
	private ObjectEntry readObjectEntry(ByteBuffer byteBuf, int rafPosition) {
		// create int buffer view of byte buffer
		IntBuffer intBuf = byteBuf.asIntBuffer();
		
		int filePosition = rafPosition + byteBuf.position() - byteBuf.capacity();
		
		// number of ints of objectlist entry is 17 plus whatever is encoded in the eleventh int
		int numInts = 17 + intBuf.get(11);
		int[] ints = new int[numInts];
		
		// extract ints
		intBuf.get(ints);
		
		// manually move byte buffer position
		byteBuf.position(byteBuf.position() + numInts * 4);
		
		return new ObjectEntry(ints, filePosition);
	}

	/**
	 * Parses the importlist entries located after the specified byte position. 
	 * @param objectListPos the byte position of the importlist
	 * @param objectListSize the expected number of importlist entries
	 * @return the importlist
	 * @throws IOException if an I/O error occurs
	 */
	private List<ImportEntry> parseImportList(int importListPos, int importListSize) throws IOException {
		// init importlist
		List<ImportEntry> importList = new ArrayList<>(importListSize);
		importList.add(null);  // add dummy element so count starts at 1
		
		// init double buffer
		byte[] buf = new byte[this.bufSize * 2];
		this.raf.seek(importListPos);
		this.raf.read(buf);

		ByteBuffer byteBuf = ByteBuffer.wrap(buf);
		byteBuf.order(ByteOrder.LITTLE_ENDIAN);
		for (int i = 0; i < importListSize; i++) {
			// extract import entry from buffer
			importList.add(this.readImportEntry(byteBuf));

			// check whether we reached into back buffer range
			int position = byteBuf.position();
			if (position > this.bufSize) {
				// rewind buffer position
				byteBuf.position(position - this.bufSize);
				// copy back buffer to front buffer range
				System.arraycopy(buf, this.bufSize, buf, 0, this.bufSize);
				// read new bytes into back buffer
				this.raf.read(buf, this.bufSize, this.bufSize);
			}
		}
		
		return importList;
	}

	/**
	 * Reads a single importlist entry at the specified byte buffer's current position.
	 * @param byteBuf the byte buffer to extract the importlist entry from
	 * @return the importlist entry
	 */
	private ImportEntry readImportEntry(ByteBuffer byteBuf) {
		// create int buffer view of byte buffer
		IntBuffer intBuf = byteBuf.asIntBuffer();
		
		// number of ints of importlist entry is always 7
		int numInts = 7;
		int[] ints = new int[numInts];
		
		// extract ints
		intBuf.get(ints);
		
		// manually move byte buffer position
		byteBuf.position(byteBuf.position() + numInts * 4);
		
		return new ImportEntry(ints);
	}

}
