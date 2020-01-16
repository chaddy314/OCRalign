package de.uniwue;

import java.io.*;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;

public class Main {

    public final int UP = 1;
    public final int LEFT = 2;
    public final int UL = 4;

    public static void main(String[] args) {
        boolean XML_MODE = false;
	    String ocrText = "";
	    String gtText = "";

	    if(args[0].endsWith(".xml")) {
            XML_MODE = true;
        } else {
	        ocrText = args[0];
            gtText = args[1];
        }

	    if(XML_MODE) {
            System.out.println("XML not yer supported");
        } else {
	        try {
                String [] alignedStrings = nwAlign(ocrText,gtText);

                for (String line: alignedStrings) {
                    System.out.println(line);
                }

                int[] count = countChar(alignedStrings[0],'-');

                alignedStrings[0] = stripLines(alignedStrings[0]);



                alignedStrings[1] = trimGt(alignedStrings[1],count[0],count[1]);

                //calcDiff(alignedStrings);
                for (String line: alignedStrings) {
                    System.out.println(line);
                }

            } catch (Exception e) {
	            e.printStackTrace();
            }

        }
    }

    public static double calcDiff(String[] alignedStrings) {
        return 0;
    }

    public static int[] countChar(String string, char c) {
        int[] count = {0,0};
        for(int i = 0; i < string.length(); i++) {
            if(string.charAt(i) != '-') {
                break;
            } else {
                count[0] ++;
            }
        }

        for(int i = string.length() - 1; i >=0; i--) {
            if(string.charAt(i) != '-') {
                break;
            } else {
                count[1] ++;
            }
        }
        return count;
    }

    public static String trimGt(String gtText, int begin, int end) {
        return stripLines(gtText.substring(begin,gtText.length()-end));
    }

    public static String stripLines(String string) {
        //for (String line: strings) {
        string = string.replaceAll("^-+(?!$)", "");
        string = string.replaceAll("-+$", "");

        return string;
    }

    public static int findMiddle(String string, int middle) {
        return 0;
    }

    public static String[] nwAlign(String s1, String s2) {

        Context context = Context.enter();
        try {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            InputStream is = classloader.getResourceAsStream("alignment.js");

            Reader jReader = new InputStreamReader(is);
            try {
                ScriptableObject scope  = context.initStandardObjects();
                context.evaluateReader(scope, jReader, "alignment.js", 5,null);
                Function fct = (Function)scope.get("nw",scope);
                Object result = fct.call(context,scope,scope, new Object[] {s1,s2});
                NativeArray arr = (NativeArray) result;
                Object [] array = new Object[(int) arr.getLength()];
                for (Object o : arr.getIds()) {
                    int index = (Integer) o;
                    array[index] = arr.get(index, null);
                }

                return new String[] {array[0].toString(),array[1].toString()};

            } catch(Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return null;
    }
}
