/*
 * Created on 14.04.2004
 */
package tvraterplugin;

import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;


/**
 * The RatingComboBox shows a specific Rating in a ComboBox
 * 
 * @author Bodo
 */
public class RatingComboBox extends JComboBox implements ListCellRenderer {
   
    /**
     * Creates the Rating
     * @param rating Rating to show
     * @param type Type to show (Erotic etc)
     */
    public RatingComboBox(Rating rating, Object type) {
        super(values);
        _rating = rating;
        _type = type;
        
        if ((_rating != null) && (_rating.getIntValue(type) >= 0)) {
            setSelectedIndex(_rating.getIntValue(type));
        } else {
            setSelectedItem(null);
        }
		setRenderer(this);
    }

    /*
     * Renders the selection
     * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
     */
    public Component getListCellRendererComponent(JList list, Object obj,
            int index, boolean isSelected, boolean hasFocus) {

        JLabel label = new JLabel();
        label.setOpaque(true);
        if (obj instanceof Integer) {
            label.setIcon(RatingIconTextFactory.getImageIconForRating(((Integer)obj).intValue()));
            label.setText(RatingIconTextFactory.getStringForRating(_type, ((Integer)obj).intValue()));
        }
        
        if (isSelected || hasFocus) {
            label.setBackground(list.getSelectionBackground());
            label.setForeground(list.getSelectionForeground());
        } else {
            label.setBackground(list.getBackground());
            label.setForeground(list.getForeground());
        }
        
        return label;
    }
    
    /**
     * Type to show
     */
    private Object _type;
    /**
     * Rating to use
     */
    private Rating _rating;
    
    /**
     * Selection
     */
	private static Integer[] values = { new Integer(0), new Integer(1), new Integer(2), new Integer(3), new Integer(4), new Integer(5) };
}
