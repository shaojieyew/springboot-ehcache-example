package app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import java.util.HashMap;
import java.util.Map;

@EnableCaching
@SpringBootApplication
public class Main implements CommandLineRunner {

    public static void main(String []args ){
        SpringApplication application = new SpringApplication(Main.class);
        application.run(args);
    }

    Logger logger = LoggerFactory.getLogger(Main.class);

    @Autowired TestService testService;
    public void run(String... args) throws Exception {
        int i = 0;
        while(true){
            Map<Integer, Integer> m = new HashMap<Integer, Integer>();
            m.put(i%3, i%5);
            testService.getTestCachBySpecificParam(Main.class, m, i);
            i++;

            logger.info("completed cycle = {}", i);
            Thread.sleep(100);
        }
    }
}
