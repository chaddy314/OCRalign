package de.uniwue;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.apache.commons.cli.*;

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
    protected static double certainty = 0.5;
    protected static boolean insert = false;
    protected static boolean wodiak = false;
    protected static boolean isBatch = false;
    protected static boolean noconfirm = false;
    protected static double simTotal = 0.0;
    protected static int lineCount = 0;
    protected static int offByOneCount = 0;
    protected static int warningPages = 0;
    protected static String offByOnePages = "";
    protected static String falsePages = "";
    protected static int ngram = 5;

    public static void main(String[] args) {

        CommandLineParser parser = new DefaultParser();
        Options options = getOptions();
        try {
            CommandLine line = parser.parse(options,args);
            execute(line,options);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void execute(CommandLine line, Options options) throws InterruptedException {
        if(line.hasOption("i")) { insert = true; System.out.println("INSERTMODE  "+ANSI_GREEN+"ON"+ANSI_RESET);}
        if(line.hasOption("sm")) { overwrite = false; System.out.println("SAFEMODE  "+ANSI_GREEN+"ON"+ANSI_RESET);}
        if(line.hasOption("a")) { shortResult = true; System.out.println("SHORTMODE "+ANSI_GREEN+"ON"+ANSI_RESET);}
        if(line.hasOption("w")) {wodiak = true; System.out.println("WODIAK "+ANSI_GREEN+"ON"+ANSI_RESET);}
        if(line.hasOption("y")) {noconfirm = true; System.out.println("NO-CONFIRM "+ANSI_GREEN+"ON"+ANSI_RESET);}
        if(line.hasOption("c")) {
            checkCertainty = true;
            System.out.println("CHECKMODE "+ANSI_GREEN+"ON"+ANSI_RESET);
            if(line.getOptionValue("c") != null) {
                certainty = Double.parseDouble(line.getOptionValue("c","0.5"));
                System.out.println("c is now " + certainty);
            }
        }
        if(line.hasOption("n")) {
            if(line.getOptionValue("c") != null) {
                ngram = Integer.parseInt(line.getOptionValue("n","5"));
                System.out.println("N-GRAM: n = "+ANSI_GREEN+ngram+ANSI_RESET+"\t (recommended: n = 5)");
            }
        }
        String ocr,gt,dir;
        if(line.hasOption("l")
                && !line.hasOption("x")
                && !line.hasOption("b")
                && !line.hasOption("i")
                && !line.hasOption("s")) {
            System.out.println("line mode");
            ocr = line.getOptionValues("l")[0];
            gt = line.getOptionValues("l")[1];
            if(checkLineMode(ocr,gt)) { doLineMode(ocr,gt); }
        } else if(line.hasOption("x")
                && !line.hasOption("l")
                && !line.hasOption("b")
                && !line.hasOption("i")
                && !line.hasOption("s")) {
            System.out.println("xml mode");
            ocr = line.getOptionValues("x")[0];
            gt = line.getOptionValues("x")[1];
            if(checkXmlMode(ocr,gt)) { doXmlMode(ocr,gt, overwrite); }
        } else if(line.hasOption("b")
                && !line.hasOption("i")
                && !line.hasOption("l")
                && !line.hasOption("x")
                && !line.hasOption("s")) {
            System.out.println("batch mode");
            isBatch = true;
            dir = line.getOptionValue("b");
            Thread.sleep(2000);
            if(checkBatchMode(dir)){ System.out.println("starting...");doBatchMode(dir,overwrite); }
        } else if(!line.hasOption("b")
                && !line.hasOption("i")
                && !line.hasOption("l")
                && !line.hasOption("x")
                && line.hasOption("s")) {
            System.out.println("Search Mode");
            ocr = line.getOptionValues("s")[0];
            dir = line.getOptionValues("s")[1];
            if(checkSearchMode(ocr,dir)){ System.out.println("starting...");doSearchMode(ocr,dir,noconfirm); }
        }else{
                HelpFormatter formatter = new HelpFormatter();
                formatter.setOptionComparator(null);
                formatter.printHelp("ocralign",options,true);
        }

    }

    public static int countLinesNew(String filename) throws IOException {
        /*InputStream is = new BufferedInputStream(new FileInputStream(filename));
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
        }*/
        FileReader fileReader = null;
        LineNumberReader lnr = null;
        String str = "";
        int i = 0;


        try {
            fileReader = new FileReader(filename);
            lnr = new LineNumberReader(fileReader);
            str = lnr.readLine();
            while(str!=null && !str.equals("")) {
                i++;
                str = lnr.readLine();
            }
        } catch (Exception e) { e.printStackTrace(); }
        finally {
            if(fileReader!=null) { fileReader.close(); }
            if(lnr!=null) { lnr.close(); }
        }
        return i;
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
        if(pathXml.endsWith(".xml") && (pathGt.endsWith("gt.txt")|| pathGt.endsWith("gt.wodiak.txt"))) {
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
        File dir = new File(folder);
        if(!dir.isDirectory()) {
            System.out.println("Not a Directory");
            return false;
        } else {return true;}
    }

    public static boolean checkSearchMode(String pathXml, String pathCorpus) {
        File corpusFolder = new File(pathCorpus);
        File[] txtInDir = corpusFolder.listFiles((d, name) -> name.endsWith(".txt"));
        if(pathXml.endsWith(".xml") && corpusFolder.isDirectory()) {
            Path path1 = Paths.get(pathXml);
            Path path2 = Paths.get(pathCorpus);
            if(Files.exists(path1) && Files.exists(path2)) {
                if(txtInDir.length !=0) {
                    return true;
                } else {
                    System.out.println("Directory has to contain at least one text file");
                    return false;
                }
            } else {
                System.out.println("ERROR: Unknown File or Path");
                return false;
            }
        } else {
            System.out.println("ERROR: First file has to end with .xml and second file hast to be a directory");
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
                if(isBatch) { textFile = ("\n" + textFile.substring(textFile.lastIndexOf(File.separator),textFile.length())); }

                if(Math.abs(ocrCount-gtCount) == 1) {
                    offByOneCount++;
                    offByOnePages += textFile;
                } else {
                    falsePages += textFile + "ocrcount = " + ocrCount + "\tgtcount: " + gtCount;
                }

                return;
            }
            BufferedReader reader;
            reader = new BufferedReader(new FileReader(textFile));
            String ocrText;
            String gtText = reader.readLine();
            for (Textline line: lines) {
                if(gtText != null) {
                    if(insert) {
                        //TODO
                    } else {
                        ocrText = Normalizer.normalize(line.getOcrText(), Normalizer.Form.NFKC);
                        gtText = Normalizer.normalize(gtText,Normalizer.Form.NFKC);

                        //String[] oldResult = Aligner.oldAlign(line.getOcrText(),gtText);
                        String[] result = Aligner.align(ocrText,gtText);
                        line.setLines(result);



                        if(!shortResult || certainty >= line.calcSim()) {
                            System.out.println("\n\nTextLine ID: "+line.getId());
                            System.out.println("\nTesting:\t" + ANSI_YELLOW + ocrText + ANSI_RESET);
                            System.out.println();
                            System.out.println("Ocr aligned:\t" + result[2]);
                            System.out.println();
                            System.out.println("GT aligned:\t" + ANSI_GREEN + result[1] + ANSI_RESET);
                            System.out.println();
                            //System.out.println("GT Line:\t" + ANSI_CYAN + gtText + ANSI_RESET + "\n");

                            System.out.println("Similarity (using Levenshtein Distance): " + ANSI_CYAN + String.format("%.2f", line.calcSim() * 100) + "%" + ANSI_RESET);
                        }
                        if(isBatch) { addSim(line.calcSim());}
                    }
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
        File dir = new File(pathDir);
        if(!dir.isDirectory()) {
            System.out.println("Not a Directory");
            return;
        }
        File[] xmlInDir = dir.listFiles((d, name) -> name.endsWith(".xml"));
        String xmlEnding = ".gt.txt";
        File[] txtInDir;

        if(wodiak) { txtInDir = dir.listFiles((d, name) -> name.endsWith(".gt.wodiak.txt"));}
        else { txtInDir = dir.listFiles((d, name) -> name.endsWith(".gt.txt")); }

        List<File> sortedXml = Arrays.stream(xmlInDir)
                .sorted((f1,f2) -> f1.getName().compareTo(f2.getName())).collect(Collectors.toList());
        List<File> sortedTxt = Arrays.stream(txtInDir)
                .sorted((f1,f2) -> f1.getName().compareTo(f2.getName())).collect(Collectors.toList());
        if(sortedXml.size() == 0 || sortedTxt.size() == 0) {
            System.out.println("no XML or TXT files in dir");
            return;
        }
        try {
            for (File xml : sortedXml) {
                for (File gt : sortedTxt) {
                    //System.out.println("comparing"+xml.getCanonicalPath()+" with "+gt.getCanonicalPath());
                    String cutXml = xml.getAbsolutePath().replaceAll("(.gt.wodiak.txt|.gt.txt|.xml)", "");
                    String cutTxt = gt.getAbsolutePath().replaceAll("(.gt.wodiak.txt|.gt.txt|.xml)","");
                    //System.out.println(cutXml + "\t" + cutTxt);
                    if (cutXml.equals(cutTxt)) {
                        String xmlFilename = xml.getCanonicalPath().substring(xml.getCanonicalPath().lastIndexOf(File.separator),xml.getCanonicalPath().length());
                        String gtFilename = gt.getCanonicalPath().substring(gt.getCanonicalPath().lastIndexOf(File.separator),gt.getCanonicalPath().length());
                        System.out.println("comparing"+xmlFilename+" with "+gtFilename);
                        doXmlMode(xml.getAbsolutePath(),gt.getAbsolutePath(),overwrite);
                    }
                }
            }
            if(offByOneCount > 0) {
                System.out.println(offByOnePages);
                System.out.println("Off by One line count was: " + offByOneCount);
                System.out.println("\n FALSE PAGES:");
                System.out.println(falsePages);
            }
            System.out.println("\n FALSE PAGES:");
            System.out.println(falsePages);
            System.out.println("\nOverall Similarity: "+ getOverallSim()*100.0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void doSearchMode(String xmlFile, String pathDir, boolean noconfirm) {
        File dir = new File(pathDir);
        File[] txtInDir = dir.listFiles((d, name) -> name.endsWith(".txt"));

        List<File> sortedTxt = Arrays.stream(txtInDir)
                .sorted((f1,f2) -> f1.getName().compareTo(f2.getName())).collect(Collectors.toList());
        try {
            PageXML pageXML = new PageXML(xmlFile);
            List<Textline> lines = pageXML.listOcrLines();
            Scanner in = new Scanner(System.in);
            String input = "";
            for (Textline line: lines) {
                String ocrText = Normalizer.normalize(line.getOcrText(), Normalizer.Form.NFKC);
                String command = buildCommand(ocrText,pathDir,ngram);
                ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", command);

                Process p = pb.start();
                String output = loadStream(p.getInputStream());
                String error = loadStream(p.getErrorStream());
                int rc = p.waitFor();
                System.out.println("Process ended with rc=" + rc);
                System.out.println(ANSI_GREEN+"\nStandard Output:\n"+ANSI_RESET);
                System.out.println(output);
                if(!error.equals("")) {
                    System.out.println("\nStandard Error:\n");
                    System.out.println(error);
                }

                if(!noconfirm) {
                    Boolean accepted = false;
                    while(!accepted) {
                        System.out.println("Confirm? Type: y(es) or n(o) and press Enter");
                        input = in.nextLine();
                        if(input.equals("y") || input.equals("yes")) {
                            accepted = true;
                            line = generateOutput(ocrText,retHit(output),line);

                        } else if(input.equals("n") || input.equals("no")) {
                            accepted = true;
                        } else {
                            System.out.println("Please type y(es) or n(o)");
                        }
                    }
                } else {
                    line = generateOutput(ocrText,retHit(output),line);
                }
            }

            if(overwrite) {pageXML.updateTextlines(lines);}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String loadStream(InputStream s) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(s));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null)
            sb.append(line).append("\n");
        return sb.toString();
    }

    private static Options getOptions() {
        Options options = new Options();
        Option lineMode       = Option.builder("l").argName("ocrdata> <gtdata")
                                                        .hasArgs()
                                                        .numberOfArgs(2)
                                                        .longOpt("line-mode")
                                                        .desc("aligns two strings")
                                                        .build();
        Option xmlMode        = Option.builder("x").argName("pagexml> <gt.txt")
                                                        .hasArgs()
                                                        .numberOfArgs(2)
                                                        .longOpt("xml-mode")
                                                        .desc("aligns every line from pageXml to corresponding gt.txt")
                                                        .build();
        Option batchMode      = Option.builder("b").argName("folder")
                                                        .hasArg()
                                                        .longOpt("batch-mode")
                                                        .desc("Aligns every file in folder")
                                                        .build();
        Option searchMode     = Option.builder("s").argName("pagexml> <corpus folder")
                                                        .hasArgs()
                                                        .numberOfArgs(2)
                                                        .longOpt("search-mode")
                                                        .desc("searches every line of xml in specified corpus folder")
                                                        .build();
        OptionGroup optionGroup = new OptionGroup();
        optionGroup.isRequired();
        optionGroup.addOption(lineMode);
        optionGroup.addOption(xmlMode);
        optionGroup.addOption(batchMode);
        optionGroup.addOption(searchMode);
        options.addOptionGroup(optionGroup);

        options.addOption("i","insert",false,"[OPTIONAL] writes every gt line in xml line");
        options.addOption("sm","safemode",false, "[OPTIONAL] Only reads XML file(s)");
        options.addOption("a","shortmode",false,"[OPTIONAL] Abbreviates output");
        options.addOption("y","no-confirm",false,"[OPTIONAL] Automatically confirms and writes every line to xml");
        Option check          = Option.builder("c").argName("certainty")
                                                        .longOpt("check")
                                                        .valueSeparator()
                                                        .desc("[OPTIONAL] Only writes if similarity > c (default:0.5)")
                                                        .build();
        Option ngram          = Option.builder("n").argName("ngram")
                .longOpt("ngram")
                .valueSeparator()
                .desc("[OPTIONAL] The length of the n-gram that is used for the search, less then 4 may result in a very long computation time")
                .build();
        options.addOption(ngram);
        options.addOption(check);
        options.addOption("w","wodiak",false,"[OPTIONAL] use gt.wodiak.txt");
        return options;
    }

    private static void addSim(double sim) {
        lineCount++;
        simTotal += sim;
    }

    private static double getOverallSim() {
        return (double)simTotal/lineCount;
    }

    private static String buildCommand(String ocrLine, String gtDir, int ngram) {
        String command = "";
        command += "python3 search.py --query=";
        command += ("\'" + ocrLine + "\' ");
        command += "--path=";
        command += ("\'" + gtDir + "\' ");
        command += "--n-gram=";
        command += ngram;
        return command;
    }

    private static String retHit(String output) {
        if(output.equals("")) {
            return "";
        } else {
            String lines[] = output.split("Hit:");
            lines = lines[1].split("at pos:");
            return lines[0].replaceAll("\n", "");
        }
    }

    public static Textline generateOutput(String ocrText, String gtLine, Textline line) {
        String[] result = Aligner.align(ocrText, gtLine);
        line.setLines(result);
        if (!shortResult || certainty >= line.calcSim()) {
            System.out.println("\n\nTextLine ID: " + line.getId());
            //ystem.out.println("\nTesting:\t" + ANSI_YELLOW + ocrText + ANSI_RESET);
            //System.out.println();
            System.out.println("Ocr aligned:\t" + result[2]);
            System.out.println();
            System.out.println("GT aligned:\t" + ANSI_GREEN + result[1] + ANSI_RESET);
            System.out.println();
            //System.out.println("GT Line:\t" + ANSI_CYAN + gtText + ANSI_RESET + "\n");

            System.out.println("Similarity (using Levenshtein Distance): " + ANSI_CYAN + String.format("%.2f", line.calcSim() * 100) + "%" + ANSI_RESET);
        }
        return line;
    }
}
