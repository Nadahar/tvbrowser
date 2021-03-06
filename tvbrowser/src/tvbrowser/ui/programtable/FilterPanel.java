package tvbrowser.ui.programtable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import devplugin.ProgramFilter;
import tvbrowser.core.filters.FilterManagerImpl;
import tvbrowser.ui.mainframe.MainFrame;
import util.ui.WrapperFilter;
import util.ui.persona.Persona;

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
  private JButton mDeactivate;
  
  private static final util.ui.Localizer mLocalizer
  = util.ui.Localizer.getLocalizerFor(FilterPanel.class);

  /**
   * Create the Filter-Panel
   * @param keyListener The key listener for FAYT.
   */
  public FilterPanel(KeyListener keyListener) {
    setLayout(new BorderLayout());
    setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    setBackground(UIManager.getColor("List.background"));
    setOpaque(false);

    mFilterLabel = new JLabel() {
      protected void paintComponent(Graphics g) {
        if(Persona.getInstance().getHeaderImage() != null) {
          final String info = mLocalizer.msg("filterActive", "Active Filter:") + ": ";
          
          final Font boldFont = getFont().deriveFont(Font.BOLD);
          
          final FontMetrics metrics = g.getFontMetrics(boldFont);
          final int textWidth = metrics.stringWidth(info);
          final int baseLine = getBaseline(getWidth(),getHeight());
        
          final String text = mCurrentName.replaceAll("</*span.*?>|</*s>|</*u>|</*b>", "");
          final boolean orange = mCurrentName.contains("color:orange");
          final boolean darkred = mCurrentName.contains("color:red");
      
          if(!Persona.getInstance().getShadowColor().equals(Persona.getInstance().getTextColor())) {
            g.setColor(Persona.getInstance().getShadowColor());
            
            g.setFont(boldFont);
            g.drawString(info,getIconTextGap()+getInsets().left+1,baseLine+1);
            g.drawString(info,getIconTextGap()+getInsets().left+2,baseLine+2);
            
            g.setFont(getFont());
            g.drawString(text,getIconTextGap()+getInsets().left+1+textWidth,baseLine+1);
            g.drawString(text,getIconTextGap()+getInsets().left+2+textWidth,baseLine+2);
          }
          
          g.setColor(Persona.getInstance().getTextColor());
          
          g.setFont(boldFont);
          g.drawString(info,getIconTextGap()+getInsets().left,baseLine);
          
          if(orange) {
            g.setColor(new Color(255,125,0));
          }
          else if(darkred) {
            g.setColor(Color.RED);
          }
          
          boolean strikeThrough = mCurrentName.contains("text-decoration:line-through") || mCurrentName.contains("<s>");
          boolean underline = mCurrentName.contains("text-decoration:underline") || mCurrentName.contains("<u>");
  
          int x = getIconTextGap()+getInsets().left+textWidth;
          
          g.setFont(getFont());
          g.drawString(text,x,baseLine);
          
          if(strikeThrough || underline) {
            int textWidth2 = g.getFontMetrics(getFont()).stringWidth(text);
            
            int y = baseLine+1;
            
            if(strikeThrough) {
              y = baseLine-baseLine/4;
            }
            
            g.drawLine(x, y, x + textWidth2-getInsets().left-getInsets().right, y);
          }
          
          g.setFont(getFont());
          g.drawString(text,x,baseLine);
        }
        else {
          super.paintComponent(g);
        }
      }
    };
    mFilterLabel.addKeyListener(keyListener);
    
    mFilterLabel.setHorizontalAlignment(SwingConstants.LEFT);
    add(mFilterLabel, BorderLayout.CENTER);
    mDeactivate = Persona.createPersonaButton(mLocalizer.msg("deactivate", "Deactivate"));
    mDeactivate.addKeyListener(keyListener);
    mDeactivate.addActionListener(e -> {
      MainFrame.getInstance().setProgramFilter(FilterManagerImpl.getInstance().getDefaultFilter());
    });
    
    if(Persona.getInstance().getHeaderImage() != null) {
      mDeactivate.setRolloverEnabled(true);
      mDeactivate.setOpaque(false);
    }
    
    add(mDeactivate, BorderLayout.EAST);
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
    mCurrentName = name.replaceAll("</*html>", "");
    
    mFilterLabel.setText("<html><body><b>" + mLocalizer.msg("filterActive", "Active Filter:")+ ":</b> "+mCurrentName+"</body></html>");
  }

  /**
   * Set the current Filter.
   * This updates the JLabel to represent the current Filter
   * @param filter current selected Filter
   */
  public void setCurrentFilter(ProgramFilter filter) {
    setFilterLabel(new WrapperFilter(filter).toString());
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
      c = Persona.testPersonaForegroundAgainst(c);
      c2 = new Color(c2.getRed(),c2.getGreen(),c2.getBlue(),200);
    }
    
    // Create the gradient paint
    GradientPaint paint = new GradientPaint((float)width / 3, 0, c, width, height, c2, false);

    g2d.setPaint(paint);
    g2d.fillRect(0, 0, width, height);
  }
  
  public void updateLabel(ProgramFilter filter) {
    setFilterLabel(new WrapperFilter(filter).toString());
  }
  
  /**
   * Updates the filter panel on Persona change.
   */
  public void updatePersona() {
    if(mDeactivate != null) {
      if(Persona.getInstance().getHeaderImage() != null) {
        mDeactivate.setBorder(Persona.getPersonaButtonBorder());
        mDeactivate.setRolloverEnabled(true);
        mDeactivate.setOpaque(false);
      }
      else {
        mDeactivate.setBorder(UIManager.getBorder("Button.border"));
        mDeactivate.setRolloverEnabled(UIManager.getBoolean("Button.rollover"));
        mDeactivate.setOpaque(true);
      }
    }
  }
}
