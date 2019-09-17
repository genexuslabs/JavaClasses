/*
 * @(#)AuthorizationInfo.java				0.3-3 06/05/2001
 *
 *  This file is part of the HTTPClient package
 *  Copyright (C) 1996-2001 Ronald Tschal�r
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free
 *  Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *  MA 02111-1307, USA
 *
 *  For questions, suggestions, bug-reports, enhancement-requests etc.
 *  I may be contacted at:
 *
 *  ronald@innovation.ch
 *
 *  The HTTPClient's home page is located at:
 *
 *  http://www.innovation.ch/java/HTTPClient/ 
 *
 */

package HTTPClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ProtocolException;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;


/**
 * Holds the information for an authorization response.
 *
 * <P>There are 7 fields which make up this class: host, port, scheme,
 * realm, cookie, params, and extra_info. The host and port select which
 * server the info will be sent to. The realm is server specified string
 * which groups various URLs under a given server together and which is
 * used to select the correct info when a server issues an auth challenge;
 * for schemes which don't use a realm (such as "NTLM", "PEM", and
 * "Kerberos") the realm must be the empty string (""). The scheme is the
 * authorization scheme used (such as "Basic" or "Digest").
 *
 * <P>There are basically two formats used for the Authorization header,
 * the one used by the "Basic" scheme and derivatives, and the one used by
 * the "Digest" scheme and derivatives. The first form contains just the
 * the scheme and a "cookie":
 * 
 * <PRE>    Authorization: Basic aGVsbG86d29ybGQ=</PRE>
 * 
 * The second form contains the scheme followed by a number of parameters
 * in the form of name=value pairs:
 * 
 * <PRE>    Authorization: Digest username="hello", realm="test", nonce="42", ...</PRE>
 * 
 * The two fields "cookie" and "params" correspond to these two forms.
 * <A HREF="#toString()">toString()</A> is used by the AuthorizationModule
 * when generating the Authorization header and will format the info
 * accordingly. Note that "cookie" and "params" are mutually exclusive: if
 * the cookie field is non-null then toString() will generate the first
 * form; otherwise it will generate the second form.
 *
 * <P>In some schemes "extra" information needs to be kept which doesn't
 * appear directly in the Authorization header. An example of this are the
 * A1 and A2 strings in the Digest scheme. Since all elements in the params
 * field will appear in the Authorization header this field can't be used
 * for storing such info. This is what the extra_info field is for. It is
 * an arbitrary object which can be manipulated by the corresponding
 * setExtraInfo() and getExtraInfo() methods, but which will not be printed
 * by toString().
 *
 * <P>The addXXXAuthorization(), removeXXXAuthorization(), and
 * getAuthorization() methods manipulate and query an internal list of
 * AuthorizationInfo instances. There can be only one instance per host,
 * port, scheme, and realm combination (see <A HREF="#equals">equals()</A>).
 *
 * @version	0.3-3  06/05/2001
 * @author	Ronald Tschal�r
 * @since	V0.1
 */
@SuppressWarnings("unchecked")
public class AuthorizationInfo implements Cloneable
{
    // class fields

    /** Holds the list of lists of authorization info structures */
    private static Hashtable     CntxtList = new Hashtable();

    /** A pointer to the handler to be called when we need authorization info */
    private static AuthorizationHandler
				 AuthHandler = new DefaultAuthHandler();

    static
    {
	CntxtList.put(HTTPConnection.getDefaultContext(), new Hashtable());
    }
	


    // the instance oriented stuff

    /** the host (lowercase) */
    private String host;

    /** the port */
    private int port;

    /** the scheme. (e.g. "Basic")
     * Note: don't lowercase because some buggy servers use a case-sensitive
     * match */
    private String scheme;

    /** the realm */
    private String realm;

    /** the string used for the "Basic", "NTLM", and other authorization
     *  schemes which don't use parameters  */
    private String cookie;

    /** any parameters */
    private NVPair[] auth_params = new NVPair[0];

    /** additional info which won't be displayed in the toString() */
    private Object extra_info = null;

    /** a list of paths where this realm has been known to be required */
    private String[] paths = new String[0];


    // Constructors

    /**
     * Creates an new info structure for the specified host and port.
     *
     * @param host   the host
     * @param port   the port
     */
    AuthorizationInfo(String host, int port)
    {
	this.host = host.trim().toLowerCase();
	this.port = port;
    }


    /**
     * Creates a new info structure for the specified host and port with the
     * specified scheme, realm, params. The cookie is set to null.
     *
     * @param host   the host
     * @param port   the port
     * @param scheme the scheme
     * @param realm  the realm
     * @param params the parameters as an array of name/value pairs, or null
     * @param info   arbitrary extra info, or null
     */
    public AuthorizationInfo(String host, int port, String scheme,
			     String realm, NVPair params[], Object info)
    {
	this.scheme = scheme.trim();
	this.host   = host.trim().toLowerCase();
	this.port   = port;
	this.realm  = realm;
	this.cookie = null;

	if (params != null)
	    auth_params = Util.resizeArray(params, params.length);

	this.extra_info   = info;
    }


