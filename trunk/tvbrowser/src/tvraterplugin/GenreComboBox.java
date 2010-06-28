/*
 * Created on 18.04.2004
 */
package tvraterplugin;

import java.awt.Color;
import java.awt.Component;
import java.util.Comparator;
import java.util.TreeSet;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;


/**
 * This Class shows a Combobox filled with all Genres
 * 
 * @author bodo
 */
public class GenreComboBox extends JComboBox implements ListCellRenderer, Comparator<Object> {

    public GenreComboBox(int curRating) {
        TreeSet<Object> tree = new TreeSet<Object>(this);

        tree.addAll(RatingIconTextFactory.getGenres().keySet());
        
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
    
    
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
            boolean cellHasFocus) {

    	if (value == null) {
            JLabel label = new JLabel("");
    	    label.setOpaque(true);
            return label;
    	}
    	
		String str = RatingIconTextFactory.getGenres().getProperty(
        value.toString(), "[TRANSLATIONERROR:" + value.toString() + " ]");
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


	/**
	 * Compares two Genres
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object o1, Object o2) {
		
		String a = o1.toString();
		String b = o2.toString();

		if (a.charAt(0) < b.charAt(0)) {
			return -1;
		} else if (a.charAt(0) > b.charAt(0)) {
			return 1;
		}

		String aText = RatingIconTextFactory.getGenres().getProperty(a, "-");
    String bText = RatingIconTextFactory.getGenres().getProperty(b, "-");
		
		return aText.compareTo(bText);
	}

}