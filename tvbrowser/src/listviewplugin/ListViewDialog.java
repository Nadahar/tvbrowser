/*
 * Created on 11.04.2004
 */
package listviewplugin;

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
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.Program;

/**
 * Creates a Dialog with a List of Programs
 * 
 * TODO: Whats running in 15 min, at 22 o'clock
 * TODO: refresh View
 * 
 * @author bodo
 */
public class ListViewDialog extends JDialog {

    /**
     * Creates the Dialog
     * 
     * @param frame Frame for modal
     * @param plugin Plugin for reference
     */
    public ListViewDialog(Frame frame, Plugin plugin) {
        super(frame, true);
        setTitle(mLocalizer.msg("viewList", "View List:"));

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
        for (int i = 0; i < channels.length; i++) {
            Iterator it = Plugin.getPluginManager().getChannelDayProgram(date, channels[i]);
            while ((it != null) && (it.hasNext())) {
                Program program = (Program) it.next();
                if (program.isOnAir()) {
                    _programList.add(program);
                }
            }
        }

    }

    /**
     * Creates the GUI
     */
    private void createGUI() {
        JPanel content = (JPanel) this.getContentPane();
        content.setLayout(new BorderLayout());
        content.add(new JLabel(mLocalizer.msg("nowRunning", "Now Running:")), BorderLayout.NORTH);

        _programJList = new JList(_programList);

        _programJList.setCellRenderer(new ListViewProgramCellRenderer());

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
    }

    /** The localizer used by this class. */
    private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ListViewDialog.class);

    private JList _programJList;

    private Vector _programList;
}