    /**
     * Creates a new info structure for the specified host and port with the
     * specified scheme, realm and cookie. The params is set to a zero-length
     * array, and the extra_info is set to null.
     *
     * @param host   the host
     * @param port   the port
     * @param scheme the scheme
     * @param realm  the realm
     * @param cookie for the "Basic" scheme this is the base64-encoded
     *               username/password; for the "NTLM" scheme this is the
     *               base64-encoded username/password message.
     */
    public AuthorizationInfo(String host, int port, String scheme,
			     String realm, String cookie)
    {
	this.scheme = scheme.trim();
	this.host   = host.trim().toLowerCase();
	this.port   = port;
	this.realm  = realm;
	if (cookie != null)
	    this.cookie = cookie.trim();
	else
	    this.cookie = null;
    }


    /**
     * Creates a new copy of the given AuthorizationInfo.
     *
     * @param templ the info to copy
     */
    AuthorizationInfo(AuthorizationInfo templ)
    {
	this.scheme = templ.scheme;
	this.host   = templ.host;
	this.port   = templ.port;
	this.realm  = templ.realm;
	this.cookie = templ.cookie;

	this.auth_params =
		Util.resizeArray(templ.auth_params, templ.auth_params.length);

	this.extra_info  = templ.extra_info;
    }


    // Class Methods

    /**
     * Set's the authorization handler. This handler is called whenever
     * the server requests authorization and no entry for the requested
     * scheme and realm can be found in the list. The handler must implement
     * the AuthorizationHandler interface.
     *
     * <P>If no handler is set then a {@link DefaultAuthHandler default
     * handler} is used. This handler currently only handles the "Basic" and
     * "Digest" schemes and brings up a popup which prompts for the username
     * and password.
     *
     * <P>The default handler can be disabled by setting the auth handler to
     * <var>null</var>.
     *
     * @param  handler the new authorization handler
     * @return the old authorization handler
     * @see    AuthorizationHandler
     */
    public static AuthorizationHandler
		    setAuthHandler(AuthorizationHandler handler)
    {
	AuthorizationHandler tmp = AuthHandler;
	AuthHandler = handler;

	return tmp;
    }


    /**
     * Get's the current authorization handler.
     *
     * @return the current authorization handler, or null if none is set.
     * @see    AuthorizationHandler
     */
    public static AuthorizationHandler getAuthHandler()
    {
	return AuthHandler;
    }


    /**
     * Searches for the authorization info using the given host, port,
     * scheme and realm. The context is the default context.
     *
     * @param  host         the host
     * @param  port         the port
     * @param  scheme       the scheme
     * @param  realm        the realm
     * @return a pointer to the authorization data or null if not found
     */
    public static AuthorizationInfo getAuthorization(
						String host, int port,
						String scheme, String realm)
    {
	return getAuthorization(host, port, scheme, realm,
				HTTPConnection.getDefaultContext());
    }


    /**
     * Searches for the authorization info in the given context using the
     * given host, port, scheme and realm.
     *
     * @param  host         the host
     * @param  port         the port
     * @param  scheme       the scheme
     * @param  realm        the realm
     * @param  context      the context this info is associated with
     * @return a pointer to the authorization data or null if not found
     */
    public static synchronized AuthorizationInfo getAuthorization(
						String host, int port,
						String scheme, String realm,
						Object context)
    {
	Hashtable AuthList = Util.getList(CntxtList, context);

	AuthorizationInfo auth_info =
	    new AuthorizationInfo(host, port, scheme, realm, (NVPair[]) null,
				  null);

	return (AuthorizationInfo) AuthList.get(auth_info);
    }


    /**
     * Queries the AuthHandler for authorization info. It also adds this
     * info to the list.
     *
     * @param  auth_info  any info needed by the AuthHandler; at a minimum the
     *                    host, scheme and realm should be set.
     * @param  req        the request which initiated this query
     * @param  resp       the full response
     * @return a structure containing the requested info, or null if either
     *	       no AuthHandler is set or the user canceled the request.
     * @exception AuthSchemeNotImplException if this is thrown by
     *                                            the AuthHandler.
     */
    static AuthorizationInfo queryAuthHandler(AuthorizationInfo auth_info,
					      RoRequest req, RoResponse resp, boolean proxy)
	throws AuthSchemeNotImplException, IOException
    {
	if (AuthHandler == null)
	    return null;

	AuthorizationInfo new_info =
		    AuthHandler.getAuthorization(auth_info, req, resp, proxy);
	if (new_info != null)
	{
	    if (req != null)
		addAuthorization((AuthorizationInfo) new_info.clone(),
				 req.getConnection().getContext());
	    else
		addAuthorization((AuthorizationInfo) new_info.clone(),
				 HTTPConnection.getDefaultContext());
	}

	return new_info;
    }


