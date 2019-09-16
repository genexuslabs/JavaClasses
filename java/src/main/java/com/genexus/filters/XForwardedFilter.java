package com.genexus.filters;

/*
 * Copyright 2009 Xebia and the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * Servlet filter to integrate "X-Forwarded-For" and "X-Forwarded-Proto" HTTP headers.
 * </p>
 * <p>
 * Most of the design of this Servlet Filter is a port of <a
 * href="http://httpd.apache.org/docs/trunk/mod/mod_remoteip.html">mod_remoteip</a>, this servlet filter replaces the apparent client remote
 * IP address and hostname for the request with the IP address list presented by a proxy or a load balancer via a request headers (e.g.
 * "X-Forwarded-For").
 * </p>
 * <p>
 * Another feature of this servlet filter is to replace the apparent scheme (http/https) and server port with the scheme presented by a
 * proxy or a load balancer via a request header (e.g. "X-Forwarded-Proto").
 * </p>
 * <p>
 * This servlet filter proceeds as follows:
 * </p>
 * <p>
 * If the incoming <code>request.getRemoteAddr()</code> matches the servlet filter's list of internal proxies :
 * <ul>
 * <li>Loop on the comma delimited list of IPs and hostnames passed by the preceding load balancer or proxy in the given request's Http
 * header named <code>$remoteIPHeader</code> (default value <code>x-forwarded-for</code>). Values are processed in right-to-left order.</li>
 * <li>For each ip/host of the list:
 * <ul>
 * <li>if it matches the internal proxies list, the ip/host is swallowed</li>
 * <li>if it matches the trusted proxies list, the ip/host is added to the created proxies header</li>
 * <li>otherwise, the ip/host is declared to be the remote ip and looping is stopped.</li>
 * </ul>
 * </li>
 * <li>If the request http header named <code>$protocolHeader</code> (e.g. <code>x-forwarded-for</code>) equals to the value of
 * <code>protocolHeaderHttpsValue</code> configuration parameter (default <code>https</code>) then <code>request.isSecure = true</code>,
 * <code>request.scheme = https</code> and <code>request.serverPort = 443</code>. Note that 443 can be overwritten with the
 * <code>$httpsServerPort</code> configuration parameter.</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Configuration parameters:</strong>
 * <table border="1">
 * <tr>
 * <th>XForwardedFilter property</th>
 * <th>Description</th>
 * <th>Equivalent mod_remoteip directive</th>
 * <th>Format</th>
 * <th>Default Value</th>
 * </tr>
 * <tr>
 * <td>remoteIPHeader</td>
 * <td>Name of the Http Header read by this servlet filter that holds the list of traversed IP addresses starting from the requesting client
 * </td>
 * <td>RemoteIPHeader</td>
 * <td>Compliant http header name</td>
 * <td>x-forwarded-for</td>
 * </tr>
 * <tr>
 * <td>allowedInternalProxies</td>
 * <td>List of internal proxies ip adress. If they appear in the <code>remoteIpHeader</code> value, they will be trusted and will not appear
 * in the <code>proxiesHeader</code> value</td>
 * <td>RemoteIPInternalProxy</td>
 * <td>Comma delimited list of regular expressions (in the syntax supported by the {@link java.util.regex.Pattern} library)</td>
 * <td>10\.\d{1,3}\.\d{1,3}\.\d{1,3}, 192\.168\.\d{1,3}\.\d{1,3}, 172\\.(?:1[6-9]|2\\d|3[0-1]).\\d{1,3}.\\d{1,3}, 
 * 169\.254\.\d{1,3}\.\d{1,3}, 127\.\d{1,3}\.\d{1,3}\.\d{1,3} <br/>
 * By default, 10/8, 192.168/16, 172.16/12, 169.254/16 and 127/8 are allowed</td>
 * </tr>
 * </tr>
 * <tr>
 * <td>proxiesHeader</td>
 * <td>Name of the http header created by this servlet filter to hold the list of proxies that have been processed in the incoming
 * <code>remoteIPHeader</code></td>
 * <td>RemoteIPProxiesHeader</td>
 * <td>Compliant http header name</td>
 * <td>x-forwarded-by</td>
 * </tr>
 * <tr>
 * <td>trustedProxies</td>
 * <td>List of trusted proxies ip adress. If they appear in the <code>remoteIpHeader</code> value, they will be trusted and will appear in
 * the <code>proxiesHeader</code> value</td>
 * <td>RemoteIPTrustedProxy</td>
 * <td>Comma delimited list of regular expressions (in the syntax supported by the {@link java.util.regex.Pattern} library)</td>
 * <td>&nbsp;</td>
 * </tr>
 * <tr>
 * <td>protocolHeader</td>
 * <td>Name of the http header read by this servlet filter that holds the flag that this request</td>
 * <td>N/A</td>
 * <td>Compliant http header name like <code>X-Forwarded-Proto</code>, <code>X-Forwarded-Ssl</code> or <code>Front-End-Https</code></td>
 * <td><code>null</code></td>
 * </tr>
 * <tr>
 * <td>protocolHeaderHttpsValue</td>
 * <td>Value of the <code>protocolHeader</code> to indicate that it is an Https request</td>
 * <td>N/A</td>
 * <td>String like <code>https</code> or <code>ON</code></td>
 * <td><code>https</code></td>
 * </tr>
 * <tr>
 * <tr>
 * <td>httpServerPort</td>
 * <td>Value returned by {@link ServletRequest#getServerPort()} when the <code>protocolHeader</code> indicates <code>http</code> protocol</td>
 * <td>N/A</td>
 * <td>integer</td>
 * <td>80</td>
 * </tr>
 * <tr>
 * <td>httpsServerPort</td>
 * <td>Value returned by {@link ServletRequest#getServerPort()} when the <code>protocolHeader</code> indicates <code>https</code> protocol</td>
 * <td>N/A</td>
 * <td>integer</td>
 * <td>443</td>
 * </tr>
 * </table>
 * </p>
 * <p>
 * <p>
 * <strong>Regular expression vs. IP address blocks:</strong> <code>mod_remoteip</code> allows to use address blocks (e.g.
 * <code>192.168/16</code>) to configure <code>RemoteIPInternalProxy</code> and <code>RemoteIPTrustedProxy</code> ; as the JVM doesnt have a
 * library similar to <a
 * href="http://apr.apache.org/docs/apr/1.3/group__apr__network__io.html#gb74d21b8898b7c40bf7fd07ad3eb993d">apr_ipsubnet_test</a>.
 * </p>
 * <hr/>
 * <p>
 * <strong>Sample with internal proxies</strong>
 * </p>
 * <p>
 * XForwardedFilter configuration:
 * </p>
 * <code><pre>
 * &lt;filter&gt;
 *    &lt;filter-name&gt;XForwardedFilter&lt;/filter-name&gt;
 *    &lt;filter-class&gt;fr.xebia.servlet.filter.XForwardedFilter&lt;/filter-class&gt;
 *    &lt;init-param&gt;
 *       &lt;param-name&gt;allowedInternalProxies&lt;/param-name&gt;&lt;param-value&gt;192\.168\.0\.10, 192\.168\.0\.11&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 *    &lt;init-param&gt;
 *       &lt;param-name&gt;remoteIPHeader&lt;/param-name&gt;&lt;param-value&gt;x-forwarded-for&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 *    &lt;init-param&gt;
 *       &lt;param-name&gt;remoteIPProxiesHeader&lt;/param-name&gt;&lt;param-value&gt;x-forwarded-by&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 *    &lt;init-param&gt;
 *       &lt;param-name&gt;protocolHeader&lt;/param-name&gt;&lt;param-value&gt;x-forwarded-proto&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 * &lt;/filter&gt;
 * 
 * &lt;filter-mapping&gt;
 *    &lt;filter-name&gt;XForwardedFilter&lt;/filter-name&gt;
 *    &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 *    &lt;dispatcher&gt;REQUEST&lt;/dispatcher&gt;
 * &lt;/filter-mapping&gt;</pre></code>
 * <p>
 * Request values:
 * <table border="1">
 * <tr>
 * <th>property</th>
 * <th>Value Before XForwardedFilter</th>
 * <th>Value After XForwardedFilter</th>
 * </tr>
 * <tr>
 * <td>request.remoteAddr</td>
 * <td>192.168.0.10</td>
 * <td>140.211.11.130</td>
 * </tr>
 * <tr>
 * <td>request.header['x-forwarded-for']</td>
 * <td>140.211.11.130, 192.168.0.10</td>
 * <td>null</td>
 * </tr>
 * <tr>
 * <td>request.header['x-forwarded-by']</td>
 * <td>null</td>
 * <td>null</td>
 * </tr>
 * <tr>
 * <td>request.header['x-forwarded-proto']</td>
 * <td>https</td>
 * <td>https</td>
 * </tr>
 * <tr>
 * <td>request.scheme</td>
 * <td>http</td>
 * <td>https</td>
 * </tr>
 * <tr>
 * <td>request.secure</td>
 * <td>false</td>
 * <td>true</td>
 * </tr>
 * <tr>
 * <td>request.serverPort</td>
 * <td>80</td>
 * <td>443</td>
 * </tr>
 * </table>
 * Note : <code>x-forwarded-by</code> header is null because only internal proxies as been traversed by the request.
 * <code>x-forwarded-by</code> is null because all the proxies are trusted or internal.
 * </p>
 * <hr/>
 * <p>
 * <strong>Sample with trusted proxies</strong>
 * </p>
 * <p>
 * XForwardedFilter configuration:
 * </p>
 * <code><pre>
 * &lt;filter&gt;
 *    &lt;filter-name&gt;XForwardedFilter&lt;/filter-name&gt;
 *    &lt;filter-class&gt;fr.xebia.servlet.filter.XForwardedFilter&lt;/filter-class&gt;
 *    &lt;init-param&gt;
 *       &lt;param-name&gt;allowedInternalProxies&lt;/param-name&gt;&lt;param-value&gt;192\.168\.0\.10, 192\.168\.0\.11&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 *    &lt;init-param&gt;
 *       &lt;param-name&gt;remoteIPHeader&lt;/param-name&gt;&lt;param-value&gt;x-forwarded-for&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 *    &lt;init-param&gt;
 *       &lt;param-name&gt;remoteIPProxiesHeader&lt;/param-name&gt;&lt;param-value&gt;x-forwarded-by&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 *    &lt;init-param&gt;
 *       &lt;param-name&gt;trustedProxies&lt;/param-name&gt;&lt;param-value&gt;proxy1, proxy2&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 * &lt;/filter&gt;
 * 
 * &lt;filter-mapping&gt;
 *    &lt;filter-name&gt;XForwardedFilter&lt;/filter-name&gt;
 *    &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 *    &lt;dispatcher&gt;REQUEST&lt;/dispatcher&gt;
 * &lt;/filter-mapping&gt;</pre></code>
 * <p>
 * Request values:
 * <table border="1">
 * <tr>
 * <th>property</th>
 * <th>Value Before XForwardedFilter</th>
 * <th>Value After XForwardedFilter</th>
 * </tr>
 * <tr>
 * <td>request.remoteAddr</td>
 * <td>192.168.0.10</td>
 * <td>140.211.11.130</td>
 * </tr>
 * <tr>
 * <td>request.header['x-forwarded-for']</td>
 * <td>140.211.11.130, proxy1, proxy2</td>
 * <td>null</td>
 * </tr>
 * <tr>
 * <td>request.header['x-forwarded-by']</td>
 * <td>null</td>
 * <td>proxy1, proxy2</td>
 * </tr>
 * </table>
 * Note : <code>proxy1</code> and <code>proxy2</code> are both trusted proxies that come in <code>x-forwarded-for</code> header, they both
 * are migrated in <code>x-forwarded-by</code> header. <code>x-forwarded-by</code> is null because all the proxies are trusted or internal.
 * </p>
 * <hr/>
 * <p>
 * <strong>Sample with internal and trusted proxies</strong>
 * </p>
 * <p>
 * XForwardedFilter configuration:
 * </p>
 * <code><pre>
 * &lt;filter&gt;
 *    &lt;filter-name&gt;XForwardedFilter&lt;/filter-name&gt;
 *    &lt;filter-class&gt;fr.xebia.servlet.filter.XForwardedFilter&lt;/filter-class&gt;
 *    &lt;init-param&gt;
 *       &lt;param-name&gt;allowedInternalProxies&lt;/param-name&gt;&lt;param-value&gt;192\.168\.0\.10, 192\.168\.0\.11&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 *    &lt;init-param&gt;
 *       &lt;param-name&gt;remoteIPHeader&lt;/param-name&gt;&lt;param-value&gt;x-forwarded-for&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 *    &lt;init-param&gt;
 *       &lt;param-name&gt;remoteIPProxiesHeader&lt;/param-name&gt;&lt;param-value&gt;x-forwarded-by&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 *    &lt;init-param&gt;
 *       &lt;param-name&gt;trustedProxies&lt;/param-name&gt;&lt;param-value&gt;proxy1, proxy2&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 * &lt;/filter&gt;
 * 
 * &lt;filter-mapping&gt;
 *    &lt;filter-name&gt;XForwardedFilter&lt;/filter-name&gt;
 *    &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 *    &lt;dispatcher&gt;REQUEST&lt;/dispatcher&gt;
 * &lt;/filter-mapping&gt;</pre></code>
 * <p>
 * Request values:
 * <table border="1">
 * <tr>
 * <th>property</th>
 * <th>Value Before XForwardedFilter</th>
 * <th>Value After XForwardedFilter</th>
 * </tr>
 * <tr>
 * <td>request.remoteAddr</td>
 * <td>192.168.0.10</td>
 * <td>140.211.11.130</td>
 * </tr>
 * <tr>
 * <td>request.header['x-forwarded-for']</td>
 * <td>140.211.11.130, proxy1, proxy2, 192.168.0.10</td>
 * <td>null</td>
 * </tr>
 * <tr>
 * <td>request.header['x-forwarded-by']</td>
 * <td>null</td>
 * <td>proxy1, proxy2</td>
 * </tr>
 * </table>
 * Note : <code>proxy1</code> and <code>proxy2</code> are both trusted proxies that come in <code>x-forwarded-for</code> header, they both
 * are migrated in <code>x-forwarded-by</code> header. As <code>192.168.0.10</code> is an internal proxy, it does not appear in
 * <code>x-forwarded-by</code>. <code>x-forwarded-by</code> is null because all the proxies are trusted or internal.
 * </p>
 * <hr/>
 * <p>
 * <strong>Sample with an untrusted proxy</strong>
 * </p>
 * <p>
 * XForwardedFilter configuration:
 * </p>
 * <code><pre>
 * &lt;filter&gt;
 *    &lt;filter-name&gt;XForwardedFilter&lt;/filter-name&gt;
 *    &lt;filter-class&gt;fr.xebia.servlet.filter.XForwardedFilter&lt;/filter-class&gt;
 *    &lt;init-param&gt;
 *       &lt;param-name&gt;allowedInternalProxies&lt;/param-name&gt;&lt;param-value&gt;192\.168\.0\.10, 192\.168\.0\.11&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 *    &lt;init-param&gt;
 *       &lt;param-name&gt;remoteIPHeader&lt;/param-name&gt;&lt;param-value&gt;x-forwarded-for&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 *    &lt;init-param&gt;
 *       &lt;param-name&gt;remoteIPProxiesHeader&lt;/param-name&gt;&lt;param-value&gt;x-forwarded-by&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 *    &lt;init-param&gt;
 *       &lt;param-name&gt;trustedProxies&lt;/param-name&gt;&lt;param-value&gt;proxy1, proxy2&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 * &lt;/filter&gt;
 * 
 * &lt;filter-mapping&gt;
 *    &lt;filter-name&gt;XForwardedFilter&lt;/filter-name&gt;
 *    &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 *    &lt;dispatcher&gt;REQUEST&lt;/dispatcher&gt;
 * &lt;/filter-mapping&gt;</pre></code>
 * <p>
 * Request values:
 * <table border="1">
 * <tr>
 * <th>property</th>
 * <th>Value Before XForwardedFilter</th>
 * <th>Value After XForwardedFilter</th>
 * </tr>
 * <tr>
 * <td>request.remoteAddr</td>
 * <td>192.168.0.10</td>
 * <td>untrusted-proxy</td>
 * </tr>
 * <tr>
 * <td>request.header['x-forwarded-for']</td>
 * <td>140.211.11.130, untrusted-proxy, proxy1</td>
 * <td>140.211.11.130</td>
 * </tr>
 * <tr>
 * <td>request.header['x-forwarded-by']</td>
 * <td>null</td>
 * <td>proxy1</td>
 * </tr>
 * </table>
 * Note : <code>x-forwarded-by</code> holds the trusted proxy <code>proxy1</code>. <code>x-forwarded-by</code> holds
 * <code>140.211.11.130</code> because <code>untrusted-proxy</code> is not trusted and thus, we can not trust that
 * <code>untrusted-proxy</code> is the actual remote ip. <code>request.remoteAddr</code> is <code>untrusted-proxy</code> that is an IP
 * verified by <code>proxy1</code>.
 * </p>
 * <hr/>
 */
