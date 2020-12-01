package ca.welbog.kevbot.responder.math;

import java.util.ArrayList;
import java.util.List;

/*
 * ParseTree.java
 *
 * Created on January 13, 2007, 6:31 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

/**
 *
 * @author Inferno
 */
public class ParseTree2 {

  public static void DEBUG(String s) {
    // System.out.println(s);
  }

  private boolean canEvaluate(Token token) {
    return token.getType() == Token.Types.CONST || token.getType() == Token.Types.SUB;
  }

  // This will hopefully be used when symbolic evaluation is a thing
  @SuppressWarnings("unused")
  private Double evaluate(Token token) throws Exception {
    if (!canEvaluate(token)) {
      return 0.0;
    }
    return token.evaluate();
  }

  public double parseInitial(TokenCollection TC) throws Exception {
    // Determine if the expression is variable assignment, function assignment
    // or a normal expression
    if ((TC.size() > 2) && (TC.getToken(1).getType() == Token.Types.ASSIGN)
        && (TC.getToken(0).getType() == Token.Types.STRING)) {
      TokenCollection TC2 = new TokenCollection(this);
      for (int i = 2; i < TC.size(); i++) {
        TC2.add(TC.getToken(i));
      }
      DEBUG("Subexpression: " + TC2.toString());
      VariableHash.instance().store(TC.getToken(0).getValue().getString(), TC2.evaluate());
      throw new AssignmentException("Variable assignment. Nothing to worry about.");
    }
    else {
      if (handleFunctionAssignment(TC)) {
        throw new AssignmentException("Function assignment. Nothing to worry about."); // It's
                                                                                       // function
                                                                                       // assignment
      }
      else {
        double result = TC.evaluate(); // It's normal
        VariableHash.instance().store("_last", result);
        return result;
      }
    }
  }

  private boolean handleFunctionAssignment(TokenCollection TC) {
    boolean containsAssign = false;
    int assignPosition = -1;
    for (int i = 0; i < TC.size(); i++) { // If there's an assign in the
                                          // statement, this might be a function
                                          // definition
      if (TC.getToken(i).getType() == Token.Types.ASSIGN && containsAssign) {
        return false;
      }
      if (TC.getToken(i).getType() == Token.Types.ASSIGN && !containsAssign) {
        containsAssign = true;
        assignPosition = i;
      }
    }
    if (!containsAssign) {
      return false;
    }

    TokenCollection signature = new TokenCollection(this), expression = new TokenCollection(this);
    for (int i = 0; i < assignPosition; i++) {
      signature.add(TC.getToken(i));
    }
    for (int i = assignPosition + 1; i < TC.size(); i++) {
      expression.add(TC.getToken(i));
    }
    // System.out.println(signature.toString() + " := " +
    // expression.toString());

    // Signature must be a string and a comma-separated list of sub expressions
    boolean signatureCorrect = signature.getToken(0).getType() == Token.Types.STRING;
    signatureCorrect = signatureCorrect && signature.getToken(1).getType() == Token.Types.SUB;
    if (!signatureCorrect) {
      return false;
    }
    for (int i = 1; i < signature.size(); i++) {
      if (i % 2 == 1) {
        signatureCorrect = signatureCorrect && signature.getToken(i).getType() == Token.Types.SUB;
      }
      else {
        signatureCorrect = signatureCorrect && signature.getToken(i).getType() == Token.Types.COMMA;
      }
    }
    if (!signatureCorrect) {
      return false;
    }
    List<Token> paramList = new ArrayList<Token>();
    for (int i = 1; i < signature.size(); i += 2) {
      TokenCollection potentialParam = signature.getToken(i).getValue().getTokenCollection();
      if (potentialParam.size() != 1) {
        return false;
      }
      Token param = potentialParam.getToken(0);
      if (param.getType() != Token.Types.STRING) {
        return false;
      }
      paramList.add(param);
    }
    // Now we have a list of parameters, and an expression

    if (assignPosition == TC.size() - 1) {
      // Assigned to a constant, so get rid of the function definition.
      FunctionHash.instance().clear(signature.getToken(0).getValue().getString(), paramList);
    }
    else {
      FunctionHash.instance().store(signature.getToken(0).getValue().getString(), paramList,
          expression);
      VariableHash.instance().store(signature.getToken(0).getValue().getString(), 0.0); // Clear
                                                                                        // the
                                                                                        // constant
    }

    return true;
  }