    /**
     * Searches for the authorization info using the host, port, scheme and
     * realm from the given info struct. If not found it queries the
     * AuthHandler (if set).
     *
     * @param  auth_info    the AuthorizationInfo
     * @param  req          the request which initiated this query
     * @param  resp         the full response
     * @param  query_auth_h if true, query the auth-handler if no info found.
     * @return a pointer to the authorization data or null if not found
     * @exception AuthSchemeNotImplException If thrown by the AuthHandler.
     */
    static synchronized AuthorizationInfo getAuthorization(
				    AuthorizationInfo auth_info, RoRequest req,
				    RoResponse resp, boolean query_auth_h, boolean proxy_auth)
	throws AuthSchemeNotImplException, IOException
    {
	Hashtable AuthList;
	if (req != null)
	    AuthList = Util.getList(CntxtList, req.getConnection().getContext());
	else
	    AuthList = Util.getList(CntxtList, HTTPConnection.getDefaultContext());

	AuthorizationInfo new_info =
	    (AuthorizationInfo) AuthList.get(auth_info);

	if (new_info == null  &&  query_auth_h)
	    new_info = queryAuthHandler(auth_info, req, resp, proxy_auth);

	return new_info;
    }


    /**
     * Searches for the authorization info given a host, port, scheme and
     * realm. Queries the AuthHandler if not found in list.
     *
     * @param  host         the host
     * @param  port         the port
     * @param  scheme       the scheme
     * @param  realm        the realm
     * @param  req          the request which initiated this query
     * @param  resp         the full response
     * @param  query_auth_h if true, query the auth-handler if no info found.
     * @return a pointer to the authorization data or null if not found
     * @exception AuthSchemeNotImplException If thrown by the AuthHandler.
     */
    static AuthorizationInfo getAuthorization(String host, int port,
					      String scheme, String realm,
					      RoRequest req, RoResponse resp,
					      boolean query_auth_h, boolean proxy)
	throws AuthSchemeNotImplException, IOException
    {
	return getAuthorization(new AuthorizationInfo(host, port, scheme,
						realm, (NVPair[]) null, null),
				req, resp, query_auth_h, proxy);
    }


    /**
     * Adds an authorization entry to the list using the default context.
     * If an entry for the specified scheme and realm already exists then
     * its cookie and params are replaced with the new data.
     *
     * @param auth_info the AuthorizationInfo to add
     */
    public static void addAuthorization(AuthorizationInfo auth_info)
    {
	addAuthorization(auth_info, HTTPConnection.getDefaultContext());
    }


    /**
     * Adds an authorization entry to the list. If an entry for the
     * specified scheme and realm already exists then its cookie and
     * params are replaced with the new data.
     *
     * @param auth_info the AuthorizationInfo to add
     * @param context   the context to associate this info with
     */
	@SuppressWarnings("unchecked")
    public static void addAuthorization(AuthorizationInfo auth_info,
					Object context)
    {
	Hashtable AuthList = Util.getList(CntxtList, context);

	// merge path list
	AuthorizationInfo old_info =
			    (AuthorizationInfo) AuthList.get(auth_info);
	if (old_info != null)
	{
	    int ol = old_info.paths.length,
		al = auth_info.paths.length;

	    if (al == 0)
		auth_info.paths = old_info.paths;
	    else
	    {
		auth_info.paths = Util.resizeArray(auth_info.paths, al+ol);
		System.arraycopy(old_info.paths, 0, auth_info.paths, al, ol);
	    }
	}

	AuthList.put(auth_info, auth_info);
    }


    /**
     * Adds an authorization entry to the list using the default context.
     * If an entry for the specified scheme and realm already exists then
     * its cookie and params are replaced with the new data.
     *
     * @param host   the host
     * @param port   the port
     * @param scheme the scheme
     * @param realm  the realm
     * @param cookie the cookie
     * @param params an array of name/value pairs of parameters
     * @param info   arbitrary extra auth info
     */
    public static void addAuthorization(String host, int port, String scheme,
					String realm, String cookie,
					NVPair params[], Object info)
    {
	addAuthorization(host, port, scheme, realm, cookie, params, info,
			 HTTPConnection.getDefaultContext());
    }


    /**
     * Adds an authorization entry to the list. If an entry for the
     * specified scheme and realm already exists then its cookie and
     * params are replaced with the new data.
     *
     * @param host    the host
     * @param port    the port
     * @param scheme  the scheme
     * @param realm   the realm
     * @param cookie  the cookie
     * @param params  an array of name/value pairs of parameters
     * @param info    arbitrary extra auth info
     * @param context the context to associate this info with
     */
    public static void addAuthorization(String host, int port, String scheme,
					String realm, String cookie,
					NVPair params[], Object info,
					Object context)
    {
	AuthorizationInfo auth =
	    new AuthorizationInfo(host, port, scheme, realm, cookie);
	if (params != null  &&  params.length > 0)
	    auth.auth_params = Util.resizeArray(params, params.length);
	auth.extra_info = info;

	addAuthorization(auth, context);
    }


