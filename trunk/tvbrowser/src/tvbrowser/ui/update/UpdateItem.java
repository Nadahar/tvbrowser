/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */
 

package tvbrowser.ui.update;

import java.util.ArrayList;
import java.net.*;
import java.io.*;


public class UpdateItem {
	
	private String mName;
	private int mType;
	private ArrayList mVersionList;
	private VersionItem mSelectedItem;
	
	public static final int PLUGIN=0, DATASERVICE=1, TVBROWSER=2;
	
	
	public UpdateItem(String name, int type) {
		mName=name;
		mType=type;
		mVersionList=new ArrayList();
		mSelectedItem=null;
		
	}
	
	public void addVersionItem(VersionItem vi) {
		mVersionList.add(vi);
	}
	
	
	public boolean equals(Object o) {
		if (o instanceof UpdateItem) {
			UpdateItem ui=(UpdateItem)o;
			return ui.mName.equals(mName) && ui.mType==mType;
		}
		return false;
		
	}
	
	public String getName() {
		return mName;
	}
	
	public int getType() {
		return mType;
	}
	
	
	public VersionItem[] getVersions() {
		Object[] objList=mVersionList.toArray();
		VersionItem[] result=new VersionItem[objList.length];
		for (int i=0;i<result.length;i++) {
			result[i]=(VersionItem)objList[i];
		}		
		return result;
	}
	
	public int getNumOfVersions() {
		return mVersionList.size();
	}
	
	public void removeIncompatibleVersions(devplugin.Version requiredVersion) {
		java.util.Iterator it=mVersionList.iterator();
		while (it.hasNext()) {
			VersionItem vi=(VersionItem)it.next();
			if (vi.getRequired()!=null && vi.getRequired().compareTo(requiredVersion)==1) {
				it.remove();
			}
		}
	}
	
	public void selectVersion(VersionItem item) {
		mSelectedItem=item;
	}
	
	public VersionItem getSelectedVersion() {
		return mSelectedItem;
	}
	
	public boolean isSelected() {
		return (mSelectedItem!=null);
	}
	
	public void download() throws IOException {
		if (mSelectedItem!=null) {
			URL sourceURL=new URL(mSelectedItem.getSource());
			File targetFile=null;
			if (mType==UpdateItem.PLUGIN) {
				targetFile=new File("plugins",mName+".jar.inst");
			}else{
				targetFile=new File("tvdataservice",mName+".jar.inst");
			}
			util.io.IOUtilities.download(sourceURL,targetFile);
		}
		
	}
	
	
}