package app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TestService  {

    Logger log = LoggerFactory.getLogger(TestService.class);

    @Autowired
    ApiClientService apiClientService;

    @Cacheable(value = "testCache")
    public void getTest(int i) throws InterruptedException {
        apiClientService.getData(i);
    }

    @Cacheable(value = "testCache")
    public void getTestExtend(Class clazz, Map<Integer,Integer> map) throws InterruptedException {
        apiClientService.getData(clazz.hashCode()+map.size());
    }

    @Cacheable(value = "testCache", key = "{#clazz.getSimpleName(), #map}" )
    public void getTestCachBySpecificParam(Class clazz, Map<Integer,Integer> map, int i) throws InterruptedException {
        log.info("getTestCachBySpecificParam: {}, {}, {}", clazz.getSimpleName(), map.toString(), i);
        apiClientService.getData(clazz.hashCode()+map.size());
    }
}
