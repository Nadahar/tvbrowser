


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