/*
 * @(#)AuthorizationModule.java				0.3-3 06/05/2001
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
import java.net.ProtocolException;
import java.util.Hashtable;
import java.util.Vector;

import com.genexus.common.interfaces.SpecificImplementation;


/**
 * This module handles authentication requests. Authentication info is
 * preemptively sent if any suitable candidate info is available. If a
 * request returns with an appropriate status (401 or 407) then the
 * necessary info is sought from the AuthenticationInfo class.
 *
 * @version	0.3-3  06/05/2001
 * @author	Ronald Tschal�r
 */
class AuthorizationModule implements HTTPClientModule
{
    /** This holds the current Proxy-Authorization-Info for each
        HTTPConnection */
    private static Hashtable proxy_cntxt_list = new Hashtable();

    /** a list of deferred authorization retries (used with
	Response.retryRequest()) */
    private static Hashtable<HttpOutputStream, AuthorizationModule> deferred_auth_list = new Hashtable<>();

    /** counters for challenge and auth-info lists */
    private int	auth_lst_idx,
		prxy_lst_idx,
		auth_scm_idx,
		prxy_scm_idx;

    /** the last auth info sent, if any */
    private AuthorizationInfo auth_sent;
    private AuthorizationInfo prxy_sent;

    /** is the info in auth_sent a preemtive guess or the result of a 4xx */
    private boolean auth_from_4xx;
    private boolean prxy_from_4xx;

    /** guard against bugs on both our side and the server side */
    private int num_tries;

    /** keep track of removed close tokens (for NTLM) */
    private boolean close_tok_remd = false;

    /** used for deferred authoriation retries */
    private Request  saved_req;
    private Response saved_resp;


    // Constructors

    /**
     * Initialize counters for challenge and auth-info lists.
     */
    AuthorizationModule()
    {
	auth_lst_idx = 0;
	prxy_lst_idx = 0;
	auth_scm_idx = 0;
	prxy_scm_idx = 0;

	auth_sent = null;
	prxy_sent = null;

	auth_from_4xx = false;
	prxy_from_4xx = false;

	num_tries  = 0;
	saved_req  = null;
	saved_resp = null;
    }


    // Methods

    /**
     * Invoked by the HTTPClient.
     */
    public int requestHandler(Request req, Response[] resp)
		throws IOException, AuthSchemeNotImplException
    {
	HTTPConnection con = req.getConnection();
	AuthorizationHandler auth_handler = AuthorizationInfo.getAuthHandler();
	AuthorizationInfo guess;
	NVPair[] hdrs = req.getHeaders();
	int rem_idx = -1;

    // check for retries

	HttpOutputStream out = req.getStream();
	if (out != null  &&  deferred_auth_list.get(out) != null)
	{
	    copyFrom(deferred_auth_list.remove(out));
	    req.copyFrom(saved_req);

	    Log.write(Log.AUTH, "AuthM: Handling deferred auth challenge");

	    handle_auth_challenge(req, saved_resp);

	    if (auth_sent != null)
		Log.write(Log.AUTH, "AuthM: Sending request with " +
				    "Authorization '" + auth_sent + "'");
	    else
		Log.write(Log.AUTH, "AuthM: Sending request with " +
				    "Proxy-Authorization '" + prxy_sent +
				    "'");

	    return REQ_RESTART;
	}


	// Preemptively send proxy authorization info

	Proxy: if (con.getProxyHost() != null  &&  !prxy_from_4xx)
	{
	    // first remove any Proxy-Auth header that still may be around

	    for (int idx=0; idx<hdrs.length; idx++)
	    {
		if (hdrs[idx].getName().equalsIgnoreCase("Proxy-Authorization"))
		{
		    rem_idx = idx;
		    break;
		}
	    }
	    Hashtable proxy_auth_list = Util.getList(proxy_cntxt_list,
						     con.getContext());
	    guess = (AuthorizationInfo) proxy_auth_list.get(
				    con.getProxyHost()+":"+con.getProxyPort());
	    if (guess == null)  break Proxy;

	    if (auth_handler != null)
	    {
		try
		    { guess = auth_handler.fixupAuthInfo(guess, req, null, null); }
		catch (AuthSchemeNotImplException asnie)
		    { break Proxy; }
		if (guess == null) break Proxy;
	    }

	    if (rem_idx == -1)	// add proxy-auth header
	    {
		rem_idx = hdrs.length;
		hdrs = Util.resizeArray(hdrs, rem_idx+1);
		req.setHeaders(hdrs);
	    }

	    hdrs[rem_idx] = new NVPair("Proxy-Authorization", guess.toString());
	    rem_idx = -1;

	    prxy_sent     = guess;
	    prxy_from_4xx = false;

	    Log.write(Log.AUTH, "AuthM: Preemptively sending " +
			        "Proxy-Authorization '" + guess + "'");
	}
	if (rem_idx >= 0)
	{
	    System.arraycopy(hdrs, rem_idx+1, hdrs, rem_idx, hdrs.length-rem_idx-1);
	    hdrs = Util.resizeArray(hdrs, hdrs.length-1);
	    req.setHeaders(hdrs);
	}


	// Preemptively send authorization info

	rem_idx = -1;
	Auth: if (!auth_from_4xx)
	{
	    // first remove any Auth header that still may be around

	    /*for (int idx=0; idx<hdrs.length; idx++)
	    {
		if (hdrs[idx].getName().equalsIgnoreCase("Authorization"))
		{
		    rem_idx = idx;
		    break;
		}
	    }*/

	    // now try and guess whether we need to send auth info

	    guess = AuthorizationInfo.findBest(req);
	    if (guess == null)  break Auth;

	    if (auth_handler != null)
	    {
		try
		    { guess = auth_handler.fixupAuthInfo(guess, req, null, null); }
		catch (AuthSchemeNotImplException asnie)
		    { break Auth; }
		if (guess == null) break Auth;
	    }

	    if (rem_idx == -1)	// add auth header
	    {
		rem_idx = hdrs.length;
		hdrs = Util.resizeArray(hdrs, rem_idx+1);
		req.setHeaders(hdrs);
	    }

	    hdrs[rem_idx] = new NVPair("Authorization", guess.toString());
	    rem_idx = -1;

	    auth_sent     = guess;
	    auth_from_4xx = false;

	    Log.write(Log.AUTH, "AuthM: Preemptively sending Authorization '"
				+ guess + "'");
	}
	if (rem_idx >= 0)
	{
	    System.arraycopy(hdrs, rem_idx+1, hdrs, rem_idx, hdrs.length-rem_idx-1);
	    hdrs = Util.resizeArray(hdrs, hdrs.length-1);
	    req.setHeaders(hdrs);
	}

	return REQ_CONTINUE;
    }


