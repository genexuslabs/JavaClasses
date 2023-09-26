package HTTPClient;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ProtocolException;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

import java.awt.Frame;
import java.awt.Panel;
import java.awt.Label;
import java.awt.Color;
import java.awt.Button;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;


/**
 * This module handles Netscape cookies (also called Version 0 cookies)
 * and Version 1 cookies. Specifically is reads the <var>Set-Cookie</var>
 * and <var>Set-Cookie2</var> response headers and sets the <var>Cookie</var>
 * and <var>Cookie2</var> headers as neccessary.
 *
 * <P>The accepting and sending of cookies is controlled by a
 * <var>CookiePolicyHandler</var>. This allows you to fine tune your privacy
 * preferences. A cookie is only added to the cookie list if the handler
 * allows it, and a cookie from the cookie list is only sent if the handler
 * allows it.
 *
 * <P>This module expects to be the only one handling cookies. Specifically, it
 * will remove any <var>Cookie</var> and <var>Cookie2</var> header fields found
 * in the request, and it will remove the <var>Set-Cookie</var> and
 * <var>Set-Cookie2</var> header fields in the response (after processing them).
 * In order to add cookies to a request or to prevent cookies from being sent,
 * you can use the {@link #addCookie(HTTPClient.Cookie) addCookie} and {@link
 * #removeCookie(HTTPClient.Cookie) removeCookie} methods to manipulate the
 * module's list of cookies.
 *
 * <P>A cookie jar can be used to store cookies between sessions. This file is
 * read when this class is loaded and is written when the application exits;
 * only cookies from the default context are saved. The name of the file is
 * controlled by the system property <var>HTTPClient.cookies.jar</var> and
 * defaults to a system dependent name. The reading and saving of cookies is
 * enabled by setting the system property <var>HTTPClient.cookies.save</var>
 * to <var>true</var>.
 *
 * @see <a href="http://home.netscape.com/newsref/std/cookie_spec.html">Netscape's cookie spec</a>
 * @see <a href="http://www.ietf.org/rfc/rfc2965.txt">HTTP State Management Mechanism spec</a>
 * @version	0.3-3  06/05/2001
 * @author	Ronald Tschal�r
 * @since	V0.3
 */
public class CookieModule implements HTTPClientModule
{
    /** the list of known cookies */
    private static Hashtable cookie_cntxt_list = new Hashtable();

    /** the file to use for persistent cookie storage */
    private static File cookie_jar = null;

    /** an object, whose finalizer will save the cookies to the jar */
    private static Object cookieSaver = null;

    /** the cookie policy handler */
    private static CookiePolicyHandler cookie_handler =
					    new DefaultCookiePolicyHandler();



    // read in cookies from disk at startup

    static
    {
	boolean persist;
	try
	    { persist = Boolean.getBoolean("HTTPClient.cookies.save"); }
	catch (Exception e)
	    { persist = false; }

	if (persist)
	{
	    loadCookies();

	    // the nearest thing to atexit() I know of...

	    cookieSaver = new Object()
		{
		    public void finalize() { saveCookies(); }
		};
	    try
		{ System.runFinalizersOnExit(true); }
	    catch (Throwable t)
		{ }
	}
    }

	@SuppressWarnings("unchecked")
    private static void loadCookies()
    {
	// The isFile() etc need to be protected by the catch as signed
	// applets may be allowed to read properties but not do IO
	try
	{
	    cookie_jar = new File(getCookieJarName());
	    if (cookie_jar.isFile()  &&  cookie_jar.canRead())
	    {
		ObjectInputStream ois =
		    new ObjectInputStream(new FileInputStream(cookie_jar));
		cookie_cntxt_list.put(HTTPConnection.getDefaultContext(),
				      (Hashtable) ois.readObject());
		ois.close();
	    }
	}
	catch (Throwable t)
	    { cookie_jar = null; }
    }


