package test.tvbrowser.extras.favoritesplugin.core;

import tvbrowser.extras.favoritesplugin.core.ActorStringSearcher;
import junit.framework.TestCase;

public class ActorStringSearcherTest extends TestCase {

  /*
   * Test method for 'tvbrowser.extras.favoritesplugin.core.ActorStringSearcher.actorInProgram(String, String)'
   */
  public void testActorInProgramStringString() {
    ActorStringSearcher searcher = new ActorStringSearcher();
    
    assertTrue(searcher.actorInProgram("The Actor Max Maria Mustermann is", "Max Mustermann"));
    assertTrue(searcher.actorInProgram("The Actor Mustermann, Max is", "Max Mustermann"));
    assertTrue(searcher.actorInProgram("The Actor Mustermann, Max Maria is", "Max Mustermann"));
    assertTrue(searcher.actorInProgram("The Actor Max Mustermann is", "Max Mustermann"));
    assertTrue(searcher.actorInProgram("The Actor Max M. Mustermann is", "Max Mustermann"));
    assertTrue(searcher.actorInProgram("The Actor M. M. Mustermann is", "Max Mustermann"));
    assertTrue(searcher.actorInProgram("The Actor M. Mustermann is", "Max Mustermann"));
    assertTrue(searcher.actorInProgram("The Actor M. Mustermann is", "Max Maria Mustermann"));
    assertFalse(searcher.actorInProgram("The Actor M. Mustermannchen is", "Max Maria Mustermann"));
    assertFalse(searcher.actorInProgram("The Actor Maria Mustermannchen is", "Max Maria Mustermann"));
    assertFalse(searcher.actorInProgram("The Actor Maxchen Mustermannchen is", "Max Maria Mustermann"));
    assertFalse(searcher.actorInProgram("The Actor Testmax Mustermannchen is", "Max Maria Mustermann"));
  }

}
