/*
 * Created on 14.04.2004
 * 
 * To change the template for this generated file go to Window - Preferences -
 * Java - Code Generation - Code and Comments
 */
package tvraterplugin;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import util.ui.Localizer;
import util.ui.ProgramList;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.Program;

/**
 * Shows a List when a specific rated program will air
 * @author Bodo
 */
public class ProgramListDialog extends JDialog implements WindowClosingIf {

    /**
     * Creates the Dialog
     * @param parent ParentFrame
     * @param title Titel of Program
     */
    public ProgramListDialog(Frame parent, String title) {
        super(parent, true);
        setTitle(mLocalizer.msg("ListDialogTitle", "Programs with \"{0}\"", title));

        _title = title;

        generateList();
        createGUI();
    }

    /**
     * Creates the Dialog
     * @param parent ParentFrame
     * @param title Titel of Program
     */
    public ProgramListDialog(Dialog parent, String title) {
        super(parent, true);
        setTitle(mLocalizer.msg("ListDialogTitle", "Programs with \"{0}\"", title));

        _title = title;

        generateList();
        createGUI();
    }

    /**
     * Genereates the List of Programs
     */
    private void generateList() {
        if (_title == null) {
            return;
        }
        
        _programList = new Vector();

        Channel[] channels = Plugin.getPluginManager().getSubscribedChannels();

        Date date = new Date();
        for (int d = 0; d < 31; d++) {

            for (int i = 0; i < channels.length; i++) {
                Iterator it = Plugin.getPluginManager().getChannelDayProgram(date, channels[i]);
                while ((it != null) && (it.hasNext())) {
                    Program program = (Program) it.next();
                    if ((program != null) && (program.getTitle() != null) && (program.getTitle().equals(_title))) {
                        _programList.add(program);
                    }
                }
            }
            date = date.addDays(1);
        }

    }

    /**
     * Creates the GUI
     */
    private void createGUI() {
        UiUtilities.registerForClosing(this);
      
        JPanel content = (JPanel) this.getContentPane();
        content.setLayout(new BorderLayout());

        Program[] prg = new Program[_programList.size()];
        
        for (int i = 0; i < _programList.size(); i++) {
            prg[i] = (Program)_programList.get(i);
        }
        
        _programJList = new ProgramList(prg);

        _programJList.addMouseListeners(TVRaterPlugin.getInstance());
        
        JScrollPane scroll = new JScrollPane(_programJList);

        content.add(scroll, BorderLayout.CENTER);

        JPanel buttonPn = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        content.add(buttonPn, BorderLayout.SOUTH);

        JButton closeButton = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
        closeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                dispose();
            }
        });
        buttonPn.add(closeButton);

        getRootPane().setDefaultButton(closeButton);
        pack();
    }


    
    private String _title;

    private ProgramList _programJList;

    private Vector _programList;

    private static final Localizer mLocalizer = Localizer.getLocalizerFor(ProgramListDialog.class);

    public void close() {
      dispose();
    }
}