    private static void saveCookies()
    {
	if (cookie_jar != null  &&  (!cookie_jar.exists()  ||
	     cookie_jar.isFile()  &&  cookie_jar.canWrite()))
	{
	    Hashtable<Cookie, Cookie> cookie_list = new Hashtable<>();
	    Enumeration enumera = Util.getList(cookie_cntxt_list,
					    HTTPConnection.getDefaultContext())
				   .elements();

	    // discard cookies which are not to be kept across sessions

	    while (enumera.hasMoreElements())
	    {
		Cookie cookie = (Cookie) enumera.nextElement();
		if (!cookie.discard())
		    cookie_list.put(cookie, cookie);
	    }


	    // save any remaining cookies in jar

	    if (cookie_list.size() > 0)
	    {
		try
		{
		    ObjectOutputStream oos =
			new ObjectOutputStream(new FileOutputStream(cookie_jar));
		    oos.writeObject(cookie_list);
		    oos.close();
		}
		catch (Throwable t)
		    { }
	    }
	}
    }


    private static String getCookieJarName()
    {
	String file = null;

	try
	    { file = System.getProperty("HTTPClient.cookies.jar"); }
	catch (Exception e)
	    { }

	if (file == null)
	{
	    // default to something reasonable

	    String os = System.getProperty("os.name");
	    if (os.equalsIgnoreCase("Windows 95")  ||
		os.equalsIgnoreCase("16-bit Windows")  ||
		os.equalsIgnoreCase("Windows"))
	    {
		file = System.getProperty("java.home") +
		       File.separator + ".httpclient_cookies";
	    }
	    else if (os.equalsIgnoreCase("Windows NT"))
	    {
		file = System.getProperty("user.home") +
		       File.separator + ".httpclient_cookies";
	    }
	    else if (os.equalsIgnoreCase("OS/2"))
	    {
		file = System.getProperty("user.home") +
		       File.separator + ".httpclient_cookies";
	    }
	    else if (os.equalsIgnoreCase("Mac OS")  ||
		     os.equalsIgnoreCase("MacOS"))
	    {
		file = "System Folder" + File.separator +
		       "Preferences" + File.separator +
		       "HTTPClientCookies";
	    }
	    else		// it's probably U*IX or VMS
	    {
		file = System.getProperty("user.home") +
		       File.separator + ".httpclient_cookies";
	    }
	}

	return file;
    }


    // Constructors

    CookieModule()
    {
    }


    // Methods

