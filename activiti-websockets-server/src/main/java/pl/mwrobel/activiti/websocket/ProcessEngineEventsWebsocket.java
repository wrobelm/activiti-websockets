package pl.mwrobel.activiti.websocket;

import java.io.IOException;
import javax.servlet.ServletContext;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import org.activiti.engine.ProcessEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessEngineEventsWebsocket extends Endpoint {

    private static final Logger log = LoggerFactory.getLogger(ProcessEngineEventsWebsocket.class);

    private ServletContext servletContext;
    private ProcessEngine processEngine;

    @Override
    public void onOpen(final Session session, EndpointConfig config) {
        log.info("Websockets connection opened");
        
        this.servletContext = (ServletContext) config.getUserProperties().get("servlet.context");
        processEngine = (ProcessEngine) servletContext.getAttribute("activiti-engine");
        processEngine.getRuntimeService().addEventListener(new ActivitiProcessEventsWebsocketBroadcaster(session));
    }
    

}