    /**
     * Invoked by the HTTPClient.
     */
    public void responsePhase1Handler(Response resp, RoRequest req)
		throws IOException
    {
	/* If auth info successful update path list. Note: if we
	 * preemptively sent auth info we don't actually know if
	 * it was necessary. Therefore we don't update the path
	 * list in this case; this prevents it from being
	 * contaminated. If the info was necessary, then the next
	 * time we access this resource we will again guess the
	 * same info and send it.
	 */
	if (resp.getStatusCode() != 401  &&  resp.getStatusCode() != 407)
	{
	    if (auth_sent != null  &&  auth_from_4xx)
	    {
		try
		{
		    AuthorizationInfo.getAuthorization(auth_sent, req, resp,
				    false, false).addPath(req.getRequestURI());
		}
		catch (AuthSchemeNotImplException asnie)
		    { /* shouldn't happen */ }
	    }

	    // reset guard if not an auth challenge
	    num_tries = 0;
	}

	auth_from_4xx = false;
	prxy_from_4xx = false;

	if (resp.getHeader("WWW-Authenticate") == null)
	{
	    auth_lst_idx = 0;
	    auth_scm_idx = 0;
	}

	if (resp.getHeader("Proxy-Authenticate") == null)
	{
	    prxy_lst_idx = 0;
	    prxy_scm_idx = 0;
	}
    }


