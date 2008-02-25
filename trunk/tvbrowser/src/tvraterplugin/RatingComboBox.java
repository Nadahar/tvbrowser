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
   * Type to show
   */
  private int mRatingKey;
  /**
   * Rating to use
   */
  private Rating mRating;
  
  /**
   * Selection
   */
  private static Integer[] values = { Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4), Integer.valueOf(5) };

    /**
     * Creates the Rating
     * @param rating Rating to show
     * @param key Type to show (Erotic etc)
     */
    public RatingComboBox(Rating rating, int key) {
        super(values);
        mRating = rating;
        mRatingKey = key;
        
        if ((mRating != null) && (mRating.getIntValue(key) >= 0)) {
            
            int num = mRating.getIntValue(key);
            
            if (num < getItemCount()) {
                setSelectedIndex(num);
            }
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
            label.setText(RatingIconTextFactory.getStringForRating(mRatingKey, ((Integer)obj).intValue()));
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
    
}
