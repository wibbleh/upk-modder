package model.upk;

import io.parser.UpkParser;

import java.io.File;
import java.io.IOException;

/**
 * Model class for UPK files.
 *
 * @author XMS
 */
public class UpkFile {

	/**
	 * The file descriptor pointing to the *.upk file.
	 */
	private File upkFile;

	/**
	 * The header of this UPK file.
	 */
	private UpkHeader upkHeader;

	/**
	 * Constructs a UPK file model class from the specified file.
	 * @param upkFile the file descriptor pointing to the referenced *.upk file
	 */
	public UpkFile(File upkFile) {
		this.upkFile = upkFile;
		this.upkHeader = this.parseHeader(upkFile);
	}

	/**
	 * Parses the header of the specified *.upk file
	 * @param upkFile the *.upk file
	 * @return the parsed header or <code>null</code> if a parsing error occurred
	 */
	private UpkHeader parseHeader(File upkFile) {
		UpkParser parser = new UpkParser(upkFile);
		try {
			return parser.parseHeader();
		} catch(IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Returns the UPK header instance.
	 * @return the UPK header
	 */
	public UpkHeader getHeader() {
		return this.upkHeader;
	}

	/**
	 * Returns the name of this UPK file.
	 * @return the name
	 */
	public String getName() {
		String name = this.upkFile.getName();
		// return file name without extension
		return name.substring(0, name.length() - 4);
	}

	/**
	 * Returns the referenced *.upk file.
	 * @return the file descriptor pointing to the referenced *.upk file
	 */
	public File getFile() {
		return this.upkFile;
	}

	public String getRefName(int ref) {
		String s = "";
		if(ref > 0) {
			try {
				s = upkHeader.getObjectList().get(ref).getName();
			} catch(Throwable x) {
			}
		} else if(ref < 0) {
			try {
				int i = -ref;
				s = upkHeader.getImportList().get(i).getName();
			} catch(Throwable x) {
			}
		}
		return s;
	}

	public String getVFRefName(int ref) {
		String s = "";
		try {
			s = upkHeader.getNameList().get(ref).getName();
		} catch(Throwable x) {
		}
		return s;
	}

	public int findRefName(String name) {
		int index;
		if(name.contains(":")) {
			index = -upkHeader.importListStrings.indexOf(name);
		} else {
			index = upkHeader.objectListStrings.indexOf(name);
		}
		return index;
	}

	public int findVFRefName(String name) {
		return upkHeader.nameListStrings.indexOf(name);
	}

}
