package vavi.apps.umjammer03.lr;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestHandler;


@Component("FileUploadServlet")
public class FileUploadServlet implements HttpRequestHandler {

    @Autowired(required = true)
    LodeRunnerService loderunnerService;

    /**
     * req.getParameter("mixi") ができない訳
     * 
     * <quote src='http://homepage1.nifty.com/algafield/core1.html'>
     * Javaサーブレットの規格は、POSTでもデータ形式がx-www-form-urlencoded
     * でなければ request.getParameter()しない/できない、と（なぜか）
     * 言い張っていますから、サーブレットで使うためのFORMでは METHOD="POST"
     * とENCTYPE="multipart/form-data"の両方を指定するとパラメータがnullに
     * なってしまいます。 ResinもTomcatもこの規格には忠実です。
     * </quote>
     */
    @Override
	public void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        ServletFileUpload fileUpload = new ServletFileUpload();
		try {
	        String mixi = null;

	        FileItemIterator itemIterator = fileUpload.getItemIterator(req);
			while (itemIterator.hasNext()) {
				FileItemStream itemStream = itemIterator.next();
System.err.println(itemStream.getFieldName());
				if (itemStream.getFieldName().equals("mixi")) {
				    ByteArrayOutputStream baos = new ByteArrayOutputStream();
				    InputStream i = itemStream.openStream();
				    byte[] b = new byte[1024];
				    while (i.available() > 0) {
				        int r = i.read(b, 0, b.length);
				        baos.write(b, 0, r);
				    }
				    mixi = new String(baos.toByteArray());
				} else if (itemStream.getFieldName().equals("file")) {
System.err.println("mixi: " + mixi);
                    InputStream is = itemStream.openStream();
System.err.println("file: " + is);
                    loderunnerService.updatePicture(mixi, is);
                    is.close();
				}
			}
		} catch (Exception e) {
		    throw new ServletException(e);
		}
	}
}
