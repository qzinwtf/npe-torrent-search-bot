package ru.qzinwtf.npe.torrent.provider;

import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import ru.qzinwtf.npe.torrent.Torrent;

import java.net.URL;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class RutorTorrentProvider implements TorrentProvider {

    private static final String URL = "https://rutor.org/search/1/1/000/10/";
    public static final int TIMEOUT_MILLIS = 20000;

    @Override
    @SneakyThrows
    public List<Torrent> getTorrents(String query) {
        Document document = Jsoup.parse(new URL(URL + query), TIMEOUT_MILLIS);

        return document
            .getElementById("index")
            .getElementsByTag("tr")
            .stream()
            .filter(Predicate.not(el -> el.className().equalsIgnoreCase("backgr")))
            .map(el -> el.getElementsByTag("td"))
            .map(el -> el.get(1).getElementsByTag("a"))
            .map(el -> Torrent.builder().url(el.get(0).attr("href")).description(el.get(2).text()).build())
            .collect(Collectors.toList());
    }
}
