package de.uniwue;


import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RED = "\u001B[31m";

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
	        String textFile = args[1];
	        String xmlFile = args[0];
            try {
                gtText = Files.readString(Paths.get(textFile));

                PageXML pageXML = new PageXML(xmlFile);
                List<Textline> lines = pageXML.listOcrLines();
                for (Textline line: lines) {
                    //System.out.println(ANSI_RED + line.getOcrText());

                    String[] result = Aligner.oldAlign(line.getOcrText(),gtText);

                    System.out.println("\n\nTextLine ID: "+line.getId()+"\n");
                    System.out.println("Testing:" + ANSI_CYAN + line.getOcrText() + ANSI_RESET);
                    System.out.println();
                    System.out.println(ANSI_RED + result[0] + ANSI_RESET);
                    System.out.println();
                    System.out.println(ANSI_GREEN + result[1] + ANSI_RESET);
                    System.out.println("\nSimilarity (using Levenshtein Distance): " + Aligner.calcSimilarity(result)*100.0 + "%");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
	        try {
                double sim = Aligner.calcSimilarity(new String[]{ocrText,gtText});
                System.out.println(ANSI_GREEN + "\nSimilarity from ocrText to gtText(using Levenshtein Distance): "+ sim + "%" + ANSI_RESET);


                String[] oldResult = Aligner.oldAlign(ocrText,gtText);
                String[] foldingResult = Aligner.align(ocrText,gtText);
                //calcDiff(alignedStrings);
                System.out.println(ANSI_CYAN+"\n\n### Aligned Strings using normal method ###\n"+ANSI_RESET);
                for (String line: oldResult) {
                    System.out.println(line);
                }
                System.out.println(ANSI_GREEN + "\nSimilarity (using Levenshtein Distance): " + Aligner.calcSimilarity(oldResult)*100.0 + "%" + ANSI_RESET);

                System.out.println(ANSI_CYAN+"\n\n### Aligned Strings using doubling method ###\n"+ANSI_RESET);
                for (String line: foldingResult) {
                    System.out.println(line);
                }
                System.out.println(ANSI_GREEN + "\nSimilarity (using Levenshtein Distance): " + Aligner.calcSimilarity(foldingResult)*100.0 + "%" + ANSI_RESET);
            } catch (Exception e) {
	            e.printStackTrace();
            }

        }
    }


}