  public double parseRecursive(TokenCollection TC) throws Exception {
    // DEBUG(calls + "");

    DEBUG("Input " + TC.toString());

    // Passes
    TC = expandVariables(TC);
    TC = evaluateFunctions(TC);
    TC = evaluateSubExpressions(TC);
    TC = evaluateDice(TC);
    TC = evaluateFactorials(TC);
    TC = evaluateExponents(TC);
    TC = collapseNegations(TC);
    TC = evaluateTerms(TC);
    TC = evaluateAddition(TC);

    // Final checks
    DEBUG("LAST " + TC.toString());
    if (TC.size() != 1) {
      throw new Exception();
    }
    if (TC.getToken(0).getType() != Token.Types.CONST) {
      throw new Exception();
    }

    return (TC.getToken(0).getValue().getDouble());
  }

  private double rollDice(double number, double upper, boolean explode) {
    double result = 0;
    int x = 1, y = 0;
    x = (int) number;
    y = (int) upper;
    if ((x > 64) || (x < -64)) {
      return Double.NaN;
    }
    if (x == 0) {
      return 0;
    }
    if (x < 0) {
      x *= -1;
      y *= -1;
    }
    if ((y > 4096) || (y < -4096)) {
      return Double.NaN;
    }
    if (y == 0) {
      return 0;
    }
    if (explode && y == 1) {
      return Double.POSITIVE_INFINITY;
    }
    if (explode && y == -1) {
      return Double.NEGATIVE_INFINITY;
    }
    int count = x;
    for (int i = 0; i < x; i++) {
      int temp = (int) Math.floor(Math.random() * (double) y) + 1;
      int total = temp;
      while (explode && temp == y && count < 64) {
        temp = (int) Math.floor(Math.random() * (double) y) + 1;
        total += temp;
        count++;
      }
      result += total;
    }
    return result;
  }

  // Parsing passes
  private TokenCollection expandVariables(TokenCollection TC) {

    // Variable/Constant pass
    TokenCollection passVar = new TokenCollection(this);
    for (int i = 0; i < TC.size(); i++) {
      Token t = TC.getToken(i);
      if (t.getType() == Token.Types.STRING) {
        // Strings are variables if they're in the variable hash, or if they're
        // not in the function hash
        String name = t.getValue().getString();
        boolean isVar = VariableHash.instance().read(name) != 0.0;
        boolean mightBeFunc = FunctionHash.instance().containsName(name);
        if (isVar) {
          passVar.add(
              new Token(Token.Types.CONST, VariableHash.instance().read(t.getValue().getString())));
        }
        else if (!mightBeFunc) {
          passVar.add(
              new Token(Token.Types.CONST, VariableHash.instance().read(t.getValue().getString())));
        }
        else {
          passVar.add(t);
        }
      }
      else {
        passVar.add(t);
      }
    }
    DEBUG("pass var " + passVar.toString());
    return passVar;
  }

  private TokenCollection evaluateSubExpressions(TokenCollection TC) throws Exception {

    // Parentheses pass
    TokenCollection pass1 = new TokenCollection(this);
    for (int i = 0; i < TC.size(); i++) {
      Token t = TC.getToken(i);
      if (t.getType() == Token.Types.SUB) {
        pass1.add(new Token(Token.Types.CONST, t.getValue().getTokenCollection().evaluate()));
      }
      else {
        pass1.add(t);
      }
    }
    DEBUG("pass 1 " + pass1.toString());
    return pass1;

  }

