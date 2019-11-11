package HTTPClient;

import java.net.InetAddress;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.util.Vector;
import java.util.StringTokenizer;

import java.awt.Frame;
import java.awt.Panel;
import java.awt.Label;
import java.awt.Button;
import java.awt.Dimension;
import java.awt.TextField;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

/**
 * This class is the default authorization handler. It currently handles the
 * authentication schemes "Basic", "Digest", and "SOCKS5" (used for the
 * SocksClient and not part of HTTP per se).
 *
 * <P>By default, when a username and password is required, this handler throws
 * up a message box requesting the desired info. However, applications can
 * {@link #setAuthorizationPrompter(HTTPClient.AuthorizationPrompter) set their
 * own authorization prompter} if desired.
 *
 * <P><strong>Note:</strong> all methods except for
 * <var>setAuthorizationPrompter</var> are meant to be invoked by the
 * AuthorizationModule only, i.e. should not be invoked by the application
 * (those methods are only public because implementing the
 * <var>AuthorizationHandler</var> interface requires them to be).
 *
 * @version	0.3-3  06/05/2001
 * @author	Ronald Tschal�r
 * @since	V0.2
 */
public class DefaultAuthHandler implements AuthorizationHandler, GlobalConstants
{
    private static final byte[] NUL = new byte[0];
    private static final byte[] zeros = new byte[24];

    private static final int DI_A1  = 0;
    private static final int DI_A1S = 1;
    private static final int DI_QOP = 2;

    private static byte[] digest_secret = null;
    private static boolean send_lm_auth = false;

    private static AuthorizationPrompter prompter    = null;
    private static boolean               prompterSet = false;

    private static HTTPClient.DESAlgorithm DES =
			new HTTPClient.DESAlgorithm(false);


    static
    {
	/*
	try
	{
	    send_lm_auth =
		Boolean.getBoolean("HTTPClient.defAuthHandler.NTLM.sendLMAuth");
	}
	catch (Exception e)
	    { }
	*/
	send_lm_auth = true;
    }


    /**
     * For Digest authentication we need to set the uri, response and
     * opaque parameters. For "Basic" and "SOCKS5" nothing is done.
     */
    public AuthorizationInfo fixupAuthInfo(AuthorizationInfo info,
					   RoRequest req,
					   AuthorizationInfo challenge,
					   RoResponse resp)
		    throws AuthSchemeNotImplException
    {
	// nothing to do for Basic and SOCKS5 schemes

	if (info.getScheme().equalsIgnoreCase("Basic")  ||
	    info.getScheme().equalsIgnoreCase("SOCKS5"))
	    return info;
	else if (!info.getScheme().equalsIgnoreCase("Digest")&&
		 !info.getScheme().equalsIgnoreCase("NTLM"))
	    throw new AuthSchemeNotImplException(info.getScheme());

	if (Log.isEnabled(Log.AUTH))
	    Log.write(Log.AUTH, "Auth:  fixing up Authorization for host " +
				info.getHost()+":"+info.getPort() +
				"; scheme: " + info.getScheme() +
				"; realm: " + info.getRealm());

	if (info.getScheme().equalsIgnoreCase("Digest"))
	    return digest_fixup(info, req, challenge, resp);
	else
	    return ntlm_fixup(info, req, challenge, resp);
    }


    /**
     * returns the requested authorization, or null if none was given.
     *
     * @param challenge the parsed challenge from the server.
     * @param req the request which solicited this response
     * @param resp the full response received
     * @return a structure containing the necessary authorization info,
     *         or null
     * @exception AuthSchemeNotImplException if the authentication scheme
     *             in the challenge cannot be handled.
     */
    public AuthorizationInfo getAuthorization(AuthorizationInfo challenge,
					      RoRequest req, RoResponse resp, boolean proxy)
		    throws AuthSchemeNotImplException, IOException
    {
	AuthorizationInfo cred;


	if (Log.isEnabled(Log.AUTH))
	    Log.write(Log.AUTH, "Auth:  Requesting Authorization for host " +
				challenge.getHost()+":"+challenge.getPort() +
				"; scheme: " + challenge.getScheme() +
				"; realm: " + challenge.getRealm());


	// we only handle Basic, Digest and SOCKS5 authentication

	if (!challenge.getScheme().equalsIgnoreCase("Basic")  &&
	    !challenge.getScheme().equalsIgnoreCase("Digest")  &&
	    !challenge.getScheme().equalsIgnoreCase("NTLM")  &&
	    !challenge.getScheme().equalsIgnoreCase("SOCKS5"))
	    return null;


	// For digest authentication, check if stale is set

	if (challenge.getScheme().equalsIgnoreCase("Digest"))
	{
	    cred = digest_check_stale(challenge, req, resp);
	    if (cred != null)
		return cred;
	}

	// For NTLM check if this is step 2 of the handshake

	else if (challenge.getScheme().equalsIgnoreCase("NTLM"))
	{
	    cred = ntlm_check_step2(challenge, req, resp, proxy);
	    if (cred != null)
		return cred;
	}



	// Ask the user for username/password

	NVPair answer;
	synchronized (getClass())
	{
	    if (!req.allowUI()  ||  prompterSet  &&  prompter == null)
		return null;

	    if (prompter == null)
		setDefaultPrompter();

	    answer = prompter.getUsernamePassword(challenge,
						  resp.getStatusCode() == 407);
	}

	if (answer == null)
	    return null;


	// Now process the username/password

	if (challenge.getScheme().equalsIgnoreCase("basic"))
	{
	    cred = new AuthorizationInfo(challenge.getHost(),
					 challenge.getPort(),
					 challenge.getScheme(),
					 challenge.getRealm(),
					 Codecs.base64Encode(
						answer.getName() + ":" +
						answer.getValue()));
	}
	else if (challenge.getScheme().equalsIgnoreCase("Digest"))
	{
	    cred = digest_gen_auth_info(challenge.getHost(),
					challenge.getPort(),
				        challenge.getRealm(), answer.getName(),
					answer.getValue(),
					req.getConnection().getContext());
	    cred = digest_fixup(cred, req, challenge, null);
	} else if (challenge.getScheme().equalsIgnoreCase("NTLM"))
      {
          cred = ntlm_gen_auth_info(challenge, answer, req);
      }
	else	// SOCKS5
	{
	    NVPair[] upwd = { answer };
	    cred = new AuthorizationInfo(challenge.getHost(),
					 challenge.getPort(),
					 challenge.getScheme(),
					 challenge.getRealm(),
					 upwd, null);
	}


	// try to get rid of any unencoded passwords in memory

	answer = null;
	System.gc();


	// Done

	Log.write(Log.AUTH, "Auth:  Got Authorization");

	return cred;
    }


    /**
     * We handle the "Authentication-Info" and "Proxy-Authentication-Info"
     * headers here.
     */
    public void handleAuthHeaders(Response resp, RoRequest req,
				  AuthorizationInfo prev,
				  AuthorizationInfo prxy)
	    throws IOException
    {
	String auth_info = resp.getHeader("Authentication-Info");
	String prxy_info = resp.getHeader("Proxy-Authentication-Info");

	if (auth_info == null  &&  prev != null  &&
	    hasParam(prev.getParams(), "qop", "auth-int"))
	    auth_info = "";

	if (prxy_info == null  &&  prxy != null  &&
	    hasParam(prxy.getParams(), "qop", "auth-int"))
	    prxy_info = "";

	try
	{
	    handleAuthInfo(auth_info, "Authentication-Info", prev, resp, req,
			   true);
	    handleAuthInfo(prxy_info, "Proxy-Authentication-Info", prxy, resp,
			   req, true);
	}
	catch (ParseException pe)
	    { throw new IOException(pe.toString()); }
    }


