package idontwant2see;

import java.util.ArrayList;

import devplugin.Date;
import devplugin.Program;

/**
 * @author Bananeweizen
 *
 */
public class IDontWant2SeeSettings {

  private boolean mSimpleMenu = true;
  private boolean mSwitchToMyFilter = true;
  private String mLastEnteredExclusionString = "";
  private Date mLastUsedDate = Date.getCurrentDate();
  private ArrayList<IDontWant2SeeListEntry> mSearchList = new ArrayList<IDontWant2SeeListEntry>();

  //TODO After 3.0 release use value from Program class
  private byte mProgramImportance = 3;

  public IDontWant2SeeSettings() {
  }

  public void setSimpleMenu(final boolean value) {
    mSimpleMenu = value;
  }

  public boolean isSimpleMenu() {
    return mSimpleMenu;
  }

  public boolean isSwitchToMyFilter() {
    return mSwitchToMyFilter;
  }

  public void setSwitchToMyFilter(final boolean value) {
    mSwitchToMyFilter = value;
  }

  public String getLastEnteredExclusionString() {
    return mLastEnteredExclusionString;
  }

  public void setLastEnteredExclusionString(final String value) {
    mLastEnteredExclusionString = value;
    IDontWant2See.getInstance().clearCache();
  }

  public void setLastUsedDate(final Date value) {
    mLastUsedDate = value;    
  }

  public Date getLastUsedDate() {
    return mLastUsedDate;
  }

  public ArrayList<IDontWant2SeeListEntry> getSearchList() {
    return mSearchList;
  }

  public void setSearchList(final ArrayList<IDontWant2SeeListEntry> value) {
    mSearchList = value;
    IDontWant2See.getInstance().clearCache();
  }

  public byte getProgramImportance() {
    return mProgramImportance;
  }
  
  public void setProgramImportance(final byte programImportance) {
    mProgramImportance = programImportance;
  }

	public void showAgain(Program program) {
    final int index = IDontWant2See.getInstance().getSearchTextIndexForProgram(program);
    if (index >= 0) {
    	getSearchList().remove(index);
    }
	}
}
