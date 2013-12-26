package util.unrealhex;

import io.model.upk.ObjectEntry;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;

import model.modtree.ModContext.ModContextType;
import model.modtree.ModTree;
import static model.modtree.ModTree.logger;
import model.modtree.ModTreeNode;
import model.upk.UpkFile;

/**
 * Utility class for consolidating hex and performing apply/revert operations to upks
 * @author Amineri
 */
public class HexSearchAndReplace {
	
	public enum ApplyStatus {
		NO_UPK,
		APPLY_ERROR,
		MIXED_STATUS,
		BEFORE_HEX_PRESENT,
		AFTER_HEX_PRESENT;
	}
	/**
	 * Parses the tree and finds any hex that is part of a [BEFORE] block
	 * Each block is returned as a separate byte[] in the list
	 * @param tree the tree to extract hex from
	 * @param upk used to convert name references into hex references
	 * @return List of byte arrays containing hex, or null if there is none
	 */
	public static List<byte[]> consolidateBeforeHex(ModTree tree, UpkFile upk) {
		return consolidateHex(tree, upk, ModContextType.BEFORE_HEX);
	}
	
	/**
	 * Parses the tree and finds any hex that is part of an [AFTER] block
	 * Each block is returned as a separate byte[] in the list
	 * @param tree the tree to extract hex from
	 * @param upk used to convert name references into hex references
	 * @return List of byte arrays containing hex, or null if there is none
	 */
	public static List<byte[]> consolidateAfterHex(ModTree tree, UpkFile upk) {
		return consolidateHex(tree, upk, ModContextType.AFTER_HEX);
	}
	
	/**
	 * Parses the tree and finds any hex that is part of any context block
	 * Each block is returned as a separate byte[] in the list
	 * @param tree the tree to extract hex from
	 * @param upk
	 * @param context the context to search for
	 * @return List of byte arrays containing hex, or null if there is none
	 */
	public static List<byte[]> consolidateHex(ModTree tree, UpkFile upk, ModContextType context) {
		List<byte[]> hexBlocks = new ArrayList<>();
		List<Integer> currentBlock = new ArrayList<>();
		Enumeration<ModTreeNode> lines = tree.getRoot().children();
		while (lines.hasMoreElements()) {
			ModTreeNode line = lines.nextElement();
			if (line.getContextFlag(context)) {
				if (line.isValidHexLine()) {
					String hex = line.toHexStringArray()[1];
					String[] tokens = hex.split("\\s+");
					for (String token : tokens) {
						if (token.startsWith("{|") && (upk != null)) {
							int value = upk.findRefByName(token.substring(2, token.length() - 2));
							if (value == 0) {
								return null;
							}
							String[] subtokens = HexStringLibrary.convertIntToHexString(value).split("\\s+");
							for (String subtoken : subtokens) {
								currentBlock.add(Integer.parseInt(subtoken, 16));
							}
						} else if (token.startsWith("<|") && (upk != null)) {
							int value = upk.findVFRefByName(token.substring(2, token.length() - 2));
							if (value < 0) {
								return null;
							}
							String[] subtokens = HexStringLibrary.convertIntToHexString(value).split("\\s+");
							for (String subtoken : subtokens) {
								currentBlock.add(Integer.parseInt(subtoken, 16));
							}
						} else {
							try {
								currentBlock.add(Integer.parseInt(token, 16));
							} catch (NumberFormatException x) {
								return null;
							}
						}
					}
				}
			} else { // found line without required context. if current block is
						// non-empty stop adding to it and start a new block
				if (!currentBlock.isEmpty()) {
					hexBlocks
							.add(HexStringLibrary
									.convertIntArrayListToByteArray((ArrayList<Integer>) currentBlock));
					currentBlock.clear();
				}
			}
		}
		return hexBlocks;
	}

