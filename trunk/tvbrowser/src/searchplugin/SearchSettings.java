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


package searchplugin;

import java.io.*;

public class SearchSettings {
	
	private String searchTxt;
	private boolean searchInTitle, searchInInfoText, caseSensitive;
	private int option;
	public final static int EXACTLY=0, KEYWORD=1, REGULAR_EXPRESSION=2;
	
	
	public SearchSettings(String searchTxt) {
		this.searchTxt=searchTxt;
		searchInTitle=true;
		searchInInfoText=false;
		caseSensitive=false;
		option=EXACTLY;
	}
	
	public SearchSettings(ObjectInputStream in) throws IOException, ClassNotFoundException {
		searchTxt=(String)in.readObject();
		searchInTitle=in.readBoolean();
		searchInInfoText=in.readBoolean();
		caseSensitive=in.readBoolean();
		option=in.readInt();
	}
	
	public void writeData(ObjectOutputStream out) throws IOException {
		out.writeObject(searchTxt);
		out.writeBoolean(searchInTitle);
		out.writeBoolean(searchInInfoText);
		out.writeBoolean(caseSensitive);
		out.writeInt(option);
	}
	
	public String toString() {
		return searchTxt;
	}
	
	public boolean getSearchInTitle() {
		return searchInTitle;
	}
	public boolean getSearchInInfoText() {
		return searchInInfoText;
	}
	public boolean getCaseSensitive() {
		return caseSensitive;
	}
	public int getOption() {
		return option;
	}
	
	public void setSearchInTitle(boolean val) {
		searchInTitle=val;
	}
	
	public void setSearchInInfoText(boolean val) {
		searchInInfoText=val;
	}
	
	public void setCaseSensitive(boolean val) {
		caseSensitive=val;
	}
	
	public void setOption(int val) {
		option=val;
	}
}