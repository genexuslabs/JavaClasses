package com.genexus.search;

import java.io.IOException;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;


public class PdfHandler implements IDocumentHandler {
    public String getText(String filename) {
        StringBuffer buffer = new StringBuffer();
        try {
            PdfReader reader = new PdfReader(filename);
            PdfTextExtractor parser = new PdfTextExtractor(reader);
            int totalPages = reader.getNumberOfPages();

            for (int i = 1; i <= totalPages; i++) {
                buffer.append(parser.getTextFromPage(i));
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println(e.getMessage());
        }
        return buffer.toString();
    }
}