/*
 *  Copyright (c) 2014-2017 Kumuluz and/or its affiliates
 *  and other contributors as indicated by the @author tags and
 *  the contributor list.
 *
 *  Licensed under the MIT License (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://opensource.org/licenses/MIT
 *
 *  The software is provided "AS IS", WITHOUT WARRANTY OF ANY KIND, express or
 *  implied, including but not limited to the warranties of merchantability,
 *  fitness for a particular purpose and noninfringement. in no event shall the
 *  authors or copyright holders be liable for any claim, damages or other
 *  liability, whether in an action of contract, tort or otherwise, arising from,
 *  out of or in connection with the software or the use or other dealings in the
 *  software. See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.kumuluz.ee.testing.arquillian.assets;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.WebListener;

/**
 * Adds Arquillian Servlet mapping to the web container.
 *
 * @author Urban Malc
 * @since 1.0.0
 */
@WebListener
public class ServletWebListener implements ServletContextListener {

    private static final String ARQUILLIAN_SERVLET_NAME = "ArquillianServletRunner";
    private static final String ARQUILLIAN_SERVLET_MAPPING = "/ArquillianServletRunner";
    private static final String ARQUILLIAN_SERVLET_CLASS_NAME = "org.jboss.arquillian.protocol.servlet.runner." +
            "ServletTestRunner";

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext context = servletContextEvent.getServletContext();
        if (isClassPresent(ARQUILLIAN_SERVLET_CLASS_NAME) &&
                context.getServletRegistration(ARQUILLIAN_SERVLET_NAME) == null) {
            ServletRegistration sr = context.addServlet(ARQUILLIAN_SERVLET_NAME, ARQUILLIAN_SERVLET_CLASS_NAME);
            sr.addMapping(ARQUILLIAN_SERVLET_MAPPING);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        // NO-OP
    }

    private boolean isClassPresent(String classname) {
        try {
            Class.forName(classname);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
