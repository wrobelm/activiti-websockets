package pl.mwrobel.activiti.websockets.test.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

@WebSocket
public class ProcessEventsWebSocket {    
    
    private final List<ProcessEventLatchPair> awaitedEvents = new ArrayList<>();
    
    private final ObjectMapper om = new ObjectMapper();
    
    @OnWebSocketMessage
    @SuppressWarnings("unchecked")
    public void onMessage(String msg) throws IOException {
        System.out.println(msg);  
        ProcessEventDTO pe = om.readValue(msg, ProcessEventDTO.class);
        
        List<ProcessEventLatchPair> matchedEvents = awaitedEvents.stream()
                .filter(pair -> pe.matches(pair.getEvent()))
                .collect(Collectors.toList());
        
        for(ProcessEventLatchPair p : matchedEvents){
            p.getLatch().countDown();
        }       
     
    } 

    
    public void addExpectedEventAndWait(long timeout, ProcessEventDTO eventTemplate) throws InterruptedException{
        ProcessEventLatchPair pair = new ProcessEventLatchPair();
        pair.setEvent(eventTemplate);
        awaitedEvents.add(pair);
        boolean gotEventInTime = pair.getLatch().await(timeout, TimeUnit.MILLISECONDS);
        awaitedEvents.remove(pair);
        if(gotEventInTime){
            System.out.println("MATCHED ****" + eventTemplate);
        }else{
            throw new RuntimeException("Failed to get event" + eventTemplate + "in time");
        }        
    }
    
    public String addExpectedEvent(ProcessEventDTO eventTemplate) throws InterruptedException{
        ProcessEventLatchPair pair = new ProcessEventLatchPair();
        pair.setEvent(eventTemplate);
        awaitedEvents.add(pair);        
        return pair.getId();
    }
    
    public void checkIfEventOccured(long timeout, String eventId) throws InterruptedException{
        
        ProcessEventLatchPair wantedEvent = awaitedEvents.stream()
                .filter(pair -> pair.getId().equals(eventId))
                .collect(Collectors.toList()).get(0);
        
        boolean gotEventInTime = wantedEvent.getLatch().await(timeout, TimeUnit.MILLISECONDS);
        awaitedEvents.remove(wantedEvent);
        if(gotEventInTime){
            System.out.println("MATCHED ****" + wantedEvent.getEvent());
        }else{
            throw new RuntimeException("Failed to get event" + wantedEvent.getEvent() + "in time");
        }        
    }
    
    
}
