import com.vdurmont.emoji.EmojiParser;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;

public class EvilShot extends TelegramLongPollingBot {

    //environmental variables
    private String BOT_TOKEN = System.getenv("BOT_TOKEN");
    private String DATAPATH = System.getenv("DATAPATH_TRAINER");

    //Telegram variables
    private boolean flag = false;
    private String wordToLookUp;
    //flag used to stop the scraper when true
    private boolean flagStop = true;

    //Scraper Variables
    private static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static SecureRandom rnd = new SecureRandom();
    private static URL imageUrl;

    public String getBotUsername() {
        return "EvilShot_bot";
    }

    public String getBotToken() {
        return BOT_TOKEN;
    }

    public void onUpdateReceived(Update update){
        if(update.hasMessage() && update.getMessage().hasText()){
            String message = update.getMessage().getText();

            if(flag){
                wordToLookUp = message;
                try {
                    scraperForWord(update.getMessage(), flagStop);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    onUpdateMenu(update.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            else {
                try {
                    onUpdateMenu(update.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void onUpdateMenu(Message message) throws IOException {

        switch (message.getText().toLowerCase()){
            case "scrape images" :
                flagStop = false;
                while(!flagStop){
                    scraper(message, flagStop);
                }
                break;
            case "scrape for a word":
                flag = true;
                flagStop = false;
                sendMsg(message, "Enter the word:");
                break;
            case "/stop":
                System.exit(0);
                flagStop = true;
                break;
            case "contact me":
                sendMsg(message, "You can contact me here: " + "@ErrorFetchingData");
                break;
            case "/start":
                String text= ("This bot can work in two different ways:" + '\n' +
                                        '\n' + "Scrape Images: the bot will scrape through LightShot pictures and return them all in chat." + '\n' +
                                        '\n' + "Scrape for a Word: the bot will scrape through lightShot pictures looking for pictures that contain the desired word, please note it might take a while"
                                         );
                lunchKeyboard(message, text);
                break;
            case "/help":
                sendMsg(message, "This bot will scrape the website LightShot.com for pictures . It also implements OCR(Objective Character Recognition) in order to search for pictures which contain a certain word, such as \"log in\", \"password\" and so on.");
            break;
            default:
                sendMsg(message, "Incorrect Input");
                lunchKeyboard(message, "Choose one the followings:");
                break;
        }
    }

    private void sendMsg(Message message, String text){

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText(text);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e){
            e.printStackTrace();
        }
    }

    private void lunchKeyboard(Message message, String text){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText(text);
        setMainKeyboard(sendMessage);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e){
            e.printStackTrace();
        }
    }

    private void setMainKeyboard(SendMessage sendMessage){

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

        KeyboardRow firstRow = new KeyboardRow();
        KeyboardRow secondRow = new KeyboardRow();

        firstRow.add("Scrape Images");
        firstRow.add("Scrape For A Word");

        secondRow.add("Contact me");

        ArrayList<KeyboardRow> keyboardList = new ArrayList<>();
        keyboardList.add(firstRow);
        keyboardList.add(secondRow);

        replyKeyboardMarkup.setKeyboard(keyboardList);

        sendMessage.setReplyMarkup(replyKeyboardMarkup);
    }

    //this method is the one used to gather pictures which contain the specified keyword
    private void scraperForWord(Message message, Boolean flagStop) throws IOException {
        while (!flagStop){
            Connection newConnection = Jsoup.connect("https://prnt.sc/" + randomString(6))
                    .followRedirects(false)
                    .timeout(10000)
                    .ignoreHttpErrors(true);
            Element newElement = newConnection.get().body();
            Elements elements = newElement.getElementsByClass("no-click screenshot-image");
            String urlParsed = elements.attr("src");
            if(!urlParsed.isEmpty()){
                try{
                    imageUrl = new URL(urlParsed);
                } catch (MalformedURLException e){
                    System.out.println("MalformedURLException " + e.toString());
                }
                //this prints the url of the scraped pic
                System.out.println(urlParsed);
                BufferedImage img = ImageIO.read(imageUrl);

                //OCR stuff
                ITesseract image = new Tesseract();
                try {
                    image.setDatapath(DATAPATH);
                    String gatheredText = image.doOCR(img);
                    searchForWord(wordToLookUp, gatheredText, message, urlParsed);
                } catch (TesseractException e){
                    e.printStackTrace();
                }
            }
        }
    }

    //this method is the general scraper
    private void scraper(Message message, Boolean flagStop) throws IOException{
        Connection newConnection = Jsoup.connect("https://prnt.sc/" + randomString(6))
                .followRedirects(false)
                .timeout(10000)
                .ignoreHttpErrors(true);
        Element newElement = newConnection.get().body();
        Elements elements = newElement.getElementsByClass("no-click screenshot-image");
        String urlParsed = elements.attr("src");
        if(!urlParsed.isEmpty()){
            //this prints the url of the scraped pic
            System.out.println(urlParsed);
            printImages(urlParsed, message);
        }
    }

    //I use this to generate random strings for the LightShot URL
    private String randomString(int len){
        StringBuilder sb = new StringBuilder(len);
        for(int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    //this method simply looks for pictures which contain the input word
    private void searchForWord(String wordToLookUp, String textToAnalyze, Message message, String imgUrl) throws IOException {
        if(textToAnalyze.toLowerCase().contains(wordToLookUp.toLowerCase())){
            System.out.println(textToAnalyze);
            System.out.println("Word found");
            printImages(imgUrl, message);
            //if the pictures contain the chosen word then this will return that picture in chat
        } else {
            System.out.println("Word not found:" + '\n' + textToAnalyze);
        }
    }

    //method used to print images in-chat on Telegram
    private void printImages(String imageUrl, Message message) {

        SendPhoto picToSend = new SendPhoto();
        InputFile inputFile = new InputFile().setMedia(imageUrl);
        picToSend.setChatId(message.getChatId().toString());
        picToSend.setPhoto(inputFile);

        try {
            execute(picToSend);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            lunchKeyboard(message, EmojiParser.parseToUnicode(":warning: post format not supported yet"));
        }
    }

}