    /**
     * Adds an authorization entry for the "Basic" authorization scheme to
     * the list using the default context. If an entry already exists for
     * the "Basic" scheme and the specified realm then it is overwritten.
     *
     * @param host   the host
     * @param port   the port
     * @param realm  the realm
     * @param user   the username
     * @param passwd the password
     */
    public static void addBasicAuthorization(String host, int port,
					     String realm, String user,
					     String passwd)
    {
	addAuthorization(host, port, "Basic", realm,
			 Codecs.base64Encode(user + ":" + passwd),
			 (NVPair[]) null, null);
    }


    /**
     * Adds an authorization entry for the "Basic" authorization scheme to
     * the list. If an entry already exists for the "Basic" scheme and the
     * specified realm then it is overwritten.
     *
     * @param host    the host
     * @param port    the port
     * @param realm   the realm
     * @param user    the username
     * @param passwd  the password
     * @param context the context to associate this info with
     */
    public static void addBasicAuthorization(String host, int port,
					     String realm, String user,
					     String passwd, Object context)
    {
	addAuthorization(host, port, "Basic", realm,
			 Codecs.base64Encode(user + ":" + passwd),
			 (NVPair[]) null, null, context);
    }


    /**
     * Adds an authorization entry for the "Digest" authorization scheme to
     * the list using the default context. If an entry already exists for the
     * "Digest" scheme and the specified realm then it is overwritten.
     *
     * @param host   the host
     * @param port   the port
     * @param realm  the realm
     * @param user   the username
     * @param passwd the password
     */
    public static void addDigestAuthorization(String host, int port,
					      String realm, String user,
					      String passwd)
    {
	addDigestAuthorization(host, port, realm, user, passwd,
			       HTTPConnection.getDefaultContext());
    }


    /**
     * Adds an authorization entry for the "Digest" authorization scheme to
     * the list. If an entry already exists for the "Digest" scheme and the
     * specified realm then it is overwritten.
     *
     * @param host    the host
     * @param port    the port
     * @param realm   the realm
     * @param user    the username
     * @param passwd  the password
     * @param context the context to associate this info with
     */
    public static void addDigestAuthorization(String host, int port,
					      String realm, String user,
					      String passwd, Object context)
    {
	AuthorizationInfo prev =
			getAuthorization(host, port, "Digest", realm, context);
	NVPair[] params;

	if (prev == null)
	{
	    params = new NVPair[4];
	    params[0] = new NVPair("username", user);
	    params[1] = new NVPair("uri", "");
	    params[2] = new NVPair("nonce", "");
	    params[3] = new NVPair("response", "");
	}
	else
	{
	    params = prev.getParams();
	    for (int idx=0; idx<params.length; idx++)
	    {
		if (params[idx].getName().equalsIgnoreCase("username"))
		{
		    params[idx] = new NVPair("username", user);
		    break;
		}
	    }
	}

	String[] extra = { MD5.hexDigest(user + ":" + realm + ":" + passwd),
			   null, null };

	addAuthorization(host, port, "Digest", realm, null, params, extra,
			 context);
    }


    /**
     * Adds an authorization entry for the "NTLM" authorization scheme to
     * the list using the default context. If an entry already exists for
     * the "NTLM" scheme and the specified realm then it is overwritten.
     *
     * @param host    the host
     * @param port    the port
     * @param realm   the realm
     * @param user    the username
     * @param passwd  the password
     */
    public static void addNTLMAuthorization(String host, int port,
					    String realm, String user,
					    String passwd)
    {
	addNTLMAuthorization(host, port, realm, user, passwd,
			     HTTPConnection.getDefaultContext());
    }


