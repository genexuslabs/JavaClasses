package com.genexus.search;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.tidy.Tidy;

import java.io.*;
import com.genexus.reports.Const;
import com.genexus.ModelContext;

public class JTidyHTMLHandler implements IDocumentHandler {

    private static String configurationFile;	private static java.io.File configFile;
    static {
        String fileName = "tidy.cfg";
        String WEB_INF="WEB-INF";
        try{
            String defaultRelativePrepend = ModelContext.getModelContext().
                                            getHttpContext().getDefaultPath();
            if (new File(defaultRelativePrepend + File.separatorChar + WEB_INF).isDirectory()) {
                configurationFile = defaultRelativePrepend + File.separatorChar + WEB_INF +
                                    File.separatorChar + fileName;
            } else {
                configurationFile = defaultRelativePrepend + File.separatorChar +
                                    fileName;
            }			configFile = new java.io.File(configurationFile);
        }catch(Exception ex){
            System.out.println(ex.getMessage());
        }
    }

    public String htmlClean(InputStream is) {
        Tidy tidy = new Tidy();		if (configFile.exists())		{
			tidy.setConfigurationFromFile(configurationFile);		}
        tidy.setMakeClean(true);		tidy.setShowWarnings(false);
        try {

            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            tidy.parse(is, baos);
            is.close();
            return baos.toString();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return "";
        }
    }

    public String getTextFromString(String text) {
        Tidy tidy = new Tidy();
		if (configFile.exists())		{
			tidy.setConfigurationFromFile(configurationFile);		}
        tidy.setQuiet(true);
        tidy.setShowWarnings(false);
        org.w3c.dom.Document root = tidy.parseDOM(new StringReader(text), null);
        Element rawDoc = root.getDocumentElement();
        if (rawDoc==null){
			return text;
		}else{
            String bodyText = getText(rawDoc);
            return bodyText;
        }
    }
    public String getText(String filename) {
        Tidy tidy = new Tidy();
		if (configFile.exists())		{
			tidy.setConfigurationFromFile(configurationFile);		}
        tidy.setQuiet(true);
        tidy.setShowWarnings(false);
        tidy.setMakeClean(true);
        try {
            InputStream is = new FileInputStream(filename);
            org.w3c.dom.Document root = tidy.parseDOM(is, null);
            Element rawDoc = root.getDocumentElement();
			String bodyText = getText(rawDoc);
			is.close();
			return bodyText;
        } catch (IOException ex) {

        }
        return "";
    }


  /**
   * Extracts text from the DOM node.
   *
   * @param node a DOM node
   * @return the text value of the node
   */
  protected String getText(Node node) {
    NodeList children = node.getChildNodes();
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      switch (child.getNodeType()) {
        case Node.ELEMENT_NODE:
          sb.append(getText(child));
          sb.append(" ");
          break;
        case Node.TEXT_NODE:
          sb.append(((Text) child).getData());
          break;
      }
    }
    return sb.toString();
  }

}
