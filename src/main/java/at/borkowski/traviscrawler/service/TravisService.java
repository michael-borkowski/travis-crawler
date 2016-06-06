package at.borkowski.traviscrawler.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import static java.lang.System.currentTimeMillis;
import static org.springframework.http.HttpMethod.GET;

@Service
public class TravisService {
    private static final long INTERVAL = 60_000 / 100 * 2;

    private final Object lock = new Object();
    private long nextClear = currentTimeMillis();

    private RestTemplate restTemplate = new RestTemplate();

    private long callCount;

    public <T> T get(String url, Class<T> clazz) {
        return exchange(GET, url, clazz);
    }

    private <T> T exchange(HttpMethod method, String url, Class<T> clazz) {
        throttle();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/vnd.travis-ci.2+json");
        HttpEntity entity = new HttpEntity(headers);
        HttpEntity<T> response = restTemplate.exchange("https://api.travis-ci.org" + url, method, entity, clazz);
        callCount++;
        return response.getBody();
    }

    private void throttle() {
        synchronized (lock) {
            while (currentTimeMillis() < nextClear) sleep(10);
            nextClear = currentTimeMillis() + INTERVAL;
        }
    }

    private void sleep(int i) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {
        }
    }

    public long getCallCount() {
        return callCount;
    }
}
