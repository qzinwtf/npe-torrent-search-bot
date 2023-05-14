package ru.qzinwtf.npe;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.qzinwtf.npe.torrent.Torrent;
import ru.qzinwtf.npe.torrent.provider.TorrentProvider;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class Bot extends TelegramLongPollingBot {

    public static final String COMMAND_SEARCH = "/search";
    public static final String COMMAND_START = "/start";
    public static final String QUERY_IS_BLANK = "⚠ Your query is blank and cannot be processed";
    private final List<TorrentProvider> torrentProviders;

    @Value("${bot.token}")
    private final String botToken;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.getMessage() == null) {
            log.error("Nothing to process message is null");
            return;
        }
        log.info("Received update from user {}", update.getMessage().getFrom().getUserName());
        String text = update.getMessage().getText();
        if (StringUtils.isNotBlank(text)) {
            Long chatId = update.getMessage().getChatId();
            if (text.startsWith(COMMAND_START)) {
                log.info("Starting conversion");
            } else if (text.startsWith(COMMAND_SEARCH)) {
                String query = text.replace(COMMAND_SEARCH, "");

                if (StringUtils.isBlank(query)) {
                    sendMessage(QUERY_IS_BLANK, chatId);
                    return;
                }

                torrentProviders
                    .stream()
                    .map(t -> t.getTorrents(query))
                    .flatMap(List::stream)
                    .limit(10)
                    .map(t -> createDocument(t, chatId))
                    .forEach(sendDocument -> {
                        try {
                            execute(sendDocument);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    });
            }
        }
    }

    @SneakyThrows
    private SendDocument createDocument(Torrent torrent, Long chatId) {
        Connection.Response torrentFile = Jsoup
            .connect(torrent.getUrl())
            .ignoreContentType(true)
            .execute();

        InputFile inputFile = new InputFile();
        inputFile.setMedia(torrentFile.bodyStream(), torrent.getDescription() + ".torrent");
        return SendDocument
            .builder()
            .chatId(chatId)
            .document(inputFile)
            .caption(torrent.getDescription())
            .build();
    }

    private void sendMessage(String message, Long chatId) {
        SendMessage answer = SendMessage
            .builder()
            .chatId(chatId)
            .text(message)
            .build();
        try {
            execute(answer);
        } catch (TelegramApiException e) {
            log.error("Error when sending answer", e);
        }
    }

    @Override
    public void onRegister() {
        super.onRegister();
        log.info("Bot registered");
    }

    @Override
    public String getBotUsername() {
        return "npe_torrent_search_bot";
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}
