package test.tvbrowser.extras.favoritesplugin.core;

import tvbrowser.extras.favoritesplugin.core.ActorSearcher;
import util.exc.TvBrowserException;
import junit.framework.TestCase;

public class ActorStringSearcherTest extends TestCase {

  /*
   * Test method for 'tvbrowser.extras.favoritesplugin.core.ActorStringSearcher.actorInProgram(String, String)'
   */
  public void testActorInProgramStringString() {
//    assertFalse(actorInProgram("Max Mustermann",(String)null));
    assertFalse(actorInProgram("Max Mustermann",""));
    assertFalse(actorInProgram("Je Li","Jet Lie"));
    assertTrue( actorInProgram("Max Mustermann","The Actor Max Mustermann is"));
    assertTrue( actorInProgram("Max Mustermann","The Actor Max Maria Mustermann"));
    assertTrue( actorInProgram("Max Mustermann","The Actor Max Maria Mustermann is"));
    assertTrue( actorInProgram("Max Mustermann","The Actor Mustermann, Max is"));
    assertTrue( actorInProgram("Max Mustermann","The Actor Mustermann, Max Maria is"));
    assertTrue( actorInProgram("Max Mustermann","The Actor Max M. Mustermann is"));
    assertTrue( actorInProgram("Max Mustermann","The Actor M. M. Mustermann is"));
    assertTrue( actorInProgram("Max Mustermann","The Actor M. Mustermann is"));
    assertTrue( actorInProgram("Courtney Cox Thorne-Smith","Jim Belushi (Jim), Courtney Thorne-Smith (Cheryl), Ki"));
    assertTrue( actorInProgram("Max Mustermann-Hauser","The Actor Max Mustermann-Hauser is"));
    assertTrue( actorInProgram("Max Maria Mustermann","The Actor M. Mustermann is"));
    assertTrue( actorInProgram("Max Maria Mustermann","The Actor Max Mustermann is"));

    assertTrue( actorInProgram("Max Maria Mustermann","The Actor M.\n Mustermann is"));
    assertTrue( actorInProgram("Max Maria Mustermann","The Actor\nMax Mustermann is"));

    
    assertFalse( actorInProgram("Max Maria Mustermann","The Actor M. Mustermannchen is"));
    assertFalse( actorInProgram("Max Maria Mustermann","The Actor Maria Mustermannchen is"));
    assertFalse( actorInProgram("Max Maria Mustermann","The Actor Maxchen Mustermannchen is"));
    assertFalse( actorInProgram("Max Maria Mustermann","The Actor Testmax Mustermannchen is"));

  
  }

  private boolean actorInProgram(String actor, String programText) {
    try {
      ActorSearcher searcher = new ActorSearcher(actor);
      // currently not possible due to visibility issues
//      return searcher.matches(programText);
    } catch (TvBrowserException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return false;
  }

}
