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

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Pattern;

import util.exc.ErrorHandler;
import util.io.stream.ObjectInputStreamProcessor;
import util.io.stream.ObjectOutputStreamProcessor;
import util.io.stream.StreamUtilities;
import devplugin.ProgramFilter;

class Token {

  final static int OR = 0, AND = 1, NOT = 2, ITEM = 3, LEFT_BRACKET = 4,
      RIGHT_BRACKET = 5;

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(Token.class);

  public int type;
  public String value;
  public int pos;

  public Token() {
  }

  public String toString() {
    switch (type) {
    case OR:
      return "OR";
    case AND:
      return "AND";
    case NOT:
      return "NOT";
    case LEFT_BRACKET:
      return "(";
    case RIGHT_BRACKET:
      return ")";
    case ITEM:
      return value;
    default:
      return mLocalizer.msg("invalidToken", "invalid token");
    }
  }
} // class Token

public class UserFilter implements devplugin.ProgramFilter {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(UserFilter.class);

  private String mName, mRule;
  private File mFile;

  private static Token[] mTokenList = null;
  private static int mCurInx, curTokenInx;
  private static char[] mRuleLine;
  private static Token mCurToken;

  private Node mRoot;

  public UserFilter(String name) {
    mName = name;
  }

  public UserFilter(File file) throws ParserException {
    mFile = file;
    try {
      StreamUtilities.objectInputStream(file, 0x1000,
          new ObjectInputStreamProcessor() {

            @Override
            public void process(final ObjectInputStream in) throws IOException {
              in.readInt(); // version not yet used
              try {
                mName = (String) in.readObject();
                mRule = (String) in.readObject();
                mRule = modifyRule(mRule, false);
              } catch (ClassNotFoundException e) {
                ErrorHandler.handle("Could not read filter from file", e);
              }
            }
          });
    } catch (IOException e) {
      ErrorHandler.handle("Could not read filter from file", e);
    }

    createTokenTree();
  }

  public void store() {
    try {
      mRule = modifyRule(mRule, true);
      final File file = new File(
          tvbrowser.core.filters.FilterList.FILTER_DIRECTORY, mName + ".filter");
      StreamUtilities.objectOutputStream(file,
          new ObjectOutputStreamProcessor() {
            public void process(ObjectOutputStream out) throws IOException {
              out.writeInt(1); // version
              out.writeObject(mName);
              out.writeObject(mRule);
            }
          });
    } catch (IOException e) {
      ErrorHandler.handle("Could not write filter to file", e);
    }
  }

  private String modifyRule(String rule, boolean normalize) {
    if (rule == null) {
      return rule;
    }
    for (String keyWord : new String[]{"or","and","not"}) {
      String localized = mLocalizer.msg(keyWord, keyWord);
      if (localized.length() > 0) {
        String normalized = keyWord.toUpperCase();
        if (normalize) {
          rule = rule.replaceAll("\\b" + Pattern.quote(localized) + "\\b", normalized);
        }
        else {
          rule = rule.replaceAll("\\b" + Pattern.quote(normalized) + "\\b", localized);
        }
      }
    }
    return rule;
  }

  public void delete() {
    if (mFile != null) {
      mFile.delete();
    }
  }

  private void createTokenTree() throws ParserException {
    mTokenList = createTokenList(mRule);
    curTokenInx = -1;
    mCurToken = getNextToken();
    if (mCurToken != null) {
      Node rule = rule();
      if (mCurToken != null) {
        // throw new
        // ParserException(curToken.pos,mLocalizer.msg("EOLExpected","end of rule expected"));
      }
      mRoot = rule;
    }
  }

  private static Token getNextToken() throws ParserException {
    curTokenInx++;
    if (mTokenList.length > curTokenInx) {
      return mTokenList[curTokenInx];
    }
    return null;
  }

  public static void testTokenTree(String rule) throws ParserException {
    mTokenList = createTokenList(rule);
    curTokenInx = -1;
    mCurToken = getNextToken();
    if (mCurToken != null) {
      rule();
      if (mCurToken != null) {
        throw new ParserException(mCurToken.pos, mLocalizer.msg("EOLExpected",
            "end of rule expected"));
      }
    }
  }

