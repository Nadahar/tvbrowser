/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */
 
 
package tvbrowser.core.filters;

import java.io.*;
import java.util.*;



import util.exc.*;


class Token {
 
      final static int OR=0, AND=1, NOT=2, ITEM=3, LEFT_BRACKET=4, RIGHT_BRACKET=5;
      
  private static final util.ui.Localizer mLocalizer
             = util.ui.Localizer.getLocalizerFor(Token.class);
      
      public int type;
      public String value;  
      public int pos;  
    
      public Token() {
      }
    
      public String toString() {
        switch (type) {
          case OR: return "OR"; 
          case AND: return "AND";
          case NOT: return "NOT"; 
          case LEFT_BRACKET: return "(";
          case RIGHT_BRACKET: return ")";
          case ITEM: return value;
          default : return mLocalizer.msg("invalidToken","invalid token"); 
        }
      }
  } // class Token

public class UserFilter implements devplugin.ProgramFilter {
  
  private static final util.ui.Localizer mLocalizer
           = util.ui.Localizer.getLocalizerFor(UserFilter.class);
 
  
  private String mName, mRule;
  private File mFile;
  
  private static Token[] tokenList=null;  
  private static int curInx, curTokenInx;
  private static char[] ruleLine;
  private static Token curToken;
    
  private Node root, curNode;
  
  
  public UserFilter(String name) {
    mName=name;
  }

  public UserFilter(File file) throws ParserException {
         mFile=file;
         ObjectInputStream in=null;
         try {
             in=new ObjectInputStream(new FileInputStream(file));            
             int version=in.readInt();
             mName=(String)in.readObject();
             mRule=(String)in.readObject();        
         }catch (IOException e) {
             ErrorHandler.handle("Could not read filter from file", e);
         }catch (ClassNotFoundException e) {
             ErrorHandler.handle("Could not read filter from file", e);
         }finally {
             try { if (in!=null) in.close(); } catch (IOException e) {}
         }
        
         try {
           in.close();
         }catch(IOException e) {}
        
         createTokenTree();            
     }
    
  public void store() {
    ObjectOutputStream out=null;
    try {
      out=new ObjectOutputStream(new FileOutputStream(new File(tvbrowser.core.filters.FilterList.FILTER_DIRECTORY,mName+".filter")));
      out.writeInt(1);  // version
      out.writeObject(mName);
      out.writeObject(mRule);
    }catch (IOException e) {
      ErrorHandler.handle("Could not write filter to file", e); 
    }finally {
      try { if (out!=null) out.close(); } catch (IOException e) {}
    }
      
  }
  
  public void delete() {
    if (mFile!=null) {
      mFile.delete();   
    }    
  }
    
  
  private void createTokenTree() throws ParserException {
    tokenList=createTokenList(mRule);
    curTokenInx=-1;
    curToken=getNextToken();
      if (curToken!=null) {
        Node rule=rule();
        if (curToken!=null) {
          //throw new ParserException(curToken.pos,mLocalizer.msg("EOLExpected","end of rule expected")); 
        }
        root=rule;
      }
    }
    
  
  private static Token getNextToken() throws ParserException {
    curTokenInx++;
    if (tokenList.length>curTokenInx) {
      return tokenList[curTokenInx];
    }
    return null;
  }
  
  public static void testTokenTree(String rule) throws ParserException {
    tokenList=createTokenList(rule);
    curTokenInx=-1;
    curToken=getNextToken();
    if (curToken!=null) {
      rule();
      if (curToken!=null) {
          throw new ParserException(curToken.pos,mLocalizer.msg("EOLExpected","end of rule expected")); 
      }
    }
  }
  
