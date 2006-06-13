/*
 * Created on 25.06.2004
 */
package util.ui;

import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import devplugin.Plugin;
import devplugin.Program;
import devplugin.ProgramReceiveIf;

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

  private ProgramReceiveIf mCaller;

  /**
   * Create the Dialog
   * 
   * @param caller Sender-Plugin
   * @param owner Owner Frame
   * @param prg List of Programs to send
   */
  public SendToPluginDialog(ProgramReceiveIf caller, Frame owner, Program[] prg) {
    super(owner, true);
    mPrograms = prg;
    mCaller = caller;
    createDialog();
    setLocationRelativeTo(owner);
  }

  /**
   * Create the Dialog
   * 
   * @param caller Sender-Plugin
   * @param owner Owner Frame
   * @param prg List of Programs to send
   */
  public SendToPluginDialog(ProgramReceiveIf caller, Dialog owner, Program[] prg) {
    super(owner, true);
    mPrograms = prg;
    mCaller = caller;
    createDialog();
    setLocationRelativeTo(owner);
  }

  /**
   * Creates the Dialog
   */
  private void createDialog() {
    setTitle(mLocalizer.msg("title", "Send to other Plugin"));
    UiUtilities.registerForClosing(this);
    
    JPanel panel = (JPanel) this.getContentPane();

    panel.setLayout(new GridBagLayout());
    
    // get the installed plugins
    ProgramReceiveIf[] installedPluginArr = Plugin.getPluginManager().getReceiveIfs(mCaller);

    Arrays.sort(installedPluginArr, new ObjectComperator());

    mPluginList = new JComboBox(installedPluginArr);

    GridBagConstraints c = new GridBagConstraints();

    c.gridwidth = GridBagConstraints.REMAINDER;
    c.weightx = 0;
    c.weighty = 0;
    c.insets = new Insets(5, 5, 5, 0);
    c.anchor = GridBagConstraints.WEST;

    panel.add(new JLabel(mLocalizer.msg("sendTo", "Send programs to")), c);

    c.gridwidth = GridBagConstraints.REMAINDER;
    c.weightx = 1.0;
    c.weighty = 0;
    c.anchor = GridBagConstraints.CENTER;
    c.insets = new Insets(5, 5, 5, 5);

    panel.add(mPluginList, c);

    c.gridwidth = GridBagConstraints.REMAINDER;
    c.anchor = GridBagConstraints.SOUTHEAST;
    c.weightx = 1.0;
    c.weighty = 1.0;

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

    JButton sendButton = new JButton(mLocalizer.msg("send", "Send"));

    sendButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent evt) {
        send();
        setVisible(false);
      }
    });

    buttonPanel.add(sendButton);

    JButton cancelButton = new JButton(mLocalizer.msg("cancel", "Cancel"));

    cancelButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent evt) {
        setVisible(false);
      }
    });

    buttonPanel.add(cancelButton);

    c.insets = new Insets(5, 5, 5, 0);
    panel.add(buttonPanel, c);

    pack();
    setResizable(false);
  }

  /**
   * Sends the Data to the selected Plugin
   */
  protected void send() {

    int result = JOptionPane.YES_OPTION;
    ProgramReceiveIf plug = (ProgramReceiveIf) mPluginList.getSelectedItem();

    if (mPrograms.length > 5) {
      result = JOptionPane.showConfirmDialog(this, mLocalizer.msg("AskBeforeSend",
          "Are you really sure to sent {0} Programs\nto \"{1}\"?", new Integer(mPrograms.length), plug.toString()),
          mLocalizer.msg("Attention", "Attention"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
    }

    if (result == JOptionPane.YES_OPTION) {
      plug.receivePrograms(mPrograms);
    }
  }

  /**
   * Comperator needed to Sort List of Plugins
   */
  private class ObjectComperator implements Comparator<ProgramReceiveIf> {

    public int compare(ProgramReceiveIf o1, ProgramReceiveIf o2) {
      return o1.toString().compareTo(o2.toString());
    }

  }

  /*
   * (non-Javadoc)
   * @see util.ui.WindowClosingIf#close()
   */
  public void close() {
    setVisible(false);
  }  
}