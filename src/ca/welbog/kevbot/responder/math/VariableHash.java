package ca.welbog.kevbot.responder.math;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
public class VariableHash {

  private Map<String, Double> hash;
  private Map<String, Double> constants;
  private static final String VARIABLE_HASH_FILENAME = "variables.txt";

  /** Creates a new instance of VariableHash */
  public VariableHash() {
    hash = new ConcurrentHashMap<String, Double>();
    constants = new ConcurrentHashMap<String, Double>();
    constants.put("e", Math.E);
    constants.put("pi", Math.PI);
    slurpVariables();
  }

  public void store(String var, double value) {
    // System.out.println("Storing " + var + "," + value);
    hash.put(var, value);
    dumpVariables();
  }

  public double read(String var) {
    if (constants.containsKey(var)) {
      // System.out.println(var + " is a constant.");
      return constants.get(var);
    }
    if (hash.containsKey(var)) {
      // System.out.println(var + " is a variable.");
      return hash.get(var);
    }
    else {
      // System.out.println(var + " is neither a constant nor a variable.");
      return 0.0;
    }
  }

  private volatile static VariableHash instance;

  public static VariableHash instance() {
    if (instance == null) {
      synchronized (VariableHash.class) {
        if (instance == null) {
          instance = new VariableHash();
        }
      }
    }
    return instance;
  }
  
  private void dumpVariables() {
    try {
      // TODO Move this to SQL at some point.
      File file = new File(VARIABLE_HASH_FILENAME);
      ObjectMapper mapper = new ObjectMapper();
      mapper.enable(SerializationFeature.INDENT_OUTPUT);
      mapper.writeValue(file, hash);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  private void slurpVariables() {
    try {
      File file = new File(VARIABLE_HASH_FILENAME);
      ObjectMapper mapper = new ObjectMapper();
      hash = mapper.readValue(file, ConcurrentHashMap.class);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

}
