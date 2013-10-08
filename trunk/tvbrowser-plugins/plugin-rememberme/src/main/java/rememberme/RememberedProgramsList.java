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
  
  public boolean remove(Object o, RememberMe rMe) {
    if(o instanceof Program) {
      for(int i = 0; i < size(); i++) {
        if(get(i).equals(o)) {
          RememberedProgram removedProg = (RememberedProgram)remove(i);
          removedProg.unmark(rMe);
          return true;
        }
      }
    }
    else if(o instanceof RememberedProgram) {
      boolean removed = false;
      
      for(int i = size()-1; i >= 0; i--) {
        if(((RememberedProgram)o).equalsForRemove(get(i))) {
          RememberedProgram removedProg = (RememberedProgram)remove(i);
          removedProg.unmark(rMe);
          removed = true;
        }
      }
      
      if(removed) {
        return true;
      }
    }
    
    return super.remove(o);
  }
}
