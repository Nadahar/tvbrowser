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
package aconsole.gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.Vector;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import util.ui.Localizer;
import aconsole.AConsole;
import aconsole.data.Console;
import aconsole.help.TVBUtilitiesHelpDialog;
import aconsole.properties.Property;

final class ConsolePanel extends JPanel implements ComponentListener,Console.Listener{
	private static final long serialVersionUID = 8848325800260966250L;
	static private final Localizer mLocalizer= Localizer.getLocalizerFor(ConsolePanel.class);
	private boolean fastscroll=false;		//false=scroll per lines; true= sroll per block
	private JPanel toolbar=new JPanel();
	private JTextField tf_loggerFilterText =null;
	private JTextPane outputTextArea = new JTextPane(new DefaultStyledDocument());
	private JScrollPane jScrollPane1=null;
	private boolean moveToEndOfOutputArea=false;					//if moveToEndOfOutputArea=true outputTextArea will scroll down at the next resize; see componentResized
	Console console=null;

	RecordFormatter formatter=new RecordFormatter();
	String loggerFilterText="";
	Level loggerFilterLevel=Level.ALL;
	private boolean initGui=false;

	private boolean showDate=false;
	private boolean showClass=false;
	private boolean showTime=true;
	private boolean showMethod=false;
	private boolean showOut=true;
	private boolean showErr=true;

