package pl.mwrobel.activiti.extensions;

import pl.mwrobel.activiti.websocket.ProcessEngineEventsWebsocket;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;
import org.activiti.engine.ProcessEngines;
import org.activiti.rest.common.servlet.ActivitiServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author michalw
 */
public class CustomActivitiServletContextListener extends ActivitiServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(CustomActivitiServletContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent event) {
        super.contextInitialized(event);
        event.getServletContext().setAttribute("activiti-engine", ProcessEngines.getDefaultProcessEngine());
        addProcessEventsEndpoint(event);
    }

    public void addProcessEventsEndpoint(ServletContextEvent ce) {
        log.info("Deploying process-engine-events websockets server endpoint");
        ServletContext sc = ce.getServletContext();

        final ServerContainer server_container
                = (ServerContainer) ce.getServletContext().getAttribute("javax.websocket.server.ServerContainer");

        try {
            ServerEndpointConfig config
                    = ServerEndpointConfig.Builder.create(ProcessEngineEventsWebsocket.class,
                            "/process-engine-events").build();
            config.getUserProperties().put("servlet.context", sc);
            server_container.addEndpoint(config);
        } catch (DeploymentException e) {
            throw new RuntimeException(e);
        }
    }

}