    /**
     * Adds an authorization entry for the "NTLM" authorization scheme to
     * the list. If an entry already exists for the "NTLM" scheme and the
     * specified realm then it is overwritten.
     *
     * @param host    the host
     * @param port    the port
     * @param realm   the realm
     * @param user    the username
     * @param passwd  the password
     * @param context the context to associate this info with
     */
    public static void addNTLMAuthorization(String host, int port,
					    String realm, String user,
					    String passwd, Object context)
    {
	// hash the password
	byte[] lm_hpw = DefaultAuthHandler.calc_lm_hpw(passwd);
	byte[] nt_hpw = DefaultAuthHandler.calc_ntcr_hpw(passwd);

	// get the local host name
	String lhost = null;
	try
	    { lhost = System.getProperty("HTTPClient.defAuthHandler.NTLM.host"); }
	catch (SecurityException se)
	    { }
	if (lhost == null)
	    try
		{ lhost = InetAddress.getLocalHost().getHostName(); }
	    catch (Exception e)
		{ }
	if (lhost == null)
	    lhost = "localhost";	// ???

	int dot = lhost.indexOf('.');
	if (dot != -1)
	    lhost = lhost.substring(0, dot);

	// get user and domain name
	String domain = null;
	int slash;
	if ((slash = user.indexOf('\\')) != -1)
	    domain = user.substring(0, slash);
	else
	{
	    try
	    {
		domain =
		    System.getProperty("HTTPClient.defAuthHandler.NTLM.domain");
	    }
	    catch (SecurityException se)
		{ }
	    if (domain == null)
		domain = lhost;	// ???
	}

	user = user.substring(slash+1);

	// store info in extra_info field
	Object[] info = { user, lhost.toUpperCase().trim(),
			  domain.toUpperCase().trim(), lm_hpw, nt_hpw };

	addAuthorization(host, port, "NTLM", realm, null, null, info, context);
    }


    /**
     * Removes an authorization entry from the list using the default context.
     * If no entry for the specified host, port, scheme and realm exists then
     * this does nothing.
     *
     * @param auth_info the AuthorizationInfo to remove
     */
    public static void removeAuthorization(AuthorizationInfo auth_info)
    {
	removeAuthorization(auth_info, HTTPConnection.getDefaultContext());
    }


    /**
     * Removes an authorization entry from the list. If no entry for the
     * specified host, port, scheme and realm exists then this does nothing.
     *
     * @param auth_info the AuthorizationInfo to remove
     * @param context   the context this info is associated with
     */
    public static void removeAuthorization(AuthorizationInfo auth_info,
					   Object context)
    {
	Hashtable AuthList = Util.getList(CntxtList, context);
	AuthList.remove(auth_info);
    }


    /**
     * Removes an authorization entry from the list using the default context.
     * If no entry for the specified host, port, scheme and realm exists then
     * this does nothing.
     *
     * @param host   the host
     * @param port   the port
     * @param scheme the scheme
     * @param realm  the realm
     */
    public static void removeAuthorization(String host, int port, String scheme,
					   String realm)
    {
	removeAuthorization(
	    new AuthorizationInfo(host, port, scheme, realm, (NVPair[]) null,
				  null));
    }


    /**
     * Removes an authorization entry from the list. If no entry for the
     * specified host, port, scheme and realm exists then this does nothing.
     *
     * @param host    the host
     * @param port    the port
     * @param scheme  the scheme
     * @param realm   the realm
     * @param context the context this info is associated with
     */
    public static void removeAuthorization(String host, int port, String scheme,
					   String realm, Object context)
    {
	removeAuthorization(
	    new AuthorizationInfo(host, port, scheme, realm, (NVPair[]) null,
				  null), context);
    }


    /**
     * Tries to find the candidate in the current list of auth info for the
     * given request. The paths associated with each auth info are examined,
     * and the one with either the nearest direct parent or child is chosen.
     * This is used for preemptively sending auth info.
     *
     * @param  req  the Request
     * @return an AuthorizationInfo containing the info for the best match,
     *         or null if none found.
     */
    static AuthorizationInfo findBest(RoRequest req)
    {
	String path = Util.getPath(req.getRequestURI());
	String host = req.getConnection().getHost();
	int    port = req.getConnection().getPort();


	// First search for an exact match

	Hashtable AuthList =
		    Util.getList(CntxtList, req.getConnection().getContext());
	Enumeration list = AuthList.elements();
	while (list.hasMoreElements())
	{
	    AuthorizationInfo info = (AuthorizationInfo) list.nextElement();

	    if (!info.host.equals(host)  ||  info.port != port)
		continue;

	    /*String[] paths = info.paths;
	    for (int idx=0; idx<paths.length; idx++)
	    {
		if (path.equals(paths[idx]))
		    return info;
	    } */
		//No busco en el path porque el mismo siempre es vacio y sino nunca manda la autorizacion.
		return info;
	}


	// Now find the closest parent or child

	AuthorizationInfo best = null;
	String base = path.substring(0, path.lastIndexOf('/')+1);
	int    min  = Integer.MAX_VALUE;

	list = AuthList.elements();
	while (list.hasMoreElements())
	{
	    AuthorizationInfo info = (AuthorizationInfo) list.nextElement();

	    if (!info.host.equals(host)  ||  info.port != port)
		continue;

	    String[] paths = info.paths;
	    for (int idx=0; idx<paths.length; idx++)
	    {
		// strip the last path segment, leaving a trailing "/"
		String ibase =
			paths[idx].substring(0, paths[idx].lastIndexOf('/')+1);

		if (base.equals(ibase))
		    return info;

		if (base.startsWith(ibase))		// found a parent
		{
		    int num_seg = 0, pos = ibase.length()-1;
		    while ((pos = base.indexOf('/', pos+1)) != -1)  num_seg++;

		    if (num_seg < min)
		    {
			min  = num_seg;
			best = info;
		    }
		}
		else if (ibase.startsWith(base))	// found a child
		{
		    int num_seg = 0, pos = base.length();
		    while ((pos = ibase.indexOf('/', pos+1)) != -1)  num_seg++;

		    if (num_seg < min)
		    {
			min  = num_seg;
			best = info;
		    }
		}
	    }
	}

	return best;
    }


