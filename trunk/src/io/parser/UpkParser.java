package io.parser;

import static model.modtree.ModTree.logger;
import io.model.upk.ImportEntry;
import io.model.upk.NameEntry;
import io.model.upk.ObjectEntry;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import model.upk.UpkHeader;

/**
 * Class featuring UPK file parsing capabilities.
 * 
 * @author XMS
 */
public class UpkParser {
	
	/**
	 * The UPK file path reference.
	 */
	private Path upkPath;
	
	/**
	 * The byte channel used for reading.
	 */
//	private SeekableByteChannel sbc;
	
	/**
	 * The buffer size.
	 */
	private int bufSize = 10240;
	
	/**
	 * Constructs an UPK parser from the specified UPK file path.
	 * @param upkFile the path to the file to parse
	 */
	public UpkParser(Path upkPath) {
		this.upkPath = upkPath;
	}

	/**
	 * Parses the specified *.upk file's header.
	 * @return the parsed header
	 * @throws IOException if an I/O error occurs
	 */
	public UpkHeader parseHeader() throws IOException {
		List<NameEntry> nameList;
		List<ObjectEntry> objectList;
		List<ImportEntry> importList;
		int numInts = 6;
		ByteBuffer byteBuf = ByteBuffer.allocate(numInts * 4);
		byteBuf.order(ByteOrder.LITTLE_ENDIAN);
		int[] ints = new int[numInts];
		byte[] aGUID = new byte[16];

		try (SeekableByteChannel sbc = Files.newByteChannel(upkPath)) {
			sbc.position(0x19L);

			sbc.read(byteBuf);
			byteBuf.rewind();

			byteBuf.asIntBuffer().get(ints);

			long startTime = System.currentTimeMillis();
			nameList = this.parseNameList(ints[1], ints[0], sbc);
			logger.log(Level.FINE, "Parsed name list, took " + (System.currentTimeMillis() - startTime) + "ms");
			startTime = System.currentTimeMillis();
			objectList = this.parseObjectList(ints[3], ints[2], sbc);
			logger.log(Level.FINE, "Parsed object list, took " + (System.currentTimeMillis() - startTime) + "ms");
			startTime = System.currentTimeMillis();
			importList = this.parseImportList(ints[5], ints[4], sbc);
			logger.log(Level.FINE, "Parsed import list, took " + (System.currentTimeMillis() - startTime) + "ms");

			sbc.position(0x45L);
			sbc.read(ByteBuffer.wrap(aGUID));
		}
		
//		this.raf.close();
//		sbc.close();

		return new UpkHeader(nameList, ints[1], objectList, ints[3], importList, ints[5], aGUID);
	}

