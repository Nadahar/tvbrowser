package tvbnetcontrol;

public class KeyCommand {
  private static final int NO_MODIFIER = 0;
  public static final int CTRL = 1;
  public static final int SHIFT = 2;
  public static final int ALT = 4;
  public static final int FOCUS = 8;
  
  private String mKey;
  private int mModifier;
  
  public KeyCommand(String key) {
    this(key,NO_MODIFIER);
  }
  
  public KeyCommand(String key, int modifier) {
    mKey = key;
    mModifier = modifier;
  }
  
  public String getCommandString() {
    StringBuilder build = new StringBuilder("k");
    
    if(isModifier(mModifier,FOCUS)) {
      build.append("f");
    }
    
    build.append("_");
    
    boolean previousModifierFound = false;
    
    if(isModifier(mModifier,CTRL)) {
      build.append(Commands.CTRL);
      previousModifierFound = true;
    }
    if(isModifier(mModifier,SHIFT)) {
      if(previousModifierFound) {
        build.append("+");
      }
      
      build.append(Commands.SHIFT);
      previousModifierFound = true;
    }
    if(isModifier(mModifier,ALT)) {
      if(previousModifierFound) {
        build.append("+");
      }
      
      build.append(Commands.ALT);
      previousModifierFound = true;
    }
    
    if(previousModifierFound) {
      build.append("+");
    }
    
    build.append(mKey);
    
    return build.toString();
  }
  
  private boolean isModifier(int test,int modifier) {
    return (test & modifier) == modifier;
  }
}