    /**
     * Invoked by the HTTPClient.
     */
    public int responsePhase2Handler(Response resp, Request req)
		throws IOException, AuthSchemeNotImplException
    {
	// Let the AuthHandler handle any Authentication headers.

	AuthorizationHandler h = AuthorizationInfo.getAuthHandler();
	if (h != null)
	    h.handleAuthHeaders(resp, req, auth_sent, prxy_sent);


	// hack for NTLM
	if (close_tok_remd)
	{
	    add_close_token(req);
	    close_tok_remd = false;
	}

	// handle 401 and 407 response codes

	int sts  = resp.getStatusCode();
	switch(sts)
	{
	    case 401: // Unauthorized
	    case 407: // Proxy Authentication Required

		// guard against infinite retries due to bugs

		num_tries++;
		if (num_tries > 10)
		    throw new ProtocolException("Bug in authorization handling: server refused the given info 10 times");


		// defer handling if a stream was used

		if (req.getStream() != null)
		{
		    if (!HTTPConnection.deferStreamed)
		    {
			Log.write(Log.AUTH, "AuthM: status " + sts +
					    " not handled - request has " +
					    "an output stream");
			return RSP_CONTINUE;
		    }

		    saved_req  = (Request)  req.clone();
		    saved_resp = (Response) resp.clone();
		    deferred_auth_list.put(req.getStream(), this);

		    req.getStream().reset();
		    resp.setRetryRequest(true);

		    Log.write(Log.AUTH, "AuthM: Handling of status " +
					sts + " deferred because an " +
					"output stream was used");

		    return RSP_CONTINUE;
		}


		// handle the challenge

		Log.write(Log.AUTH, "AuthM: Handling status: " + sts + " " +
				    resp.getReasonLine());

		handle_auth_challenge(req, resp);


		// check for valid challenge

		if (auth_sent != null  ||  prxy_sent != null)
		{
		    try { resp.getInputStream().close(); }
		    catch (IOException ioe) { }

		    if (auth_sent != null)
			Log.write(Log.AUTH, "AuthM: Resending request " +
					    "with Authorization '" +
					    auth_sent + "'");
		    else
			Log.write(Log.AUTH, "AuthM: Resending request " +
					    "with Proxy-Authorization '" +
					    prxy_sent + "'");

		    return RSP_REQUEST;
		}


		if (req.getStream() != null)
		    Log.write(Log.AUTH, "AuthM: status " + sts + " not " +
				        "handled - request has an output " +
				        "stream");
		else
		    Log.write(Log.AUTH, "AuthM: No Auth Info found - " +
				        "status " + sts + " not handled");

		return RSP_CONTINUE;

	    default:

		return RSP_CONTINUE;
	}
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
	// Let the AuthHandler handle any Authentication headers.

	AuthorizationHandler h = AuthorizationInfo.getAuthHandler();
	if (h != null)
	    h.handleAuthTrailers(resp, req, auth_sent, prxy_sent);
    }


    /**
     *
     */
	@SuppressWarnings("unchecked")
    private void handle_auth_challenge(Request req, Response resp)
	    throws AuthSchemeNotImplException, IOException
    {
	// handle WWW-Authenticate

	int[] idx_arr = { auth_lst_idx,	// hack to pass by ref
			  auth_scm_idx};
	auth_sent = setAuthHeaders(resp.getHeader("WWW-Authenticate"),
				   req, resp, "Authorization", false, idx_arr,
				   auth_sent);
	if (auth_sent != null)
	{
	    auth_from_4xx = true;
	    auth_lst_idx = idx_arr[0];
	    auth_scm_idx = idx_arr[1];
	}
	else
	{
	    auth_lst_idx = 0;
	    auth_scm_idx = 0;
	}


	// handle Proxy-Authenticate

	idx_arr[0] = prxy_lst_idx;	// hack to pass by ref
	idx_arr[1] = prxy_scm_idx;
	prxy_sent = setAuthHeaders(resp.getHeader("Proxy-Authenticate"),
				   req, resp, "Proxy-Authorization", true,
				   idx_arr, prxy_sent);
	if (prxy_sent != null)
	{
	    prxy_from_4xx = true;
	    prxy_lst_idx = idx_arr[0];
	    prxy_scm_idx = idx_arr[1];
	}
	else
	{
	    prxy_lst_idx = 0;
	    prxy_scm_idx = 0;
	}

	if (prxy_sent != null)
	{
	    HTTPConnection con = req.getConnection();
	    Util.getList(proxy_cntxt_list, con.getContext())
		.put(con.getProxyHost()+":"+con.getProxyPort(),
		     prxy_sent);
	}

	// check for headers

	if (auth_sent == null  &&  prxy_sent == null  &&
	    resp.getHeader("WWW-Authenticate") == null  &&
	    resp.getHeader("Proxy-Authenticate") == null)
	{

		if (resp.getStatusCode() != 401)
			throw new ProtocolException("Missing Proxy-Authenticate header");
		if (SpecificImplementation.SendErrorOn401)
		{
			if (resp.getStatusCode() == 401)
				throw new ProtocolException("Missing WWW-Authenticate header");
		}
	}
    }