    /**
     * We handle the "Authentication-Info" and "Proxy-Authentication-Info"
     * trailers here.
     */
    public void handleAuthTrailers(Response resp, RoRequest req,
				   AuthorizationInfo prev,
				   AuthorizationInfo prxy)
	    throws IOException
    {
	String auth_info = resp.getTrailer("Authentication-Info");
	String prxy_info = resp.getTrailer("Proxy-Authentication-Info");

	try
	{
	    handleAuthInfo(auth_info, "Authentication-Info", prev, resp, req,
			   false);
	    handleAuthInfo(prxy_info, "Proxy-Authentication-Info", prxy, resp,
			   req, false);
	}
	catch (ParseException pe)
	    { throw new IOException(pe.toString()); }
    }


    private static void handleAuthInfo(String auth_info, String hdr_name,
				       AuthorizationInfo prev, Response resp,
				       RoRequest req, boolean in_headers)
	    throws ParseException, IOException
    {
	if (auth_info == null)  return;

	Vector<HttpHeaderElement> pai = Util.parseHeader(auth_info);
	HttpHeaderElement elem;

	if (handle_nextnonce(prev, req,
			     elem = Util.getElement(pai, "nextnonce")))
	    pai.removeElement(elem);
	if (handle_discard(prev, req, elem = Util.getElement(pai, "discard")))
	    pai.removeElement(elem);

	if (in_headers)
	{
	    HttpHeaderElement qop = null;

	    if (pai != null  &&
		(qop = Util.getElement(pai, "qop")) != null  &&
		qop.getValue() != null)
	    {
		handle_rspauth(prev, resp, req, pai, hdr_name);
	    }
	    else if (prev != null  &&
		     (Util.hasToken(resp.getHeader("Trailer"), hdr_name)  &&
		      hasParam(prev.getParams(), "qop", null)  ||
		      hasParam(prev.getParams(), "qop", "auth-int")))
	    {
		handle_rspauth(prev, resp, req, null, hdr_name);
	    }

	    else if ((pai != null  &&  qop == null  &&
		      pai.contains(new HttpHeaderElement("digest")))  ||
		     (Util.hasToken(resp.getHeader("Trailer"), hdr_name)  &&
		      prev != null  &&
		      !hasParam(prev.getParams(), "qop", null)))
	    {
		handle_digest(prev, resp, req, hdr_name);
	    }
	}

	if (pai.size() > 0)
	    resp.setHeader(hdr_name, Util.assembleHeader(pai));
	else
	    resp.deleteHeader(hdr_name);
    }


    private static final boolean hasParam(NVPair[] params, String name,
					  String val)
    {
	for (int idx=0; idx<params.length; idx++)
	    if (params[idx].getName().equalsIgnoreCase(name)  &&
		(val == null  ||  params[idx].getValue().equalsIgnoreCase(val)))
		return true;

	return false;
    }


    /*
     * Here are all the Digest specific methods
     */

    private static AuthorizationInfo digest_gen_auth_info(String host, int port,
							  String realm,
							  String user,
							  String pass,
							  Object context)
    {
	String A1 = user + ":" + realm + ":" + pass;
	String[] info = { MD5.hexDigest(A1), null, null };

	AuthorizationInfo prev = AuthorizationInfo.getAuthorization(host, port,
						    "Digest", realm, context);
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

	return new AuthorizationInfo(host, port, "Digest", realm, params, info);
    }


