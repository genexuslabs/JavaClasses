package com.genexus.filters;

/*
 * Copyright 2008-2009 the original author or authors.
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
import java.util.ArrayList;
import java.util.List;
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
 * Sets {@link RequestFacade#isSecure()} to <code>true</code> if
 * {@link ServletRequest#getRemoteAddr()} matches one of the
 * <code>securedRemoteAddresses</code> of this filter.
 * </p>
 * <p>
 * This filter is often preceded by the {@link XForwardedFilter} to get the
 * remote address of the client even if the request goes through load balancers
 * (e.g. F5 Big IP, Nortel Alteon) or proxies (e.g. Apache mod_proxy_http)
 * </p>
 * <p>
 * <strong>Configuration parameters:</strong>
 * <table border="1">
 * <tr>
 * <th>XForwardedFilter property</th>
 * <th>Description</th>
 * <th>Format</th>
 * <th>Default value</th>
 * </tr>
 * <tr>
 * <td>securedRemoteAddresses</td>
 * <td>IP addresses for which {@link ServletRequest#isSecure()} must return
 * <code>true</code></td>
 * <td>Comma delimited list of regular expressions (in the syntax supported by
 * the {@link java.util.regex.Pattern} library)</td>
 * <td>Class A, B and C <a
 * href="http://en.wikipedia.org/wiki/Private_network">private network IP
 * address blocks</a> : 10\.\d{1,3}\.\d{1,3}\.\d{1,3},
 * 192\.168\.\d{1,3}\.\d{1,3}, 172\\.(?:1[6-9]|2\\d|3[0-1]).\\d{1,3}.\\d{1,3},
 * 169\.254\.\d{1,3}\.\d{1,3}, 127\.\d{1,3}\.\d{1,3}\.\d{1,3}</td>
 * </tr>
 * </table>
 * Note : the default configuration is can usually be used as internal servers
 * are often trusted.
 * </p>
 * <p>
 * <strong>Sample with secured remote addresses limited to 192.168.0.10 and
 * 192.168.0.11</strong>
 * </p>
 * <p>
 * SecuredRemoteAddressFilter configuration sample :
 * </p>
 * 
 * <code><pre>
 * &lt;filter&gt;
 *    &lt;filter-name&gt;SecuredRemoteAddressFilter&lt;/filter-name&gt;
 *    &lt;filter-class&gt;fr.xebia.servlet.filter.SecuredRemoteAddressFilter&lt;/filter-class&gt;
 *    &lt;init-param&gt;
 *       &lt;param-name&gt;securedRemoteAddresses&lt;/param-name&gt;&lt;param-value&gt;192\.168\.0\.10, 192\.168\.0\.11&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 * &lt;/filter&gt;
 * 
 * &lt;filter-mapping&gt;
 *    &lt;filter-name&gt;SecuredRemoteAddressFilter&lt;/filter-name&gt;
 *    &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 *    &lt;dispatcher&gt;REQUEST&lt;/dispatcher&gt;
 * &lt;/filter-mapping&gt;</pre></code>
 * <p>
 * A request with
 * <code>{@link ServletRequest#getRemoteAddr()} = 192.168.0.10 or 192.168.0.11</code>
 * will be seen as <code>{@link ServletRequest#isSecure()} == true</code> even if
 * <code>{@link HttpServletRequest#getScheme()} == "http"</code>.
 * </p>
 * 
 * @author <a href="mailto:cyrille@cyrilleleclerc.com">Cyrille Le Clerc</a>
 */
public class SecuredRemoteAddressFilter implements Filter {

    protected final static String SECURED_REMOTE_ADDRESSES_PARAMETER = "securedRemoteAddresses";

    /**
     * {@link Pattern} for a comma delimited string that support whitespace
     * characters
     */
    private static final Pattern commaSeparatedValuesPattern = Pattern.compile("\\s*,\\s*");

    /**
     * Logger
     */


    /**
     * Convert a given comma delimited list of regular expressions into an array
     * of compiled {@link Pattern}
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
     * Convert a given comma delimited list of regular expressions into an array
     * of String
     */
    protected static String[] commaDelimitedListToStringArray(String commaDelimitedStrings) {
        return (commaDelimitedStrings == null || commaDelimitedStrings.length() == 0) ? new String[0] : commaSeparatedValuesPattern
                .split(commaDelimitedStrings);
    }

    /**
     * Return <code>true</code> if the given <code>str</code> matches at least
     * one of the given <code>patterns</code>.
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
     * @see #setSecuredRemoteAddresses(String)
     */
    private Pattern[] securedRemoteAddresses = new Pattern[] { Pattern.compile("10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"),
            Pattern.compile("192\\.168\\.\\d{1,3}\\.\\d{1,3}"), Pattern.compile("172\\.(?:1[6-9]|2\\d|3[0-1]).\\d{1,3}.\\d{1,3}"),
            Pattern.compile("169\\.254\\.\\d{1,3}\\.\\d{1,3}"), Pattern.compile("127\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}") };

    /**
     * Nothing to do. No resource to release.
     */
    public void destroy() {

    }

    /**
     * If incoming remote address matches one of the declared IP pattern, wraps
     * the incoming {@link HttpServletRequest} to override
     * {@link HttpServletRequest#isSecure()} to set it to <code>true</code>.
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        ServletRequest xRequest;
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            if (!request.isSecure() && matchesOne(request.getRemoteAddr(), securedRemoteAddresses)) {
                xRequest = new HttpServletRequestWrapper((HttpServletRequest) request) {
                    @Override
                    public boolean isSecure() {
                        return true;
                    }
                };
            } else {
                xRequest = request;
            }
           
        } else {
            xRequest = request;
        }

        chain.doFilter(xRequest, response);
    }

    /**
     * Compile the secured remote addresses patterns.
     */
    public void init(FilterConfig filterConfig) throws ServletException {
        String comaDelimitedSecuredRemoteAddresses = filterConfig.getInitParameter(SECURED_REMOTE_ADDRESSES_PARAMETER);
        if (comaDelimitedSecuredRemoteAddresses != null) {
            setSecuredRemoteAdresses(comaDelimitedSecuredRemoteAddresses);
        }
    }

    /**
     * <p>
     * Comma delimited list of secured remote addresses. Expressed with regular
     * expressions.
     * </p>
     * <p>
     * Default value : 10\.\d{1,3}\.\d{1,3}\.\d{1,3},
     * 192\.168\.\d{1,3}\.\d{1,3},
     * 172\\.(?:1[6-9]|2\\d|3[0-1]).\\d{1,3}.\\d{1,3},
     * 169\.254\.\d{1,3}\.\d{1,3}, 127\.\d{1,3}\.\d{1,3}\.\d{1,3}
     * </p>
     */
    public void setSecuredRemoteAdresses(String comaDelimitedSecuredRemoteAddresses) {
        this.securedRemoteAddresses = commaDelimitedListToPatternArray(comaDelimitedSecuredRemoteAddresses);

    }
}
