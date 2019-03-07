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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;


/**
 * <p>
 * ExpiresFilter is a Java Servlet API port of Apache mod_expires to add '
 * <tt>Expires</tt>' and '<tt>Cache-Control: max-age=</tt>' headers to HTTP
 * response according to its '<tt>Content-Type</tt>'
 * </p>
 * 
 * <p>
 * Following documentation is inspired by <tt>mod_expires</tt> .
 * </p>
 * <h1>Summary</h1>
 * <p>
 * This filter controls the setting of the <tt>Expires</tt> HTTP header and the
 * <tt>max-age</tt> directive of the <tt>Cache-Control</tt> HTTP header in
 * server responses. The expiration date can set to be relative to either the
 * time the source file was last modified, or to the time of the client access.
 * </p>
 * <p>
 * These HTTP headers are an instruction to the client about the document&#x27;s
 * validity and persistence. If cached, the document may be fetched from the
 * cache rather than from the source until this time has passed. After that, the
 * cache copy is considered &quot;expired&quot; and invalid, and a new copy must
 * be obtained from the source.
 * </p>
 * <p>
 * To modify <tt>Cache-Control</tt> directives other than <tt>max-age</tt> (see
 * <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9" >RFC
 * 2616 section 14.9</a>), you can use other servlet filters or <a
 * href="http://httpd.apache.org/docs/2.2/mod/mod_headers.html" >Apache Httpd
 * mod_headers</a> module.
 * </p>
 * <h1>Filter Configuration</h1><h2>Basic configuration to add &#x27;
 * <tt>Expires</tt>&#x27; and &#x27; <tt>Cache-Control: max-age=</tt>&#x27;
 * headers to images, css and javascript</h2>
 * <p>
 * The filter will process the <tt>x-forwarded-for</tt> http header.
 * </p>
 * 
 * <code><pre>
 * &lt;web-app ...&gt;
 *    ...
 *    &lt;filter&gt;
 *       &lt;filter-name&gt;ExpiresFilter&lt;/filter-name&gt;
 *       &lt;filter-class&gt;fr.xebia.servlet.filter.ExpiresFilter&lt;/filter-class&gt;
 *       &lt;init-param&gt;
 *          &lt;param-name&gt;ExpiresByType image&lt;/param-name&gt;
 *          &lt;param-value&gt;access plus 10 minutes&lt;/param-value&gt;
 *       &lt;/init-param&gt;
 *       &lt;init-param&gt;
 *          &lt;param-name&gt;ExpiresByType text/css&lt;/param-name&gt;
 *          &lt;param-value&gt;access plus 10 minutes&lt;/param-value&gt;
 *       &lt;/init-param&gt;
 *       &lt;init-param&gt;
 *          &lt;param-name&gt;ExpiresByType text/javascript&lt;/param-name&gt;
 *          &lt;param-value&gt;access plus 10 minutes&lt;/param-value&gt;
 *       &lt;/init-param&gt;
 *    &lt;/filter&gt;
 *    ...
 *    &lt;filter-mapping&gt;
 *       &lt;filter-name&gt;ExpiresFilter&lt;/filter-name&gt;
 *       &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 *       &lt;dispatcher&gt;REQUEST&lt;/dispatcher&gt;
 *    &lt;/filter-mapping&gt;
 *    ...
 * &lt;/web-app&gt;
 * </pre></code>
 * 
 * <h2>Configuration Parameters</h2><h3><a name="ExpiresActive"/>
 * <tt>ExpiresActive</tt></h3>
 * <p>
 * This directive enables or disables the generation of the <tt>Expires</tt> and
 * <tt>Cache-Control</tt> headers by this <tt>ExpiresFilter</tt>. If set to
 * <tt>Off</tt>, the headers will not be generated for any HTTP response. If set
 * to <tt>On</tt> or <tt>true</tt>, the headers will be added to served HTTP
 * responses according to the criteria defined by the
 * <tt>ExpiresByType &lt;content-type&gt;</tt> and <tt>ExpiresDefault</tt>
 * directives. Note that this directive does not guarantee that an
 * <tt>Expires</tt> or <tt>Cache-Control</tt> header will be generated. If the
 * criteria aren&#x27;t met, no header will be sent, and the effect will be as
 * though this directive wasn&#x27;t even specified.
 * </p>
 * <p>
 * This parameter is optional, default value is <tt>true</tt>.
 * </p>
 * <p>
 * <i>Enable filter</i>
 * </p>
 * 
 * <code><pre>
 * &lt;init-param&gt;
 *    &lt;!-- supports case insensitive &#x27;On&#x27; or &#x27;true&#x27; --&gt;
 *    &lt;param-name&gt;ExpiresActive&lt;/param-name&gt;&lt;param-value&gt;On&lt;/param-value&gt;
 * &lt;/init-param&gt;
 * </pre></code>
 * <p>
 * <i>Disable filter</i>
 * </p>
 * 
 * <code><pre>
 * &lt;init-param&gt;
 *    &lt;!-- supports anything different from case insensitive &#x27;On&#x27; and &#x27;true&#x27; --&gt;
 *    &lt;param-name&gt;ExpiresActive&lt;/param-name&gt;&lt;param-value&gt;Off&lt;/param-value&gt;
 * &lt;/init-param&gt;
 * </pre></code>
 * 
 * <h3>
 * <tt>ExpiresByType &lt;content-type&gt;</tt></h3>
 * <p>
 * This directive defines the value of the <tt>Expires</tt> header and the
 * <tt>max-age</tt> directive of the <tt>Cache-Control</tt> header generated for
 * documents of the specified type (<i>e.g.</i>, <tt>text/html</tt>). The second
 * argument sets the number of seconds that will be added to a base time to
 * construct the expiration date. The <tt>Cache-Control: max-age</tt> is
 * calculated by subtracting the request time from the expiration date and
 * expressing the result in seconds.
 * </p>
 * <p>
 * The base time is either the last modification time of the file, or the time
 * of the client&#x27;s access to the document. Which should be used is
 * specified by the <tt>&lt;code&gt;</tt> field; <tt>M</tt> means that the
 * file&#x27;s last modification time should be used as the base time, and
 * <tt>A</tt> means the client&#x27;s access time should be used. The duration
 * is expressed in seconds. <tt>A2592000</tt> stands for
 * <tt>access plus 30 days</tt> in alternate syntax.
 * </p>
 * <p>
 * The difference in effect is subtle. If <tt>M</tt> (<tt>modification</tt> in
 * alternate syntax) is used, all current copies of the document in all caches
 * will expire at the same time, which can be good for something like a weekly
 * notice that&#x27;s always found at the same URL. If <tt>A</tt> (
 * <tt>access</tt> or <tt>now</tt> in alternate syntax) is used, the date of
 * expiration is different for each client; this can be good for image files
 * that don&#x27;t change very often, particularly for a set of related
 * documents that all refer to the same images (<i>i.e.</i>, the images will be
 * accessed repeatedly within a relatively short timespan).
 * </p>
 * <p>
 * <strong>Example:</strong>
 * </p>
 * 
 * <code><pre>
 * &lt;init-param&gt;
 *    &lt;param-name&gt;ExpiresByType text/html&lt;/param-name&gt;&lt;param-value&gt;access plus 1 month 15   days 2 hours&lt;/param-value&gt;
 * &lt;/init-param&gt;
 *  
 * &lt;init-param&gt;
 *    &lt;!-- 2592000 seconds = 30 days --&gt;
 *    &lt;param-name&gt;ExpiresByType image/gif&lt;/param-name&gt;&lt;param-value&gt;A2592000&lt;/param-value&gt;
 * &lt;/init-param&gt;
 * </pre></code>
 * <p>
 * Note that this directive only has effect if <tt>ExpiresActive On</tt> has
 * been specified. It overrides, for the specified MIME type <i>only</i>, any
 * expiration date set by the <tt>ExpiresDefault</tt> directive.
 * </p>
 * <p>
 * You can also specify the expiration time calculation using an alternate
 * syntax, described earlier in this document.
 * </p>
 * <h3>
 * <tt>ExpiresExcludedResponseStatusCodes</tt></h3>
 * <p>
 * This directive defines the http response status codes for which the
 * <tt>ExpiresFilter</tt> will not generate expiration headers. By default, the
 * <tt>304</tt> status code (&quot;<tt>Not modified</tt>&quot;) is skipped. The
 * value is a comma separated list of http status codes.
 * </p>
 * <p>
 * This directive is useful to ease usage of <tt>ExpiresDefault</tt> directive.
 * Indeed, the behavior of <tt>304 Not modified</tt> (which does specify a
 * <tt>Content-Type</tt> header) combined with <tt>Expires</tt> and
 * <tt>Cache-Control:max-age=</tt> headers can be unnecessarily tricky to
 * understand.
 * </p>
 * <p>
 * Configuration sample :
 * </p>
 * 
 * <code><pre>
 * &lt;init-param&gt;
 *    &lt;param-name&gt;ExpiresExcludedResponseStatusCodes&lt;/param-name&gt;&lt;param-value&gt;302, 500, 503&lt;/param-value&gt;
 * &lt;/init-param&gt;
 * </pre></code>
 * 
 * <h1>Alternate Syntax</h1>
 * <p>
 * The <tt>ExpiresDefault</tt> and <tt>ExpiresByType</tt> directives can also be
 * defined in a more readable syntax of the form:
 * </p>
 * 
 * <code><pre>
 * &lt;init-param&gt;
 *    &lt;param-name&gt;ExpiresDefault&lt;/param-name&gt;&lt;param-value&gt;&lt;base&gt; [plus] {&lt;num&gt;   &lt;type&gt;}*&lt;/param-value&gt;
 * &lt;/init-param&gt;
 *  
 * &lt;init-param&gt;
 *    &lt;param-name&gt;ExpiresByType type/encoding&lt;/param-name&gt;&lt;param-value&gt;&lt;base&gt; [plus]   {&lt;num&gt; &lt;type&gt;}*&lt;/param-value&gt;
 * &lt;/init-param&gt;
 * </pre></code>
 * <p>
 * where <tt>&lt;base&gt;</tt> is one of:
 * <ul>
 * <li><tt>access</tt></li>
 * <li><tt>now</tt> (equivalent to &#x27;<tt>access</tt>&#x27;)</li>
 * <li><tt>modification</tt></li>
 * </ul>
 * </p>
 * <p>
 * The <tt>plus</tt> keyword is optional. <tt>&lt;num&gt;</tt> should be an
 * integer value (acceptable to <tt>Integer.parseInt()</tt>), and
 * <tt>&lt;type&gt;</tt> is one of:
 * <ul>
 * <li><tt>years</tt></li>
 * <li><tt>months</tt></li>
 * <li><tt>weeks</tt></li>
 * <li><tt>days</tt></li>
 * <li><tt>hours</tt></li>
 * <li><tt>minutes</tt></li>
 * <li><tt>seconds</tt></li>
 * </ul>
 * For example, any of the following directives can be used to make documents
 * expire 1 month after being accessed, by default:
 * </p>
 * 
 * <code><pre>
 * &lt;init-param&gt;
 *    &lt;param-name&gt;ExpiresDefault&lt;/param-name&gt;&lt;param-value&gt;access plus 1 month&lt;/param-value&gt;
 * &lt;/init-param&gt;
 *  
 * &lt;init-param&gt;
 *    &lt;param-name&gt;ExpiresDefault&lt;/param-name&gt;&lt;param-value&gt;access plus 4 weeks&lt;/param-value&gt;
 * &lt;/init-param&gt;
 *  
 * &lt;init-param&gt;
 *    &lt;param-name&gt;ExpiresDefault&lt;/param-name&gt;&lt;param-value&gt;access plus 30 days&lt;/param-value&gt;
 * &lt;/init-param&gt;
 * </pre></code>
 * <p>
 * The expiry time can be fine-tuned by adding several &#x27;
 * <tt>&lt;num&gt; &lt;type&gt;</tt>&#x27; clauses:
 * </p>
 * 
 * <code><pre>
 * &lt;init-param&gt;
 *    &lt;param-name&gt;ExpiresByType text/html&lt;/param-name&gt;&lt;param-value&gt;access plus 1 month 15   days 2 hours&lt;/param-value&gt;
 * &lt;/init-param&gt;
 *  
 * &lt;init-param&gt;
 *    &lt;param-name&gt;ExpiresByType image/gif&lt;/param-name&gt;&lt;param-value&gt;modification plus 5 hours 3   minutes&lt;/param-value&gt;
 * &lt;/init-param&gt;
 * </pre></code>
 * <p>
 * Note that if you use a modification date based setting, the <tt>Expires</tt>
 * header will <strong>not</strong> be added to content that does not come from
 * a file on disk. This is due to the fact that there is no modification time
 * for such content.
 * </p>
 * <h1>Expiration headers generation eligibility</h1>
 * <p>
 * A response is eligible to be enriched by <tt>ExpiresFilter</tt> if :
 * <ol>
 * <li>no expiration header is defined (<tt>Expires</tt> header or the
 * <tt>max-age</tt> directive of the <tt>Cache-Control</tt> header),</li>
 * <li>the response status code is not excluded by the directive
 * <tt>ExpiresExcludedResponseStatusCodes</tt>,</li>
 * <li>The <tt>Content-Type</tt> of the response matches one of the types
 * defined the in <tt>ExpiresByType</tt> directives or the
 * <tt>ExpiresDefault</tt> directive is defined.</li>
 * </ol>
 * </p>
 * <p>
 * Note :
 * <ul>
 * <li>If <tt>Cache-Control</tt> header contains other directives than
 * <tt>max-age</tt>, they are concatenated with the <tt>max-age</tt> directive
 * that is added by the <tt>ExpiresFilter</tt>.</li>
 * </ul>
 * </p>
 * <h1>Expiration configuration selection</h1>
 * <p>
 * The expiration configuration if elected according to the following algorithm:
 * <ol>
 * <li><tt>ExpiresByType</tt> matching the exact content-type returned by
 * <tt>HttpServletResponse.getContentType()</tt> possibly including the charset
 * (e.g. &#x27;<tt>text/xml;charset=UTF-8</tt>&#x27;),</li>
 * <li><tt>ExpiresByType</tt> matching the content-type without the charset if
 * <tt>HttpServletResponse.getContentType()</tt> contains a charset (e.g. &#x27;
 * <tt>text/xml;charset=UTF-8</tt>&#x27; -&gt; &#x27;<tt>text/xml</tt>&#x27;),</li>
 * <li><tt>ExpiresByType</tt> matching the major type (e.g. substring before
 * &#x27;<tt>/</tt>&#x27;) of <tt>HttpServletResponse.getContentType()</tt>
 * (e.g. &#x27;<tt>text/xml;charset=UTF-8</tt>&#x27; -&gt; &#x27;<tt>text</tt>
 * &#x27;),</li>
 * <li><tt>ExpiresDefault</tt></li>
 * </ol>
 * </p>
 * <h1>Install / Download</h1>
 * <p>
 * <strong>1.0.3 is not yet available, please build the snapshot</strong>
 * </p>
 * <ul>
 * <li>Maven Project, add the following to <tt>pom.xml</tt></li>
 * 
 * <code><pre>
 * &lt;project ...&gt;
 *    ...
 *    &lt;repositories&gt;
 *       &lt;repository&gt;
 *         &lt;id&gt;xebia-france-googlecode-repository&lt;/id&gt;
 *         &lt;url&gt;http://xebia-france.googlecode.com/svn/repository/maven2/&lt;/url&gt;
 *       &lt;/repository&gt;
 *    &lt;/repositories&gt;
 *    ...
 *    &lt;dependencies&gt;
 *       ...
 *       &lt;dependency&gt;
 *          &lt;groupId&gt;fr.xebia.web.extras&lt;/groupId&gt;
 *          &lt;artifactId&gt;xebia-servlet-extras&lt;/artifactId&gt;
 *          &lt;version&gt;1.0.3&lt;/version&gt;
 *          &lt;scope&gt;runtime&lt;/scope&gt;
 *       &lt;/dependency&gt;
 *       ...
 *    &lt;/dependencies&gt;
 *    ...
 * &lt;/project&gt;
 * </pre></code>
 * 
 * <li>Jar : Drop the jar <a href=
 * "http://xebia-france.googlecode.com/files/xebia-servlet-extras-1.0.3.jar"
 * >xebia-servlet-extras-1.0.3.jar</a> (<a href="http://xebia-france.googlecode.com/files/xebia-servlet-extras-1.0.3-sources.jar"
 * >sources</a>) in your web application classpath.</li>
 * <li>Java class : <a href="http://xebia-france.googlecode.com/svn/web/xebia-servlet-extras/trunk/src/main/java/fr/xebia/servlet/filter/ExpiresFilter.java"
 * >ExpiresFilter.java</a></li>
 * <li>Java project :
 * <tt>svn checkouthttp://xebia-france.googlecode.com/svn/web/xebia-servlet-extras/tags/xebia-servlet-extras-1.0.3/</tt>
 * </li>
 * </ul>
 * <h1>Implementation Details</h1><h2>When to write the expiration headers ?</h2>
 * <p>
 * The <tt>ExpiresFilter</tt> traps the &#x27;on before write response
 * body&#x27; event to decide whether it should generate expiration headers or
 * not.
 * </p>
 * <p>
 * To trap the &#x27;before write response body&#x27; event, the
 * <tt>ExpiresFilter</tt> wraps the http servlet response&#x27;s writer and
 * outputStream to intercept calls to the methods <tt>write()</tt>,
 * <tt>print()</tt>, <tt>close()</tt> and <tt>flush()</tt>. For empty response
 * body (e.g. empty files), the <tt>write()</tt>, <tt>print()</tt>,
 * <tt>close()</tt> and <tt>flush()</tt> methods are not called; to handle this
 * case, the <tt>ExpiresFilter</tt>, at the end of its <tt>doFilter()</tt>
 * method, manually triggers the <tt>onBeforeWriteResponseBody()</tt> method.
 * </p>
 * <h2>Configuration syntax</h2>
 * <p>
 * The <tt>ExpiresFilter</tt> supports the same configuration syntax as Apache
 * Httpd mod_expires.
 * </p>
 * <p>
 * A challenge has been to choose the name of the <tt>&lt;param-name&gt;</tt>
 * associated with <tt>ExpiresByType</tt> in the <tt>&lt;filter&gt;</tt>
 * declaration. Indeed, Several <tt>ExpiresByType</tt> directives can be
 * declared when <tt>web.xml</tt> syntax does not allow to declare several
 * <tt>&lt;init-param&gt;</tt> with the same name.
 * </p>
 * <p>
 * The workaround has been to declare the content type in the
 * <tt>&lt;param-name&gt;</tt> rather than in the <tt>&lt;param-value&gt;</tt>.
 * </p>
 * <h2>Designed for extension : the open/close principle</h2>
 * <p>
 * The <tt>ExpiresFilter</tt> has been designed for extension following the
 * open/close principle.
 * </p>
 * <p>
 * Key methods to override for extension are :
 * <ul>
 * <li>
 * {@link #isEligibleToExpirationHeaderGeneration(HttpServletRequest, XHttpServletResponse)}
 * </li>
 * <li>
 * {@link #getExpirationDate(HttpServletRequest, XHttpServletResponse)}</li>
 * </ul>
 * </p>
 * <h1>Troubleshooting</h1>
 * <p>
 * To troubleshoot, enable logging on the
 * <tt>fr.xebia.servlet.filter.ExpiresFilter</tt>. Logging relies on SLF4J.
 * </p>
 * <p>
 * Extract of log4j.properties
 * </p>
 * 
 * <code><pre>
 * log4j.logger.fr.xebia.servlet.filter.ExpiresFilter = DEBUG
 * </pre></code>
 * <p>
 * Sample of initialization log message :
 * </p>
 * 
 * <code><pre>
 * 2010/03/24 18:18:12,637  INFO [main] fr.xebia.servlet.filter.ExpiresFilter - Filter initialized with configuration ExpiresFilter[
 *    active=true, 
 *    excludedResponseStatusCode=[304], 
 *    default=null, 
 *    byType={
 *       image=ExpiresConfiguration[startingPoint=ACCESS_TIME, duration=[10 MINUTE]], 
 *       text/css=ExpiresConfiguration[startingPoint=ACCESS_TIME, duration=[10 MINUTE]], 
 *       text/javascript=ExpiresConfiguration[startingPoint=ACCESS_TIME, duration=[10 MINUTE]], 
 *       text/html=ExpiresConfiguration[startingPoint=ACCESS_TIME, duration=[5 MINUTE]]}]
 * </pre></code>
 * <p>
 * Sample of per-request log message where <tt>ExpiresFilter</tt> adds an
 * expiration date
 * </p>
 * 
 * <code><pre>
 * 2010/03/24 18:43:34,586 DEBUG [http-8080-1] fr.xebia.servlet.filter.ExpiresFilter - 
 *    Request &#x27;/&#x27; with response status &#x27;200&#x27; content-type &#x27;text/html', set expiration date Wed Mar 24 18:48:34 CET 2010
 * </pre></code>
 * <p>
 * Sample of per-request log message where <tt>ExpiresFilter</tt> does not add
 * an expiration date
 * </p>
 * 
 * <code><pre>
 * 2010/03/24 18:43:34,564 DEBUG [http-8080-2] fr.xebia.servlet.filter.ExpiresFilter - 
 *    Request &#x27;/services/helloWorldService&#x27; with response status &#x27;200&#x27; content-type &#x27;text/xml;charset=UTF-8' status , no expiration configured
 * </pre></code>
 * 
 * 
 * @author <a href="mailto:cyrille@cyrilleleclerc.com">Cyrille Le Clerc</a>
 */