    /**
     * The fixup handler
     */
    private static AuthorizationInfo digest_fixup(AuthorizationInfo info,
						  RoRequest req,
						  AuthorizationInfo challenge,
						  RoResponse resp)
	    throws AuthSchemeNotImplException
    {
	// get various parameters from challenge

	int ch_domain=-1, ch_nonce=-1, ch_alg=-1, ch_opaque=-1, ch_stale=-1,
	    ch_dreq=-1, ch_qop=-1;
	NVPair[] ch_params = null;
	if (challenge != null)
	{
	    ch_params = challenge.getParams();

	    for (int idx=0; idx<ch_params.length; idx++)
	    {
		String name = ch_params[idx].getName().toLowerCase();
		if (name.equals("domain"))               ch_domain = idx;
		else if (name.equals("nonce"))           ch_nonce  = idx;
		else if (name.equals("opaque"))          ch_opaque = idx;
		else if (name.equals("algorithm"))       ch_alg    = idx;
		else if (name.equals("stale"))           ch_stale  = idx;
		else if (name.equals("digest-required")) ch_dreq   = idx;
		else if (name.equals("qop"))             ch_qop    = idx;
	    }
	}


	// get various parameters from info

	int uri=-1, user=-1, alg=-1, response=-1, nonce=-1, cnonce=-1, nc=-1,
	    opaque=-1, digest=-1, dreq=-1, qop=-1;
	NVPair[] params;
	String[] extra;

	synchronized (info)	// we need to juggle nonce, nc, etc
	{
	    params = info.getParams();

	    for (int idx=0; idx<params.length; idx++)
	    {
		String name = params[idx].getName().toLowerCase();
		if (name.equals("uri"))                  uri      = idx;
		else if (name.equals("username"))        user     = idx;
		else if (name.equals("algorithm"))       alg      = idx;
		else if (name.equals("nonce"))           nonce    = idx;
		else if (name.equals("cnonce"))          cnonce   = idx;
		else if (name.equals("nc"))              nc       = idx;
		else if (name.equals("response"))        response = idx;
		else if (name.equals("opaque"))          opaque   = idx;
		else if (name.equals("digest"))          digest   = idx;
		else if (name.equals("digest-required")) dreq     = idx;
		else if (name.equals("qop"))             qop      = idx;
	    }

	    extra = (String[]) info.getExtraInfo();


	    // currently only MD5 hash (and "MD5-sess") is supported

	    if (alg != -1  &&
		!params[alg].getValue().equalsIgnoreCase("MD5")  &&
		!params[alg].getValue().equalsIgnoreCase("MD5-sess"))
		throw new AuthSchemeNotImplException("Digest auth scheme: " +
				    "Algorithm " + params[alg].getValue() +
				    " not implemented");

	    if (ch_alg != -1  &&
		!ch_params[ch_alg].getValue().equalsIgnoreCase("MD5") &&
		!ch_params[ch_alg].getValue().equalsIgnoreCase("MD5-sess"))
		throw new AuthSchemeNotImplException("Digest auth scheme: " +
				    "Algorithm " + ch_params[ch_alg].getValue()+
				    " not implemented");


	    // fix up uri and nonce

	    params[uri] = new NVPair("uri",
		    URI.escape(req.getRequestURI(), URI.escpdPathChar, false));
	    String old_nonce = params[nonce].getValue();
	    if (ch_nonce != -1  &&
		!old_nonce.equals(ch_params[ch_nonce].getValue()))
		params[nonce] = ch_params[ch_nonce];


	    // update or add optional attributes (opaque, algorithm, cnonce,
	    // nonce-count, and qop

	    if (ch_opaque != -1)
	    {
		if (opaque == -1)
		{
		    params = Util.resizeArray(params, params.length+1);
		    opaque = params.length-1;
		}
		params[opaque] = ch_params[ch_opaque];
	    }

	    if (ch_alg != -1)
	    {
		if (alg == -1)
		{
		    params = Util.resizeArray(params, params.length+1);
		    alg = params.length-1;
		}
		params[alg] = ch_params[ch_alg];
	    }

	    if (ch_qop != -1  ||
		(ch_alg != -1  &&
		 ch_params[ch_alg].getValue().equalsIgnoreCase("MD5-sess")))
	    {
		if (cnonce == -1)
		{
		    params = Util.resizeArray(params, params.length+1);
		    cnonce = params.length-1;
		}

		if (digest_secret == null)
		    digest_secret = gen_random_bytes(20);

		long l_time = System.currentTimeMillis();
		byte[] time = new byte[8];
		time[0] = (byte) (l_time & 0xFF);
		time[1] = (byte) ((l_time >>  8) & 0xFF);
		time[2] = (byte) ((l_time >> 16) & 0xFF);
		time[3] = (byte) ((l_time >> 24) & 0xFF);
		time[4] = (byte) ((l_time >> 32) & 0xFF);
		time[5] = (byte) ((l_time >> 40) & 0xFF);
		time[6] = (byte) ((l_time >> 48) & 0xFF);
		time[7] = (byte) ((l_time >> 56) & 0xFF);

		params[cnonce] =
		    new NVPair("cnonce", MD5.hexDigest(digest_secret, time));
	    }


	    // select qop option

	    if (ch_qop != -1)
	    {
		if (qop == -1)
		{
		    params = Util.resizeArray(params, params.length+1);
		    qop = params.length-1;
		}
		extra[DI_QOP] = ch_params[ch_qop].getValue();


		// select qop option

		String[] qops = splitList(extra[DI_QOP], ",");
		String p = null;
		for (int idx=0; idx<qops.length; idx++)
		{
		    if (qops[idx].equalsIgnoreCase("auth-int")  &&
			(req.getStream() == null  ||
			 req.getConnection().ServProtVersKnown  &&
			 req.getConnection().ServerProtocolVersion >= HTTP_1_1))
		    {
			p = "auth-int";
			break;
		    }
		    if (qops[idx].equalsIgnoreCase("auth"))
			p = "auth";
		}
		if (p == null)
		{
		    for (int idx=0; idx<qops.length; idx++)
			if (qops[idx].equalsIgnoreCase("auth-int"))
			    throw new AuthSchemeNotImplException(
				"Digest auth scheme: Can't comply with qop " +
				"option 'auth-int' because an HttpOutputStream " +
				"is being used and the server doesn't speak " +
				"HTTP/1.1");

		    throw new AuthSchemeNotImplException("Digest auth scheme: "+
				"None of the available qop options '" +
				ch_params[ch_qop].getValue() + "' implemented");
		}
		params[qop] = new NVPair("qop", p);
	    }


	    // increment nonce-count.

	    if (qop != -1)
	    {
		/* Note: we should actually be serializing all requests through
		 *       here so that the server sees the nonce-count in a
		 *       strictly increasing order. However, this would be a
		 *       *major* hassle to do, so we're just winging it. Most
		 *       of the time the requests will go over the wire in the
		 *       same order as they pass through here, but in MT apps
		 *       it's possible for one request to "overtake" another
		 *       between here and the synchronized block in
		 *       sendRequest().
		 */
		if (nc == -1)
		{
		    params = Util.resizeArray(params, params.length+1);
		    nc = params.length-1;
		    params[nc] = new NVPair("nc", "00000001");
		}
		else if (old_nonce.equals(params[nonce].getValue()))
		{
		    String c = Long.toHexString(
				Long.parseLong(params[nc].getValue(), 16) + 1);
		    params[nc] =
			new NVPair("nc", "00000000".substring(c.length()) + c);
		}
		else
		    params[nc] = new NVPair("nc", "00000001");
	    }


	    // calc new session key if necessary

	    if (challenge != null  &&
		(ch_stale == -1  ||
		 !ch_params[ch_stale].getValue().equalsIgnoreCase("true"))  &&
		alg != -1  &&
		params[alg].getValue().equalsIgnoreCase("MD5-sess"))
	    {
		extra[DI_A1S] = MD5.hexDigest(extra[DI_A1] + ":" +
					      params[nonce].getValue() + ":" +
					      params[cnonce].getValue());
	    }


	    // update parameters for next auth cycle

	    info.setParams(params);
	    info.setExtraInfo(extra);
	}


	// calc "response" attribute

	String hash = null;
	if (qop != -1 && params[qop].getValue().equalsIgnoreCase("auth-int") &&
	    req.getStream() == null)
	{
	    hash = MD5.hexDigest(req.getData() == null ? NUL : req.getData());
	}

	if (req.getStream() == null)
	    params[response] = new NVPair("response",
		  calcResponseAttr(hash, extra, params, alg, uri, qop, nonce,
				   nc, cnonce, req.getMethod()));


	// calc digest if necessary

	AuthorizationInfo new_info;

	boolean ch_dreq_val = false;
	if (ch_dreq != -1  &&
	    (ch_params[ch_dreq].getValue() == null  ||
	     ch_params[ch_dreq].getValue().equalsIgnoreCase("true")))
	    ch_dreq_val = true;

	if ((ch_dreq_val  ||  digest != -1)  &&  req.getStream() == null)
	{
	    NVPair[] d_params;
	    if (digest == -1)
	    {
		d_params = Util.resizeArray(params, params.length+1);
		digest = params.length;
	    }
	    else
		d_params = params;
	    d_params[digest] = new NVPair("digest",
		   calc_digest(req, extra[DI_A1], params[nonce].getValue()));

	    if (dreq == -1)	// if server requires digest, then so do we...
	    {
		dreq = d_params.length;
		d_params = Util.resizeArray(d_params, d_params.length+1);
		d_params[dreq] = new NVPair("digest-required", "true");
	    }

	    new_info = new AuthorizationInfo(info.getHost(), info.getPort(),
					     info.getScheme(), info.getRealm(),
					     d_params, extra);
	}
	else if (ch_dreq_val)
	    new_info = null;
	else
	    new_info = new AuthorizationInfo(info.getHost(), info.getPort(),
					     info.getScheme(), info.getRealm(),
					     params, extra);


	// add info for other domains, if listed

	boolean from_server = (challenge != null)  &&
	    challenge.getHost().equalsIgnoreCase(req.getConnection().getHost());
	if (ch_domain != -1)
	{
	    URI base = null;
	    try
	    {
		base = new URI(req.getConnection().getProtocol(),
			       req.getConnection().getHost(),
			       req.getConnection().getPort(),
			       req.getRequestURI());
	    }
	    catch (ParseException pe)
		{ }

	    StringTokenizer tok =
			new StringTokenizer(ch_params[ch_domain].getValue());
	    while (tok.hasMoreTokens())
	    {
		URI Uri;
		try
		    { Uri = new URI(base, tok.nextToken()); }
		catch (ParseException pe)
		    { continue; }
		if (Uri.getHost() == null)
		    continue;

		AuthorizationInfo tmp =
		    AuthorizationInfo.getAuthorization(Uri.getHost(),
						       Uri.getPort(),
						       info.getScheme(),
						       info.getRealm(),
					     req.getConnection().getContext());
		if (tmp == null)
		{
		    params[uri] = new NVPair("uri", Uri.getPathAndQuery());
		    tmp = new AuthorizationInfo(Uri.getHost(), Uri.getPort(),
					        info.getScheme(),
						info.getRealm(), params,
						extra);
		    AuthorizationInfo.addAuthorization(tmp);
		}
		if (from_server)
		    tmp.addPath(Uri.getPathAndQuery());
	    }
	}
	else if (from_server  &&  challenge != null)
	{
	    // Spec says that if no domain attribute is present then the
	    // whole server should be considered being in the same space
	    AuthorizationInfo tmp =
		AuthorizationInfo.getAuthorization(challenge.getHost(),
						   challenge.getPort(),
						   info.getScheme(),
						   info.getRealm(),
					     req.getConnection().getContext());
	    if (tmp != null)  tmp.addPath("/");
	}


	// now return the one to use

	return new_info;
    }


