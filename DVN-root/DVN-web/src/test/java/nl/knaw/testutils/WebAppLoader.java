package nl.knaw.testutils;

import org.apache.log4j.Logger;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.support.AbstractContextLoader;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;

/**
 * @author Chris Watts
 *
 */
class WebAppLoader extends AbstractContextLoader
{
    /** Application root directory (usually project/web). */
    protected String basePath = "src/main/webapp";
    /**
     * set to false for each servlet to load an additional context (the way
     * {@link DispatcherServlet} works by default). Useful if you want to load
     * the servlet's context in the main context.
     */
    protected boolean shareContext = false;
    private static final Logger logger = Logger.getLogger(WebAppLoader.class);
    /** attribute name to pass context via */
    private static final String CONTEXT_PATH = "com.example.WebApplicationContext";

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.test.context.support.AbstractContextLoader#getResourceSuffix()
     */
    @Override
    protected String getResourceSuffix()
    {
        return "-context.xml";
    }

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.test.context.ContextLoader#loadContext(java.lang.String[])
     */
    @SuppressWarnings("unchecked")
    public XmlWebApplicationContext loadContext(String... locations) throws Exception
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Loading ApplicationContext for locations ["
                    + StringUtils.arrayToCommaDelimitedString(locations) + "].");
        }

        //resolve absolute path
        File path = ResourceUtils.getFile(basePath);
        if (!path.exists())
            throw new FileNotFoundException("base path does not exist; " + basePath);
        basePath = "file:" + path.getAbsolutePath();

        XmlWebApplicationContext appContext = new XmlWebApplicationContext();

        MockServletContext servletContext = new MockServletContext(basePath);
        servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, appContext);

        if (shareContext)
        {
            servletContext.setAttribute(CONTEXT_PATH, appContext);
        }

        appContext.setServletContext(servletContext);
        appContext.setConfigLocations(locations);
        appContext.refresh();
        appContext.registerShutdownHook();

        Map<String, DispatcherServlet> map = appContext.getBeansOfType(DispatcherServlet.class);
        for (Map.Entry<String, DispatcherServlet> entry : map.entrySet())
        {
            DispatcherServlet servlet = entry.getValue();
            String servletName = entry.getKey();

            if (shareContext)
                servlet.setContextAttribute(CONTEXT_PATH);

            MockServletConfig servletConfig = new MockServletConfig(servletContext, servletName);
            servlet.init(servletConfig);
        }

        return appContext;
    }
}