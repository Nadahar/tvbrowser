/*
 * Created on 11.09.2004
 */
package tvbrowser.ui.mainframe;

import java.awt.BorderLayout;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import util.ui.ChannelListCellRenderer;
import devplugin.Channel;


/**
 * @author bodum
 */
public class ChannelChooserPanel extends JPanel {

    
    private DefaultListModel mChannelChooserModel;
    private JList mList;
    private MainFrame mParent;
    
    /**
     * @param frame
     */
    public ChannelChooserPanel(MainFrame frame) {
        mParent =frame;

        mChannelChooserModel = new DefaultListModel();
        updateChannelChooser();
        mList = new JList(mChannelChooserModel);
        
        setLayout(new BorderLayout());
        add(new JScrollPane(mList));
        
        mList.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                mParent.showChannel((Channel)mList.getSelectedValue());
            }
            
        });
        
        mList.setCellRenderer(new ChannelListCellRenderer());
    }

    public void updateChannelChooser() {  
        mChannelChooserModel.removeAllElements();
        Channel[] channelList=tvbrowser.core.ChannelList.getSubscribedChannels();
        for (int i=0;i<channelList.length;i++) {
          mChannelChooserModel.addElement(channelList[i]);
        }
      }

}