    /**
     * @return the fixed info is stale=true; null otherwise
     */
    private static AuthorizationInfo digest_check_stale(
					      AuthorizationInfo challenge,
					      RoRequest req, RoResponse resp)
	    throws AuthSchemeNotImplException, IOException
    {
	AuthorizationInfo cred = null;

	NVPair[] params = challenge.getParams();
	for (int idx=0; idx<params.length; idx++)
	{
	    String name = params[idx].getName();
	    if (name.equalsIgnoreCase("stale")  &&
		params[idx].getValue().equalsIgnoreCase("true"))
	    {
		cred = AuthorizationInfo.getAuthorization(challenge, req, resp,
							  false, false);
		if (cred != null)	// should always be the case
		    return digest_fixup(cred, req, challenge, resp);
		break;			// should never be reached
	    }
	}

	return cred;
    }


    /**
     * Handle nextnonce field.
     */
    private static boolean handle_nextnonce(AuthorizationInfo prev,
					    RoRequest req,
					    HttpHeaderElement nextnonce)
	    throws IOException
    {
	if (prev == null  ||  nextnonce == null  ||
	    nextnonce.getValue() == null)
	    return false;

	AuthorizationInfo ai;
	try
	    { ai = AuthorizationInfo.getAuthorization(prev, req, null, false, false); }
	catch (AuthSchemeNotImplException asnie)
	    { ai = prev; /* shouldn't happen */ }
	synchronized (ai)
	{
	    NVPair[] params = ai.getParams();
	    params = setValue(params, "nonce", nextnonce.getValue());
	    params = setValue(params, "nc", "00000000");
	    ai.setParams(params);
	}

	return true;
    }


    /**
     * Handle digest field of the Authentication-Info response header.
     */
    private static boolean handle_digest(AuthorizationInfo prev, Response resp,
					 RoRequest req, String hdr_name)
	    throws IOException
    {
	if (prev == null)
	    return false;

	NVPair[] params = prev.getParams();
	VerifyDigest
	    verifier = new VerifyDigest(((String[]) prev.getExtraInfo())[0],
					getValue(params, "nonce"),
					req.getMethod(),
					getValue(params, "uri"),
					hdr_name, resp);

	if (resp.hasEntity())
	{
	    Log.write(Log.AUTH, "Auth:  pushing md5-check-stream to verify "+
				"digest from " + hdr_name);
	    resp.inp_stream = new MD5InputStream(resp.inp_stream, verifier);
	}
	else
	{
	    Log.write(Log.AUTH, "Auth:  verifying digest from " + hdr_name);
	    verifier.verifyHash(MD5.digest(NUL), 0);
	}

	return true;
    }


    /**
     * Handle rspauth field of the Authentication-Info response header.
     */
    private static boolean handle_rspauth(AuthorizationInfo prev, Response resp,
					  RoRequest req, Vector<HttpHeaderElement> auth_info,
					  String hdr_name)
	    throws IOException
    {
	if (prev == null)
	    return false;


	// get the parameters we sent

	NVPair[] params = prev.getParams();
	int uri=-1, alg=-1, nonce=-1, cnonce=-1, nc=-1;
	for (int idx=0; idx<params.length; idx++)
	{
	    String name = params[idx].getName().toLowerCase();
	    if (name.equals("uri"))            uri    = idx;
	    else if (name.equals("algorithm")) alg    = idx;
	    else if (name.equals("nonce"))     nonce  = idx;
	    else if (name.equals("cnonce"))    cnonce = idx;
	    else if (name.equals("nc"))        nc     = idx;
	}


	// create hash verifier to verify rspauth

	VerifyRspAuth
	    verifier = new VerifyRspAuth(params[uri].getValue(),
			      ((String[]) prev.getExtraInfo())[0],
			      (alg == -1 ? null : params[alg].getValue()),
						  params[nonce].getValue(),
			      (cnonce == -1 ? "" : params[cnonce].getValue()),
			      (nc == -1 ? "" : params[nc].getValue()),
			      hdr_name, resp);


	// if Authentication-Info in header and qop=auth then verify immediately

	HttpHeaderElement qop = null;
	if (auth_info != null  &&
	    (qop = Util.getElement(auth_info, "qop")) != null  &&
	    qop.getValue() != null  &&
	    (qop.getValue().equalsIgnoreCase("auth")  ||
	     !resp.hasEntity()  &&  qop.getValue().equalsIgnoreCase("auth-int"))
	   )
	{
	    Log.write(Log.AUTH, "Auth:  verifying rspauth from " + hdr_name);
	    verifier.verifyHash(MD5.digest(NUL), 0);
	}
	else
	{
	    // else push md5 stream and verify after body

	    Log.write(Log.AUTH, "Auth:  pushing md5-check-stream to verify "+
				"rspauth from " + hdr_name);
	    resp.inp_stream = new MD5InputStream(resp.inp_stream, verifier);
	}

	return true;
    }


    /**
     * Calc "response" attribute for a request.
     */
    private static String calcResponseAttr(String hash, String[] extra,
					   NVPair[] params, int alg,
					   int uri, int qop, int nonce,
					   int nc, int cnonce, String method)
    {
	String A1, A2, resp_val;

	if (alg != -1  &&
	    params[alg].getValue().equalsIgnoreCase("MD5-sess"))
	    A1 = extra[DI_A1S];
	else
	    A1 = extra[DI_A1];

	A2 = method + ":" + params[uri].getValue();
	if (qop != -1  &&
	    params[qop].getValue().equalsIgnoreCase("auth-int"))
	{
	    A2 += ":" + hash;
	}
	A2 = MD5.hexDigest(A2);

	if (qop == -1)
	    resp_val =
		MD5.hexDigest(A1 + ":" + params[nonce].getValue() + ":" + A2);
	else
	    resp_val =
		MD5.hexDigest(A1 + ":" + params[nonce].getValue() + ":" +
			      params[nc].getValue() + ":" +
			      params[cnonce].getValue() + ":" +
			      params[qop].getValue() + ":" + A2);

	return resp_val;
    }