    /**
     * Adds the path from the given resource to our path list. The path
     * list is used for deciding when to preemptively send auth info.
     *
     * @param resource the resource from which to extract the path
     */
    public synchronized void addPath(String resource)
    {
	String path = Util.getPath(resource);

	// First check that we don't already have this one
	for (int idx=0; idx<paths.length; idx++)
	    if (paths[idx].equals(path)) return;

	// Ok, add it
	paths = Util.resizeArray(paths, paths.length+1);
	paths[paths.length-1] = path;
    }


    /**
     * Parses the authentication challenge(s) into an array of new info
     * structures for the specified host and port.
     *
     * @param challenge a string containing authentication info. This must
     *                  have the same format as value part of a
     *                  WWW-authenticate response header field, and may
     *                  contain multiple authentication challenges.
     * @param req       the original request.
     * @exception ProtocolException if any error during the parsing occurs.
     */
    static AuthorizationInfo[] parseAuthString(String challenge, RoRequest req,
					       RoResponse resp)
	    throws ProtocolException
    {
	int    beg = 0,
	       end = 0;
	char[] buf = challenge.toCharArray();
	int    len = buf.length;
	int[]  pos_ref = new int[2];

	AuthorizationInfo auth_arr[] = new AuthorizationInfo[0],
			  curr;
	while (Character.isWhitespace(buf[len-1]))  len--;

	while (true)			// get all challenges
	{
	    // get scheme
	    beg = Util.skipSpace(buf, beg);
	    if (beg == len)  break;

	    end = Util.findSpace(buf, beg+1);

	    int sts;
	    try
		{ sts = resp.getStatusCode(); }
	    catch (IOException ioe)
		{ throw new ProtocolException(ioe.toString()); }
	    if (sts == 401)
		curr = new AuthorizationInfo(req.getConnection().getHost(),
					     req.getConnection().getPort());
	    else
		curr = new AuthorizationInfo(req.getConnection().getProxyHost(),
					     req.getConnection().getProxyPort());

		if(Log.isEnabled(Log.EXTENDED_INFO))
		{
			Log.write(Log.EXTENDED_INFO, "ExtInfo: [Beg=" + beg + ", End=" + end + ", BufEnd=" + buf[end-1] + "]");
		}
	    /* Hack for schemes like NTLM which don't have any params or cookie.
	     * Mickeysoft, hello? What were you morons thinking here? I suppose
	     * you weren't, as usual, huh?
	     */
	    if (buf[end-1] == ',')
	    {
			if(end > beg)curr.scheme = challenge.substring(beg, end-1);
			else curr.scheme = challenge;
			// @gusbro
			// Algunos servidores IIS (Win2000) fallan aqui porque ponen el scheme en Negotiate y los par�metros en NTLM
			// asi que lo chequeamos aca
			try
			{
				if(curr.scheme.equalsIgnoreCase("Negotiate"))
				{
					beg = end;
					end = Util.findSpace(buf, beg + 1);
					curr.scheme = challenge.substring(beg, end).trim();
					if (buf[end-1] == ',')
						curr.scheme = curr.scheme.substring(0, curr.scheme.length() - 1);
				}
			}catch(Exception e)
			{
				if(Log.isEnabled(Log.EXTENDED_INFO))
				{
					Log.write(Log.EXTENDED_INFO, "ExtInfo: Exception ignored[1]: ", e);
					end = beg;
				}
			}
			// @gusbro\
			beg = end;
		}
	    else
	    {
			if(end >= beg)curr.scheme = challenge.substring(beg, end);
			else curr.scheme = challenge;
			// @gusbro
			// Algunos servidores IIS (Win2000) fallan aqui porque ponen el scheme en Negotiate y los par�metros en NTLM
			// asi que lo chequeamos aca
			try
			{
				if(curr.scheme.equalsIgnoreCase("Negotiate"))
				{
					beg = end;
					end = Util.findSpace(buf, beg + 1);
					if(end > challenge.length())
					{ // @Hack: Si caigo aca tipicamente es porque el AuthString tiene SOLO Negotiate, 
					  // as� que asumo que es NTLM	
						Log.write(Log.EXTENDED_INFO, "ExtInfo: No further challenge information. Assuming NTLM authentication");
						curr.scheme = "NTLM";
						end = beg; // Aparte en este caso dejo Begin = end
					}
					else
					{
						curr.scheme = challenge.substring(beg, end).trim();
						if (buf[end-1] == ',')
							curr.scheme = curr.scheme.substring(0, end-1);
					}
				}
			}catch(Exception e)
			{ // @Hack: Si caigo aca tipicamente es porque el AuthString tiene SOLO Negotiate, 
			  // as� que asumo que es NTLM	
				if(Log.isEnabled(Log.EXTENDED_INFO))
				{
					Log.write(Log.EXTENDED_INFO, "ExtInfo: Exception ignored[2]: ", e);
					Log.write(Log.EXTENDED_INFO, "ExtInfo: Assuming NTLM authentication ");
					curr.scheme = "NTLM";
					end = beg; // Aparte en este caso dejo Begin = end
				}
			}
			// @gusbro\

			pos_ref[0] = beg; pos_ref[1] = end;
			Vector params = parseParams(challenge, buf, pos_ref, len, curr);
			beg = pos_ref[0]; end = pos_ref[1];

			if (!params.isEmpty())
			{
				curr.auth_params = new NVPair[params.size()];
				params.copyInto(curr.auth_params);
			}
		}

	    if (curr.realm == null)
		/* Can't do this if we're supposed to allow for broken schemes
		 * such as NTLM, Kerberos, and PEM.
		 *
		throw new ProtocolException("Bad Authentication header "
		    + "format: " + challenge + "\nNo realm value found");
		 */
		curr.realm = "";

	    auth_arr = Util.resizeArray(auth_arr, auth_arr.length+1);
	    auth_arr[auth_arr.length-1] = curr;

		if(Log.isEnabled(Log.EXTENDED_INFO))
		{
			Log.write(Log.EXTENDED_INFO, "ExtInfo: Scheme Processed: " + curr.scheme  + " [Realm:" + curr.realm + "]");
		}
	}

	return auth_arr;
    }

