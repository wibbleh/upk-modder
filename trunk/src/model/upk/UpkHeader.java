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

import io.model.upk.ImportEntry;
import io.model.upk.NameEntry;
import io.model.upk.ObjectEntry;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import static model.modtree.ModTree.logger;

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

        /**
	 * The importList of this UPK header.
	 */
	private List<ImportEntry> importList;

	/**
	 * The byte position of the import list inside the *.upk file.
	 */
	private int importListPos;
        
	/**
	 * Searchable list of basic names.
         * TODO : construct object that can be searched faster
	 */
	public ArrayList<String> nameListStrings;

	/**
	 * Searchable list of import names.
         * TODO : construct object that can be searched faster
	 */
	public ArrayList<String> importListStrings;

	/**
	 * Searchable list of import names.
         * TODO : construct object that can be searched faster
	 */
	public ArrayList<String> objectListStrings;

	/**
	 * The GUID (Global Unique ID).
	 */
	private byte[] aGUID = new byte[16];

	@Deprecated
	private String m_sUpkName;

	@Deprecated
	public UpkHeader() {
	}

	public UpkHeader(List<NameEntry> nameList, int nameListPos,
			List<ObjectEntry> objectList, int objectListPos,
			List<ImportEntry> importList, int importListPos, byte[] aGUID) {
		this.nameList = nameList;
		this.nameListPos = nameListPos;
		this.objectList = objectList;
		this.objectListPos = objectListPos;
		this.importList = importList;
		this.importListPos = importListPos;
		this.aGUID = aGUID;
		long startTime = System.currentTimeMillis();
		constructImportNames(importList);
		logger.log(Level.FINE, "Constructed import list names, took " + (System.currentTimeMillis() - startTime) + "ms");
		startTime = System.currentTimeMillis();
		constructObjectNames(objectList);
		logger.log(Level.FINE, "Constructed object list names, took " + (System.currentTimeMillis() - startTime) + "ms");
		startTime = System.currentTimeMillis();
		constructNames(nameList);
		logger.log(Level.FINE, "Constructed searchable names, took " + (System.currentTimeMillis() - startTime) + "ms");
	}

        private void constructNames(List<NameEntry> list){
            nameListStrings = new ArrayList<>(list.size());
            for(int i = 0; i < list.size(); i++){
                nameListStrings.add(nameList.get(i).getName());
            }
        }
        
        private void constructObjectNames(List<ObjectEntry> list) {
            int iPrevOuterIndex, iOuterIndex;

            objectListStrings = new ArrayList(list.size());
            objectListStrings.add("");
            for(int i = 1; i < list.size(); i++) {
                String name;
                iPrevOuterIndex = -1;
                name = "";
                name += nameList.get(list.get(i).getNameIdx()).getName();
                iOuterIndex = list.get(i).getOuterIdx();
                while ((iOuterIndex <= list.size())
                                && (iOuterIndex > 0)
                                && (iOuterIndex != i)
                                && (iPrevOuterIndex != iOuterIndex))  {
                    name += "@" + nameList.get(list.get(iOuterIndex).getNameIdx()).getName();
                    iPrevOuterIndex = iOuterIndex;
                    iOuterIndex = list.get(iOuterIndex).getOuterIdx();
                }
                objectListStrings.add(name);
                list.get(i).setName(name);
            }
        }
        
	private void constructImportNames(List<ImportEntry> list) {
		int iPrevOuterIndex, iOuterIndex;

		importListStrings = new ArrayList(list.size());
		importListStrings.add("");
		for(int i = 1; i < list.size(); i ++) {
			String name;
			iPrevOuterIndex = -1;
			if(list.get(i).getPackageIdx() != 0) {
				name = nameList.get(list.get(i).getPackageIdx()).getName() + ":";
			} else {
				name = "";
			}
			name += nameList.get(list.get(i).getNameIdx()).getName();
			iOuterIndex =  - list.get(i).getOuterIdx();
			while((iOuterIndex <= list.size())
					&& (iOuterIndex > 0)
					&& (iOuterIndex != i)
					&& (iPrevOuterIndex != iOuterIndex)) {
				name += "@" + nameList.get(list.get(iOuterIndex).getNameIdx()).getName();
				iPrevOuterIndex = iOuterIndex;
				iOuterIndex =  - list.get(iOuterIndex).getOuterIdx();
			}
			importListStrings.add(name);
			list.get(i).setName(name);
//			System.out.println(i + " : " + name);
		}
	}

	public byte[] getGUID() {
		return this.aGUID;
	}
	
	/**
	 * Returns the namelist.
	 * @return the namelist
	 */
	public List<NameEntry> getNameList() {
		return this.nameList;
	}
        
	public int getNameListSize() {
		return this.nameList.size();
	}

	public int getNamelistPosition() {
		return this.nameListPos;
	}
	
	/**
	 * Returns the objectlist.
	 * @return the objectlist
	 */
	public List<ObjectEntry> getObjectList() {
		return this.objectList;
	}

	public int getObjectListSize() {
		return this.objectList.size();
	}

	public int getObjectlistPosition() {
		return this.objectListPos;
	}
	
	/**
	 * Returns the importlist.
	 * @return the importlist
	 */
	public List<ImportEntry> getImportList() {
		return this.importList;
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