public class ExpiresFilter implements Filter {

    /**
     * Duration composed of an {@link #amount} and a {@link #unit}
     */
    protected static class Duration {

        public static Duration minutes(int amount) {
            return new Duration(amount, DurationUnit.MINUTE);
        }

        public static Duration seconds(int amount) {
            return new Duration(amount, DurationUnit.SECOND);
        }

        final protected int amount;

        final protected DurationUnit unit;

        public Duration(int amount, DurationUnit unit) {
            super();
            this.amount = amount;
            this.unit = unit;
        }

        public int getAmount() {
            return amount;
        }

        public DurationUnit getUnit() {
            return unit;
        }

        @Override
        public String toString() {
            return amount + " " + unit;
        }
    }

    /**
     * Duration unit
     */
    protected enum DurationUnit {
        DAY(Calendar.DAY_OF_YEAR), HOUR(Calendar.HOUR), MINUTE(Calendar.MINUTE), MONTH(Calendar.MONTH), SECOND(Calendar.SECOND), WEEK(
                Calendar.WEEK_OF_YEAR), YEAR(Calendar.YEAR);
        private final int calendardField;

        private DurationUnit(int calendardField) {
            this.calendardField = calendardField;
        }

        public int getCalendardField() {
            return calendardField;
        }

    }

