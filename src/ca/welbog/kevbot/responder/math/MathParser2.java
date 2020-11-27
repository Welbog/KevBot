package ca.welbog.kevbot.responder.math;
/*
 * MathParser2.java
 *
 * Created on January 13, 2007, 4:29 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

/**
 *
 * @author Inferno
 */
public class MathParser2 {
    private boolean fail;
    private String value = Double.NaN+"";
    
    /** Creates a new instance of MathParser2 */
    public MathParser2(String expr) {
        fail = false;
        // Parse string
        // Tokenize first
        try {
          String input = new String(expr).toLowerCase();
          TokenCollection TC = TokenCollection.tokenize(input);
          if (TC == null) {
              fail = true;
              return;
          }
          System.out.println(TC.toString());
          if ((TC.size() == 1) && TC.getToken(0).getType() == Token.Types.CONST) {
            fail = true;
            return;
          }
          if (TC.size() == 1 &&
              TC.getToken(0).getType() == Token.Types.STRING &&
              FunctionHash.instance().containsName(TC.getToken(0).getValue().getString()) &&
              !FunctionHash.instance().isBuiltIn(TC.getToken(0).getValue().getString()) &&
              VariableHash.instance().read(TC.getToken(0).getValue().getString()) == 0.0) {
            value = FunctionHash.instance().getStringsByName(TC.getToken(0).getValue().getString());
          }
          else {
            value = TC.evaluateInitial() + "";
            //value = new ParseTree2().parse(TC) + "";
          }
        }
        catch (AssignmentException e) {
          fail = true;
        }
        catch (Exception e) {
          System.err.println("Exception parsin' math: " + e.getMessage());
            e.printStackTrace();
            fail = true;
        }
    }
    
    public boolean getFail() {
        return fail;
    }
    
    public String getValue() {
        if (fail) { return Double.NaN + ""; }
        return value;
    }

    public static void main(String[] args) {
        //MathParser2 A = new MathParser2(">dog(x+1)=x^2");
        //MathParser2 A2 = new MathParser2(">dog(x,y)=x+y");
        //MathParser2 A3 = new MathParser2("b+=a");
        //MathParser2 B = new MathParser2(">bob(i)=i+1");
        //MathParser2 B2 = new MathParser2("cat(a)=2+bob(a)");
        //MathParser2 E = new MathParser2("e");
        //System.out.println(E.getValue());
      
        String input = "$dog($a)=$a^2";
        String input2 = "$x=5";
        String input3 = "$dog(4!)-(1+(-($x))) + (5)5/5%4"; // 581
        String input4 = "log(4d5,3)";
        String input5 = "$x";
        /*
        MathParser2 MP = new MathParser2(input);
        System.out.println(MP.getFail());
        System.out.println(MP.getValue());
        */
        MathParser2 MP2 = new MathParser2(input2);
        System.out.println(MP2.getValue());
        MathParser2 MP3 = new MathParser2(input3);
        System.out.println(MP3.getValue());
        MathParser2 MP4 = new MathParser2(input4);
        System.out.println(MP4.getValue());
        MathParser2 MP5 = new MathParser2(input5);
        System.out.println(MP5.getValue());
        
        String input6 = "$recursive($a) = $if($a,$recursive($a-1)+$a,0)";
        String input7 = "$recursive(22)";
        String input8 = "$recursive(10)";
        /*
        MathParser2 MP6 = new MathParser2(input6);
        System.out.println(MP6.getValue());
        */
        MathParser2 MP7 = new MathParser2(input7);
        System.out.println(MP7.getValue());
        MathParser2 MP8 = new MathParser2(input8);
        System.out.println(MP8.getValue());

        String input9 = "1x1";
        MathParser2 MP9 = new MathParser2(input9);
        System.out.println(MP9.getValue());
        String input10 = "6d1";
        MathParser2 MP10 = new MathParser2(input10);
        System.out.println(MP10.getValue());
        String input11 = "(-5)d(-5)";
        MathParser2 MP11 = new MathParser2(input11);
        System.out.println(MP11.getValue());
        String input12 = "2^(-1)";
        MathParser2 MP12 = new MathParser2(input12);
        System.out.println(MP12.getValue());
        String input13 = "2^2+2^2";
        MathParser2 MP13 = new MathParser2(input13);
        System.out.println(MP13.getValue());
    }
    
}
