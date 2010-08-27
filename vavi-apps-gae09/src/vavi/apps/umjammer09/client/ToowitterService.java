
package vavi.apps.umjammer09.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;


/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("toowitter")
public interface ToowitterService extends RemoteService {
    boolean isSigned();
    String upload(String text);
    String uploadAsQrcode(String text);
}
