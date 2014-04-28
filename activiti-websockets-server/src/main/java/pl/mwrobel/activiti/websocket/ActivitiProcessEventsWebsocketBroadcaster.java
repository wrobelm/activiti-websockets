/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.mwrobel.activiti.websocket;

import pl.mwrobel.activiti.domain.ProcessEventDTO;
import java.io.IOException;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import org.activiti.engine.delegate.event.ActivitiActivityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author michalw
 */
public class ActivitiProcessEventsWebsocketBroadcaster implements ActivitiEventListener {

    private static final Logger log = LoggerFactory.getLogger(ActivitiProcessEventsWebsocketBroadcaster.class);
    private final Session session;
    private final ObjectMapper om;

    public ActivitiProcessEventsWebsocketBroadcaster(Session session) {
        this.session = session;
        om = new ObjectMapper();
    }

    @Override
    public void onEvent(ActivitiEvent event) {        
        switch (event.getType()) {
            case ACTIVITY_STARTED: {
                broadcastEvent((ActivitiActivityEvent)event);
                break;
            }
            case ACTIVITY_COMPLETED: {
                broadcastEvent((ActivitiActivityEvent)event);
                break;
            }
        }
    }

    private void broadcastEvent(ActivitiActivityEvent e) {
        ProcessEventDTO dto = ProcessEventDTO.builder().activityId(e.getActivityId())
                .activityName(e.getActivityId())
                .activityType(e.getType().toString())
                .processId(e.getProcessInstanceId())
                .build();
        log.info("Activiti event received: " + e.getType());
        RemoteEndpoint.Basic remoteEndpoint = session.getBasicRemote();
        try {                        
            remoteEndpoint.sendText(om.writeValueAsString(dto));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean isFailOnException() {
        // The logic in the onEvent method of this listener is not critical, exceptions
        // can be ignored if logging fails...
        return false;
    }

}
