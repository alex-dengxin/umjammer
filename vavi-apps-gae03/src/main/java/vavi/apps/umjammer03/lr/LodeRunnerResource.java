/*
 * Copyright (c) 2010 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.umjammer03.lr;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import vavi.util.openfeint.jaxb.highscore.ResourceSections;
import vavi.util.openfeint.jaxb.highscore.ResourceSections.ResourceSection;
import vavi.util.openfeint.jaxb.highscore.ResourceSections.ResourceSection.Resources;
import vavi.util.openfeint.jaxb.highscore.ResourceSections.ResourceSection.Resources.HighScore;


/**
 * JSR-311 resources. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2010/02/19 nsano initial version <br>
 */
@Path("/")
@Component
@Scope("request")
public class LodeRunnerResource {

    @Autowired(required = true)
    LodeRunnerService loderunnerService;

    /**
     */
    @GET
    @Path("get/{mixi}")
    @Produces("text/plain")
    public String get(@PathParam("mixi") String mixi) {
        return loderunnerService.get(mixi);
    }                

    @GET
    @Path("add/{mixi}/{feint}")
    @Produces("text/plain")
    public String add(@PathParam("mixi") String mixi, @PathParam("feint") String feint) {
        loderunnerService.add(mixi, feint);
        return "OK";
    }
    
    @GET
    @Path("update/{mixi}/{feint}")
    @Produces("text/plain")
    public String update(@PathParam("mixi") String mixi, @PathParam("feint") String feint) {
        loderunnerService.update(mixi, feint);
        return "OK";
    }

    @GET
    @Path("delete/{mixi}")
    @Produces("text/plain")
    public String delete(@PathParam("mixi") String mixi) {
        loderunnerService.delete(mixi);
        return "OK";
    }

    @POST
    @Path("rename")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED) 
    @Produces("text/plain")
    public String rename(@FormParam("mixi") String mixi, @FormParam("name") String name) {
//System.err.println("エンコーディング: " + System.getProperty("file.encoding"));
System.err.println("mixi: " + mixi);
System.err.println("name: " + name);
//for (char c : name.toCharArray()) {
// System.err.printf("%c, %04x\n", c, (int) c);
//}
        try {
            loderunnerService.rename(mixi, name);
            return "OK";
        } catch (Exception e) {
            return "NG";
        }
    }

    @GET
    @Path("list")
    @Produces("text/html")
    public String list() {
        List<MixiFeint> mixiFeints = loderunnerService.list();

        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<body>");
        for (MixiFeint mixiFeint : mixiFeints) {
            sb.append(mixiFeint.getMixi());
            sb.append(":");
            sb.append(mixiFeint.getFeint());
            sb.append("<br/>");
        }
        sb.append("</body>");
        sb.append("</html>");
        return sb.toString();
    }

    @GET
    @Path("score/{mixi}/{score}")
    @Produces("text/plain")
    public String score(@PathParam("mixi") String mixi, @PathParam("score") long score) throws Exception {
        loderunnerService.score(mixi, score);
        return "OK";
    }

    @GET
    @Path("highscore/{mixi}")
    @Produces("text/html")
    public String highscore(@PathParam("mixi") String mixi) throws Exception {
        ResourceSections rss = loderunnerService.highscore(mixi);

        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<head>");
        sb.append("<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>");
        sb.append("</head>");
        sb.append("<body>");
        sb.append("<table>");
        sb.append("<tr>");
        sb.append("<th colspan='4'>");
        sb.append("<img src='/oflbicon.png'/> Your Ranking");
        sb.append("</th>");
        sb.append("</tr>");
        for (ResourceSection rs : rss.getResourceSection()) {
            String type = rs.getName();
            if (type.equals("My Score")) {
                Resources r = rs.getResources();
                for (HighScore hs : r.getHighScore()) {
                    String tmpPict = hs.getUser().getProfilePictureUrl();
                    String pict = tmpPict.isEmpty() ? "/ofdefault.png" :
                        tmpPict.startsWith("/uploads") ? "https://api.openfeint.com" + tmpPict : 
                        tmpPict;                         
                    sb.append("<tr>");
                    sb.append("<td>");
                    sb.append(hs.getRank().longValue());
                    sb.append("</td>");
                    sb.append("<td>");
                    sb.append("<img width=50 height=50 src=" + pict + ">");
                    sb.append("</td>");
                    sb.append("<td>");
                    sb.append(hs.getUser().getName());
                    sb.append("</td>");
                    sb.append("<td>");
                    sb.append(hs.getScore().longValue());
                    sb.append("</td>");
                    sb.append("</tr>");
                    break;
                }
                break;
            }
        }
        sb.append("</table>");
        sb.append("<hr/>");
        sb.append("<table>");
        sb.append("<tr>");
        sb.append("<th colspan='4'>");
        sb.append("<img src='/oflbicon.png'/> Ranking");
        sb.append("</th>");
        sb.append("</tr>");
        for (ResourceSection rs : rss.getResourceSection()) {
            String type = rs.getName();
            if (type.equals("Everyone")) {
                Resources r = rs.getResources();
                for (HighScore hs : r.getHighScore()) {
                    String tmpPict = hs.getUser().getProfilePictureUrl();
                    String pict = tmpPict.isEmpty() ? "/ofdefault.png" :
                        tmpPict.startsWith("/uploads") ? "https://api.openfeint.com" + tmpPict : 
                        tmpPict;                         
                    sb.append("<tr>");
                    sb.append("<td>");
                    sb.append(hs.getRank().longValue());
                    sb.append("</td>");
                    sb.append("<td>");
                    sb.append("<img width=50 height=50 src=" + pict + ">");
                    sb.append("</td>");
                    sb.append("<td>");
                    sb.append(hs.getUser().getName());
                    sb.append("</td>");
                    sb.append("<td>");
                    sb.append(hs.getScore().longValue());
                    sb.append("</td>");
                    sb.append("</tr>");
                }
            }
        }
        sb.append("</table>");
        sb.append("</body>");
        sb.append("</html>");
        
        return sb.toString();
    }
}

/* */
