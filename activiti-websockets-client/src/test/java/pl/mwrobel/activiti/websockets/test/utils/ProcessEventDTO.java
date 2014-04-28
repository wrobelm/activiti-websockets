package pl.mwrobel.activiti.websockets.test.utils;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 *
 * @author michalw
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class ProcessEventDTO {
    private String processId;    
    private String activityId;
    private String activityName;
    private String activityType;
    private String customActivityType;
    
    public boolean matches(ProcessEventDTO other){                
        if(other.processId != null){
            if(!other.getProcessId().equals(processId)){
                return false;
            }
        }
        if(other.activityId != null){
            if(!other.getActivityId().equals(activityId)){
                return false;
            }
        }
        if(other.activityName != null){
            if(!other.getActivityName().equals(activityName)){
                return false;
            }
        }
        if(other.activityType != null){
            if(!other.getActivityType().equals(activityType)){
                return false;
            }
        }
        if(other.customActivityType != null){
            if(!other.getCustomActivityType().equals(customActivityType)){
                return false;
            }
        }
        return true;
    }
}
