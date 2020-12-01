package ca.welbog.kevbot.responder.math;

import java.util.ArrayList;
import java.util.List;

import ca.welbog.kevbot.responder.math.Token.TokenValue;

/*
 * TokenCollection.java
 *
 * Created on January 13, 2007, 4:56 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

/**
 *
 * @author Inferno
 */
public class TokenCollection {
  private List<Token> tokens;
  private ParseTree2 parser;
  private int calls = 0;

  public static final int MAX_CALLS = 2 << 14;

  public TokenCollection() {
    calls = 0;
    tokens = new ArrayList<>();
  }

  /** Creates a new instance of TokenCollection */
  public TokenCollection(ParseTree2 parseTree) {
    tokens = new ArrayList<>();
    parser = parseTree;
  }

  public void replaceToken(int position, Token replacement) {
    if (position >= 0 && position < tokens.size()) {
      tokens.set(position, replacement);
    }
  }

  public List<Token> getTokens() {
    return tokens;
  }

  public void setTokens(List<Token> tokens) {
    this.tokens = tokens;
  }

  public int size() {
    return tokens.size();
  }

  public Token getToken(int position) {
    if (position < 0 || position >= size()) {
      throw new IndexOutOfBoundsException();
    }
    return tokens.get(position);
  }

  public void add(Token t) {
    tokens.add(t);
  }

  public TokenCollection clone() {
    TokenCollection clone = new TokenCollection(this.parser);
    for (int i = 0; i < this.size(); i++) {
      clone.add(this.getToken(i).clone());
    }
    clone.calls = this.calls;
    return clone;
  }

  public double evaluateInitial() throws Exception {
    calls = 0;
    if (parser == null) {
      parser = new ParseTree2();
    }
    return parser.parseInitial(this);
  }

  public double evaluate() throws Exception {
    calls++;
    if (calls >= MAX_CALLS) {
      return 0.0;
    }
    if (parser == null) {
      parser = new ParseTree2();
    }
    return parser.parseRecursive(this);
  }

  public String toPrettyString() {
    String r = "";
    for (int i = 0; i < size(); i++) {
      Token.Types type = tokens.get(i).getType();
      TokenValue value = tokens.get(i).getValue();
      switch (type) {
      case ASSIGN:
        r += "=";
        break;
      case CLOSE:
        r += ")";
        break;
      case COMMA:
        r += ",";
        break;
      case CONST:
        r += value.toString();
        break;
      case DICE:
        r += "d";
        break;
      case EXPLODING_DICE:
        r += "x";
        break;
      case DIV:
        r += "/";
        break;
      case EQ:
        r += "=";
        break;
      case EXP:
        r += "^";
        break;
      case FACT:
        r += "!";
        break;
      case FUNCT:
        r += value.getString();
        break;
      case GE:
        r += ">=";
        break;
      case GT:
        r += ">";
        break;
      case LE:
        r += "<=";
        break;
      case LT:
        r += "<";
        break;
      case MOD:
        r += "%";
        break;
      case MULT:
        r += "*";
        break;
      case NE:
        r += "!=";
        break;
      case NEG:
        r += "-";
        break;
      case OPEN:
        r += "(";
        break;
      case PARAM:
        r += "$p" + value.toString();
        break;
      case PLUS:
        r += "+";
        break;
      case STRING:
        if (FunctionHash.instance().containsName(value.getString())) {
          r += value.getString();
        }
        else {
          r += "$" + value.getString();
        }
        break;
      case SUB:
        r += "(" + ((TokenCollection) value.getTokenCollection()).toPrettyString() + ")";
        break;
      case TREE:
        r += "";
        break;
      case VAR:
        r += "$" + value.getString();
        break;
      }
    }
    return r;
  }