  private static Token[] createTokenList(String rule) {
        
          ruleLine=rule.toCharArray();
          curInx=0;
          Token curToken=null;
          ArrayList list=new ArrayList();
          do {
              curToken=readNextToken();    
              if (curToken!=null) {
                  list.add(curToken);
              }
          }while (curToken!=null);
        
          Token[] result=new Token[list.size()];
          for (int i=0;i<result.length;i++) {
              result[i]=(Token)list.get(i);
          }
          return result;
      }
  private static Token readNextToken() {
        
        
    ignoreSpaces();
    int i=curInx;
        
    if (curInx==ruleLine.length) {
      return null;
    }
        
    Token result=new Token();
    result.pos=curInx;
        
    if (ruleLine[curInx]=='(') {
      result.type=Token.LEFT_BRACKET;
      curInx++;
    }else if (ruleLine[curInx]==')') {
      result.type=Token.RIGHT_BRACKET;
      curInx++;
    }else {
      readString();
      result.value=new String(ruleLine,i,curInx-i);
      if ("or".equalsIgnoreCase(result.value) || mLocalizer.msg("or", "or").equalsIgnoreCase(result.value)) {
        result.type=Token.OR;
      }else if ("and".equalsIgnoreCase(result.value)  || mLocalizer.msg("and", "and").equalsIgnoreCase(result.value)) {
        result.type=Token.AND;
      }else if ("not".equalsIgnoreCase(result.value) || mLocalizer.msg("not", "not").equalsIgnoreCase(result.value)) {
        result.type=Token.NOT;
      }else {
        result.type=Token.ITEM;
      }
    }       
    return result;
  }    
      
  private static void readString() {
    while (curInx<ruleLine.length
                 && !Character.isWhitespace(ruleLine[curInx])
                 && ruleLine[curInx]!='('
                 && ruleLine[curInx]!=')') {
      curInx++;
    }
  }    
      
  private static void ignoreSpaces() {
    while (curInx<ruleLine.length && Character.isWhitespace(ruleLine[curInx])) {
      curInx++;
    }   
  }
      
  private static void expectToken(int[] type, Token got) throws ParserException {
          if (got==null) {
              throw new ParserException(mLocalizer.msg("unexpectedEOL","unexpected end of rule"));
          }
        
          for (int i=0;i<type.length;i++) {
            if (type[i]==got.type) {
              return;
            }
          }
        
          String msg="";
          for (int i=0;i<type.length;i++) {
            if (type[i]==Token.AND) {
              msg+="'"+mLocalizer.msg("and", "and")+"'";
            }else if (type[i]==Token.ITEM) {
              msg+=mLocalizer.msg("componentName","component name");
            }else if (type[i]==Token.LEFT_BRACKET) {
              msg+="'('";
            }else if (type[i]==Token.RIGHT_BRACKET) {
              msg+="')'";
            }else if (type[i]==Token.NOT) {
              msg+="'"+mLocalizer.msg("not", "not")+"'";
            }else if (type[i]==Token.OR) {
              msg+="'"+mLocalizer.msg("or", "or")+"'";
            }
            if (i<type.length-1) {
              msg+=", ";
            }
          }
          msg+=mLocalizer.msg("expected","expected");
          throw new ParserException(got.pos,msg);
      }
        
      
        
  private static Node rule() throws ParserException {
    Node result=new OrNode();
    result.addNode(condTerm());
        
    while (curToken!=null && curToken.type==Token.OR) {
      curToken=getNextToken();
      if (curToken==null) {
        //throw new ParserException(mLocalizer.msg("unexpectedEOL","unexpected end of rule"));
      }
      result.addNode(condTerm());               
    }
        
    return result;
  }  
      
  private static Node condTerm() throws ParserException {
         Node result=new AndNode();
        
         result.addNode(condFact());
         while (curToken!=null && curToken.type==Token.AND) {
            curToken=getNextToken();
            if (curToken==null) {
              //  throw new ParserException(mLocalizer.msg("unexpectedEOL","unexpected end of rule"));
            }
            result.addNode(condFact());                
          }  
        return result;
     }
    
