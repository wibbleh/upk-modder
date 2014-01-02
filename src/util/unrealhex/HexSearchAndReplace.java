package util.unrealhex;

import static model.modtree.ModTree.logger;
import io.model.upk.ObjectEntry;

import java.io.FileNotFoundException;
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
import model.modtree.ModTreeNode;
import model.upk.UpkFile;
import ui.ApplyStatus;

/**
 * Utility class for consolidating hex and performing apply/revert operations to upks
 * @author Amineri
 */
public class HexSearchAndReplace {
	
	// TODO: wrong logger is used here (statically imported from ModTree), create own logger instance
	
	/**
	 * Parses the tree and finds any hex that is part of a [BEFORE] block
	 * Each block is returned as a separate byte[] in the list
	 * @param tree the tree to extract hex from
	 * @return List of byte arrays containing hex, or null if there is none
	 */
	public static List<byte[]> consolidateBeforeHex(ModTree tree) {
		return consolidateHex(tree, ModContextType.BEFORE_HEX);
	}
	
	/**
	 * Parses the tree and finds any hex that is part of an [AFTER] block
	 * Each block is returned as a separate byte[] in the list
	 * @param tree the tree to extract hex from
	 * @return List of byte arrays containing hex, or null if there is none
	 */
	public static List<byte[]> consolidateAfterHex(ModTree tree) {
		return consolidateHex(tree, ModContextType.AFTER_HEX);
	}
	
	/**
	 * Parses the tree and finds any hex that is part of any context block.
	 * Can perform "on the fly" replacement of reference names into reference values using the current target upk within the supplied tree.
	 * Each block is returned as a separate byte[] in the list
	 * @param tree the tree to extract hex from
	 * @param context the context to search for
	 * @return List of byte arrays containing hex, or null if there is none
	 */
	protected static List<byte[]> consolidateHex(ModTree tree, ModContextType context) {
		List<byte[]> hexBlocks = new ArrayList<>();
		List<Byte> currentBlock = new ArrayList<>();
		Enumeration<ModTreeNode> lines = tree.getRoot().children();
		while (lines.hasMoreElements()) {
			ModTreeNode line = lines.nextElement();
			if (line.getContextFlag(context)) {
				if (line.isValidHexLine()) {
					String hex = line.toHexStringArray()[1];
					String[] tokens = hex.split("\\s+");
					// TODO: @Amineri, idea for further simplification, split at pipe character and parse non-reference token chains using HexStringLibrary.convertStringToByteArray()
					for (String token : tokens) {
//						if (token.startsWith("{|") && (upk != null)) {
						if ((tree.getTargetUpk() != null) && token.matches("^([{<]\\|)")) {
							String contents = token.substring(2, token.length() - 2);
							int value = token.startsWith("{|")
									? tree.getTargetUpk().findRefByName(contents)
									: tree.getTargetUpk().findVFRefByName(contents);
							if (value == 0) {
								return null;
							}
							byte[] bytes = HexStringLibrary.convertIntToByteArray(value);
							for (byte b : bytes) {
								currentBlock.add(b);
							}
						} else {
							try {
								currentBlock.add((byte) Integer.parseInt(token, 16));
							} catch (NumberFormatException x) {
								return null;
							}
						}
					}
				}
			} else { // found line without required context. if current block is
						// non-empty stop adding to it and start a new block
				if (!currentBlock.isEmpty()) {
					hexBlocks.add(HexStringLibrary.convertByteListToByteArray(currentBlock));
					currentBlock.clear();
				}
			}
		}
		return hexBlocks;
	}

