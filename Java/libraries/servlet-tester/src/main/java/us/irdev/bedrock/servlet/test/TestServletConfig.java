package us.irdev.bedrock.servlet.test;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import java.util.Enumeration;

public class TestServletConfig implements ServletConfig {
    us.irdev.bedrock.servlet.test.TestServletContext servletContext;
    String name;

    public TestServletConfig () {
        this ("Test");
    }

    public TestServletConfig (String name) {
        this.name = name;
        servletContext = new us.irdev.bedrock.servlet.test.TestServletContext ();
    }

    @Override
    public String getServletName () {
        return name;
    }

    @Override
    public ServletContext getServletContext () {
        return servletContext;
    }

    @Override
    public String getInitParameter (String s) {
        return null;
    }

    @Override
    public Enumeration<String> getInitParameterNames () {
        return null;
    }
}
