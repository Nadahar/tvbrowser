/*
 * Created on 18.04.2004
 */
package tvraterplugin;

import java.awt.Color;
import java.awt.Component;
import java.util.TreeSet;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;


/**
 * @author bodo
 */
public class GenreComboBox extends JComboBox implements ListCellRenderer {

    public GenreComboBox(int curRating) {
        TreeSet tree = new TreeSet(RatingIconTextFactory.getGenres().keySet());

        DefaultComboBoxModel model = new DefaultComboBoxModel(tree.toArray());
	    setModel(model);

    	setRenderer(this);
        
		if (curRating <= -1) {
			setSelectedItem(null);
		} else {
			String curStr = Integer.toString(curRating);
			
			while (curStr.length() < 3) {
				curStr = "0" + curStr;
			}

	   	    int ind = model.getIndexOf(curStr);
	   	    
	   	    if (ind == -1) {
		   	    ind = model.getIndexOf("999");
	   	    }
	   	    
	        setSelectedIndex(ind);
		}
		
        
    }
    
    
    /* (non-Javadoc)
     * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
     */
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
            boolean cellHasFocus) {

    	if (value == null) {
            JLabel label = new JLabel("");
    	    label.setOpaque(true);
            return label;
    	}
    	
		String str = RatingIconTextFactory.getGenres().getProperty(value.toString(), "[TRANSLATIONERROR:" + value.toString()+ " ]").toString();
        JLabel label = new JLabel(str);
	    label.setOpaque(true);

        if (isSelected || cellHasFocus) {
            label.setBackground(list.getSelectionBackground());
            label.setForeground(list.getSelectionForeground());
        } else {
        	if (value.toString().startsWith("0")) {
            	label.setBackground(Color.WHITE);
        	} else if (value.toString().startsWith("1")) {
            	label.setBackground(new Color(239, 238, 244));
        	} else if (value.toString().equals("999")) {
            	label.setBackground(new Color(202, 203, 222));
        	}
            label.setForeground(Color.BLACK);
        }        
        return label;
    }

}