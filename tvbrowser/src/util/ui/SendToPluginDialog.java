/*
 * Created on 25.06.2004
 */
package util.ui;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import tvbrowser.core.Settings;

import com.jgoodies.forms.builder.ButtonBarBuilder2;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;

import devplugin.Plugin;
import devplugin.Program;
import devplugin.ProgramReceiveIf;
import devplugin.ProgramReceiveTarget;

/**
 * Ein Dialog, der es erlaubt, Programme an andere Plugins weiter zu reichen
 * 
 * @author bodum
 */
public class SendToPluginDialog extends JDialog implements WindowClosingIf {

  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(SendToPluginDialog.class);

  /**
   * Programs to send
   */
  private Program[] mPrograms;

  /**
   * List of Plugins
   */
  private JComboBox mPluginList;
  private JComboBox mTargetList;

  private ProgramReceiveIf mCaller;
  private ProgramReceiveTarget mCallerTarget;

  /**
   * Create the Dialog
   * 
   * @param caller
   *          Sender-Plugin
   * @param owner
   *          Owner Frame
   * @param prg
   *          List of Programs to send
   * @since 3.0
   */
  public SendToPluginDialog(ProgramReceiveIf caller, Window owner, Program[] prg) {
    this(caller, null, owner, prg);
  }

  /**
   * Create the Dialog
   * 
   * @param caller
   *          Sender-Plugin
   * @param owner
   *          Owner Frame
   * @param prg
   *          List of Programs to send
   * @deprecated since 3.0
   */
  @Deprecated
  public SendToPluginDialog(ProgramReceiveIf caller, Frame owner, Program[] prg) {
    this(caller, null, (Window) owner, prg);
  }

  /**
   * Create the Dialog
   * 
   * @param caller
   *          Sender-Plugin
   * @param owner
   *          Owner Frame
   * @param prg
   *          List of Programs to send
   * @deprecated since 3.0
   */
  @Deprecated
  public SendToPluginDialog(ProgramReceiveIf caller, Dialog owner, Program[] prg) {
    this(caller, null, (Window) owner, prg);
  }

  /**
   * Create the Dialog
   * 
   * @param caller
   *          Sender-Plugin
   * @param callerTarget
   *          The target which calls this dialog
   * @param owner
   *          Owner Frame
   * @param prg
   *          List of Programs to send
   * @deprecated since 3.0
   */
  @Deprecated
  public SendToPluginDialog(ProgramReceiveIf caller, ProgramReceiveTarget callerTarget, Frame owner, Program[] prg) {
    this(caller, callerTarget, (Window) owner, prg);
  }

  /**
   * Create the Dialog
   * 
   * @param caller
   *          Sender-Plugin
   * @param callerTarget
   *          The target which calls this dialog
   * @param owner
   *          Owner Frame
   * @param prg
   *          List of Programs to send
   * @since 3.0
   */
  public SendToPluginDialog(ProgramReceiveIf caller,
      ProgramReceiveTarget callerTarget, Window owner, Program[] prg) {
    super(owner);
    setModal(true);
    mPrograms = prg;
    mCaller = caller;
    mCallerTarget = callerTarget;
    createDialog();
    setLocationRelativeTo(owner);
  }

  /**
   * Create the Dialog
   * 
   * @param caller
   *          Sender-Plugin
   * @param callerTarget
   *          The target which calls this dialog
   * @param owner
   *          Owner Frame
   * @param prg
   *          List of Programs to send
   * @since 2.5
   * @deprecated since 3.0
   */
  @Deprecated
  public SendToPluginDialog(ProgramReceiveIf caller, ProgramReceiveTarget callerTarget, Dialog owner, Program[] prg) {
    this(caller, callerTarget, (Window) owner, prg);
  }

