package de.uniwue;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.List;

public class Main {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    protected static boolean overwrite =  true;
    protected static boolean checkCertainty = false;
    protected static boolean shortResult = false;

    enum Mode {
        LINE,
        XML,
        BATCH,
        HELP
    }

    public static void main(String[] args) {


        String ocrText = "";
        String gtText = "";
        String pathDir = "";
        Mode mode;
        if(args.length < 1) {
            printHelp();
            return;
        }

        // Determines Main Mode
        switch (args[0]) {
            case "-l":  mode = Mode.LINE;
                        ocrText = args[1];
                        gtText = args[2];
                        checkLineMode(ocrText,gtText);
                        break;
            case "-x":  mode = Mode.XML;
                        ocrText = args[1];
                        gtText = args[2];
                        overwrite = !(args.length == 4)&&args[3].equals("-s");
                        //-----
                        if(!overwrite){System.out.println("SAFEMODE "+ANSI_GREEN+"ON"+ANSI_RESET);}
                        //-----
                        checkXmlMode(ocrText,gtText);
                        break;
            case "-b":  mode = Mode.BATCH;
                        pathDir = args[1];
                        checkBatchMode(pathDir);
                        break;
            case "-h":  mode = Mode.HELP;
                        break;
            default:
                mode = Mode.HELP;
                System.out.println("Use flag -h for help");
        }
        //Check for other flags in arguments
        for(int i = 0; i < args.length; i++) {
            if (args[i].equals("-s")) {overwrite = false;}
            if (args[i].equals("-c")) {checkCertainty = true;}
            if (args[i].equals("-a")) {shortResult = true;}
        }

        switch (mode) {
            case LINE:  doLineMode(ocrText,gtText); break;
            case XML:   doXmlMode(ocrText,gtText,overwrite); break;
            case BATCH: doBatchMode(pathDir,overwrite); break;
            case HELP:  printHelp(); break;
            default:    System.out.println("You should not be here");
        }

	    /*if(args[0].endsWith(".xml")) {
            XML_MODE = true;
        } else {
	        ocrText = args[0];
            gtText = args[1];
        }*/

	   /*if(XML_MODE) {
	        String textFile = args[1];
	        String xmlFile = args[0];

	        if(args.length == 3 && args[2].equals("prox")) {
	            try {
                    gtText = Files.readString(Paths.get(textFile));

                    PageXML pageXML = new PageXML(xmlFile);
                    List<Textline> lines = pageXML.listOcrLines();

                    for(int i = lines.size() -1; i >= 0 ; i--) {
                        ocrText = lines.get(i).getOcrText();
                        gtText = lines.get(i).getOcrText();
                        ocrText = Normalizer.normalize(ocrText, Normalizer.Form.NFKC);
                        gtText = Normalizer.normalize(gtText,Normalizer.Form.NFKC);

                        String[] proxResult = Aligner.proxAlign(ocrText,gtText, (int) Math.min(10,i));
                        System.out.println("\n\nTextLine ID: "+lines.get(i).getId()+"\n");
                        System.out.println("Testing:" + ANSI_CYAN + ocrText + ANSI_RESET);
                        System.out.println();
                        System.out.println(ANSI_RED + proxResult[0] + ANSI_RESET);
                        System.out.println();
                        System.out.println(ANSI_GREEN + proxResult[1] + ANSI_RESET);
                        System.out.println("\nSimilarity (using Levenshtein Distance): " + Aligner.calcSimilarity(proxResult)*100.0 + "%");
                        gtText = Aligner.cutGroundTruth(gtText,ocrText);
                    }

                    System.out.println("\nRest Ground Truth:\n"+gtText);
                } catch (Exception e) {
	                e.printStackTrace();
                }
            } else {

            }

        } else {


        }*/
    }

