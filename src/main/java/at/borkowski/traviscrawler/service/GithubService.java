package at.borkowski.traviscrawler.service;

import at.borkowski.traviscrawler.dto.GithubLimitDTO;
import at.borkowski.traviscrawler.entities.GithubRate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.lang.System.currentTimeMillis;
import static org.springframework.http.HttpMethod.GET;

@Service
public class GithubService {
    private static String clientId = readLine("/home/michael/research/github-clientid");
    private static String clientSecret = readLine("/home/michael/research/github-clientsecret");

    private static String readLine(String path) {
        try {
            return Files.readAllLines(Paths.get(path)).get(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private long interval = 1500;
    private final Object lock = new Object();
    private long nextClear = currentTimeMillis();

    private RestTemplate restTemplate = new RestTemplate();

    private long callCount;

    public <T> T get(String url, Class<T> clazz) {
        return exchange(GET, url, clazz, true);
    }

    private <T> T exchange(HttpMethod method, String url, Class<T> clazz, boolean throttle) {
        if (throttle) throttle();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity entity = new HttpEntity(headers);
        char sep = url.contains("?") ? '&' : '?';
        HttpEntity<T> response = restTemplate.exchange("https://api.github.com" + url + sep + "client_id=" + clientId + "&client_secret=" + clientSecret, method, entity, clazz);
        callCount++;
        return response.getBody();
    }

    private void throttle() {
        synchronized (lock) {
            while (currentTimeMillis() < nextClear) sleep(10);
            nextClear = currentTimeMillis() + interval;
        }
    }

    private void sleep(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException ignored) {
        }
    }

    @Scheduled(fixedRate = 15_000)
    public void adjustInterval() {
        interval = getRateStatus().getIntervalMilliseconds() * 10 / 9;
        System.out.println("[github] API interval is " + interval + " ms");
    }

    public long getCallCount() {
        return callCount;
    }

    public GithubRate getRateStatus() {
        GithubLimitDTO limitDTO = exchange(GET, "/rate_limit", GithubLimitDTO.class, false);
        return new GithubRate(limitDTO.resources.core.limit, limitDTO.resources.core.remaining, limitDTO.resources.core.reset);
    }
}