    /**
     * Invoked by the HTTPClient.
     */
    public int requestHandler(Request req, Response[] resp)
    {
	// First remove any Cookie headers we might have set for a previous
	// request

	NVPair[] hdrs = req.getHeaders();
	int length = hdrs.length;
	for (int idx=0; idx<hdrs.length; idx++)
	{
	    int beg = idx;
	    while (idx < hdrs.length  && cookie_cntxt_list.size() != 0 &&
		   hdrs[idx].getName().equalsIgnoreCase("Cookie"))
		idx++;

	    if (idx-beg > 0)
	    {
		length -= idx-beg;
		System.arraycopy(hdrs, idx, hdrs, beg, length-beg);
	    }
	}
	if (length < hdrs.length)
	{
	    hdrs = Util.resizeArray(hdrs, length);
	    req.setHeaders(hdrs);
	}


	// Now set any new cookie headers

	Hashtable cookie_list =
	    Util.getList(cookie_cntxt_list, req.getConnection().getContext());
	if (cookie_list.size() == 0)
	    return REQ_CONTINUE;	// no need to create a lot of objects

	Vector<String>  names   = new Vector<>();
	Vector<Integer>  lens    = new Vector<>();
	int     version = 0;

	synchronized (cookie_list)
	{
	    Enumeration list = cookie_list.elements();
	    Vector<Cookie> remove_list = null;

	    while (list.hasMoreElements())
	    {
		Cookie cookie = (Cookie) list.nextElement();

		if (cookie.hasExpired())
		{
		    Log.write(Log.COOKI, "CookM: cookie has expired and is " +
					 "being removed: " + cookie);
		    if (remove_list == null)  remove_list = new Vector<>();
		    remove_list.addElement(cookie);
		    continue;
		}

		if (cookie.sendWith(req)  &&  (cookie_handler == null  ||
		    cookie_handler.sendCookie(cookie, req)))
		{
		    int len = cookie.getPath().length();
		    int idx;

		    // insert in correct position
		    for (idx=0; idx<lens.size(); idx++)
			if (lens.elementAt(idx).intValue() < len)
			    break;

		    names.insertElementAt(cookie.toExternalForm(), idx);
		    lens.insertElementAt(new Integer(len), idx);

		    if (cookie instanceof Cookie2)
			version = Math.max(version, ((Cookie2) cookie).getVersion());
		}
	    }

	    // remove any marked cookies
	    // Note: we can't do this during the enumeration!
	    if (remove_list != null)
	    {
		for (int idx=0; idx<remove_list.size(); idx++)
		    cookie_list.remove(remove_list.elementAt(idx));
	    }
	}

	if (!names.isEmpty())
	{
	    StringBuffer value = new StringBuffer();

	    if (version > 0)
		value.append("$Version=\"" + version + "\"; ");

	    value.append(names.elementAt(0));
	    for (int idx=1; idx<names.size(); idx++)
	    {
		value.append("; ");
		value.append(names.elementAt(idx));
	    }
	    hdrs = Util.resizeArray(hdrs, hdrs.length+1);
	    hdrs[hdrs.length-1] = new NVPair("Cookie", value.toString());

	    // add Cookie2 header if necessary
	    if (version != 1)	// we currently know about version 1 only
	    {
		int idx;
		for (idx=0; idx<hdrs.length; idx++)
		    if (hdrs[idx].getName().equalsIgnoreCase("Cookie2"))
			break;
		if (idx == hdrs.length)
		{
		    hdrs = Util.resizeArray(hdrs, hdrs.length+1);
		    hdrs[hdrs.length-1] =
				    new NVPair("Cookie2", "$Version=\"1\"");
		}
	    }

	    req.setHeaders(hdrs);

	    Log.write(Log.COOKI, "CookM: Sending cookies '" + value + "'");
	}

	return REQ_CONTINUE;
    }


    /**
     * Invoked by the HTTPClient.
     */
    public void responsePhase1Handler(Response resp, RoRequest req)
	    throws IOException
    {
	String set_cookie  = resp.getHeader("Set-Cookie");
	String set_cookie2 = resp.getHeader("Set-Cookie2");
	if (set_cookie == null  &&  set_cookie2 == null)
	    return;

	resp.deleteHeader("Set-Cookie");
	resp.deleteHeader("Set-Cookie2");

	if (set_cookie != null)
	    handleCookie(set_cookie, false, req, resp);
	if (set_cookie2 != null)
	    handleCookie(set_cookie2, true, req, resp);
    }


    /**
     * Invoked by the HTTPClient.
     */
    public int responsePhase2Handler(Response resp, Request req)
    {
	return RSP_CONTINUE;
    }


    /**
     * Invoked by the HTTPClient.
     */
    public void responsePhase3Handler(Response resp, RoRequest req)
    {
    }


    /**
     * Invoked by the HTTPClient.
     */
    public void trailerHandler(Response resp, RoRequest req)  throws IOException
    {
	String set_cookie = resp.getTrailer("Set-Cookie");
	String set_cookie2 = resp.getTrailer("Set-Cookie2");
	if (set_cookie == null  &&  set_cookie2 == null)
	    return;

	resp.deleteTrailer("Set-Cookie");
	resp.deleteTrailer("Set-Cookie2");

	if (set_cookie != null)
	    handleCookie(set_cookie, false, req, resp);
	if (set_cookie2 != null)
	    handleCookie(set_cookie2, true, req, resp);
    }


