package de.uniwue;

import org.apache.commons.lang3.StringUtils;

public class Textline {
    private String id;
    private String ocrText;
    private String gtText;
    private double similarity;

    public Textline(String id, String ocrText) {
        this.id = id;
        this.ocrText = ocrText;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setOcrText(String ocrText) {
        this.ocrText = ocrText;
    }

    public void setGtText(String gtText) {
        this.gtText = gtText;
    }

    public void setLines(String[] lines) {
        this.ocrText = lines[0];
        this.gtText = lines[1];
    }

    public String getId() {
        return id;
    }

    public String getOcrText() {
        return ocrText;
    }

    public String getGtText() {
        return gtText;
    }

    public double calcSim() {
        if(gtText != null && ocrText != null) {
            String longer = ocrText, shorter = gtText;
            if (ocrText.length() < gtText.length()) { // longer should always have greater length
                longer = gtText; shorter = ocrText;
            }
            return (1.0)-(StringUtils.getLevenshteinDistance(gtText,ocrText))/(double)longer.length();
        } else {
            System.out.println("OCRtext or GTtext was null");
            return 0;
        }
    }
}
