
package com.dicoding.menirukanmu;



import com.google.gson.Gson;
import com.linecorp.bot.client.LineMessagingServiceBuilder;
import com.linecorp.bot.client.LineSignatureValidator;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.StickerMessage;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.model.response.BotApiResponse;

import com.sun.org.apache.bcel.internal.generic.PUSH;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Response;

@RestController
@RequestMapping(value="/linebot")
public class LineBotController
{

    public JSONObject jsonNation(String id) {
        JSONObject jsonObject = null;
        try {
            jsonObject = readJsonFromUrl("https://restcountries.eu/rest/v2/alpha/"+id);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }


    public String bukalapakUrl(String produk)
    {
        URL url;
        InputStream is = null;
        BufferedReader br;
        String line;
        String jsonString="";



        try {
            url = new URL("https://api.bukalapak.com/v2/products.json?keywords="+produk+"&page=1&top_seller=1&per_page=1&u=67287:lXymG93y83m6RHzZV5FY");
            is = url.openStream();  // throws an IOException
            br = new BufferedReader(new InputStreamReader(is));

            while ((line = br.readLine()) != null) {
                //  System.out.println(line);
                jsonString+=line;
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

    public String osuUrl(String nickname,String mode)
    {
        URL url;
        InputStream is = null;
        BufferedReader br;
        String line;
        String jsonString="";



        try {
            url = new URL("https://osu.ppy.sh/api/get_user?u="+nickname+"&k=37967304c711a663eb326dcf8b41e1a5987e2b7f&m="+mode);
            is = url.openStream();  // throws an IOException
            br = new BufferedReader(new InputStreamReader(is));

            while ((line = br.readLine()) != null) {
                //  System.out.println(line);
                jsonString+=line;
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
    public void jsonResultForOsu(String msgText,String idTarget,String mode,String osuMode)
    {

        String string = msgText.toString();
        String[] parts = string.split(" ");
        String part2 = parts[1];

        try {
            String username="";
            String countryRank="";
            String pprank="";
            String country="";
            String accuracy="";

            JSONArray jsonArray=new JSONArray(osuUrl(part2,mode));
            for(int a=0;a<jsonArray.length();a++)
            {
                JSONObject jsonObject = jsonArray.getJSONObject(a);
                username=jsonObject.getString("username");
                countryRank=jsonObject.getString("pp_country_rank");
                pprank=jsonObject.getString("pp_rank");
                country=jsonObject.getString("country");
                accuracy=jsonObject.getString("accuracy");
            }

            getMessageData("Username: "+username+" from "+jsonNation(country).getString("name")+"\nMode: "+osuMode+"\nCountry Rank: "+countryRank+"\nGlobal Rank: "+pprank+"\nAccuracy: "+Math.round(Double.parseDouble(accuracy))+"%",idTarget);
          // getMessageData(osuUrl("deceitful","2"),idTarget);
        } catch (IOException e) {
            e.printStackTrace();
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

    @RequestMapping(value="/callback", method=RequestMethod.POST)
    public ResponseEntity<String> callback(
        @RequestHeader("X-Line-Signature") String aXLineSignature,
        @RequestBody String aPayload)
    {
        final String text=String.format("The Signature is: %s",
            (aXLineSignature!=null && aXLineSignature.length() > 0) ? aXLineSignature : "N/A");
        System.out.println(text);
        final boolean valid=new LineSignatureValidator(lChannelSecret.getBytes()).validateSignature(aPayload.getBytes(), aXLineSignature);
        System.out.println("The signature is: " + (valid ? "valid" : "tidak valid"));
        if(aPayload!=null && aPayload.length() > 0)
        {
            System.out.println("Payload: " + aPayload);
        }
        Gson gson = new Gson();
        Payload payload = gson.fromJson(aPayload, Payload.class);


        String msgText = " ";
        String idTarget = " ";
        String eventType = payload.events[0].type;


        if (eventType.equals("join")){
            if (payload.events[0].source.type.equals("group")){
                replyToUser(payload.events[0].replyToken, "Hello Group");

            }
            if (payload.events[0].source.type.equals("room")){
                replyToUser(payload.events[0].replyToken, "Hello Room");
            }
        } else if (eventType.equals("message")){
            if (payload.events[0].source.type.equals("group")){
                idTarget = payload.events[0].source.groupId;
            } else if (payload.events[0].source.type.equals("room")){
                idTarget = payload.events[0].source.roomId;
            } else if (payload.events[0].source.type.equals("user")){
                idTarget = payload.events[0].source.userId;
            }

            if (!payload.events[0].message.type.equals("text")) {
                // replyToUser(payload.events[0].replyToken, "Unknown message");
                if(payload.events[0].message.type.equals("sticker"))
                {
                    try {
                        getMessageDataForSticker(idTarget);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                msgText = payload.events[0].message.text;
                msgText = msgText.toLowerCase();



                if (!msgText.contains("bot leave")){
                    if(msgText.contains("/weather"))
                    {
                        String string = msgText.toString();
                        String[] parts = string.split(" ");

                        String part2 = parts[1];
                        try {

                            JSONObject json = readJsonFromUrl("http://api.openweathermap.org/data/2.5/weather?q="+part2+"&APPID=fe18035f6b83c8b163d1a7a8ef934a75");
                            JSONObject jsonForeCast = readJsonFromUrl("http://api.openweathermap.org/data/2.5/forecast?q=jakarta&appid=fe18035f6b83c8b163d1a7a8ef934a75");

                            String weather = json.get("weather").toString();

                            //variable
                            String message="";


                            //jsonobject
                            JSONObject jsonSys = json.getJSONObject("sys");
                            String country = jsonSys.getString("country");
                            JSONObject jsonCity = jsonForeCast.getJSONObject("city");
                            String city = jsonCity.getString("name");

                            //array
                            JSONArray arr = new JSONArray(weather);

                            boolean counter = false;
                            for(int i=0;i<arr.length();i++)
                            {
                                JSONObject jsonPart =arr.getJSONObject(i);
                                String main ="";
                                String description="";

                                main = jsonPart.getString("main");
                                description = jsonPart.getString("description");

                                if(main != "" && description!="")
                                {
                                    message+=main+": "+description;
                                    counter = true;
                                }
                            }
                            if(counter)
                            {
                                getMessageData("current weather on " + part2 + "," + country + " is " + message, idTarget);
                            }
                            else
                            {
                                getMessageData("don't know",idTarget);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    //osu
                    if(msgText.contains("/mania"))
                    {
                        jsonResultForOsu(msgText.toLowerCase().toString(),idTarget,"3","osu!Mania");
                    }
                    if(msgText.contains("/ctb"))
                    {
                        jsonResultForOsu(msgText.toLowerCase().toString(),idTarget,"2","Catch the Beat");
                    }
                    if(msgText.contains("/taiko"))
                    {
                        jsonResultForOsu(msgText.toLowerCase().toString(),idTarget,"1","Taiko");
                    }
                    if(msgText.contains("/std"))
                    {
                        jsonResultForOsu(msgText.toLowerCase().toString(),idTarget,"0","Osu Standard!");
                    }
                    if(msgText.contains("/puasa"))
                    {
                        //5db94b590c066277ad540f984a288bac
                        String string = msgText.toString();
                        String[] parts = string.split(" ");
                        String part2 = parts[1];
                        String date_for="";
                        String fajr="";
                        String shurooq ="";
                        String dhuhr="";
                        String asr="";
                        String maghrib="";
                        String isha="";


                        try {
                           // JSONObject json= readJsonFromUrl("http://muslimsalat.com/"+part2+".json?key=5db94b590c066277ad540f984a288bac");
                            JSONObject json = readJsonFromUrl("http://muslimsalat.com/"+part2+"/daily/07-06-2017.json?key=5db94b590c066277ad540f984a288bac");
                            JSONArray jsonArray = new JSONArray(json.get("items").toString());

                            for(int a=0;a<jsonArray.length();a++)
                            {
                                JSONObject jsonObject = jsonArray.getJSONObject(a);
                                date_for=jsonObject.getString("date_for");
                                fajr=jsonObject.getString("fajr");
                                shurooq=jsonObject.getString("shurooq");
                                dhuhr=jsonObject.getString("dhuhr");
                                asr=jsonObject.getString("asr");
                                maghrib=jsonObject.getString("maghrib");
                                isha=jsonObject.getString("isha");

                                getMessageData(json.get("state").toString()+", "+json.get("country").toString()+"\n"+"date: "+date_for+"\nfajr: "+fajr+"\nshurooq: "+shurooq+"\ndhuhr: "+dhuhr+"\nasr: "+asr+"\nmaghrib: "+maghrib+"\nisha: "+isha+"\n -http://muslimsalat.com-",idTarget);

                            }



                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                    if(msgText.contains("hello"))
                    {
                        //162a37b7350d4aaaa9f2c0df18bf3a54
                        try {
                           // getMessageData("debug",idTarget);
                            getMessageData("Hello "+profile(payload.events[0].source.userId)+" can i help you? just type /help",idTarget);

                          //  profile(payload.events[0].message.id);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if(msgText.contains("love live"))
                    {
                        try {
                            getMessageData("did i hear love live????",idTarget);
                             getMessageDataForImage(idTarget);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if(msgText.contains("/bukalapak"))
                    {
                        String hasil = "";

                        Pattern p =Pattern.compile("/bukalapak (.*?);");
                        Matcher m =p.matcher(msgText.toLowerCase().toString());
                        while (m.find()) {


                            hasil=m.group(1);

                        }
                        int price=0;
                        int positive=0;
                        int negative=0;
                        String seller_name="";


                        try {
                            JSONObject jsonObject = readJsonFromUrl("https://api.bukalapak.com/v2/products.json?keywords="+hasil+"&page=1&top_seller=1&per_page=1");

                            JSONArray jsonArray = new JSONArray(jsonObject.get("products").toString());

                            for(int a=0;a<jsonArray.length();a++)
                            {
                                JSONObject jsonPart = jsonArray.getJSONObject(a);
                                price=jsonPart.getInt("price");
                                positive=jsonPart.getInt("seller_positive_feedback");
                                negative=jsonPart.getInt("seller_negative_feedback");
                                seller_name=jsonPart.getString("seller_name");

                            }
                            getMessageData(hasil,idTarget);
                            getMessageData("Seller Name: "+seller_name+"\nPositive Rating: "+Integer.toString(positive)+"\nNegative Rating"+Integer.toString(negative)+"\nPrice: Rp. "+Integer.toString(price),idTarget);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }


                    if(msgText.contains("/help"))
                    {

                        try {
                            getMessageData("command list : /weather [city name] \n/[osu_mode] [nickname] eg : /mania jakads\n/puasa [city_name]\nunder development for personal amusement -titus efferian",idTarget);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }


                } else {
                    if (payload.events[0].source.type.equals("group")){
                        leaveGR(payload.events[0].source.groupId, "group");
                    } else if (payload.events[0].source.type.equals("room")){
                        leaveGR(payload.events[0].source.roomId, "room");
                    }
                }

            }
        }
         
        return new ResponseEntity<String>(HttpStatus.OK);
    }

    private void getMessageData(String message, String targetID) throws IOException{
        if (message!=null){
            pushMessage(targetID, message);
        }
    }
    private void getMessageDataForSticker(String targetID) throws  IOException
    {
        pushSticker(targetID);
    }
    private void getMessageDataForImage(String targetId)throws  IOException
    {
        pushImageMessage(targetId);
    }



    private void replyToUser(String rToken, String messageToUser){
        TextMessage textMessage = new TextMessage(messageToUser);
        ReplyMessage replyMessage = new ReplyMessage(rToken, textMessage);

        try {
            Response<BotApiResponse> response = LineMessagingServiceBuilder
                .create(lChannelAccessToken)
                .build()
                .replyMessage(replyMessage)
                .execute();
            System.out.println("Reply Message: " + response.code() + " " + response.message());
        } catch (IOException e) {
            System.out.println("Exception is raised ");
            e.printStackTrace();
        }
    }
    private void pushImageMessage(String sourceId)
    {
        ImageMessage imageMessage = new ImageMessage("https://bisakimiadotcom.files.wordpress.com/2015/03/trash.jpg","https://bisakimiadotcom.files.wordpress.com/2015/03/trash.jpg");

        // ImageMessage imageMessage = new ImageMessage("http://muslimsalat.com/qibla_compass/200/188.82.png","http://muslimsalat.com/qibla_compass/200/188.82.png");
        PushMessage pushMessage=new PushMessage(sourceId,imageMessage);
       response(pushMessage);
    }

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
    }
    private void pushSticker(String sourceID)
    {
        Random random = new Random();
        StickerMessage sticker = new StickerMessage("1",Integer.toString(random.nextInt(17)+1));
        PushMessage pushMessage = new PushMessage(sourceID,sticker);
        response(pushMessage);
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

