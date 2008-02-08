package util.paramhandler;

import junit.framework.TestCase;
import tvbrowser.core.plugin.PluginManagerImpl;
import util.paramhandler.ParamParser;
import devplugin.PluginManager;

public class ParamParserTest extends TestCase {

  public void testAnalyse() {
    PluginManager manager = PluginManagerImpl.getInstance();
    ParamParser parser = new ParamParser();

    String in = "oblda {clean(\"halloechen du da ?!\")} da \\{ {concat(urlencode(isset(original_title, \"hallo\"), \"utf8\"), \"HHHHH\")} {\"str}i()ng\"}test bla";
    
    System.out.println(in);
    String result = parser.analyse(in, manager.getExampleProgram());
    
    System.out.println(result);
    System.out.println(parser.getErrorString());

    assertEquals("oblda halloechen_du_da_ da { The+WaltonsHHHHH str}i()ngtest bla", result);
  }

}
