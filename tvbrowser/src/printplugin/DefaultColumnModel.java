package printplugin;

import devplugin.Program;


public class DefaultColumnModel implements ColumnModel {
 
  private String mTitle;
  private Program[] mPrograms;

  public DefaultColumnModel(String title, Program[] progs) {
    mTitle = title;
    mPrograms = progs;
  }

 

	public Program getProgramAt(int inx) {
    if (inx>=0 && inx<mPrograms.length) {
      return mPrograms[inx];
    }
    return null;
	}

	
	public int getProgramCount() {
    if (mPrograms == null) {
      return 0;
    }
		return mPrograms.length;
	}

	
	public String getTitle() {
		return mTitle;
	}
  
  
  
}