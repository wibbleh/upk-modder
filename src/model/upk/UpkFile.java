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
	private File file;

	/**
	 * The header of this UPK file.
	 */
	private UpkHeader header;

	/**
	 * Constructs a UPK file model class from the specified file.
	 * @param upkFile the file descriptor pointing to the referenced *.upk file
	 */
	public UpkFile(File upkFile) {
		this.file = upkFile;
		this.header = this.parseHeader(upkFile);
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
		return this.header;
	}

	/**
	 * Returns the name of this UPK file.
	 * @return the name
	 */
	public String getName() {
		String name = this.file.getName();
		// return file name without extension
		return name.substring(0, name.length() - 4);
	}

	/**
	 * Returns the referenced *.upk file.
	 * @return the file descriptor pointing to the referenced *.upk file
	 */
	public File getFile() {
		return this.file;
	}

	public String getRefName(int ref) {
		String s = "";
		if(ref > 0) {
			try {
				s = header.getObjectList().get(ref).getName();
			} catch(Throwable x) {
			}
		} else if(ref < 0) {
			try {
				int i = -ref;
				s = header.getImportList().get(i).getName();
			} catch(Throwable x) {
			}
		}
		return s;
	}

	public String getVFRefName(int ref) {
		String s = "";
		try {
			s = header.getNameList().get(ref).getName();
		} catch(Throwable x) {
		}
		return s;
	}

	/**
	 * Returns the reference value corresponding to the specified name string.
	 * @param name the name to look up
	 * @return the reference value
	 */
	public int findRefByName(String name) {
		int index;
		if (name.contains(":")) {
			index = -header.importListStrings.indexOf(name);
			if (index > 0) {
				index = 0;
			}
		} else {
			index = header.objectListStrings.indexOf(name);
			if (index < 0) {
				index = 0;
			}
		}
		return index;
	}

	/**
	 * Returns the virtual function reference value corresponding to the
	 * specified name string.
	 * @param name the name to look up
	 * @return the virtual function reference value
	 */
	public int findVFRefByName(String name) {
		return header.nameListStrings.indexOf(name);
	}

}