  private TokenCollection evaluateFunctions(TokenCollection TC) throws Exception {

    // N-variable function pass
    TokenCollection functionPass = new TokenCollection(this);
    java.util.Stack<Token> stack = new java.util.Stack<Token>();
    for (int i = 0; i < TC.size(); i++) {
      Token t = TC.getToken(i);
      if (t.getType() == Token.Types.STRING) {
        String name = t.getValue().getString();
        boolean mightBeFunc = FunctionHash.instance().containsName(name);
        if (mightBeFunc) {
          if (stack.empty()) {
            stack.push(t);
          }
          else {
            if (canEvaluate(stack.peek())) {
              List<Token> stackdump = new ArrayList<Token>(stack);
              Token function = stackdump.get(0);
              stackdump.remove(0);
              double result = (FunctionHash.instance().read(function.getValue().getString(),
                  stackdump, this)).evaluate();
              functionPass.add(new Token(Token.Types.CONST, result));
              stack.clear();
              stack.push(t);
            }
            else {
              throw new Exception();
            }
          }
        }
        else { // !mightBeFunc
          functionPass.add(new Token(Token.Types.CONST, 0.0));
        }
      }
      else if (canEvaluate(t)) {
        if (stack.empty()) {
          functionPass.add(t);
        }
        else { // Stack should contain either a STRING or COMMA on the top
          if (stack.peek().getType() == Token.Types.COMMA) {
            stack.pop();
            stack.push(t);
          }
          else if (stack.peek().getType() == Token.Types.STRING) {
            stack.push(t);
          }
          else {
            // If it's not a stack or a comma, evaluate the function
            List<Token> stackdump = new ArrayList<Token>(stack);
            Token function = stackdump.get(0);
            stackdump.remove(0);
            double result = (FunctionHash.instance().read(function.getValue().getString(),
                stackdump, this)).evaluate();
            functionPass.add(new Token(Token.Types.CONST, result));
            stack.clear();
          }
        }
      }
      else if (t.getType() == Token.Types.COMMA) {
        if (stack.empty()) {
          throw new Exception();
        }
        else {
          if (canEvaluate(stack.peek())) {
            stack.push(t);
          }
          else {
            throw new Exception();
          }
        }
      }
      else {
        if (stack.empty()) {
          functionPass.add(t);
        }
        else {
          List<Token> stackdump = new ArrayList<Token>(stack);
          Token function = stackdump.get(0);
          stackdump.remove(0);
          double result = (FunctionHash.instance().read(function.getValue().getString(), stackdump,
              this)).evaluate();
          functionPass.add(new Token(Token.Types.CONST, result));
          stack.clear();
          functionPass.add(t);
        }
      }
    }

    if (!stack.isEmpty() && canEvaluate(stack.peek())) {
      List<Token> stackdump = new ArrayList<Token>(stack);
      Token function = stackdump.get(0);
      stackdump.remove(0);
      double result = (FunctionHash.instance().read(function.getValue().getString(), stackdump,
          this)).evaluate();
      functionPass.add(new Token(Token.Types.CONST, result));
      stack.clear();
    }

    DEBUG("pass function " + functionPass.toString());
    if (!stack.empty()) {
      throw new Exception();
    }

    return functionPass;

  }

  private TokenCollection evaluateDice(TokenCollection TC) throws Exception {

    // Dice pass
    TokenCollection passDice = new TokenCollection(this);
    java.util.Stack<Token> stack = new java.util.Stack<Token>();
    for (int i = 0; i < TC.size(); i++) {
      Token t = TC.getToken(i);
      if (t.getType() == Token.Types.CONST) {
        if (stack.empty()) {
          stack.push(t);
        }
        else {
          if (stack.peek().getType() == Token.Types.CONST) {
            passDice.add(stack.pop());
            stack.push(t);
          }
          else {
            if (stack.peek().getType() == Token.Types.DICE) {
              stack.pop();
              stack.push(new Token(Token.Types.CONST,
                  rollDice(stack.pop().getValue().getDouble().doubleValue(),
                      t.getValue().getDouble().doubleValue(), false)));
            }
            else if (stack.peek().getType() == Token.Types.EXPLODING_DICE) {
              stack.pop();
              stack.push(new Token(Token.Types.CONST,
                  rollDice(stack.pop().getValue().getDouble().doubleValue(),
                      t.getValue().getDouble().doubleValue(), true)));
            }
            else {
              throw new Exception();
            }
          }
        }
      }
      else if (t.getType() == Token.Types.DICE || t.getType() == Token.Types.EXPLODING_DICE) {
        if (stack.empty()) {
          stack.push(new Token(Token.Types.CONST, 1.0));
          stack.push(t);
        }
        else {
          if (stack.peek().getType() == Token.Types.CONST) {
            stack.push(t);
          }
          else {
            throw new Exception();
          }
        }
      }
      else {
        if (stack.empty()) {
          passDice.add(t);
        }
        else {
          if (stack.peek().getType() == Token.Types.CONST) {
            passDice.add(stack.pop());
            passDice.add(t);
          }
          else {
            throw new Exception();
          }
        }
      }
    }
    if (!stack.empty() && stack.peek().getType() == Token.Types.CONST) {
      passDice.add(stack.pop());
    }
    DEBUG("pass Dice " + passDice.toString());
    if (!stack.empty()) {
      throw new Exception();
    }

    return passDice;
  }