	/**
	 * Finds the specified hex within the given upk.
	 * Scope of the search is limited to the function in the associated tree.
	 * @param hex hex to search for
	 * @param upk upk to search for hex within
	 * @param tree provides function to limit scope of search
	 * @return file offset of found hex, or -1 if not found
	 * @throws IOException
	 */
	public static long findFilePosition(byte[] hex, UpkFile upk, ModTree tree)
			throws IOException {
        long replaceOffset = -1;
        
		// find possible destination
		
		// retrieve function name from tree
		String targetFunction = tree.getFunctionName().trim();
		
		// retrieve objectlist index from upk -- this is the same as references but is not named such here
		int objectIndex = upk.findRefByName(targetFunction);
		if(objectIndex == 0)
			return -1;
		
		// retrieve object entry
		ObjectEntry functionEntry = upk.getHeader().getObjectList().get(objectIndex);
		
		// retrieve file position/length of function hex in upk
		long functPos = functionEntry.getUpkPos();
		long functLength = functionEntry.getUpkSize();
		
		//testing method that uses fewer file reads
		// method below occasionally fails, incorrectly reporting the wrong position, but not indicating failure
//		ByteBuffer fileBuf = ByteBuffer.allocate((int) functLength); // read entire target
//		
//		//open channel to upk for read-only
//		SeekableByteChannel sbc = Files.newByteChannel(upk.getFile().toPath(), StandardOpenOption.READ);
//		
//		sbc.position(functPos); // set file position
//		sbc.read(fileBuf); // read entire search space
//		String searchSpace = new String(fileBuf.array()); // allocate buffer into String
//		
//		int findIndex = searchSpace.indexOf(new String(hex)); // search for instance
//		if(findIndex <0) { // failure
//			return findIndex;
//		} else { // success
//			return findIndex + functPos;
//		}
		
		//allocate buffer as large as we need
		ByteBuffer fileBuf = ByteBuffer.allocate(hex.length);
		try (SeekableByteChannel sbc = Files.newByteChannel(upk.getFile().toPath(), StandardOpenOption.READ)) {
			long endSearch = functPos + functLength - hex.length;
			for (long currPos = functPos; currPos < endSearch; currPos++) {
				
				// TODO: search code could probably be done faster with a match method, but I couldn't get it to work
				// TODO: @Amineri how about using 'new String(bytes).indexOf(new String(hex))' on a block of UPK bytes?
				// TODO: @Amineri we could also implement Knuth-Morris-Pratt or Boyer-Moore algorithm for maximum pattern matching performance
				boolean bMatch = true;
				sbc.position(currPos); // set file position
				sbc.read(fileBuf);
				for (int jCount = 0; jCount < hex.length; jCount++) {
					if (fileBuf.get(jCount) != hex[jCount]) {
						bMatch = false;
						break;
					}
				}
				if (bMatch) {
					replaceOffset = currPos;
					break;
				}
				fileBuf.clear();
			}
		}
		return replaceOffset;
	}
	
	/**
	 * Concatenates the contents of the byte arrays in the provided list into a single byte array.
	 * @param bytesList the list of byte arrays to concatenate
	 * @return a byte array containing all bytes of the list
	 */
	@Deprecated
	public static byte[] concatenate(List<byte[]> bytesList) {
		int size = 0;
		for (byte[] bytes : bytesList) {
			size += bytes.length;
		}
		
		byte[] res = new byte[size];
		int index = 0;
		for (byte[] bytes : bytesList) {
			int len = bytes.length;
			System.arraycopy(bytes, 0, res, index, len);
			index += len;
		}
		
		return res;
	}

	public static void applyHexChange(byte[] hex, UpkFile upk, long filePos) throws IOException {
		//allocate buffer as large as we need and wrap the hex to write
//		ByteBuffer fileBuf = ByteBuffer.allocate(hex.length);
		ByteBuffer fileBuf = ByteBuffer.wrap(hex);
		try (SeekableByteChannel sbc = Files.newByteChannel(upk.getFile().toPath(), StandardOpenOption.WRITE)) {
			sbc.position(filePos);
			sbc.write(fileBuf);
		}
	}
	
