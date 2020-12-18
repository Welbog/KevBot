package ca.welbog.kevbot.communication;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Subsequence {
	private Set<SubsequenceFlag> flags = new HashSet<>();
	private String sequence;
	private String originString;
	private int firstTickIndex = -1, lastTickIndex = -1;
  
  public static final String FLAG_START = "_(";
  public static final String FLAG_STOP = ")_";
	
	public static Subsequence buildFirstSubsequence(String origin) {
		int firstTick = getTickIndex(origin, 0);
		if (firstTick < 0) {
			// No backticks!
			return new Subsequence("", new ArrayList<>(), origin, -1, -1);
		}
		int secondTick = getTickIndex(origin, firstTick+1);
		if (secondTick < 0) {
			// No pair of backticks!
			return new Subsequence("", new ArrayList<>(), origin, -1, -1);
		}
		
		// Look for flags
		int start = firstTick;
		int end = secondTick;
		Set<Character> flagChars = new HashSet<>();
		String sequence = origin.substring(start+1, end);
		if (origin.charAt(secondTick-2) == ')' && origin.charAt(secondTick-1) == '_') {
			// Flags
			int flagsStart = origin.indexOf(FLAG_START, firstTick);
			// TODO: Finish this up
			// Validate flag start is before the end marker
			if (flagsStart < secondTick) {
				for (int i = flagsStart; i < secondTick; i++) {
					flagChars.add(origin.charAt(i));
				}
			}
			sequence = origin.substring(start+1, flagsStart);
		}
		Set<SubsequenceFlag> flags = new HashSet<>();
		for (char potentialFlag : flagChars) {
			for (SubsequenceFlag flag : SubsequenceFlag.values()) {
				if (flag.flagName == potentialFlag) {
					flags.add(flag);
				}
			}
		}
		return new Subsequence(sequence.replaceAll("[$][`]", "`"), flags, origin, start, end);
	}
	
	public Subsequence(String sequence, Collection<SubsequenceFlag> flags, String origin, int start, int end) {
		this.flags.addAll(flags);
		this.sequence = sequence;
		this.originString = origin;
		this.firstTickIndex = start;
		this.lastTickIndex = end;
	}
	
	public String getRecursiveString() {
		return sequence;
	}
	
	public String replaceSubsequence(String replacement) {

    if (replacement == null) {
      replacement = "";
    }
    // Apply flags to replacement
    replacement = applyFlags(replacement, flags);
    ca.welbog.kevbot.log.Logger.debugStatic("Original: " + originString);
    String prefix = originString.substring(0, firstTickIndex);
    String suffix = originString.substring(lastTickIndex+1, originString.length());
    String body = prefix + replacement + suffix;
    ca.welbog.kevbot.log.Logger.debugStatic("replacement: " + replacement);
    ca.welbog.kevbot.log.Logger.debugStatic("new body: " + body);
		return body;
	}
	
	private String applyFlags(String string, Collection<SubsequenceFlag> flags) {
		if (string.length() == 0) {
			return string;
		}
		if (flags.contains(SubsequenceFlag.LOWER_CASE)) {
			string = string.toLowerCase();
		}
		if (flags.contains(SubsequenceFlag.UPPER_CASE)) {
			string = string.toUpperCase();
		}
		if (flags.contains(SubsequenceFlag.FIRST_CAPITAL)) {
			string = Character.toUpperCase(string.charAt(0)) + string.substring(1);
		}
		if (flags.contains(SubsequenceFlag.REMOVE_END_PUNCTUATION)) {
			string = string.replace("\\s*[.!?]+$", "");
		}
		if (flags.contains(SubsequenceFlag.REMOVE_SPACES)) {
			string = string.replaceAll(" ", "");
		}
		return string;
	}
	
	public boolean containsSubsequence() {
		return firstTickIndex != -1;
	}
	
	public String toString() {
		return originString.replaceAll("[$][`]", "`");
	}	
  
  private static int getTickIndex(String origin, int startIndex) {
  	boolean ignorenext = false;
  	for (int i = startIndex; i < origin.length(); i++) {
  		if (ignorenext) {
  			ignorenext = false;
  			continue;
  		}
  		if (origin.charAt(i) == '$') {
  			// $` is a literal ` ($ is the escape character)
  			ignorenext = true;
  			continue;
  		}
  		if (origin.charAt(i) == '`') {
  			return i;
  		}
  	}
  	return -1;
  }
	
	public static enum SubsequenceFlag {
		UPPER_CASE('u'),
		LOWER_CASE('l'),
		FIRST_CAPITAL('f'),
		REMOVE_END_PUNCTUATION('p'),
		REMOVE_SPACES('s');
		
		private final char flagName;
		
		private SubsequenceFlag(char flagName) {
			this.flagName = flagName;
		}
	}
}
