package util.unrealhex;

import io.model.upk.ObjectEntry;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.logging.Level;
import static model.modtree.ModTree.logger;
import model.upk.UpkFile;

/**
 * utility class to move and resize a given function
 * @author Amineri
 */


public class MoveAndResizeFunction {

	/**
	 * Copies and appends a given function block to the end of the supplied upk
	 * @param bytesToAdd the number of bytes to add to the function
	 * @param targetFunction the name of the function to relocate
	 * @param upk the upk file to make the modification to
	 * @return true if the operation was successful, false otherwise. detailed information about failure modes written to the logger.
	 */
	public static boolean moveAndResizeFunction(int bytesToAdd, String targetFunction, UpkFile upk) {
		
		// check that user is attempting to add size
		if(bytesToAdd < 0) {
			logger.log(Level.INFO, "Attempted to size function smaller.");
			return false;
		}
		
		int objectIndex = upk.findRefByName(targetFunction);
		if (objectIndex == 0) {
			logger.log(Level.INFO, "Function name " + targetFunction + " not found.");
			return false;
		}
		if (objectIndex < 0) {
			logger.log(Level.INFO, "Supplied Import Function. Only Export functions can be relocated.");
			return false;
		}

		// retrieve object entry
		ObjectEntry functionEntry = upk.getHeader().getObjectList().get(objectIndex);
		
		// retrieve file position/length of function hex in upk
		int functPos = functionEntry.getUpkPos();
		int functLength = functionEntry.getUpkSize();

		//allocate buffer to read original function hex into
		ByteBuffer originalFunctBuf = ByteBuffer.allocate(functLength);
		originalFunctBuf.order(ByteOrder.LITTLE_ENDIAN);
		
		// read original function
		try (SeekableByteChannel sbc = Files.newByteChannel(upk.getFile().toPath(), StandardOpenOption.READ)) {
			sbc.position(functPos); // set file position
			sbc.read(originalFunctBuf);		// retrieve original function code (including 48 byte header + 15 byte footer
		} catch(IOException ex) {
			logger.log(Level.SEVERE, "IO Failure when attempting to read original function", ex);
			return false;
		}
		
		// test that EOS token is in expected location
		if(originalFunctBuf.get(functLength-16) != (byte) 0x53) { // did not find EOS token at expected position
			logger.log(Level.INFO, "Did not find EOS token at expected position");
			return false;
		}
	
		originalFunctBuf.rewind();
		// allocate new function buffer
		ByteBuffer newFunctionBuf = ByteBuffer.allocate(functLength + bytesToAdd + 24);
		newFunctionBuf.order(ByteOrder.LITTLE_ENDIAN);
		
		// copy function header, updating memory and virtual sizes
		for (int i = 0; i < 10;  i ++) { // first 10 integers are straight transfer
			newFunctionBuf.putInt(originalFunctBuf.getInt());
		}
		// memory size increased
		newFunctionBuf.putInt(originalFunctBuf.getInt()+ bytesToAdd);
		
		// file size increased
		newFunctionBuf.putInt(originalFunctBuf.getInt()+ bytesToAdd);
		
		// copy header + function body (up to but not including 0x53 token)
		byte[] front = new byte[functLength - (16 + 48)];
		originalFunctBuf.get(front, 0, functLength - (16 + 48));
		newFunctionBuf.put(front);
		
		//create new filler array with null ops (0x0B) of required size
		byte[] filler = new byte[bytesToAdd];
		Arrays.fill(filler, (byte) 0x0B);
		
		// fill in null-ops for remainder of function
		newFunctionBuf.put(filler);
		
		// add EOS token
		newFunctionBuf.put(originalFunctBuf.get());
		
		// fill in remainder footer (last 15 bytes)
		byte[] footer = new byte[15];
		originalFunctBuf.get(footer, 0, 15);
		newFunctionBuf.put(footer);
		
		// add uninstall info
		byte[] id = new byte[] { (byte) 0x7A, (byte) 0xA0, (byte) 0x56, (byte) 0xC9, (byte) 0x60, (byte) 0x5F, (byte) 0x7B, 
				(byte) 0x31, (byte) 0x72, (byte) 0x5D, (byte) 0x4B, (byte) 0xC4, (byte) 0x7C, (byte) 0xD2, (byte) 0x4D, (byte) 0xD9};
		
		// add unique hashId
		newFunctionBuf.put(id);
		
		newFunctionBuf.putInt(functLength);
		newFunctionBuf.putInt(functPos);
		
		newFunctionBuf.rewind();
		
		int newFunctPos;
		// append new function
		try (SeekableByteChannel sbc = Files.newByteChannel(upk.getFile().toPath(), StandardOpenOption.APPEND)) {
			newFunctPos = (int) sbc.position();
			sbc.write(newFunctionBuf);		// write buffer
		} catch(IOException ex) {
			logger.log(Level.SEVERE, "IO Failure when attempting to append enlarged function function", ex);
			return false;
		}
		
		// fix up object list entry to point to new location
		int objectListPos = functionEntry.getObjectEntryPos();
		ByteBuffer intBuf = ByteBuffer.allocate(8);
		intBuf.order(ByteOrder.LITTLE_ENDIAN);
		intBuf.putInt(functLength + bytesToAdd + 24);
		intBuf.putInt(newFunctPos);
		intBuf.rewind();
		
		// append new function
		try (SeekableByteChannel sbc = Files.newByteChannel(upk.getFile().toPath(), StandardOpenOption.WRITE)) {
			sbc.position(objectListPos +32); // set file position
			sbc.write(intBuf);		// write buffer
		} catch(IOException ex) {
			logger.log(Level.SEVERE, "IO Failure when attempting to update Object Entry", ex);
			return false;
		}
		
		
		return true;
	}
	
}
