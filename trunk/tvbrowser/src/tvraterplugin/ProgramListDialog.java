/*
 * Created on 14.04.2004
 * 
 * To change the template for this generated file go to Window - Preferences -
 * Java - Code Generation - Code and Comments
 */
package tvraterplugin;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import listviewplugin.ListViewPlugin;
import util.ui.Localizer;
import util.ui.ProgramListCellRenderer;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.Program;

/**
 * Shows a List when a specific rated program will air
 * @author Bodo
 */
public class ProgramListDialog extends JDialog {

    /**
     * Creates the Dialog
     * @param parent ParentFrame
     * @param rating
     */
    public ProgramListDialog(Frame parent, Rating rating) {
        super(parent, true);
        setTitle(mLocalizer.msg("ListDialog", "Listening"));

        _rating = rating;

        generateList();
        createGUI();
    }

    /**
     * Genereates the List of Programs
     */
    private void generateList() {
        _programList = new Vector();

        Channel[] channels = Plugin.getPluginManager().getSubscribedChannels();

        Date date = new Date();
        for (int d = 0; d < 31; d++) {

            for (int i = 0; i < channels.length; i++) {
                Iterator it = Plugin.getPluginManager().getChannelDayProgram(date, channels[i]);
                while ((it != null) && (it.hasNext())) {
                    Program program = (Program) it.next();
                    if (program.getTitle().equals(_rating.getTitle())) {
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
        JPanel content = (JPanel) this.getContentPane();
        content.setLayout(new BorderLayout());

        _programJList = new JList(_programList);

        _programJList.setCellRenderer(new ProgramListCellRenderer());

        _programJList.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int inx = _programJList.locationToIndex(e.getPoint());
                    Program p = (Program) _programJList.getModel().getElementAt(inx);
                    _programJList.setSelectedIndex(inx);
                    JPopupMenu menu = devplugin.Plugin.getPluginManager().createPluginContextMenu(p,
                            ListViewPlugin.getInstance());
                    menu.show(_programJList, e.getX() - 15, e.getY() - 15);
                } else if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == 2)) {
                    int inx = _programJList.locationToIndex(e.getPoint());
                    Program p = (Program) _programJList.getModel().getElementAt(inx);

                    Plugin plugin = devplugin.Plugin.getPluginManager().getDefaultContextMenuPlugin();
                    if (plugin != null) {
                        plugin.execute(p);
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(_programJList);

        content.add(scroll, BorderLayout.CENTER);

        JPanel buttonPn = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        content.add(buttonPn, BorderLayout.SOUTH);

        JButton okButton = new JButton(mLocalizer.msg("ok", "OK"));
        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                dispose();
            }
        });
        buttonPn.add(okButton);
        pack();
    }


    
    private Rating _rating;

    private JList _programJList;

    private Vector _programList;

    private static final Localizer mLocalizer = Localizer.getLocalizerFor(ProgramListDialog.class);
}