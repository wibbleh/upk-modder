package util.unrealhex;

import io.model.upk.ObjectEntry;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import model.modtree.ModContext.ModContextType;
import model.modtree.ModTree;
import model.modtree.ModTreeNode;
import model.upk.UpkFile;

/**
 * Utility class for consolidating hex and performing apply/revert operations to upks
 * @author Amineri
 */
public class HexSearchAndReplace {
	
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
		
		//allocate buffer as large as we need
//		ByteBuffer fileBuf = ByteBuffer.allocate(hex.length);
		ByteBuffer fileBuf = ByteBuffer.allocate((int) functLength); // read entire target
		
		//open channel to upk for read-only
		SeekableByteChannel sbc = Files.newByteChannel(upk.getFile().toPath(), StandardOpenOption.READ);
		
		sbc.position(functPos); // set file position
		sbc.read(fileBuf); // read entire search space
		String searchSpace = new String(fileBuf.array()); // allocate buffer into String
		
		int findIndex = searchSpace.indexOf(new String(hex)); // search for instance
		if(findIndex <0) { // failure
			return findIndex;
		} else { // success
			return findIndex + functPos;
		}
		
		// replaced with above -- remove after some more testing done : 19/12/2013 - amineri
//		long endSearch = functPos + functLength - hex.length;
//		for (long currPos = functPos; currPos < endSearch; currPos++) {
//			
//			// TODO: search code could probably be done faster with a match method, but I couldn't get it to work
//			// TODO: @Amineri how about using 'new String(bytes).indexOf(new String(hex))' on a block of UPK bytes?
//			// TODO: @Amineri we could also implement Knuth-Morris-Pratt or Boyer-Moore algorithm for maximum pattern matching performance
//			boolean bMatch = true;
//			sbc.position(currPos); // set file position
//			sbc.read(fileBuf);
//			for (int jCount = 0; jCount < hex.length; jCount++) {
//				if (fileBuf.get(jCount) != hex[jCount]) {
//					bMatch = false;
//					break;
//				}
//			}
//			if (bMatch) {
//				replaceOffset = currPos;
//				break;
//			}
//			fileBuf.clear();
//		}
//		return replaceOffset;
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
		
		//open channel to upk for read-only
		SeekableByteChannel sbc = Files.newByteChannel(upk.getFile().toPath(), StandardOpenOption.WRITE);
		sbc.position(filePos);
		sbc.write(fileBuf);
	}
	
}
