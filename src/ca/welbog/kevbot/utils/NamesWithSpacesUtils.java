package ca.welbog.kevbot.utils;

import java.util.Arrays;
import java.util.Vector;

public class NamesWithSpacesUtils {
  
  /**
   * This is a method that converts a single-word name into a multi-word name if there are quotes around the name.
   * @param firstWord
   * @param remainingWords
   * @return
   */
  public static NameAndRemainder getNameWithSpaces(String firstWord, Vector<String> remainingWords) {
    NameAndRemainder result = new NameAndRemainder();
    result.name = firstWord;
    result.remainder = remainingWords;
    
    if (!firstWord.matches("^\".*")) { // We only care about this if the first word is quoted.
      return result; // Just return the input
    }
    
    result.name = result.name.substring(1); // Remove the quote
    
    int i = 0;
    for (; i < remainingWords.size(); i++) {
      String word = remainingWords.get(i);
      if (word.matches(".*\"$")) {
        result.name += " " + word.substring(0, word.length()-1); // Remove the quote
        break;
      }
      else {
        result.name += " " + word;
      }
    }
    result.remainder = new Vector<>(remainingWords.subList(i+1, remainingWords.size())); // Remove the name
    
    return result;
  }
  
  public static class NameAndRemainder {
    public String name;
    public Vector<String> remainder;
    public String toString() {
      return "name: " + name + ", remainder: " + remainder.toString();
    }
  }
  
  public static void main(String[] args) {
    String first = "bob\"";
    Vector<String> remainder = new Vector<String>(Arrays.asList(new String[] {"bob\"", "mindy"}));
    System.out.println("1: " + getNameWithSpaces(first, remainder));
    
    first = "\"bob";
    remainder = new Vector<>(Arrays.asList(new String[] {"bob\"", "\"mindy\""}));
    System.out.println("2: " + getNameWithSpaces(first, remainder));
    
    first = "\"bob";
    remainder = new Vector<>(Arrays.asList(new String[] {"bob", "mindy\""}));
    System.out.println("3: " + getNameWithSpaces(first, remainder));
  }
}
