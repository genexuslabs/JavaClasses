package com.genexus.webpanels;

import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;

import com.genexus.servlet.ServletException;
import com.genexus.servlet.http.HttpServlet;
import com.genexus.servlet.http.IHttpServletRequest;
import com.genexus.servlet.http.IHttpServletResponse;

public final class gxver extends HttpServlet 
{
	private static Logger log = org.apache.logging.log4j.LogManager.getLogger(gxver.class);

    public void doGet(IHttpServletRequest request, IHttpServletResponse response) throws IOException
    {
    	if (log.isInfoEnabled()) {
			response.setContentType("text/html");
			PrintWriter writer = response.getWriter();
			writer.println("<html>");
			writer.println("<body>");
			writer.println("<b>Running GeneXus Runtime Classes Version " + com.genexus.Version.getFullVersion() + "</body>");
			writer.println("</body>");
			writer.println("</html>");
		}
    	else
		{
			response.setStatus(404);
		}
    }

	protected void callExecute(String method, IHttpServletRequest req, IHttpServletResponse res) throws ServletException{
    	try {
			doGet(req, res);
		}catch (IOException e) {
    		throw new ServletException(e.getMessage());
		}
	}
}
