
package com.dicoding.menirukanmu;



import com.google.gson.Gson;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.client.LineMessagingService;
import com.linecorp.bot.client.LineMessagingServiceBuilder;

import com.linecorp.bot.client.LineSignatureValidator;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.*;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.model.response.BotApiResponse;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Response;

import static java.lang.Math.round;

@RestController
@RequestMapping(value="/linebot")
public class LineBotController {
    public String splitter(String msgText, String splitter, String content) {
        String hasil = "";
        if (!msgText.substring(msgText.length() - 1).equals(";")) {
            return null;
        } else {
            if (msgText.contains(content)) {
                String string = msgText.toString();

                Pattern p = Pattern.compile(splitter);
                Matcher m = p.matcher(string);

                while (m.find()) {
                    hasil = m.group(1);
                    // hasil = hasil.replaceAll(" ", "%20");
                }
            }
        }

        return hasil;
    }


    public JSONObject jsonNation(String id) {
        JSONObject jsonObject = null;
        try {
            jsonObject = readJsonFromUrl("https://restcountries.eu/rest/v2/alpha/" + id);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }


    public String osuUrl(String nickname, String mode) {
        URL url;
        InputStream is = null;
        BufferedReader br;
        String line;
        String jsonString = "";


        try {
            url = new URL("https://osu.ppy.sh/api/get_user?u=" + nickname + "&k=37967304c711a663eb326dcf8b41e1a5987e2b7f&m=" + mode);
            is = url.openStream();  // throws an IOException
            br = new BufferedReader(new InputStreamReader(is));

            while ((line = br.readLine()) != null) {
                //  System.out.println(line);
                jsonString += line;
                // getMessageData(line,idTarget);
            }

        } catch (MalformedURLException mue) {
            mue.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ioe) {
                // nothing to see here
            }
        }


        return jsonString;
    }