    /**
     * Calculates the digest of the request body. This was in RFC-2069
     * and draft-ietf-http-authentication-00.txt, but has subsequently
     * been removed. Here for backwards compatibility.
     */
    private static String calc_digest(RoRequest req, String A1_hash,
				      String nonce)
    {
	if (req.getStream() != null)
	    return "";

	int ct=-1, ce=-1, lm=-1, ex=-1, dt=-1;
	for (int idx=0; idx<req.getHeaders().length; idx++)
	{
	    String name = req.getHeaders()[idx].getName();
	    if (name.equalsIgnoreCase("Content-type"))
		ct = idx;
	    else if (name.equalsIgnoreCase("Content-Encoding"))
		ce = idx;
	    else if (name.equalsIgnoreCase("Last-Modified"))
		lm = idx;
	    else if (name.equalsIgnoreCase("Expires"))
		ex = idx;
	    else if (name.equalsIgnoreCase("Date"))
		dt = idx;
	}


	NVPair[] hdrs = req.getHeaders();
	byte[] entity_body = (req.getData() == null ? NUL : req.getData());
	String entity_hash = MD5.hexDigest(entity_body);

	String entity_info = MD5.hexDigest(req.getRequestURI() + ":" +
	     (ct == -1 ? "" : hdrs[ct].getValue()) + ":" +
	     entity_body.length + ":" +
	     (ce == -1 ? "" : hdrs[ce].getValue()) + ":" +
	     (lm == -1 ? "" : hdrs[lm].getValue()) + ":" +
	     (ex == -1 ? "" : hdrs[ex].getValue()));
	String entity_digest = A1_hash + ":" + nonce + ":" + req.getMethod() +
			":" + (dt == -1 ? "" : hdrs[dt].getValue()) +
			":" + entity_info + ":" + entity_hash;

	if (Log.isEnabled(Log.AUTH))
	{
	    Log.write(Log.AUTH, "Auth:  Entity-Info: '" + req.getRequestURI() + ":" +
		 (ct == -1 ? "" : hdrs[ct].getValue()) + ":" +
		 entity_body.length + ":" +
		 (ce == -1 ? "" : hdrs[ce].getValue()) + ":" +
		 (lm == -1 ? "" : hdrs[lm].getValue()) + ":" +
		 (ex == -1 ? "" : hdrs[ex].getValue()) +"'");
	    Log.write(Log.AUTH, "Auth:  Entity-Body: '" + entity_hash + "'");
	    Log.write(Log.AUTH, "Auth:  Entity-Digest: '" + entity_digest + "'");
	}

	return MD5.hexDigest(entity_digest);
    }


    /**
     * Handle discard token
     */
    private static boolean handle_discard(AuthorizationInfo prev, RoRequest req,
					  HttpHeaderElement discard)
    {
	if (discard != null  &&  prev != null)
	{
	    AuthorizationInfo.removeAuthorization(prev,
					    req.getConnection().getContext());
	    return true;
	}

	return false;
    }


    /**
     * Generate <var>num</var> bytes of random data.
     *
     * @param num  the number of bytes to generate
     * @return a byte array of random data
     */
    private static byte[] gen_random_bytes(int num)
    {
	// first try /dev/random
	try
	{
	    FileInputStream rnd = new FileInputStream("/dev/random");
	    DataInputStream din = new DataInputStream(rnd);
	    byte[] data = new byte[num];
	    din.readFully(data);
	    try { din.close(); } catch (IOException ioe) { }
	    return data;
	}
	catch (Throwable t)
	    { }

	/* This is probably a much better generator, but it can be awfully
	 * slow (~ 6 secs / byte on my old LX)
	 */
	//return new java.security.SecureRandom().getSeed(num);

	/* this is faster, but needs to be done better... */
	byte[] data = new byte[num];
	try
	{
	    long fm = Runtime.getRuntime().freeMemory();
	    data[0] = (byte) (fm & 0xFF);
	    data[1] = (byte) ((fm >>  8) & 0xFF);

	    int h = data.hashCode();
	    data[2] = (byte) (h & 0xFF);
	    data[3] = (byte) ((h >>  8) & 0xFF);
	    data[4] = (byte) ((h >> 16) & 0xFF);
	    data[5] = (byte) ((h >> 24) & 0xFF);

	    long time = System.currentTimeMillis();
	    data[6] = (byte) (time & 0xFF);
	    data[7] = (byte) ((time >>  8) & 0xFF);
	}
	catch (ArrayIndexOutOfBoundsException aioobe)
	    { }

	return data;
    }

    /*
     * Here are all the NTLM specific methods
     */

    private static AuthorizationInfo ntlm_gen_auth_info(
				AuthorizationInfo challenge, NVPair answer,
				RoRequest req)
	    throws AuthSchemeNotImplException
    {
	// hash the password
	byte[] lm_hpw = calc_lm_hpw(answer.getValue());
	byte[] nt_hpw = calc_ntcr_hpw(answer.getValue());

	// get the local host name
	String host = null;
	try
	    { host = System.getProperty("HTTPClient.defAuthHandler.NTLM.host"); }
	catch (SecurityException se)
	    { }
	if (host == null)
	    try
		{ host = InetAddress.getLocalHost().getHostName(); }
	    catch (Exception e)
		{ }
	if (host == null)
	    host = "localhost";	// ???

	int dot = host.indexOf('.');
	if (dot != -1)
	    host = host.substring(0, dot);

	// get user and domain name
	String domain = null;
	int slash;
	if ((slash = answer.getName().indexOf('\\')) != -1)
	    domain = answer.getName().substring(0, slash);
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
		domain = host;	// ???
	}

	String user = answer.getName().substring(slash+1);

	// store info in extra_info field
	Object[] info = { user, host.toUpperCase().trim(),
			  domain.toUpperCase().trim(), lm_hpw, nt_hpw };

	AuthorizationInfo cred = new AuthorizationInfo(challenge.getHost(),
						       challenge.getPort(),
						       challenge.getScheme(),
						       challenge.getRealm(),
						       null, info);

