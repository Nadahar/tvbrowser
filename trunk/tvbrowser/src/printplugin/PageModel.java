package printplugin;



public interface PageModel {
  
  public int getColumnCount();
  public ColumnModel getColumnAt(int inx);
  public String getHeader();
  public String getFooter();
  
}