	public static ApplyStatus testFileStatus(ModTree tree) {
		// consolidate BEFORE hex
		List<byte[]> beforeHex = consolidateBeforeHex(tree, tree.getSourceUpk());
		
		//consolidate AFTER hex
		List<byte[]> afterHex = consolidateAfterHex(tree, tree.getSourceUpk());
		
		if(beforeHex.size() != afterHex.size()) {
			return ApplyStatus.APPLY_ERROR;
		}
		
		boolean foundSomeBefore = false;  // will be true if any BEFORE blocks are found
		boolean missingSomeBefore = false; // will be true if any BEFORE blocks are not found
		boolean foundSomeAfter = false;  // will be true if any AFTER blocks are found
		boolean missingSomeAfter = false;
		long beforePos, afterPos;
		for (int i = 0 ; i < beforeHex.size() ; i ++ ) {
			try {
				beforePos = findFilePosition(beforeHex.get(i), tree.getSourceUpk(), tree);
				afterPos = findFilePosition(afterHex.get(i), tree.getSourceUpk(), tree);
			}
			catch (IOException ex) {
				logger.log(Level.SEVERE, "IO Exception: ", ex);
				return ApplyStatus.APPLY_ERROR;
			}
			
			if((beforePos < 0) && (afterPos < 0)) { // both blocks not found return error
				return ApplyStatus.APPLY_ERROR;
			}
			if((beforePos >=0) && (afterPos < 0)) { // found before block and not after
				foundSomeBefore = true;
				missingSomeAfter = true;
			}

			if((beforePos < 0) && (afterPos >= 0)) { // found after block and not before
				foundSomeAfter = true;
				missingSomeAfter = true;
			}

			if((beforePos >= 0) && (afterPos >= 0)) { // matched both before and after blocks... this is probably an error
				foundSomeAfter = true;
				foundSomeBefore = true;
			}
		}

		if(foundSomeBefore && foundSomeAfter) {
			return ApplyStatus.MIXED_STATUS;
		}
		
		if(foundSomeBefore && !missingSomeBefore && !foundSomeAfter) {
			return ApplyStatus.BEFORE_HEX_PRESENT;
		}
		
		if(foundSomeAfter && !missingSomeAfter && !foundSomeBefore) {
			return ApplyStatus.AFTER_HEX_PRESENT;
		}
				
		return ApplyStatus.APPLY_ERROR;
	}
	
