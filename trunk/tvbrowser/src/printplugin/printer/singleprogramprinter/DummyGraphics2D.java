/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */
package printplugin.printer.singleprogramprinter;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.RenderingHints.Key;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

/**
 * This is a Dummy Graphics2D implementation for the Preview-Dialog
 * @author bodum
 */
public class DummyGraphics2D extends Graphics2D {

  public void rotate(double theta) {
    // TODO Auto-generated method stub

  }

  public void scale(double sx, double sy) {
    // TODO Auto-generated method stub

  }

  public void shear(double shx, double shy) {
    // TODO Auto-generated method stub

  }

  public void translate(double tx, double ty) {
    // TODO Auto-generated method stub

  }

  public void rotate(double theta, double x, double y) {
    // TODO Auto-generated method stub

  }

  public void translate(int x, int y) {
    // TODO Auto-generated method stub

  }

  public Color getBackground() {
    // TODO Auto-generated method stub
    return null;
  }

  public void setBackground(Color color) {
    // TODO Auto-generated method stub

  }

  public Composite getComposite() {
    // TODO Auto-generated method stub
    return null;
  }

  public void setComposite(Composite comp) {
    // TODO Auto-generated method stub

  }

  public GraphicsConfiguration getDeviceConfiguration() {
    // TODO Auto-generated method stub
    return null;
  }

  public Paint getPaint() {
    // TODO Auto-generated method stub
    return null;
  }

  public void setPaint(Paint paint) {
    // TODO Auto-generated method stub

  }

  public RenderingHints getRenderingHints() {
    // TODO Auto-generated method stub
    return null;
  }

  public void clip(Shape s) {
    // TODO Auto-generated method stub

  }

  public void draw(Shape s) {
    // TODO Auto-generated method stub

  }

  public void fill(Shape s) {
    // TODO Auto-generated method stub

  }

  public Stroke getStroke() {
    // TODO Auto-generated method stub
    return null;
  }

  public void setStroke(Stroke s) {
    // TODO Auto-generated method stub

  }

  public FontRenderContext getFontRenderContext() {
    // TODO Auto-generated method stub
    return null;
  }

  public void drawGlyphVector(GlyphVector g, float x, float y) {
    // TODO Auto-generated method stub

  }

  public AffineTransform getTransform() {
    // TODO Auto-generated method stub
    return null;
  }

  public void setTransform(AffineTransform Tx) {
    // TODO Auto-generated method stub

  }

  public void transform(AffineTransform Tx) {
    // TODO Auto-generated method stub

  }

  public void drawString(String s, float x, float y) {
    // TODO Auto-generated method stub

  }

  public void drawString(String str, int x, int y) {
    // TODO Auto-generated method stub

  }

  public void drawString(AttributedCharacterIterator iterator, float x, float y) {
    // TODO Auto-generated method stub

  }

  public void drawString(AttributedCharacterIterator iterator, int x, int y) {
    // TODO Auto-generated method stub

  }

  public void addRenderingHints(Map hints) {
    // TODO Auto-generated method stub

  }

  public void setRenderingHints(Map hints) {
    // TODO Auto-generated method stub

  }

  public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
    // TODO Auto-generated method stub
    return false;
  }

  public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
    // TODO Auto-generated method stub

  }

  public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
    // TODO Auto-generated method stub

  }

  public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
    // TODO Auto-generated method stub

  }

  public Object getRenderingHint(Key hintKey) {
    // TODO Auto-generated method stub
    return null;
  }

  public void setRenderingHint(Key hintKey, Object hintValue) {
    // TODO Auto-generated method stub

  }

  public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
    // TODO Auto-generated method stub
    return false;
  }

  public void dispose() {
    // TODO Auto-generated method stub

  }

  public void setPaintMode() {
    // TODO Auto-generated method stub

  }

  public void clearRect(int x, int y, int width, int height) {
    // TODO Auto-generated method stub

  }

  public void clipRect(int x, int y, int width, int height) {
    // TODO Auto-generated method stub

  }

  public void drawLine(int x1, int y1, int x2, int y2) {
    // TODO Auto-generated method stub

  }

  public void drawOval(int x, int y, int width, int height) {
    // TODO Auto-generated method stub

  }

  public void fillOval(int x, int y, int width, int height) {
    // TODO Auto-generated method stub

  }

  public void fillRect(int x, int y, int width, int height) {
    // TODO Auto-generated method stub

  }

  public void setClip(int x, int y, int width, int height) {
    // TODO Auto-generated method stub

  }

  public void copyArea(int x, int y, int width, int height, int dx, int dy) {
    // TODO Auto-generated method stub

  }

  public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
    // TODO Auto-generated method stub

  }

  public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
    // TODO Auto-generated method stub

  }

  public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
    // TODO Auto-generated method stub

  }

  public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
    // TODO Auto-generated method stub

  }

  public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
    // TODO Auto-generated method stub

  }

  public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
    // TODO Auto-generated method stub

  }

  public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
    // TODO Auto-generated method stub

  }

  public Color getColor() {
    // TODO Auto-generated method stub
    return null;
  }

  public void setColor(Color c) {
    // TODO Auto-generated method stub

  }

  public void setXORMode(Color c1) {
    // TODO Auto-generated method stub

  }

  public Font getFont() {
    // TODO Auto-generated method stub
    return null;
  }

  public void setFont(Font font) {
    // TODO Auto-generated method stub

  }

  public Graphics create() {
    // TODO Auto-generated method stub
    return null;
  }

  public Rectangle getClipBounds() {
    // TODO Auto-generated method stub
    return null;
  }

  public Shape getClip() {
    // TODO Auto-generated method stub
    return null;
  }

  public void setClip(Shape clip) {
    // TODO Auto-generated method stub

  }

  public FontMetrics getFontMetrics(Font f) {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2,
      ImageObserver observer) {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2,
      Color bgcolor, ImageObserver observer) {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer) {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
    // TODO Auto-generated method stub
    return false;
  }

}
