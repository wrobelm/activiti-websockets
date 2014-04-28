package pl.mwrobel.activiti.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Builder;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 *
 * @author michalw
 */
@Data
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class ProcessEventDTO {
    private String processId;    
    private String activityId;
    private String activityName;
    private String activityType;
    private String customActivityType;
}
