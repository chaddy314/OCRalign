package de.uniwue;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class PageXML {

    private Document pageXml;
    private Element rootElement;
    private List<Textline> textlines;

    private String path = "";
    public PageXML(String path) {
        this.path = path;
        try {
            pageXml = parseXML(path);
            this.rootElement = pageXml.getDocumentElement();
            this.textlines = listOcrLines();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Textline> listOcrLines() {
        List<Textline> ocrLines = new LinkedList<Textline>();
        NodeList nList = rootElement.getChildNodes();
        NodeList cList = null;
        for(int i = 0; i < nList.getLength() ;i++) {
            if (nList.item(i).getNodeName() == "Page") {
                cList = nList.item(i).getChildNodes();
            }
        }
        List<Element> pList = new LinkedList<Element>();
        if(cList != null) {

            for(int i = 0; i < cList.getLength(); i++) {
                Element element = (Element) cList.item(i);
                if(element.getAttribute("type").equals("paragraph")) {
                    pList.add(element);
                }
            }

            if(pList.size() != 0) {
                NodeList tList;
                List<Element> textElementList = new LinkedList<Element>();
                for (Element  element: pList) {
                    tList = element.getElementsByTagName("TextLine");
                    for(int i = 0; i< tList.getLength(); i++) {
                        textElementList.add((Element)tList.item(i));
                    }
                }

                for (Element xmlTextline : textElementList){
                    String id = xmlTextline.getAttribute("id");
                    //System.out.println(id);
                    NodeList anotherList = xmlTextline.getChildNodes();
                    for(int i = 0; i < anotherList.getLength();i++) {
                        Element element = (Element) anotherList.item(i);
                        //System.out.println(element.getTagName());
                        //System.out.println();
                        //Element unicode = (Element) element.getChildNodes().item(0);
                        if(element.getTagName() == "TextEquiv" && element.getAttribute("index").equals("1")) {
                            //System.out.println(element.getChildNodes().item(0).getTextContent());
                            ocrLines.add(new Textline(id,element.getChildNodes().item(0).getTextContent()));
                        }
                    }
                }
            } else {
                System.out.println("no paragraphs found");
            }

        } else {
            System.out.println("cList was null");
        }

        return ocrLines;
    }

    public Textline getTextLineByID(String id) {
        for (Textline line : textlines) {
            if(line.getId().equals(id)) {
                return line;
            }
        }
        return null;
    }

    public void updateTextlines(List<Textline> lines) {
        this.textlines = lines;
        List<Textline> ocrLines = new LinkedList<Textline>();
        NodeList nList = rootElement.getChildNodes();
        NodeList cList = null;
        Element e = null;
        for(int i = 0; i < nList.getLength() ;i++) {
            if (nList.item(i).getNodeName() == "Page") {
                cList = nList.item(i).getChildNodes();
            }
        }
        List<Element> pList = new LinkedList<Element>();
        if(cList != null) {

            for(int i = 0; i < cList.getLength(); i++) {
                Element element = (Element) cList.item(i);
                if(element.getAttribute("type").equals("paragraph")) {
                    pList.add(element);
                }
            }

            if(pList.size() != 0) {
                NodeList tList;
                List<Element> textElementList = new LinkedList<Element>();
                for (Element  element: pList) {
                    tList = element.getElementsByTagName("TextLine");
                    for(int i = 0; i< tList.getLength(); i++) {
                        textElementList.add((Element)tList.item(i));
                    }
                }

                for (Element xmlTextline : textElementList){
                    boolean hasGt = false;
                    String xmlId = xmlTextline.getAttribute("id");
                    Textline line = getTextLineByID(xmlId);
                    if(Main.checkCertainty && !line.isSure()) {
                        System.out.println("Not updating line("+xmlId+") because similarity was " + line.calcSim()*100+"%");
                    } else {
                        NodeList anotherList = xmlTextline.getChildNodes();
                        for(int i = 0; i<anotherList.getLength(); i++) {
                            Element node = (Element) anotherList.item(i);
                            String index = node.getAttribute("index");
                            if(index.equals("0")) {
                                hasGt = true;
                                node.getChildNodes().item(0).setTextContent(line.getGtText());
                                System.out.println("changed "+node.getChildNodes().item(0).getNodeName()+" GT content of "+xmlId+" to"+node.getChildNodes().item(0).getTextContent());
                            } else if (index.equals("1")) {
                                node.getChildNodes().item(0).setTextContent(line.getOcrText());
                                System.out.println("changed OCR content of "+xmlId+" to"+node.getChildNodes().item(0).getTextContent());
                            }
                        }
                        //make new GT child and append
                        if(!hasGt) {
                            Element newTextEquiv = pageXml.createElement("TextEquiv");
                            newTextEquiv.setAttribute("index", "0");
                            Element newText = pageXml.createElement("Unicode");
                            newText.appendChild(pageXml.createTextNode(line.getGtText()));
                            newTextEquiv.appendChild(newText);
                            xmlTextline.appendChild(newTextEquiv);
                            System.out.println("added GT textline with id: "+xmlId);
                        }
                    }
                }

                try {
                    DOMSource source = new DOMSource(pageXml);

                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    StreamResult result = new StreamResult(path);
                    transformer.transform(source,result);
                } catch (TransformerConfigurationException te) {
                    te.printStackTrace();
                } catch (TransformerException te) {
                    te.printStackTrace();
                }

            } else {
                System.out.println("no paragraphs found");
            }

        } else {
            System.out.println("cList was null");
        }
    }

    private Document parseXML(String filePath) throws ParserConfigurationException,
                                                      IOException,
                                                      org.xml.sax.SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(filePath);
        doc.getDocumentElement().normalize();
        return doc;
    }
}