  private TokenCollection evaluateFactorials(TokenCollection TC) throws Exception {

    // Factorial pass
    TokenCollection pass3 = new TokenCollection(this);
    java.util.Stack<Token> stack = new java.util.Stack<Token>();
    for (int i = 0; i < TC.size(); i++) {
      Token t = TC.getToken(i);
      if (t.getType() == Token.Types.CONST) {
        if (stack.empty()) {
          stack.push(t);
        }
        else {
          pass3.add(stack.pop());
          stack.push(t);
        }
      }
      else if (t.getType() == Token.Types.FACT) {
        if (stack.empty()) {
          throw new Exception();
        }
        else {
          double temp = 1;
          double l = (stack.peek().getValue().getDouble());
          if (Double.isInfinite(l) || Double.isNaN(l)) {
            temp = Double.POSITIVE_INFINITY;
          }
          else {
            for (int y = 1; y <= (int) l; y++) {
              temp *= y;
              if (Double.isInfinite(temp) || Double.isNaN(temp)) {
                break;
              }
            }
          }
          stack.pop();
          pass3.add(new Token(Token.Types.CONST, temp));
        }
      }
      else {
        if (stack.empty()) {
          pass3.add(t);
        }
        else {
          pass3.add(stack.pop());
          pass3.add(t);
        }
      }

    }
    if (!stack.empty()) {
      pass3.add(stack.pop());
    }
    DEBUG("pass 3 " + pass3.toString());
    return pass3;
  }

  private TokenCollection collapseNegations(TokenCollection TC) throws Exception {

    // Collapse negatives pass
    TokenCollection pass4 = new TokenCollection(this);
    java.util.Stack<Token> stack = new java.util.Stack<Token>();
    for (int i = 0; i < TC.size(); i++) {
      Token t = TC.getToken(i);
      if (t.getType() == Token.Types.NEG) {
        if (stack.empty()) {
          stack.push(t);
        }
        else {
          if (stack.peek().getType() == Token.Types.NEG) {
            stack.pop();
            if (stack.empty()) {
              stack.push(new Token(Token.Types.PLUS));
            }
          }
          else {
            stack.push(t);
          }
        }
      }
      else if (t.getType() == Token.Types.EXP || t.getType() == Token.Types.DIV
          || t.getType() == Token.Types.MULT || t.getType() == Token.Types.PLUS
          || t.getType() == Token.Types.MOD) {
        if (stack.empty()) {
          stack.push(t);
        }
        else {
          throw new Exception();
        }
      }
      else if (t.getType() == Token.Types.CONST) {
        if (stack.empty()) {
          pass4.add(t);
        }
        else {
          double val = t.getValue().getDouble();
          if (stack.peek().getType() == Token.Types.NEG) {
            stack.pop();
            val *= -1.0;
          }
          if (stack.empty()) {
            if (pass4.size() > 0) {
              pass4.add(new Token(Token.Types.PLUS));
            }
          }
          else {
            pass4.add(stack.pop());
          }
          pass4.add(new Token(Token.Types.CONST, val));
        }
      }
      else {
        pass4.add(t);
      }
    }
    DEBUG("pass 4 " + pass4.toString());
    if (!stack.empty()) {
      throw new Exception();
    }
    return pass4;
  }

  private TokenCollection evaluateExponents(TokenCollection TC) throws Exception {

    // Exponent pass
    TokenCollection pass5 = new TokenCollection(this);
    java.util.Stack<Token> stack = new java.util.Stack<Token>();
    for (int i = 0; i < TC.size(); i++) {
      Token t = TC.getToken(i);
      if (t.getType() == Token.Types.EXP) {
        if (stack.empty()) {
          throw new Exception();
        }
        else {
          if (stack.peek().getType() == Token.Types.CONST) {
            stack.push(t);
          }
          else {
            throw new Exception();
          }
        }
      }
      else if (t.getType() == Token.Types.CONST) {
        if (stack.empty()) {
          stack.push(t);
        }
        else {
          boolean negate = false;
          if (stack.peek().getType() == Token.Types.NEG) {
            stack.pop();
            negate = true;
          }
          if (stack.peek().getType() == Token.Types.EXP) {
            stack.pop();
            double power = t.getValue().getDouble() * (negate ? -1.0 : 1.0);
            double val = Math.pow(stack.pop().getValue().getDouble(), power);
            stack.push(new Token(Token.Types.CONST, val));
          }
          else {
            pass5.add(stack.pop());
            stack.push(t);
          }
        }
      }
      else if (t.getType() == Token.Types.NEG) {
        if (stack.empty()) {
          pass5.add(t);
        }
        else {
          if (stack.peek().getType() == Token.Types.EXP) {
            stack.push(t);
          }
          else if (stack.peek().getType() == Token.Types.NEG) {
            stack.pop();
          }
          else if (stack.peek().getType() == Token.Types.CONST) {
            pass5.add(stack.pop());
            pass5.add(t);
          }
        }
      }
      else {
        if (stack.empty()) {
          pass5.add(t);
        }
        else {
          if (stack.peek().getType() == Token.Types.CONST) {
            pass5.add(stack.pop());
            pass5.add(t);
          }
          else {
            throw new Exception();
          }
        }
      }
    }
    if (!stack.empty() && stack.peek().getType() == Token.Types.CONST) {
      pass5.add(stack.pop());
    }
    DEBUG("pass 5 " + pass5.toString());
    if (!stack.empty()) {
      throw new Exception();
    }
    return pass5;
  }