	/**
	 * Parses the namelist entries located after the specified byte position. 
	 * @param nameListPos the byte position of the namelist
	 * @param nameListSize the expected number of namelist entries
	 * @return the namelist
	 * @throws IOException if an I/O error occurs
	 */
	private List<NameEntry> parseNameList(int nameListPos, int nameListSize, SeekableByteChannel sbc) throws IOException {
		// init namelist
		List<NameEntry> entryList = new ArrayList<>(nameListSize);
		
		// init double buffer
		byte[] buf = new byte[bufSize * 2];
		sbc.position(nameListPos);
		
		ByteBuffer byteBuf = ByteBuffer.wrap(buf);
		byteBuf.order(ByteOrder.LITTLE_ENDIAN);
		sbc.read(byteBuf);
		byteBuf.rewind();
		for (int i = 0; i < nameListSize; i++) {
			// extract name entry from buffer
			entryList.add(this.readNameEntry(byteBuf));

			// check whether we reached into back buffer range
			int position = byteBuf.position();
			if (position > bufSize) {
				int newPosition = position - bufSize;
				// copy back buffer to front buffer range
				System.arraycopy(buf, bufSize, buf, 0, bufSize);
				// read new bytes into back buffer
				byteBuf.position(bufSize);
				sbc.read(byteBuf);
				// rewind buffer position
				byteBuf.position(newPosition);
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
		// TODO: investigate BufferUnderflowException
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
	private List<ObjectEntry> parseObjectList(int objectListPos, int objectListSize, SeekableByteChannel sbc) throws IOException {
		// init objectlist
		List<ObjectEntry> objectList = new ArrayList<>(objectListSize);
		objectList.add(null);   // add dummy element so count starts at 1
		
		// init double buffer
		byte[] buf = new byte[this.bufSize * 2];
		sbc.position(objectListPos);

		ByteBuffer byteBuf = ByteBuffer.wrap(buf);
		byteBuf.order(ByteOrder.LITTLE_ENDIAN);
		sbc.read(byteBuf);
		byteBuf.rewind();
		for (int i = 0; i < objectListSize; i++) {
			// extract object entry from buffer
			objectList.add(this.readObjectEntry(byteBuf, sbc.position()));

			// check whether we reached into back buffer range
			int position = byteBuf.position();
			if (position > this.bufSize) {
				int newPosition = position - bufSize;
				// copy back buffer to front buffer range
				System.arraycopy(buf, this.bufSize, buf, 0, this.bufSize);
				// read new bytes into back buffer
//				this.raf.read(buf, this.bufSize, this.bufSize);
				byteBuf.position(bufSize);
				sbc.read(byteBuf);
				// rewind buffer position
				byteBuf.position(newPosition);
			}
		}
		
		return objectList;
	}

	/**
	 * Reads a single objectlist entry at the specified byte buffer's current position.
	 * @param byteBuf the byte buffer to extract the objectlist entry from
	 * @param pos the file offset
	 * @return the objectlist entry
	 */
	private ObjectEntry readObjectEntry(ByteBuffer byteBuf, long pos) {
		// create int buffer view of byte buffer
		IntBuffer intBuf = byteBuf.asIntBuffer();
		
		long filePosition = pos + byteBuf.position() - byteBuf.capacity();
		
		// number of ints of objectlist entry is 17 plus whatever is encoded in the eleventh int
		int numInts = 17 + intBuf.get(11);
		int[] ints = new int[numInts];
		
		// extract ints
		intBuf.get(ints);
		
		// manually move byte buffer position
		byteBuf.position(byteBuf.position() + numInts * 4);
		
		return new ObjectEntry(ints, (int) filePosition);
	}

	/**
	 * Parses the importlist entries located after the specified byte position. 
	 * @param objectListPos the byte position of the importlist
	 * @param objectListSize the expected number of importlist entries
	 * @return the importlist
	 * @throws IOException if an I/O error occurs
	 */
	private List<ImportEntry> parseImportList(int importListPos, int importListSize, SeekableByteChannel sbc) throws IOException {
		// init importlist
		List<ImportEntry> importList = new ArrayList<>(importListSize);
		importList.add(null);  // add dummy element so count starts at 1
		
		// init double buffer
		byte[] buf = new byte[this.bufSize * 2];
		sbc.position(importListPos);

		ByteBuffer byteBuf = ByteBuffer.wrap(buf);
		byteBuf.order(ByteOrder.LITTLE_ENDIAN);
		sbc.read(byteBuf);
		byteBuf.rewind();
		for (int i = 0; i < importListSize; i++) {
			// extract import entry from buffer
			importList.add(this.readImportEntry(byteBuf));

			// check whether we reached into back buffer range
			int position = byteBuf.position();
			if (position > this.bufSize) {
				int newPosition = position - bufSize;
				// copy back buffer to front buffer range
				System.arraycopy(buf, this.bufSize, buf, 0, this.bufSize);
				// read new bytes into back buffer
				byteBuf.position(bufSize);
				sbc.read(byteBuf);
				// rewind buffer position
				byteBuf.position(newPosition);
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
