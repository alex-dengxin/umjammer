/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.enterprise.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;


/**
 * JSP をフォーマットするフィルタです。
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 060222 nsano initial version <br>
 */
public class JspFormatFilter implements Filter {

    /** */
    protected FilterConfig filterConfig = null;

    /** */
    private String encoding = "Windows-31J";

    /**
     * Take this filter out of service.
     */
    public void destroy() {
        this.filterConfig = null;
    }

    /**
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletResponseWrapper rw = new WeakReference<HttpServletResponseWrapper>(new CharArrayResponseWrapper((HttpServletResponse) response, encoding)).get();

        // Pass control on to the next filter
        chain.doFilter(request, rw);

        String output = rw.toString().replaceAll("^\\s*", "");
//System.err.println("JspFormatFilter::doFilter:\n" + output);
        PrintWriter out = response.getWriter();
        out.write(output);
        out.close();
    }

    /**
     * Place this filter into service.
     */
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
        String encoding = filterConfig.getInitParameter("encoding");
        if (encoding != null) {
            this.encoding = encoding;
        }
    }
}

/* */
