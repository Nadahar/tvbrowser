/*
 * Created on 28.09.2004
 */
package util.ui;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import devplugin.Channel;


/**
 * @author bodum
 */
public class ChannelListCellRenderer extends DefaultListCellRenderer {

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        
        
        if (value instanceof Channel) {
            label.setIcon(((Channel)value).getIcon());
        }
        
        return label;
    }
}
