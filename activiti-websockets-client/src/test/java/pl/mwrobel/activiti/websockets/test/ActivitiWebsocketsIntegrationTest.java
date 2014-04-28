package pl.mwrobel.activiti.websockets.test;

import java.io.IOException;
import static java.lang.Thread.sleep;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pl.mwrobel.activiti.websockets.test.utils.ProcessEventDTO;
import pl.mwrobel.activiti.websockets.test.utils.ProcessEventsWebSocket;
import pl.mwrobel.activiti.websockets.test.utils.ProcessEventsWebSocketConnector;

/**
 *
 * @author michalw
 */
public class ActivitiWebsocketsIntegrationTest {

    private final Client client;

    private static final String host = "127.0.0.1";
    private static final String port = "9080";
    private static final String activitiServiceURL = "http://" + host + ":" + port + "/service/";
    private static final String authHeader = "Basic a2VybWl0Omtlcm1pdA==";

    private ProcessEventsWebSocketConnector con;

    public ActivitiWebsocketsIntegrationTest() {
        client = ClientBuilder.newClient();
    }

    @Before
    public void setUp() throws Exception {
        con = new ProcessEventsWebSocketConnector(host, port);
        con.init();
    }

    @After
    public void tearDown() {
        con.close();
        System.out.println("closed connection");
    }

    @Test
    public void processShouldComplete() throws InterruptedException, Exception {
        final ProcessEventsWebSocket ws = con.getProcessEventsWebsocket();

        String processInstanceId = createProcessInstance();
        
        ws.addExpectedEventAndWait(15000, ProcessEventDTO.builder()
                .activityName("user-task").processId(processInstanceId).activityType("ACTIVITY_STARTED")
                .build());
        
        completeUserTask(processInstanceId);        
        checkIfProcessHasFinished(processInstanceId);
    }

    private String createProcessInstance() {
        String businessKey = UUID.randomUUID().toString();
        String url = activitiServiceURL + "runtime/process-instances";
        System.out.println(url);
        String payload = "{\n"
                + "   \"processDefinitionKey\":\"event-demo-process\",\n"
                + "   \"businessKey\":\"" + businessKey + "\"  \n"
                + "}";
        String method = "POST";
        Response response = clientHTTPRequest(url, method, payload, authHeader);
        HashMap<String, Object> serverResponse = from(response);
        String processInstanceId = (String) serverResponse.get("id");
        return processInstanceId;

    }
    
    private String getUserTaskId(String processInstanceId) {
        String url = activitiServiceURL + "runtime/tasks?processInstanceId=" + processInstanceId;
        String payload = null;
        String method = "GET";
        Response response = clientHTTPRequest(url, method, payload, authHeader);
        HashMap<String, Object> serverResponse = from(response);
        Map<String, Object> task = ((List<Map<String,Object>>)serverResponse.get("data")).get(0);
        String taskId = (String)task.get("id");        
        return taskId;
    }
    
    private void completeUserTaskByTaskId(String taskId) {
        String url = activitiServiceURL + "runtime/tasks/" + taskId;
        String payload ="{\"action\" : \"complete\"}";
        String method = "POST";
        clientHTTPRequest(url, method, payload, authHeader);        
    }
    
    private void completeUserTask(String processInstanceId) {
        completeUserTaskByTaskId(getUserTaskId(processInstanceId));
    }

    private void checkIfProcessHasFinished(String processInstanceId) {
        String url = activitiServiceURL + "runtime/process-instances/" + processInstanceId;
        String payload = null;
        String method = "GET";
        try {
            clientHTTPRequest(url, method, payload, authHeader);
        } catch (NotFoundException e) {
            // this exception should be thrown if process is finished
            return;
        }
        throw new RuntimeException("process " + processInstanceId + " still exists");
    }

    private Response clientHTTPRequest(String url, String method, String payload, String authHeader) {
        WebTarget webTarget = client.target(url);
        String input = payload;

        Invocation.Builder invocationBuilder
                = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
        invocationBuilder.header("Authorization", authHeader);

        Response response = invocationBuilder.method(method, Entity.json(input), Response.class);

        if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
            if (response.getStatus() == 404) {
                throw new NotFoundException("Failed : HTTP error code : "
                        + response.getStatus() + " message:" + response.readEntity(String.class));
            }
            throw new RuntimeException("Failed : HTTP error code : "
                    + response.getStatus() + " message:" + response.readEntity(String.class));
        }
        return response;
    }

    private HashMap<String, Object> from(Response JSONResponse) {
        String serverOutput = JSONResponse.readEntity(String.class);
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<HashMap<String, Object>> typeRef
                = new TypeReference< 
                 HashMap<String, Object>>() {
                };
        try {
            return mapper.readValue(serverOutput, typeRef);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
