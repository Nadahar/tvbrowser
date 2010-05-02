/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourcceforge.net)
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
package tvbrowserdataservice;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import util.ui.ChannelListCellRenderer;
import util.ui.Localizer;
import util.ui.TabLayout;
import devplugin.Channel;


/**
 * This Dialog shows Details for a ChannelGroup
 * 
 * @author bodum
 */
public class ChannelGroupDialog extends JDialog {
    /** Current ChannelGroup */
    private TvBrowserDataServiceChannelGroup mGroup;
    /** The localizer for this class. */
    private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(ChannelGroupDialog.class);

    /**
     * Create the Dialog
     * @param parent Parent-Frame
     * @param group Group to show
     */
    public ChannelGroupDialog(Window parent, TvBrowserDataServiceChannelGroup group) {
    super(parent);
    setModal(true);
        mGroup = group;
        createGui();
    }

    /**
     * Creates the GUI
     */
    private void createGui() {
        setTitle(mLocalizer.msg("viewdetails", "View Details"));
        
        setLocationRelativeTo(getParent());
        
        JPanel cpanel = (JPanel) getContentPane();
        cpanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        cpanel.setLayout(new BorderLayout());
        
        JPanel panel = new JPanel(new TabLayout(2));
        panel.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("details", "Details")));
        
        panel.add(new JLabel(mLocalizer.msg("name", "Name") + ":"));
        panel.add(new JLabel(mGroup.getName()));
        
        panel.add(new JLabel(mLocalizer.msg("provider", "Provider")+":"));
        panel.add(new JLabel(mGroup.getProviderName()));

        panel.add(new JLabel(mLocalizer.msg("url", "Url")+":"));

        String[] mirrors = mGroup.getMirrorArr();
        if (mirrors.length == 0) {
          panel.add(new JLabel("-"));
        }
        else {
          panel.add(new JLabel(mirrors[0] + "/" + mGroup.getId()));
        }

        
//        panel.add(new JLabel(mLocalizer.msg("webpage","WebPage") + ":"));
//        if (mGroup.getProviderWebPage() == null) {
//            panel.add(new JLabel(mLocalizer.msg("nowebpage", "Not availabe")));
//        } else {
//            LinkButton b = new LinkButton(mGroup.getProviderWebPage());
//            b.setHorizontalAlignment(LinkButton.LEFT);
//            panel.add(b);
//        }

        JPanel descPanel = new JPanel(new BorderLayout());
        descPanel.add(new JLabel(mLocalizer.msg("description","Description") + ":"), BorderLayout.NORTH);
        panel.add(descPanel);
        panel.add(createTextArea(mGroup.getDescription()));
        
        JPanel channelPanel = new JPanel(new BorderLayout());
        channelPanel.setBorder(BorderFactory.createTitledBorder(Localizer.getLocalization(Localizer.I18N_CHANNELS)));
        
        Channel[] c = mGroup.getAvailableChannels();
        Channel[] ch = new Channel[c.length];
        
        System.arraycopy(c, 0, ch, 0, c.length);
        
        Arrays.sort(ch,new Comparator<Channel>() {
            public int compare(Channel o1, Channel o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });
        
        JList list = new JList(ch);
        list.setCellRenderer(new ChannelListCellRenderer(true, true));
        
        channelPanel.add(new JScrollPane(list), BorderLayout.CENTER);
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton ok = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
        ok.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
            
        });
        btnPanel.add(ok);
        
        cpanel.add(panel, BorderLayout.NORTH);
        cpanel.add(channelPanel, BorderLayout.CENTER);
        cpanel.add(btnPanel, BorderLayout.SOUTH);
        
        pack();
    
    }
    
    /**
     * Create a Text Area for the Description 
     * @param text Text to show
     * @return filled Textarea
     */
    private JTextArea createTextArea(String text) {
        JTextArea chArea =new JTextArea(3,40);
        chArea.setFont(new JLabel().getFont());
        chArea.setLineWrap(true);
        chArea.setWrapStyleWord(true);
        chArea.setEditable(false);
        chArea.setOpaque(false);
        chArea.setText(text);
        return chArea;
    }
}