	/**
	 * Finds the specified hex within the target upk stored within the given tree.
	 * Scope of the search is limited to the function in the associated tree.
	 * The entire search hex is loaded into memory, so this is not appropriate for extreme cases.
	 * The largest upk in XCOM is ~44MB, and the largest modded is ~19 MB, 
	 *						so there should be no problems for this application.
	 * @param pattern hex to search for
	 * @param tree provides function to limit scope of search
	 * @return file offset of found hex, or -1 if not found
	 * @throws IOException
	 */
	public static long findFilePosition(byte[] pattern, ModTree tree) throws IOException {
        long replaceOffset;
        
		// find possible destination
		
		// retrieve function name from tree
		String targetFunction = tree.getFunctionName().trim();
		
		// retrieve objectlist index from upk -- this is the same as references but is not named such here
		int objectIndex = tree.getTargetUpk().findRefByName(targetFunction);
		if(objectIndex == 0)
			return -1;
		
		// retrieve object entry
		ObjectEntry functionEntry = tree.getTargetUpk().getHeader().getObjectList().get(objectIndex);
		
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
		
		// read search area fully and use KMPMatch to search for the pattern
		// tested by performing "test status" on entire Long War project with no errors
		ByteBuffer fileBuf = ByteBuffer.allocate((int) functLength);  // allocate enough space to read entire search space
		
		// open channel to upk for read-only to read in search space
		try (SeekableByteChannel sbc = Files.newByteChannel(tree.getTargetUpk().getPath(), StandardOpenOption.READ)) {
			sbc.position(functPos);
			sbc.read(fileBuf);
			
		}
		byte[] searchSpace = fileBuf.array();
		replaceOffset = KMPMatch.indexOf(searchSpace, pattern);
		if (replaceOffset >=0) {
			replaceOffset += functPos;
		}
		
		//allocate buffer as large as we need
//		ByteBuffer fileBuf = ByteBuffer.allocate(hex.length);
//		try (SeekableByteChannel sbc = Files.newByteChannel(tree.getTargetUpk().getPath(), StandardOpenOption.READ)) {
//			long endSearch = functPos + functLength - hex.length;
//			for (long currPos = functPos; currPos <= endSearch; currPos++) {
//				
//				// TODO: search code could probably be done faster with a match method, but I couldn't get it to work
//				// TODO: @Amineri how about using 'new String(bytes).indexOf(new String(hex))' on a block of UPK bytes?
//				// TODO: @Amineri we could also implement Knuth-Morris-Pratt or Boyer-Moore algorithm for maximum pattern matching performance
//				boolean bMatch = true;
//				sbc.position(currPos); // set file position
//				sbc.read(fileBuf);
//				for (int jCount = 0; jCount < hex.length; jCount++) {
//					if (fileBuf.get(jCount) != hex[jCount]) {
//						bMatch = false;
//						break;
//					}
//				}
//				if (bMatch) {
//					replaceOffset = currPos;
//					break;
//				}
//				fileBuf.clear();
//			}
//		}
		return replaceOffset;
	}
	