  private TokenCollection evaluateTerms(TokenCollection TC) throws Exception {

    // multiplication pass
    TokenCollection pass6 = new TokenCollection(this);
    java.util.Stack<Token> stack = new java.util.Stack<Token>();
    for (int i = 0; i < TC.size(); i++) {
      Token t = TC.getToken(i);
      if (t.getType() == Token.Types.CONST) {
        if (stack.empty()) {
          stack.push(t);
        }
        else {
          if (stack.peek().getType() == Token.Types.CONST) {
            double val = (Double) stack.pop().getValue().getDouble();
            val *= (Double) t.getValue().getDouble();
            stack.push(new Token(Token.Types.CONST, val));
          }
          else if (stack.peek().getType() == Token.Types.MULT) {
            stack.pop();
            double val = (Double) stack.pop().getValue().getDouble();
            val *= (Double) t.getValue().getDouble();
            stack.push(new Token(Token.Types.CONST, val));
          }
          else if (stack.peek().getType() == Token.Types.DIV) {
            stack.pop();
            double val = (Double) stack.pop().getValue().getDouble();
            val /= (Double) t.getValue().getDouble();
            stack.push(new Token(Token.Types.CONST, val));
          }
          else if (stack.peek().getType() == Token.Types.MOD) {
            stack.pop();
            double val = (Double) stack.pop().getValue().getDouble();
            val %= (Double) t.getValue().getDouble();
            stack.push(new Token(Token.Types.CONST, val));
          }
        }
      }
      else if (t.getType() == Token.Types.MULT || t.getType() == Token.Types.DIV
          || t.getType() == Token.Types.MOD) {
        if (stack.empty()) {
          throw new Exception();
        }
        else {
          if (stack.peek().getType() == Token.Types.CONST) {
            stack.push(t);
          }
          else {
            throw new Exception();
          }
        }
      }
      else {
        if (stack.empty()) {
          pass6.add(t);
        }
        else {
          if (stack.peek().getType() == Token.Types.CONST) {
            pass6.add(stack.pop());
            pass6.add(t);
          }
          else {
            throw new Exception();
          }
        }
      }
    }
    if (!stack.empty() && stack.peek().getType() == Token.Types.CONST) {
      pass6.add(stack.pop());
    }
    DEBUG("pass 6 " + pass6.toString());
    if (!stack.empty()) {
      throw new Exception();
    }
    return pass6;
  }

  private TokenCollection evaluateAddition(TokenCollection TC) throws Exception {

    // Addition pass
    TokenCollection pass7 = new TokenCollection(this);
    java.util.Stack<Token> stack = new java.util.Stack<Token>();
    for (int i = 0; i < TC.size(); i++) {
      Token t = TC.getToken(i);
      if (t.getType() == Token.Types.CONST) {
        if (stack.empty()) {
          stack.push(t);
        }
        else {
          if (stack.peek().getType() == Token.Types.CONST) {
            throw new Exception();
          }
          else {
            stack.pop();
            double val = (Double) stack.pop().getValue().getDouble();
            val += (Double) t.getValue().getDouble();
            stack.push(new Token(Token.Types.CONST, val));
          }
        }
      }
      else if (t.getType() == Token.Types.PLUS) {
        if (stack.empty()) {
          throw new Exception();
        }
        else {
          if (stack.peek().getType() == Token.Types.CONST) {
            stack.push(t);
          }
          else {
            throw new Exception();
          }
        }
      }
      else {
        throw new Exception();
      }
    }
    if (!stack.empty() && stack.peek().getType() == Token.Types.CONST) {
      pass7.add(stack.pop());
    }
    if (!stack.empty()) {
      throw new Exception();
    }
    return pass7;
  }
}
