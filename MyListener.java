import java.net.URL;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.awt.Color;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.EmbedBuilder;

public class MyListener extends ListenerAdapter {

    /* compiler : javac -cp .:JDA-4.1.1_101-withDependencies.jar:json-simple-1.1.jar  MainTest.java 
    éxécuter : java -cp .:JDA-4.1.1_101-withDependencies.jar:j:json-simple-1.1.jar  MainTest.java Njk0MTQ3NTY1MTU5MDU1Mzkx.XoHcOw.07-eJ-_97CZEn0u2w6-LJozjqV0 */

    static String getMeteo(MessageReceivedEvent e,String town ) {
        String result = "";
        try {
            String APIkey = "229aae3c4f05d8d4b911e888101e285e";
            String serv = "http://api.openweathermap.org/data/2.5/weather";
            String param = "q=" + town + "&units=metric&appid=";
            URL url = new URL(serv+"?"+param+APIkey);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code:" + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            result = br.lines().collect(Collectors.joining());
            conn.disconnect();
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public void showMeteo(MessageReceivedEvent e, JSONObject meteo){
        String town = (String) meteo.get("name");

        JSONObject main = (JSONObject) meteo.get("main");
        double temp = (double) main.get("temp");
        long humidity = (long) main.get("humidity");
        double feel = (double) main.get("feels_like");

        JSONObject sys = (JSONObject) meteo.get("sys");

        MessageChannel channel = e.getChannel();

        JSONArray weather = (JSONArray) meteo.get("weather");
        JSONObject weather0 = (JSONObject) weather.get(0);
        String icon = (String) weather0.get("icon");

        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle("Météo à "+town);
        eb.addField("Ville", town, true);
        eb.addField("Température", temp+"°C", true);
        eb.addField("Ressentie", feel + "°C", true);
        eb.addField("Humidité", humidity + "%", true);
        eb.addBlankField(true);
        eb.setThumbnail("http://openweathermap.org/img/wn/"+icon+"@2x.png");
        eb.setColor(new Color(69, 135, 244));
        eb.setFooter("Created with API openweathermap","http://openweathermap.org/");

        channel.sendMessage(eb.build()).queue();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        String bot_prefix = "/62";

        if (event.getAuthor().isBot()) return;
        // We don't want to respond to other bot accounts, including ourself
        Message message = event.getMessage();
        String content = message.getContentRaw(); 
        // getContentRaw() is an atomic getter
        // getContentDisplay() is a lazy getter which modifies the content for e.g. console view (strip discord formatting)
        
        if (content.equals(bot_prefix +" ping"))
        {
            MessageChannel channel = event.getChannel();
            channel.sendMessage("Pong!").queue(); // Important to call .queue() on the RestAction returned by sendMessage(...)
        }

        if (content.contains(bot_prefix +" dice"))
        {   
            try{
                MessageChannel channel = event.getChannel();
                String[] tab = content.split(" ");
                int number;
                if (tab.length == 2) {
                    number =(int)((Math.random()*6)+1);
                } else {
                    number =(int)(Math.random()*(Integer.valueOf(tab[2]))+1);
                }
                channel.sendMessage(" "+ number +" ").queue(); // Important to call .queue() on the RestAction returned by sendMessage(...)
            }
            catch (NumberFormatException e) {
                MessageChannel channel = event.getChannel();
                channel.sendMessage(" Please provide an integer ! ").queue();
            }
        }

        if (content.contains(bot_prefix +" cat")){
            try{
                MessageChannel channel = event.getChannel();
                String[] tab = content.split(" ");
            
                if (content.contains("says")){
                    String text = "";
                    for (int i = 3; i < tab.length; i++) {
                        text += tab[i] + " ";
                    }
                    URL url = new URL("https://cataas.com/c/s/" + text);
                    InputStream image = url.openStream();
                    channel.sendFile(image,"cat.jpg").queue();
                }
                else if(content.contains("gif")){
                    URL url = new URL("https://cataas.com/cat/gif");
                    InputStream image = url.openStream();
                    channel.sendFile(image,"cat.jpg").queue();
                }
                else if(content.contains("filter")){
                    String filter = "";
                    filter += tab[3];
                    URL url = new URL("https://cataas.com/cat?filter="+ filter);
                    InputStream image = url.openStream();
                    channel.sendFile(image,"cat.jpg").queue();
                }
                else{
                    URL url = new URL("https://cataas.com/c");
                    InputStream image = url.openStream();
                    channel.sendFile(image,"cat.jpg").queue();
                }
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (content.contains(bot_prefix +" meteo")){
            /*String meteo = showMeteo(e, meteo);;
            MessageChannel channel = event.getChannel();
            channel.sendMessage(" "+ meteo +" ").queue(); // Important to call .queue() on the RestAction returned by sendMessage(...)*/
            MessageChannel channel = event.getChannel();
            String[] tab = content.split(" ");
            String meteo;

            if(tab.length > 2){
                String location = new String();
                for(int i=2; i<tab.length; i++){
                    location = location+tab[i]+" ";
                }
                meteo = getMeteo(event,location);
            } else {
                meteo = getMeteo(event,new String("Calais"));
            }

            JSONParser parser = new JSONParser();
            JSONObject jsonMeteo=null;
            try{
                jsonMeteo = (JSONObject) parser.parse(meteo);
            }catch(Exception ex){
                ex.printStackTrace();
            }

            showMeteo(event,jsonMeteo);
        }
    }
}
