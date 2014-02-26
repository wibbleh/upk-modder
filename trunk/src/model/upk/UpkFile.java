package model.upk;

import io.parser.UpkParser;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Model class for UPK files.
 *
 * @author XMS
 */
public class UpkFile {

	/**
	 * The pathr pointing to the *.upk file.
	 */
//	private File file;
	private Path upkPath;

	/**
	 * The header of this UPK file.
	 */
	private UpkHeader header;

	/**
	 * Constructs a UPK file model class from the specified file.
	 * @param upkPath the path pointing to the referenced *.upk file
	 */
	public UpkFile(Path upkPath) {
		this.upkPath = upkPath;
		this.header = this.parseHeader(upkPath);
	}

	/**
	 * Parses the header of the specified *.upk file
	 * @param upkPath the *.upk file path
	 * @return the parsed header or <code>null</code> if a parsing error occurred
	 */
	private UpkHeader parseHeader(Path upkPath) {
		UpkParser parser = new UpkParser(upkPath);
		try {
			return parser.parseHeader();
		} catch(IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Reloads the upk header information from the file
	 */
	public void reload()
	{
		this.header = this.parseHeader(this.upkPath);
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
		String name = upkPath.getFileName().toString();
		// return file name without extension
		return name.substring(0, name.length() - 4);
	}

	/**
	 * Returns the referenced *.upk file.
	 * @return the file descriptor pointing to the referenced *.upk file
	 */
	public Path getPath() {
		return this.upkPath;
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
