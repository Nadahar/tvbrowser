package switchplugin;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableModel;

import util.paramhandler.ParamHelpDialog;
import util.ui.Localizer;
import util.ui.UiUtilities;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Channel;
import devplugin.SettingsTab;

/**
 * The settings tab for the SwitchPlugin.
 * 
 * @author René Mach
 *
 */
public class SwitchPluginSettingsTab implements SettingsTab {

  private static Localizer mLocalizer = Localizer.getLocalizerFor(SwitchPluginSettingsTab.class);
  
  private ChannelTable mChannelTable;
  private JTextField mApp,mContext,mQuestion;
  private JTextArea mParaArea;
  private JCheckBox mAsk;
  private JTabbedPane mTabbedPane;
  
  private static int CURRENT_TAB_INDEX = 0;
  
  public JPanel createSettingsPanel() {
    SwitchPlugin switchPlugin = SwitchPlugin.getInstance();
    
    mApp = new JTextField();
    mApp.setText(switchPlugin.getProperty("app","").trim());  
    
    JButton file = new JButton(mLocalizer.msg("file","File"));
    file.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.showDialog(UiUtilities.getBestDialogParent(SwitchPlugin.getInstance().getSuperFrame()),"Programmauswahl");
        
        if(chooser.getSelectedFile() != null && chooser.getSelectedFile().isFile())
          mApp.setText(chooser.getSelectedFile().toString());
          
      }
    }); 
    
    mParaArea = new JTextArea();
    mParaArea.setText(switchPlugin.getProperty("para","").trim());    
    mParaArea.setLineWrap(true);
    
    JScrollPane areaScrollPane = new JScrollPane(mParaArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    
    JButton paraHelp = new JButton(mLocalizer.msg("help","Help"));
    paraHelp.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        java.awt.Window w = UiUtilities.getLastModalChildOf(SwitchPlugin.getInstance().getSuperFrame());
        ParamHelpDialog helpDlg;
        
        if(w instanceof JDialog) {
          helpDlg = new ParamHelpDialog((JDialog)w, new SwitchParamLibrary());
        }
        else {
          helpDlg = new ParamHelpDialog((JFrame)w, new SwitchParamLibrary());
        }
        
        helpDlg.setLocationRelativeTo(w);
        helpDlg.setVisible(true);
      }
    });
    
    
    String[] names = {mLocalizer.msg("internal","Internal channel name"),
        mLocalizer.msg("external","External channel name"),};
    mChannelTable = new ChannelTable(SwitchPlugin.getInstance().getTableEntries(),names);

    JScrollPane tableScrollPane = new JScrollPane(mChannelTable);    
    tableScrollPane.setBorder(null);
    
    mAsk = new JCheckBox(mLocalizer.msg("ask","Ask before execution"));
    mAsk.setSelected(switchPlugin.getProperty("ask","true").equals("true"));
    
    mQuestion = new JTextField(switchPlugin.getProperty("question",SwitchPlugin.mLocalizer.msg("qmsg","hould the external application be executed now?")));    
    mContext = new JTextField(switchPlugin.getProperty("context",SwitchPlugin.mLocalizer.msg("run","Switch")));
    
    CellConstraints cc = new CellConstraints();
    PanelBuilder program = new PanelBuilder(new FormLayout("5dlu,pref:grow,5dlu,pref,5dlu",
        "5dlu,pref,top:pref,10dlu,pref,top:pref,fill:pref:grow,5dlu"));    
    
    program.addLabel(mLocalizer.msg("app","External application") + ":", cc.xyw(2,2,3));
    program.add(mApp, cc.xy(2,3));
    program.add(file, cc.xy(4,3));
    program.addLabel(mLocalizer.msg("para","Parameters") + ":", cc.xyw(2,5,3));
    program.add(areaScrollPane, cc.xywh(2,6,1,2));
    program.add(paraHelp, cc.xy(4,6));
    
    PanelBuilder misc = new PanelBuilder(new FormLayout("5dlu,pref,5dlu,pref:grow,5dlu","5dlu,pref,pref,5dlu,pref"));
    misc.add(mAsk, cc.xyw(2,2,3));
    
    final JLabel label = misc.addLabel(mLocalizer.msg("question","Question value: "), cc.xy(2,3));
    misc.add(mQuestion, cc.xy(4,3));
    
    misc.addLabel(mLocalizer.msg("context","Context menu value: "), cc.xy(2,5));
    misc.add(mContext, cc.xy(4,5));
    
    mAsk.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        label.setEnabled(mAsk.isSelected());
        mQuestion.setEnabled(mAsk.isSelected());
      }
    });
    
    mAsk.getChangeListeners()[0].stateChanged(null);
    
    mTabbedPane = new JTabbedPane();
    mTabbedPane.setBorder(Borders.DIALOG_BORDER);        
    mTabbedPane.addTab(mLocalizer.msg("appli","Application/Parameters"),program.getPanel());
    mTabbedPane.addTab(mLocalizer.msg("channels","Channels"),tableScrollPane);
    mTabbedPane.addTab(mLocalizer.msg("misc","Misc"),misc.getPanel());
    
    mTabbedPane.setSelectedIndex(CURRENT_TAB_INDEX);

    JPanel p = new JPanel(new GridLayout());
    p.add(mTabbedPane);

    return p;
  }

  public void saveSettings() {
    if(mChannelTable.isEditing())
      mChannelTable.getCellEditor().stopCellEditing();
    
    TableModel model = mChannelTable.getModel();    
    StringBuffer buffer = new StringBuffer();
    
    for(int i = 0; i < model.getRowCount(); i++) {
      buffer.append(((Channel)model.getValueAt(i,0)).getDefaultName()).append(";").append(model.getValueAt(i,1) == null || ((String)model.getValueAt(i,1)).trim().length() == 0 ? null : model.getValueAt(i,1)).append(";");
    }
    
    System.out.println(buffer);
    
    CURRENT_TAB_INDEX = mTabbedPane.getSelectedIndex();
    
    SwitchPlugin.getInstance().setProperty("channels", buffer.toString());
    SwitchPlugin.getInstance().setProperty("app", mApp.getText().trim());
    SwitchPlugin.getInstance().setProperty("para", mParaArea.getText().trim());
    SwitchPlugin.getInstance().setProperty("ask", String.valueOf(mAsk.isSelected()));
    SwitchPlugin.getInstance().setProperty("context", mContext.getText());
    SwitchPlugin.getInstance().setProperty("question", mQuestion.getText());
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return null;
  }
}