    /**
     * <p>
     * Main piece of configuration of the filter.
     * </p>
     * <p>
     * Can be expressed like '<tt>access plus 1 month 15   days 2 hours</tt>'.
     * </p>
     */
    protected static class ExpiresConfiguration {
        /**
         * List of duration elements.
         */
        private List<Duration> durations;

        /**
         * Starting point of the elaspse to set in the response.
         */
        private StartingPoint startingPoint;

        public ExpiresConfiguration(StartingPoint startingPoint, Duration... durations) {
            this(startingPoint, Arrays.asList(durations));
        }

        public ExpiresConfiguration(StartingPoint startingPoint, List<Duration> durations) {
            super();
            this.startingPoint = startingPoint;
            this.durations = durations;
        }

        public List<Duration> getDurations() {
            return durations;
        }

        public StartingPoint getStartingPoint() {
            return startingPoint;
        }

        @Override
        public String toString() {
            return "ExpiresConfiguration[startingPoint=" + startingPoint + ", duration=" + durations + "]";
        }
    }

    /**
     * Expiration configuration starting point. Either the time the
     * html-page/servlet-response was served ({@link StartingPoint#ACCESS_TIME})
     * or the last time the html-page/servlet-response was modified (
     * {@link StartingPoint#LAST_MODIFICATION_TIME}).
     */
    protected enum StartingPoint {
        ACCESS_TIME, LAST_MODIFICATION_TIME
    }

