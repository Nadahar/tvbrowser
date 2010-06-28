/*
 * Created on 12.01.2005
 *
 */
package util.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

/**
 * Adds Line-Numbes to JTextAreas
 * 
 * Example:
 * <code>
 * JTextArea area = new JTextArea();
 * JScrollPane scrollPane = new JScrollPane(area);
 * LineNumberHeader header = new LineNumberHeader(area);
 * scrollPane.setRowHeaderView(header);
 * </code>
 * 
 * @author Christoph Mohr
 * 
 */
public class LineNumberHeader extends JComponent {
  private int maxLines = 0;

  private int borderWidth = 2;
  
  private boolean borderLine = false;

  private JTextComponent textComponent;

  private FontMetrics metrics;

  /**
   * Create the LineNumberHeader
   * 
   * @param textArea TextArea to use
   * @param borderWidth Width of the Border
   * @param borderLine Draw Border-Lines
   */
  public LineNumberHeader(JTextComponent textArea, int borderWidth, boolean borderLine) {
    this(textArea);
    this.borderWidth = Math.max(0, borderWidth);
    this.borderLine = borderLine;
  }

  /**
   * Create the LineNumberHeader
   * 
   * @param textArea TextArea to use
   */
  public LineNumberHeader(JTextComponent textArea) {
    this.textComponent = textArea;
    register();
  }

  private void register() {
    this.textComponent.getDocument().addDocumentListener(new DocumentListener() {
      public void insertUpdate(DocumentEvent e) {
        setLines();
      }

      public void removeUpdate(DocumentEvent e) {
        setLines();
      }

      public void changedUpdate(DocumentEvent e) {
        setLines();
      }
    });

    this.textComponent.addPropertyChangeListener("font", new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        setMetrics();
      }
    });
    // Initiale Werte setzen
    setMetrics();
    setLines();
  }

  
  /**
   * Set local FontMetrics from the <code>JTextArea</code>
   * Calls revalidate() and repaint()
   */
  private void setMetrics() {
    Font refFont = null;
   
    Document doc = this.textComponent.getDocument();
    if(doc instanceof DefaultStyledDocument) {
        DefaultStyledDocument myDoc = (DefaultStyledDocument)doc;
        refFont = myDoc.getFont(myDoc.getDefaultRootElement().getElement(0)
          .getAttributes());
    } else {
        refFont = this.textComponent.getFont();
    }

    metrics = this.textComponent.getFontMetrics(refFont);
     
    revalidate();
    repaint();
  }
  
  /**
   * Setzt lokal die max. Zeilenzahl. FÃ¼hrt falls nÃ¶tig revalidate() und
   * repaint() aus.
   */
  private void setLines() {
    int newLines;
    
    if (textComponent instanceof JTextArea) {
      newLines = ((JTextArea)textComponent).getLineCount();
    } else {
      newLines = textComponent.getDocument().getDefaultRootElement().getElementCount();
    }
    
    if (newLines != this.maxLines) {
      if (newLines / 10 != this.maxLines / 10) {
        // Neuer 10er Schritt, Platz schaffen bzw. freigeben
        revalidate();
      }
      this.maxLines = newLines;
      repaint(this.getVisibleRect());
    }
  }

  public Dimension getPreferredSize() {
    // Breite des Gesamtstrings ermitteln
    // Also: Maximale-Zeilenzahl als String, dessen Stringbreite
    final int w = metrics.stringWidth(Integer.toString(maxLines));
    // Höhe einer einzelnen Textzeile
    final int h = metrics.getHeight();

    // Breite: 2 * Randbreite + max. Stringbreite
    // Höhe: max. Zeilenzahl * Höhe einer Zeile (Stringhöhe)
    return new Dimension(w + 2 * this.borderWidth, maxLines * h + this.textComponent.getMargin().top
        + this.textComponent.getMargin().bottom);
  }

  public Dimension getMinimumSize() {
    return getPreferredSize();
  }

  public void paint(Graphics g) {
    super.paint(g);

    // Font setzen
    g.setFont(metrics.getFont());
    g.setColor(Color.DARK_GRAY);

    // Maximale Stringbreite (wie in getPrefSize()
    final int w = metrics.stringWidth(Integer.toString(maxLines));
    // Schrifthöhe (wie in getPrefSize()
    final int h = metrics.getHeight();

    // Verschiebung nach links um Gesamtbreite (String) + ein Rand
    int x = getWidth() - this.borderWidth - w;
    // y = Absoluter Startpunkt fÃ¼r erste Textzeile
    int y = this.textComponent.getMargin().top + metrics.getAscent();

    Rectangle rect = g.getClipBounds();

    // Gesucht: 1. Absolute Textzeile die Sichtbar ist!
    if (rect.y > y) {
      y = (rect.y / h) * h + y;
    }

    int start = y / h;
    if (start == 0) {
      start = 1;
    }
    int end = Math.min(maxLines, rect.height / h + start);

    for (int i = start; i <= end; i++) {
      // Die Zeilennummer
      final String s = Integer.toString(i);
      // Breite des aktuell zu zeichnenden Strings
      final int sw = metrics.stringWidth(s);
      // String zeichnen
      g.drawString(s, x + w - sw, y);
      // y-Position setzen
      y += h;
    }

    if (borderLine) {
      // Abgrenzungslinie zeichnen
      g.drawLine(getWidth() - 1, rect.y, getWidth() - 1, rect.y + rect.height);
    }
  }

}