public class XForwardedFilter implements Filter {
    public static class XForwardedRequest extends HttpServletRequestWrapper {
        
        final static ThreadLocal<SimpleDateFormat[]> threadLocalDateFormats = new ThreadLocal<SimpleDateFormat[]>() {
            protected SimpleDateFormat[] initialValue() {
                return new SimpleDateFormat[] {
                    new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US),
                    new SimpleDateFormat("EEEEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US),
                    new SimpleDateFormat("EEE MMMM d HH:mm:ss yyyy", Locale.US)
                };
                
            };
        };
        
        protected Map<String, List<String>> headers;
        
        protected String remoteAddr;
        
        protected String remoteHost;
        
        protected String scheme;
        
        protected boolean secure;
        
        protected int serverPort;
        
        @SuppressWarnings("unchecked")
        public XForwardedRequest(HttpServletRequest request) {
            super(request);
            this.remoteAddr = request.getRemoteAddr();
            this.remoteHost = request.getRemoteHost();
            this.scheme = request.getScheme();
            this.secure = request.isSecure();
            this.serverPort = request.getServerPort();
            
            headers = new HashMap<String, List<String>>();
            for (Enumeration<String> headerNames = request.getHeaderNames(); headerNames.hasMoreElements();) {
                String header = headerNames.nextElement();
                headers.put(header, Collections.list(request.getHeaders(header)));
            }
        }
        
