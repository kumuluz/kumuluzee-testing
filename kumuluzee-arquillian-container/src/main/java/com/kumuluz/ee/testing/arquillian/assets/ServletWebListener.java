package com.kumuluz.ee.testing.arquillian.assets;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.WebListener;

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
