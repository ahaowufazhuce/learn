package org.geektimes.web.mvc;

import org.jolokia.http.AgentServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

/**
 * JolokiaServlet
 *
 * @author pengxing on 2021/3/15
 */
public class JolokiaServlet extends AgentServlet {
    private static final long serialVersionUID = -5363863411895449519L;

    @Override
    public void init(ServletConfig pServletConfig) throws ServletException {
        super.init(pServletConfig);
    }

}