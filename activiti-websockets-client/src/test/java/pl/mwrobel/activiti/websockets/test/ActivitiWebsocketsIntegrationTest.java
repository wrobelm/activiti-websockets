package pl.mwrobel.activiti.websockets.test;

import static com.jayway.restassured.RestAssured.given;
import com.jayway.restassured.http.ContentType;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

    private static final String host = "127.0.0.1";
    private static final String port = "9080";
    private static final String activitiServiceURL = "http://" + host + ":" + port + "/service/";    
    private static final String USERNAME = "kermit";
    private static final String PASSWORD = "kermit";    
    private static final int HTTP_STATUS_CREATED = 201;
    private static final int HTTP_STATUS_SUCCESSFUL = 200;
    private static final int HTTP_STATUS_NOT_FOUND = 404;

    private ProcessEventsWebSocketConnector con;    

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
        String response = 
        given()
                .contentType(ContentType.JSON)
                .auth().basic(USERNAME, PASSWORD)                
                .body(payload)
        .when()
                .post(url)
        .then()
                .assertThat().statusCode(HTTP_STATUS_CREATED)
        .extract().asString();
        HashMap<String, Object> serverResponse = from(response);
        String processInstanceId = (String) serverResponse.get("id");
        return processInstanceId;

    }
    
    private String getUserTaskId(String processInstanceId) {
        String url = activitiServiceURL + "runtime/tasks?processInstanceId=" + processInstanceId;
        String response = 
        given()
                .contentType(ContentType.JSON)
                .auth().basic(USERNAME, PASSWORD)                
        .when()
                .get(url)
        .then()
                .assertThat().statusCode(HTTP_STATUS_SUCCESSFUL)
        .extract().asString();
        HashMap<String, Object> serverResponse = from(response);
        Map<String, Object> task = ((List<Map<String,Object>>)serverResponse.get("data")).get(0);
        String taskId = (String)task.get("id");        
        return taskId;
    }
    
    private void completeUserTaskByTaskId(String taskId) {
        String url = activitiServiceURL + "runtime/tasks/" + taskId;
        String payload ="{\"action\" : \"complete\"}";
        given()
                .contentType(ContentType.JSON)
                .auth().basic(USERNAME, PASSWORD)
                .body(payload)
        .when()
                .post(url)
        .then()
                .assertThat().statusCode(HTTP_STATUS_SUCCESSFUL);        
    }
    
    private void completeUserTask(String processInstanceId) {
        completeUserTaskByTaskId(getUserTaskId(processInstanceId));
    }

    private void checkIfProcessHasFinished(String processInstanceId) {
        String url = activitiServiceURL + "runtime/process-instances/" + processInstanceId;
        given()
                .contentType(ContentType.JSON)
                .auth().basic(USERNAME, PASSWORD)                
        .when()
                .get(url)
        .then()
                .assertThat().statusCode(HTTP_STATUS_NOT_FOUND);
    }
    
    private HashMap<String, Object> from(String JSONString) {        
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<HashMap<String, Object>> typeRef
                = new TypeReference< 
                 HashMap<String, Object>>() {
                };
        try {
            return mapper.readValue(JSONString, typeRef);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
