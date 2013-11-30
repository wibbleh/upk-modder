/*
 * Copyright (C) 2013 Rachel Norman
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package model.upk;

import io.model.upk.NameEntry;
import io.model.upk.ObjectEntry;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.List;

/**
 * Model class for an UPK file header.
 * 
 * @author Amineri, XMS
 */
public class UpkHeader {
	
	/**
	 * The name list of this UPK header.
	 */
	private List<NameEntry> nameList;
	
	/**
	 * The byte position of the name list inside the *.upk file.
	 */
	private int nameListPos;
	
	/**
	 * The objectList of this UPK header.
	 */
	private List<ObjectEntry> objectList;

	/**
	 * The byte position of the object list inside the *.upk file.
	 */
	private int objectListPos;
	
	@Deprecated
	private String m_sUpkName;

	@Deprecated
	public UpkHeader() {
	}

	public UpkHeader(List<NameEntry> nameList, int nameListPos,
			List<ObjectEntry> objectList, int objectListPos) {
		this.nameList = nameList;
		this.nameListPos = nameListPos;
		this.objectList = objectList;
		this.objectListPos = objectListPos;
	}

	public int getNameListSize() {
		return this.nameList.size();
	}

	public int getNamelistPosition() {
		return this.nameListPos;
	}

	public int getObjectListSize() {
		return this.objectList.size();
	}

	public int getObjectlistPosition() {
		return this.objectListPos;
	}

	@Deprecated
	public String getUpkName() {
		return m_sUpkName;
	}

	@Deprecated
	public void setUpkName(String sName) {
		m_sUpkName = sName;
	}

	@Deprecated
	public void parseUPKHeader(Path thisfile, boolean bVerbose)
			throws IOException {
		try (FileChannel fc = FileChannel.open(thisfile)) {
			ByteBuffer buf = ByteBuffer.allocate(100);
			buf.order(ByteOrder.LITTLE_ENDIAN);

			fc.position(0x19);
			fc.read(buf);

			int numNamelistEntries = buf.getInt(0);
			if (bVerbose)
				System.out.println("\tNamelist entries : " + numNamelistEntries);

			nameListPos = buf.getInt(4);
			if (bVerbose)
				System.out.println("\tNamelist start pos: " + nameListPos);

			int numObjectlistEntries = buf.getInt(8);
			if (bVerbose)
				System.out.println("\tObjectlist entries: " + numObjectlistEntries);

			objectListPos = buf.getInt(12);
			if (bVerbose)
				System.out.println("\tObjectlist start pos: " + objectListPos);

			buf.clear();
		} catch (IOException x) {
			System.out.println("caught exception: " + x);
		}
	}
}