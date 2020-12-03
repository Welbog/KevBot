package ca.welbog.kevbot.persist;

/**
 * For acquiring new SingleFiles and DoubleFiles, which helps facilitate testing consumer code.
 */
public class RAHLFactory {

  public RAHLFactory() {
    
  }
  
  public SingleFile createSingleFile(String filename) {
    return new SingleFile(filename);
  }
  
  public DoubleFile createDoubleFile(String filename1, String filename2) {
    return new DoubleFile(filename1, filename2);
  }
  
  public KarmaFile createKarmaFile(String filename1, String filename2) {
  	return new KarmaFile(filename1, filename2);
  }
}
