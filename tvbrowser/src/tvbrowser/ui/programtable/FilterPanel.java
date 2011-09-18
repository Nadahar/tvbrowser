package tvbrowser.ui.programtable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import tvbrowser.core.filters.FilterManagerImpl;
import tvbrowser.ui.mainframe.MainFrame;
import util.ui.persona.Persona;
import devplugin.ProgramFilter;

/**
 * This Class represents the Panel above the ProgramPanel. If a
 * Filter is selected, this Panel is visible and shows the name of
 * the current selected filter
 *
 * @author bodum
 */
public class FilterPanel extends JPanel {
  /** Label that is used */
  private JLabel mFilterLabel;

  /**
   * remember current filter name to avoid repeated UI updates
   */
  private String mCurrentName;

  private static final util.ui.Localizer mLocalizer
  = util.ui.Localizer.getLocalizerFor(FilterPanel.class);

  /**
   * Create the Filter-Panel
   * @param keyListener The key listener for FAYT.
   */
  public FilterPanel(KeyListener keyListener) {
    setLayout(new BorderLayout());
    setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    setBackground(Color.WHITE);
    setOpaque(false);

    mFilterLabel = new JLabel() {
      protected void paintComponent(Graphics g) {
        if(Persona.getInstance().getHeaderImage() != null) {

          String info = mLocalizer.msg("filterActive", "Active Filter:") + ": ";
          
          Font boldFont = getFont().deriveFont(Font.BOLD);
          
          FontMetrics metrics = g.getFontMetrics(boldFont);
          int textWidth = metrics.stringWidth(info);
          int baseLine = getBaseline(getWidth(),getHeight());
        
      
        if(!Persona.getInstance().getShadowColor().equals(Persona.getInstance().getTextColor())) {
          g.setColor(Persona.getInstance().getShadowColor());
          
          g.setFont(boldFont);
          g.drawString(info,getIconTextGap()+getInsets().left+1,baseLine+1);
          g.drawString(info,getIconTextGap()+getInsets().left+2,baseLine+2);
          
          g.setFont(getFont());
          g.drawString(mCurrentName,getIconTextGap()+getInsets().left+1+textWidth,baseLine+1);
          g.drawString(mCurrentName,getIconTextGap()+getInsets().left+2+textWidth,baseLine+2);
        }
        
        g.setColor(Persona.getInstance().getTextColor());
        
        g.setFont(boldFont);
        g.drawString(info,getIconTextGap()+getInsets().left,baseLine);
        
        g.setFont(getFont());
        g.drawString(mCurrentName,getIconTextGap()+getInsets().left+textWidth,baseLine);
      }
      else {
        super.paintComponent(g);
      }
      }
    };
    mFilterLabel.addKeyListener(keyListener);
    
    mFilterLabel.setHorizontalAlignment(SwingConstants.LEFT);
    add(mFilterLabel, BorderLayout.CENTER);

    JButton deactivate = new JButton(mLocalizer.msg("deactivate", "Deactivate"));
    deactivate.addKeyListener(keyListener);
    deactivate.addActionListener(new ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        MainFrame.getInstance().setProgramFilter(FilterManagerImpl.getInstance().getDefaultFilter());
      };
    });

    add(deactivate, BorderLayout.EAST);
  }

  /**
   * Set the Name of the Filter
   * @param name Name of the Filter
   */
  private void setFilterLabel(String name) {
    // avoid repainting during repeated filter updates
    if (name.equals(mCurrentName)) {
      return;
    }
    mCurrentName = name;
    mFilterLabel.setText("<html><body><b>" + mLocalizer.msg("filterActive", "Active Filter:")+ ":</b> "+name+"</body></html>");
  }

  /**
   * Set the current Filter.
   * This updates the JLabel to represent the current Filter
   * @param filter current selected Filter
   */
  public void setCurrentFilter(ProgramFilter filter) {
    setFilterLabel(filter.getName());
  }

  /**
   * Paints the component
   */
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    Graphics2D g2d = (Graphics2D)g;

    int width = getWidth();
    int height = getHeight();

    Color c2 = Persona.getInstance().getTextColor() != null ? Persona.getInstance().getTextColor() : UIManager.getColor("List.selectionBackground");
    Color c = Persona.getInstance().getAccentColor() != null ? Persona.getInstance().getAccentColor() : getBackground();
    
    if(Persona.getInstance().getHeaderImage() != null) {
      double test = (0.2126 * Persona.getInstance().getTextColor().getRed()) + (0.7152 * Persona.getInstance().getTextColor().getGreen()) + (0.0722 * Persona.getInstance().getTextColor().getBlue());
      int alpha = 100;
      
      if(test <= 30) {
        c = Color.white;
        alpha = 200;
      }
      else if(test <= 40) {
        c = c.brighter().brighter().brighter().brighter().brighter().brighter();
        alpha = 200;
      }
      else if(test <= 60) {
        c = c.brighter().brighter().brighter();
        alpha = 160;
      }
      else if(test <= 100) {
        c = c.brighter().brighter();
        alpha = 140;
      }
      else if(test <= 145) {
        alpha = 120;
      }
      else if(test <= 170) {
        c = c.darker();
        alpha = 120;
      }
      else if(test <= 205) {
        c = c.darker().darker();
        alpha = 120;
      }
      else if(test <= 220){
        c = c.darker().darker().darker();
        alpha = 100;
      }
      else if(test <= 235){
        c = c.darker().darker().darker().darker();
        alpha = 100;
      }
      else {
        c = Color.black;
        alpha = 100;
      }
      
      
      
      c = new Color(c.getRed(),c.getGreen(),c.getBlue(),alpha);
      c2 = new Color(c2.getRed(),c2.getGreen(),c2.getBlue(),200);
    }
    
    // Create the gradient paint
    GradientPaint paint =
        new GradientPaint((float)width / 3, 0, c, width, height, c2, false);

    g2d.setPaint(paint);
    g2d.fillRect(0, 0, width, height);
  }

}
