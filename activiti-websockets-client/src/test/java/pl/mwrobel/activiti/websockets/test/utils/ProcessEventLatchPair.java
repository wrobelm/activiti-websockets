package pl.mwrobel.activiti.websockets.test.utils;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author michalw
 */
@Data
@NoArgsConstructor
public class ProcessEventLatchPair {    
    private CountDownLatch latch = new CountDownLatch(1);
    private ProcessEventDTO event;
    private String id = UUID.randomUUID().toString();
}