  private static Token[] createTokenList(String rule) {

    mRuleLine = rule.toCharArray();
    mCurInx = 0;
    Token curToken = null;
    ArrayList<Token> list = new ArrayList<Token>();
    do {
      curToken = readNextToken();
      if (curToken != null) {
        list.add(curToken);
      }
    } while (curToken != null);

    Token[] result = new Token[list.size()];
    for (int i = 0; i < result.length; i++) {
      result[i] = list.get(i);
    }
    return result;
  }

  private static Token readNextToken() {

    ignoreSpaces();
    int i = mCurInx;

    if (mCurInx == mRuleLine.length) {
      return null;
    }

    Token result = new Token();
    result.pos = mCurInx;

    if (mRuleLine[mCurInx] == '(') {
      result.type = Token.LEFT_BRACKET;
      mCurInx++;
    } else if (mRuleLine[mCurInx] == ')') {
      result.type = Token.RIGHT_BRACKET;
      mCurInx++;
    } else {
      readString();
      result.value = new String(mRuleLine, i, mCurInx - i);
      if ("or".equalsIgnoreCase(result.value)
          || "oder".equalsIgnoreCase(result.value)
          || mLocalizer.msg("or", "or").equalsIgnoreCase(result.value)) {
        result.type = Token.OR;
      } else if ("and".equalsIgnoreCase(result.value)
          || "und".equalsIgnoreCase(result.value)
          || mLocalizer.msg("and", "and").equalsIgnoreCase(result.value)) {
        result.type = Token.AND;
      } else if ("not".equalsIgnoreCase(result.value)
          || "nicht".equalsIgnoreCase(result.value)
          || mLocalizer.msg("not", "not").equalsIgnoreCase(result.value)) {
        result.type = Token.NOT;
      } else {
        result.type = Token.ITEM;
      }
    }
    return result;
  }

  private static void readString() {
    while (mCurInx < mRuleLine.length
        && !Character.isWhitespace(mRuleLine[mCurInx]) && mRuleLine[mCurInx] != '('
        && mRuleLine[mCurInx] != ')') {
      mCurInx++;
    }
  }

  private static void ignoreSpaces() {
    while (mCurInx < mRuleLine.length && Character.isWhitespace(mRuleLine[mCurInx])) {
      mCurInx++;
    }
  }

  private static void expectToken(int[] expectedTypes, Token foundToken) throws ParserException {
    if (foundToken == null) {
      throw new ParserException(mLocalizer.msg("unexpectedEOL",
          "unexpected end of rule"));
    }

    for (int expectedType : expectedTypes) {
      if (expectedType == foundToken.type) {
        return;
      }
    }

    String msg = "";
    for (int i = 0; i < expectedTypes.length; i++) {
      if (expectedTypes[i] == Token.AND) {
        msg += "'" + mLocalizer.msg("and", "and") + "'";
      } else if (expectedTypes[i] == Token.ITEM) {
        msg += mLocalizer.msg("componentName", "component name");
      } else if (expectedTypes[i] == Token.LEFT_BRACKET) {
        msg += "'('";
      } else if (expectedTypes[i] == Token.RIGHT_BRACKET) {
        msg += "')'";
      } else if (expectedTypes[i] == Token.NOT) {
        msg += "'" + mLocalizer.msg("not", "not") + "'";
      } else if (expectedTypes[i] == Token.OR) {
        msg += "'" + mLocalizer.msg("or", "or") + "'";
      }
      if (i < expectedTypes.length - 1) {
        msg += ", ";
      }
    }
    msg += mLocalizer.msg("expected", "expected");
    throw new ParserException(foundToken.pos, msg);
  }

  private static Node rule() throws ParserException {
    Node result = new OrNode();
    result.addNode(condTerm());

    while (mCurToken != null && mCurToken.type == Token.OR) {
      mCurToken = getNextToken();
      if (mCurToken == null) {
        // throw new
        // ParserException(mLocalizer.msg("unexpectedEOL","unexpected end of rule"));
      }
      result.addNode(condTerm());
    }

    return result.optimize();
  }

  private static Node condTerm() throws ParserException {
    Node result = new AndNode();

    result.addNode(condFact());
    while (mCurToken != null && mCurToken.type == Token.AND) {
      mCurToken = getNextToken();
      if (mCurToken == null) {
        // throw new
        // ParserException(mLocalizer.msg("unexpectedEOL","unexpected end of rule"));
      }
      result.addNode(condFact());
    }
    return result;
  }