    public static int countLinesNew(String filename) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(filename));
        try {
            byte[] c = new byte[1024];

            int readChars = is.read(c);
            if (readChars == -1) {
                // bail out if nothing to read
                return 0;
            }

            // make it easy for the optimizer to tune this loop
            int count = 0;
            while (readChars == 1024) {
                for (int i=0; i<1024;) {
                    if (c[i++] == '\n') {
                        ++count;
                    }
                }
                readChars = is.read(c);
            }

            // count remaining characters
            while (readChars != -1) {
                System.out.println(readChars);
                for (int i=0; i<readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
                readChars = is.read(c);
            }

            return count == 0 ? 1 : count;
        } finally {
            is.close();
        }
    }

    public static void printHelp() {
        System.out.println("usage:");
        System.out.println();
        System.out.println("LINE_MODE: \t[-l <string> <string>]");
        System.out.println("XML_MODE: \t[-x xml-path=<path> gt-path<path>] [-s --safemode]");
        System.out.println("BATCH_MODE:\t[-b <path>] [-s --safemode]");
    }

    public static boolean checkLineMode(String s1, String s2) {
        if(s1.isEmpty() || s2.isEmpty()) {
            System.out.println("ERROR: There was only one string");
            return false;
        } else {
            return true;
        }
    }

    public static boolean checkXmlMode(String pathXml, String pathGt) {
        if(pathXml.endsWith(".xml") && pathGt.endsWith("gt.txt")) {
            Path path1 = Paths.get(pathXml);
            Path path2 = Paths.get(pathGt);
            if(Files.exists(path1) && Files.exists(path2)) {
                return true;
            } else {
                System.out.println("ERROR: Unknown File or Path");
                return false;
            }
        } else {
            System.out.println("ERROR: First file has to end with .xml and second file hast to end with .gt.txt");
            return false;
        }
    }

    public static boolean checkBatchMode(String folder) {
        Path path = Paths.get(folder);
        if(Files.exists(path)) {
            return true;
        } else {
            System.out.println("ERROR: Unknown File or Path");
            return false;
        }
    }

    public static void doLineMode(String ocrText, String gtText) {
        try {
            ocrText = Normalizer.normalize(ocrText, Normalizer.Form.NFKC);
            gtText = Normalizer.normalize(gtText,Normalizer.Form.NFKC);
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

    public static void doXmlMode(String xmlFile, String textFile, boolean overwrite) {
        try {

            int gtCount = countLinesNew(textFile);
            PageXML pageXML = new PageXML(xmlFile);
            List<Textline> lines = pageXML.listOcrLines();
            int ocrCount = lines.size();
            if(ocrCount != gtCount) {
                System.out.println("numbers of lines differed");
                System.out.println("No. of  gt liens was: " + gtCount);
                System.out.println("No. of ocr lines was: " + ocrCount);
                return;
            }
            BufferedReader reader;
            reader = new BufferedReader(new FileReader(textFile));
            String ocrText;
            String gtText = reader.readLine();
            for (Textline line: lines) {
                if(gtText != null) {
                    ocrText = Normalizer.normalize(line.getOcrText(), Normalizer.Form.NFKC);
                    gtText = Normalizer.normalize(gtText,Normalizer.Form.NFKC);

                    //String[] oldResult = Aligner.oldAlign(line.getOcrText(),gtText);
                    String[] result = Aligner.align(ocrText,gtText);
                    line.setLines(result);

                    System.out.println("\n\nTextLine ID: "+line.getId());

                    if(!shortResult) {
                        System.out.println("\nTesting:\t" + ANSI_YELLOW + ocrText + ANSI_RESET);
                        System.out.println();
                        System.out.println("Ocr aligned:\t"+result[2]);
                        System.out.println();
                        System.out.println("GT aligned:\t"+ANSI_GREEN + result[1]+ ANSI_RESET);
                        System.out.println();
                        System.out.println("GT Line:\t"+ ANSI_CYAN + gtText + ANSI_RESET + "\n");
                    }
                    System.out.println("Similarity (using Levenshtein Distance): " +ANSI_PURPLE +  String.format("%.2f", line.calcSim()*100) + "%" + ANSI_RESET);
                    gtText = reader.readLine();
                }
            }
            reader.close();
            if(overwrite) {pageXML.updateTextlines(lines);}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void doBatchMode(String pathDir, boolean overwrite) {
        //TODO
    }
}
