package checkerplugin.check;

import devplugin.Program;

public class UnknownProgramCheck extends AbstractCheck {

  @Override
  protected void doCheck(final Program program) {
    if ("***Titel noch nicht bekannt!***".equals(program.getTitle())) {
      addError(mLocalizer.msg("unknownTitle", "Unknown title"));
    }
  }

}