  private static Node condFact() throws ParserException {
    Node result, notNode = null;

    if (mCurToken == null) {
      throw new ParserException(mLocalizer.msg("unexpectedEOL",
          "unexpected end of rule"));
    }

    if (mCurToken.type == Token.NOT) {
      notNode = new NotNode();
      mCurToken = getNextToken();
      if (mCurToken == null) {
        throw new ParserException(mLocalizer.msg("unexpectedEOL",
            "unexpected end of rule"));
      }

    }

    if (mCurToken.type == Token.LEFT_BRACKET) {
      mCurToken = getNextToken();
      result = rule();
      expectToken(new int[] { Token.RIGHT_BRACKET }, mCurToken);
      mCurToken = getNextToken();
    }

    else {
      result = item();
      mCurToken = getNextToken();
    }

    if (notNode != null) {
      notNode.addNode(result);
      return notNode;
    }

    return result;
  }

  private static Node item() throws ParserException {
    Token tk = mCurToken;
    if (tk.type != Token.ITEM) {
      throw new ParserException(mLocalizer.msg("compExpected",
          "component name expected."));
    }

    FilterComponent component = FilterComponentList.getInstance()
        .getFilterComponentByName(tk.value);
    if (component != null) {
      return new ItemNode(component);
    }
    throw new ParserException(mLocalizer.msg("invalidCompName",
        "{0} is not a valid component name", tk.value));
  }

  public boolean accept(devplugin.Program prog) {
    if (mRoot == null) {
      return false;
    }
    return mRoot.accept(prog);
  }

  public void setName(String name) {
    if (!name.equals(mName) && mFile != null) {
      mFile.delete();
    }
    mName = name;
  }

  public String getName() {
    return mName;
  }

  public String toString() {
    return mName;
  }

  public void setRule(String rule) throws ParserException {
    mRule = rule;
    createTokenTree();
  }

  public String getRule() {
    return mRule;
  }

  public boolean containsRuleComponent(String comp) {
    if (mRoot == null) {
      return false;
    }
    return mRoot.containsRuleComponent(comp);
  }

  public boolean equals(Object o) {
    if (o instanceof ProgramFilter) {
      return getClass().equals(o.getClass())
          && getName().equals(((ProgramFilter) o).getName());
    }

    return false;
  }
}

abstract class Node {

  protected HashSet<Node> mNodes;

  public Node() {
    mNodes = new HashSet<Node>();
  }

  public Node optimize() {
    return this;
  }

  public void addNode(Node n) {
    mNodes.add(n);
  }

  public abstract boolean accept(devplugin.Program prog);

  public boolean containsRuleComponent(String compName) {
    Iterator<Node> it = mNodes.iterator();
    while (it.hasNext()) {
      Node n = it.next();
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
    Iterator<Node> it = mNodes.iterator();
    while (it.hasNext()) {
      Node n = it.next();
      if (n.accept(prog)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Node optimize() {
    if (mNodes.size() == 1) {
      return mNodes.iterator().next();
    }
    return super.optimize();
  }
}

class AndNode extends Node {
  public AndNode() {
    super();
  }

  public boolean accept(devplugin.Program prog) {
    Iterator<Node> it = mNodes.iterator();
    while (it.hasNext()) {
      Node n = it.next();
      if (!n.accept(prog)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public Node optimize() {
    if (mNodes.size() == 1) {
      return mNodes.iterator().next();
    }
    return super.optimize();
  }
}

class NotNode extends Node {
  private Node mNode = null;

  public NotNode() {
  }

  public void addNode(Node node) {
    this.mNode = node;
  }

  public boolean accept(devplugin.Program prog) {
    return !mNode.accept(prog);
  }
}

class ItemNode extends Node {
  private FilterComponent mRule;

  public ItemNode(FilterComponent rule) {
    mRule = rule;
  }

  public boolean accept(devplugin.Program prog) {
    return mRule.accept(prog);
  }

  public boolean containsRuleComponent(String compName) {
    return (mRule.getName().equalsIgnoreCase(compName));
  }

}
