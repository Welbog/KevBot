package ca.welbog.kevbot.responder.math;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/*
 * VariableHash.java
 *
 * Created on February 17, 2007, 2:35 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

/**
 *
 * @author Inferno
 */
public class FunctionHash {

  public static class BuiltInFunctionSignatureMap
      extends ConcurrentHashMap<String, BuiltInFunction> {
  }

  public static class UserFunctionSignatureMap extends ConcurrentHashMap<String, UserFunction> {
  }

  public static class BuiltInFunctionNameMap
      extends ConcurrentHashMap<String, BuiltInFunctionSignatureMap> {
  }

  public static class UserFunctionNameMap
      extends ConcurrentHashMap<String, UserFunctionSignatureMap> {
  }

  private BuiltInFunctionNameMap builtInFunctions;
  private UserFunctionNameMap userFunctions;
  private static final String FUNCTION_HASH_FILENAME = "functions.txt";

  // TODO: I'm going to need to separate out the built-in functions from the
  // non-built-in functions
  // so that the ObjectMapper has a uniform class that it needs to de/serialize.
  // Also need to solve some way for names (like "floor")
  // to map to multiple signatures (like "floor1", "floor2").
  // Maybe a structure that's Map<String, Map<String, Function>>?
  // But these things will need type aliases because of type erasure. :(
  // Maybe I'll have to write a de/serializer for Functions?

  /** Creates a new instance of VariableHash */
  private FunctionHash() {
    builtInFunctions = new BuiltInFunctionNameMap();
    userFunctions = new UserFunctionNameMap();
    slurpFunctions();
    initializeDefaults();
  }

  private void initializeDefaults() {
    putFunction(new FunctionSin());
    putFunction(new FunctionCos());
    putFunction(new FunctionTan());
    putFunction(new FunctionAbs());
    putFunction(new FunctionRand());
    putFunction(new FunctionLog1());
    putFunction(new FunctionLog2());
    putFunction(new FunctionLn());
    putFunction(new FunctionLg());
    putFunction(new FunctionIf1());
    putFunction(new FunctionIf2());
    putFunction(new FunctionIf3());
    putFunction(new FunctionSign());
    putFunction(new FunctionFloor1());
    putFunction(new FunctionFloor2());
    putFunction(new FunctionCeiling1());
    putFunction(new FunctionCeiling2());
    putFunction(new FunctionRound1());
    putFunction(new FunctionRound2());
  }

  private void putFunction(BuiltInFunction function) {
    String name = function.name;
    String signature = function.signature;
    Map<String, BuiltInFunction> signatureMap = builtInFunctions.computeIfAbsent(name,
        x -> new BuiltInFunctionSignatureMap());
    signatureMap.putIfAbsent(signature, function);
  }

  private void putFunction(UserFunction function) {
    String name = function.name;
    String signature = function.signature;
    Map<String, UserFunction> signatureMap = userFunctions.computeIfAbsent(name,
        x -> new UserFunctionSignatureMap());
    signatureMap.put(signature, function);
  }

  private Optional<Function> getFunction(String name, String signature) {
    Map<String, BuiltInFunction> builtInFunctionSignatures = builtInFunctions.get(name);
    if (builtInFunctionSignatures != null) {
      return Optional.ofNullable(builtInFunctionSignatures.get(signature));
    }
    Map<String, UserFunction> userFunctionSignatures = userFunctions.get(name);
    if (userFunctionSignatures != null) {
      return Optional.ofNullable(userFunctionSignatures.get(signature));
    }
    return Optional.empty();
  }