  /**
   * Creates the Dialog
   */
  private void createDialog() {
    setTitle(mLocalizer.msg("title", "Send to other Plugin"));

    CellConstraints cc = new CellConstraints();
    PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,0dlu:grow,5dlu","pref,5dlu,pref,5dlu,pref,5dlu,pref,fill:10dlu:grow,pref"), (JPanel)this.getContentPane());
    pb.setDefaultDialogBorder();
    
    pb.addSeparator(mLocalizer.msg("sendTo", "Send {0} programs to", mPrograms.length), cc.xyw(1,1,3));
    
    // get the installed plugins
    ProgramReceiveIf[] installedPluginArr = Plugin.getPluginManager().getReceiveIfs(mCaller,mCallerTarget);

    Arrays.sort(installedPluginArr, new ObjectComparator());
    
    mPluginList = new JComboBox(installedPluginArr);
    pb.add(mPluginList, cc.xy(2, 3));

    pb.addSeparator(mLocalizer.msg("target","Target:"), cc.xyw(1,5,3));
    
    mTargetList = new JComboBox(installedPluginArr[0]
        .getProgramReceiveTargets());
    pb.add(mTargetList, cc.xy(2, 7));
    final DefaultComboBoxModel model = (DefaultComboBoxModel)mTargetList.getModel();
    mTargetList.setEnabled(installedPluginArr[0].canReceiveProgramsWithTarget()
        && mTargetList.getItemCount() > 1);
    
    mPluginList.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if(e.getStateChange() == ItemEvent.SELECTED) {
          ProgramReceiveTarget[] targets = ((ProgramReceiveIf)e.getItem()).getProgramReceiveTargets();
          
          model.removeAllElements();
          
          if(((ProgramReceiveIf)e.getItem()).canReceiveProgramsWithTarget()) {
            for(ProgramReceiveTarget target : targets) {
              if(!target.equals(mCallerTarget)) {
                model.addElement(target);
              }
            }
            
            mTargetList.setEnabled(targets.length > 1);
          }
          else if(targets != null && targets.length > 0) {
            model.addElement(targets[0]);
            mTargetList.setEnabled(false);
          }
          
          mTargetList.repaint();
        }
      }
    });
    
    // select same plugin and target like last time
    String lastUsedPlugin = Settings.propLastUsedReceivePlugin.getString();
    if (lastUsedPlugin != null) {
      for (ProgramReceiveIf programReceiveIf : installedPluginArr) {
        if (programReceiveIf.getId().equals(lastUsedPlugin)) {
          mPluginList.setSelectedItem(programReceiveIf);
          String lastUsedTarget = Settings.propLastUsedReceiveTarget.getString();
          if (lastUsedTarget != null) {
            for (ProgramReceiveTarget target: programReceiveIf.getProgramReceiveTargets()) {
              if (target.getTargetId().equals(lastUsedTarget)) {
                mTargetList.setSelectedItem(lastUsedTarget);
              }
            }
          }
        }
      }
    }
    
    JButton sendButton = new JButton(mLocalizer.msg("send", "Send"));

    sendButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent evt) {
        send();
        setVisible(false);
      }
    });
    sendButton.setEnabled(mPrograms.length > 0);

    JButton cancelButton = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    cancelButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent evt) {
        setVisible(false);
      }
    });

    ButtonBarBuilder2 buttonBuilder = new ButtonBarBuilder2();
    buttonBuilder.addGlue();
    buttonBuilder.addButton(new JButton[] {sendButton, cancelButton});
    
    pb.add(buttonBuilder.getPanel(), cc.xyw(1,9,3));

    Settings.layoutWindow("util.sendToDialog", this, new Dimension(Sizes
        .dialogUnitXAsPixel(220, this), Sizes.dialogUnitYAsPixel(125, this)));

    UiUtilities.registerForClosing(this);
    getRootPane().setDefaultButton(sendButton);
  }

  /**
   * Sends the Data to the selected Plugin
   */
  protected void send() {

    int result = JOptionPane.YES_OPTION;
    ProgramReceiveIf plug = (ProgramReceiveIf) mPluginList.getSelectedItem();

    if (mPrograms.length > 5) {
      result = JOptionPane.showConfirmDialog(this, mLocalizer.msg("AskBeforeSend",
          "Are you really sure to send {0} programs\nto \"{1}\"?",
          mPrograms.length, plug.toString()),
          mLocalizer.msg("Attention", "Attention"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
    }

    if (result == JOptionPane.YES_OPTION) {
      ProgramReceiveTarget target = (ProgramReceiveTarget)mTargetList.getSelectedItem();
      plug.receivePrograms(mPrograms, target);
      Settings.propLastUsedReceivePlugin.setString(plug.getId());
      Settings.propLastUsedReceiveTarget.setString(target.getTargetId());
    }
  }

  /**
   * Comparator needed to Sort List of Plugins
   */
  private static class ObjectComparator implements Comparator<ProgramReceiveIf> {

    public int compare(ProgramReceiveIf o1, ProgramReceiveIf o2) {
      return o1.toString().compareTo(o2.toString());
    }

  }

  public void close() {
    setVisible(false);
  }
}