	return ntlm_fixup(cred, req, challenge, null);
    }


    /**
     * The fixup handler
     */
    private static AuthorizationInfo ntlm_fixup(AuthorizationInfo info,
						RoRequest req,
						AuthorizationInfo challenge,
						RoResponse resp)
	    throws AuthSchemeNotImplException
    {
	if (challenge == null)
	    return info;	// preemptive stuff - nothing to be done


	// get the various info we store in the extra_info field

	Object[] extra = (Object[]) info.getExtraInfo();
	String user   = (String) extra[0];
	String host   = (String) extra[1];
	String dom    = (String) extra[2];
	byte[] lm_hpw = (byte[]) extra[3];
	byte[] nt_hpw = (byte[]) extra[4];


	// response to the challenges

	byte[] msg;

	if (challenge.getCookie() == null)	// Initial challenge
	{
	    // send type 1 message

	    msg = new byte[32 + host.length() + dom.length()];
	    "NTLMSSP".getBytes(0, 7, msg, 0);		// NTLMSSP message
	    msg[8]  = 1;				// type 1
	    int off = 32, len;

	    msg[12] = (byte) 0x03;			// ???
	    msg[13] = (byte) 0xb2;

	    len = host.length();
	    msg[24] = (byte) len;			// host length
	    msg[25] = (byte) (len >> 8);
	    msg[26] = (byte) len;			// host length
	    msg[27] = (byte) (len >> 8);
	    msg[28] = (byte) off;			// host offset
	    msg[29] = (byte) (off >> 8);
	    host.getBytes(0, len, msg, off);		// host
	    off += len;

	    len = dom.length();
	    msg[16] = (byte) len;			// domain length
	    msg[17] = (byte) (len >> 8);
	    msg[18] = (byte) len;			// domain length
	    msg[19] = (byte) (len >> 8);
	    msg[20] = (byte) off;			// domain offset
	    msg[21] = (byte) (off >> 8);
	    dom.getBytes(0, len, msg, off);		// domain
	    off += len;
	}
	else					// expect type 2 message
	{
	    // decode message

	    String enc_msg = challenge.getCookie();
	    byte tmp[] = new byte[enc_msg.length()];
	    enc_msg.getBytes(0, tmp.length, tmp, 0);
	    msg = Codecs.base64Decode(tmp);
	    if (msg.length < 32  ||  msg[8] != 2  ||  msg[16] != msg.length)
		throw new AuthSchemeNotImplException("NTLM auth scheme: " +
						    "received invalid message");


	    // get nonce

	    byte[] nonce = new byte[8];
	    System.arraycopy(msg, 24, nonce, 0, 8);


	    // create new type 3 message

	    msg = new byte[64 + 2*dom.length() + 2*user.length() +
			   2*host.length() + 48];
	    "NTLMSSP".getBytes(0, 7, msg, 0);		// NTLMSSP message
	    msg[8]  = 3;				// type 3
	    int off = 64, len;

	    msg[60] = (byte) 0x01;			// ???
	    msg[61] = (byte) 0x82;

	    len = 2*dom.length();
	    msg[28] = (byte) len;			// domain length
	    msg[29] = (byte) (len >> 8);
	    msg[30] = (byte) len;			// domain length
	    msg[29] = (byte) (len >> 8);
	    msg[32] = (byte) off;			// domain offset
	    msg[33] = (byte) (off >> 8);
	    off = writeUnicode(dom, msg, off);		// domain

	    len = 2*user.length();
	    msg[36] = (byte) len;			// user length
	    msg[37] = (byte) (len >> 8);
	    msg[38] = (byte) len;			// user length
	    msg[39] = (byte) (len >> 8);
	    msg[40] = (byte) off;			// user offset
	    msg[41] = (byte) (off >> 8);
	    off = writeUnicode(user, msg, off);		// user

	    len = 2*host.length();
	    msg[44] = (byte) len;			// host length
	    msg[45] = (byte) (len >> 8);
	    msg[46] = (byte) len;			// host length
	    msg[47] = (byte) (len >> 8);
	    msg[48] = (byte) off;			// host offset
	    msg[49] = (byte) (off >> 8);
	    off = writeUnicode(host, msg, off);		// host

	    msg[12] = 24;				// lm hash length
	    msg[13] = 0;
	    msg[14] = 24;				// lm hash length
	    msg[15] = 0;
	    msg[16] = (byte) (off);			// lm hash offset
	    msg[17] = (byte) (off >> 8);
	    if (send_lm_auth)
		System.arraycopy(calc_ntcr_resp(lm_hpw, nonce), 0, msg, off, 24);
	    else
		System.arraycopy(zeros, 0, msg, off, 24);
	    off += 24;

	    msg[20] = 24;				// nt hash length
	    msg[21] = 0;
	    msg[22] = 24;				// nt hash length
	    msg[23] = 0;
	    msg[24] = (byte) (off);			// nt hash offset
	    msg[25] = (byte) (off >> 8);
	    System.arraycopy(calc_ntcr_resp(nt_hpw, nonce), 0, msg, off, 24);
	    off += 24;

	    msg[56] = (byte) (off);			// message length
	    msg[57] = (byte) (off >> 8);		// message length
	}


	// return new AuthorizationInfo

	String cookie = new String(Codecs.base64Encode(msg), 0);

	AuthorizationInfo cred = new AuthorizationInfo(challenge.getHost(),
						       challenge.getPort(),
						       challenge.getScheme(),
						       challenge.getRealm(),
						       cookie);
	cred.setExtraInfo(extra);
	info.setCookie(cookie);

	return cred;
    }


    /**
     * @return the info for step 2 if appropriate; null otherwise
     */
    private static AuthorizationInfo ntlm_check_step2(
					      AuthorizationInfo challenge,
					      RoRequest req, RoResponse resp,
					      boolean proxy)
		    throws AuthSchemeNotImplException, IOException
    {
	NVPair[] hdrs = req.getHeaders();
	String auth = null;
	for (int idx=0; idx<hdrs.length; idx++)
	{
	    if (proxy  &&  hdrs[idx].getName().equalsIgnoreCase("Proxy-Authorization")  ||
		!proxy  &&  hdrs[idx].getName().equalsIgnoreCase("Authorization"))
	    {
		auth = hdrs[idx].getValue();
		break;
	    }
	}

	AuthorizationInfo cred = AuthorizationInfo.getAuthorization(challenge,
						    req, resp, false, proxy);

	/* If we received a cookie (i.e. type 2 message) in the challenge
	 * and our previously sent data was a type 1 message, then this must
	 * be step 2 in the handshake.
	 */
	if (challenge.getCookie() != null  &&  cred != null  &&
	    auth != null  &&  auth.startsWith("NTLM TlRMTVNTUAAB"))
	    return ntlm_fixup(cred, req, challenge, null);

	return null;
    }


    /**
     * Write unicode string to buffer in little-endian format.
     */
    private static int writeUnicode(String str, byte[] buf, int off)
    {
	int len = str.length();
	for (int idx=0; idx<len; idx++)
	{
	    int c = str.charAt(idx);
	    buf[off++] = (byte) c;
	    buf[off++] = (byte) (c >> 8);
	}

	return off;
    }


    /**
     * Calculates the NTCR hashed unicode password. See
     * ftp://samba.anu.edu.au/pub/samba/docs/ENCRYPTION.txt more info.
     *
     * @param passw the password
     * @return the hashed password with 5 zeros appended
     */
    static byte[] calc_ntcr_hpw(String passw)
    {
	// put password into an array of bytes, writing the unicode chars
	// in little endian order

	byte[] uc = new byte[passw.length() * 2];
	for (int idx=0, dst=0; idx<passw.length(); idx++)
	{
	    char ch = passw.charAt(idx);
	    uc[dst++] = (byte) (ch & 0xFF);
	    uc[dst++] = (byte) (ch >>> 8);
	}


	// calc MD4 hash of password (as unicode array)

	byte[] hash = new MD4(uc).getHash();
	return Util.resizeArray(hash, 21);
    }


    /**
     * Calculates the LanManger hashed password. See
     * ftp://samba.anu.edu.au/pub/samba/docs/ENCRYPTION.txt more info.
     *
     * @param passw the password
     * @return the hashed password
     */
    static byte[] calc_lm_hpw(String passw)
    {
	// uppercase the password
	passw = passw.toUpperCase();

	// store in byte array, truncating or extending to 14 bytes
	byte[] keys = new byte[14];
	passw.getBytes(0, Math.min(passw.length(), 14), keys, 0);

	// DES encode the magic value with the above generated keys
	byte[] resp  = new byte[21],
	       /* the following must decrypted with an all-zeroes key
	       magic = { (byte) 0xAA, (byte) 0xD3, (byte) 0xB4, (byte) 0x35,
			 (byte) 0xB5, (byte) 0x14, (byte) 0x04, (byte) 0xEE},
		* to yield the real magic text:
	        */
	       magic = { (byte) 0x4B, (byte) 0x47, (byte) 0x53, (byte) 0x21,
			 (byte) 0x40, (byte) 0x23, (byte) 0x24, (byte) 0x25},
	       crypt = new byte[8];

	int[] ks = setup_key(keys, 0);
	DES.des_ecb_encrypt(magic, crypt, ks, true);
	System.arraycopy(crypt, 0, resp, 0, 8);

	ks = setup_key(keys, 7);
	DES.des_ecb_encrypt(magic, crypt, ks, true);
	System.arraycopy(crypt, 0, resp, 8, 8);

	// done
	return resp;
    }


    /**
     * Calculates the NTLM response. See
     * ftp://samba.anu.edu.au/pub/samba/docs/ENCRYPTION.txt more info.
     *
     * @param hpw   the hashed password
     * @param nonce the nonce from the server
     * @return the response String
     */
    private static byte[] calc_ntcr_resp(byte[] hpw, byte[] nonce)
    {
	// do the DES encryptions

	byte[] resp  = new byte[24],
	       crypt = new byte[8];

	int[] ks = setup_key(hpw, 0);
	DES.des_ecb_encrypt(nonce, crypt, ks, true);
	System.arraycopy(crypt, 0, resp, 0, 8);

	ks = setup_key(hpw, 7);
	DES.des_ecb_encrypt(nonce, crypt, ks, true);
	System.arraycopy(crypt, 0, resp, 8, 8);

	ks = setup_key(hpw, 14);
	DES.des_ecb_encrypt(nonce, crypt, ks, true);
	System.arraycopy(crypt, 0, resp, 16, 8);


	// done

	return resp;
    }


    private static int[] setup_key(byte[] k_56, int off)
    {
	// set DES key

	byte[] key = new byte[8];
	int[]  ks  = new int[32];

	key[0] = (byte) k_56[off];
	key[1] = (byte) ((k_56[off+0] << 7) | ((k_56[off+1] & 0xFF) >> 1));
	key[2] = (byte) ((k_56[off+1] << 6) | ((k_56[off+2] & 0xFF) >> 2));
	key[3] = (byte) ((k_56[off+2] << 5) | ((k_56[off+3] & 0xFF) >> 3));
	key[4] = (byte) ((k_56[off+3] << 4) | ((k_56[off+4] & 0xFF) >> 4));
	key[5] = (byte) ((k_56[off+4] << 3) | ((k_56[off+5] & 0xFF) >> 5));
	key[6] = (byte) ((k_56[off+5] << 2) | ((k_56[off+6] & 0xFF) >> 6));
	key[7] = (byte) (k_56[off+6] << 1);

	DES.des_set_odd_parity(key);
	DES.des_set_key(key, ks);

	return ks;
    }


    /**
     * Return the value of the first NVPair whose name matches the key
     * using a case-insensitive search.
     *
     * @param list an array of NVPair's
     * @param key  the key to search for
     * @return the value of the NVPair with that key, or null if not
     *         found.
     */
    private final static String getValue(NVPair[] list, String key)
    {
	int len = list.length;

	for (int idx=0; idx<len; idx++)
	    if (list[idx].getName().equalsIgnoreCase(key))
		return list[idx].getValue();

	return null;
    }

    /**
     * Return the index of the first NVPair whose name matches the key
     * using a case-insensitive search.
     *
     * @param list an array of NVPair's
     * @param key  the key to search for
     * @return the index of the NVPair with that key, or -1 if not
     *         found.
     */
    private final static int getIndex(NVPair[] list, String key)
    {
	int len = list.length;

	for (int idx=0; idx<len; idx++)
	    if (list[idx].getName().equalsIgnoreCase(key))
		return idx;

	return -1;
    }

    /**
     * Sets the value of the NVPair with the name that matches the key
     * (case-insensitive). If no name matches, a new entry is created.
     *
     * @param list an array of NVPair's
     * @param key  the name of the NVPair
     * @param val  the value of the new NVPair
     * @return the (possibly) new list
     */
    private final static NVPair[] setValue(NVPair[] list, String key, String val)
    {
	int idx = getIndex(list, key);
	if (idx == -1)
	{
	    idx = list.length;
	    list = Util.resizeArray(list, list.length+1);
	}

	list[idx] = new NVPair(key, val);
	return list;
    }


    /**
     * Split a list into an array of Strings, using sep as the
     * separator and removing whitespace around the separator.
     */
    private static String[] splitList(String str, String sep)
    {
	if (str == null)  return new String[0];

	StringTokenizer tok = new StringTokenizer(str, sep);
	String[] list = new String[tok.countTokens()];
	for (int idx=0; idx<list.length; idx++)
	    list[idx] = tok.nextToken().trim();

	return list;
    }


    /**
     * Produce a string of the form "A5:22:F1:0B:53"
     */
    static String hex(byte[] buf)
    {
	StringBuffer str = new StringBuffer(buf.length*3);
	for (int idx=0; idx<buf.length; idx++)
	{
	    str.append(Character.forDigit((buf[idx] >> 4) & 15, 16));
	    str.append(Character.forDigit(buf[idx] & 15, 16));
	    str.append(':');
	}
	str.setLength(str.length()-1);

	return str.toString();
    }


    static final byte[] unHex(String hex)
    {
	byte[] digest = new byte[hex.length()/2];

	for (int idx=0; idx<digest.length; idx++)
	{
	    digest[idx] = (byte) (0xFF & Integer.parseInt(
				  hex.substring(2*idx, 2*(idx+1)), 16));
	}

	return digest;
    }


    /**
     * Set a new username/password prompter.
     *
     * @param prompt the AuthorizationPrompter to use whenever a username
     *               and password are needed; if null, no querying will be
     *               done
     * @return the previous prompter
     */
    public static synchronized AuthorizationPrompter setAuthorizationPrompter(
					    AuthorizationPrompter prompt)
    {
	AuthorizationPrompter prev = prompter;
	prompter    = prompt;
	prompterSet = true;
	return prev;
    }


    /**
     * Set the default authorization prompter. It first tries to figure out
     * if the AWT is running, and if it is then the GUI popup prompter is used;
     * otherwise the command line prompter is used.
     */
    private static void setDefaultPrompter()
    {
	    prompter = new SimpleAuthPrompt();
    }

    /**
     * Try and figure out if the AWT is running. This is done by searching all
     * threads and looking for one whose name starts with "AWT-".
     */
    private static final boolean isAWTRunning() {
	// find top-level thread group
	ThreadGroup root = Thread.currentThread().getThreadGroup();
	while (root.getParent() != null)
	    root = root.getParent();

	// search all threads
	Thread[] t_list = new Thread[root.activeCount() + 5];
	int t_num = root.enumerate(t_list);
	for (int idx=0; idx<t_num; idx++)
	{
	    if (t_list[idx].getName().startsWith("AWT-"))
		return true;
	}

	return false;
    }
}


