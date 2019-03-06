package com.genexus.webpanels;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class gxver extends HttpServlet 
{
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
        response.setContentType("text/html");
        PrintWriter writer = response.getWriter();
        writer.println("<html>");
        writer.println("<body>");
        writer.println("<b>Running GeneXus Runtime Classes Version " + com.genexus.Version.getFullVersion() + "</body>");
        writer.println("</body>");
        writer.println("</html>");
    }
}
