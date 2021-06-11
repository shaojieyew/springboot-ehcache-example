package app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ApiClientService {
Logger logger = LoggerFactory.getLogger(ApiClientService.class);
    public void getData(int i) throws InterruptedException {
        logger.info("getData called {}", i);
        Thread.sleep(1000);
    }
}
