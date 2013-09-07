package rememberme;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.factories.CC;

import util.ui.EnhancedPanelBuilder;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;

import devplugin.ProgramReceiveTarget;
import devplugin.SettingsTab;

public class RemeberMeSettingsTab implements SettingsTab {
  private JList mTagList;
  private DefaultListModel mListModel;
  private RememberMe mRememberMe;
  
  public RemeberMeSettingsTab(RememberMe rMe) {
    mRememberMe = rMe;
  }
  
  @Override
  public JPanel createSettingsPanel() {
    EnhancedPanelBuilder pb = new EnhancedPanelBuilder("5dlu,default,default:grow,default");
    
    pb.appendRow("5dlu");
    pb.appendRow("default");
    pb.addLabel(RememberMe.mLocalizer.msg("targets", "Export targets/tags"), CC.xyw(2, pb.getRowCount(), 3));
    
    pb.appendRow("fill:default:grow");
    
    mListModel = new DefaultListModel();
    
    mTagList = new JList(mListModel);
    mTagList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    mTagList.setPreferredSize(new Dimension(200,200));
    
    for(ProgramReceiveTarget target : mRememberMe.getProgramReceiveTargets()) {
      mListModel.addElement(target);
    }
    
    pb.add(mTagList, CC.xyw(2, pb.getRowCount(), 3));
    
    JButton add = new JButton(Localizer.getLocalization(Localizer.I18N_ADD), TVBrowserIcons.newIcon(TVBrowserIcons.SIZE_SMALL));
    add.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String value = JOptionPane.showInputDialog(RememberMe.mLocalizer.msg("targetsNew","Please enter new target/tag name:"));
        
        if(value != null) {
          ProgramReceiveTarget newTarget = new ProgramReceiveTarget(mRememberMe, value, "###RememberMe###"+value+"###");
          mListModel.addElement(newTarget);
        }
      }
    });
    
    final JButton delete = new JButton(Localizer.getLocalization(Localizer.I18N_DELETE), TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));
    delete.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if(mTagList.getSelectedIndex() > 0) {
          mListModel.remove(mTagList.getSelectedIndex());
        }
      }
    });
    delete.setEnabled(false);
    
    mTagList.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        if(!e.getValueIsAdjusting()) {
          delete.setEnabled(mTagList.getSelectedIndex() > 0);
        }
      }
    });
    
    pb.appendRow("5dlu");
    pb.appendRow("default");
    pb.add(add, CC.xy(2,pb.getRowCount()));
    pb.add(delete, CC.xy(4, pb.getRowCount()));
    
    return pb.getPanel();
  }

  @Override
  public void saveSettings() {
    ArrayList<ProgramReceiveTarget> targets = new ArrayList<ProgramReceiveTarget>();
    
    for(int i = 0; i < mListModel.size(); i++) {
      targets.add((ProgramReceiveTarget)mListModel.get(i));
    }
    
    mRememberMe.setReceiveTargets(targets.toArray(new ProgramReceiveTarget[targets.size()]));
  }

  @Override
  public Icon getIcon() {
    return null;
  }

  @Override
  public String getTitle() {
    return null;
  }
}
