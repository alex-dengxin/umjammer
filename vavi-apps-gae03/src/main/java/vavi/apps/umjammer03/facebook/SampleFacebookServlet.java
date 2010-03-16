
package vavi.apps.umjammer03.facebook;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import com.google.code.facebookapi.FacebookXmlRestClient;


/**
 * @checked 2010-02-10
 */
@SuppressWarnings("serial")
public class SampleFacebookServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doService(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doService(req, resp);
    }

    private void doService(HttpServletRequest request, HttpServletResponse resp) throws IOException, ServletException {
        try {
            FacebookXmlRestClient client = FacebookUserFilter.getUserClient(request.getSession());
            Document response = client.friends_get();

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            DOMSource source = new DOMSource(response);
            StreamResult result = new StreamResult(resp.getOutputStream());
            Properties props = new Properties();
            props.setProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperties(props);
            transformer.transform(source, result);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
