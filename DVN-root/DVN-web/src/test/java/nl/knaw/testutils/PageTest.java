package nl.knaw.testutils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/common-beans.xml", "classpath:/src/test/resources/servlet-test.xml" }, loader = WebAppLoader.class)
public class PageTest
{
    /** the servlet */
    @Autowired
    protected DispatcherServlet servlet;

    public void onStart() throws Exception
    {
        // obtain bean stored in the servlet's own context
        servlet.getWebApplicationContext().getBean("messages");
    }

    @Test
    public void welcomePageLoggedIn() throws Exception
    {
        HashMap<String, String> parameters = new HashMap<String, String>();
        MockHttpServletResponse response = process("/home.do", parameters, true);
        assertThat("ret", response, notNullValue());
        assertThat("status code", response.getStatus(), equalTo(200));
        assertThat("getErrorMessage", response.getErrorMessage(), nullValue());
        assertThat("redirect", response.getRedirectedUrl(), nullValue());
        String responseContentAsString = response.getContentAsString();
        assertThat("forward", response.getForwardedUrl(), equalTo("/jsp/tiles/mainLayout.jsp"));
    }

    protected MockHttpServletResponse process(String uri, Map<String, String> reqParams, boolean loggedIn)
            throws Exception
    {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", uri);
        MockHttpServletResponse res = new MockHttpServletResponse();

        //used for accessing request scoped proxied beans
        RequestContextHolder.setRequestAttributes(new ServletWebRequest(req, res));

        req.addParameters(reqParams);
        HttpSession session = req.getSession();
        if (loggedIn)
        {
            //set properties to indicate logged in
        }

        servlet.service(req, res);
        return res;
    }
}