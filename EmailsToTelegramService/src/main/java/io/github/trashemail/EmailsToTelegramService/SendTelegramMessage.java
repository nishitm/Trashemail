package io.github.trashemail.EmailsToTelegramService;

import io.github.trashemail.EmailsToTelegramService.Configuration.ImapClientServiceConfig;
import io.github.trashemail.EmailsToTelegramService.MessageEntities.Entities.InlineKeyboardButton;
import io.github.trashemail.EmailsToTelegramService.MessageEntities.Entities.InlineKeyboardMarkup;
import io.github.trashemail.EmailsToTelegramService.MessageEntities.TelegramMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Component
@EnableAsync
public class SendTelegramMessage {
    @Autowired
    private ImapClientServiceConfig imapClientServiceConfig;

    @Autowired
    private RestTemplate restTemplate;

    private static final Logger log = LoggerFactory.getLogger(
            SendTelegramMessage.class);

    public ArrayList<String> chunks(String message){
        /*
        Checking the message size and do splitting into chunks if required
        */
        int maxMessageSize = imapClientServiceConfig.getTelegram().getSize();

        ArrayList<String> split = new ArrayList<>();
        for (int i = 0; i <= message.length() / maxMessageSize; i++) {
            split.add(message.substring(i * maxMessageSize,
                                        Math.min((i + 1) * maxMessageSize,
                                                 message.length())));
        }
        return split;
    }

    @Async
    public void sendMessage(String message, long chatId){
        this.sendMessage(
                message,
                chatId,
                null
        );
    }

    @Async
    public void sendMessage(String message, long chatId, String filename) {
        String telegramURI = imapClientServiceConfig.getTelegram().getUrl() +
                imapClientServiceConfig.getTelegram().getBotToken() +
                "/sendMessage";

        ArrayList<String> messageChunks = chunks(message);
        for (int i = 0; i < messageChunks.size(); i++) {
            TelegramMessage request = new TelegramMessage(
                    chatId,
                    messageChunks.get(i));

            ResponseEntity response = restTemplate.postForEntity(
                    telegramURI,
                    request,
                    TelegramMessage.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                log.debug("Message sent to user: " + chatId);
            }
            else
                log.error("Unable to send message to user: " + chatId);
        }

        if (filename != null) {
            /*
            Send HTML button back to user if everything is good with filename.
            */
            InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();

            InlineKeyboardButton keyboardButton = new InlineKeyboardButton();
            keyboardButton.setText("HTML version");
            keyboardButton.setUrl(
                    imapClientServiceConfig.getEmails().getHostPath() +
                            filename);

            List<List<InlineKeyboardButton>> buttonList = new ArrayList<>();

            buttonList.add(new ArrayList<>());
            buttonList.get(0).add(keyboardButton);

            markupKeyboard.setInlineKeyboardButtonList(buttonList);

            TelegramMessage telegramResponse =
                    new TelegramMessage(
                            chatId,
                            "To view in HTML format click the link below.",
                            markupKeyboard);

            ResponseEntity response = restTemplate.postForEntity(
                    telegramURI,
                    telegramResponse,
                    TelegramMessage.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                log.debug("HTML link sent to user: " + chatId + filename);
            }
            else log.error("Unable to HTML Link to user: " + chatId + filename);
        }
    }
}

