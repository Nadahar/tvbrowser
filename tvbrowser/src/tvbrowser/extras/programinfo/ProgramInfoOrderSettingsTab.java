package tvbrowser.extras.programinfo;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import util.program.ProgramTextCreator;
import util.ui.OrderChooser;

import devplugin.Plugin;
import devplugin.ProgramFieldType;
import devplugin.SettingsTab;

/**
 * The order settings for the ProgramInfo.
 * 
 * @author René Mach
 *
 */
public class ProgramInfoOrderSettingsTab implements SettingsTab {
  
  private OrderChooser mList;
  private String mOldOrder, mOldSetupState;
  
  public JPanel createSettingsPanel() {
    mOldOrder = ProgramInfo.getInstance().getProperty("order", "");
    mOldSetupState = ProgramInfo.getInstance().getProperty("setupwasdone","false");        
    
    Object[] order;
    
    if (mOldOrder.indexOf(";") == -1) {
      if(mOldSetupState.compareTo("false") == 0) {
        order = ProgramTextCreator.getDefaultOrder();
      } else {
        order = new Object[0];
      }
      
      mList = new OrderChooser(order,ProgramTextCreator.getDefaultOrder(),true);
    }
    else {
      String[] id = mOldOrder.trim().split(";");
      order = new Object[id.length];
      for (int i = 0; i < order.length; i++) {
        try {
          order[i] = ProgramFieldType
              .getTypeForId(Integer.parseInt(id[i]));
          
          if(((ProgramFieldType)order[i]).getTypeId() == ProgramFieldType.UNKOWN_FORMAT) {
            order[i] = ProgramTextCreator.getDurationTypeString();
          }
          
        } catch (Exception e) {
          order[i] = id[i];
        }
      }
      mList = new OrderChooser(order,ProgramTextCreator.getDefaultOrder(),true);
    }
    
    JButton previewBtn = new JButton(ProgramInfo.mLocalizer.msg("preview", "Prewview"));
    previewBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveSettings();
        ProgramInfo.getInstance().showProgramInformation(
            Plugin.getPluginManager().getExampleProgram(), false);
        restoreSettings();
      }
    });

    JButton defaultBtn = new JButton(ProgramInfo.mLocalizer.msg("default", "Default"));
    defaultBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        resetSettings();
      }
    });
    
    
    CellConstraints cc = new CellConstraints();
    PanelBuilder builder = new PanelBuilder(new FormLayout("5dlu,pref:grow,5dlu","5dlu,fill:pref:grow,10dlu,pref"));
    builder.setDefaultDialogBorder();
    
    builder.add(mList, cc.xy(2,2));
    
    FormLayout layout = new FormLayout("pref,pref:grow,pref","pref");
    layout.setColumnGroups(new int[][] {{1,3}});
    JPanel buttonPn = new JPanel(layout);
    buttonPn.add(previewBtn, cc.xy(3,1));
    buttonPn.add(defaultBtn, cc.xy(1,1));
        
    builder.add(buttonPn, cc.xyw(1,4,3));
    
    return builder.getPanel();
  }

  private void resetSettings() {
    mList.setOrder(ProgramTextCreator.getDefaultOrder(),ProgramTextCreator.getDefaultOrder());
  }

  public void saveSettings() {
    Object[] objects = mList.getOrder();

    String temp = "";

    for (Object object : objects) {
      if (object instanceof String) {
        temp += ProgramFieldType.UNKOWN_FORMAT + ";";
      } else {
        temp += ((ProgramFieldType) object).getTypeId() + ";";
      }
    }

    ProgramInfo.getInstance().getSettings().setProperty("order", temp);
    ProgramInfo.getInstance().getSettings().setProperty("setupwasdone", "true");
    ProgramInfo.getInstance().setOrder();
    ProgramInfo.getInstance().setLook();
  }

  private void restoreSettings() {
    ProgramInfo.getInstance().getSettings().setProperty("setupwasdone", mOldSetupState);
    ProgramInfo.getInstance().getSettings().setProperty("order", mOldOrder);
    ProgramInfo.getInstance().setOrder();
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return ProgramInfo.getInstance().toString();
  }

}