	/**
	 * Resizes and replaces the function defined in tree.
	 * Creates find/replace blocks
	 * Verifies that tree has only one replacement block
	 * Verifies size change
	 * Find file position for change
	 * Invokes copyAndReplace function
	 * If size altered, adjusts object list positions in new upk file
	 * @param apply flag indicating apply or revert. true if apply, false if revert
	 * @param tree model of the function to be replaced/resized
	 * @param upk the target upkfile
	 * @return
	 */
	public static boolean resizeAndReplace(boolean apply, ModTree tree, UpkFile upk) {
		boolean success = true;
		
		if(tree.getFileVersion() < 4) {
			logger.log(Level.INFO, "Modfile version does not support resize operations");
			return false;
		}
		
		int currentObjectIndex = upk.findRefByName(tree.getFunctionName());
		if(currentObjectIndex < 0) {
			logger.log(Level.INFO, "Cannot resize import objects");
			return false;
		} else if(currentObjectIndex == 0) {
			logger.log(Level.INFO, "Function not found in upk");
			return false;
		}

		int resizeAmount;
		if(apply) {
			resizeAmount = tree.getResizeAmount();
		} else {
			resizeAmount = - tree.getResizeAmount();
		}
		
		//Create find/replace blocks
		List<byte[]> findHexList;
		List<byte[]> replaceHexList;
		if(apply) {
			findHexList = consolidateHex(tree, upk, ModContextType.BEFORE_HEX);
			replaceHexList = consolidateHex(tree, upk, ModContextType.AFTER_HEX);
		} else {
			findHexList = consolidateHex(tree, upk, ModContextType.AFTER_HEX);
			replaceHexList = consolidateHex(tree, upk, ModContextType.BEFORE_HEX);
		}
		
		// Verify that tree has only one replacement block
		if(findHexList.size()!= 1 || replaceHexList.size() != 1) {
			logger.log(Level.INFO, "Resize operation requires exactly 1 BEFORE and 1 AFTER block");
			return false;
		}
		
		byte[] findHex = findHexList.get(0);
		byte[] replaceHex = replaceHexList.get(0);
		
		// Verify size change
		if(findHex.length + resizeAmount != replaceHex.length) {
			logger.log(Level.INFO, "Mismatch in expected size difference between FIND/REPLACE blocks\n"
					+ "    FIND size: " + findHex.length + "\n"
					+ "    Request resize: " + resizeAmount + "\n"
					+ "    REPLACE size: " + replaceHex.length + "\n");
			return false;
		}
		
		// Find file position for change
		long filePosition;
		try {
			filePosition = findFilePosition(findHex, upk,  tree);
		} catch(IOException ex) {
			logger.log(Level.SEVERE, "IO Error finding file position", ex);
			return false;
		}
		
		// verify found file position
		if(filePosition == -1) {
			logger.log(Level.SEVERE, "FIND hex not found");
			return false;
		}
		
		long startTime = System.currentTimeMillis();
		
		// Invoke copyAndReplace function
		File newFile = copyAndReplaceUpk((int) filePosition, findHex, replaceHex , upk);
		if(newFile == null) {
			logger.log(Level.INFO, "Failure during copyAndReplace");
			return false;
		}
		logger.log(Level.FINE, "Resize: inserted new hex, took " + (System.currentTimeMillis() - startTime) + "ms");


		startTime = System.currentTimeMillis();

		
		try (SeekableByteChannel sbc = Files.newByteChannel(upk.getFile().toPath(), StandardOpenOption.WRITE)) {
			ByteBuffer intBuf = ByteBuffer.allocate(4);
			intBuf.order(ByteOrder.LITTLE_ENDIAN);
			
			// update changed object/function's ObjectEntry size in file
			ObjectEntry currObjectEntry = upk.getHeader().getObjectList().get(currentObjectIndex);
			int objectListPos = currObjectEntry.getObjectEntryPos();
			int objectSize = currObjectEntry.getUpkSize();
			intBuf.putInt(objectSize + resizeAmount);
			intBuf.rewind();
			sbc.position(objectListPos + 32); // set file position -- 32 writes to the 8th word in the ObjectEntry, object size
			sbc.write(intBuf);		// write buffer
			
			// update changes object/function's ObjectEntry size in memory
			currObjectEntry.setUpkSize(objectSize + resizeAmount);
		
			// If size altered, adjusts object list positions in new upk file
			if(resizeAmount != 0) {
				for(int i = 1 ; i < upk.getHeader().getObjectListSize() ; i++) { // for every object in the object list

					currObjectEntry = upk.getHeader().getObjectList().get(i);
					// check if object is after the inserted file position 
					if(currObjectEntry.getUpkPos() > filePosition) {
						// update Object Entry's position in file
						intBuf.clear();
						intBuf.putInt(currObjectEntry.getUpkPos() + resizeAmount);
						intBuf.rewind();
						objectListPos = currObjectEntry.getObjectEntryPos();
						sbc.position(objectListPos + 36); // set file position -- 36 writes to the 9th word in the ObjectEntry, object position
						sbc.write(intBuf);		// write buffer
						
						// update Object Entry's position in memory
						currObjectEntry.setUpkPos(currObjectEntry.getUpkPos() + resizeAmount);
					}
				}
			}
		} catch(IOException ex) {
			logger.log(Level.SEVERE, "IO Failure when attempting to update Object Entries", ex);
			return false;
		}
		
		logger.log(Level.FINE, "Resize: rewrote object table, took " + (System.currentTimeMillis() - startTime) + "ms");
		return success;
	}
	
