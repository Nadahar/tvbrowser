/*
 * TV-Browser Copyright (C) 04-2003 Martin Oberhauser
 * (darras@users.sourceforge.net)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * CVS information: $RCSfile$ $Source:
 * /cvsroot/tvbrowser/tvbrowser/src/tvbrowser/core/filters/filtercomponents/TimeFilterComponent.java,v $
 * $Date$ $Author$ $Revision$
 */

package tvbrowser.core.filters.filtercomponents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import util.exc.ErrorHandler;
import util.ui.LineNumberHeader;
import util.ui.beanshell.BeanShellEditor;
import bsh.EvalError;
import bsh.Interpreter;
import devplugin.Plugin;
import devplugin.Program;
import devplugin.beanshell.BeanShellProgramFilterIf;

/**
 * This Filter allows the User to create a small Script that Filters the
 * Programs
 * 
 */
public class BeanShellFilterComponent extends AbstractFilterComponent {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(BeanShellFilterComponent.class);

  private BeanShellEditor mScriptEditor;

  private String mScriptSource;

  private BeanShellProgramFilterIf mScript;

  public BeanShellFilterComponent() {
    this("", "");
  }

  public BeanShellFilterComponent(String name, String description) {
    super(name, description);
    mScriptSource = "import devplugin.beanshell.BeanShellProgramFilterIf;\n"
        + "import devplugin.Program;\n"
        + "import devplugin.ProgramFieldType;\n\n" + "accept(Program p) {\n\n"
        + "	// " + mLocalizer.msg("addCodeHere", "Add Code here!") + "\n\n"
        + "	return true;\n" + "}\n\n"
        + "return (BeanShellProgramFilterIf) this;";
  }

  private void compileSource() throws EvalError {
    if (mScriptSource != null && mScript == null) {
      mScript = (BeanShellProgramFilterIf) new Interpreter()
          .eval(mScriptSource);
    }
  }

  public void read(ObjectInputStream in, int version) throws IOException,
      ClassNotFoundException {
    mScriptSource = (String) in.readObject();
  }

  public void write(ObjectOutputStream out) throws IOException {
    out.writeObject(mScriptSource);
  }

  @Override
  public String toString() {
    return mLocalizer.msg("BeanShellFilter", "BeanShell-Filter");
  }

  public void saveSettings() {
    mScriptSource = mScriptEditor.getText();
    try {
      compileSource();
    } catch (Exception e) {
      e.printStackTrace();
      mScript = null;
    }
  }

  public JPanel getSettingsPanel() {
    JPanel content = new JPanel(new BorderLayout());

    mScriptEditor = new BeanShellEditor();
    mScriptEditor.setText(mScriptSource);

    JScrollPane scrollPane = new JScrollPane(mScriptEditor);
    scrollPane.setBackground(Color.WHITE);
    LineNumberHeader header = new LineNumberHeader(mScriptEditor);
    scrollPane.setRowHeaderView(header);

    content.add(scrollPane, BorderLayout.CENTER);

    JPanel buttonp = new JPanel(new FlowLayout(FlowLayout.RIGHT));

    JButton test = new JButton(mLocalizer.msg("testScript", "Test Script"));

    test.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent arg0) {
        testScript();
      }

    });

    buttonp.add(test);

    content.add(buttonp, BorderLayout.SOUTH);

    return content;
  }

  protected void testScript() {
    boolean error = false;
    try {
      BeanShellProgramFilterIf filter;
      filter = (BeanShellProgramFilterIf) new Interpreter().eval(mScriptEditor
          .getText());
      filter.accept(Plugin.getPluginManager().getExampleProgram());
    } catch (Exception e) {
      ErrorHandler.handle(mLocalizer.msg("errorParsing",
          "Error while parsing Script"), e);
      error = true;
    }

    if (!error) {
      JOptionPane.showMessageDialog(null, mLocalizer.msg("SyntaxOK",
          "Syntax is OK."));
    }
  }

  public boolean accept(Program program) {
    try {
      if (mScript == null) {
        compileSource();
      }
      return mScript.accept(program);
    } catch (Exception e) {
      return false;
    }
  }

  public int getVersion() {
    return 1;
  }

}