	/**
	 * Branches on the three current type of apply operations (stored in the currTree)
	 * 1) Basic same-size search and replace
	 * 2) Resize object and search and replace
	 * 3) Change to Header Table Entry (currently only variable type change supported)
	 * @param apply true to attempt to apply changes, false to revert
	 * @param currTree the tree on which to attempt to apply/revert
	 * @return true if changes applied successfully, false if not
	 * @XTMS -- the key here is that there can be multiple non-adjacent before/after blocks
	 * see AIAddNewObjectives@XGStrategyAI.upk_mod in the sample project
	 *      -- a few lines at the end of the function are changed, as well as the header
	 */
	public static boolean applyRevertChanges(boolean apply, ModTree currTree) {
		try {
			if (currTree.getAction().equals("")) {
				// default action of making changes to object
				if (currTree.getResizeAmount() == 0) {
					// basic search and replace without file backup
					if (HexSearchAndReplace.searchAndReplace(apply, currTree)) {
						if(apply) {
							logger.log(Level.INFO, "AFTER Hex Installed");
						} else {
							logger.log(Level.INFO, "BEFORE Hex Installed");
						}
						return true;
					}
				} else {
					// advanced search and replace resizing function (many changes to upk)
					if (resizeAndReplace(apply, currTree)) {
						if(apply) {
							logger.log(Level.INFO, "Function resized and AFTER Hex Installed");
						} else {
							logger.log(Level.INFO, "Function resized and BEFORE Hex Installed");
						}
						return true;
					}
				}
			} else {
				// perform special action
				// TODO: replace within enumeration?
				if (currTree.getAction().equalsIgnoreCase("typechange")) {
					if (changeObjectType(apply, currTree)) {
						if (apply) {
							logger.log(Level.INFO, "Variable type changed to AFTER");
						} else {
							logger.log(Level.INFO, "Variable type changed to BEFORE");
						}
						return true;
					}
				}
			}
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "File error", ex);
		}
		return false;
	}

	/**
	 * Searches the associated UPK file for the byte data of the <code>BEFORE</code> or AFTER
	 * block(s) and overwrites it using the byte data of the <code>AFTER</code> or BEFORE block(s).
	 * @param apply true if applying (BEFORE->AFTER), false if reverting (AFTER->BEFORE)
	 * @param currTree the tree to which the changes are made
	 * @return true if S&R was successful, false otherwise
	 * @throws java.io.IOException
	 */
	protected static boolean searchAndReplace(boolean apply, ModTree currTree) throws IOException {
		List<byte[]> patterns;
		List<byte[]> replacements;
		if(apply) {
			patterns = consolidateHex(currTree, ModContextType.BEFORE_HEX);
			replacements = consolidateHex(currTree, ModContextType.AFTER_HEX);
		} else {
			patterns = consolidateHex(currTree, ModContextType.AFTER_HEX);
			replacements = consolidateHex(currTree, ModContextType.BEFORE_HEX);
		}
		// perform error checking first
		long[] filePositions = testBeforeAndAfterBlocks(patterns, replacements, currTree);
		if (filePositions == null) {
			return false;
		}

		// everything matches, time to make the change(s)
		for(int i = 0 ; i < filePositions.length; i++) {
			HexSearchAndReplace.applyHexChange(replacements.get(i), currTree.getTargetUpk(), filePositions[i]);
		}
		return true;
	}

	/**
	 * Sequence of tests on the supplied list of patterns and replacements.
	 * Does error checking to determine if there are any expected problems with the apply/revert operation being requested.
	 * Performs tests under the assumption of same-size apply/revert operation.
	 * @param patterns List of byte arrays that are expected to be found in the file
	 * @param replacements List of byte arrays that are expected to be written to the file
	 * @param currTree the current tree model of the modfile
	 * @return an array of absolute file positions found, or null if any error occurred
	 * @throws IOException
	 */
	protected static long[] testBeforeAndAfterBlocks(List<byte[]> patterns, List<byte[]> replacements, ModTree currTree) throws IOException {
		// TODO: move to HexSearchAndReplace class
		// perform simple error checking first
		// check for same number of blocks
		if (patterns.size() != replacements.size()) {
			logger.log(Level.INFO, "Block count mismatch");
			return null;
		}
		// check each block has same number of bytes
		long[] filePositions = new long[patterns.size()];
		for (int i = 0; i < patterns.size() ; i++) {
			if (patterns.get(i).length != replacements.get(i).length) {
				logger.log(Level.INFO, "Block " + i + " bytecount mismatch. FIND = " 
					+ patterns.get(i).length + ", REPLACE = " 
					+ replacements.get(i).length);
				return null;
			}
		}
		// try and find each pattern blocks position
		for (int j = 0; j < patterns.size(); j++) {
			long filePos = HexSearchAndReplace.findFilePosition(patterns.get(j), currTree);
			if (filePos == -1) {
				logger.log(Level.INFO, "Block " + j + " FIND not found");
				return null;
			} else {
				filePositions[j]= filePos;
			}
		}
		return filePositions;
	}

	/**
	 * Unchecked primitive write operation that writes changes to target upk.
	 * Should only be called after other error-checking operations have taken place.
	 * @param hex the byte array to be written 
	 * @param upk the file to be written to
	 * @param filePos the absolute file position within the file to write to
	 * @throws IOException
	 */
	private static void applyHexChange(byte[] hex, UpkFile upk, long filePos) throws IOException {
		// allocate buffer as large as we need and wrap the hex to write
		ByteBuffer fileBuf = ByteBuffer.wrap(hex);
		try (SeekableByteChannel sbc = Files.newByteChannel(upk.getPath(), StandardOpenOption.WRITE)) {
			sbc.position(filePos);
			sbc.write(fileBuf);
		}
	}
	
	/**
	 * Performs non-destructive testing on the supplied tree to determine its current application status.
	 * Measured relative to its associated target upk (stored internally to the tree)
	 * @param tree The tree model of the modfile to test
	 * @return ApplyStatus enum indicating test result
	 */
	public static ApplyStatus testFileStatus(ModTree tree) {
		
		if (tree.getTargetUpk() == null) {
			return ApplyStatus.UNKNOWN;
		}

		// TODO: test install status for mods that alter Table Entries (as opposed to objects)
		if(!tree.getAction().isEmpty()) {
			return testSpecialStatus(tree);
		}
		
		// consolidate BEFORE hex
		List<byte[]> beforeHex = consolidateBeforeHex(tree);
		
		//consolidate AFTER hex
		List<byte[]> afterHex = consolidateAfterHex(tree);

		if (beforeHex.size() != afterHex.size()) {
			return ApplyStatus.APPLY_ERROR;
		}
		
		boolean foundSomeBefore = false;  // will be true if any BEFORE blocks are found
		boolean missingSomeBefore = false; // will be true if any BEFORE blocks are not found
		boolean foundSomeAfter = false;  // will be true if any AFTER blocks are found
		boolean missingSomeAfter = false;
		long beforePos, afterPos;
		for (int i = 0 ; i < beforeHex.size() ; i ++ ) {
			try {
				beforePos = findFilePosition(beforeHex.get(i), tree);
				afterPos = findFilePosition(afterHex.get(i), tree);
			} catch (IOException ex) {
				logger.log(Level.SEVERE, "IO Exception: ", ex);
				return ApplyStatus.APPLY_ERROR;
			}
			
			if ((beforePos < 0) && (afterPos < 0)) {
				// both blocks not found, return error
				return ApplyStatus.APPLY_ERROR;
			}
			if ((beforePos >= 0) && (afterPos < 0)) {
				// found before block and not after
				foundSomeBefore = true;
				missingSomeAfter = true;
			}

			if ((beforePos < 0) && (afterPos >= 0)) {
				// found after block and not before
				foundSomeAfter = true;
				missingSomeBefore = true;
			}

			if ((beforePos >= 0) && (afterPos >= 0)) {
				// matched both before and after blocks... this is probably an error
				foundSomeAfter = true;
				foundSomeBefore = true;
			}
		}

		if (foundSomeBefore && foundSomeAfter) {
			return ApplyStatus.MIXED_STATUS;
		}

		if (foundSomeBefore && !missingSomeBefore && !foundSomeAfter) {
			return ApplyStatus.BEFORE_HEX_PRESENT;
		}

		if (foundSomeAfter && !missingSomeAfter && !foundSomeBefore) {
			return ApplyStatus.AFTER_HEX_PRESENT;
		}
				
		return ApplyStatus.APPLY_ERROR;
	}
	
	/**
	 * Test status of special operations.
	 * Currently supported:
	 *     Action=typechange
	 * @param tree the ModTree to be tested
	 * @return Application status
	 */
	private static ApplyStatus testSpecialStatus (ModTree tree) {
		if (tree.getAction().equalsIgnoreCase("typechange")) {
			int beforeSize=-1, afterSize=-1; 
			//retrieve type change properties from tree from BEFORE section
			String beforeType = findByKeyword("OBJECT_TYPE", tree, ModContextType.BEFORE_HEX);
			String beforeSizeString = findByKeyword("SIZE", tree, ModContextType.BEFORE_HEX);
			if(!beforeSizeString.isEmpty()) {
				try {
					beforeSize = Integer.parseInt(beforeSizeString, 16);
				}
				catch (NumberFormatException ex) {
					logger.log(Level.INFO, "Invalid BEFORE size.", ex);
				}
			}
			//retrieve type change properties from tree from AFTER section
			String afterType = findByKeyword("OBJECT_TYPE", tree, ModContextType.AFTER_HEX);
			String afterSizeString = findByKeyword("SIZE", tree, ModContextType.AFTER_HEX);
			if(!afterSizeString.isEmpty()) {
				try {
					afterSize = Integer.parseInt(afterSizeString, 16);
				}
				catch (NumberFormatException ex) {
					logger.log(Level.INFO, "Invalid AFTER size.", ex);
				}
			}
			// retrieve import table references for types
			int beforeTypeIdx = tree.getTargetUpk().findRefByName(beforeType);
			if(beforeTypeIdx == 0) {
				logger.log(Level.INFO, "No match for BEFORE type in Import Table");
			}
			if(beforeTypeIdx > 0) {
				logger.log(Level.INFO, "ERROR - BEFORE object type can only be from Import Table");
			}
			// retrieve import table references for types
			int afterTypeIdx = tree.getTargetUpk().findRefByName(afterType);
			if(afterTypeIdx == 0) {
				logger.log(Level.INFO, "No match for AFTER type in Import Table");
			}
			if(afterTypeIdx > 0) {
				logger.log(Level.INFO, "ERROR - AFTER object type can only be from Import Table");
			}
			
			// attempt to find object
			int objectIdx = tree.getTargetUpk().findRefByName(tree.getFunctionName());
			if(objectIdx == 0) {
				logger.log(Level.INFO, "Object not found");
				return ApplyStatus.APPLY_ERROR;
			}
			if(objectIdx < 0) {
				logger.log(Level.INFO, "Import Objects cannot have type changed");
				return ApplyStatus.APPLY_ERROR;
			}
			
			ObjectEntry currentObjEntry = tree.getTargetUpk().getHeader().getObjectList().get(objectIdx);
			
			boolean beforeFound = true;
			// verify that old values are there.
			if(currentObjEntry.getType() != beforeTypeIdx) {
				beforeFound = false;
			}
			if(currentObjEntry.getUpkSize() != beforeSize) {
				beforeFound = false;
			}
			
			boolean afterFound = true;
			// verify that old values are there.
			if(currentObjEntry.getType() != afterTypeIdx) {
				afterFound = false;
			}
			if(currentObjEntry.getUpkSize() != afterSize) {
				afterFound = false;
			}
			
			if(beforeFound && afterFound) {
				return ApplyStatus.MIXED_STATUS;
			}
			if(beforeFound) {
				return ApplyStatus.BEFORE_HEX_PRESENT;
			}
			if(afterFound) {
				return ApplyStatus.AFTER_HEX_PRESENT;
			}
			return ApplyStatus.APPLY_ERROR;
		}
			
		return ApplyStatus.UNKNOWN;
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
	 * @return
	 */
	public static boolean resizeAndReplace(boolean apply, ModTree tree) {
		boolean success = true;
		
		if(tree.getFileVersion() < 4) {
			logger.log(Level.INFO, "Modfile version does not support resize operations");
			return false;
		}
		
		int currentObjectIndex = tree.getTargetUpk().findRefByName(tree.getFunctionName());
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
			findHexList = consolidateHex(tree, ModContextType.BEFORE_HEX);
			replaceHexList = consolidateHex(tree, ModContextType.AFTER_HEX);
		} else {
			findHexList = consolidateHex(tree, ModContextType.AFTER_HEX);
			replaceHexList = consolidateHex(tree, ModContextType.BEFORE_HEX);
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
			filePosition = findFilePosition(findHex, tree);
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
		Path newPath = copyAndReplaceUpk((int) filePosition, findHex, replaceHex , tree.getTargetUpk());
		if(!Files.exists(newPath)){
			logger.log(Level.INFO, "Failure during copyAndReplace");
			return false;
		}
		logger.log(Level.INFO, "Resize: inserted new hex, took " + (System.currentTimeMillis() - startTime) + "ms");


		startTime = System.currentTimeMillis();

		
		try (SeekableByteChannel sbc = Files.newByteChannel(tree.getTargetUpk().getPath(), StandardOpenOption.WRITE)) {
			ByteBuffer intBuf = ByteBuffer.allocate(4);
			intBuf.order(ByteOrder.LITTLE_ENDIAN);
			
			// update changed object/function's ObjectEntry size in file
			ObjectEntry currObjectEntry = tree.getTargetUpk().getHeader().getObjectList().get(currentObjectIndex);
			long objectListPos = currObjectEntry.getObjectEntryPos();
			int objectSize = currObjectEntry.getUpkSize();
			intBuf.putInt(objectSize + resizeAmount);
			intBuf.rewind();
			sbc.position(objectListPos + 32); // set file position -- 32 writes to the 8th word in the ObjectEntry, object size
			sbc.write(intBuf);		// write buffer
			
			// update changes object/function's ObjectEntry size in memory
			currObjectEntry.setUpkSize(objectSize + resizeAmount);
		
			// If size altered, adjusts object list positions in new upk file
			if(resizeAmount != 0) {
				for(int i = 1 ; i < tree.getTargetUpk().getHeader().getObjectListSize() ; i++) { // for every object in the object list

					currObjectEntry = tree.getTargetUpk().getHeader().getObjectList().get(i);
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
		
		logger.log(Level.INFO, "Resize: rewrote object table, took " + (System.currentTimeMillis() - startTime) + "ms");
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
	private static Path copyAndReplaceUpk(int filePosition, byte[] findHex, byte[] replaceHex , UpkFile upk) {
		
		Path origPath = upk.getPath();
		
		// verify that oldHex is at FilePosition 
		ByteBuffer fileBuf = ByteBuffer.allocate(findHex.length);
		try (SeekableByteChannel sbc = Files.newByteChannel(origPath, StandardOpenOption.READ)) {
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
		
		// Rename upk to .bak version
		// verify that filename ends with ".upk"
		String origFilename = origPath.toAbsolutePath().toString();
		if (!origFilename.endsWith(".upk")) {
			logger.log(Level.INFO, "Target file not valid upk");
			return null;
		}
		String backupFileName = origFilename.replace(".upk", ".bak");
		Path backupPath = Paths.get(backupFileName);
		
		try {
			Files.move(origPath, backupPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
		} catch(IOException ex) {
			logger.log(Level.INFO, "Failed to create backup file", ex);
			return null;
		} catch(Exception ex) {
			logger.log(Level.INFO, "Failed to create backup file", ex);
			return null;
		}
		try {
			// create new file with same name as original upk
			Files.createFile(origPath);
		} catch(IOException ex) {
			logger.log(Level.SEVERE, "Could not create new upk file", ex);
			return null;
		}
		
		// benchmark comparisons : http://java.dzone.com/articles/file-copy-java-%E2%80%93-benchmark
		// NIO copy method
		try (FileChannel source = FileChannel.open(backupPath, StandardOpenOption.READ);
				FileChannel destination = FileChannel.open(origPath, StandardOpenOption.WRITE)) {

			// copy all hex from start to filePosition from backup file to new file
			destination.transferFrom(source, 0, filePosition);
			destination.position(filePosition);

			// Write replacement bytes to new File
			ByteBuffer replaceHexBuf = ByteBuffer.wrap(replaceHex);
			destination.write(replaceHexBuf);
			filePosition += findHex.length;

			// Copy remaining bytes from backup file to new file
			source.position(filePosition);
			destination.transferFrom(source, destination.size(), Long.MAX_VALUE);
			
		} catch (FileNotFoundException ex) {
			logger.log(Level.SEVERE, "File not found during copy", ex);
			return null;
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "IO Error during file copy", ex);
			return null;
		}

		return origPath;
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
		if (apply) {
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
		int findTypeIdx = tree.getTargetUpk().findRefByName(findType);
		if(findTypeIdx == 0) {
			logger.log(Level.INFO, "No match for FIND type in Import Table");
			return false;
		}
		if(findTypeIdx > 0) {
			logger.log(Level.INFO, "ERROR - FIND object type can only be from Import Table");
			return false;
		}
		
		int replaceTypeIdx = tree.getTargetUpk().findRefByName(replaceType);
		if(replaceTypeIdx == 0) {
			logger.log(Level.INFO, "No match for REPLACE type in Import Table");
			return false;
		}
		if(replaceTypeIdx > 0) {
			logger.log(Level.INFO, "ERROR - REPLACE object type can only be from Import Table");
			return false;
		}
		
		// attempt to find object
		int objectIdx = tree.getTargetUpk().findRefByName(tree.getFunctionName());
		if(objectIdx == 0) {
			logger.log(Level.INFO, "Object not found");
			return false;
		}
		if(objectIdx < 0) {
			logger.log(Level.INFO, "Import Objects cannot have type changed");
			return false;
		}
		
		ObjectEntry currentObjEntry = tree.getTargetUpk().getHeader().getObjectList().get(objectIdx);
		
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
		try (SeekableByteChannel sbc = Files.newByteChannel(tree.getTargetUpk().getPath(), StandardOpenOption.WRITE)) {
			ByteBuffer intBuf = ByteBuffer.allocate(4);
			intBuf.order(ByteOrder.LITTLE_ENDIAN);
			long objectListPos = currentObjEntry.getObjectEntryPos();
			
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
				if (lineString.startsWith(keyword)) {
					if (lineString.contains("=")) {
						return lineString.split("//")[0].trim().split("=")[1]
								.trim();
					}
				}
			}
		}
		return "";
	}
	/**
	 * Copies and appends a given function block to the end of the supplied upk
	 * Currently not implemented through UI
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
		try (SeekableByteChannel sbc = Files.newByteChannel(upk.getPath(), StandardOpenOption.READ)) {
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
		try (SeekableByteChannel sbc = Files.newByteChannel(upk.getPath(), StandardOpenOption.APPEND)) {
			newFunctPos = (int) sbc.position();
//			sbc.write(newFunctionBuf);		// write buffer
		} catch(IOException ex) {
			logger.log(Level.SEVERE, "IO Failure when attempting to append enlarged function function", ex);
			return false;
		}
		
		// fix up object list entry to point to new location
		long objectListPos = functionEntry.getObjectEntryPos();
		ByteBuffer intBuf = ByteBuffer.allocate(8);
		intBuf.order(ByteOrder.LITTLE_ENDIAN);
		intBuf.putInt(functLength + bytesToAdd + 24);
		intBuf.putInt(newFunctPos);
		intBuf.rewind();
		
		// append new function
		try (SeekableByteChannel sbc = Files.newByteChannel(upk.getPath(), StandardOpenOption.WRITE)) {
			sbc.position(objectListPos + 32); // set file position
//			sbc.write(intBuf);		// write buffer
		} catch(IOException ex) {
			logger.log(Level.SEVERE, "IO Failure when attempting to update Object Entry", ex);
			return false;
		}
		
		
		return true;
	}

}