  public String toString() {
    String r = "";
    // *
    for (int i = 0; i < size(); i++) {
      if (tokens.get(i).getType() == Token.Types.SUB) {
        r += " " + tokens.get(i).getValue().toString();
      }
      else if (tokens.get(i).getType() == Token.Types.FUNCT) {
        r += " " + tokens.get(i).getType().toString();
        r += "(" + tokens.get(i).getValue().toString() + ")";
      }
      else if (tokens.get(i).getType() == Token.Types.CONST) {
        r += " " + tokens.get(i).getType().toString();
        r += "(" + tokens.get(i).getValue().toString() + ")";
      }
      else if (tokens.get(i).getType() == Token.Types.PARAM) {
        r += " " + tokens.get(i).getType().toString();
        r += "(p" + tokens.get(i).getValue().toString() + ")";
      }
      else if (tokens.get(i).getType() == Token.Types.STRING) {
        r += " STRING('";
        String v = tokens.get(i).getValue().toString();
        r += v + "',";
        Double constValue = (VariableHash.instance().read(v));
        boolean needsDivider = false;
        if (constValue != 0.0) {
          r += "VAR?" + constValue;
          needsDivider = true;
        }
        String functionValue = (FunctionHash.instance().getStringsByName(v));
        if (functionValue != null && !functionValue.equals("")) {
          if (needsDivider) {
            r += ";";
          }
          r += "FUNCT?" + functionValue;
        }
        r += ")";
      }
      else if (tokens.get(i).getType() == Token.Types.VAR) {
        r += " " + tokens.get(i).getType().toString();
        r += "(" + tokens.get(i).getValue().toString() + ":";
        r += "" + VariableHash.instance().read(tokens.get(i).getValue().toString()) + ")";
      }
      else {
        r += " " + tokens.get(i).getType().toString();
      }
    }
    // */
    return "[" + r + " ]";
  }