  private Collection<Function> getFunctions(String name) {
    Map<String, BuiltInFunction> builtInFunctionSignatures = builtInFunctions.get(name);
    if (builtInFunctionSignatures != null) {
      return builtInFunctionSignatures.values().stream().collect(Collectors.toList());
    }
    Map<String, UserFunction> userFunctionSignatures = userFunctions.get(name);
    if (userFunctionSignatures != null) {
      return userFunctionSignatures.values().stream().collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  public boolean containsName(String name) {
    return !getFunctions(name).isEmpty();
  }

  public boolean isBuiltIn(String name) {
    return builtInFunctions.containsKey(name);
  }

  public synchronized void clear(String name, List<Token> params) {
    if (isBuiltIn(name)) {
      return;
    } // Can't overwrite built-in functions
    String signature = name + params.size();
    Map<String, UserFunction> signatureMap = userFunctions.get(name);
    if (signatureMap != null) {
      signatureMap.remove(signature);
      if (signatureMap.isEmpty()) {
        userFunctions.remove(name);
      }
    }
    dumpFunctions();
  }

  public synchronized void store(String name, List<Token> params, TokenCollection expression) {
    if (isBuiltIn(name)) {
      return;
    } // Can't overwrite built-in functions
    String signature = name + params.size();
    // rename incoming parameters
    renameParameters(params, expression);
    for (int paramIndex = 0; paramIndex < params.size(); paramIndex++) {
      Token param = params.get(paramIndex);
      String paramName = param.getValue().getString();
      for (int i = 0; i < expression.size(); i++) {
        Token currentToken = expression.getToken(i);
        if (currentToken.getType() == Token.Types.STRING
            && paramName.equalsIgnoreCase(currentToken.getValue().getString())) {
          expression.replaceToken(i, new Token(Token.Types.PARAM, paramIndex));
        }
      }
    }
    // System.out.println("New expression: " + expression.toString());
    UserFunction f = new UserFunction(name, signature, expression);
    putFunction(f);
    dumpFunctions();
  }

  private void renameParameters(List<Token> params, TokenCollection expression) {
    for (int paramIndex = 0; paramIndex < params.size(); paramIndex++) {
      Token param = params.get(paramIndex);
      String paramName = param.getValue().getString();
      for (int i = 0; i < expression.size(); i++) {
        Token currentToken = expression.getToken(i);
        if (currentToken.getType() == Token.Types.STRING
            && paramName.equalsIgnoreCase(currentToken.getValue().getString())) {
          expression.replaceToken(i, new Token(Token.Types.PARAM, paramIndex));
        }
        if (currentToken.getType() == Token.Types.SUB) {
          renameParameters(params, currentToken.getValue().getTokenCollection());
        }
      }
    }
  }

  public synchronized TokenCollection read(String name, List<Token> tokens, ParseTree2 parser)
      throws Exception {
    String signature = name + tokens.size();
    Optional<Function> f = getFunction(name, signature);
    if (f.isPresent()) {
      if (f.get().isBuiltIn()) {
        TokenCollection temp = new TokenCollection(parser);
        Token result = new Token(Token.Types.CONST, f.get().evaluate(tokens));
        temp.add(result);
        return temp;
      }
      else {
        // replace the parameters with their input constants
        TokenCollection expression = f.get().getExpression();
        // System.out.println("Original expression for signature " + signature +
        // ": "+expression.toString());
        replaceParameters(tokens, expression);
        // System.out.println("Expression with parameters replaced:
        // "+expression.toString());
        return expression;
      }
    }
    else {
      TokenCollection temp = new TokenCollection(parser);
      Token result = new Token(Token.Types.CONST, 0.0);
      temp.add(result);
      return temp;
    }
  }

  private void replaceParameters(List<Token> tokens, TokenCollection expression) {
    for (int i = 0; i < expression.size(); i++) {
      Token currentToken = expression.getToken(i);
      if (currentToken.getType() == Token.Types.PARAM) {
        int paramIndex = currentToken.getValue().getInteger();
        expression.replaceToken(i, tokens.get(paramIndex));
      }
      if (currentToken.getType() == Token.Types.SUB) {
        // System.out.println("INFERNO1: " + currentToken);
        // System.out.println("INFERNO2: " + tokens);
        // System.out.println("INFERNO3: " + expression);
        replaceParameters(tokens, currentToken.getValue().getTokenCollection());
      }
    }
  }

  public String getStringsByName(String name) {
    String result = "";
    for (Function f : getFunctions(name)) {
      if (!result.equals("")) {
        result += " | ";
      }
      result += f.toString();
    }
    return result;
  }

  private static FunctionHash instance;

  public static FunctionHash instance() {
    if (instance == null) {
      synchronized (VariableHash.class) {
        if (instance == null) {
          instance = new FunctionHash();
        }
      }
    }
    return instance;
  }

  private void dumpFunctions() {
    try {
      // TODO Move this to SQL at some point.
      File file = new File(FUNCTION_HASH_FILENAME);
      ObjectMapper mapper = new ObjectMapper();
      mapper.enable(SerializationFeature.INDENT_OUTPUT);
      mapper.writeValue(file, userFunctions);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void slurpFunctions() {
    try {
      File file = new File(FUNCTION_HASH_FILENAME);
      ObjectMapper mapper = new ObjectMapper();
      userFunctions = mapper.readValue(file, UserFunctionNameMap.class);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static abstract class Function {
    String name;
    String signature;
    TokenCollection value;

    boolean isBuiltIn() {
      return false;
    }

    public Function(String n, String s, TokenCollection v) {
      name = n;
      signature = s;
      value = v;
    }

    public TokenCollection getExpression() {
      return value.clone();
    }

    public double evaluate(List<Token> tokens) throws Exception {
      return 0.0;
    }

    public String toString() {
      return signature + ":=" + value.toPrettyString();
    }
  }

  private class FunctionSin extends BuiltInFunction {
    public FunctionSin() {
      super("sin", "sin1", null);
    }

    public double evaluate(List<Token> tokens) throws Exception {
      return Math.sin(tokens.get(0).evaluate());
    }
  }

  private class FunctionCos extends BuiltInFunction {
    public FunctionCos() {
      super("cos", "cos1", null);
    }

    public double evaluate(List<Token> tokens) throws Exception {
      return Math.cos(tokens.get(0).evaluate());
    }
  }

  private class FunctionTan extends BuiltInFunction {
    public FunctionTan() {
      super("tan", "tan1", null);
    }

    public double evaluate(List<Token> tokens) throws Exception {
      return Math.tan(tokens.get(0).evaluate());
    }
  }

  private class FunctionAbs extends BuiltInFunction {
    public FunctionAbs() {
      super("abs", "abs1", null);
    }

    public double evaluate(List<Token> tokens) throws Exception {
      return Math.abs(tokens.get(0).evaluate());
    }
  }

  private class FunctionSign extends BuiltInFunction {
    public FunctionSign() {
      super("sign", "sign1", null);
    }

    public double evaluate(List<Token> tokens) throws Exception {
      return Math.signum(tokens.get(0).evaluate());
    }
  }

  private class FunctionLn extends BuiltInFunction {
    public FunctionLn() {
      super("ln", "ln1", null);
    }

    public double evaluate(List<Token> tokens) throws Exception {
      return Math.log(tokens.get(0).evaluate());
    }
  }

  private class FunctionLg extends BuiltInFunction {
    public FunctionLg() {
      super("lg", "lg1", null);
    }

    public double evaluate(List<Token> tokens) throws Exception {
      return Math.log(tokens.get(0).evaluate()) / Math.log(2.0);
    }
  }

  private class FunctionRand extends BuiltInFunction {
    public FunctionRand() {
      super("rand", "rand1", null);
    }

    public double evaluate(List<Token> tokens) throws Exception {
      return Math.random() * tokens.get(0).evaluate();
    }
  }

  private class FunctionLog1 extends BuiltInFunction {
    public FunctionLog1() {
      super("log", "log1", null);
    }

    public double evaluate(List<Token> tokens) throws Exception {
      return Math.log10(tokens.get(0).evaluate());
    }
  }

  private class FunctionLog2 extends BuiltInFunction {
    public FunctionLog2() {
      super("log", "log2", null);
    }

    public double evaluate(List<Token> tokens) throws Exception {
      return Math.log(tokens.get(0).evaluate()) / Math.log(tokens.get(1).evaluate());
    }
  }

  private class FunctionIf1 extends BuiltInFunction {
    public FunctionIf1() {
      super("if", "if1", null);
    }

    public double evaluate(List<Token> tokens) throws Exception {
      return (tokens.get(0).evaluate()) == 0 ? 0.0 : 1.0;
    }
  }

  private class FunctionIf2 extends BuiltInFunction {
    public FunctionIf2() {
      super("if", "if2", null);
    }

    public double evaluate(List<Token> tokens) throws Exception {
      return (tokens.get(0).evaluate()) == 0 ? 0.0 : tokens.get(1).evaluate();
    }
  }

  private class FunctionIf3 extends BuiltInFunction {
    public FunctionIf3() {
      super("if", "if3", null);
    }

    public double evaluate(List<Token> tokens) throws Exception {
      return (tokens.get(0).evaluate()) == 0 ? tokens.get(2).evaluate() : tokens.get(1).evaluate();
    }
  }

  private class FunctionFloor1 extends BuiltInFunction {
    public FunctionFloor1() {
      super("floor", "floor1", null);
    }

    public double evaluate(List<Token> tokens) throws Exception {
      return Math.floor((tokens.get(0).evaluate()));
    }
  }

  private class FunctionCeiling1 extends BuiltInFunction {
    public FunctionCeiling1() {
      super("ceiling", "ceiling1", null);
    }

    public double evaluate(List<Token> tokens) throws Exception {
      return Math.ceil((tokens.get(0).evaluate()));
    }
  }

  private class FunctionRound1 extends BuiltInFunction {
    public FunctionRound1() {
      super("round", "round1", null);
    }

    public double evaluate(List<Token> tokens) throws Exception {
      return Math.round((tokens.get(0).evaluate()));
    }
  }

  // Why doesn't Java provide this normally?
  private class FunctionFloor2 extends BuiltInFunction {
    public FunctionFloor2() {
      super("floor", "floor2", null);
    }

    public double evaluate(List<Token> tokens) throws Exception {
      double param1 = tokens.get(0).evaluate();
      double param2 = tokens.get(1).evaluate();
      double precision = Math.pow(10, param2);
      return Math.floor(param1 / precision) * precision;
    }
  }

  private class FunctionCeiling2 extends BuiltInFunction {
    public FunctionCeiling2() {
      super("ceiling", "ceiling2", null);
    }

    public double evaluate(List<Token> tokens) throws Exception {
      double param1 = tokens.get(0).evaluate();
      double param2 = tokens.get(1).evaluate();
      double precision = Math.pow(10, param2);
      return Math.ceil(param1 / precision) * precision;
    }
  }

  private class FunctionRound2 extends BuiltInFunction {
    public FunctionRound2() {
      super("round", "round2", null);
    }

    public double evaluate(List<Token> tokens) throws Exception {
      double param1 = tokens.get(0).evaluate();
      double param2 = tokens.get(1).evaluate();
      double precision = Math.pow(10, param2);
      return Math.round(param1 / precision) * precision;
    }
  }

  private static class UserFunction extends Function {

    public UserFunction(String n, String s, TokenCollection v) {
      super(n, s, v);
    }

    public UserFunction() {
      super("", "", null);
    }

    public String getName() {
      return name;
    }

    public String getSignature() {
      return signature;
    }

    public TokenCollection getExpression() {
      return value.clone();
    }

    public void setName(String name) {
      this.name = name;
    }

    public void setSignature(String signature) {
      this.signature = signature;
    }

    public void setExpression(TokenCollection collection) {
      this.value = collection;
    }

  }

  private static class BuiltInFunction extends Function {
    public BuiltInFunction(String n, String s, TokenCollection v) {
      super(n, s, null);
    }

    boolean isBuiltIn() {
      return true;
    }

    public TokenCollection getExpression() {
      return null;
    }

    public String toString() {
      Token function = new Token(Token.Types.FUNCT, name);
      return signature + ":=" + function.toString();
    }
  }

}
