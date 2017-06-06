
package com.dicoding.menirukanmu;



import com.google.gson.Gson;
import com.linecorp.bot.client.LineMessagingServiceBuilder;
import com.linecorp.bot.client.LineSignatureValidator;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.response.BotApiResponse;

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

import retrofit2.Response;

@RestController
@RequestMapping(value="/linebot")
public class LineBotController
{

    public String ramadhanUrl() {
        URL url;
        InputStream is = null;
        BufferedReader br;
        String line;
        String jsonString = "";


        try {
            url = new URL("http://muslimsalat.com/monthly.json");
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
            getMessageData("Username: "+username+" from "+country+"\nMode: "+osuMode+"\nCountry Rank: "+countryRank+"\nGlobal Rank: "+pprank+"\nAccuracy: "+Math.round(Double.parseDouble(accuracy))+"%",idTarget);
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

            if (!payload.events[0].message.type.equals("text")){
               // replyToUser(payload.events[0].replyToken, "Unknown message");
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
                       String jsonPuasa ="{\"title\":\"\",\"query\":null,\"for\":\"monthly\",\"method\":1,\"prayer_method_name\":\"Egyptian General Authority of Survey\",\"daylight\":\"0\",\"timezone\":\"7\",\"map_image\":\"http:\\/\\/maps.google.com\\/maps\\/api\\/staticmap?center=-6.214620,106.845000&sensor=false&zoom=13&size=300x300\",\"sealevel\":\"3\",\"today_weather\":{\"pressure\":\"1009\",\"temperature\":\"31\"},\"link\":\"http:\\/\\/muslimsalat.com\\/\",\"qibla_direction\":\"295.12\",\"latitude\":\"-6.214620\",\"longitude\":\"106.845000\",\"address\":\"\",\"city\":\"\",\"state\":\"Jakarta\",\"postal_code\":\"\",\"country\":\"Indonesia\",\"country_code\":\"ID\",\"items\":[{\"date_for\":\"2017-6-6\",\"fajr\":\"4:34 am\",\"shurooq\":\"5:54 am\",\"dhuhr\":\"11:51 am\",\"asr\":\"3:13 pm\",\"maghrib\":\"5:47 pm\",\"isha\":\"6:59 pm\"},{\"date_for\":\"2017-6-7\",\"fajr\":\"4:35 am\",\"shurooq\":\"5:54 am\",\"dhuhr\":\"11:51 am\",\"asr\":\"3:13 pm\",\"maghrib\":\"5:48 pm\",\"isha\":\"6:59 pm\"},{\"date_for\":\"2017-6-8\",\"fajr\":\"4:35 am\",\"shurooq\":\"5:55 am\",\"dhuhr\":\"11:51 am\",\"asr\":\"3:13 pm\",\"maghrib\":\"5:48 pm\",\"isha\":\"6:59 pm\"},{\"date_for\":\"2017-6-9\",\"fajr\":\"4:35 am\",\"shurooq\":\"5:55 am\",\"dhuhr\":\"11:51 am\",\"asr\":\"3:13 pm\",\"maghrib\":\"5:48 pm\",\"isha\":\"6:59 pm\"},{\"date_for\":\"2017-6-10\",\"fajr\":\"4:35 am\",\"shurooq\":\"5:55 am\",\"dhuhr\":\"11:52 am\",\"asr\":\"3:14 pm\",\"maghrib\":\"5:48 pm\",\"isha\":\"6:59 pm\"},{\"date_for\":\"2017-6-11\",\"fajr\":\"4:35 am\",\"shurooq\":\"5:55 am\",\"dhuhr\":\"11:52 am\",\"asr\":\"3:14 pm\",\"maghrib\":\"5:48 pm\",\"isha\":\"7:00 pm\"},{\"date_for\":\"2017-6-12\",\"fajr\":\"4:35 am\",\"shurooq\":\"5:55 am\",\"dhuhr\":\"11:52 am\",\"asr\":\"3:14 pm\",\"maghrib\":\"5:48 pm\",\"isha\":\"7:00 pm\"},{\"date_for\":\"2017-6-13\",\"fajr\":\"4:36 am\",\"shurooq\":\"5:56 am\",\"dhuhr\":\"11:52 am\",\"asr\":\"3:14 pm\",\"maghrib\":\"5:49 pm\",\"isha\":\"7:00 pm\"},{\"date_for\":\"2017-6-14\",\"fajr\":\"4:36 am\",\"shurooq\":\"5:56 am\",\"dhuhr\":\"11:52 am\",\"asr\":\"3:14 pm\",\"maghrib\":\"5:49 pm\",\"isha\":\"7:00 pm\"},{\"date_for\":\"2017-6-15\",\"fajr\":\"4:36 am\",\"shurooq\":\"5:56 am\",\"dhuhr\":\"11:53 am\",\"asr\":\"3:15 pm\",\"maghrib\":\"5:49 pm\",\"isha\":\"7:00 pm\"},{\"date_for\":\"2017-6-16\",\"fajr\":\"4:36 am\",\"shurooq\":\"5:56 am\",\"dhuhr\":\"11:53 am\",\"asr\":\"3:15 pm\",\"maghrib\":\"5:49 pm\",\"isha\":\"7:01 pm\"},{\"date_for\":\"2017-6-17\",\"fajr\":\"4:36 am\",\"shurooq\":\"5:57 am\",\"dhuhr\":\"11:53 am\",\"asr\":\"3:15 pm\",\"maghrib\":\"5:49 pm\",\"isha\":\"7:01 pm\"},{\"date_for\":\"2017-6-18\",\"fajr\":\"4:37 am\",\"shurooq\":\"5:57 am\",\"dhuhr\":\"11:53 am\",\"asr\":\"3:15 pm\",\"maghrib\":\"5:50 pm\",\"isha\":\"7:01 pm\"},{\"date_for\":\"2017-6-19\",\"fajr\":\"4:37 am\",\"shurooq\":\"5:57 am\",\"dhuhr\":\"11:53 am\",\"asr\":\"3:15 pm\",\"maghrib\":\"5:50 pm\",\"isha\":\"7:01 pm\"},{\"date_for\":\"2017-6-20\",\"fajr\":\"4:37 am\",\"shurooq\":\"5:57 am\",\"dhuhr\":\"11:54 am\",\"asr\":\"3:16 pm\",\"maghrib\":\"5:50 pm\",\"isha\":\"7:01 pm\"},{\"date_for\":\"2017-6-21\",\"fajr\":\"4:37 am\",\"shurooq\":\"5:58 am\",\"dhuhr\":\"11:54 am\",\"asr\":\"3:16 pm\",\"maghrib\":\"5:50 pm\",\"isha\":\"7:02 pm\"},{\"date_for\":\"2017-6-22\",\"fajr\":\"4:38 am\",\"shurooq\":\"5:58 am\",\"dhuhr\":\"11:54 am\",\"asr\":\"3:16 pm\",\"maghrib\":\"5:50 pm\",\"isha\":\"7:02 pm\"},{\"date_for\":\"2017-6-23\",\"fajr\":\"4:38 am\",\"shurooq\":\"5:58 am\",\"dhuhr\":\"11:54 am\",\"asr\":\"3:16 pm\",\"maghrib\":\"5:51 pm\",\"isha\":\"7:02 pm\"},{\"date_for\":\"2017-6-24\",\"fajr\":\"4:38 am\",\"shurooq\":\"5:58 am\",\"dhuhr\":\"11:55 am\",\"asr\":\"3:16 pm\",\"maghrib\":\"5:51 pm\",\"isha\":\"7:02 pm\"},{\"date_for\":\"2017-6-25\",\"fajr\":\"4:38 am\",\"shurooq\":\"5:58 am\",\"dhuhr\":\"11:55 am\",\"asr\":\"3:17 pm\",\"maghrib\":\"5:51 pm\",\"isha\":\"7:03 pm\"},{\"date_for\":\"2017-6-26\",\"fajr\":\"4:38 am\",\"shurooq\":\"5:59 am\",\"dhuhr\":\"11:55 am\",\"asr\":\"3:17 pm\",\"maghrib\":\"5:51 pm\",\"isha\":\"7:03 pm\"},{\"date_for\":\"2017-6-27\",\"fajr\":\"4:39 am\",\"shurooq\":\"5:59 am\",\"dhuhr\":\"11:55 am\",\"asr\":\"3:17 pm\",\"maghrib\":\"5:52 pm\",\"isha\":\"7:03 pm\"},{\"date_for\":\"2017-6-28\",\"fajr\":\"4:39 am\",\"shurooq\":\"5:59 am\",\"dhuhr\":\"11:55 am\",\"asr\":\"3:17 pm\",\"maghrib\":\"5:52 pm\",\"isha\":\"7:03 pm\"},{\"date_for\":\"2017-6-29\",\"fajr\":\"4:39 am\",\"shurooq\":\"5:59 am\",\"dhuhr\":\"11:56 am\",\"asr\":\"3:18 pm\",\"maghrib\":\"5:52 pm\",\"isha\":\"7:03 pm\"},{\"date_for\":\"2017-6-30\",\"fajr\":\"4:39 am\",\"shurooq\":\"5:59 am\",\"dhuhr\":\"11:56 am\",\"asr\":\"3:18 pm\",\"maghrib\":\"5:52 pm\",\"isha\":\"7:04 pm\"}],\"status_valid\":1,\"status_code\":1,\"status_description\":\"Success.\"}";
                        try {
                            getMessageData(jsonPuasa,idTarget);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                    if(msgText.contains("/help"))
                    {
                        try {
                            getMessageData("command list : /weather [city name] \n/[osu_mode] [nickname] eg : /mania jakads\nunder development for personal amusement -titus efferian",idTarget);
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

