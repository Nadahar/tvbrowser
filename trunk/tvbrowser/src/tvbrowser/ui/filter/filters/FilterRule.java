
package tvbrowser.ui.filter.filters;
import javax.swing.*;


public abstract class FilterRule implements tvbrowser.ui.programtable.ProgramFilter {
 
    private int mType;
    protected String mName, mDescription;
    
    protected java.util.Properties mSettings;
    protected JPanel mPanel;
    
    public FilterRule() {
    }
    
    public FilterRule(String name, String description) {
        mName=name;
        mDescription=description;
        mSettings=new java.util.Properties();
    }
    
    public FilterRule(java.io.ObjectInputStream in) {
        
    }
    
    public void setName(String name) {
        mName=name.trim().replace(' ','_');
    }
    
    public void setDescription(String desc) {
        mDescription=desc;
    }
    
    public void setType(int type) {
        mType=type;
    }
    
    public String getName() {
        return mName;
    }
    
    public String getDescription() {
        return mDescription;
    }
    
    public abstract String toString();
    
    public abstract void ok();
    
    public abstract void store(java.io.ObjectOutputStream out);
    
    //public abstract java.util.Properties getSettings();
    
    //public abstract void setSettings(java.util.Properties settings);
    
    public abstract JPanel getPanel();
    
    abstract public boolean accept(devplugin.Program program);    
}


