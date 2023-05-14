package ru.qzinwtf.npe.torrent.provider;

import ru.qzinwtf.npe.torrent.Torrent;

import java.util.List;

public interface TorrentProvider {

    List<Torrent> getTorrents(String query);
}