        @Override
        public long getDateHeader(String name) {
            String value = getHeader(name);
            if (value == null) {
                return -1;
            }
            DateFormat[] dateFormats = threadLocalDateFormats.get();
            Date date = null;
            for (int i = 0; ((i < dateFormats.length) && (date == null)); i++) {
                DateFormat dateFormat = dateFormats[i];
                try {
                    date = dateFormat.parse(value);
                } catch (Exception ParseException) {
                    ;
                }
            }
            if (date == null) {
                throw new IllegalArgumentException(value);
            } else {
                return date.getTime();
            }
        }
        
        @Override
        public String getHeader(String name) {
            Map.Entry<String, List<String>> header = getHeaderEntry(name);
            if (header == null || header.getValue() == null || header.getValue().isEmpty()) {
                return null;
            } else {
                return header.getValue().get(0);
            }
        }
        
        protected Map.Entry<String, List<String>> getHeaderEntry(String name) {
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(name)) {
                    return entry;
                }
            }
            return null;
        }
        
        @Override
        public Enumeration<String> getHeaderNames() {
            return Collections.enumeration(headers.keySet());
        }
        
        @Override
        public Enumeration<String> getHeaders(String name) {
            Map.Entry<String, List<String>> header = getHeaderEntry(name);
            if (header == null || header.getValue() == null) {
                return Collections.enumeration(Collections.emptyList());
            } else {
                return Collections.enumeration(header.getValue());
            }
        }
        
        @Override
        public int getIntHeader(String name) {
            String value = getHeader(name);
            if (value == null) {
                return -1;
            } else {
                return Integer.parseInt(value);
            }
        }
        
        @Override
        public String getRemoteAddr() {
            return this.remoteAddr;
        }
        
        @Override
        public String getRemoteHost() {
            return this.remoteHost;
        }
        
        public String getScheme() {
            return scheme;
        }
        
        public int getServerPort() {
            return serverPort;
        }
        
        public boolean isSecure() {
            return secure;
        }
        
        public void removeHeader(String name) {
            Map.Entry<String, List<String>> header = getHeaderEntry(name);
            if (header != null) {
                headers.remove(header.getKey());
            }
        }
        
        public void setHeader(String name, String value) {
            List<String> values = Arrays.asList(value);
            Map.Entry<String, List<String>> header = getHeaderEntry(name);
            if (header == null) {
                headers.put(name, values);
            } else {
                header.setValue(values);
            }
            
        }
        
        public void setRemoteAddr(String remoteAddr) {
            this.remoteAddr = remoteAddr;
        }
        
        public void setRemoteHost(String remoteHost) {
            this.remoteHost = remoteHost;
        }
        
        public void setScheme(String scheme) {
            this.scheme = scheme;
        }
        
        public void setSecure(boolean secure) {
            this.secure = secure;
        }
        
        public void setServerPort(int serverPort) {
            this.serverPort = serverPort;
        }
    }
    
    /**
     * {@link Pattern} for a comma delimited string that support whitespace characters
     */
    private static final Pattern commaSeparatedValuesPattern = Pattern.compile("\\s*,\\s*");
    
    protected static final String HTTP_SERVER_PORT_PARAMETER = "httpServerPort";

    protected static final String HTTPS_SERVER_PORT_PARAMETER = "httpsServerPort";

    protected static final String INTERNAL_PROXIES_PARAMETER = "allowedInternalProxies";
    
    /**
     * Logger
     */

    
    protected static final String PROTOCOL_HEADER_PARAMETER = "protocolHeader";
    
    protected static final String PROTOCOL_HEADER_SSL_VALUE_PARAMETER = "protocolHeaderSslValue";
    
    protected static final String PROXIES_HEADER_PARAMETER = "proxiesHeader";
    
    protected static final String REMOTE_IP_HEADER_PARAMETER = "remoteIPHeader";
    
    protected static final String TRUSTED_PROXIES_PARAMETER = "trustedProxies";
    
    /**
     * Convert a given comma delimited list of regular expressions into an array of compiled {@link Pattern}
     * 
     * @return array of patterns (not <code>null</code>)
     */
    protected static Pattern[] commaDelimitedListToPatternArray(String commaDelimitedPatterns) {
        String[] patterns = commaDelimitedListToStringArray(commaDelimitedPatterns);
        List<Pattern> patternsList = new ArrayList<Pattern>();
        for (String pattern : patterns) {
            try {
                patternsList.add(Pattern.compile(pattern));
            } catch (PatternSyntaxException e) {
                throw new IllegalArgumentException("Illegal pattern syntax '" + pattern + "'", e);
            }
        }
        return patternsList.toArray(new Pattern[0]);
    }
    
    /**
     * Convert a given comma delimited list of regular expressions into an array of String
     * 
     * @return array of patterns (non <code>null</code>)
     */
    protected static String[] commaDelimitedListToStringArray(String commaDelimitedStrings) {
        return (commaDelimitedStrings == null || commaDelimitedStrings.length() == 0) ? new String[0] : commaSeparatedValuesPattern
            .split(commaDelimitedStrings);
    }
    
    /**
     * Convert an array of strings in a comma delimited string
     */
    protected static String listToCommaDelimitedString(List<String> stringList) {
        if (stringList == null) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (Iterator<String> it = stringList.iterator(); it.hasNext();) {
            Object element = it.next();
            if (element != null) {
                result.append(element);
                if (it.hasNext()) {
                    result.append(", ");
                }
            }
        }
        return result.toString();
    }
    
    /**
     * Return <code>true</code> if the given <code>str</code> matches at least one of the given <code>patterns</code>.
     */
    protected static boolean matchesOne(String str, Pattern... patterns) {
        for (Pattern pattern : patterns) {
            if (pattern.matcher(str).matches()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * @see #setHttpServerPort(int)
     */
    private int httpServerPort = 80;

    /**
     * @see #setHttpsServerPort(int)
     */
    private int httpsServerPort = 443;

    /**
     * @see #setInternalProxies(String)
     */
    private Pattern[] allowedInternalProxies = new Pattern[] { Pattern.compile("10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"),
            Pattern.compile("192\\.168\\.\\d{1,3}\\.\\d{1,3}"), Pattern.compile("172\\.(?:1[6-9]|2\\d|3[0-1]).\\d{1,3}.\\d{1,3}"), 
            Pattern.compile("169\\.254\\.\\d{1,3}\\.\\d{1,3}"), Pattern.compile("127\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}") };
    
    /**
     * @see #setProtocolHeader(String)
     */
    private String protocolHeader = null;
    
    private String protocolHeaderSslValue = "https";
    
    /**
     * @see #setProxiesHeader(String)
     */
    private String proxiesHeader = "X-Forwarded-By";
    
    /**
     * @see #setRemoteIPHeader(String)
     */
    private String remoteIPHeader = "X-Forwarded-For";
    
    /**
     * @see #setTrustedProxies(String)
     */
    private Pattern[] trustedProxies = new Pattern[0];
    
    public void destroy() {
    }
    
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        
        if (matchesOne(request.getRemoteAddr(), allowedInternalProxies)) {
            String remoteIp = null;
            // In java 6, proxiesHeaderValue should be declared as a java.util.Deque
            LinkedList<String> proxiesHeaderValue = new LinkedList<String>();
            
            String[] remoteIPHeaderValue = commaDelimitedListToStringArray(request.getHeader(remoteIPHeader));
            int idx;
            // loop on remoteIPHeaderValue to find the first trusted remote ip and to build the proxies chain
            for (idx = remoteIPHeaderValue.length - 1; idx >= 0; idx--) {
                String currentRemoteIp = remoteIPHeaderValue[idx];
                remoteIp = currentRemoteIp;
                if (matchesOne(currentRemoteIp, allowedInternalProxies)) {
                    // do nothing, allowedInternalProxies IPs are not appended to the
                } else if (matchesOne(currentRemoteIp, trustedProxies)) {
                    proxiesHeaderValue.addFirst(currentRemoteIp);
                } else {
                    idx--; // decrement idx because break statement doesn't do it
                    break;
                }
            }
            // continue to loop on remoteIPHeaderValue to build the new value of the remoteIPHeader
            LinkedList<String> newRemoteIpHeaderValue = new LinkedList<String>();
            for (; idx >= 0; idx--) {
                String currentRemoteIp = remoteIPHeaderValue[idx];
                newRemoteIpHeaderValue.addFirst(currentRemoteIp);
            }
            
            XForwardedRequest xRequest = new XForwardedRequest(request);
            if (remoteIp != null) {
                
                xRequest.setRemoteAddr(remoteIp);
                xRequest.setRemoteHost(remoteIp);
                
                if (proxiesHeaderValue.size() == 0) {
                    xRequest.removeHeader(proxiesHeader);
                } else {
                    String commaDelimitedListOfProxies = listToCommaDelimitedString(proxiesHeaderValue);
                    xRequest.setHeader(proxiesHeader, commaDelimitedListOfProxies);
                }
                if (newRemoteIpHeaderValue.size() == 0) {
                    xRequest.removeHeader(remoteIPHeader);
                } else {
                    String commaDelimitedRemoteIpHeaderValue = listToCommaDelimitedString(newRemoteIpHeaderValue);
                    xRequest.setHeader(remoteIPHeader, commaDelimitedRemoteIpHeaderValue);
                }
            }
            
            if (protocolHeader != null) {
                String protocolHeaderValue = request.getHeader(protocolHeader);
                if (protocolHeaderValue == null) {
                    // don't modify the secure,scheme and serverPort attributes of the request
                } else if (protocolHeaderSslValue.equalsIgnoreCase(protocolHeaderValue)) {
                    xRequest.setSecure(true);
                    xRequest.setScheme("https");
                    xRequest.setServerPort(httpsServerPort);
                } else {
                    xRequest.setSecure(false);
                    xRequest.setScheme("http");
                    xRequest.setServerPort(httpServerPort);
                }

            }
            
           
            chain.doFilter(xRequest, response);
        } else {
            
            chain.doFilter(request, response);
        }
        
    }
    
    /**
     * Wrap the incoming <code>request</code> in a {@link XForwardedRequest} if the http header <code>x-forwareded-for</code> is not empty.
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            doFilter((HttpServletRequest)request, (HttpServletResponse)response, chain);
        } else {
            chain.doFilter(request, response);
        }
    }
    
    public int getHttpsServerPort() {
        return httpsServerPort;
    }
    

    public Pattern[] getInternalProxies() {
        return allowedInternalProxies;
    }
    
    public String getProtocolHeader() {
        return protocolHeader;
    }
    
    public String getProtocolHeaderSslValue() {
        return protocolHeaderSslValue;
    }
    
    public String getProxiesHeader() {
        return proxiesHeader;
    }
    
    public String getRemoteIPHeader() {
        return remoteIPHeader;
    }
    
    
    public Pattern[] getTrustedProxies() {
        return trustedProxies;
    }
    
    public void init(FilterConfig filterConfig) throws ServletException {
        if (filterConfig.getInitParameter(INTERNAL_PROXIES_PARAMETER) != null) {
            setAllowedInternalProxies(filterConfig.getInitParameter(INTERNAL_PROXIES_PARAMETER));
        }

        if (filterConfig.getInitParameter(PROTOCOL_HEADER_PARAMETER) != null) {
            setProtocolHeader(filterConfig.getInitParameter(PROTOCOL_HEADER_PARAMETER));
        }

        if (filterConfig.getInitParameter(PROTOCOL_HEADER_SSL_VALUE_PARAMETER) != null) {
            setProtocolHeaderSslValue(filterConfig.getInitParameter(PROTOCOL_HEADER_SSL_VALUE_PARAMETER));
        }

        if (filterConfig.getInitParameter(PROXIES_HEADER_PARAMETER) != null) {
            setProxiesHeader(filterConfig.getInitParameter(PROXIES_HEADER_PARAMETER));
        }

        if (filterConfig.getInitParameter(REMOTE_IP_HEADER_PARAMETER) != null) {
            setRemoteIPHeader(filterConfig.getInitParameter(REMOTE_IP_HEADER_PARAMETER));
        }

        if (filterConfig.getInitParameter(TRUSTED_PROXIES_PARAMETER) != null) {
            setTrustedProxies(filterConfig.getInitParameter(TRUSTED_PROXIES_PARAMETER));
        }

        if (filterConfig.getInitParameter(HTTP_SERVER_PORT_PARAMETER) != null) {
            try {
                setHttpServerPort(Integer.parseInt(filterConfig.getInitParameter(HTTP_SERVER_PORT_PARAMETER)));
            } catch (NumberFormatException e) {
                throw new NumberFormatException("Illegal " + HTTP_SERVER_PORT_PARAMETER + " : " + e.getMessage());
            }
        }

        if (filterConfig.getInitParameter(HTTPS_SERVER_PORT_PARAMETER) != null) {
            try {
                setHttpsServerPort(Integer.parseInt(filterConfig.getInitParameter(HTTPS_SERVER_PORT_PARAMETER)));
            } catch (NumberFormatException e) {
                throw new NumberFormatException("Illegal " + HTTPS_SERVER_PORT_PARAMETER + " : " + e.getMessage());
            }
        }
    }
    
    /**
     * <p>
     * Comma delimited list of internal proxies. Expressed with regular expressions.
     * </p>
     * <p>
     * Default value : 10\.\d{1,3}\.\d{1,3}\.\d{1,3}, 192\.168\.\d{1,3}\.\d{1,3}, 172\\.(?:1[6-9]|2\\d|3[0-1]).\\d{1,3}.\\d{1,3}, 
     * 169\.254\.\d{1,3}\.\d{1,3}, 127\.\d{1,3}\.\d{1,3}\.\d{1,3}
     * </p>
     */
    public void setAllowedInternalProxies(String allowedInternalProxies) {
        this.allowedInternalProxies = commaDelimitedListToPatternArray(allowedInternalProxies);
    }
    
    /**
     * <p>
     * Server Port value if the {@link #protocolHeader} does not indicate HTTPS
     * </p>
     * <p>
     * Default value : 80
     * </p>
     */
    public void setHttpServerPort(int httpServerPort) {
        this.httpServerPort = httpServerPort;
    }

    /**
     * <p>
     * Server Port value if the {@link #protocolHeader} indicates HTTPS
     * </p>
     * <p>
     * Default value : 443
     * </p>
     */
    public void setHttpsServerPort(int httpsServerPort) {
        this.httpsServerPort = httpsServerPort;
    }

    /**
     * <p>
     * Header that holds the incoming protocol, usally named <code>X-Forwarded-Proto</code>. If <code>null</code>, request.scheme and
     * request.secure will not be modified.
     * </p>
     * <p>
     * Default value : <code>null</code>
     * </p>
     */
    public void setProtocolHeader(String protocolHeader) {
        this.protocolHeader = protocolHeader;
    }
    
    /**
     * <p>
     * Case insensitive value of the protocol header to indicate that the incoming http request uses SSL.
     * </p>
     * <p>
     * Default value : <code>HTTPS</code>
     * </p>
     */
    public void setProtocolHeaderSslValue(String protocolHeaderSslValue) {
        this.protocolHeaderSslValue = protocolHeaderSslValue;
    }
    
    /**
     * <p>
     * The proxiesHeader directive specifies a header into which mod_remoteip will collect a list of all of the intermediate client IP
     * addresses trusted to resolve the actual remote IP. Note that intermediate RemoteIPTrustedProxy addresses are recorded in this header,
     * while any intermediate RemoteIPInternalProxy addresses are discarded.
     * </p>
     * <p>
     * Name of the http header that holds the list of trusted proxies that has been traversed by the http request.
     * </p>
     * <p>
     * The value of this header can be comma delimited.
     * </p>
     * <p>
     * Default value : <code>X-Forwarded-By</code>
     * </p>
     */
    public void setProxiesHeader(String proxiesHeader) {
        this.proxiesHeader = proxiesHeader;
    }
    
    /**
     * <p>
     * Name of the http header from which the remote ip is extracted.
     * </p>
     * <p>
     * The value of this header can be comma delimited.
     * </p>
     * <p>
     * Default value : <code>X-Forwarded-For</code>
     * </p>
     */
    public void setRemoteIPHeader(String remoteIPHeader) {
        this.remoteIPHeader = remoteIPHeader;
    }
    
    /**
     * <p>
     * Comma delimited list of proxies that are trusted when they appear in the {@link #remoteIPHeader} header. Can be expressed as a
     * regular expression.
     * </p>
     * <p>
     * Default value : empty list, no external proxy is trusted.
     * </p>
     */
    public void setTrustedProxies(String trustedProxies) {
        this.trustedProxies = commaDelimitedListToPatternArray(trustedProxies);
    }
}