    /**
     * Handles authentication requests and sets the authorization headers.
     * It tries to retrieve the neccessary parameters from AuthorizationInfo,
     * and failing that calls the AuthHandler. Handles multiple authentication
     * headers.
     *
     * @param  auth_str the authentication header field returned by the server.
     * @param  req      the Request used
     * @param  resp     the full Response received
     * @param  header   the header name to use in the new headers array.
     * @param  idx_arr  an array of indicies holding the state of where we
     *                  are when handling multiple authorization headers.
     * @param  prev     the previous auth info sent, or null if none
     * @return the new credentials, or null if none found
     * @exception ProtocolException if <var>auth_str</var> is null.
     * @exception AuthSchemeNotImplException if thrown by the AuthHandler.
     * @exception IOException if thrown by the AuthHandler.
     */
    private AuthorizationInfo setAuthHeaders(String auth_str, Request req,
					     RoResponse resp, String header,
					     boolean proxy_auth, int[] idx_arr,
					     AuthorizationInfo prev)
	throws ProtocolException, AuthSchemeNotImplException, IOException
    {

		try
		{
			if(Log.isEnabled(Log.EXTENDED_INFO))
			{
				Log.write(Log.EXTENDED_INFO, "ExtInfo: AuthStr: " + auth_str);
				Log.write(Log.EXTENDED_INFO, "ExtInfo: Header: " + header);
				Log.write(Log.EXTENDED_INFO, "ExtInfo: ProxyAuth: " + proxy_auth);
			}
			if (auth_str == null)  return null;

			// get the list of challenges the server sent
			AuthorizationInfo[] challenges =
											AuthorizationInfo.parseAuthString(auth_str, req, resp);

			if (Log.isEnabled(Log.AUTH))
			{
				Log.write(Log.AUTH, "AuthM: parsed " + challenges.length +
									" challenges:");
				for (int idx=0; idx<challenges.length; idx++)
					Log.write(Log.AUTH, "AuthM: Challenge " + challenges[idx]);
			}

			if (challenges.length == 0)
				return null;


			/* some servers expect a 401 to invalidate sent credentials.
			* However, only do this for Basic scheme (because e.g. digest
			* "stale" handling will fail otherwise)
			*/
			if (prev != null  &&  prev.getScheme().equalsIgnoreCase("Basic"))
			{
				for (int idx=0; idx<challenges.length; idx++)
					if (prev.getRealm().equals(challenges[idx].getRealm())  &&
						prev.getScheme().equalsIgnoreCase(challenges[idx].getScheme()))
						AuthorizationInfo.removeAuthorization(prev,
													  req.getConnection().getContext());
			}

			AuthorizationInfo    credentials  = null;
			AuthorizationHandler auth_handler = AuthorizationInfo.getAuthHandler();

			// try next auth challenge in list
			while (credentials == null  &&  idx_arr[0] != -1  &&
				   idx_arr[0] < challenges.length)
			{
				credentials = AuthorizationInfo.getAuthorization(
																 challenges[idx_arr[0]], req, resp,
																 false, proxy_auth);
				if (auth_handler != null  &&  credentials != null)
					credentials = auth_handler.fixupAuthInfo(credentials, req,
															 challenges[idx_arr[0]], resp);
				if (++idx_arr[0] == challenges.length)
					idx_arr[0] = -1;
			}


			// if we don't have any credentials then prompt the user
			if (credentials == null)
			{
				for (int idx=0; idx<challenges.length; idx++)
				{
					if (idx_arr[1] >= challenges.length)
						idx_arr[1] = 0;

					try
					{
						credentials = AuthorizationInfo.queryAuthHandler(
																		 challenges[idx_arr[1]], req, resp, proxy_auth);
						break;
					}
					catch (AuthSchemeNotImplException asnie)
					{
						if (idx == challenges.length-1)
							throw asnie;
					}
					finally
					{ idx_arr[1]++; }
				}
			}

			if(Log.isEnabled(Log.EXTENDED_INFO))
			{
				if(credentials == null)
				{
					Log.write(Log.EXTENDED_INFO, "ExtInfo: No credentials");
				}
				else
				{
					Log.write(Log.EXTENDED_INFO, "ExtInfo: Credentials: Scheme=" + credentials.getScheme() + " - Realm=" + credentials.getRealm() + " Host:" + credentials.getHost() + " - Port: " + credentials.getPort());
				}
			}

			// if we still don't have any credentials then give up
			if (credentials == null)
				return null;

			// find auth info
			int auth_idx;
			NVPair[] hdrs = req.getHeaders();
			for (auth_idx=0; auth_idx<hdrs.length; auth_idx++)
			{
				if (hdrs[auth_idx].getName().equalsIgnoreCase(header))
					break;
			}

			// add credentials to headers
			if (auth_idx == hdrs.length)
			{
				hdrs = Util.resizeArray(hdrs, auth_idx+1);
				req.setHeaders(hdrs);
			}
			hdrs[auth_idx] = new NVPair(header, credentials.toString());

			// hack for NTLM
			if (credentials.getScheme().equalsIgnoreCase("NTLM")  &&
				credentials.getCookie().startsWith("TlRMTVNTUAAB"))
			{
				if (remove_close_token(req))
					close_tok_remd = true;
			}

			// done
			return credentials;
		}catch(ProtocolException e)
		{
			if(Log.isEnabled(Log.EXTENDED_INFO))
			{
				Log.write(Log.EXTENDED_INFO, "ExtInfo: Rethrowing exception ", e);
			}
			throw e;
		}catch(AuthSchemeNotImplException e)
		{
			if(Log.isEnabled(Log.EXTENDED_INFO))
			{
				Log.write(Log.EXTENDED_INFO, "ExtInfo: Rethrowing exception ", e);
			}
			throw e;
		}catch(IOException e)
		{
			if(Log.isEnabled(Log.EXTENDED_INFO))
			{
				Log.write(Log.EXTENDED_INFO, "ExtInfo: Rethrowing exception ", e);
			}
			throw e;
		}catch(Throwable e)
		{
			if(Log.isEnabled(Log.EXTENDED_INFO))
			{
				Log.write(Log.EXTENDED_INFO, "ExtInfo: Rethrowing unhandled exception ", e);
			}
			throw new IOException(e.toString());
		}
    }


