package de.uniwue;


import com.google.gson.Gson;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.json.JSONObject;
import java.io.InputStreamReader;
import java.util.Arrays;


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

	    /*
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("javascript");
        try {
            engine.eval(new InputStreamReader(Main.class.getResourceAsStream("alignment.js")));

        }  catch (ScriptException e) {
            e.printStackTrace();
        }

        Invocable inv = (Invocable) engine;*/

	    if(XML_MODE) {
            System.out.println("XML not yer supported");
        } else {
	        /*try {
                Object obj = inv.invokeFunction("nw", ocrText, gtText);
                Gson gson = new Gson();
                String k = gson.toJson(obj);
                JSONObject o = new JSONObject(k);
                System.out.println(o.getString("0"));
                System.out.println(o.getString("1"));
            } catch (Exception e) {
	            e.printStackTrace();
            }*/

        }
    }

    public static void nw(String s1, String s2) {
        final int G = 2;
        final int P = 1;
        final int M = -1;

        int[][] mat = new int[s1.length()][s2.length()];
        int[][] direc = new int[s1.length()][s2.length()];

        for(int[] row : mat){
            Arrays.fill(row, 0);
        }
        for(int i = 0; i<=s1.length(); i++) {
            Arrays.fill(mat[i],0);
        }
    }
}