  private static TokenCollection tokenizeInternal(String input, ParseTree2 parser) {
    // System.out.println("INFERNO: TOKENIZING " + input);
    TokenCollection TC = new TokenCollection(parser);
    input = input.replaceAll("[(][)]", "");

    // Count parentheses
    int set = 0;
    for (int i = 0; i < input.length(); i++) {
      if (input.charAt(i) == '(') {
        set++;
      }
      else if (input.charAt(i) == ')') {
        set--;
      }
      if (set < 0) {
        return null;
      }
    }
    if (set != 0) {
      return null;
    }

    while (input.length() > 0) {
      if (input.matches("[0-9]*[.][0-9]+[.].*")) {
        // Avoid the "IP address" bug
        return null;
      }
      if (input.matches("[0-9]*[.][0-9]+.*")) {
        String num = "";
        while (input.matches("[0-9].*")) {
          num += input.charAt(0);
          input = input.substring(1);
        }
        num += ".";
        input = input.substring(1);
        while (input.matches("[0-9].*")) {
          num += input.charAt(0);
          input = input.substring(1);
        }
        TC.add(new Token(Token.Types.CONST, Double.parseDouble(num)));
      }
      else if (input.matches("[0-9]+.*")) {
        String num = "";
        while (input.matches("[0-9].*")) {
          num += input.charAt(0);
          input = input.substring(1);
        }
        TC.add(new Token(Token.Types.CONST, Double.parseDouble(num)));
      }
      else if (input.matches("[$][a-z_]+[*+-^/%][=].*")) {
        String var = "";
        input = input.substring(1);
        while (input.matches("[a-z_]+.*")) {
          var += input.charAt(0);
          input = input.substring(1);
        }
        TC.add(new Token(Token.Types.STRING, var));
        TC.add(new Token(Token.Types.ASSIGN));
        TC.add(new Token(Token.Types.STRING, var));
        char op = input.charAt(0);
        input = op + input.substring(2);
      }
      else if (input.matches("[$][a-z_]+.*")) {
        String var = "";
        input = input.substring(1);
        while (input.matches("[a-z_]+.*")) {
          var += input.charAt(0);
          input = input.substring(1);
        }
        TC.add(new Token(Token.Types.STRING, var));
      }
      else if (input.matches("d.*")) {
        input = input.substring(1);
        TC.add(new Token(Token.Types.DICE));
      }
      else if (input.matches("x.*")) {
        input = input.substring(1);
        TC.add(new Token(Token.Types.EXPLODING_DICE));
      }
      else if (input.matches("e.*")) {
        input = input.substring(1);
        TC.add(new Token(Token.Types.STRING, "e"));
      }
      else if (input.matches("(pi|lg|ln).*")) {
        String function = input.substring(0, 2);
        input = input.substring(2);
        TC.add(new Token(Token.Types.STRING, function));
      }
      else if (input.matches("(sin|cos|tan|abs|log).*")) {
        String function = input.substring(0, 3);
        input = input.substring(3);
        TC.add(new Token(Token.Types.STRING, function));
      }
      else if (input.matches("(rand).*")) {
        String function = input.substring(0, 4);
        input = input.substring(4);
        TC.add(new Token(Token.Types.STRING, function));
      }
      else if (input.matches("[=].*")) {
        input = input.substring(1);
        TC.add(new Token(Token.Types.ASSIGN));
      }
      else if (input.matches("[+].*")) {
        input = input.substring(1);
        TC.add(new Token(Token.Types.PLUS));
      }
      else if (input.matches("[-].*")) {
        input = input.substring(1);
        TC.add(new Token(Token.Types.NEG));
      }
      else if (input.matches("[!].*")) {
        input = input.substring(1);
        TC.add(new Token(Token.Types.FACT));
      }
      else if (input.matches("[/].*")) {
        input = input.substring(1);
        TC.add(new Token(Token.Types.DIV));
      }
      else if (input.matches("\\^.*")) {
        input = input.substring(1);
        TC.add(new Token(Token.Types.EXP));
      }
      else if (input.matches("[%].*")) {
        input = input.substring(1);
        TC.add(new Token(Token.Types.MOD));
      }
      else if (input.matches("[*].*")) {
        input = input.substring(1);
        TC.add(new Token(Token.Types.MULT));
      }
      else if (input.matches("[,].*")) {
        input = input.substring(1);
        TC.add(new Token(Token.Types.COMMA));
      }
      else if (input.matches("[(].*")) {
        ///////
        // Find the brother of this open parenthesis
        int close = 0;
        int sub = 0;
        int start = 1;
        for (int i = 0; i < input.length(); i++) {
          if (input.charAt(i) == '(') {
            sub++;
          }
          else if (input.charAt(i) == ')') {
            sub--;
          }
          if ((sub == 0) && (input.charAt(i) == ')')) {
            close = i;
            break;
          }
          if ((sub == 1) && (input.charAt(i) == ',')) {
            close = i;
            TokenCollection SubTC = TokenCollection.tokenizeInternal(input.substring(start, close),
                parser);
            input = input.substring(close + 1);
            TC.add(new Token(Token.Types.SUB, SubTC));
            TC.add(new Token(Token.Types.COMMA));
            start = 0;
            i = -1;
          }
        }
        TokenCollection SubTC = TokenCollection.tokenizeInternal(input.substring(start, close),
            parser);
        input = input.substring(close + 1);
        TC.add(new Token(Token.Types.SUB, SubTC));
        // System.out.println("INFERNO: SubExpression: " + SubTC);
        // System.out.println("INFERNO: Expression: " + TC);
      }
      else {
        return null;
      }
    }
    // System.out.println("INFERNO: TOKENIZED TO: " + TC.toString());
    return TC;
  }

  public static TokenCollection tokenize(String expr) {
    ParseTree2 parser = new ParseTree2();
    String input = expr.replaceAll("[ ]", "");
    if (input.length() == 0) {
      return null;
    }
    // if (input.charAt(0) != '>') { return null; }
    // input = input.substring(1);

    return tokenizeInternal(input, parser);
  }

}
