/*
 * CapturePlugin by Andreas Hessel (Vidrec@gmx.de), Bodo Tasche
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */
package captureplugin.drivers.defaultdriver.configpanels;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import captureplugin.drivers.defaultdriver.DeviceConfig;

import util.ui.Localizer;


/**
 * This Panel makes it possible to assign external names to channels
 */
public class ChannelPanel extends JPanel {

    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(ChannelPanel.class);

    /** GUI */
    private JTextField mChannelNameTextField = new JTextField();

    private JTextField mChannelNumberTextField = new JTextField();

    private JTable mChannelTable = new JTable();

    private ChannelTableModel mChannelTableModel;
    
    /** Settings */
    private DeviceConfig mData;

    /**
     * Creates the Panel
     * @param data Data to use
     */
    public ChannelPanel(DeviceConfig data) {
        mData = data;
        createPanel();
    }
    
    /**
     * creates a JPanel for managing the channels
     */
    private void createPanel() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("ChannelNames", "Channel Names")));
        // Panel
        JPanel p1 = new JPanel();
        p1.setLayout(new BorderLayout());
        // Name - Label
        JLabel channelNameLabel = new JLabel(mLocalizer.msg("InternalName", "Internal Name"));
        p1.add(channelNameLabel, BorderLayout.NORTH);
        //channelNameLabel.setPreferredSize(new Dimension(80, 20));
        // Name - Textfield
        p1.add(mChannelNameTextField, BorderLayout.CENTER);
        //channelNameTextField.setPreferredSize(new Dimension(80, 20));
        // Number - Lable
        JLabel channelNumberLabel = new JLabel(mLocalizer.msg("ExternalName", "External Name"));
        p1.add(channelNumberLabel, BorderLayout.SOUTH);
        //channelNumberLabel.setPreferredSize(new Dimension(80, 20));

        // Panel
        JPanel p2 = new JPanel();
        p2.setLayout(new BorderLayout());
        p2.add(p1, BorderLayout.NORTH);
        // Number - Textfield
        p2.add(mChannelNumberTextField, BorderLayout.CENTER);
        //channelNumberTextField.setPreferredSize(new Dimension(80, 20));
        // ADD - Button
        JButton channelAddButton = new JButton(mLocalizer.msg("AddChannel", "add"));
        p2.add(channelAddButton, BorderLayout.SOUTH);
        //channelAddButton.setPreferredSize(new Dimension(80, 20));
        channelAddButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                addButtonPressed(e);
            }
        });

        // Panel
        JPanel p3 = new JPanel();
        p3.setLayout(new BorderLayout());
        p3.add(p2, BorderLayout.NORTH);
        // REM - Button
        JButton channelRemButton = new JButton(mLocalizer.msg("RemoveChannel", "remove"));
        p3.add(channelRemButton, BorderLayout.CENTER);
        //channelRemButton.setPreferredSize(new Dimension(80, 20));
        channelRemButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                remButtonPressed(e);
            }
        });

        // Panel
        JPanel p4 = new JPanel();
        p4.setLayout(new BorderLayout());
        p4.add(p3, BorderLayout.NORTH);

        mChannelTableModel = new ChannelTableModel(mData);
        mChannelTable.setModel(mChannelTableModel);
        mChannelTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mChannelTable.getTableHeader().setReorderingAllowed(false);
        mChannelTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                channelTableValueChanged();
            }
        });

        JScrollPane sp = new JScrollPane(mChannelTable);

        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 0.8;

        add(sp, c);

        c.fill = GridBagConstraints.VERTICAL;
        c.weightx = 0;
        c.weighty = 1.0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        add(p4, c);
    }

    /**
     * invoked when the user clicks into the table, the two TextFields
     * ChannelName and ChannelNumber will then be filled with the tabledata
     */
    public void channelTableValueChanged() {
        Object key, value;
        int row = mChannelTable.getSelectedRow();
        if (row != -1) {
            key = mChannelTable.getValueAt(row, 0);
            value = mChannelTable.getValueAt(row, 1);
            mChannelNameTextField.setText((String) key);
            mChannelNumberTextField.setText((String) value);
        }
    }

    /**
     * invoked when the user clicks the ADD - Button, the values of the two
     * TextFields ChannelName and ChannelNumber will then be put into the Table
     */
    public void addButtonPressed(ActionEvent e) {
        String key, value;
        key = mChannelNameTextField.getText();
        value = mChannelNumberTextField.getText().trim();
        if (key.length() > 0) {
            mData.getChannels().put(key, value);
            // update the table
            mChannelTableModel.fireTableDataChanged();
        }
    }

    /**
     * invoked when the user clicks the REM - Button, if there is an entry in
     * the table with the value of the TextField ChannelName, it will be removed
     */
    public void remButtonPressed(ActionEvent e) {
        String key;
        key = mChannelNameTextField.getText();

        if (key.length() > 0) {
            if (mData.getChannels().containsKey(key)) mData.getChannels().remove(key);
            mChannelTableModel.fireTableDataChanged();
        }
    }
   
}