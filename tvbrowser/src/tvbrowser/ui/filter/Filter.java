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

package tvbrowser.ui.filter;

import tvbrowser.ui.filter.filters.*;

import java.io.*;
import util.exc.*;
import java.util.*;

class Token {
 
    final static int OR=0, AND=1, NOT=2, ITEM=3, LEFT_BRACKET=4, RIGHT_BRACKET=5;
    public int type;
    public String value;  
    public int pos;  
    
    public Token() {
    }
}


public class Filter implements tvbrowser.ui.programtable.ProgramFilter {
 
    private String mName, mRule;    
    //private java.util.HashSet mRules;
    private File mFile=null;

    private static int curInx, curTokenInx;
    private static char[] ruleLine;
    private static Token[] tokenList=null;
    private static Token curToken;
    
    private Node root, curNode;

 
    public Filter(String name) {
        mName=name;
       //mRules=new java.util.HashSet();
    }

    public Filter(File file) {
        mFile=file;
        ObjectInputStream in=null;
        //mRules=new java.util.HashSet();
        try {
            in=new ObjectInputStream(new FileInputStream(file));            
            int version=in.readInt();
            mName=(String)in.readObject();
            mRule=(String)in.readObject();
            /*
            int rulesCnt=in.readInt();
            for (int i=0;i<rulesCnt;i++) {
                String className=(String)in.readObject();
                if ("tvbrowser.ui.filter.filters.KeywordFilterRule".equals(className)) {
                    mRules.add(new KeywordFilterComponent(in));
                }else if ("tvbrowser.ui.filter.filters.PluginFilterRule".equals(className)) {
                    mRules.add(new PluginFilterComponent(in));
                }else{
                    System.out.println("invalid filter");
                }
                               
            }*/
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
        
        try {
            createTokenTree();            
        }catch (ParserException e) {
            ErrorHandler.handle("Error parsing filter rule",e);
        }
    }
    
    
    
    public void setName(String name) {
        if (!name.equals(mName) && mFile!=null) {
            mFile.delete();
        }
        mName=name;
    }
    
    public void setRule(String rule) throws ParserException {
        mRule=rule;
        createTokenTree();
    }
    
    
    public String getRule() {
        return mRule;
    }
 
    
    
    public String toString() {
        return mName;
    }
    
    public String getName() {
        return mName;
    }
    
    public boolean containsRuleComponent(String comp) {
      return root.containsRuleComponent(comp);
    }
    
    /*
    public void addRule(FilterComponent rule) {
        mRules.add(rule);
    }
    
    public void removeRule(FilterComponent rule) {
        mRules.remove(rule);
    }
    */
    /*
    public FilterComponent[] getRules() {
        Object[] o=mRules.toArray();
      FilterComponent[] result=new FilterComponent[o.length];
        for (int i=0;i<result.length;i++) {
            result[i]=(FilterComponent)o[i];            
        }
        return result;
    }
    */
    public void store(File directory) {
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("directory expected");
        }
        ObjectOutputStream out=null;
        try {
            out=new ObjectOutputStream(new FileOutputStream(new File(directory,mName+".filter")));
            out.writeInt(1);
            out.writeObject(mName);
            out.writeObject(mRule);
            /*
            out.writeInt(mRules.size());
            java.util.Iterator it=mRules.iterator();
            while (it.hasNext()) {
                Object o=it.next();
                out.writeObject(o.getClass().getName());
                ((FilterComponent)o).store(out);
            }*/
        }catch (IOException e) {
            ErrorHandler.handle("Could not write filter to file", e); 
        }finally {
            try { if (out!=null) out.close(); } catch (IOException e) {}
        }
        
        
        
       // accept(null);
    }
    
    
    public void delete() {
        if (mFile!=null) {
            mFile.delete();   
        }    
    }
    
    
    
   /**
    * Rule = CondTerm { OR CondTerm }
    *        | '(' Rule ')'.
    * 
    * CondTerm = CondFact { AND CondFact }
    *            | '(' Rule ')'.
    * 
    * CondFact = Item
    *            | '(' Rule ')'.
    * 
    */
    
    public boolean accept(devplugin.Program prog) {
        if (root==null) {
          return false;        
        }
        return root.accept(prog);
    }
    
    private void createTokenTree() throws ParserException {
        tokenList=createTokenList(mRule);
        curTokenInx=-1;
        curToken=getNextToken();
        if (curToken!=null) {
          root=rule();
        }
    }
    
