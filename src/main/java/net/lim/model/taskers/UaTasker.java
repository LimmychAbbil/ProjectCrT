package net.lim.model.taskers;

import lombok.extern.slf4j.Slf4j;
import net.lim.model.Subscriber;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class UaTasker implements Tasker {

    private static final String URL = "https://btc-trade.com.ua/stock";
    private volatile boolean isRunning = false;
    private static final int TIMEOUT = 5 * 60 * 1000; //5 min

    private List<Subscriber> subscribers = new ArrayList<>();
    private Map<String,Double> valueMap = new HashMap<>();

    @Override
    public void registerSubscriber(Subscriber o) {
        subscribers.add(o);
        o.update(valueMap);
    }

    @Override
    public void removeSubscriber(Subscriber o) {
        subscribers.remove(o);
    }

    @Override
    public void notifyObservers() {
        for(Subscriber s: subscribers) {
            s.update(valueMap);
        }
    }

    public void saveUpdates(Map<String, Double> map) {
        this.valueMap = map;
        notifyObservers();
    }

    @Override
    public void run() {
        if (!isRunning) {
            isRunning = true;
            while (true) {
                try {
                    log.info("Updating...");
                    updateCrMap();
                    saveUpdates(valueMap);
                    Thread.sleep(TIMEOUT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateCrMap() {
        Client client = ClientBuilder.newClient();
        Response response = client.target(URL).request().get();
        String htmlString = response.readEntity(String.class);

        Document document = Jsoup.parse(htmlString);
        Element crMapElement = document.body().getElementsByClass("cd-side-nav").get(0);
        List<Element> crMapElements = crMapElement.child(0).children().stream().filter(element -> element.hasClass("overview")).collect(Collectors.toList());

        for (Element e : crMapElements) {
            String text = e.getElementsByTag("a").get(0).text();
            if (text.contains("UAH")) {
                valueMap.put(text.substring(0, text.indexOf("/")), Double.valueOf(text.split(" ")[1]));
            }
        }
    }
}
