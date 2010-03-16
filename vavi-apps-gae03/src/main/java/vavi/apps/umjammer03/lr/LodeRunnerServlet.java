
package vavi.apps.umjammer03.lr;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestHandler;

import vavi.util.openfeint.jaxb.highscore.ResourceSections;
import vavi.util.openfeint.jaxb.highscore.ResourceSections.ResourceSection;
import vavi.util.openfeint.jaxb.highscore.ResourceSections.ResourceSection.Resources;
import vavi.util.openfeint.jaxb.highscore.ResourceSections.ResourceSection.Resources.HighScore;


/**
 * Sample of Spring Servlet injection
 */
@Component("LodeRunnerServlet")
public class LodeRunnerServlet implements HttpRequestHandler {

    @Autowired(required = true)
    LodeRunnerService loderunnerService;
    
    @Override
    public void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String uri = req.getRequestURI();
System.err.println("uri: " + uri);

            if (uri.equals("/lr/get")) {
    
                String mixi = req.getParameter("mixi");
        
                String feint = loderunnerService.get(mixi);
    
                PrintWriter out = resp.getWriter();
                out.print(feint);
                out.flush();
                
            } else if (uri.equals("/lr/add")) {

                String mixi = req.getParameter("mixi");
                String feint = req.getParameter("feint");
        
                loderunnerService.add(feint, mixi);

                PrintWriter out = resp.getWriter();
                out.print("<Html>OK</html>");
                out.flush();

            } else if (uri.equals("/lr/list")) {

                List<MixiFeint> mixiFeints = loderunnerService.list();

                PrintWriter out = resp.getWriter();
                out.print("<Html>");
                out.print("<body>");
                for (MixiFeint mixiFeint : mixiFeints) {
                    out.print(mixiFeint.getMixi());
                    out.print(":");
                    out.print(mixiFeint.getFeint());
                    out.print("<br/>");
                }
                out.print("</body>");
                out.print("</html>");
                out.flush();

            } else if (uri.equals("/lr/score")) {

                String mixi = req.getParameter("mixi");
                long score = Long.parseLong(req.getParameter("score"));

                loderunnerService.score(mixi, score);

                PrintWriter out = resp.getWriter();
                out.print("<html><body>OK</body></html>");
                out.flush();

            } else if (uri.equals("/lr/highscore")) {

                String mixi = req.getParameter("mixi");

                ResourceSections rss = loderunnerService.highscore(mixi);

                PrintWriter out = resp.getWriter();
                out.print("<html>");
                out.print("<head>");
                out.print("<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>");
                out.print("</head>");
                out.print("<body>");
                out.print("<table>");
                out.print("<tr>");
                out.print("<th colspan='4'>");
                out.print("Your Ranking");
                out.print("</th>");
                out.print("</tr>");
                for (ResourceSection rs : rss.getResourceSection()) {
                    String type = rs.getName();
                    if (type.equals("My Score")) {
                        Resources r = rs.getResources();
                        for (HighScore hs : r.getHighScore()) {
                            out.print("<tr>");
                            out.print("<td>");
                            out.print(hs.getRank().longValue());
                            out.print("</td>");
                            out.print("<td>");
                            out.print("<img src=" + hs.getUser().getProfilePictureUrl() + ">");
                            out.print("</td>");
                            out.print("<td>");
                            out.print(hs.getUser().getName());
                            out.print("</td>");
                            out.print("<td>");
                            out.print(hs.getScore().longValue());
                            out.print("</td>");
                            out.print("</tr>");
                        }
                    }
                }
                out.print("</table>");
                out.print("<hr/>");
                out.print("<table>");
                out.print("<tr>");
                out.print("<th colspan='4'>");
                out.print("Ranking");
                out.print("</th>");
                out.print("</tr>");
                for (ResourceSection rs : rss.getResourceSection()) {
                    String type = rs.getName();
                    if (type.equals("Everyone")) {
                        Resources r = rs.getResources();
                        for (HighScore hs : r.getHighScore()) {
                            out.print("<tr>");
                            out.print("<td>");
                            out.print(hs.getRank().longValue());
                            out.print("</td>");
                            out.print("<td>");
                            out.print("<img src=" + hs.getUser().getProfilePictureUrl() + ">");
                            out.print("</td>");
                            out.print("<td>");
                            out.print(hs.getUser().getName());
                            out.print("</td>");
                            out.print("<td>");
                            out.print(hs.getScore().longValue());
                            out.print("</td>");
                            out.print("</tr>");
                        }
                    }
                }
                out.print("</table>");
                out.print("</body>");
                out.print("</html>");
                out.flush();

            } else {

                throw new ServletException("unknown command: " + uri);

            }

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
