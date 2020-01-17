package de.uniwue;

public class Textline {
    private String id;
    private String ocrText;
    private String gtText;

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

    public String getId() {
        return id;
    }

    public String getOcrText() {
        return ocrText;
    }

    public String getGtText() {
        return gtText;
    }

}