    /**
     * <p>
     * Wrapping extension of the {@link HttpServletResponse} to yrap the
     * "Start Write Response Body" event.
     * </p>
     * <p>
     * For performance optimization, this extended response only holds the
     * {@link #lastModifiedHeader} and {@link #cacheControlHeader} instead of
     * holding the full list of headers.
     * </p>
     */
    public class XHttpServletResponse extends HttpServletResponseWrapper {

        /**
         * Value of the <tt>Cache-Control/tt> http response header if it has
         * been set.
         */
        private String cacheControlHeader;

        /**
         * Value of the <tt>Last-Modified</tt> http response header if it has
         * been set.
         */
        private long lastModifiedHeader;

        private PrintWriter printWriter;

        private HttpServletRequest request;

        private ServletOutputStream servletOutputStream;

        /**
         * <p>
         * Response status.
         * </p>
         * <p>
         * We store it because {@link HttpServletResponse} only exposes setters
         * but no getter.
         * </p>
         */
        private int status = HttpServletResponse.SC_OK;

        /**
         * Indicates whether calls to write methods (<tt>write(...)</tt>,
         * <tt>print(...)</tt>, etc) of the response body have been called or
         * not.
         */
        private boolean writeResponseBodyStarted;

        public XHttpServletResponse(HttpServletRequest request, HttpServletResponse response) {
            super(response);
            this.request = request;
        }

        @Override
        public void addDateHeader(String name, long date) {
            super.addDateHeader(name, date);
            if (!containsHeader(HEADER_LAST_MODIFIED)) {
                this.lastModifiedHeader = date;
            }
        }

        @Override
        public void addHeader(String name, String value) {
            super.addHeader(name, value);
            if (HEADER_CACHE_CONTROL.equalsIgnoreCase(name) && cacheControlHeader == null) {
                cacheControlHeader = value;
            }
        }

        public String getCacheControlHeader() {
            return cacheControlHeader;
        }