	private static final Level[] loggerLevels={
		Level.SEVERE,
		Level.WARNING,
		Level.INFO,
		Level.CONFIG,
		Level.FINE,
		Level.FINER,
		Level.FINEST,
		Level.ALL
	};
	//constructor
	public ConsolePanel(JFrame frame,Console console,java.awt.Font font,Color caretcolor,Color disabledcolor){
		this.console=console;
		loggerFilterText=AConsole.getLoggerFilter().get();
		showDate=AConsole.getShowDate().get();
		showTime=AConsole.getShowTime().get();
		showClass=AConsole.getShowClass().get();
		showMethod=AConsole.getShowMethod().get();
		showOut=AConsole.getShowOut().get();
		showErr=AConsole.getShowErr().get();
		int defaultLoggerLevel=AConsole.getLoggerLevel().get();
		if (defaultLoggerLevel<0 && defaultLoggerLevel>loggerLevels.length) {
      defaultLoggerLevel=1;
    }

		formatter.setStyle(showDate,showTime,showClass,showMethod);

		setLayout(new BorderLayout());
		outputTextArea.setAutoscrolls(true);
		outputTextArea.setEnabled(true);
		outputTextArea.setEditable(false);
		outputTextArea.addComponentListener(this);
		jScrollPane1 = new JScrollPane(outputTextArea);
		jScrollPane1.setAutoscrolls(true);
		setFont(font,caretcolor,disabledcolor);
		add(jScrollPane1, BorderLayout.CENTER);

		toolbar.setLayout(new BorderLayout());
		tf_loggerFilterText = new JTextField();
		tf_loggerFilterText.setToolTipText(mLocalizer.msg("loggerfilter_tt","show logger with names starting with this pattern"));
		tf_loggerFilterText.setText(loggerFilterText);
		toolbar.add(tf_loggerFilterText,BorderLayout.CENTER);
		tf_loggerFilterText.getDocument().addDocumentListener(new DocumentListener(){
			public void insertUpdate(DocumentEvent e) {
				updateOutput();
			}

			public void removeUpdate(DocumentEvent e) {
				updateOutput();
			}

			public void changedUpdate(DocumentEvent e) {
				updateOutput();
			}
			private void updateOutput(){
				ConsolePanel.this.console.removeListener(ConsolePanel.this);
				ConsolePanel.this.loggerFilterText=tf_loggerFilterText.getText();
				AConsole.getLoggerFilter().set(ConsolePanel.this.loggerFilterText);
				clear();
				ConsolePanel.this.console.addListener(ConsolePanel.this);
			}
		});

		JPanel buttonpanel=new JPanel();
		toolbar.add(buttonpanel,BorderLayout.EAST);
		final JComboBox cb_loggerFilterLevel=new JComboBox(loggerLevels);
		cb_loggerFilterLevel.setToolTipText(mLocalizer.msg("loggerlevel_tt","set the minimum level for logger events to show"));
		buttonpanel.add(cb_loggerFilterLevel);


		loggerFilterLevel=loggerLevels[defaultLoggerLevel];
		cb_loggerFilterLevel.setSelectedItem(loggerFilterLevel);
		cb_loggerFilterLevel.addActionListener(new AbstractAction(){
			private static final long serialVersionUID = -547094630942977565L;

			public void actionPerformed(ActionEvent e) {
				try{
					ConsolePanel.this.console.removeListener(ConsolePanel.this);
					loggerFilterLevel=(Level)cb_loggerFilterLevel.getSelectedItem();
					clear();
					AConsole.getLoggerLevel().set(cb_loggerFilterLevel.getSelectedIndex());
					ConsolePanel.this.console.addListener(ConsolePanel.this);
				}catch (Exception ex){
					AConsole.foundABug(ex);
				}
			}
		});
		cb_loggerFilterLevel.setEditable(false);


		final JToggleButton btnShowDate= new JToggleButton(mLocalizer.msg("date","date"));
		btnShowDate.setToolTipText(mLocalizer.msg("date_tt","show/hide logger-event date"));
		btnShowDate.setSelected(showDate);

		buttonpanel.add(btnShowDate);
		final JToggleButton btnShowTime= new JToggleButton(mLocalizer.msg("time","time"));
		btnShowTime.setToolTipText(mLocalizer.msg("time_tt","show/hide logger-event time"));
		btnShowTime.setSelected(showTime);
		buttonpanel.add(btnShowTime);
		final JToggleButton btnShowClass= new JToggleButton(mLocalizer.msg("class","class"));
		btnShowClass.setToolTipText(mLocalizer.msg("class_tt","show/hide name of logger (normally the class name)"));
		btnShowClass.setSelected(showClass);
		buttonpanel.add(btnShowClass);
		final JToggleButton btnShowMethod= new JToggleButton(mLocalizer.msg("method","method"));
		btnShowMethod.setToolTipText(mLocalizer.msg("method_tt","show/hide java methods where the logger occured (may be null)"));
		btnShowMethod.setSelected(showMethod);
		buttonpanel.add(btnShowMethod);
		final JToggleButton btnShowOut= new JToggleButton(mLocalizer.msg("out","out"));
		btnShowOut.setToolTipText(mLocalizer.msg("out_tt","show/hide text from the system default output stream"));
		btnShowOut.setSelected(showOut);
		buttonpanel.add(btnShowOut);
		final JToggleButton btnShowErr= new JToggleButton(mLocalizer.msg("err","err"));
		btnShowErr.setToolTipText(mLocalizer.msg("err_tt","show/hide text from the system error output stream"));
		btnShowErr.setSelected(showErr);
		buttonpanel.add(btnShowErr);

		AbstractAction styleactionlistener=new AbstractAction(){
			private static final long serialVersionUID = 6352251822580898635L;

			public void actionPerformed(ActionEvent e) {
				try{
					if (!initGui) {
            return;
          }
					ConsolePanel.this.console.removeListener(ConsolePanel.this);
					AConsole.getShowDate().set(showDate=btnShowDate.isSelected());
					AConsole.getShowTime().set(showTime=btnShowTime.isSelected());
					AConsole.getShowClass().set(showClass=btnShowClass.isSelected());
					AConsole.getShowMethod().set(showMethod=btnShowMethod.isSelected());
					AConsole.getShowOut().set(showOut=btnShowOut.isSelected());
					AConsole.getShowErr().set(showErr=btnShowErr.isSelected());

					formatter.setStyle(
						btnShowDate.isSelected(),
						btnShowTime.isSelected(),
						btnShowClass.isSelected(),
						btnShowMethod.isSelected()
					);
					clear();
					ConsolePanel.this.console.addListener(ConsolePanel.this);
				}catch (Exception ex){
					AConsole.foundABug(ex);
				}
			}
		};


		btnShowDate.addActionListener(styleactionlistener);
		btnShowTime.addActionListener(styleactionlistener);
		btnShowClass.addActionListener(styleactionlistener);
		btnShowMethod.addActionListener(styleactionlistener);
		btnShowOut.addActionListener(styleactionlistener);
		btnShowErr.addActionListener(styleactionlistener);


		buttonpanel.add(getOpenHelpButton(frame));
		add(toolbar, BorderLayout.NORTH);

		fastscroll=false;

		Property.Listener colorChangedListener=new Property.Listener(){
			public void changedProperty(Property<?> p) {
				ConsolePanel.this.console.removeListener(ConsolePanel.this);
				clear();

				outputTextArea.setForeground(AConsole.getSystemOutText().get());
				setBackground(AConsole.getBg().get());
				outputTextArea.setBackground(AConsole.getBg().get());
				outputTextArea.setSelectionColor(AConsole.getSelection().get());

				ConsolePanel.this.console.addListener(ConsolePanel.this);
			}

		};
		AConsole.getBg().addListener(colorChangedListener);
		AConsole.getSelection().addListener(colorChangedListener);
		AConsole.getSelectionText().addListener(colorChangedListener);
		AConsole.getSystemOutText().addListener(colorChangedListener);
		AConsole.getSystemErrText().addListener(colorChangedListener);
		AConsole.getLevelSevereText().addListener(colorChangedListener);
		AConsole.getLevelWarningText().addListener(colorChangedListener);
		AConsole.getLevelInfoText().addListener(colorChangedListener);
		AConsole.getLevelOtherText().addListener(colorChangedListener);
		initGui=true;
		console.addListener(this);
	}
	public void setFont(java.awt.Font font,Color caretcolor, Color disabledcolor){
		outputTextArea.setFont(font);
		outputTextArea.setForeground(AConsole.getSystemOutText().get());
		setBackground(AConsole.getBg().get());
		outputTextArea.setBackground(AConsole.getBg().get());
		outputTextArea.setSelectionColor(AConsole.getSelection().get());
		outputTextArea.setCaretColor(caretcolor);
		outputTextArea.setDisabledTextColor(disabledcolor);
	}
	/*config-stuff*********************************************/
	public void setFastScroll(boolean fs){
		fastscroll=fs;
	}