/**
 * This verifies the "rspauth" from draft-ietf-http-authentication-03
 */
class VerifyRspAuth implements HashVerifier, GlobalConstants
{
    private String     uri;
    private String     HA1;
    private String     alg;
    private String     nonce;
    private String     cnonce;
    private String     nc;
    private String     hdr;
    private RoResponse resp;


    public VerifyRspAuth(String uri, String HA1, String alg, String nonce,
			 String cnonce, String nc, String hdr, RoResponse resp)
    {
	this.uri    = uri;
	this.HA1    = HA1;
	this.alg    = alg;
	this.nonce  = nonce;
	this.cnonce = cnonce;
	this.nc     = nc;
	this.hdr    = hdr;
	this.resp   = resp;
    }


    public void verifyHash(byte[] hash, long len)  throws IOException
    {
	String auth_info = resp.getHeader(hdr);
	if (auth_info == null)
	    auth_info = resp.getTrailer(hdr);
	if (auth_info == null)
	    return;

	Vector<HttpHeaderElement> pai;
	try
	    { pai = Util.parseHeader(auth_info); }
	catch (ParseException pe)
	    { throw new IOException(pe.toString()); }

	String qop;
	HttpHeaderElement elem = Util.getElement(pai, "qop");
	if (elem == null  ||  (qop = elem.getValue()) == null  ||
	    (!qop.equalsIgnoreCase("auth")  &&
	     !qop.equalsIgnoreCase("auth-int")))
	    return;

	elem = Util.getElement(pai, "rspauth");
	if (elem == null  ||  elem.getValue() == null) return;
	byte[] digest = DefaultAuthHandler.unHex(elem.getValue());

	elem = Util.getElement(pai, "cnonce");
	if (elem != null  &&  elem.getValue() != null  &&
	    !elem.getValue().equals(cnonce))
	    throw new IOException("Digest auth scheme: received wrong " +
				  "client-nonce '" + elem.getValue() +
				  "' - expected '" + cnonce + "'");

	elem = Util.getElement(pai, "nc");
	if (elem != null  &&  elem.getValue() != null  &&
	    !elem.getValue().equals(nc))
	    throw new IOException("Digest auth scheme: received wrong " +
				  "nonce-count '" + elem.getValue() +
				  "' - expected '" + nc + "'");

	String A1, A2;
	if (alg != null  &&  alg.equalsIgnoreCase("MD5-sess"))
	    A1 = MD5.hexDigest(HA1 + ":" + nonce + ":" + cnonce);
	else
	    A1 = HA1;

	// draft-01 was: A2 = resp.getStatusCode() + ":" + uri;
	A2 = ":" + uri;
	if (qop.equalsIgnoreCase("auth-int"))
	    A2 += ":" + MD5.toHex(hash);
	A2 = MD5.hexDigest(A2);

	hash = MD5.digest(A1 + ":" + nonce + ":" +  nc + ":" + cnonce + ":" +
			  qop + ":" + A2);

	for (int idx=0; idx<hash.length; idx++)
	{
	    if (hash[idx] != digest[idx])
		throw new IOException("MD5-Digest mismatch: expected " +
				      DefaultAuthHandler.hex(digest) +
				      " but calculated " +
				      DefaultAuthHandler.hex(hash));
	}

	Log.write(Log.AUTH, "Auth:  rspauth from " + hdr +
			    " successfully verified");
    }
}