    public static void testTokenTree(String rule) throws ParserException {
        tokenList=createTokenList(rule);
        curTokenInx=-1;
        curToken=getNextToken();
        if (curToken!=null) {
          rule();
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
    
    private static void ignoreSpaces() {
        while (curInx<ruleLine.length && Character.isWhitespace(ruleLine[curInx])) {
            curInx++;
        }   
    }
    
    
    
    private static void readString() {
        while (curInx<ruleLine.length
                && !Character.isWhitespace(ruleLine[curInx])
                && ruleLine[curInx]!='('
                && ruleLine[curInx]!=')') {
            curInx++;
        }
    }
        
        
    // eins or (zwei or drei and vier) and fünf    
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
            if ("or".equals(result.value)) {
                result.type=Token.OR;
            }else if ("and".equals(result.value)) {
                result.type=Token.AND;
            }else if ("not".equals(result.value)) {
                result.type=Token.NOT;
            }else {
                result.type=Token.ITEM;
            }
        }       
        return result;
    }
    
    
    private static void expectToken(int type) throws ParserException {
        if (curTokenInx>=tokenList.length) {
            throw new ParserException("unexpected end of rule");
        }
        if (tokenList[curTokenInx].type!=type) {
            String msg="";
            if (type==Token.AND) {
                msg="'AND' expected";
            }else if (type==Token.ITEM) {
                msg="filter rule expected";
            }else if (type==Token.LEFT_BRACKET) {
                msg="'(' expected";
            }else if (type==Token.RIGHT_BRACKET) {
                msg="')' exptected";
            }else if (type==Token.NOT) {
                msg="'not' expected";
            }else if (type==Token.OR) {
                msg="'OR' expected";
            }
            throw new ParserException(tokenList[curTokenInx].pos,msg);
        }
    }
    
    
    private static Token getNextToken() {
        curTokenInx++;
        if (tokenList.length>curTokenInx) {
            return tokenList[curTokenInx];
        }
        return null;
    }
    
    private static Node rule() throws ParserException {
        Node result=new OrNode();
        
        if (curToken.type==Token.LEFT_BRACKET) {
            curToken=getNextToken();
            
            result.addNode(rule());
            
            expectToken(Token.RIGHT_BRACKET);
            curToken=getNextToken();
        }
        else {
            
            result.addNode(condTerm());
            
            //curToken=getNextToken();
            while (curToken!=null && curToken.type==Token.OR) {
                curToken=getNextToken();
                if (curToken==null) {
                    throw new ParserException("unexpected end of rule");
                }
                result.addNode(condTerm());               
                //curToken=getNextToken();
            }
        }
      
        return result;
    }
    
    
    private static Node condTerm() throws ParserException {
        Node result=new AndNode();
        
        if (curToken.type==Token.LEFT_BRACKET) {
            curToken=getNextToken();
            result.addNode(rule());
            expectToken(Token.RIGHT_BRACKET);
            curToken=getNextToken();
        }
        else {
            
            result.addNode(condFact());
            
            //curToken=getNextToken();
            
            while (curToken!=null && curToken.type==Token.AND) {
                curToken=getNextToken();
                if (curToken==null) {
                    throw new ParserException("unexpected end of rule");
                }
                result.addNode(condFact());                
                //curToken=getNextToken();
            }  
            
        }
        return result;
    }
    
    private static Node condFact() throws ParserException {
        Node result;
        
        if (curToken.type==Token.LEFT_BRACKET) {
            curToken=getNextToken();
            result=rule();
            expectToken(Token.RIGHT_BRACKET);
            curToken=getNextToken();
        }
        
        else {
            result=item();
            curToken=getNextToken();
        }
        
        
        return result;
    }
    
 
    private static Node item() throws ParserException {
        Token tk=curToken;
        if (tk.type!=Token.ITEM) {
            throw new ParserException("filter rule expected.");
        }
      //  Iterator it=mRules.iterator();
      Iterator it=FilterComponentList.iterator();
        while (it.hasNext()) {
          FilterComponent rule=(FilterComponent)it.next();
          //System.out.println("try: "+rule.getName());
          if (tk.value.equals(rule.getName())) {
            return new ItemNode(rule);                                 
          }            
        }
        throw new ParserException(tk.value+" is not a valid filter rule");
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
        System.out.println("ItemNode {"+mRule.getName()+" }");
    }
    
    public boolean containsRuleComponent(String compName) {
      System.out.print(mRule.getName()+"<-->"+compName+": ");
      boolean result=mRule.getName().equalsIgnoreCase(compName);
      System.out.println(result);
      return (mRule.getName().equalsIgnoreCase(compName)); 
    }
    
}
    
