package pl.mwrobel.activiti.websockets.test.utils;

import java.net.URI;
import java.util.concurrent.Future;
import lombok.Getter;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

public class ProcessEventsWebSocketConnector {

    @Getter
    private final ProcessEventsWebSocket processEventsWebsocket = new ProcessEventsWebSocket();
    private Future<Session> session;
    
    private final String destUri;
    private final WebSocketClient client;
    
    public ProcessEventsWebSocketConnector(String host, String port){
        destUri = "ws://" + host + ":" + port + "/process-engine-events";
        client = new WebSocketClient();
    }

    public void init() throws Exception {        
        client.start();
        ClientUpgradeRequest request = new ClientUpgradeRequest();
        session = client.connect(processEventsWebsocket, URI.create(destUri), request);
    }

    public void close() {
        try {
            session.get().close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