        public boolean isLastModifiedHeaderSet() {
            return containsHeader(HEADER_LAST_MODIFIED);
        }

        public long getLastModifiedHeader() {
            return this.lastModifiedHeader;
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            if (servletOutputStream == null) {
                servletOutputStream = new XServletOutputStream(super.getOutputStream(), request, this);
            }
            return servletOutputStream;
        }

        public int getStatus() {
            return status;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            if (printWriter == null) {
                printWriter = new XPrintWriter(super.getWriter(), request, this);
            }
            return printWriter;
        }

        public boolean isWriteResponseBodyStarted() {
            return writeResponseBodyStarted;
        }

        @Override
        public void sendError(int sc) throws IOException {
            this.status = sc;
            super.sendError(sc);
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            this.status = sc;
            super.sendError(sc, msg);
        }

        @Override
        public void sendRedirect(String location) throws IOException {
            this.status = HttpServletResponse.SC_FOUND;
            super.sendRedirect(location);
        }

        @Override
        public void setDateHeader(String name, long date) {
            super.setDateHeader(name, date);
            if (HEADER_LAST_MODIFIED.equalsIgnoreCase(name)) {
                this.lastModifiedHeader = date;
            }
        }

        @Override
        public void setHeader(String name, String value) {
            super.setHeader(name, value);
            if (HEADER_CACHE_CONTROL.equalsIgnoreCase(name)) {
                this.cacheControlHeader = value;
            }
        }

        @Override
        public void setStatus(int sc) {
            this.status = sc;
            super.setStatus(sc);
        }

        @Override
        public void setStatus(int sc, String sm) {
            this.status = sc;
            super.setStatus(sc, sm);
        }

        public void setWriteResponseBodyStarted(boolean writeResponseBodyStarted) {
            this.writeResponseBodyStarted = writeResponseBodyStarted;
        }
    }

    /**
     * Wrapping extension of {@link PrintWriter} to trap the
     * "Start Write Response Body" event.
     */
    public class XPrintWriter extends PrintWriter {
        private PrintWriter out;

        private HttpServletRequest request;

        private XHttpServletResponse response;

        public XPrintWriter(PrintWriter out, HttpServletRequest request, XHttpServletResponse response) {
            super(out);
            this.out = out;
            this.request = request;
            this.response = response;
        }

        public PrintWriter append(char c) {
            fireBeforeWriteResponseBodyEvent();
            return out.append(c);
        }

        public PrintWriter append(CharSequence csq) {
            fireBeforeWriteResponseBodyEvent();
            return out.append(csq);
        }

        public PrintWriter append(CharSequence csq, int start, int end) {
            fireBeforeWriteResponseBodyEvent();
            return out.append(csq, start, end);
        }

        public void close() {
            fireBeforeWriteResponseBodyEvent();
            out.close();
        }

        private void fireBeforeWriteResponseBodyEvent() {
            if (!this.response.isWriteResponseBodyStarted()) {
                this.response.setWriteResponseBodyStarted(true);
                onBeforeWriteResponseBody(request, response);
            }
        }

        public void flush() {
            fireBeforeWriteResponseBodyEvent();
            out.flush();
        }

        public void print(boolean b) {
            fireBeforeWriteResponseBodyEvent();
            out.print(b);
        }

        public void print(char c) {
            fireBeforeWriteResponseBodyEvent();
            out.print(c);
        }

        public void print(char[] s) {
            fireBeforeWriteResponseBodyEvent();
            out.print(s);
        }

        public void print(double d) {
            fireBeforeWriteResponseBodyEvent();
            out.print(d);
        }

        public void print(float f) {
            fireBeforeWriteResponseBodyEvent();
            out.print(f);
        }

        public void print(int i) {
            fireBeforeWriteResponseBodyEvent();
            out.print(i);
        }

        public void print(long l) {
            fireBeforeWriteResponseBodyEvent();
            out.print(l);
        }

        public void print(Object obj) {
            fireBeforeWriteResponseBodyEvent();
            out.print(obj);
        }

        public void print(String s) {
            fireBeforeWriteResponseBodyEvent();
            out.print(s);
        }

        public PrintWriter printf(Locale l, String format, Object... args) {
            fireBeforeWriteResponseBodyEvent();
            return out.printf(l, format, args);
        }

        public PrintWriter printf(String format, Object... args) {
            fireBeforeWriteResponseBodyEvent();
            return out.printf(format, args);
        }

        public void println() {
            fireBeforeWriteResponseBodyEvent();
            out.println();
        }

        public void println(boolean x) {
            fireBeforeWriteResponseBodyEvent();
            out.println(x);
        }

        public void println(char x) {
            fireBeforeWriteResponseBodyEvent();
            out.println(x);
        }

        public void println(char[] x) {
            fireBeforeWriteResponseBodyEvent();
            out.println(x);
        }

        public void println(double x) {
            fireBeforeWriteResponseBodyEvent();
            out.println(x);
        }

        public void println(float x) {
            fireBeforeWriteResponseBodyEvent();
            out.println(x);
        }

        public void println(int x) {
            fireBeforeWriteResponseBodyEvent();
            out.println(x);
        }

        public void println(long x) {
            fireBeforeWriteResponseBodyEvent();
            out.println(x);
        }

        public void println(Object x) {
            fireBeforeWriteResponseBodyEvent();
            out.println(x);
        }

        public void println(String x) {
            fireBeforeWriteResponseBodyEvent();
            out.println(x);
        }

        public void write(char[] buf) {
            fireBeforeWriteResponseBodyEvent();
            out.write(buf);
        }

        public void write(char[] buf, int off, int len) {
            fireBeforeWriteResponseBodyEvent();
            out.write(buf, off, len);
        }

        public void write(int c) {
            fireBeforeWriteResponseBodyEvent();
            out.write(c);
        }

        public void write(String s) {
            fireBeforeWriteResponseBodyEvent();
            out.write(s);
        }

        public void write(String s, int off, int len) {
            fireBeforeWriteResponseBodyEvent();
            out.write(s, off, len);
        }

    }

    /**
     * Wrapping extension of {@link ServletOutputStream} to trap the
     * "Start Write Response Body" event.
     */
    public class XServletOutputStream extends ServletOutputStream {

        private HttpServletRequest request;

        private XHttpServletResponse response;

        private ServletOutputStream servletOutputStream;

        public XServletOutputStream(ServletOutputStream servletOutputStream, HttpServletRequest request, XHttpServletResponse response) {
            super();
            this.servletOutputStream = servletOutputStream;
            this.response = response;
            this.request = request;
        }

        public void close() throws IOException {
            fireOnBeforeWriteResponseBodyEvent();
            servletOutputStream.close();
        }

