package checkerplugin.check;

import java.awt.event.KeyEvent;
import java.util.ArrayList;

import devplugin.Program;
import devplugin.ProgramFieldType;

public class PrintableCharactersCheck extends AbstractCheck {
  
  private static final ArrayList<ProgramFieldType> textFields = getFieldTypes(ProgramFieldType.TEXT_FORMAT);

  @Override
  protected void doCheck(final Program program) {
    for (ProgramFieldType textField : textFields) {
      final String content = program.getTextField(textField);
      if (content != null) {
        int length = content.length();
        for (int i = 0; i < length; i++) {
          char c = content.charAt(i);
          if (!Character.isWhitespace(c) && !isPrintable(c)) {
            addError(mLocalizer.msg("nonPrintable",
                "Non printable characters in {0}.", textField.getLocalizedName()));
            break;
          }
        }
      }
    }
  }

  private boolean isPrintable(char c) {
    Character.UnicodeBlock block = Character.UnicodeBlock.of( c );
    if (Character.isISOControl(c) ||
            c == KeyEvent.CHAR_UNDEFINED ||
            block == null ||
            block == Character.UnicodeBlock.SPECIALS) {
      return false;
    }
    return true;
  }
}
