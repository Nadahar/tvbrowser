package rememberme;

import java.util.ArrayList;

import devplugin.Program;

public class RememberedProgramsList<E> extends ArrayList<E> {
  @Override
  public boolean contains(Object o) {
    if(o instanceof Program) {
      for(int i = 0; i < size(); i++) {
        if(get(i).equals(o)) {
          return true;
        }
      }
    }
    
    return super.contains(o);
  }
  
  @Override
  public boolean remove(Object o) {
    if(o instanceof Program) {
      for(int i = 0; i < size(); i++) {
        if(get(i).equals(o)) {
          remove(i);
          return true;
        }
      }
    }
    
    return super.remove(o);
  }
}