     private static Node condFact() throws ParserException {
         Node result, notNode=null;
        
       if (curToken==null) {
         throw new ParserException(mLocalizer.msg("unexpectedEOL","unexpected end of rule"));
       }
        
        
         if (curToken.type==Token.NOT) {
           notNode=new NotNode();
           curToken=getNextToken();
           if (curToken==null) {
             throw new ParserException(mLocalizer.msg("unexpectedEOL","unexpected end of rule"));
           }
            
         }
        
      
         if (curToken.type==Token.LEFT_BRACKET) {
             curToken=getNextToken();
             result=rule();
             expectToken(new int[]{Token.RIGHT_BRACKET}, curToken);
             curToken=getNextToken();
         }
        
         else {
             result=item();
             curToken=getNextToken();
         }
        
         if (notNode!=null) {
           notNode.addNode(result);
           return notNode;
         }
        
         return result;
     }
    
 
     private static Node item() throws ParserException {
         Token tk=curToken;
         if (tk.type!=Token.ITEM) {
             throw new ParserException(mLocalizer.msg("compExpected","component name expected."));
         }
         
         
         FilterComponent component = FilterComponentList.getInstance().getFilterComponentByName(tk.value);
         if (component!=null) {
           return new ItemNode(component);
         }
         throw new ParserException(mLocalizer.msg("invalidCompName","{0} is not a valid component name",tk.value));
     }    
  
  public boolean accept(devplugin.Program prog) {
    if (root==null) {
      return false;        
    }
    return root.accept(prog);
  }
  
  public void setName(String name) {
    if (!name.equals(mName) && mFile!=null) {
      mFile.delete();
    }
    mName=name;
  }

  public String getName() {
      return mName;
  }
  
  public String toString() { 
    return mName;
  }
  
  public void setRule(String rule) throws ParserException {
    mRule=rule;
    createTokenTree();
  }
      
  public String getRule() {
    return mRule;
  }
  
  public boolean containsRuleComponent(String comp) {
    boolean res=root.containsRuleComponent(comp);
    return res;
  } 
  
}
  
  abstract class Node {
 
      protected HashSet nodes;
 
      public Node() {
          nodes=new HashSet();
      }
    
      public void addNode(Node n) {
          nodes.add(n);
      }
        
      public abstract boolean accept(devplugin.Program prog);
    
      public abstract void dump();
    
      public boolean containsRuleComponent(String compName) {
          Iterator it=nodes.iterator();
          while (it.hasNext()) {
              Node n=(Node)it.next();
              if (n.containsRuleComponent(compName)) {
                return true;
              }
          }
          return false;  
      }     
  }

  class OrNode extends Node {
      public OrNode() {
          super();
      } 
    
      public boolean accept(devplugin.Program prog) {
          Iterator it=nodes.iterator();
          while (it.hasNext()) {
              Node n=(Node)it.next();
              if (n.accept(prog)) {
                  return true;    
              }
          }        
          return false;   
      }
    
      public void dump() {
          Iterator it=nodes.iterator();
          while (it.hasNext()) {
              Node n=(Node)it.next();
              n.dump();
          }
          System.out.println("}");        
      }
    
  

  }
    
  class AndNode extends Node {
      public AndNode() {
          super();
      }
    
      public boolean accept(devplugin.Program prog) {
          Iterator it=nodes.iterator();
          while (it.hasNext()) {
              Node n=(Node)it.next();
              if (!n.accept(prog)) {
                  return false;    
              }
          }        
          return true;   
      }
    
      public void dump() {
          System.out.println("AndNode {");
          Iterator it=nodes.iterator();
          while (it.hasNext()) {
              Node n=(Node)it.next();
              n.dump();
          }        
          System.out.println("}");   
      }
  }

  class NotNode extends Node {
      private Node n;
      public NotNode() {
      }
    
      public void addNode(Node n) {
          this.n=n;
      }
    
      public boolean accept(devplugin.Program prog) {
          return !n.accept(prog);        
      }
    
      public void dump() {
          System.out.println("NotNode { }");
      }
  }

  class ItemNode extends Node {
      private FilterComponent mRule;
      public ItemNode(FilterComponent rule) {
          mRule=rule;            
      }
    
      public boolean accept(devplugin.Program prog) {
          return mRule.accept(prog);        
      }
    
      public void dump() {
          System.out.println("ItemNode {"+mRule.toString()+" }");
      }
    
      public boolean containsRuleComponent(String compName) {
        return (mRule.getName().equalsIgnoreCase(compName)); 
      }
    
  }
    
  
  
