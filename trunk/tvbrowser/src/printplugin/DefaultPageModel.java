package printplugin;

import devplugin.*;

import java.util.ArrayList;

public class DefaultPageModel implements PageModel {

  private Date mDate;
  private ArrayList mColumns;

  public DefaultPageModel(Date date) {
    mDate = date;
    mColumns = new ArrayList();
  }
  
  public void addChannelDayProgram(Channel channel, Program[] programArr) {
    ColumnModel col = new DefaultColumnModel(channel.getName(), programArr);
    mColumns.add(col);
  }


	public int getColumnCount() {
		return mColumns.size();
	}

	
	public ColumnModel getColumnAt(int inx) {
		return (ColumnModel)mColumns.get(inx);
	}

	
	public String getHeader() {
		return mDate.toString();
	}


	public String getFooter() {
		return "Copyright (c) by TV-Browser - http://tvbrowser.sourceforge.net";
	}
  
  
  
  
}