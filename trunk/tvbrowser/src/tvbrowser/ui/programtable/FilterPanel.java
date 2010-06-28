package tvbrowser.ui.programtable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import tvbrowser.core.filters.FilterManagerImpl;
import tvbrowser.ui.mainframe.MainFrame;
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
   */
  public FilterPanel() {
    setLayout(new BorderLayout());
    setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    setBackground(Color.WHITE);

    mFilterLabel = new JLabel();

    mFilterLabel.setHorizontalAlignment(SwingConstants.LEFT);
    add(mFilterLabel, BorderLayout.CENTER);

    JButton deactivate = new JButton(mLocalizer.msg("deactivate", "Deactivate"));

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

    // Create the gradient paint
    GradientPaint paint =
        new GradientPaint((float)width / 3, 0, getBackground(), width, height, UIManager.getColor("List.selectionBackground"), false);

    g2d.setPaint(paint);
    g2d.fillRect(0, 0, width, height);
  }

}