        private void fireOnBeforeWriteResponseBodyEvent() {
            if (!this.response.isWriteResponseBodyStarted()) {
                this.response.setWriteResponseBodyStarted(true);
                onBeforeWriteResponseBody(request, response);
            }
        }

        public void flush() throws IOException {
            fireOnBeforeWriteResponseBodyEvent();
            servletOutputStream.flush();
        }

        public void print(boolean b) throws IOException {
            fireOnBeforeWriteResponseBodyEvent();
            servletOutputStream.print(b);
        }

        public void print(char c) throws IOException {
            fireOnBeforeWriteResponseBodyEvent();
            servletOutputStream.print(c);
        }

        public void print(double d) throws IOException {
            fireOnBeforeWriteResponseBodyEvent();
            servletOutputStream.print(d);
        }

        public void print(float f) throws IOException {
            fireOnBeforeWriteResponseBodyEvent();
            servletOutputStream.print(f);
        }

        public void print(int i) throws IOException {
            fireOnBeforeWriteResponseBodyEvent();
            servletOutputStream.print(i);
        }

        public void print(long l) throws IOException {
            fireOnBeforeWriteResponseBodyEvent();
            servletOutputStream.print(l);
        }

        public void print(String s) throws IOException {
            fireOnBeforeWriteResponseBodyEvent();
            servletOutputStream.print(s);
        }

        public void println() throws IOException {
            fireOnBeforeWriteResponseBodyEvent();
            servletOutputStream.println();
        }

        public void println(boolean b) throws IOException {
            fireOnBeforeWriteResponseBodyEvent();
            servletOutputStream.println(b);
        }

        public void println(char c) throws IOException {
            fireOnBeforeWriteResponseBodyEvent();
            servletOutputStream.println(c);
        }

        public void println(double d) throws IOException {
            fireOnBeforeWriteResponseBodyEvent();
            servletOutputStream.println(d);
        }

        public void println(float f) throws IOException {
            fireOnBeforeWriteResponseBodyEvent();
            servletOutputStream.println(f);
        }

        public void println(int i) throws IOException {
            fireOnBeforeWriteResponseBodyEvent();
            servletOutputStream.println(i);
        }

        public void println(long l) throws IOException {
            fireOnBeforeWriteResponseBodyEvent();
            servletOutputStream.println(l);
        }

        public void println(String s) throws IOException {
            fireOnBeforeWriteResponseBodyEvent();
            servletOutputStream.println(s);
        }

        public void write(byte[] b) throws IOException {
            fireOnBeforeWriteResponseBodyEvent();
            servletOutputStream.write(b);
        }

        public void write(byte[] b, int off, int len) throws IOException {
            fireOnBeforeWriteResponseBodyEvent();
            servletOutputStream.write(b, off, len);
        }

        public void write(int b) throws IOException {
            fireOnBeforeWriteResponseBodyEvent();
            servletOutputStream.write(b);
        }

    }

    /**
     * {@link Pattern} for a comma delimited string that support whitespace
     * characters
     */
    private static final Pattern commaSeparatedValuesPattern = Pattern.compile("\\s*,\\s*");

    private static final String HEADER_CACHE_CONTROL = "Cache-Control";

    private static final String HEADER_EXPIRES = "Expires";

    private static final String HEADER_LAST_MODIFIED = "Last-Modified";

    

    private static final String PARAMETER_EXPIRES_ACTIVE = "ExpiresActive";

    private static final String PARAMETER_EXPIRES_BY_TYPE = "ExpiresByType";

    private static final String PARAMETER_EXPIRES_DEFAULT = "ExpiresDefault";

    private static final String PARAMETER_EXPIRES_EXCLUDED_RESPONSE_STATUS_CODES = "ExpiresExcludedResponseStatusCodes";