/**
 * This verifies the "digest" from rfc-2069
 */
class VerifyDigest implements HashVerifier, GlobalConstants
{
    private String     HA1;
    private String     nonce;
    private String     method;
    private String     uri;
    private String     hdr;
    private RoResponse resp;


    public VerifyDigest(String HA1, String nonce, String method, String uri,
			String hdr, RoResponse resp)
    {
	this.HA1    = HA1;
	this.nonce  = nonce;
	this.method = method;
	this.uri    = uri;
	this.hdr    = hdr;
	this.resp   = resp;
    }


    public void verifyHash(byte[] hash, long len)  throws IOException
    {
	String auth_info = resp.getHeader(hdr);
	if (auth_info == null)
	    auth_info = resp.getTrailer(hdr);
	if (auth_info == null)
	    return;

	Vector<HttpHeaderElement> pai;
	try
	    { pai = Util.parseHeader(auth_info); }
	catch (ParseException pe)
	    { throw new IOException(pe.toString()); }
	HttpHeaderElement elem = Util.getElement(pai, "digest");
	if (elem == null  ||  elem.getValue() == null)
	    return;

	byte[] digest = DefaultAuthHandler.unHex(elem.getValue());

	String entity_info = MD5.hexDigest(
				uri + ":" +
				header_val("Content-Type", resp) + ":" +
				header_val("Content-Length", resp) + ":" +
				header_val("Content-Encoding", resp) + ":" +
				header_val("Last-Modified", resp) + ":" +
				header_val("Expires", resp));
	hash = MD5.digest(HA1 + ":" + nonce + ":" + method + ":" +
			  header_val("Date", resp) +
			  ":" + entity_info + ":" + MD5.toHex(hash));

	for (int idx=0; idx<hash.length; idx++)
	{
	    if (hash[idx] != digest[idx])
		throw new IOException("MD5-Digest mismatch: expected " +
				      DefaultAuthHandler.hex(digest) +
				      " but calculated " +
				      DefaultAuthHandler.hex(hash));
	}

	Log.write(Log.AUTH, "Auth:  digest from " + hdr +
			    " successfully verified");
    }


    private static final String header_val(String hdr_name, RoResponse resp)
	    throws IOException
    {
	String hdr = resp.getHeader(hdr_name);
	String tlr = resp.getTrailer(hdr_name);
	return (hdr != null ? hdr : (tlr != null ? tlr : ""));
    }
}

/**
 * This class implements a simple command line prompter that request
 * username and password used for the "basic" and "digest" authentication
 * schemes.
 *
 * @version	0.3-3  06/05/2001
 * @author	Ronald Tschal�r
 */
class SimpleAuthPrompt implements AuthorizationPrompter
{
    /**
     * the method called by DefaultAuthHandler.
     *
     * @return the username/password pair
     */
    public NVPair getUsernamePassword(AuthorizationInfo challenge, boolean forProxy)
    {
	String user, pass;

	if (challenge.getScheme().equalsIgnoreCase("SOCKS5"))
	{
	    System.out.println("Enter username and password for SOCKS " +
			       "server on host " + challenge.getHost());
	    System.out.println("Authentication Method: username/password");
	}
	else
	{
	    System.out.println("Enter username and password for realm `" +
			       challenge.getRealm() + "' on host " +
			       challenge.getHost() + ":" +
			       challenge.getPort());
	    System.out.println("Authentication Scheme: " +
			       challenge.getScheme());
	}


	// get username

	BufferedReader inp =
		    new BufferedReader(new InputStreamReader(System.in));
	System.out.print("Username: "); System.out.flush();
	try
	    { user = inp.readLine(); }
	catch (IOException ioe)
	    { return null; }
	if (user == null  ||  user.length() == 0)
	    return null;		// cancel'd


	// get password

	echo(false);
	System.out.print("Password: "); System.out.flush();
	try
	    { pass = inp.readLine(); }
	catch (IOException ioe)
	    { return null; }
	System.out.println();
	echo(true);

	if (pass == null)
	    return null;		// cancel'd


	// done

	return new NVPair(user, pass);
    }


    /*
     * Turn command-line echoing of typed characters on or off.
     */
    private static void echo(boolean on)
    {
	String os = System.getProperty("os.name");
	String[] cmd = null;

	if (os.equalsIgnoreCase("Windows 95")  ||
	    os.equalsIgnoreCase("Windows NT"))
	    // I don't think this works on M$ ...
	    cmd = new String[] { "echo", on ? "on" : "off" };
	else if (os.equalsIgnoreCase("Windows")  ||
		 os.equalsIgnoreCase("16-bit Windows"))
	    ;	// ???
	else if (os.equalsIgnoreCase("OS/2"))
	    ;	// ???
	else if (os.equalsIgnoreCase("Mac OS")  ||
		 os.equalsIgnoreCase("MacOS"))
	    ;	// ???
	else if (os.equalsIgnoreCase("OpenVMS") ||
		 os.equalsIgnoreCase("VMS"))
	    cmd = new String[] { "SET TERMINAL " + (on ? "/ECHO" : "/NOECHO") };
	else			// probably unix
	    cmd = new String[] { "/bin/sh", "-c",
				 "stty " + (on ? "echo" : "-echo") + " < /dev/tty" };

        if (cmd != null)
	    try
		{ Runtime.getRuntime().exec(cmd).waitFor(); }
	    catch (Exception e)
		{ }
    }

    /**
     * @return true for Unix's and VMS
     */
    static boolean canUseCLPrompt() {
	String os = System.getProperty("os.name");

	return (os.indexOf("Linux") >= 0    ||  os.indexOf("SunOS") >= 0  ||
		os.indexOf("Solaris") >= 0  ||  os.indexOf("BSD") >= 0    ||
		os.indexOf("AIX") >= 0      ||  os.indexOf("HP-UX") >= 0  ||
		os.indexOf("IRIX") >= 0     ||  os.indexOf("OSF") >= 0    ||
		os.indexOf("A/UX") >= 0     ||  os.indexOf("VMS") >= 0);
    }
}
