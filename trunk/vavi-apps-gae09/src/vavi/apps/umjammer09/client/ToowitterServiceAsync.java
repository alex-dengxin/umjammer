
package vavi.apps.umjammer09.client;

import com.google.gwt.user.client.rpc.AsyncCallback;


/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface ToowitterServiceAsync {
    void isSigned(AsyncCallback<Boolean> callback);
    void upload(String input, AsyncCallback<String> callback);
    void uploadAsQrcode(String input, AsyncCallback<String> callback);
}