	@SuppressWarnings("unchecked")
    private void handleCookie(String set_cookie, boolean cookie2, RoRequest req,
			      Response resp)
	    throws ProtocolException
    {
	Cookie[] cookies;
	if (cookie2)
	    cookies = Cookie2.parse(set_cookie, req);
	else
	    cookies = Cookie.parse(set_cookie, req);

	if (Log.isEnabled(Log.COOKI))
	{
	    Log.write(Log.COOKI, "CookM: Received and parsed " + cookies.length +
				 " cookies:");
	    for (int idx=0; idx<cookies.length; idx++)
		Log.write(Log.COOKI, "CookM: Cookie " + idx + ": " + cookies[idx]);
	}

	Hashtable cookie_list =
	    Util.getList(cookie_cntxt_list, req.getConnection().getContext());
	synchronized (cookie_list)
	{
	    for (int idx=0; idx<cookies.length; idx++)
	    {
		Cookie cookie = (Cookie) cookie_list.get(cookies[idx]);
		if (cookie != null  &&  cookies[idx].hasExpired())
		{
		    Log.write(Log.COOKI, "CookM: cookie has expired and is " +
					 "being removed: " + cookie);
		    cookie_list.remove(cookie);		// expired, so remove
		}
		else if (!cookies[idx].hasExpired())	// new or replaced
		{
		    if (cookie_handler == null  ||
			cookie_handler.acceptCookie(cookies[idx], req, resp))
			cookie_list.put(cookies[idx], cookies[idx]);
		}
	    }
	}
    }


    /**
     * Discard all cookies for all contexts. Cookies stored in persistent
     * storage are not affected.
     */
    public static void discardAllCookies()
    {
	cookie_cntxt_list.clear();
    }


    /**
     * Discard all cookies for the given context. Cookies stored in persistent
     * storage are not affected.
     *
     * @param context the context Object
     */
    public static void discardAllCookies(Object context)
    {
	if (context != null)
	    cookie_cntxt_list.remove(context);
    }


    /**
     * List all stored cookies for all contexts.
     *
     * @return an array of all Cookies
     * @since V0.3-1
     */
    public static Cookie[] listAllCookies()
    {
	synchronized (cookie_cntxt_list)
	{
	    Cookie[] cookies = new Cookie[0];
	    int idx = 0;

	    Enumeration cntxt_list = cookie_cntxt_list.elements();
	    while (cntxt_list.hasMoreElements())
	    {
		Hashtable cntxt = (Hashtable) cntxt_list.nextElement();
		synchronized (cntxt)
		{
		    cookies = Util.resizeArray(cookies, idx+cntxt.size());
		    Enumeration cookie_list = cntxt.elements();
		    while (cookie_list.hasMoreElements())
			cookies[idx++] = (Cookie) cookie_list.nextElement();
		}
	    }

	    return cookies;
	}
    }


    /**
     * List all stored cookies for a given context.
     *
     * @param  context the context Object.
     * @return an array of Cookies
     * @since V0.3-1
     */
    public static Cookie[] listAllCookies(Object context)
    {
	Hashtable cookie_list = Util.getList(cookie_cntxt_list, context);

	synchronized (cookie_list)
	{
	    Cookie[] cookies = new Cookie[cookie_list.size()];
	    int idx = 0;

	    Enumeration enumera = cookie_list.elements();
	    while (enumera.hasMoreElements())
		cookies[idx++] = (Cookie) enumera.nextElement();

	    return cookies;
	}
    }


