package test.tvbrowser.extras.favoritesplugin.core;

import tvbrowser.extras.favoritesplugin.core.ActorStringSearcher;
import junit.framework.TestCase;

public class ActorStringSearcherTest extends TestCase {

  /*
   * Test method for 'tvbrowser.extras.favoritesplugin.core.ActorStringSearcher.actorInProgram(String, String)'
   */
  public void testActorInProgramStringString() {
    assertFalse(new ActorStringSearcher("Max Mustermann").actorInProgram((String)null));
    assertFalse(new ActorStringSearcher("Max Mustermann").actorInProgram(""));
    assertFalse(new ActorStringSearcher("Je Li").actorInProgram("Jet Lie"));
    assertTrue(new ActorStringSearcher("Max Mustermann").actorInProgram("The Actor Max Mustermann is"));
    assertTrue(new ActorStringSearcher("Max Mustermann").actorInProgram("The Actor Max Maria Mustermann"));
    assertTrue(new ActorStringSearcher("Max Mustermann").actorInProgram("The Actor Max Maria Mustermann is"));
    assertTrue(new ActorStringSearcher("Max Mustermann").actorInProgram("The Actor Mustermann, Max is"));
    assertTrue(new ActorStringSearcher("Max Mustermann").actorInProgram("The Actor Mustermann, Max Maria is"));
    assertTrue(new ActorStringSearcher("Max Mustermann").actorInProgram("The Actor Max M. Mustermann is"));
    assertTrue(new ActorStringSearcher("Max Mustermann").actorInProgram("The Actor M. M. Mustermann is"));
    assertTrue(new ActorStringSearcher("Max Mustermann").actorInProgram("The Actor M. Mustermann is"));
    assertTrue(new ActorStringSearcher("Max Maria Mustermann").actorInProgram("The Actor M. Mustermann is"));
    assertTrue(new ActorStringSearcher("Max Maria Mustermann").actorInProgram("The Actor Max Mustermann is"));
    assertFalse(new ActorStringSearcher("Max Maria Mustermann").actorInProgram("The Actor M. Mustermannchen is"));
    assertFalse(new ActorStringSearcher("Max Maria Mustermann").actorInProgram("The Actor Maria Mustermannchen is"));
    assertFalse(new ActorStringSearcher("Max Maria Mustermann").actorInProgram("The Actor Maxchen Mustermannchen is"));
    assertFalse(new ActorStringSearcher("Max Maria Mustermann").actorInProgram("The Actor Testmax Mustermannchen is"));
  }

}