    public void jsonResultForOsu(String msgText, String idTarget, String mode, String osuMode) {

        if (msgText == null) {
            try {
                getMessageData("you forgot the semicolon; eg: /mania nickname;", idTarget);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {

            try {
                String username = "";
                String countryRank = "";
                String pprank = "";
                String country = "";
                String accuracy = "";
                String userid = "";

                JSONArray jsonArray = new JSONArray(osuUrl(msgText, mode));
                for (int a = 0; a < jsonArray.length(); a++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(a);
                    username = jsonObject.getString("username");
                    countryRank = jsonObject.getString("pp_country_rank");
                    pprank = jsonObject.getString("pp_rank");
                    country = jsonObject.getString("country");
                    accuracy = jsonObject.getString("accuracy");
                    userid = jsonObject.getString("user_id");
                }
                if (username == "") {
                    getMessageData("don't know", idTarget);
                }

                replyForMessageContaintImages("Username: " + username + " from " + jsonNation(country).getString("name") + "\nMode: " + osuMode + "\nCountry Rank: " + countryRank + "\nGlobal Rank: " + pprank + "\nAccuracy: " + round(Double.parseDouble(accuracy)) + "%", "https://a.ppy.sh/"+userid,idTarget);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);

            return json;
        } finally {
            is.close();
        }
    }

    @Autowired
    @Qualifier("com.linecorp.channel_secret")
    String lChannelSecret;

    @Autowired
    @Qualifier("com.linecorp.channel_access_token")
    String lChannelAccessToken;

    @RequestMapping(value = "/callback", method = RequestMethod.POST)
    public ResponseEntity<String> callback(
            @RequestHeader("X-Line-Signature") String aXLineSignature,
            @RequestBody String aPayload) {
        final String text = String.format("The Signature is: %s",
                (aXLineSignature != null && aXLineSignature.length() > 0) ? aXLineSignature : "N/A");
        System.out.println(text);
        final boolean valid = new LineSignatureValidator(lChannelSecret.getBytes()).validateSignature(aPayload.getBytes(), aXLineSignature);
        System.out.println("The signature is: " + (valid ? "valid" : "tidak valid"));
        if (aPayload != null && aPayload.length() > 0) {
            System.out.println("Payload: " + aPayload);
        }
        Gson gson = new Gson();
        Payload payload = gson.fromJson(aPayload, Payload.class);


        String videoMsgTxt = "";
        String msgText = " ";
        String idTarget = " ";
        // String idToken = payload.events[0].replyToken;
        String eventType = payload.events[0].type;


        if (eventType.equals("join")) {
            if (payload.events[0].source.type.equals("group")) {
                replyToUser(payload.events[0].replyToken, "Hello Group");


            }
            if (payload.events[0].source.type.equals("room")) {
                replyToUser(payload.events[0].replyToken, "Hello Room");
                replyToUser(payload.events[0].replyToken, "hello my room");
            }
        } else if (eventType.equals("message")) {
            if (payload.events[0].source.type.equals("group")) {
                idTarget = payload.events[0].replyToken;
            } else if (payload.events[0].source.type.equals("room")) {
                idTarget = payload.events[0].replyToken;
            } else if (payload.events[0].source.type.equals("user")) {
                idTarget = payload.events[0].replyToken;
            }

            if (!payload.events[0].message.type.equals("text")) {
                // replyToUser(payload.events[0].replyToken, "Unknown message");
             /*   if(payload.events[0].message.type.equals("sticker"))
                {
                    try {
                        getMessageDataForSticker(idTarget);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }*/
            } else {
                msgText = payload.events[0].message.text;
                videoMsgTxt = payload.events[0].message.text;
                msgText = msgText.toLowerCase();


                if (!msgText.contains("bot leave")) {
                    if (msgText.contains("/weather")) {


                        String part2 = splitter(msgText + ";", "/weather (.*?);", "/weather");
                        try {

                            JSONObject json = readJsonFromUrl("http://api.openweathermap.org/data/2.5/weather?q=" + part2 + "&APPID=fe18035f6b83c8b163d1a7a8ef934a75");
                            JSONObject jsonForeCast = readJsonFromUrl("http://api.openweathermap.org/data/2.5/forecast?q=jakarta&appid=fe18035f6b83c8b163d1a7a8ef934a75");

                            String weather = json.get("weather").toString();

                            //variable
                            String message = "";


                            //jsonobject
                            JSONObject jsonSys = json.getJSONObject("sys");
                            String country = jsonSys.getString("country");
                            JSONObject jsonCity = jsonForeCast.getJSONObject("city");
                            String city = jsonCity.getString("name");

                            //array
                            JSONArray arr = new JSONArray(weather);

                            boolean counter = false;
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject jsonPart = arr.getJSONObject(i);
                                String main = "";
                                String description = "";

                                main = jsonPart.getString("main");
                                description = jsonPart.getString("description");

                                if (main != "" && description != "") {
                                    message += main + ": " + description;
                                    counter = true;
                                }
                            }
                            if (part2 == null) {
                                getMessageData("you forgot the semicolon; eg: /weather cityName;", idTarget);
                            } else {
                                if (counter) {
                                    getMessageData("current weather on " + part2 + "," + country + " is " + message, idTarget);
                                } else {
                                    getMessageData("don't know", idTarget);
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    //osu
                    if (msgText.contains("/mania")) {
                        jsonResultForOsu(splitter(msgText + ";", "/mania (.*?);", "/mania"), idTarget, "3", "osu!Mania");
                    }
                    if (msgText.contains("/ctb")) {
                        jsonResultForOsu(splitter(msgText + ";", "/ctb (.*?);", "/ctb"), idTarget, "2", "Catch the Beat");
                    }
                    if (msgText.contains("/taiko")) {
                        jsonResultForOsu(splitter(msgText + ";", "/taiko (.*?);", "/taiko"), idTarget, "1", "Taiko");
                    }
                    if (msgText.contains("/std")) {
                        jsonResultForOsu(splitter(msgText + ";", "/std (.*?);", "/std"), idTarget, "0", "Osu Standard!");
                    }
                    if (msgText.contains("/puasa")) {
                        //5db94b590c066277ad540f984a288bac


                      //  String string = msgText.toString();
                      //  String[] parts = string.split(" ");
                        String part2 = splitter(msgText+";",("/puasa (.*?);"),"/puasa");
                        String date_for = "";
                        String fajr = "";
                        String shurooq = "";
                        String dhuhr = "";
                        String asr = "";
                        String maghrib = "";
                        String isha = "";


                        try {
                            // JSONObject json= readJsonFromUrl("http://muslimsalat.com/"+part2+".json?key=5db94b590c066277ad540f984a288bac");
                            JSONObject json = readJsonFromUrl("http://muslimsalat.com/" + part2 + "/daily/09-06-2017.json?key=5db94b590c066277ad540f984a288bac");
                            JSONArray jsonArray = new JSONArray(json.get("items").toString());

                            for (int a = 0; a < jsonArray.length(); a++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(a);
                                date_for = jsonObject.getString("date_for");
                                fajr = jsonObject.getString("fajr");
                                shurooq = jsonObject.getString("shurooq");
                                dhuhr = jsonObject.getString("dhuhr");
                                asr = jsonObject.getString("asr");
                                maghrib = jsonObject.getString("maghrib");
                                isha = jsonObject.getString("isha");

                                getMessageData(json.get("state").toString() + ", " + json.get("country").toString() + "\n" + "date: " + date_for + "\nfajr: " + fajr + "\nshurooq: " + shurooq + "\ndhuhr: " + dhuhr + "\nasr: " + asr + "\nmaghrib: " + maghrib + "\nisha: " + isha + "\n -http://muslimsalat.com-", idTarget);

                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                    if (msgText.contains("hello")) {
                        //162a37b7350d4aaaa9f2c0df18bf3a54
                        try {
                            // getMessageData("debug",idTarget);
                            getMessageData("Hello " + profile(payload.events[0].source.userId) + " can i help you? just type /help", idTarget);

                            //  profile(payload.events[0].message.id);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if(msgText.contains("/convert"))
                    {

                        String variable1="";
                        String variable2="";
                        int number=0;

                        Pattern pattern = Pattern.compile("/convert (.*?) ");
                        Matcher matcher = pattern.matcher(msgText);

                        while (matcher.find())
                        {
                            number=Integer.parseInt(matcher.group(1));
                        }

                        number = round(number);

                        Pattern p1 = Pattern.compile("/convert "+number+" (.*?) to");
                        Matcher m1 = p1.matcher(msgText);

                        Pattern p2 = Pattern.compile("to (.*?);");
                        Matcher m2 = p2.matcher(msgText+";");

                        while(m1.find())
                        {
                            variable1=m1.group(1);

                        }
                        while (m2.find())
                        {
                            variable2=m2.group(1);
                        }
                        try {
                            JSONObject jsonObject = readJsonFromUrl("http://api.fixer.io/latest?base="+variable1);
                            JSONObject rates = new JSONObject(jsonObject.get("rates").toString());
                            Double d = rates.getDouble(variable2.toUpperCase());
                           // long l = rates.getLong(variable2.toUpperCase());
                            DecimalFormat formatter = new DecimalFormat("###.###");
                            String currencyResult = formatter.format(Math.floor((d*number)*100)/100);
                            String currency = NumberFormat.getNumberInstance().format(Math.floor((d*round(number))*100)/100);


                            replyToUser(idTarget,"latest currency on: "+jsonObject.get("date").toString()+"\n"+number+" "+variable1.toUpperCase()+" = "+variable2.toUpperCase()+" "+currencyResult);



                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (msgText.contains("/bukalapak")) {
                        String string = msgText.toString() + ";";
                        String hasil = "";

                        if (!string.substring(string.length() - 1).equals(";")) {
                            try {
                                getMessageData("you forgot the semicolon; eg: /bukalapak productName;", idTarget);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {


                            Pattern p = Pattern.compile("/bukalapak (.*?);");
                            Matcher m = p.matcher(string);

                            int price = 0;
                            int positive = 0;
                            int negative = 0;
                            String seller_name = "";
                            String url = "";
                            String name = "";
                            String imagesUrl = "";

                            while (m.find()) {
                                hasil = m.group(1);
                                hasil = hasil.replaceAll(" ", "%20");
                            }
                            try {
                                JSONObject jsonObject = readJsonFromUrl("https://api.bukalapak.com/v2/products.json?keywords=" + hasil + "&page=1&top_seller=1&per_page=1");
                                JSONArray jsonArray = new JSONArray(jsonObject.get("products").toString());
                                for (int a = 0; a < jsonArray.length(); a++) {
                                    JSONObject jsonPart = jsonArray.getJSONObject(a);

                                    JSONArray jsonImages = new JSONArray(jsonPart.get("images").toString());

                                    imagesUrl = jsonImages.getString(0);


                                    price = jsonPart.getInt("price");
                                    positive = jsonPart.getInt("seller_positive_feedback");
                                    negative = jsonPart.getInt("seller_negative_feedback");
                                    seller_name = jsonPart.getString("seller_name");
                                    url = jsonPart.getString("url");
                                    name = jsonPart.getString("name");


                                }
                                if (seller_name == "" && price == 0) {
                                    getMessageData("don't know", idTarget);
                                } else {
                                    replyForMessageContaintImages("Seller Name: " + seller_name + "\nPositive Rating: " + Integer.toString(positive) + "\nNegative Rating: " + Integer.toString(negative) + "\nName: " + name + "\nPrice: Rp. " + Integer.toString(price) + "\n" + url, imagesUrl,idTarget);
                                   // getMessageDataForImage(payload.events[0].replyToken, imagesUrl);
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (videoMsgTxt.contains("/video")) {
                        try {
                            String splitterString = splitter(videoMsgTxt + ";", "/video (.*?);", "/video");
                            JSONObject jsonObject = readJsonFromUrl("http://megumin-yt.herokuapp.com/api/info?url=" + splitterString);
                            JSONObject info = new JSONObject(jsonObject.get("info").toString());
                            JSONArray jsonArray = new JSONArray(info.get("formats").toString());
                            String result = "";


                            for (int a = 0; a < jsonArray.length(); a++) {
                                JSONObject jsonPart = jsonArray.getJSONObject(a);
                                result = jsonPart.getString("url");

                            }
                            replyVideoMessage(payload.events[0].replyToken, result, info.get("thumbnail").toString());

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                    if(msgText.contains("/weekly anime"))
                    {
                        try {
                            getMessageDataForImage(idTarget,"https://scontent-sit4-1.xx.fbcdn.net/v/t1.0-9/18839085_1919727671598077_3075675389604525350_n.png?oh=b4a2c4823de7e3d83443073da5e9f0b9&oe=59ACFC8F");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if(msgText.contains("/weekly couple"))
                    {
                        try {
                            getMessageDataForImage(idTarget,"https://scontent-sit4-1.xx.fbcdn.net/v/t31.0-0/p480x480/18954935_1922328001338044_5723289886971793934_o.png?oh=b36a6810342f82e3125f85fcba01bbde&oe=59DB252A");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }


                    if (msgText.contains("/help")) {
                        try {
                            // getMessageData("command list :\n/weather city name\n/osu_mode nickname eg : /mania jakads;\n/puasa city_name\n/bukalapak product_name\n/video youtubelink;\n\nbot leave for kick out this shit\n\n\nunder development for personal amusement\n-titus efferian",idTarget);
                            getMessageData("command list :\n/weather city name\n/osu_mode nickname eg : /mania jakads;\n/puasa city_name\n/bukalapak product_name\n/video youtubelink;\n/convert 10 USD to IDR\n/weekly anime\n/weekly couple\nbot leave for kick out this shit\n\n\nunder development for personal amusement\n-titus efferian & kato@linuxsec.org", payload.events[0].replyToken);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }




                } else {
                    String imagesUrl = "https://myanimelist.cdn-dena.com/r/360x360/images/characters/5/325307.jpg?s=781315723350f071b8fcf201f626a731";
                    if (payload.events[0].source.type.equals("group")) {

                        // getMessageData("my name is Tamachan, i'm the one who is going to beat hibiki!",idTarget);
                        //  replyToUser(idToken,"my name is Tamachan, i'm the one who is going to beat hibiki!");
                       // replyForMessageContaintImages("my name is meguri, i'm the one who is going to beat hibiki!",imagesUrl,idTarget);

                        leaveGR(payload.events[0].source.groupId, "group");
                    } else if (payload.events[0].source.type.equals("room")) {
                       // replyForMessageContaintImages("my name is meguri, i'm the one who is going to beat hibiki!",imagesUrl,idTarget);

                        try {
                            getMessageDataForImage(idTarget, imagesUrl);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        leaveGR(payload.events[0].source.roomId, "room");
                    }
                }

            }
        }

        return new ResponseEntity<String>(HttpStatus.OK);
    }
    /*
    private void getMessageData(String message, String targetID) throws IOException{
        if (message!=null){
            pushMessage(targetID, message);
        }
    }*/


    private void getMessageData(String message, String targetId) throws IOException {

        replyToUser(targetId, message);

    }/*
    private void getMessageDataForImage(String targetId,String string)throws  IOException
    {
        replyImageMessage(targetId,string);
    }
    private void getMessageDataForVideo(String targetId,String videoString,String imageString)throws  IOException
    {
        replyVideoMessage(targetId,videoString,imageString);
    }*/
    /*

    private void getMessageDataForImage(String targetId,String string)throws  IOException
    {
        pushImageMessage(targetId,string);
    }*/
    /*
    private void getVideoData(String targetId,String videoString,String imageString)
    {
        pushVideoMessage(targetId,videoString,imageString);
    }*/


    private void replyForMessageContaintImages(String textMessage,String imageUrl,String sourceId) {

        ReplyMessage replyMessage = new ReplyMessage(sourceId,Arrays.asList(new TextMessage(textMessage),new ImageMessage(imageUrl,imageUrl)));

        try {
            Response<BotApiResponse> response = LineMessagingServiceBuilder
                    .create(lChannelAccessToken)
                    .build()
                    .replyMessage(replyMessage)
                    .execute();
            System.out.println(response.code() + " " + response.message());
        } catch (IOException e) {
            System.out.println("Exception is raised ");
            e.printStackTrace();
        }
    }









    private void replyToUser(String sourceId, String txt){


        TextMessage textMessage = new TextMessage(txt);
        ReplyMessage replyMessage = new ReplyMessage(sourceId,textMessage);
        try {

            Response<BotApiResponse> response = LineMessagingServiceBuilder
                    .create(lChannelAccessToken)
                    .build()
                    .replyMessage(replyMessage)
                    .execute();
            System.out.println(response.code() + " " + response.message());
        } catch (IOException e) {
            System.out.println("Exception is raised ");
            e.printStackTrace();
        }

    }
    private void replyVideoMessage(String sourceId,String videoString ,String imageString)throws IOException
    {
        VideoMessage videoMessage = new VideoMessage(videoString,imageString);
        ReplyMessage replyMessage = new ReplyMessage(sourceId,videoMessage);
        responseReply(replyMessage);
    }
    private void getMessageDataForImage(String sourceId,String string)throws IOException
    {
        ImageMessage imageMessage = new ImageMessage(string,string);
        // ImageMessage imageMessage = new ImageMessage("http://muslimsalat.com/qibla_compass/200/188.82.png","http://muslimsalat.com/qibla_compass/200/188.82.png");
        ReplyMessage replyMessage=new ReplyMessage(sourceId,imageMessage);
        responseReply(replyMessage);
    }
    /*
    private void pushVideoMessage(String sourceId,String videoString,String imageString)
    {
        VideoMessage videoMessage = new VideoMessage(videoString,imageString);
        PushMessage pushMessage = new PushMessage(sourceId,videoMessage);
        response(pushMessage);
    }*/

    /*
    private void pushImageMessage(String sourceId,String string)
    {
        ImageMessage imageMessage = new ImageMessage(string,string);

        // ImageMessage imageMessage = new ImageMessage("http://muslimsalat.com/qibla_compass/200/188.82.png","http://muslimsalat.com/qibla_compass/200/188.82.png");
        PushMessage pushMessage=new PushMessage(sourceId,imageMessage);
       response(pushMessage);
    }*/
    /*
    private void response(PushMessage pushMessage)
    {
        try {
            Response<BotApiResponse> response = LineMessagingServiceBuilder
                    .create(lChannelAccessToken)
                    .build()
                    .pushMessage(pushMessage)
                    .execute();
            System.out.println(response.code() + " " + response.message());
        } catch (IOException e) {
            System.out.println("Exception is raised ");
            e.printStackTrace();
        }
    }*/
    private void responseReply(ReplyMessage replyMessage)
    {
        try {
            Response<BotApiResponse> response = LineMessagingServiceBuilder
                    .create(lChannelAccessToken)
                    .build()
                    .replyMessage(replyMessage)
                    .execute();
            System.out.println(response.code() + " " + response.message());
        } catch (IOException e) {
            System.out.println("Exception is raised ");
            e.printStackTrace();
        }
    }

    private void pushMessage(String sourceId, String txt){
        TextMessage textMessage = new TextMessage(txt);
        PushMessage pushMessage = new PushMessage(sourceId,textMessage);
        try {
            Response<BotApiResponse> response = LineMessagingServiceBuilder
            .create(lChannelAccessToken)
            .build()
            .pushMessage(pushMessage)
            .execute();
            System.out.println(response.code() + " " + response.message());
        } catch (IOException e) {
            System.out.println("Exception is raised ");
            e.printStackTrace();
        }

    }

    private String profile(String userId)
    {

        Response<UserProfileResponse> response = null;

        try {
            response = LineMessagingServiceBuilder
                    .create(lChannelAccessToken)
                    .build()
                    .getProfile(userId)
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (response.isSuccessful()) {
            UserProfileResponse profile = response.body();
            System.out.println(profile.getDisplayName());
            System.out.println(profile.getPictureUrl());
            System.out.println(profile.getStatusMessage());
        } else {
            System.out.println(response.code() + " " + response.message());
        }

        return response.body().getDisplayName();
    }
    private void leaveGR(String id, String type){
        try {
            if (type.equals("group")){
                Response<BotApiResponse> response = LineMessagingServiceBuilder
                    .create(lChannelAccessToken)
                    .build()
                    .leaveGroup(id)
                    .execute();
                System.out.println(response.code() + " " + response.message());
            } else if (type.equals("room")){
                Response<BotApiResponse> response = LineMessagingServiceBuilder
                    .create(lChannelAccessToken)
                    .build()
                    .leaveRoom(id)
                    .execute();
                System.out.println(response.code() + " " + response.message());
            }
        } catch (IOException e) {
            System.out.println("Exception is raised ");
            e.printStackTrace();
        }
    }
}


//kairos

/*App name
personal's App
App ID
59a354d5
Key
4eea450f6518a2d5cbb2291e1b5fbd39*/