    /**
     * Convert a comma delimited list of numbers into an <tt>int[]</tt>.
     * 
     * @param commaDelimitedInts
     *            can be <code>null</code>
     * @return never <code>null</code> array
     */
    protected static int[] commaDelimitedListToIntArray(String commaDelimitedInts) {
        String[] intsAsStrings = commaDelimitedListToStringArray(commaDelimitedInts);
        int[] ints = new int[intsAsStrings.length];
        for (int i = 0; i < intsAsStrings.length; i++) {
            String intAsString = intsAsStrings[i];
            try {
                ints[i] = Integer.parseInt(intAsString);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Exception parsing number '" + i + "' (zero based) of comma delimited list '"
                        + commaDelimitedInts + "'");
            }
        }
        return ints;
    }

    /**
     * Convert a given comma delimited list of strings into an array of String
     * 
     * @return array of patterns (non <code>null</code>)
     */
    protected static String[] commaDelimitedListToStringArray(String commaDelimitedStrings) {
        return (commaDelimitedStrings == null || commaDelimitedStrings.length() == 0) ? new String[0] : commaSeparatedValuesPattern
                .split(commaDelimitedStrings);
    }

    /**
     * Return <code>true</code> if the given <code>str</code> contains the given
     * <code>searchStr</code>.
     */
    protected static boolean contains(String str, String searchStr) {
        if (str == null || searchStr == null) {
            return false;
        }
        return str.indexOf(searchStr) >= 0;
    }

    /**
     * Convert an array of ints into a comma delimited string
     */
    protected static String intsToCommaDelimitedString(int[] ints) {
        if (ints == null) {
            return "";
        }

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < ints.length; i++) {
            result.append(ints[i]);
            if (i < (ints.length - 1)) {
                result.append(", ");
            }
        }
        return result.toString();
    }

    /**
     * Return <code>true</code> if the given <code>str</code> is
     * <code>null</code> or has a zero characters length.
     */
    protected static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * Return <code>true</code> if the given <code>str</code> has at least one
     * character (can be a withespace).
     */
    protected static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * Return <code>true</code> if the given <code>string</code> starts with the
     * given <code>prefix</code> ignoring case.
     * 
     * @param string
     *            can be <code>null</code>
     * @param prefix
     *            can be <code>null</code>
     */
    protected static boolean startsWithIgnoreCase(String string, String prefix) {
        if (string == null || prefix == null) {
            return string == null && prefix == null;
        }
        if (prefix.length() > string.length()) {
            return false;
        }

        return string.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    /**
     * Return the subset of the given <code>str</code> that is before the first
     * occurence of the given <code>separator</code>. Return <code>null</code>
     * if the given <code>str</code> or the given <code>separator</code> is
     * null. Return and empty string if the <code>separator</code> is empty.
     * 
     * @param str
     *            can be <code>null</code>
     * @param separator
     *            can be <code>null</code>
     * @return
     */
    protected static String substringBefore(String str, String separator) {
        if (isEmpty(str) || separator == null) {
            return null;
        }

        if (separator.length() == 0) {
            return "";
        }

        int separatorIndex = str.indexOf(separator);
        if (separatorIndex == -1) {
            return str;
        }
        return str.substring(0, separatorIndex);
    }

    /**
     * @see #isActive()
     */
    private boolean active = true;

    /**
     * Default Expires configuration.
     */
    private ExpiresConfiguration defaultExpiresConfiguration;

    /**
     * list of response status code for which the {@link ExpiresFilter} will not
     * generate expiration headers.
     */
    private int[] excludedResponseStatusCodes = new int[] { HttpServletResponse.SC_NOT_MODIFIED };

    /**
     * Expires configuration by content type. Visible for test.
     */
    private Map<String, ExpiresConfiguration> expiresConfigurationByContentType = new LinkedHashMap<String, ExpiresConfiguration>();

    public void destroy() {

    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            if (response.isCommitted()) {
                
                chain.doFilter(request, response);
            } else if (active) {
                XHttpServletResponse xResponse = new XHttpServletResponse(httpRequest, httpResponse);
                chain.doFilter(request, xResponse);
                if (!xResponse.isWriteResponseBodyStarted()) {
                    // Empty response, manually trigger
                    // onBeforeWriteResponseBody()
                    onBeforeWriteResponseBody(httpRequest, xResponse);
                }
            } else {
               
                chain.doFilter(request, response);
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    public ExpiresConfiguration getDefaultExpiresConfiguration() {
        return defaultExpiresConfiguration;
    }

    public String getExcludedResponseStatusCodes() {
        return intsToCommaDelimitedString(excludedResponseStatusCodes);
    }


    public int[] getExcludedResponseStatusCodesAsInts() {
        return excludedResponseStatusCodes;
    }

    /**
     * <p>
     * Returns the expiration date of the given {@link XHttpServletResponse} or
     * <code>null</code> if no expiration date has been configured for the
     * declared content type.
     * </p>
     * <p>
     * <code>protected</code> for extension.
     * </p>
     * 
     * @see HttpServletResponse#getContentType()
     */
    protected Date getExpirationDate(HttpServletRequest request, XHttpServletResponse response) {
        String contentType = response.getContentType();

        // lookup exact content-type match (e.g.
        // "text/html; charset=iso-8859-1")
        ExpiresConfiguration configuration = expiresConfigurationByContentType.get(contentType);
        String matchingContentType = contentType;

        if (configuration == null && contains(contentType, ";")) {
            // lookup content-type without charset match (e.g. "text/html")
            String contentTypeWithoutCharset = substringBefore(contentType, ";").trim();
            configuration = expiresConfigurationByContentType.get(contentTypeWithoutCharset);
            matchingContentType = contentTypeWithoutCharset;
        }

        if (configuration == null && contains(contentType, "/")) {
            // lookup content-type without charset match (e.g. "text/html")
            String majorType = substringBefore(contentType, "/");
            configuration = expiresConfigurationByContentType.get(majorType);
            matchingContentType = majorType;
        }

        if (configuration == null) {
            configuration = defaultExpiresConfiguration;
            matchingContentType = "#DEFAULT#";
        }

        if (configuration == null) {
           
            return null;
        }

        

        Calendar calendar;
        switch (configuration.getStartingPoint()) {
        case ACCESS_TIME:
            calendar = GregorianCalendar.getInstance();
            break;
        case LAST_MODIFICATION_TIME:
            if (response.isLastModifiedHeaderSet()) {
                long lastModified = response.getLastModifiedHeader();
                calendar = GregorianCalendar.getInstance();
                calendar.setTimeInMillis(lastModified);
            } else {
                // Last-Modified header not found, use now
                calendar = GregorianCalendar.getInstance();
            }
            break;
        default:
            throw new IllegalStateException("Unsupported startingPoint '" + configuration.getStartingPoint() + "'");
        }
        for (Duration duration : configuration.getDurations()) {
            calendar.add(duration.getUnit().getCalendardField(), duration.getAmount());
        }

        return calendar.getTime();
    }

    public Map<String, ExpiresConfiguration> getExpiresConfigurationByContentType() {
        return expiresConfigurationByContentType;
    }

    @SuppressWarnings("unchecked")
    public void init(FilterConfig filterConfig) throws ServletException {
        for (Enumeration<String> names = filterConfig.getInitParameterNames(); names.hasMoreElements();) {
            String name = names.nextElement();
            String value = filterConfig.getInitParameter(name);

            try {
                if (name.startsWith(PARAMETER_EXPIRES_BY_TYPE)) {
                    String contentType = name.substring(PARAMETER_EXPIRES_BY_TYPE.length()).trim();
                    ExpiresConfiguration expiresConfiguration = parseExpiresConfiguration(value);
                    this.expiresConfigurationByContentType.put(contentType, expiresConfiguration);
                } else if (name.equalsIgnoreCase(PARAMETER_EXPIRES_DEFAULT)) {
                    ExpiresConfiguration expiresConfiguration = parseExpiresConfiguration(value);
                    this.defaultExpiresConfiguration = expiresConfiguration;
                } else if (name.equalsIgnoreCase(PARAMETER_EXPIRES_ACTIVE)) {
                    this.active = "On".equalsIgnoreCase(value) || Boolean.valueOf(value);
                } else if (name.equalsIgnoreCase(PARAMETER_EXPIRES_EXCLUDED_RESPONSE_STATUS_CODES)) {
                    this.excludedResponseStatusCodes = commaDelimitedListToIntArray(value);
                } else {

                }
            } catch (RuntimeException e) {
                throw new ServletException("Exception processing configuration parameter '" + name + "':'" + value + "'", e);
            }
        }

        
    }

    /**
     * Indicates that the filter is active. If <code>false</code>, the filter is
     * pass-through. Default is <code>true</code>.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * 
     * <p>
     * <code>protected</code> for extension.
     * </p>
     */
    protected boolean isEligibleToExpirationHeaderGeneration(HttpServletRequest request, XHttpServletResponse response) {
        boolean expirationHeaderHasBeenSet = response.containsHeader(HEADER_EXPIRES)
                || contains(response.getCacheControlHeader(), "max-age");
        if (expirationHeaderHasBeenSet) {
           
            return false;
        }

        for (int skippedStatusCode : this.excludedResponseStatusCodes) {
            if (response.getStatus() == skippedStatusCode) {
               
                return false;
            }
        }

        return true;
    }

    /**
     * <p>
     * If no expiration header has been set by the servlet and an expiration has
     * been defined in the {@link ExpiresFilter} configuration, sets the '
     * <tt>Expires</tt>' header and the attribute '<tt>max-age</tt>' of the '
     * <tt>Cache-Control</tt>' header.
     * </p>
     * <p>
     * Must be called on the "Start Write Response Body" event.
     * </p>
     * <p>
     * Invocations to <tt>Logger.debug(...)</tt> are guarded by
     * {@link Logger#isDebugEnabled()} because
     * {@link HttpServletRequest#getRequestURI()} and
     * {@link HttpServletResponse#getContentType()} costs <tt>String</tt>
     * objects instantiations (as of Tomcat 7).
     * </p>
     */
    public void onBeforeWriteResponseBody(HttpServletRequest request, XHttpServletResponse response) {

        if (!isEligibleToExpirationHeaderGeneration(request, response)) {
            return;
        }

        Date expirationDate = getExpirationDate(request, response);
        if (expirationDate == null) {
            
        } else {
            

            String maxAgeDirective = "max-age=" + ((expirationDate.getTime() - System.currentTimeMillis()) / 1000);

            String cacheControlHeader = response.getCacheControlHeader();
            String newCacheControlHeader = (cacheControlHeader == null) ? maxAgeDirective : cacheControlHeader + ", " + maxAgeDirective;
            response.setHeader(HEADER_CACHE_CONTROL, newCacheControlHeader);
            response.setDateHeader(HEADER_EXPIRES, expirationDate.getTime());
        }

    }

    /**
     * Parse configuration lines like '
     * <tt>access plus 1 month 15 days 2 hours</tt>' or '
     * <tt>modification 1 day 2 hours 5 seconds</tt>'
     * 
     * @param line
     */
    protected ExpiresConfiguration parseExpiresConfiguration(String line) {
        line = line.trim();

        StringTokenizer tokenizer = new StringTokenizer(line, " ");

        String currentToken;

        try {
            currentToken = tokenizer.nextToken();
        } catch (NoSuchElementException e) {
            throw new IllegalStateException("Starting point (access|now|modification|a<seconds>|m<seconds>) not found in directive '"
                    + line + "'");
        }

        StartingPoint startingPoint;
        if ("access".equalsIgnoreCase(currentToken) || "now".equalsIgnoreCase(currentToken)) {
            startingPoint = StartingPoint.ACCESS_TIME;
        } else if ("modification".equalsIgnoreCase(currentToken)) {
            startingPoint = StartingPoint.LAST_MODIFICATION_TIME;
        } else if (!tokenizer.hasMoreTokens() && startsWithIgnoreCase(currentToken, "a")) {
            startingPoint = StartingPoint.ACCESS_TIME;
            // trick : convert duration configuration from old to new style
            tokenizer = new StringTokenizer(currentToken.substring(1) + " seconds", " ");
        } else if (!tokenizer.hasMoreTokens() && startsWithIgnoreCase(currentToken, "m")) {
            startingPoint = StartingPoint.LAST_MODIFICATION_TIME;
            // trick : convert duration configuration from old to new style
            tokenizer = new StringTokenizer(currentToken.substring(1) + " seconds", " ");
        } else {
            throw new IllegalStateException("Invalid starting point (access|now|modification|a<seconds>|m<seconds>) '" + currentToken
                    + "' in directive '" + line + "'");
        }

        try {
            currentToken = tokenizer.nextToken();
        } catch (NoSuchElementException e) {
            throw new IllegalStateException("Duration not found in directive '" + line + "'");
        }

        if ("plus".equalsIgnoreCase(currentToken)) {
            // skip
            try {
                currentToken = tokenizer.nextToken();
            } catch (NoSuchElementException e) {
                throw new IllegalStateException("Duration not found in directive '" + line + "'");
            }
        }

        List<Duration> durations = new ArrayList<Duration>();

        while (currentToken != null) {
            int amount;
            try {
                amount = Integer.parseInt(currentToken);
            } catch (NumberFormatException e) {
                throw new IllegalStateException("Invalid duration (number) '" + currentToken + "' in directive '" + line + "'");
            }

            try {
                currentToken = tokenizer.nextToken();
            } catch (NoSuchElementException e) {
                throw new IllegalStateException("Duration unit not found after amount " + amount + " in directive '" + line + "'");
            }
            DurationUnit durationUnit;
            if ("years".equalsIgnoreCase(currentToken)) {
                durationUnit = DurationUnit.YEAR;
            } else if ("month".equalsIgnoreCase(currentToken) || "months".equalsIgnoreCase(currentToken)) {
                durationUnit = DurationUnit.MONTH;
            } else if ("week".equalsIgnoreCase(currentToken) || "weeks".equalsIgnoreCase(currentToken)) {
                durationUnit = DurationUnit.WEEK;
            } else if ("day".equalsIgnoreCase(currentToken) || "days".equalsIgnoreCase(currentToken)) {
                durationUnit = DurationUnit.DAY;
            } else if ("hour".equalsIgnoreCase(currentToken) || "hours".equalsIgnoreCase(currentToken)) {
                durationUnit = DurationUnit.HOUR;
            } else if ("minute".equalsIgnoreCase(currentToken) || "minutes".equalsIgnoreCase(currentToken)) {
                durationUnit = DurationUnit.MINUTE;
            } else if ("second".equalsIgnoreCase(currentToken) || "seconds".equalsIgnoreCase(currentToken)) {
                durationUnit = DurationUnit.SECOND;
            } else {
                throw new IllegalStateException("Invalid duration unit (years|months|weeks|days|hours|minutes|seconds) '" + currentToken
                        + "' in directive '" + line + "'");
            }

            Duration duration = new Duration(amount, durationUnit);
            durations.add(duration);

            if (tokenizer.hasMoreTokens()) {
                currentToken = tokenizer.nextToken();
            } else {
                currentToken = null;
            }
        }

        return new ExpiresConfiguration(startingPoint, durations);
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setDefaultExpiresConfiguration(ExpiresConfiguration defaultExpiresConfiguration) {
        this.defaultExpiresConfiguration = defaultExpiresConfiguration;
    }


    public void setExcludedResponseStatusCodes(int[] excludedResponseStatusCodes) {
        this.excludedResponseStatusCodes = excludedResponseStatusCodes;
    }

    public void setExpiresConfigurationByContentType(Map<String, ExpiresConfiguration> expiresConfigurationByContentType) {
        this.expiresConfigurationByContentType = expiresConfigurationByContentType;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[active=" + this.active + ", excludedResponseStatusCode=["
                + intsToCommaDelimitedString(this.excludedResponseStatusCodes) + "], default=" + this.defaultExpiresConfiguration
                + ", byType=" + this.expiresConfigurationByContentType + "]";
    }
}