	/*add/remove text*********************************************/
	/**
	 * @see tvbconsole.Console.Listener#addText(tvbconsole.Console.LoggerConsoleEvent)
	 */
	public void addText(Console.LoggerConsoleEvent ce) {
		if (!initGui) {
      return;
    }
		Color textcolor=null;
		String text=null;
		try{
			String loggername=ce.getLoggerName();
			if (loggername!=null && loggername.equals(Console.SYSTEMOUT_LOGGER)){
				if (!showOut) {
          return;
        }
				textcolor=AConsole.getSystemOutText().get();
				text=ce.getMessage();
				if (!text.endsWith("\n")) {
          text+="\n";
        }
			}else if (loggername!=null && loggername.equals(Console.SYSTEMERR_LOGGER)){
				if (!showErr) {
          return;
        }
				textcolor=AConsole.getSystemErrText().get();
				text=ce.getMessage();
				if (!text.endsWith("\n")) {
          text+="\n";
        }
			}else{
				if (loggerFilterText!=null && !(ce.getLoggerName().startsWith(loggerFilterText) && ce.getLevel().intValue()>=loggerFilterLevel.intValue())) {
          return;
        }
				int level=ce.getLevel().intValue();
				if (level>=Level.SEVERE.intValue()){
					textcolor=AConsole.getLevelSevereText().get();
				}else if (level>=Level.WARNING.intValue()){
					textcolor=AConsole.getLevelWarningText().get();
				}else if (level>=Level.INFO.intValue()){
					textcolor=AConsole.getLevelInfoText().get();
				}else{
					textcolor=AConsole.getLevelOtherText().get();
				}
				text=ce.toString(formatter);
			}
			moveToEndOfOutputArea=true;
			DefaultStyledDocument doc=(DefaultStyledDocument)outputTextArea.getStyledDocument();
			MutableAttributeSet attr = new SimpleAttributeSet();
			StyleConstants.setForeground(attr, textcolor);

			try {
				doc.insertString(doc.getLength(),text,attr);
			} catch (BadLocationException e1) {
				e1.printStackTrace();
			}

			if (outputTextArea.isDisplayable()) {
				try {
					outputTextArea.setCaretPosition(Integer.MAX_VALUE); // Scroll to end of window
				} catch (Exception e) {} // Just for safety
			}
			validate();
			if (!fastscroll) {
        componentResized(null);
      }
		}catch (Exception e){
			AConsole.mLog.warning("skipping message because it caused an exception: "+e.toString());
			if (console!=null && console.systemerr!=null) {
        e.printStackTrace(console.systemerr);
      }
			return;
		}
	}
	public void clear() {
		outputTextArea.setText("");
		validate();
		JScrollBar sb=jScrollPane1.getVerticalScrollBar();
		if (sb!=null) {
      sb.setValue(0);
    }
		try {
			outputTextArea.setCaretPosition(0); // Scroll to begin of window
		} catch (Exception e) {} // Just for safety
		return;
	}
	public boolean save(String filename){
		if (!initGui) {
      return false;
    }
		System.err.print("Dumping console content to "+filename+"...");
		Vector<String> textvec=new Vector<String>();
		StringReader sr=new StringReader(outputTextArea.getText());
		BufferedReader br=new BufferedReader(sr);
		String oneline=null;
		do {
			try {
				oneline=br.readLine();
			} catch (Exception ex) {}
			if (oneline!=null) {
        textvec.addElement(oneline);
      }
		} while (oneline!=null);
		try {
			writefromvec(textvec,filename);
		} catch (Exception ex) {
			System.err.println("Save caused "+ex);
			return false;
		}
		System.err.println("OK");
		return true;
	}
	/* intern stuff *********************************************/
	private static void writefromvec(Vector<String> vec, String filename) throws IOException
	{
		if (vec==null) {
      return;
    }
		BufferedWriter outfile;
		outfile = new BufferedWriter(new FileWriter(filename));
		for (int i=0;i<vec.size();i++) {
			String thisline= (vec.elementAt(i));
			outfile.write(thisline);
			outfile.newLine();
		}
		outfile.close();
	}

