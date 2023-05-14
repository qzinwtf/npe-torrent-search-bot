package ru.qzinwtf.npe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.telegram.telegrambots.starter.TelegramBotStarterConfiguration;

@SpringBootApplication
@ImportAutoConfiguration(TelegramBotStarterConfiguration.class)
public class TorrentSearchBotApp {
    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext context = SpringApplication.run(TorrentSearchBotApp.class, args);
    }
}