    /**
     * Add the specified cookie to the list of cookies in the default context.
     * If a compatible cookie (as defined by <var>Cookie.equals()</var>)
     * already exists in the list then it is replaced with the new cookie.
     *
     * @param cookie the Cookie to add
     * @since V0.3-1
     */
	@SuppressWarnings("unchecked")
    public static void addCookie(Cookie cookie)
    {
	Hashtable cookie_list =
	    Util.getList(cookie_cntxt_list, HTTPConnection.getDefaultContext());
	cookie_list.put(cookie, cookie);
    }


    /**
     * Add the specified cookie to the list of cookies for the specified
     * context. If a compatible cookie (as defined by
     * <var>Cookie.equals()</var>) already exists in the list then it is
     * replaced with the new cookie.
     *
     * @param cookie  the cookie to add
     * @param context the context Object.
     * @since V0.3-1
     */
	@SuppressWarnings("unchecked")
    public static void addCookie(Cookie cookie, Object context)
    {
	Hashtable cookie_list = Util.getList(cookie_cntxt_list, context);
	cookie_list.put(cookie, cookie);
    }


    /**
     * Remove the specified cookie from the list of cookies in the default
     * context. If the cookie is not found in the list then this method does
     * nothing.
     *
     * @param cookie the Cookie to remove
     * @since V0.3-1
     */
    public static void removeCookie(Cookie cookie)
    {
	Hashtable cookie_list =
	    Util.getList(cookie_cntxt_list, HTTPConnection.getDefaultContext());
	cookie_list.remove(cookie);
    }


    /**
     * Remove the specified cookie from the list of cookies for the specified
     * context. If the cookie is not found in the list then this method does
     * nothing.
     *
     * @param cookie  the cookie to remove
     * @param context the context Object
     * @since V0.3-1
     */
    public static void removeCookie(Cookie cookie, Object context)
    {
	Hashtable cookie_list = Util.getList(cookie_cntxt_list, context);
	cookie_list.remove(cookie);
    }


    /**
     * Sets a new cookie policy handler. This handler will be called for each
     * cookie that a server wishes to set and for each cookie that this
     * module wishes to send with a request. In either case the handler may
     * allow or reject the operation. If you wish to blindly accept and send
     * all cookies then just disable the handler with
     * <code>CookieModule.setCookiePolicyHandler(null);</code>.
     *
     * <P>At initialization time a default handler is installed. This
     * handler allows all cookies to be sent. For any cookie that a server
     * wishes to be set two lists are consulted. If the server matches any
     * host or domain in the reject list then the cookie is rejected; if
     * the server matches any host or domain in the accept list then the
     * cookie is accepted (in that order). If no host or domain match is
     * found in either of these two lists and user interaction is allowed
     * then a dialog box is poped up to ask the user whether to accept or
     * reject the cookie; if user interaction is not allowed the cookie is
     * accepted.
     *
     * <P>The accept and reject lists in the default handler are initialized
     * at startup from the two properties
     * <var>HTTPClient.cookies.hosts.accept</var> and
     * <var>HTTPClient.cookies.hosts.reject</var>. These properties must
     * contain a "|" separated list of host and domain names. All names
     * beginning with a "." are treated as domain names, all others as host
     * names. An empty string will match all hosts. The two lists are
     * further expanded if the user chooses one of the "Accept All from
     * Domain" or "Reject All from Domain" buttons in the dialog box.
     *
     * <P>Note: the default handler does not implement the rules concerning
     * unverifiable transactions (section 3.3.6, <A
     * HREF="http://www.ietf.org/rfc/rfc2965.txt">RFC-2965</A>). The reason
     * for this is simple: the default handler knows nothing about the
     * application using this client, and it therefore does not have enough
     * information to determine when a request is verifiable and when not. You
     * are therefore encouraged to provide your own handler which implements
     * section 3.3.6 (use the <code>CookiePolicyHandler.sendCookie</code>
     * method for this).
     *
     * @param handler the new policy handler
     * @return the previous policy handler
     */
    public static synchronized CookiePolicyHandler
			    setCookiePolicyHandler(CookiePolicyHandler handler)
    {
	CookiePolicyHandler old = cookie_handler;
	cookie_handler = handler;
	return old;
    }
}


