package de.uniwue;

import org.apache.commons.lang3.StringUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.ScriptableObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.Normalizer;

public class Aligner {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RED = "\u001B[31m";

    public static final char symbol = '֍';


    public static double calcSimilarity(String[] strings) {
        /*
    }
        System.out.println(StringUtils.getLevenshteinDistance(strings[0],strings[1]));
        System.out.println(strings[0].length());
        */
        String longer = strings[0], shorter = strings[1];
        if (strings[0].length() < strings[1].length()) { // longer should always have greater length
            longer = strings[1]; shorter = strings[0];
        }
        return (double)(1.0-(StringUtils.getLevenshteinDistance(strings[1],strings[0]))/(double)longer.length());
    }

    public static String[] reverseStrings(String[] strings) {
        String[] output = new String[2];
        StringBuilder sBuilder = new StringBuilder();
        sBuilder.append(strings[0]);
        output[0] = sBuilder.reverse().toString();
        sBuilder.setLength(0);
        sBuilder.append(strings[1]);
        output[1] = sBuilder.reverse().toString();
        return output;
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

    public static int countCharBegin(String string, char c) {
        int count = 0;
        for(int i = 0; i < string.length(); i++) {
            if(string.charAt(i) != '-') {
                break;
            } else {
                count++;
            }
        }
        return count;
    }

    public static int countCharEnd(String string, char c) {
        int count = 0;
        for(int i = string.length() - 1; i >=0; i--) {
            if(string.charAt(i) != '-') {
                break;
            } else {
                count++;
            }
        }
        return count;
    }

    public static String trimGt(String gtText, int begin, int end) {
        if(begin+end > gtText.length()) {
            return stripLines(gtText);
        }
        return stripLines(gtText.substring(begin,gtText.length()-end));
    }

    public static String stripLines(String string) {
        //for (String line: strings) {
        string = string.replaceAll("^-+(?!$)", "");
        string = string.replaceAll("-+$", "");

        return string;
    }

    public static String[] align(String s1, String s2) {

        s1 = Normalizer.normalize(s1, Normalizer.Form.NFKC);
        s2 = Normalizer.normalize(s2,Normalizer.Form.NFKC);
        String[] alignedStrings = nwAlign(s1,s2);

        String[] rStrings = reverseStrings(new String[]{s1,s2});
        String[] rAlignedStrings = nwAlign(rStrings[0],rStrings[1]);

        alignedStrings[0] = highlight(alignedStrings[0],alignedStrings[1]);
        /*
        //----------Testing Area----------------
        System.out.println(ANSI_YELLOW + "\n### Normal alignment from Back: ###\n" + ANSI_RESET);
        for (String line: alignedStrings) {
            System.out.println(line);
        }
        System.out.println(ANSI_GREEN + "\nSimilarity (using Levenshtein Distance): " + calcSimilarity(alignedStrings)*100.0 + "%" + ANSI_RESET);
        System.out.println(ANSI_YELLOW+"\n\n### Reverse alignment: ###\n"+ANSI_RESET);
        for (String line: rAlignedStrings) {
            System.out.println(line);
        }
        System.out.println(ANSI_GREEN + "\nSimilarity (using Levenshtein Distance): " + calcSimilarity(rAlignedStrings)*100.0 + "%" + ANSI_RESET);
        //----------Testing Area----------------
        */

        int countBegin = countCharEnd(rAlignedStrings[0],symbol);
        int countEnd = countCharEnd(alignedStrings[0],symbol);
        alignedStrings[1] = trimGt(alignedStrings[1],countBegin,countEnd);
        alignedStrings[0] = stripLines(alignedStrings[0]);
        alignedStrings[0] = alignedStrings[0].replaceAll("["+symbol+"]{1,}","-");
        alignedStrings[1] = alignedStrings[1].replaceAll("["+symbol+"]{1,}","");
        //alignedStrings = nwAlign(alignedStrings[0],alignedStrings[1]);
        //alignedStrings[1] = alignedStrings[1].replaceAll("["+symbol+"]{1,}","");
        return alignedStrings;
    }

    public static String[] oldAlign(String s1, String s2) {
        String[] alignedStrings = nwAlign(s1,s2);
        int countBegin = countCharBegin(alignedStrings[0],'-');
        int countEnd = countCharEnd(alignedStrings[0],'-');

        alignedStrings[0] = stripLines(alignedStrings[0]);
        alignedStrings[1] = trimGt(alignedStrings[1],countBegin,countEnd);

        return alignedStrings;
    }

    public static String[] proxAlign(String s1, String s2, int proximity) {

        String[] alignedStrings = nwAlign(s1,s2);

        //alignedStrings[0] = stripLines(alignedStrings[0]);
        //alignedStrings[0] = alignedStrings[0].replaceAll("[-]{2,}","");
        int startIndex = s2.length()-(s1.length()+proximity);
        startIndex = (startIndex < 0) ? 0 : startIndex;
        String[] proxStrings = nwAlign(s1,s2.substring(startIndex));

        String[] rStrings = reverseStrings(new String[]{s1,s2});
        String[] rAlignedStrings = nwAlign(rStrings[0],rStrings[1]);


        int countBegin = countCharEnd(rAlignedStrings[0],'-');
        int countEnd = countCharEnd(alignedStrings[0],'-');
        alignedStrings[1] = trimGt(alignedStrings[1],countBegin,countEnd);


        proxStrings[0] = proxStrings[0].replaceAll("[-]{2,}","");
        return align(proxStrings[0],proxStrings[1]);

    }

    private static String trimEnd(String string) {
        string.replaceAll("[-.,]$","");
        return string;
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

    public static String cutGroundTruth(String gtText, String ocrText) {
        if(ocrText.length() >= gtText.length()) {
            return "";
        }

        return gtText.substring(0,gtText.length() - ocrText.length());
    }

    public static String highlight(String ocr, String gt) {
        String hlOcr = "";

        for(int i = 0;  i < ocr.length() && i < gt.length(); i++) {
            if(ocr.charAt(i) == gt.charAt(i)) {
                hlOcr += ANSI_GREEN + ocr.charAt(i) + ANSI_RESET;
            } else {
                hlOcr += ANSI_RED + ocr.charAt(i) + ANSI_RESET;
            }
        }

        return hlOcr;
    }
}


