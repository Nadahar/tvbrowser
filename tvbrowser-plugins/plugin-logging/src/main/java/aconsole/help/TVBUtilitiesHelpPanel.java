/*
 *******************************************************************
 *              TVBConsole plugin for TVBrowser                    *
 *                                                                 *
 * Copyright (C) 2010 Tomas Schackert.                             *
 * Contact koumori@web.de                                          *
 *******************************************************************

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, in version 3 of the License.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program, in a file called LICENSE in the top
 directory of the distribution; if not, write to 
 the Free Software Foundation, Inc., 59 Temple Place, Suite 330, 
 Boston, MA  02111-1307  USA
 
 *******************************************************************/

package aconsole.help;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;


/**
 * A help dialog which is able to show HTML pages that are located in a jar
 * file.
 *
 * @author Til Schneider, www.murfman.de, Tomas Schackert
 */
public class TVBUtilitiesHelpPanel extends JPanel implements  HyperlinkListener {
	private static final long serialVersionUID = -3087792114733777445L;
	public interface Listener{
		/**
		 * @param panel
		 */
		void changedHelpPanelState(TVBUtilitiesHelpPanel panel);
		
	}
	Vector<Listener> listeners=new Vector<Listener>();
	public void addHelpPanelListener(Listener l){
		this.listeners.add(l);
	}
	public void removeHelpPanelListener(Listener l){
		this.listeners.remove(l);
	}
  
  /** The minimum size of the help dialog. */  
  public static final Dimension MIN_HELP_DIALOG_SIZE = new Dimension(350, 400);
  
  /** The maximum size of the help dialog. */  
  public static final Dimension MAX_HELP_DIALOG_SIZE = new Dimension(500, 10000);

  
  /** The history. */  
  private Stack<HistoryStackElement> mHistoryStack;

  
  /**
   * url of the site if known
   */
  private URL mUrl;
  
  private JEditorPane mEditorPane;
  private JScrollPane mScrollPane;

  public TVBUtilitiesHelpPanel() {
	mHistoryStack = new Stack<HistoryStackElement>();
	
	this.setLayout(new BorderLayout());

	mEditorPane = new JEditorPane();
	mEditorPane.setContentType("text/html");
	mEditorPane.addHyperlinkListener(this);
	mEditorPane.setEditable(false);
    
    
    
    
	this.add(mScrollPane = new JScrollPane(mEditorPane));

	// buttons
	JPanel buttonPn = new JPanel(new FlowLayout(FlowLayout.TRAILING));
	this.add(buttonPn, BorderLayout.SOUTH);
  }

  // Hilfsmethoden


  /**
   * Sets the text currently show.
   *
   * @param text The HTML text.
   * @param updateScrollBarTo The relative position to scroll to. (Between 0 and 1)
   * @param url The URL of the page, used to display images
   */  
  protected void setEditorText(URL url, double updateScrollBarTo,String text) {
  	this.mUrl=url;
  	if (text!=null){
		SwingUtilities.invokeLater(new ScrollBarUpdater(0));
		((HTMLDocument)(mEditorPane.getDocument())).setBase(url);
		mEditorPane.setText(text);
		SwingUtilities.invokeLater(new ScrollBarUpdater(updateScrollBarTo));
  	}else{
  		if (url!=null){
			try {
				SwingUtilities.invokeLater(new ScrollBarUpdater(0));
				mEditorPane.setPage(url);
				SwingUtilities.invokeLater(new ScrollBarUpdater(updateScrollBarTo));
			} catch (IOException e) {
			}	
  		}else{
			mEditorPane.setText("");
  		}
  	}
  }


  /**
   * Hilfsmethode für {@link #hyperlinkUpdate(HyperlinkEvent) hyperlinkUpdate}.
   * <p>
   * Fügt die aktuelle Seite zum mHistoryStack hinzu.
   */
  protected void addThisSiteToHistory() {
	// Neues HistoryStackElement erstellen
	HistoryStackElement newSite = new HistoryStackElement();
	newSite.mText = mEditorPane.getText();
	newSite.mUrl=mUrl;

	JScrollBar scrollBar = mScrollPane.getVerticalScrollBar();
	newSite.mVerticalScrollBarRelValue = ((double) scrollBar.getValue()) / ((double) scrollBar.getMaximum());

	// Neues HistoryStackElement auf Stack werfen
	mHistoryStack.push(newSite);

	// zurück-Button sichtbar machen
	fireStateChanged();
  }
  private void fireStateChanged(){
  	Iterator<Listener> it=new Vector<Listener>(listeners).iterator();
  	while (it.hasNext()){
  		Listener l=it.next();
  		l.changedHelpPanelState(this);
  	}
  }
  public boolean haveHistory(){
	return mHistoryStack.size()>0;
  }



  /**
   * Hilfsmethode für {@link #actionPerformed(ActionEvent)}.
   * <P>
   * Ruft die oberste Seite des mHistoryStack wieder auf.
   */
  protected void popFromHistory() {
	HistoryStackElement lastSite = (HistoryStackElement) mHistoryStack.pop();

	double updateScrollBarTo = lastSite.mVerticalScrollBarRelValue;
	setEditorText(lastSite.mUrl, updateScrollBarTo,lastSite.mText);

	mScrollPane.validate();
	fireStateChanged();
  }



  /**
   * Lädt eine HTML-Seite und gibt deren Inhalt als String zurück.
   * <p>
   * Die Seite kann auch in der Jar-Datei sein, in dem diese Klasse ist.
   *
   * @param clazz A class in the jar where the HTML site is located.
   * @param filename Der Dateiname der HTML-Seite.
   */
  public void openSite(URL url) {
	addThisSiteToHistory();
	setEditorText(url,0,null);
  }



  // implements HyperlinkListener
  public void hyperlinkUpdate(HyperlinkEvent evt) {
	if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
	  if (evt instanceof HTMLFrameHyperlinkEvent) {
		HTMLFrameHyperlinkEvent frameEvt = (HTMLFrameHyperlinkEvent) evt;
		HTMLDocument doc = (HTMLDocument) mEditorPane.getDocument();
		doc.processHTMLFrameHyperlinkEvent(frameEvt);
	  } else {
		TVBUtilitiesHelpDialog.showHelpPage(this,evt.getURL(),null);
	  }
	}
  }
  
  
  // inner class ScrollBarUpdater
  
  
  protected class ScrollBarUpdater implements Runnable {
    
	private double mUpdateScrollBarTo;
    
	public ScrollBarUpdater (double updateScrollBarTo) {
	  mUpdateScrollBarTo = updateScrollBarTo;
	}
    
	public void run() {
	  JScrollBar bar = mScrollPane.getVerticalScrollBar();
	  bar.setValue((int) (mUpdateScrollBarTo * bar.getMaximum()));
	}
    
  }


  // inner class HistoryStackElement
  

  protected static class HistoryStackElement {
	/**
	 * Quellpfad der zuletzt besuchten Seite. Kann <CODE>null</CODE> sein, wenn
	 * der Text nicht aus einer Datei stammt.
	 */
	public String mSourcePath;
    
	/** The class of the jar, where the page is in. */
	public Class<?> mSourceClass;

	/** Der Text der zuletzt besuchten Seite. */
	public String mText;

	/** Die letzte relative Position der vertikalen ScrollBar. (value / maximum) */
	public double mVerticalScrollBarRelValue;
	
	public URL mUrl;
  }

}
