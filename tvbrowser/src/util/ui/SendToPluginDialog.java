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
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import tvbrowser.core.plugin.PluginProxy;

import devplugin.Plugin;
import devplugin.PluginAccess;
import devplugin.Program;

/**
 * Ein Dialog, der es erlaubt, Programme an andere Plugins weiter zu reichen
 * 
 * @author bodum
 */
public class SendToPluginDialog extends JDialog {

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

    /**
     * Create the Dialog
     * 
     * @param plug Sender-Plugin
     * @param owner Owner Frame
     * @param prg List of Programs to send
     */
    public SendToPluginDialog(Frame owner, Program[] prg) {
        super(owner, true);
        mPrograms = prg;
        createDialog();
        setLocationRelativeTo(owner);
    }

    /**
     * Create the Dialog
     * 
     * @param plug Sender-Plugin
     * @param owner Owner Frame
     * @param prg List of Programs to send
     */
    public SendToPluginDialog(Dialog owner, Program[] prg) {
        super(owner, true);
        mPrograms = prg;
        createDialog();
        setLocationRelativeTo(owner);
    }

    /**
     * Creates the Dialog
     */
    private void createDialog() {
        setTitle(mLocalizer.msg("title", "Send to other Plugin"));

        JPanel panel = (JPanel) this.getContentPane();

        panel.setLayout(new GridBagLayout());

        // get the installed plugins
        PluginAccess[] installedPluginArr = Plugin.getPluginManager().getActivatedPlugins();

        PluginAccess[] copy = new PluginProxy[installedPluginArr.length];
        
        for (int i = 0; i < installedPluginArr.length;i++) {
            copy[i] = installedPluginArr[i];
        }
        
        
        Arrays.sort(copy, new ObjectComperator());

        // create a list of those who support multiple program execution
        Vector selectablePluginList = new Vector();
        for (int i = 0; i < copy.length; i++) {
            if (copy[i].canReceivePrograms()) {
                selectablePluginList.add(copy[i]);
            }
        }

        mPluginList = new JComboBox(selectablePluginList);

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
                hide();
            }
        });

        buttonPanel.add(sendButton);

        JButton cancelButton = new JButton(mLocalizer.msg("cancel", "Cancel"));

        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                hide();
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
        PluginAccess plug = (PluginAccess) mPluginList.getSelectedItem();
        plug.receivePrograms(mPrograms);
    }

    /**
     * Comperator needed to Sort List of Plugins
     */
    private class ObjectComperator implements Comparator {

        public int compare(Object o1, Object o2) {
            return o1.toString().compareTo(o2.toString());
        }

    }
}