    private static final Vector parseParams(String challenge, char[] buf,
					    int[] pos_ref, int len,
					    AuthorizationInfo curr)
	    throws ProtocolException
    {
	int beg = pos_ref[0];
	int end = pos_ref[1];

	// get auth-parameters
	boolean first = true;
	Vector<NVPair> params = new Vector<>();
	while (true)
	{
	    beg = Util.skipSpace(buf, end);
	    if (beg == len)  break;

	    if (!first)				// expect ","
	    {
		if (buf[beg] != ',')
		    throw new ProtocolException("Bad Authentication header "
						+ "format: '" + challenge +
						"'\nExpected \",\" at position "+
						beg);

		beg = Util.skipSpace(buf, beg+1);	// find param name
		if (beg == len)  break;
		if (buf[beg] == ',')	// skip empty params
		{
		    end = beg;
		    continue;
		}
	    }

	    int pstart = beg;

	    // extract name
	    end = beg + 1;
	    while (end < len  &&  !Character.isWhitespace(buf[end]) &&
		   buf[end] != '='  &&  buf[end] != ',')
		end++;

	    // hack to deal with schemes which use cookies in challenge
	    if (first  &&
		(end == len   ||  buf[end] == '='  &&
		(end+1 == len  ||  (buf[end+1] == '='  &&  end+2 == len))))
	    {
		curr.cookie = challenge.substring(beg, len);
		beg = len;
		break;
	    }

	    String param_name = challenge.substring(beg, end),
		   param_value;

	    beg = Util.skipSpace(buf, end);	// find "=" or ","

	    if (beg < len  &&  buf[beg] != '='  &&  buf[beg] != ','  ||
		/* This deals with the M$ crap */
		!first  &&  (beg == len   ||  buf[beg] == ','))
	    {
		// It's not a param, but another challenge
		beg = pstart;
		break;
	    }


	    if (beg < len  &&  buf[beg] == '=')		// we have a value
	    {
		beg = Util.skipSpace(buf, beg+1);
		if (beg == len)
		    throw new ProtocolException("Bad Authentication header "
						+ "format: " + challenge +
						"\nUnexpected EOL after token" +
						" at position " + (end-1));
		if (buf[beg] != '"')	// it's a token
		{
		    end = Util.skipToken(buf, beg);
		    if (end == beg)
			throw new ProtocolException("Bad Authentication header "
			    + "format: " + challenge + "\nToken expected at " +
			    "position " + beg);
		    param_value = challenge.substring(beg, end);
		}
		else			// it's a quoted-string
		{
		    end = beg++;
		    do
			end = challenge.indexOf('"', end+1);
		    while (end != -1  &&  challenge.charAt(end-1) == '\\');
		    if (end == -1)
			throw new ProtocolException("Bad Authentication header "
			    + "format: " + challenge + "\nClosing <\"> for "
			    + "quoted-string starting at position " + beg
			    + " not found");
		    param_value =
			Util.dequoteString(challenge.substring(beg, end));
		    end++;
		}
	    }
	    else				// this is not strictly allowed
		param_value = null;

	    if (param_name.equalsIgnoreCase("realm"))
		curr.realm = param_value;
	    else
		params.addElement(new NVPair(param_name, param_value));

	    first = false;
	}

	pos_ref[0] = beg;
	pos_ref[1] = end;
	return params;
    }


