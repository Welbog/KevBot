package ca.welbog.kevbot.responder.math;

import com.fasterxml.jackson.annotation.JsonIgnore;

/*
 * Token.java
 *
 * Created on January 13, 2007, 4:44 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

/**
 *
 * @author Inferno
 */

public class Token {
    public static enum Types { CONST,MULT,DIV,MOD,PLUS,NEG,EXP,FACT,OPEN,CLOSE,FUNCT,SUB,TREE,COMMA,DICE,EXPLODING_DICE,VAR,GT,EQ,LT,LE,GE,NE,ASSIGN,STRING,PARAM };
    private Types _type;
    private TokenValue _value;
    
    public static class TokenValue {
      private String stringValue = null;
      private Double doubleValue = null;
      private Integer integerValue = null;
      private TokenCollection tokenCollectionValue = null;
      public boolean isString() { return stringValue != null; }
      public boolean isDouble() { return doubleValue != null; }
      public boolean isInteger() { return integerValue != null; }
      public boolean isTokenCollection() { return tokenCollectionValue != null; }
      @JsonIgnore
      public boolean isEmpty() { return !(isString() || isDouble() || isInteger() || isTokenCollection()); }
      public void setString(String string) { stringValue = string; }
      public void setDouble(Double doubleValue) { this.doubleValue = doubleValue; }
      public void setInteger(Integer integer) { integerValue = integer; }
      public void setTokenCollection(TokenCollection collection) { tokenCollectionValue = collection; }
      public String getString() { return stringValue; }
      public Double getDouble() { return doubleValue; }
      public Integer getInteger() { return integerValue; }
      public TokenCollection getTokenCollection() { return tokenCollectionValue; }
      public TokenValue clone() {
        TokenValue clone = new TokenValue();
        clone.setDouble(getDouble());
        clone.setInteger(getInteger());
        clone.setString(getString());
        if (isTokenCollection()) {
          clone.setTokenCollection(getTokenCollection().clone());
        }
        return clone;
      }
      public String toString() {
        if (isString()) { return stringValue; }
        if (isDouble()) { return doubleValue.toString(); }
        if (isInteger()) { return integerValue.toString(); }
        if (isTokenCollection()) { return tokenCollectionValue.toString(); }
        return "_";
      }
    }
    
    public Types getType() { return _type; }
    public TokenValue getValue() { return _value; }
    public void setType(Types type) { this._type = type; }
    public void setValue(TokenValue value) { this._value = value; }
    
    public Token clone() {
      if (_value != null) {
        return new Token(this._type, _value.clone());
      }
      return new Token(this._type);
    }
    
    public String toString() {
      return "Token (Type: " + _type +", Value: " + _value + ")";
    }
    
    public Token() {
      // For serialization
    }
    
    public Token(Types type) {
        _value = null; //new TokenValue();
        _type = type;
    }
    public Token(Types type, TokenValue value) {
      _value = value;
      _type = type;
    }
    public Token(Types type, TokenCollection tc) {
      if (tc != null) {
        TokenValue value = new TokenValue();
        value.setTokenCollection(tc.clone());
        _value = value;
      }
      _type = type; 
    }
    public Token(Types type, String name) {
      if (name != null) {
        TokenValue value = new TokenValue();
        value.setString(name);
        _value = value;
      }
      _type = type;
    }
    public Token(Types type, double doubleValue) {
        TokenValue value = new TokenValue();
        value.setDouble(doubleValue);
        _value = value;
        _type = type;
    }
    public Token(Types type, int integerValue) {
      TokenValue value = new TokenValue();
      value.setInteger(integerValue);
      _value = value;
      _type = type;
    }
    
    public double evaluate() throws Exception {
      if (_type == Types.CONST) {
        return _value.doubleValue;
      }
      else if (_type == Types.SUB) {
        return _value.getTokenCollection().evaluate();
      }
      return 0.0;
    }
}