/*
 * Created on 11.04.2004
 */
package clipboardplugin;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Calendar;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import util.ui.ImageUtilities;
import util.ui.ProgramList;
import util.ui.SendToPluginDialog;
import util.ui.UiUtilities;
import devplugin.Plugin;
import devplugin.Program;

/**
 * Creates a Dialog with a List of Programs
 * 
 * @author bodo
 */
public class ClipboardDialog extends JDialog {

    /** The localizer used by this class. */
    private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ClipboardDialog.class);

    /** JList component */
    private ProgramList mProgramJList;

    /** ParentFrame */
    private Frame mFrame;

    /** Vector with Programs */
    private Vector mClipList;

    /** Plugin that called this Dialog */
    private Plugin mPlugin;

    /**
     * Creates the Dialog
     * 
     * @param frame Frame for modal
     * @param plugin Plugin for reference
     * @param clipboard
     */
    public ClipboardDialog(Frame frame, Plugin plugin, Vector clipboard) {
        super(frame, true);
        setTitle(mLocalizer.msg("viewList", "View List:"));
        mClipList = clipboard;
        mFrame = frame;
        mPlugin = plugin;
        createGUI();
    }

    /**
     * Returns the current Time in minutes
     * 
     * @return Time in minutes
     */
    private int getCurrentTime() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
    }

    /**
     * Creates the GUI
     */
    private void createGUI() {
        JPanel content = (JPanel) this.getContentPane();
        content.setLayout(new BorderLayout());
        content.setBorder(UiUtilities.DIALOG_BORDER);
        
        mProgramJList = new ProgramList(mClipList);

        mProgramJList.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int inx = mProgramJList.locationToIndex(e.getPoint());
                    if (inx == -1) { return; }
                    Program p = (Program) mProgramJList.getModel().getElementAt(inx);
                    mProgramJList.setSelectedIndex(inx);
                    JPopupMenu menu = devplugin.Plugin.getPluginManager().createPluginContextMenu(p);
                    menu.show(mProgramJList, e.getX() - 15, e.getY() - 15);
                } else if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == 2)) {
                    int inx = mProgramJList.locationToIndex(e.getPoint());

                    if (inx == -1) { return; }
                    Program p = (Program) mProgramJList.getModel().getElementAt(inx);

                    devplugin.Plugin.getPluginManager().handleProgramDoubleClick(p);
                }
            }
        });
        
        JScrollPane scroll = new JScrollPane(mProgramJList);

        content.add(scroll, BorderLayout.CENTER);

        JPanel buttonRight = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0, 5, 5, 0);
        c.anchor = GridBagConstraints.NORTH;
        c.gridwidth = GridBagConstraints.REMAINDER;

        JButton upButton = new JButton(new ImageIcon("imgs/up16.gif"));
        upButton.setToolTipText(mLocalizer.msg("up", "Moves the selected program up"));
        upButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                upItems();
            }
        });
        buttonRight.add(upButton, c);

        JButton downButton = new JButton(new ImageIcon("imgs/down16.gif"));
        downButton.setToolTipText(mLocalizer.msg("down", "Moves the selected program down"));
        downButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                downItems();
            }
        });
        buttonRight.add(downButton, c);

        c.weighty = 1.0;

        JButton deleteButton = new JButton(ImageUtilities.createImageIconFromJar("clipboardplugin/Delete16.gif", getClass()));
        deleteButton.setToolTipText(mLocalizer.msg("delete", "Deletes the selected program"));

        deleteButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                deleteItems();
            }
        });

        buttonRight.add(deleteButton, c);

        content.add(buttonRight, BorderLayout.EAST);

        JButton sendButton = new JButton();

        sendButton.setToolTipText(mLocalizer.msg("send", "Send Program to another Plugin"));
        sendButton.setIcon(new ImageIcon("imgs/SendToPlugin.png"));
        sendButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                showSendDialog();
            }

        });

        JPanel buttonPn = new JPanel(new BorderLayout());
        buttonPn.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        content.add(buttonPn, BorderLayout.SOUTH);

        buttonPn.add(sendButton, BorderLayout.WEST);

        JButton closeButton = new JButton(mLocalizer.msg("close", "Close"));
        closeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                dispose();
            }
        });

        
        
        buttonPn.add(closeButton, BorderLayout.EAST);
        getRootPane().setDefaultButton(closeButton);

    }

    /**
     * Move Item Down
     */
    protected void downItems() {
        moveSelectedItems(1);
    }

    /**
     * Move Item Up
     */
    protected void upItems() {
        moveSelectedItems(-1);
    }

    /**
     * Moves the selected Items
     * 
     * @param nrRows # Rows to Move
     */
    protected void moveSelectedItems(int nrRows) {
        int[] selection = mProgramJList.getSelectedIndices();
        if (selection.length == 0) return;

        for (int i = 0; i < selection.length; i++) {
            if (selection[i] >= mClipList.size()) {
                mProgramJList.setSelectedIndex(-1);
                return;
            }
        }
        
        int insertPos = selection[0] + nrRows;
        if (insertPos < 0) {
            insertPos = 0;
        }
        if (insertPos > mClipList.size() - selection.length) {
            insertPos = mClipList.size() - selection.length;
        }

        // Markierte Zeilen merken und entfernen
        Object[] selectedRows = new Object[selection.length];
        for (int i = selectedRows.length - 1; i >= 0; i--) {
            selectedRows[i] = mClipList.elementAt(selection[i]);
          	mClipList.removeElementAt(selection[i]);
        }

        // Zeilen wieder einf�gen
        for (int i = 0; i < selectedRows.length; i++) {
            mClipList.insertElementAt(selectedRows[i], insertPos + i);
        }

        // Zeilen markieren
        mProgramJList.getSelectionModel().setSelectionInterval(insertPos, insertPos + selection.length - 1);

        // Scrollen
        int scrollPos = insertPos;
        if (nrRows > 0) scrollPos += selection.length;
        mProgramJList.ensureIndexIsVisible(scrollPos);
        mProgramJList.updateUI();
    }

    /**
     * Delete-Button was pressed.
     */
    protected void deleteItems() {
        if ((mClipList.size() == 0) || (mProgramJList.getSelectedIndex() >= mClipList.size())) { return; }

        Object[] obj = mProgramJList.getSelectedValues();
        for (int i = 0; i < obj.length; i++) {
            mClipList.remove(obj[i]);
            Program prg = (Program) obj[i];
            prg.unmark(mPlugin);
        }

        if (mClipList.size() == 0) {
            hide();
        }

        mProgramJList.updateUI();
    }

    /**
     * Shows the Send-Dialog
     * 
     * @param dialog
     */
    private void showSendDialog() {
        Program[] prgList = new Program[mClipList.size()];

        for (int i = 0; i < mClipList.size(); i++) {
            prgList[i] = (Program) mClipList.get(i);
        }

        SendToPluginDialog send = new SendToPluginDialog(this, prgList);

        send.show();
    }

}