    // Instance Methods

    /**
     * Get the host.
     *
     * @return a string containing the host name.
     */
    public final String getHost()
    {
	return host;
    }


    /**
     * Get the port.
     *
     * @return an int containing the port number.
     */
    public final int getPort()
    {
	return port;
    }


    /**
     * Get the scheme.
     *
     * @return a string containing the scheme.
     */
    public final String getScheme()
    {
	return scheme;
    }


    /**
     * Get the realm.
     *
     * @return a string containing the realm.
     */
    public final String getRealm()
    {
	return realm;
    }


    /**
     * Get the cookie
     *
     * @return the cookie String
     * @since V0.3-1
     */
    public final String getCookie()
    {
	return cookie;
    }


    /**
     * Set the cookie
     *
     * @param cookie the new cookie
     * @since V0.3-1
     */
    public final void setCookie(String cookie)
    {
	this.cookie = cookie;
    }


    /**
     * Get the authentication parameters.
     *
     * @return an array of name/value pairs.
     */
    public final NVPair[] getParams()
    {
	return Util.resizeArray(auth_params, auth_params.length);
    }


    /**
     * Set the authentication parameters.
     *
     * @param an array of name/value pairs.
     */
    public final void setParams(NVPair[] params)
    {
	if (params != null)
	    auth_params = Util.resizeArray(params, params.length);
	else
	    auth_params = new NVPair[0];
    }


    /**
     * Get the extra info.
     *
     * @return the extra_info object
     */
    public final Object getExtraInfo()
    {
	return extra_info;
    }


    /**
     * Set the extra info.
     *
     * @param info the extra info
     */
    public final void setExtraInfo(Object info)
    {
	extra_info = info;
    }


    /**
     * Constructs a string containing the authorization info. The format
     * is that of the http Authorization header.
     *
     * @return a String containing all info.
     */
    public String toString()
    {
	StringBuffer field = new StringBuffer(100);

	field.append(scheme);
	field.append(" ");

	if (cookie != null)
	{
	    field.append(cookie);
	}
	else
	{
	    if (realm.length() > 0)
	    {
		field.append("realm=\"");
		field.append(Util.quoteString(realm, "\\\""));
		field.append('"');
	    }

	    for (int idx=0; idx<auth_params.length; idx++)
	    {
			if (idx>0 || realm.length() > 0)
				field.append(',');
		field.append(auth_params[idx].getName());
		if (auth_params[idx].getValue() != null)
		{
		    field.append("=\"");
		    field.append(
			Util.quoteString(auth_params[idx].getValue(), "\\\""));
		    field.append('"');
		}
	    }
	}

	return field.toString();
    }


    /**
     * Produces a hash code based on host, scheme and realm. Port is not
     * included for simplicity (and because it probably won't make much
     * difference). Used in the AuthorizationInfo.AuthList hash table.
     *
     * @return the hash code
     */
    public int hashCode()
    {
	return (host+scheme.toLowerCase()+realm).hashCode();
    }

    /**
     * Two AuthorizationInfos are considered equal if their host, port,
     * scheme and realm match. Used in the AuthorizationInfo.AuthList hash
     * table.
     *
     * @param obj another AuthorizationInfo against which this one is
     *            to be compared.
     * @return true if they match in the above mentioned fields; false
     *              otherwise.
     */
    public boolean equals(Object obj)
    {
	if ((obj != null)  &&  (obj instanceof AuthorizationInfo))
	{
	    AuthorizationInfo auth = (AuthorizationInfo) obj;
	    if (host.equals(auth.host)  &&
		(port == auth.port)  &&
		scheme.equalsIgnoreCase(auth.scheme)  &&
		realm.equals(auth.realm))
		    return true;
	}
	return false;
    }


    /**
     * @return a clone of this AuthorizationInfo using a deep copy
     */
    public Object clone()
    {
	AuthorizationInfo ai;
	try
	{
	    ai = (AuthorizationInfo) super.clone();
	    ai.auth_params = Util.resizeArray(auth_params, auth_params.length);
	    try
	    {
		// ai.extra_info  = extra_info.clone();
		ai.extra_info = extra_info.getClass().getMethod("clone").
				invoke(extra_info);
	    }
	    catch (Throwable t)
		{ }
	    ai.paths = new String[paths.length];
	    System.arraycopy(paths, 0, ai.paths, 0, paths.length);
	}
	catch (CloneNotSupportedException cnse)
	    { throw new InternalError(cnse.toString()); /* shouldn't happen */ }

	return ai;
    }
}
