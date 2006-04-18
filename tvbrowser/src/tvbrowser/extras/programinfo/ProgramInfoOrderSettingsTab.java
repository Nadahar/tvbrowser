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
 * @author Ren� Mach
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
      if(mOldSetupState.compareTo("false") == 0)
        order = ProgramTextCreator.getDefaultOrder();
      else
        order = new Object[0];
      
      mList = new OrderChooser(order,ProgramTextCreator.getDefaultOrder(),true);
    }
    else {
      String[] id = mOldOrder.trim().split(";");
      order = new Object[id.length];
      for (int i = 0; i < order.length; i++)
        try {
          order[i] = ProgramFieldType
              .getTypeForId(Integer.parseInt((String) id[i]));
          
          if(((ProgramFieldType)order[i]).getTypeId() == ProgramFieldType.UNKOWN_FORMAT)
            order[i] = ProgramTextCreator.getDurationTypeString();
          
        } catch (Exception e) {
          order[i] = id[i];
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
    PanelBuilder builder = new PanelBuilder(new FormLayout("5dlu,pref:grow,5dlu","pref,5dlu,fill:pref:grow,5dlu,pref,50dlu,pref,5dlu"));
    builder.setDefaultDialogBorder();
    
    builder.addSeparator(ProgramInfo.mLocalizer.msg("order","Info choosing/ordering"), cc.xyw(1,1,3));
    builder.add(mList, cc.xy(2,3)); 
    builder.addSeparator("", cc.xyw(1,5,3));
    
    FormLayout layout = new FormLayout("pref,pref:grow,pref","pref");
    layout.setColumnGroups(new int[][] {{1,3}});
    JPanel buttonPn = new JPanel(layout);
    buttonPn.add(previewBtn, cc.xy(3,1));
    buttonPn.add(defaultBtn, cc.xy(1,1));
    
    builder.add(buttonPn, cc.xyw(1,7,3));
    
    return builder.getPanel();
  }

  private void resetSettings() {
    mList.setOrder(ProgramTextCreator.getDefaultOrder(),ProgramTextCreator.getDefaultOrder());
  }

  public void saveSettings() {
    Object[] o = mList.getOrder();

    String temp = "";

    for (int i = 0; i < o.length; i++)
      if (o[i] instanceof String)
        temp += ProgramFieldType.UNKOWN_FORMAT + ";";        
      else
        temp += ((ProgramFieldType) o[i]).getTypeId() + ";";

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
    // TODO Automatisch erstellter Methoden-Stub
    return null;
  }

  public String getTitle() {
    // TODO Automatisch erstellter Methoden-Stub
    return "Infoauswahl/-sortierung";
  }

}
