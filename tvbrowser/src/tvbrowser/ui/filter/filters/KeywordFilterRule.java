package tvbrowser.ui.filter.filters;

import javax.swing.*;
import java.awt.*;
import java.util.regex.*;


import java.io.*;

class SearchSetting {
 public static final int TITLE=0, INFO=1, ANY=2;
 public String keyword="";
 public int area=TITLE;
 public boolean caseSensitive;
 
    
    
 public SearchSetting() {
        
 }
 public SearchSetting(String kw, int a, boolean cs) {
     keyword=kw;
     area=a;
     caseSensitive=cs;
 }
 
 public SearchSetting(ObjectInputStream in) {
     try {
         int version=in.readInt();
         keyword=(String)in.readObject();
         area=in.readInt();
         caseSensitive=in.readBoolean();
     }catch (IOException e) {
         util.exc.ErrorHandler.handle("Could not read filter rule from file", e);
     }catch (ClassNotFoundException e) {
         util.exc.ErrorHandler.handle("Could not read filter rule from file", e);
     }
 }
 
 public void store(ObjectOutputStream out) {
     try {
         out.writeInt(1);
         out.writeObject(keyword);
         out.writeInt(area);
         out.writeBoolean(caseSensitive);         
     }catch (IOException e) {
         util.exc.ErrorHandler.handle("Could not write filter rule to file", e);
     }
     
 }
 
 
}

public class KeywordFilterRule extends FilterRule {
    
    private JRadioButton mTitleRadio, mInfoRadio, mAnyRadio;
    private JTextField mKeywordTF;
    private JCheckBox mCaseSensitiveCkB;
    private Pattern mPattern;
    
    private SearchSetting mSearchSettings;
    
    public KeywordFilterRule(String name, String description) {
        super(name, description);
        mSearchSettings=new SearchSetting();     
    }
    
    public KeywordFilterRule(ObjectInputStream in) {
        try {
            int version=in.readInt();
            mName=(String)in.readObject();
            mDescription=(String)in.readObject();
            mSearchSettings=new SearchSetting(in);    
        }catch (IOException e) {
         util.exc.ErrorHandler.handle("Could not read filter rule from file", e);
     }catch (ClassNotFoundException e) {
         util.exc.ErrorHandler.handle("Could not read filter rule from file", e);
     }
     
     int flags=0;
        
     if (! mSearchSettings.caseSensitive) {
                 flags |= Pattern.CASE_INSENSITIVE;
     }
     try {
        mPattern = Pattern.compile(".*\\Q" + mSearchSettings.keyword + "\\E.*", flags);
     }catch (PatternSyntaxException exc) {
     }
     
     
    }
    
    public void store(ObjectOutputStream out) {
        try {
            out.writeInt(1);
            out.writeObject(mName);
            out.writeObject(mDescription);
            mSearchSettings.store(out);            
        }catch (IOException e) {
            util.exc.ErrorHandler.handle("Could not write keyword filter to file", e); 
        }
    }
    
  
    public void ok() {
        if (mTitleRadio.isSelected()) {
            mSearchSettings.area=SearchSetting.TITLE;    
        }else if (mInfoRadio.isSelected()) {
            mSearchSettings.area=SearchSetting.INFO;
        }else {
            mSearchSettings.area=SearchSetting.ANY;
        }   
        mSearchSettings.keyword=mKeywordTF.getText();
        mSearchSettings.caseSensitive=mCaseSensitiveCkB.isSelected();        


        int flags=0;
        
        if (! mSearchSettings.caseSensitive) {
            flags |= Pattern.CASE_INSENSITIVE;
        }
       
        try {
            mPattern = Pattern.compile(".*\\Q" + mSearchSettings.keyword + "\\E.*", flags);
        }catch (PatternSyntaxException exc) {
        
        }

    }
    
    
    public boolean accept(devplugin.Program program) {
        
        boolean searchInTitle=mSearchSettings.area==SearchSetting.TITLE;
        boolean searchInInfo=mSearchSettings.area==SearchSetting.INFO;
        boolean searchInBoth=mSearchSettings.area==SearchSetting.ANY;
        
        if (searchInBoth) {
            searchInTitle=true;
            searchInInfo=true;
        }
        return tvbrowser.core.DataService.getInstance().search(program,mPattern,searchInTitle,searchInInfo);
        
       
       
    }
    
    public JPanel getPanel() {
        if (mPanel==null) {
            mPanel=new JPanel();
            mPanel.setLayout(new BoxLayout(mPanel,BoxLayout.Y_AXIS));
        
            JPanel keywordPanel=new JPanel(new BorderLayout());
            keywordPanel.add(new JLabel("Keyword:"),BorderLayout.WEST);
            mKeywordTF=new JTextField(mSearchSettings.keyword);
            keywordPanel.add(mKeywordTF,BorderLayout.CENTER);
            mCaseSensitiveCkB=new JCheckBox("case sensitive");
            mCaseSensitiveCkB.setSelected(mSearchSettings.caseSensitive);
            keywordPanel.add(mCaseSensitiveCkB,BorderLayout.EAST);
        
            mPanel.add(keywordPanel);
        
            JPanel searchPanel=new JPanel(new BorderLayout());
            JPanel panel1=new JPanel();
            panel1.setLayout(new GridLayout(0,1));
            panel1.setBorder(BorderFactory.createTitledBorder("Must be found in..."));
            panel1.setMinimumSize(new Dimension(300,10));
            mTitleRadio=new JRadioButton("title");
            mInfoRadio=new JRadioButton("info");
            mAnyRadio=new JRadioButton("any");
            panel1.add(mTitleRadio);
            panel1.add(mInfoRadio);
            panel1.add(mAnyRadio);
            
            if (mSearchSettings.area==SearchSetting.TITLE) {
                mTitleRadio.setSelected(true);
            }else if (mSearchSettings.area==SearchSetting.INFO) {
                mInfoRadio.setSelected(true);
            }else {
                mAnyRadio.setSelected(true);
            }
        
            ButtonGroup group = new ButtonGroup();
            group.add(mTitleRadio);
            group.add(mInfoRadio);
            group.add(mAnyRadio);
        
            searchPanel.add(panel1,BorderLayout.CENTER);
            mPanel.add(searchPanel);            
        }
        return mPanel;
    }
    
    public String toString() {
        return "keyword";
    }
}