/**
 * A simple cookie policy handler.
 */
class DefaultCookiePolicyHandler implements CookiePolicyHandler
{
    /** a list of all hosts and domains from which to silently accept cookies */
    private String[] accept_domains = new String[0];

    /** a list of all hosts and domains from which to silently reject cookies */
    private String[] reject_domains = new String[0];



    DefaultCookiePolicyHandler()
    {
	// have all cookies been accepted or rejected?
	String list;

	try
	    { list = System.getProperty("HTTPClient.cookies.hosts.accept"); }
	catch (Exception e)
	    { list = null; }
	String[] domains = Util.splitProperty(list);
	for (int idx=0; idx<domains.length; idx++)
	    addAcceptDomain(domains[idx].toLowerCase());

	try
	    { list = System.getProperty("HTTPClient.cookies.hosts.reject"); }
	catch (Exception e)
	    { list = null; }
	domains = Util.splitProperty(list);
	for (int idx=0; idx<domains.length; idx++)
	    addRejectDomain(domains[idx].toLowerCase());
    }


    /**
     * returns whether this cookie should be accepted. First checks the
     * stored lists of accept and reject domains, and if it is neither
     * accepted nor rejected by these then query the user via a popup.
     *
     * @param cookie   the cookie in question
     * @param req      the request
     * @param resp     the response
     * @return true if we accept this cookie.
     */
    public boolean acceptCookie(Cookie cookie, RoRequest req, RoResponse resp)
    {
	String server = req.getConnection().getHost();
	if (server.indexOf('.') == -1)  server += ".local";


	// Check lists. Reject takes priority over accept

	for (int idx=0; idx<reject_domains.length; idx++)
	{
	    if (reject_domains[idx].length() == 0  ||
		reject_domains[idx].charAt(0) == '.'  &&
		server.endsWith(reject_domains[idx])  ||
		reject_domains[idx].charAt(0) != '.'  &&
		server.equals(reject_domains[idx]))
		    return false;
	}

	for (int idx=0; idx<accept_domains.length; idx++)
	{
	    if (accept_domains[idx].length() == 0  ||
		accept_domains[idx].charAt(0) == '.'  &&
		server.endsWith(accept_domains[idx])  ||
		accept_domains[idx].charAt(0) != '.'  &&
		server.equals(accept_domains[idx]))
		    return true;
	}


	return true;
    }


    /**
     * This handler just allows all cookies to be sent which were accepted
     * (i.e. no further restrictions are placed on the sending of cookies).
     *
     * @return true
     */
    public boolean sendCookie(Cookie cookie, RoRequest req)
    {
	return true;
    }


    void addAcceptDomain(String domain)
    {
	if (domain.indexOf('.') == -1  &&  domain.length() > 0)
	    domain += ".local";

	for (int idx=0; idx<accept_domains.length; idx++)
	{
	    if (domain.endsWith(accept_domains[idx]))
		return;
	    if (accept_domains[idx].endsWith(domain))
	    {
		accept_domains[idx] = domain;
		return;
	    }
	}
	accept_domains =
		    Util.resizeArray(accept_domains, accept_domains.length+1);
	accept_domains[accept_domains.length-1] = domain;
    }

    void addRejectDomain(String domain)
    {
	if (domain.indexOf('.') == -1  &&  domain.length() > 0)
	    domain += ".local";

	for (int idx=0; idx<reject_domains.length; idx++)
	{
	    if (domain.endsWith(reject_domains[idx]))
		return;
	    if (reject_domains[idx].endsWith(domain))
	    {
		reject_domains[idx] = domain;
		return;
	    }
	}

	reject_domains =
		    Util.resizeArray(reject_domains, reject_domains.length+1);
	reject_domains[reject_domains.length-1] = domain;
    }
}