	public void componentHidden(ComponentEvent e){
	}
	public void componentMoved(ComponentEvent e){
	}
	public void componentResized(ComponentEvent e){
		if (!initGui) {
      return;
    }
		if (moveToEndOfOutputArea){
			try{
				Dimension d=outputTextArea.getSize();
				if (d.height>10){
					Rectangle r=new Rectangle(0,d.height-1,1,d.height);
					outputTextArea.scrollRectToVisible(r);
					moveToEndOfOutputArea=false;
				}
			}catch (RuntimeException re){
				//what the ...
			}
		}
	}
	public void componentShown(ComponentEvent e){
	}
	/**
	 * @see tvbconsole.Console.Listener#shutdownConsole()
	 */
	public void shutdownConsole() {
		initGui=false;
		clear();
		console.removeListener(this);
		if (console!=null) {
      console=null;
    }
	}
	public static JButton getOpenHelpButton(final JFrame frame) {
		JButton helpBtn = new JButton(mLocalizer.msg("help", "help"));
		helpBtn.setToolTipText(mLocalizer.msg("help_tt", "displays a short howto for this plugin"));
		helpBtn.addActionListener(new AbstractAction() {
			private static final long serialVersionUID = 1650632281184882665L;

			public void actionPerformed(ActionEvent evt) {
				try{
					final String filename="index.html";
					TVBUtilitiesHelpDialog.showHelpPage(frame,AConsole.getHelpUrl(filename),mLocalizer.msg("titleHelp", "Help"));
				}catch (Exception ex){
					AConsole.foundABug(ex);
				}
			}
		});
		return helpBtn;
	}


}