    private void copyFrom(AuthorizationModule other)
    {
	this.auth_lst_idx  = other.auth_lst_idx;
	this.prxy_lst_idx  = other.prxy_lst_idx;
	this.auth_scm_idx  = other.auth_scm_idx;
	this.prxy_scm_idx  = other.prxy_scm_idx;

	this.auth_sent     = other.auth_sent;
	this.prxy_sent     = other.prxy_sent;

	this.auth_from_4xx = other.auth_from_4xx;
	this.prxy_from_4xx = other.prxy_from_4xx;

	this.num_tries     = other.num_tries;

	this.saved_req     = other.saved_req;
	this.saved_resp    = other.saved_resp;
    }
    private boolean remove_close_token(Request req)  throws ProtocolException
    {
	NVPair[] hdrs = req.getHeaders();
	int idx;

	for (idx=0; idx<hdrs.length; idx++)
	{
	    if (hdrs[idx].getName().equalsIgnoreCase("Connection"))
		break;
	}

	if (idx >= hdrs.length)  return false;

	Vector<HttpHeaderElement> pcon;
	try
	    { pcon = Util.parseHeader(hdrs[idx].getValue()); }
	catch (ParseException pe)
	    { throw new ProtocolException(pe.toString()); }

	HttpHeaderElement cls = new HttpHeaderElement("close");
	if (!pcon.removeElement(cls))  return false;
	while (pcon.removeElement(cls))
	    ;

	if (pcon.size() > 0)
	{
	    hdrs[idx] = new NVPair("Connection", Util.assembleHeader(pcon));
	}
	else
	{
	    NVPair[] nhdrs = new NVPair[hdrs.length-1];
	    System.arraycopy(hdrs, 0, nhdrs, 0, idx);
	    System.arraycopy(hdrs, idx+1, nhdrs, idx, nhdrs.length-idx);
	    req.setHeaders(nhdrs);
	}

	return true;
    }


    private void add_close_token(Request req)  throws ProtocolException
    {
	NVPair[] hdrs = req.getHeaders();
	int idx;

	for (idx=0; idx<hdrs.length; idx++)
	{
	    if (hdrs[idx].getName().equalsIgnoreCase("Connection"))
		break;
	}

	if (idx >= hdrs.length)
	{
	    hdrs = Util.resizeArray(hdrs, hdrs.length+1);
	    hdrs[idx] = new NVPair("Connection", "close");
	    req.setHeaders(hdrs);
	}
	else
	{
	    Vector<HttpHeaderElement> pcon;
	    try
		{ pcon = Util.parseHeader(hdrs[idx].getValue()); }
	    catch (ParseException pe)
		{ throw new ProtocolException(pe.toString()); }

	    pcon.addElement(new HttpHeaderElement("close"));
	    hdrs[idx] = new NVPair("Connection", Util.assembleHeader(pcon));
	}
    }

}