	/**
	 * Primitive file copy operation with replacement.
	 * Resizing is allowed (oldHex can be a different size than newHex)
	 * Verifies that oldHex is at filePosition.
	 * Renames UpkFile upk to .bak version
	 * Creates new File to copy into
	 * Copies all hex from start of file to filePosition from old File to new File
	 * Writes newHex to newFile
	 * Copies all hex from end of oldHex from old File to newFile
	 * @param filePosition
	 * @param findHex
	 * @param replaceHex
	 * @param upk the upk file to make the modification to
	 * @return The newly created File, or null if the operation failed
	 */
	public static File copyAndReplaceUpk(int filePosition, byte[] findHex, byte[] replaceHex , UpkFile upk) {
		
		File origFile = upk.getFile();
		
		// verify that oldHex is at FilePosition 
		ByteBuffer fileBuf = ByteBuffer.allocate(findHex.length);
		try (SeekableByteChannel sbc = Files.newByteChannel(origFile.toPath(), StandardOpenOption.READ)) {
			sbc.position(filePosition);
			sbc.read(fileBuf);
		} catch(IOException ex) {
			logger.log(Level.SEVERE, "Failed to read upk file", ex);
			return null;
		}
		if(!Arrays.equals(findHex, fileBuf.array())) {
			logger.log(Level.INFO, "Find hex not found");
			return null;
		}
		long endFindHexPosition = filePosition + findHex.length;
		
		// Rename upk to .bak version
		// verify that filename ends with ".upk"
		String origFilename = origFile.toPath().toAbsolutePath().toString();
		if(!origFilename.endsWith(".upk")) {
			logger.log(Level.INFO, "Target file not valid upk");
			return null;
		}
		String backupFileName = origFilename.replace(".upk", ".bak");
		Path backupFilePath = Paths.get(backupFileName);
//		File backupFile = new File(backupFileName);
		// delete old backup if it exists
//		try {
//			Files.deleteIfExists(backupFilePath); 
//		} catch(IOException ex) {
//			Logger.getLogger(HexSearchAndReplace.class.getName()).log(Level.SEVERE, "Unable to delete old backup file", ex);
//		}
		
		// wait until file is delete so can rename .upk to .bak
//		if(new File(backupFileName).exists()) {
//			try {
//				TimeUnit.MILLISECONDS.sleep(50);
//			} catch(InterruptedException ex) {
//				Logger.getLogger(HexSearchAndReplace.class.getName()).log(Level.SEVERE, "Error while sleeping", ex);
//			}
//		}
		
//		File backupFile = new File(backupFileName);
		Path newPath;
		try {
			newPath = Files.move(origFile.toPath(), backupFilePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
		} catch(IOException ex) {
			logger.log(Level.INFO, "Failed to create backup file", ex);
			return null;
		}
		File backupFile = newPath.toFile();
		
		// create new file with same name as original upk
		File newFile = new File(origFilename);
		if(!newFile.exists()) {
			try {
				newFile.createNewFile();
			} catch(IOException ex) {
				logger.log(Level.SEVERE, "Could not create new upk file", ex);
				return null;
			}
		}
		
		// benchmark comparisons : http://java.dzone.com/articles/file-copy-java-%E2%80%93-benchmark
		// NIO copy method
		try (FileChannel source = new FileInputStream(backupFile).getChannel();
			 FileChannel destination = new FileOutputStream(newFile).getChannel()) {
				
				// copy all hex from start of file to filePosition from oldFile to newFile
				destination.transferFrom(source, 0, filePosition);
				
				ByteBuffer replaceHexBuf = ByteBuffer.wrap(replaceHex);
				//Write newHex to newFile
				destination.position(destination.size());
				destination.write(replaceHexBuf);
				
				//Copy all hex from end of oldHex from old File to newFile
				source.position(source.position()+findHex.length);
				destination.position(destination.size());
				destination.transferFrom(source, destination.size(), source.size()-endFindHexPosition);
				
		} catch(FileNotFoundException ex) {
			logger.log(Level.SEVERE, "File not found during copy", ex);
			return null;
		} catch(IOException ex) {
			logger.log(Level.SEVERE, "IO Error during file copy", ex);
			return null;
		}

		return newFile;
	}

	/**
	 * Alters the object table entry specified in the ModTree to alter the object's type
	 * @param apply whether the change is being applied or reverted
	 * @param tree contains the data necessary to make the change
	 * @return
	 */
	public static boolean changeObjectType(boolean apply, ModTree tree) {

		ModContextType findContext;
		ModContextType replaceContext;
		if(apply) {
			findContext = ModContextType.BEFORE_HEX;
			replaceContext = ModContextType.AFTER_HEX;
		} else {
			findContext = ModContextType.AFTER_HEX;
			replaceContext = ModContextType.BEFORE_HEX;
		}
		
		int findSize, replaceSize;
		
		//retrieve type change properties from tree
		String findType = findByKeyword("OBJECT_TYPE", tree, findContext);
		if (findType.isEmpty()) {
			logger.log(Level.INFO, "No FIND object type.");
			return false;
		}
		String findSizeString = findByKeyword("SIZE", tree, findContext);
		if (findSizeString.isEmpty()) {
			logger.log(Level.INFO, "No FIND object size.");
			return false;
		}
		try {
			findSize = Integer.parseInt(findSizeString, 16);
		}
		catch (NumberFormatException ex) {
			logger.log(Level.INFO, "Invalid FIND size.", ex);
			return false;
		}
		
		String replaceType = findByKeyword("OBJECT_TYPE", tree, replaceContext);
		if (replaceType.isEmpty()) {
			logger.log(Level.INFO, "No REPLACE object type.");
			return false;
		}
		String replaceSizeString = findByKeyword("SIZE", tree, replaceContext);
		if (replaceSizeString.isEmpty()) {
			logger.log(Level.INFO, "No REPLACE object size.");
			return false;
		}
		try {
			replaceSize = Integer.parseInt(replaceSizeString, 16);
		}
		catch (NumberFormatException ex) {
			logger.log(Level.INFO, "Invalid REPLACE size.", ex);
			return false;
		}
		
		// retrieve import table references for types
		int findTypeIdx = tree.getSourceUpk().findRefByName(findType);
		if(findTypeIdx == 0) {
			logger.log(Level.INFO, "No match for FIND type in Import Table");
			return false;
		}
		if(findTypeIdx > 0) {
			logger.log(Level.INFO, "ERROR - FIND object type can only be from Import Table");
			return false;
		}
		
		int replaceTypeIdx = tree.getSourceUpk().findRefByName(replaceType);
		if(replaceTypeIdx == 0) {
			logger.log(Level.INFO, "No match for REPLACE type in Import Table");
			return false;
		}
		if(replaceTypeIdx > 0) {
			logger.log(Level.INFO, "ERROR - REPLACE object type can only be from Import Table");
			return false;
		}
		
		// attempt to find object
		int objectIdx = tree.getSourceUpk().findRefByName(tree.getFunctionName());
		if(objectIdx == 0) {
			logger.log(Level.INFO, "Object not found");
			return false;
		}
		if(objectIdx < 0) {
			logger.log(Level.INFO, "Import Objects cannot have type changed");
			return false;
		}
		
		ObjectEntry currentObjEntry = tree.getSourceUpk().getHeader().getObjectList().get(objectIdx);
		
		// verify that old values are there.
		if(currentObjEntry.getType() != findTypeIdx) {
			logger.log(Level.INFO, "FIND type mismatch - unexpected type found");
			return false;
		}
		if(currentObjEntry.getUpkSize() != findSize) {
			logger.log(Level.INFO, "FIND size mismatch - unexpected size found");
			return false;
		}
		
		// change the file entries
		try (SeekableByteChannel sbc = Files.newByteChannel(tree.getSourceUpk().getFile().toPath(), StandardOpenOption.WRITE)) {
			ByteBuffer intBuf = ByteBuffer.allocate(4);
			intBuf.order(ByteOrder.LITTLE_ENDIAN);
			int objectListPos = currentObjEntry.getObjectEntryPos();
			
			// update changed object/function's ObjectEntry size in file
			intBuf.putInt(replaceTypeIdx);
			intBuf.rewind();
			sbc.position(objectListPos + 0); // set file position -- 0 writes to the 0th word in the ObjectEntry, object type
			sbc.write(intBuf);		// write buffer
			intBuf.clear();
			
			intBuf.putInt(replaceSize);
			intBuf.rewind();
			sbc.position(objectListPos + 32); // set file position -- 32 writes to the 8th word in the ObjectEntry, object size
			sbc.write(intBuf);		// write buffer
			
			//update data in memory model of UPK
			currentObjEntry.setType(replaceTypeIdx);
			currentObjEntry.setUpkSize(replaceSize);
			
		} catch(IOException ex) {
			logger.log(Level.SEVERE, "IO Exception while writing data", ex);
			return false;
		}
		
		return true;
	}
	
	private static String findByKeyword(String keyword, ModTree tree, ModContextType context) {
		Enumeration<ModTreeNode> lines = tree.getRoot().children();
		while (lines.hasMoreElements()) {
			ModTreeNode line = lines.nextElement();
			if (line.getContextFlag(context)) {
				String lineString = line.getFullText().trim();
				if(lineString.startsWith(keyword)) {
					if(lineString.contains("=")) {
						return lineString.split("//")[0].trim().split("=")[1].trim();
					}
				}
			}
		}
		return "";
	}
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
//			sbc.write(newFunctionBuf);		// write buffer
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
//			sbc.write(intBuf);		// write buffer
		} catch(IOException ex) {
			logger.log(Level.SEVERE, "IO Failure when attempting to update Object Entry", ex);
			return false;
		}
		
		
		return true;
	}

}
