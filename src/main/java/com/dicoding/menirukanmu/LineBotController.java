
package com.dicoding.menirukanmu;



import com.google.gson.Gson;
import com.linecorp.bot.client.LineMessagingServiceBuilder;
import com.linecorp.bot.client.LineSignatureValidator;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.message.*;
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
    public String splitter(String msgText,String splitter,String content) {
        String hasil = "";
        if(!msgText.substring(msgText.length()-1).equals(";"))
        {
            return null;
        }
        else {
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
            jsonObject = readJsonFromUrl("https://restcountries.eu/rest/v2/alpha/"+id);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }


    public String youtubeUrl(String string)
    {
        URL url;
        InputStream is = null;
        BufferedReader br;
        String line;
        String jsonString="";
        try {
            url = new URL(string);
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

        if(msgText==null)
        {
            try {
                getMessageData("you forgot the semicolon; eg: /mania nickname;",idTarget);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {

            try {
                String username = "";
                String countryRank = "";
                String pprank = "";
                String country = "";
                String accuracy = "";
                String userid="";

                JSONArray jsonArray = new JSONArray(osuUrl(msgText, mode));
                for (int a = 0; a < jsonArray.length(); a++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(a);
                    username = jsonObject.getString("username");
                    countryRank = jsonObject.getString("pp_country_rank");
                    pprank = jsonObject.getString("pp_rank");
                    country = jsonObject.getString("country");
                    accuracy = jsonObject.getString("accuracy");
                    userid =jsonObject.getString("user_id");
                }
                if (username == "") {
                    getMessageData("don't know", idTarget);
                }

                getMessageData("Username: " + username + " from " + jsonNation(country).getString("name") + "\nMode: " + osuMode + "\nCountry Rank: " + countryRank + "\nGlobal Rank: " + pprank + "\nAccuracy: " + Math.round(Double.parseDouble(accuracy)) + "%", idTarget);
                getMessageDataForImage(idTarget,"https://a.ppy.sh/"+userid);
                // getMessageData(osuUrl("deceitful","2"),idTarget);
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
                msgText = msgText.toLowerCase();



                if (!msgText.contains("bot leave")){
                    if(msgText.contains("/weather"))
                    {


                       String part2 = splitter(msgText+";","/weather (.*?);","/weather");
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
                            if(part2==null)
                            {
                                getMessageData("you forgot the semicolon; eg: /weather cityName;",idTarget);
                            }
                            else {
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
                    if(msgText.contains("/mania"))
                    {
                        jsonResultForOsu(splitter(msgText+";","/mania (.*?);","/mania"),idTarget,"3","osu!Mania");
                    }
                    if(msgText.contains("/ctb"))
                    {
                        jsonResultForOsu(splitter(msgText+";","/ctb (.*?);","/ctb"),idTarget,"2","Catch the Beat");
                    }
                    if(msgText.contains("/taiko"))
                    {
                        jsonResultForOsu(splitter(msgText+";","/taiko (.*?);","/taiko"),idTarget,"1","Taiko");
                    }
                    if(msgText.contains("/std"))
                    {
                        jsonResultForOsu(splitter(msgText+";","/std (.*?);","/std"),idTarget,"0","Osu Standard!");
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
                            JSONObject json = readJsonFromUrl("http://muslimsalat.com/"+part2+"/daily/08-06-2017.json?key=5db94b590c066277ad540f984a288bac");
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

                    if(msgText.contains("/bukalapak")) {
                        String string = msgText.toString()+";";
                        String hasil = "";

                        if(!string.substring(string.length()-1).equals(";"))
                        {
                            try {
                                getMessageData("you forgot the semicolon; eg: /bukalapak productName;",idTarget);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        else {


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
                                    getMessageData("Seller Name: " + seller_name + "\nPositive Rating: " + Integer.toString(positive) + "\nNegative Rating: " + Integer.toString(negative) + "\nName: " + name + "\nPrice: Rp. " + Integer.toString(price) + "\n", idTarget);
                                    getMessageData(url, idTarget);
                                    getMessageDataForImage(idTarget, imagesUrl);
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if(msgText.contains("/video"))
                    {

                        String hasil = splitter(msgText+";","/video (.*?);","/video");
                        String string ="{\n" +
                                "  \"info\": {\n" +
                                "    \"abr\": 192, \n" +
                                "    \"acodec\": \"mp4a.40.2\", \n" +
                                "    \"age_limit\": 0, \n" +
                                "    \"alt_title\": null, \n" +
                                "    \"annotations\": null, \n" +
                                "    \"automatic_captions\": {}, \n" +
                                "    \"average_rating\": 4.94513893127, \n" +
                                "    \"categories\": [\n" +
                                "      \"Education\"\n" +
                                "    ], \n" +
                                "    \"creator\": null, \n" +
                                "    \"description\": \"Naga adalah makhluk yang melegenda di berbagai belahan dunia. Tapi, gimana asal usul sebenarnya? Benarkah naga ada pada zaman dinosaurus? Enjoy the video!\\n\\n---\\n\\nTanyakan pertanyaan aneh anda disini! NGGA ada pertanyaan yang bodoh! 'Kok Bisa' ngga cuma sekedar mencoba menjawab pertanyaan-pertanyaan yang terlihat bodoh, aneh dan dungu disini, tapi juga mencoba menumbuhkan rasa keingintahuan anda terhadap segala hal di dunia ini. Jadi tunggu apalagi? Ayo subscribe, let's watch the videos and go curiosity!\\n\\n---\\n\\nFAQ (Frequently Asked Questions):\\nQ: Min, upload tiap hari apa?\\nA: Tiap hari Rabu. Tapi kadang-kadang ada video yang butuh waktu pembuatan lebih lama. So, stay tuned!\\n\\nQ: Min, animasinya pake software apa?\\nA: Adobe after effects\\n\\nQ: MIN, KENAPA DI VIDEONYA ADA BAKSONYA TERUS!?\\nA: Bakso is inspiration *wink\\n\\n---\\n\\nFollow our social media for more updates, stuff and facts!\\n\\nFacebook: https://www.facebook.com/kokbisachannel\\nInstagram: https://instagram.com/kokbisa\\nLine: http://bit.ly/linekokbisa\\nTwitter: https://twitter.com/kokbisachannel\\n\\n---\\n\\nFor business inquiries: kokbisachannel@gmail.com\\n\\n---\\n\\nReferences:\\nGiants, Monsters, and Dragons: An Encyclopedia of Folklore, Legend, and Myth Paperback \\u2013 November 17, 2001 by Carol Rose\\nDragons: A Natural History Hardcover \\u2013 November, 1995 by Karl P. N. Shuker\\nAn Instinct For Dragon, 2000 by David E. Jones\\nhttps://www.youtube.com/redirect?redir_token=WSXRns1HMPTO8GcATbUpZA5ADml8MTQ5NjkzMzI1MkAxNDk2ODQ2ODUy&q=http%3A%2F%2Fwww.livescience.com%2F9726-origin-komodo-dragon-revealed.html\\nhttps://www.youtube.com/redirect?redir_token=WSXRns1HMPTO8GcATbUpZA5ADml8MTQ5NjkzMzI1MkAxNDk2ODQ2ODUy&q=http%3A%2F%2Fshc.stanford.edu%2Fnews%2Fresearch%2Fdinosaurs-and-dragons-oh-my\\nhttps://www.youtube.com/redirect?redir_token=WSXRns1HMPTO8GcATbUpZA5ADml8MTQ5NjkzMzI1MkAxNDk2ODQ2ODUy&q=http%3A%2F%2Fedition.cnn.com%2F2015%2F01%2F30%2Fasia%2Fchina-dragon-dinosaur%2F\\nhttps://www.youtube.com/redirect?redir_token=WSXRns1HMPTO8GcATbUpZA5ADml8MTQ5NjkzMzI1MkAxNDk2ODQ2ODUy&q=https%3A%2F%2Fwww.thoughtco.com%2Fdinosaurs-and-dragons-the-real-story-1092002\\nhttps://www.youtube.com/redirect?redir_token=WSXRns1HMPTO8GcATbUpZA5ADml8MTQ5NjkzMzI1MkAxNDk2ODQ2ODUy&q=http%3A%2F%2Fwww.smithsonianmag.com%2Fscience-nature%2Fwhere-did-dragons-come-from-23969126%2F\\nhttps://www.youtube.com/redirect?redir_token=WSXRns1HMPTO8GcATbUpZA5ADml8MTQ5NjkzMzI1MkAxNDk2ODQ2ODUy&q=http%3A%2F%2Fwww.smithsonianmag.com%2Fscience-nature%2Fthe-origin-of-the-komodo-dragon-17655352%2F\\nhttps://www.youtube.com/redirect?redir_token=WSXRns1HMPTO8GcATbUpZA5ADml8MTQ5NjkzMzI1MkAxNDk2ODQ2ODUy&q=http%3A%2F%2Fwww.livescience.com%2F27402-komodo-dragons.html\\nhttps://www.youtube.com/redirect?redir_token=WSXRns1HMPTO8GcATbUpZA5ADml8MTQ5NjkzMzI1MkAxNDk2ODQ2ODUy&q=http%3A%2F%2Fwww.livescience.com%2F25559-dragons.html https://www.youtube.com/redirect?redir_token=WSXRns1HMPTO8GcATbUpZA5ADml8MTQ5NjkzMzI1MkAxNDk2ODQ2ODUy&q=http%3A%2F%2Fwww.mampam.com%2Findex.php%3Foption%3Dcom_content%26task%3Dview%26id%3D48%26Itemid%3D97%26limit%3D1%26limitstart%3D0\\n\\n---\\nCredits:\\n-\\n- Kevin MacLeod for awesome music\\n- Sunshinelammi from Reddit, for this episode's end narrator (soundcloud.com/sunshinelammi)\\n- And a massive THANK YOU to everyone for watching this and for all of your support!\", \n" +
                                "    \"dislike_count\": 68, \n" +
                                "    \"display_id\": \"7g6ruRV_pUA\", \n" +
                                "    \"duration\": 185, \n" +
                                "    \"end_time\": null, \n" +
                                "    \"episode_number\": null, \n" +
                                "    \"ext\": \"mp4\", \n" +
                                "    \"extractor\": \"youtube\", \n" +
                                "    \"extractor_key\": \"Youtube\", \n" +
                                "    \"format\": \"22 - 1280x720 (hd720)\", \n" +
                                "    \"format_id\": \"22\", \n" +
                                "    \"format_note\": \"hd720\", \n" +
                                "    \"formats\": [\n" +
                                "      {\n" +
                                "        \"abr\": 48, \n" +
                                "        \"acodec\": \"mp4a.40.5\", \n" +
                                "        \"asr\": 22050, \n" +
                                "        \"container\": \"m4a_dash\", \n" +
                                "        \"ext\": \"m4a\", \n" +
                                "        \"filesize\": 1100503, \n" +
                                "        \"format\": \"139 - audio only (DASH audio)\", \n" +
                                "        \"format_id\": \"139\", \n" +
                                "        \"format_note\": \"DASH audio\", \n" +
                                "        \"fps\": null, \n" +
                                "        \"height\": null, \n" +
                                "        \"http_headers\": {\n" +
                                "          \"Accept\": \"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\", \n" +
                                "          \"Accept-Charset\": \"ISO-8859-1,utf-8;q=0.7,*;q=0.7\", \n" +
                                "          \"Accept-Encoding\": \"gzip, deflate\", \n" +
                                "          \"Accept-Language\": \"en-us,en;q=0.5\", \n" +
                                "          \"User-Agent\": \"Mozilla/5.0 (X11; Linux x86_64; rv:10.0) Gecko/20150101 Firefox/47.0 (Chrome)\"\n" +
                                "        }, \n" +
                                "        \"language\": null, \n" +
                                "        \"manifest_url\": \"https://manifest.googlevideo.com/api/manifest/dash/ip/54.90.126.14/requiressl/yes/playback_host/r1---sn-p5qs7n7e.googlevideo.com/sparams/as%2Cei%2Chfr%2Cid%2Cip%2Cipbits%2Citag%2Cmm%2Cmn%2Cms%2Cmv%2Cpl%2Cplayback_host%2Crequiressl%2Csource%2Cexpire/mv/u/mt/1496846665/signature/9A59EB354CE2F8E4002A803E9A74293D87700392.928FE2E316878C8247A3CC151C422FCFA6C4A890/ms/au/key/yt6/mm/31/ipbits/0/hfr/1/id/ee0eabb9157fa540/pl/21/source/youtube/mn/sn-p5qs7n7e/as/fmp4_audio_clear%2Cfmp4_sd_hd_clear/expire/1496868452/ei/BBI4WeuUDsXA8wTW1q3wAw/itag/0\", \n" +
                                "        \"preference\": -50, \n" +
                                "        \"protocol\": \"https\", \n" +
                                "        \"tbr\": 48, \n" +
                                "        \"url\": \"https://r1---sn-p5qs7n7e.googlevideo.com/videoplayback?id=ee0eabb9157fa540&itag=139&source=youtube&requiressl=yes&mv=u&ms=au&mm=31&pl=21&mn=sn-p5qs7n7e&ei=BBI4WeuUDsXA8wTW1q3wAw&ratebypass=yes&mime=audio/mp4&gir=yes&clen=1100503&lmt=1496819255683009&dur=184.877&mt=1496846665&signature=8329E921ADA788F98D95F77A7D7158BFD359F4D4.32F9BCD9F4754FEEBC1E3FE553191A036F04122D&key=dg_yt0&ip=54.90.126.14&ipbits=0&expire=1496868452&sparams=ip,ipbits,expire,id,itag,source,requiressl,mv,ms,mm,pl,mn,ei,ratebypass,mime,gir,clen,lmt,dur\", \n" +
                                "        \"vcodec\": \"none\", \n" +
                                "        \"width\": null\n" +
                                "      }, \n" +
                                "      {\n" +
                                "        \"abr\": 50, \n" +
                                "        \"acodec\": \"opus\", \n" +
                                "        \"ext\": \"webm\", \n" +
                                "        \"filesize\": 1388807, \n" +
                                "        \"format\": \"249 - audio only (DASH audio)\", \n" +
                                "        \"format_id\": \"249\", \n" +
                                "        \"format_note\": \"DASH audio\", \n" +
                                "        \"http_headers\": {\n" +
                                "          \"Accept\": \"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\", \n" +
                                "          \"Accept-Charset\": \"ISO-8859-1,utf-8;q=0.7,*;q=0.7\", \n" +
                                "          \"Accept-Encoding\": \"gzip, deflate\", \n" +
                                "          \"Accept-Language\": \"en-us,en;q=0.5\", \n" +
                                "          \"User-Agent\": \"Mozilla/5.0 (X11; Linux x86_64; rv:10.0) Gecko/20150101 Firefox/47.0 (Chrome)\"\n" +
                                "        }, \n" +
                                "        \"player_url\": null, \n" +
                                "        \"preference\": -50, \n" +
                                "        \"protocol\": \"https\", \n" +
                                "        \"tbr\": 63.511, \n" +
                                "        \"url\": \"https://r1---sn-p5qs7n7e.googlevideo.com/videoplayback?mime=audio%2Fwebm&key=yt6&expire=1496868451&lmt=1496820732979666&ipbits=0&itag=249&pl=21&dur=184.741&source=youtube&pcm2=yes&requiressl=yes&gir=yes&id=o-AK6oXClKukrGjcel-IjVf80irXZt6hmOUfPtmHSwfnbL&keepalive=yes&clen=1388807&ip=54.90.126.14&ei=AxI4WbGHL8S88wSboJaICw&sparams=clen%2Cdur%2Cei%2Cgir%2Cid%2Cip%2Cipbits%2Citag%2Ckeepalive%2Clmt%2Cmime%2Cmm%2Cmn%2Cms%2Cmv%2Cpcm2%2Cpl%2Crequiressl%2Csource%2Cexpire&ms=au&signature=AF9973C8AEE54173C6BFDBEFAD11E60EB3C5A3BC.6F71B8D8926C01D410B1C5A61BA3918A06616BAD&mt=1496846665&mv=u&mm=31&mn=sn-p5qs7n7e&ratebypass=yes\", \n" +
                                "        \"vcodec\": \"none\"\n" +
                                "      }, \n" +
                                "      {\n" +
                                "        \"abr\": 70, \n" +
                                "        \"acodec\": \"opus\", \n" +
                                "        \"ext\": \"webm\", \n" +
                                "        \"filesize\": 1829978, \n" +
                                "        \"format\": \"250 - audio only (DASH audio)\", \n" +
                                "        \"format_id\": \"250\", \n" +
                                "        \"format_note\": \"DASH audio\", \n" +
                                "        \"http_headers\": {\n" +
                                "          \"Accept\": \"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\", \n" +
                                "          \"Accept-Charset\": \"ISO-8859-1,utf-8;q=0.7,*;q=0.7\", \n" +
                                "          \"Accept-Encoding\": \"gzip, deflate\", \n" +
                                "          \"Accept-Language\": \"en-us,en;q=0.5\", \n" +
                                "          \"User-Agent\": \"Mozilla/5.0 (X11; Linux x86_64; rv:10.0) Gecko/20150101 Firefox/47.0 (Chrome)\"\n" +
                                "        }, \n" +
                                "        \"player_url\": null, \n" +
                                "        \"preference\": -50, \n" +
                                "        \"protocol\": \"https\", \n" +
                                "        \"tbr\": 83.4, \n" +
                                "        \"url\": \"https://r1---sn-p5qs7n7e.googlevideo.com/videoplayback?mime=audio%2Fwebm&key=yt6&expire=1496868451&lmt=1496820711873827&ipbits=0&itag=250&pl=21&dur=184.741&source=youtube&pcm2=yes&requiressl=yes&gir=yes&id=o-AK6oXClKukrGjcel-IjVf80irXZt6hmOUfPtmHSwfnbL&keepalive=yes&clen=1829978&ip=54.90.126.14&ei=AxI4WbGHL8S88wSboJaICw&sparams=clen%2Cdur%2Cei%2Cgir%2Cid%2Cip%2Cipbits%2Citag%2Ckeepalive%2Clmt%2Cmime%2Cmm%2Cmn%2Cms%2Cmv%2Cpcm2%2Cpl%2Crequiressl%2Csource%2Cexpire&ms=au&signature=509494835C8178C3C92981C0EBEE1EC5F0D5593B.6BD41E30D573431697850A08FF40DA911E4F94C1&mt=1496846665&mv=u&mm=31&mn=sn-p5qs7n7e&ratebypass=yes\", \n" +
                                "        \"vcodec\": \"none\"\n" +
                                "      }, \n" +
                                "      {\n" +
                                "        \"abr\": 128, \n" +
                                "        \"acodec\": \"vorbis\", \n" +
                                "        \"ext\": \"webm\", \n" +
                                "        \"filesize\": 2712768, \n" +
                                "        \"format\": \"171 - audio only (DASH audio)\", \n" +
                                "        \"format_id\": \"171\", \n" +
                                "        \"format_note\": \"DASH audio\", \n" +
                                "        \"http_headers\": {\n" +
                                "          \"Accept\": \"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\", \n" +
                                "          \"Accept-Charset\": \"ISO-8859-1,utf-8;q=0.7,*;q=0.7\", \n" +
                                "          \"Accept-Encoding\": \"gzip, deflate\", \n" +
                                "          \"Accept-Language\": \"en-us,en;q=0.5\", \n" +
                                "          \"User-Agent\": \"Mozilla/5.0 (X11; Linux x86_64; rv:10.0) Gecko/20150101 Firefox/47.0 (Chrome)\"\n" +
                                "        }, \n" +
                                "        \"player_url\": null, \n" +
                                "        \"preference\": -50, \n" +
                                "        \"protocol\": \"https\", \n" +
                                "        \"tbr\": 126.808, \n" +
                                "        \"url\": \"https://r1---sn-p5qs7n7e.googlevideo.com/videoplayback?mime=audio%2Fwebm&key=yt6&expire=1496868451&lmt=1496820729343664&ipbits=0&itag=171&pl=21&dur=184.725&source=youtube&pcm2=yes&requiressl=yes&gir=yes&id=o-AK6oXClKukrGjcel-IjVf80irXZt6hmOUfPtmHSwfnbL&keepalive=yes&clen=2712768&ip=54.90.126.14&ei=AxI4WbGHL8S88wSboJaICw&sparams=clen%2Cdur%2Cei%2Cgir%2Cid%2Cip%2Cipbits%2Citag%2Ckeepalive%2Clmt%2Cmime%2Cmm%2Cmn%2Cms%2Cmv%2Cpcm2%2Cpl%2Crequiressl%2Csource%2Cexpire&ms=au&signature=326EDF759341D41B91DC92BDB6544109EF4D5CAC.DC5F2B81DDB49759C0B24FB42A5B1B1A45E30536&mt=1496846665&mv=u&mm=31&mn=sn-p5qs7n7e&ratebypass=yes\", \n" +
                                "        \"vcodec\": \"none\"\n" +
                                "      }, \n" +
                                "      {\n" +
                                "        \"abr\": 128, \n" +
                                "        \"acodec\": \"mp4a.40.2\", \n" +
                                "        \"asr\": 44100, \n" +
                                "        \"container\": \"m4a_dash\", \n" +
                                "        \"ext\": \"m4a\", \n" +
                                "        \"filesize\": 2936036, \n" +
                                "        \"format\": \"140 - audio only (DASH audio)\", \n" +
                                "        \"format_id\": \"140\", \n" +
                                "        \"format_note\": \"DASH audio\", \n" +
                                "        \"fps\": null, \n" +
                                "        \"height\": null, \n" +
                                "        \"http_headers\": {\n" +
                                "          \"Accept\": \"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\", \n" +
                                "          \"Accept-Charset\": \"ISO-8859-1,utf-8;q=0.7,*;q=0.7\", \n" +
                                "          \"Accept-Encoding\": \"gzip, deflate\", \n" +
                                "          \"Accept-Language\": \"en-us,en;q=0.5\", \n" +
                                "          \"User-Agent\": \"Mozilla/5.0 (X11; Linux x86_64; rv:10.0) Gecko/20150101 Firefox/47.0 (Chrome)\"\n" +
                                "        }, \n" +
                                "        \"language\": null, \n" +
                                "        \"manifest_url\": \"https://manifest.googlevideo.com/api/manifest/dash/ip/54.90.126.14/requiressl/yes/playback_host/r1---sn-p5qs7n7e.googlevideo.com/sparams/as%2Cei%2Chfr%2Cid%2Cip%2Cipbits%2Citag%2Cmm%2Cmn%2Cms%2Cmv%2Cpl%2Cplayback_host%2Crequiressl%2Csource%2Cexpire/mv/u/mt/1496846665/signature/9A59EB354CE2F8E4002A803E9A74293D87700392.928FE2E316878C8247A3CC151C422FCFA6C4A890/ms/au/key/yt6/mm/31/ipbits/0/hfr/1/id/ee0eabb9157fa540/pl/21/source/youtube/mn/sn-p5qs7n7e/as/fmp4_audio_clear%2Cfmp4_sd_hd_clear/expire/1496868452/ei/BBI4WeuUDsXA8wTW1q3wAw/itag/0\", \n" +
                                "        \"preference\": -50, \n" +
                                "        \"protocol\": \"https\", \n" +
                                "        \"tbr\": 128, \n" +
                                "        \"url\": \"https://r1---sn-p5qs7n7e.googlevideo.com/videoplayback?id=ee0eabb9157fa540&itag=140&source=youtube&requiressl=yes&mv=u&ms=au&mm=31&pl=21&mn=sn-p5qs7n7e&ei=BBI4WeuUDsXA8wTW1q3wAw&ratebypass=yes&mime=audio/mp4&gir=yes&clen=2936036&lmt=1496819255079373&dur=184.784&mt=1496846665&signature=7AE3DDD5D8E49663A6A3F8F31689C7D3829FA391.4531B7CDE588EB2F5D37FDC1DFBB4F6CC0BA283A&key=dg_yt0&ip=54.90.126.14&ipbits=0&expire=1496868452&sparams=ip,ipbits,expire,id,itag,source,requiressl,mv,ms,mm,pl,mn,ei,ratebypass,mime,gir,clen,lmt,dur\", \n" +
                                "        \"vcodec\": \"none\", \n" +
                                "        \"width\": null\n" +
                                "      }, \n" +
                                "      {\n" +
                                "        \"abr\": 160, \n" +
                                "        \"acodec\": \"opus\", \n" +
                                "        \"ext\": \"webm\", \n" +
                                "        \"filesize\": 3580631, \n" +
                                "        \"format\": \"251 - audio only (DASH audio)\", \n" +
                                "        \"format_id\": \"251\", \n" +
                                "        \"format_note\": \"DASH audio\", \n" +
                                "        \"http_headers\": {\n" +
                                "          \"Accept\": \"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\", \n" +
                                "          \"Accept-Charset\": \"ISO-8859-1,utf-8;q=0.7,*;q=0.7\", \n" +
                                "          \"Accept-Encoding\": \"gzip, deflate\", \n" +
                                "          \"Accept-Language\": \"en-us,en;q=0.5\", \n" +
                                "          \"User-Agent\": \"Mozilla/5.0 (X11; Linux x86_64; rv:10.0) Gecko/20150101 Firefox/47.0 (Chrome)\"\n" +
                                "        }, \n" +
                                "        \"player_url\": null, \n" +
                                "        \"preference\": -50, \n" +
                                "        \"protocol\": \"https\", \n" +
                                "        \"tbr\": 162.554, \n" +
                                "        \"url\": \"https://r1---sn-p5qs7n7e.googlevideo.com/videoplayback?mime=audio%2Fwebm&key=yt6&expire=1496868451&lmt=1496820711531425&ipbits=0&itag=251&pl=21&dur=184.741&source=youtube&pcm2=yes&requiressl=yes&gir=yes&id=o-AK6oXClKukrGjcel-IjVf80irXZt6hmOUfPtmHSwfnbL&keepalive=yes&clen=3580631&ip=54.90.126.14&ei=AxI4WbGHL8S88wSboJaICw&sparams=clen%2Cdur%2Cei%2Cgir%2Cid%2Cip%2Cipbits%2Citag%2Ckeepalive%2Clmt%2Cmime%2Cmm%2Cmn%2Cms%2Cmv%2Cpcm2%2Cpl%2Crequiressl%2Csource%2Cexpire&ms=au&signature=B10FE12C2980E43434432D4D8E5FAC26E27F8054.7CF690D6F0D7C8EA17BC341955820A0643D1F03B&mt=1496846665&mv=u&mm=31&mn=sn-p5qs7n7e&ratebypass=yes\", \n" +
                                "        \"vcodec\": \"none\"\n" +
                                "      }, \n" +
                                "      {\n" +
                                "        \"acodec\": \"none\", \n" +
                                "        \"asr\": null, \n" +
                                "        \"ext\": \"mp4\", \n" +
                                "        \"filesize\": 1185474, \n" +
                                "        \"format\": \"160 - 256x144 (DASH video)\", \n" +
                                "        \"format_id\": \"160\", \n" +
                                "        \"format_note\": \"DASH video\", \n" +
                                "        \"fps\": 30, \n" +
                                "        \"height\": 144, \n" +
                                "        \"http_headers\": {\n" +
                                "          \"Accept\": \"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\", \n" +
                                "          \"Accept-Charset\": \"ISO-8859-1,utf-8;q=0.7,*;q=0.7\", \n" +
                                "          \"Accept-Encoding\": \"gzip, deflate\", \n" +
                                "          \"Accept-Language\": \"en-us,en;q=0.5\", \n" +
                                "          \"User-Agent\": \"Mozilla/5.0 (X11; Linux x86_64; rv:10.0) Gecko/20150101 Firefox/47.0 (Chrome)\"\n" +
                                "        }, \n" +
                                "        \"language\": null, \n" +
                                "        \"manifest_url\": \"https://manifest.googlevideo.com/api/manifest/dash/ip/54.90.126.14/requiressl/yes/playback_host/r1---sn-p5qs7n7e.googlevideo.com/sparams/as%2Cei%2Chfr%2Cid%2Cip%2Cipbits%2Citag%2Cmm%2Cmn%2Cms%2Cmv%2Cpl%2Cplayback_host%2Crequiressl%2Csource%2Cexpire/mv/u/mt/1496846665/signature/9A59EB354CE2F8E4002A803E9A74293D87700392.928FE2E316878C8247A3CC151C422FCFA6C4A890/ms/au/key/yt6/mm/31/ipbits/0/hfr/1/id/ee0eabb9157fa540/pl/21/source/youtube/mn/sn-p5qs7n7e/as/fmp4_audio_clear%2Cfmp4_sd_hd_clear/expire/1496868452/ei/BBI4WeuUDsXA8wTW1q3wAw/itag/0\", \n" +
                                "        \"preference\": -40, \n" +
                                "        \"protocol\": \"https\", \n" +
                                "        \"tbr\": 111, \n" +
                                "        \"url\": \"https://r1---sn-p5qs7n7e.googlevideo.com/videoplayback?id=ee0eabb9157fa540&itag=160&source=youtube&requiressl=yes&mv=u&ms=au&mm=31&pl=21&mn=sn-p5qs7n7e&ei=BBI4WeuUDsXA8wTW1q3wAw&ratebypass=yes&mime=video/mp4&gir=yes&clen=1185474&lmt=1496819262704005&dur=184.699&mt=1496846665&signature=07AF3EB998A751F5E003DDA58193AE51117A3A43.8523F142413DA99329AFB2A52DB7C17D7F19C24A&key=dg_yt0&ip=54.90.126.14&ipbits=0&expire=1496868452&sparams=ip,ipbits,expire,id,itag,source,requiressl,mv,ms,mm,pl,mn,ei,ratebypass,mime,gir,clen,lmt,dur\", \n" +
                                "        \"vcodec\": \"avc1.4d400c\", \n" +
                                "        \"width\": 256\n" +
                                "      }, \n" +
                                "      {\n" +
                                "        \"acodec\": \"none\", \n" +
                                "        \"container\": \"webm\", \n" +
                                "        \"ext\": \"webm\", \n" +
                                "        \"filesize\": 1615119, \n" +
                                "        \"format\": \"278 - 256x144 (144p)\", \n" +
                                "        \"format_id\": \"278\", \n" +
                                "        \"format_note\": \"144p\", \n" +
                                "        \"fps\": 30, \n" +
                                "        \"height\": 144, \n" +
                                "        \"http_headers\": {\n" +
                                "          \"Accept\": \"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\", \n" +
                                "          \"Accept-Charset\": \"ISO-8859-1,utf-8;q=0.7,*;q=0.7\", \n" +
                                "          \"Accept-Encoding\": \"gzip, deflate\", \n" +
                                "          \"Accept-Language\": \"en-us,en;q=0.5\", \n" +
                                "          \"User-Agent\": \"Mozilla/5.0 (X11; Linux x86_64; rv:10.0) Gecko/20150101 Firefox/47.0 (Chrome)\"\n" +
                                "        }, \n" +
                                "        \"player_url\": null, \n" +
                                "        \"preference\": -40, \n" +
                                "        \"protocol\": \"https\", \n" +
                                "        \"tbr\": 128.102, \n" +
                                "        \"url\": \"https://r1---sn-p5qs7n7e.googlevideo.com/videoplayback?mime=video%2Fwebm&key=yt6&expire=1496868451&lmt=1496821414077593&ipbits=0&itag=278&pl=21&dur=184.666&source=youtube&pcm2=yes&requiressl=yes&gir=yes&id=o-AK6oXClKukrGjcel-IjVf80irXZt6hmOUfPtmHSwfnbL&keepalive=yes&clen=1615119&ip=54.90.126.14&ei=AxI4WbGHL8S88wSboJaICw&sparams=clen%2Cdur%2Cei%2Cgir%2Cid%2Cip%2Cipbits%2Citag%2Ckeepalive%2Clmt%2Cmime%2Cmm%2Cmn%2Cms%2Cmv%2Cpcm2%2Cpl%2Crequiressl%2Csource%2Cexpire&ms=au&signature=C0EF78E8DC954DFDE8D6CBB4C72AE123DF14509F.A5708BBEA9D94CC7AC76FBA3D44402BA70B77282&mt=1496846665&mv=u&mm=31&mn=sn-p5qs7n7e&ratebypass=yes\", \n" +
                                "        \"vcodec\": \"vp9\", \n" +
                                "        \"width\": 256\n" +
                                "      }, \n" +
                                "      {\n" +
                                "        \"acodec\": \"none\", \n" +
                                "        \"ext\": \"webm\", \n" +
                                "        \"filesize\": 1967096, \n" +
                                "        \"format\": \"242 - 426x240 (240p)\", \n" +
                                "        \"format_id\": \"242\", \n" +
                                "        \"format_note\": \"240p\", \n" +
                                "        \"fps\": 30, \n" +
                                "        \"height\": 240, \n" +
                                "        \"http_headers\": {\n" +
                                "          \"Accept\": \"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\", \n" +
                                "          \"Accept-Charset\": \"ISO-8859-1,utf-8;q=0.7,*;q=0.7\", \n" +
                                "          \"Accept-Encoding\": \"gzip, deflate\", \n" +
                                "          \"Accept-Language\": \"en-us,en;q=0.5\", \n" +
                                "          \"User-Agent\": \"Mozilla/5.0 (X11; Linux x86_64; rv:10.0) Gecko/20150101 Firefox/47.0 (Chrome)\"\n" +
                                "        }, \n" +
                                "        \"player_url\": null, \n" +
                                "        \"preference\": -40, \n" +
                                "        \"protocol\": \"https\", \n" +
                                "        \"tbr\": 229.791, \n" +
                                "        \"url\": \"https://r1---sn-p5qs7n7e.googlevideo.com/videoplayback?mime=video%2Fwebm&key=yt6&expire=1496868451&lmt=1496821414386963&ipbits=0&itag=242&pl=21&dur=184.666&source=youtube&pcm2=yes&requiressl=yes&gir=yes&id=o-AK6oXClKukrGjcel-IjVf80irXZt6hmOUfPtmHSwfnbL&keepalive=yes&clen=1967096&ip=54.90.126.14&ei=AxI4WbGHL8S88wSboJaICw&sparams=clen%2Cdur%2Cei%2Cgir%2Cid%2Cip%2Cipbits%2Citag%2Ckeepalive%2Clmt%2Cmime%2Cmm%2Cmn%2Cms%2Cmv%2Cpcm2%2Cpl%2Crequiressl%2Csource%2Cexpire&ms=au&signature=8D2CB4A0378E91624EFE4F701F2814DBBCA67382.5A6A0AD86E1F53CCA62083A116CB16A89142C770&mt=1496846665&mv=u&mm=31&mn=sn-p5qs7n7e&ratebypass=yes\", \n" +
                                "        \"vcodec\": \"vp9\", \n" +
                                "        \"width\": 426\n" +
                                "      }, \n" +
                                "      {\n" +
                                "        \"acodec\": \"none\", \n" +
                                "        \"asr\": null, \n" +
                                "        \"ext\": \"mp4\", \n" +
                                "        \"filesize\": 2411972, \n" +
                                "        \"format\": \"133 - 426x240 (DASH video)\", \n" +
                                "        \"format_id\": \"133\", \n" +
                                "        \"format_note\": \"DASH video\", \n" +
                                "        \"fps\": 30, \n" +
                                "        \"height\": 240, \n" +
                                "        \"http_headers\": {\n" +
                                "          \"Accept\": \"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\", \n" +
                                "          \"Accept-Charset\": \"ISO-8859-1,utf-8;q=0.7,*;q=0.7\", \n" +
                                "          \"Accept-Encoding\": \"gzip, deflate\", \n" +
                                "          \"Accept-Language\": \"en-us,en;q=0.5\", \n" +
                                "          \"User-Agent\": \"Mozilla/5.0 (X11; Linux x86_64; rv:10.0) Gecko/20150101 Firefox/47.0 (Chrome)\"\n" +
                                "        }, \n" +
                                "        \"language\": null, \n" +
                                "        \"manifest_url\": \"https://manifest.googlevideo.com/api/manifest/dash/ip/54.90.126.14/requiressl/yes/playback_host/r1---sn-p5qs7n7e.googlevideo.com/sparams/as%2Cei%2Chfr%2Cid%2Cip%2Cipbits%2Citag%2Cmm%2Cmn%2Cms%2Cmv%2Cpl%2Cplayback_host%2Crequiressl%2Csource%2Cexpire/mv/u/mt/1496846665/signature/9A59EB354CE2F8E4002A803E9A74293D87700392.928FE2E316878C8247A3CC151C422FCFA6C4A890/ms/au/key/yt6/mm/31/ipbits/0/hfr/1/id/ee0eabb9157fa540/pl/21/source/youtube/mn/sn-p5qs7n7e/as/fmp4_audio_clear%2Cfmp4_sd_hd_clear/expire/1496868452/ei/BBI4WeuUDsXA8wTW1q3wAw/itag/0\", \n" +
                                "        \"preference\": -40, \n" +
                                "        \"protocol\": \"https\", \n" +
                                "        \"tbr\": 245, \n" +
                                "        \"url\": \"https://r1---sn-p5qs7n7e.googlevideo.com/videoplayback?id=ee0eabb9157fa540&itag=133&source=youtube&requiressl=yes&mv=u&ms=au&mm=31&pl=21&mn=sn-p5qs7n7e&ei=BBI4WeuUDsXA8wTW1q3wAw&ratebypass=yes&mime=video/mp4&gir=yes&clen=2411972&lmt=1496819262923241&dur=184.699&mt=1496846665&signature=8AB11628028F6411878C910F890D763CB40FDEC7.46E5619F68387D37A7DA0AB47715CD50539602F0&key=dg_yt0&ip=54.90.126.14&ipbits=0&expire=1496868452&sparams=ip,ipbits,expire,id,itag,source,requiressl,mv,ms,mm,pl,mn,ei,ratebypass,mime,gir,clen,lmt,dur\", \n" +
                                "        \"vcodec\": \"avc1.4d4015\", \n" +
                                "        \"width\": 426\n" +
                                "      }, \n" +
                                "      {\n" +
                                "        \"acodec\": \"none\", \n" +
                                "        \"ext\": \"webm\", \n" +
                                "        \"filesize\": 3531238, \n" +
                                "        \"format\": \"243 - 640x360 (360p)\", \n" +
                                "        \"format_id\": \"243\", \n" +
                                "        \"format_note\": \"360p\", \n" +
                                "        \"fps\": 30, \n" +
                                "        \"height\": 360, \n" +
                                "        \"http_headers\": {\n" +
                                "          \"Accept\": \"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\", \n" +
                                "          \"Accept-Charset\": \"ISO-8859-1,utf-8;q=0.7,*;q=0.7\", \n" +
                                "          \"Accept-Encoding\": \"gzip, deflate\", \n" +
                                "          \"Accept-Language\": \"en-us,en;q=0.5\", \n" +
                                "          \"User-Agent\": \"Mozilla/5.0 (X11; Linux x86_64; rv:10.0) Gecko/20150101 Firefox/47.0 (Chrome)\"\n" +
                                "        }, \n" +
                                "        \"player_url\": null, \n" +
                                "        \"preference\": -40, \n" +
                                "        \"protocol\": \"https\", \n" +
                                "        \"tbr\": 437.394, \n" +
                                "        \"url\": \"https://r1---sn-p5qs7n7e.googlevideo.com/videoplayback?mime=video%2Fwebm&key=yt6&expire=1496868451&lmt=1496821414296668&ipbits=0&itag=243&pl=21&dur=184.666&source=youtube&pcm2=yes&requiressl=yes&gir=yes&id=o-AK6oXClKukrGjcel-IjVf80irXZt6hmOUfPtmHSwfnbL&keepalive=yes&clen=3531238&ip=54.90.126.14&ei=AxI4WbGHL8S88wSboJaICw&sparams=clen%2Cdur%2Cei%2Cgir%2Cid%2Cip%2Cipbits%2Citag%2Ckeepalive%2Clmt%2Cmime%2Cmm%2Cmn%2Cms%2Cmv%2Cpcm2%2Cpl%2Crequiressl%2Csource%2Cexpire&ms=au&signature=6CFD2E946AF015BA7B4F87D05B5939140F7D9518.7FBB71FB94ED261B4CE3D43173974F079F2E56AB&mt=1496846665&mv=u&mm=31&mn=sn-p5qs7n7e&ratebypass=yes\", \n" +
                                "        \"vcodec\": \"vp9\", \n" +
                                "        \"width\": 640\n" +
                                "      }, \n" +
                                "      {\n" +
                                "        \"acodec\": \"none\", \n" +
                                "        \"asr\": null, \n" +
                                "        \"ext\": \"mp4\", \n" +
                                "        \"filesize\": 4067231, \n" +
                                "        \"format\": \"134 - 640x360 (DASH video)\", \n" +
                                "        \"format_id\": \"134\", \n" +
                                "        \"format_note\": \"DASH video\", \n" +
                                "        \"fps\": 30, \n" +
                                "        \"height\": 360, \n" +
                                "        \"http_headers\": {\n" +
                                "          \"Accept\": \"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\", \n" +
                                "          \"Accept-Charset\": \"ISO-8859-1,utf-8;q=0.7,*;q=0.7\", \n" +
                                "          \"Accept-Encoding\": \"gzip, deflate\", \n" +
                                "          \"Accept-Language\": \"en-us,en;q=0.5\", \n" +
                                "          \"User-Agent\": \"Mozilla/5.0 (X11; Linux x86_64; rv:10.0) Gecko/20150101 Firefox/47.0 (Chrome)\"\n" +
                                "        }, \n" +
                                "        \"language\": null, \n" +
                                "        \"manifest_url\": \"https://manifest.googlevideo.com/api/manifest/dash/ip/54.90.126.14/requiressl/yes/playback_host/r1---sn-p5qs7n7e.googlevideo.com/sparams/as%2Cei%2Chfr%2Cid%2Cip%2Cipbits%2Citag%2Cmm%2Cmn%2Cms%2Cmv%2Cpl%2Cplayback_host%2Crequiressl%2Csource%2Cexpire/mv/u/mt/1496846665/signature/9A59EB354CE2F8E4002A803E9A74293D87700392.928FE2E316878C8247A3CC151C422FCFA6C4A890/ms/au/key/yt6/mm/31/ipbits/0/hfr/1/id/ee0eabb9157fa540/pl/21/source/youtube/mn/sn-p5qs7n7e/as/fmp4_audio_clear%2Cfmp4_sd_hd_clear/expire/1496868452/ei/BBI4WeuUDsXA8wTW1q3wAw/itag/0\", \n" +
                                "        \"preference\": -40, \n" +
                                "        \"protocol\": \"https\", \n" +
                                "        \"tbr\": 635, \n" +
                                "        \"url\": \"https://r1---sn-p5qs7n7e.googlevideo.com/videoplayback?id=ee0eabb9157fa540&itag=134&source=youtube&requiressl=yes&mv=u&ms=au&mm=31&pl=21&mn=sn-p5qs7n7e&ei=BBI4WeuUDsXA8wTW1q3wAw&ratebypass=yes&mime=video/mp4&gir=yes&clen=4067231&lmt=1496819263307816&dur=184.699&mt=1496846665&signature=38778A6D26B4F0A358D820F5367396D00A798CA6.55B6633E813CDB31AC6B9229B0085A99FA17FB39&key=dg_yt0&ip=54.90.126.14&ipbits=0&expire=1496868452&sparams=ip,ipbits,expire,id,itag,source,requiressl,mv,ms,mm,pl,mn,ei,ratebypass,mime,gir,clen,lmt,dur\", \n" +
                                "        \"vcodec\": \"avc1.4d401e\", \n" +
                                "        \"width\": 640\n" +
                                "      }, \n" +
                                "      {\n" +
                                "        \"acodec\": \"none\", \n" +
                                "        \"ext\": \"webm\", \n" +
                                "        \"filesize\": 5584218, \n" +
                                "        \"format\": \"244 - 854x480 (480p)\", \n" +
                                "        \"format_id\": \"244\", \n" +
                                "        \"format_note\": \"480p\", \n" +
                                "        \"fps\": 30, \n" +
                                "        \"height\": 480, \n" +
                                "        \"http_headers\": {\n" +
                                "          \"Accept\": \"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\", \n" +
                                "          \"Accept-Charset\": \"ISO-8859-1,utf-8;q=0.7,*;q=0.7\", \n" +
                                "          \"Accept-Encoding\": \"gzip, deflate\", \n" +
                                "          \"Accept-Language\": \"en-us,en;q=0.5\", \n" +
                                "          \"User-Agent\": \"Mozilla/5.0 (X11; Linux x86_64; rv:10.0) Gecko/20150101 Firefox/47.0 (Chrome)\"\n" +
                                "        }, \n" +
                                "        \"player_url\": null, \n" +
                                "        \"preference\": -40, \n" +
                                "        \"protocol\": \"https\", \n" +
                                "        \"tbr\": 745.813, \n" +
                                "        \"url\": \"https://r1---sn-p5qs7n7e.googlevideo.com/videoplayback?mime=video%2Fwebm&key=yt6&expire=1496868451&lmt=1496821415925592&ipbits=0&itag=244&pl=21&dur=184.666&source=youtube&pcm2=yes&requiressl=yes&gir=yes&id=o-AK6oXClKukrGjcel-IjVf80irXZt6hmOUfPtmHSwfnbL&keepalive=yes&clen=5584218&ip=54.90.126.14&ei=AxI4WbGHL8S88wSboJaICw&sparams=clen%2Cdur%2Cei%2Cgir%2Cid%2Cip%2Cipbits%2Citag%2Ckeepalive%2Clmt%2Cmime%2Cmm%2Cmn%2Cms%2Cmv%2Cpcm2%2Cpl%2Crequiressl%2Csource%2Cexpire&ms=au&signature=B3F5C735BEB7ACCA2E614AFB7F33D7C61DF78EAD.5902B6B7737EF6D6800AA8AEDF2769B64D78F000&mt=1496846665&mv=u&mm=31&mn=sn-p5qs7n7e&ratebypass=yes\", \n" +
                                "        \"vcodec\": \"vp9\", \n" +
                                "        \"width\": 854\n" +
                                "      }, \n" +
                                "      {\n" +
                                "        \"acodec\": \"none\", \n" +
                                "        \"asr\": null, \n" +
                                "        \"ext\": \"mp4\", \n" +
                                "        \"filesize\": 7404809, \n" +
                                "        \"format\": \"135 - 854x480 (DASH video)\", \n" +
                                "        \"format_id\": \"135\", \n" +
                                "        \"format_note\": \"DASH video\", \n" +
                                "        \"fps\": 30, \n" +
                                "        \"height\": 480, \n" +
                                "        \"http_headers\": {\n" +
                                "          \"Accept\": \"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\", \n" +
                                "          \"Accept-Charset\": \"ISO-8859-1,utf-8;q=0.7,*;q=0.7\", \n" +
                                "          \"Accept-Encoding\": \"gzip, deflate\", \n" +
                                "          \"Accept-Language\": \"en-us,en;q=0.5\", \n" +
                                "          \"User-Agent\": \"Mozilla/5.0 (X11; Linux x86_64; rv:10.0) Gecko/20150101 Firefox/47.0 (Chrome)\"\n" +
                                "        }, \n" +
                                "        \"language\": null, \n" +
                                "        \"manifest_url\": \"https://manifest.googlevideo.com/api/manifest/dash/ip/54.90.126.14/requiressl/yes/playback_host/r1---sn-p5qs7n7e.googlevideo.com/sparams/as%2Cei%2Chfr%2Cid%2Cip%2Cipbits%2Citag%2Cmm%2Cmn%2Cms%2Cmv%2Cpl%2Cplayback_host%2Crequiressl%2Csource%2Cexpire/mv/u/mt/1496846665/signature/9A59EB354CE2F8E4002A803E9A74293D87700392.928FE2E316878C8247A3CC151C422FCFA6C4A890/ms/au/key/yt6/mm/31/ipbits/0/hfr/1/id/ee0eabb9157fa540/pl/21/source/youtube/mn/sn-p5qs7n7e/as/fmp4_audio_clear%2Cfmp4_sd_hd_clear/expire/1496868452/ei/BBI4WeuUDsXA8wTW1q3wAw/itag/0\", \n" +
                                "        \"preference\": -40, \n" +
                                "        \"protocol\": \"https\", \n" +
                                "        \"tbr\": 1163, \n" +
                                "        \"url\": \"https://r1---sn-p5qs7n7e.googlevideo.com/videoplayback?id=ee0eabb9157fa540&itag=135&source=youtube&requiressl=yes&mv=u&ms=au&mm=31&pl=21&mn=sn-p5qs7n7e&ei=BBI4WeuUDsXA8wTW1q3wAw&ratebypass=yes&mime=video/mp4&gir=yes&clen=7404809&lmt=1496819263812955&dur=184.699&mt=1496846665&signature=6F744CDC1124627010D02BABD8D2095B09B02465.0BE3D98C01DDD6B3E2EEEF3D880033855BE0F6D3&key=dg_yt0&ip=54.90.126.14&ipbits=0&expire=1496868452&sparams=ip,ipbits,expire,id,itag,source,requiressl,mv,ms,mm,pl,mn,ei,ratebypass,mime,gir,clen,lmt,dur\", \n" +
                                "        \"vcodec\": \"avc1.4d401f\", \n" +
                                "        \"width\": 854\n" +
                                "      }, \n" +
                                "      {\n" +
                                "        \"acodec\": \"none\", \n" +
                                "        \"ext\": \"webm\", \n" +
                                "        \"filesize\": 10542115, \n" +
                                "        \"format\": \"247 - 1280x720 (720p)\", \n" +
                                "        \"format_id\": \"247\", \n" +
                                "        \"format_note\": \"720p\", \n" +
                                "        \"fps\": 30, \n" +
                                "        \"height\": 720, \n" +
                                "        \"http_headers\": {\n" +
                                "          \"Accept\": \"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\", \n" +
                                "          \"Accept-Charset\": \"ISO-8859-1,utf-8;q=0.7,*;q=0.7\", \n" +
                                "          \"Accept-Encoding\": \"gzip, deflate\", \n" +
                                "          \"Accept-Language\": \"en-us,en;q=0.5\", \n" +
                                "          \"User-Agent\": \"Mozilla/5.0 (X11; Linux x86_64; rv:10.0) Gecko/20150101 Firefox/47.0 (Chrome)\"\n" +
                                "        }, \n" +
                                "        \"player_url\": null, \n" +
                                "        \"preference\": -40, \n" +
                                "        \"protocol\": \"https\", \n" +
                                "        \"tbr\": 1598.027, \n" +
                                "        \"url\": \"https://r1---sn-p5qs7n7e.googlevideo.com/videoplayback?mime=video%2Fwebm&key=yt6&expire=1496868451&lmt=1496821415551140&ipbits=0&itag=247&pl=21&dur=184.666&source=youtube&pcm2=yes&requiressl=yes&gir=yes&id=o-AK6oXClKukrGjcel-IjVf80irXZt6hmOUfPtmHSwfnbL&keepalive=yes&clen=10542115&ip=54.90.126.14&ei=AxI4WbGHL8S88wSboJaICw&sparams=clen%2Cdur%2Cei%2Cgir%2Cid%2Cip%2Cipbits%2Citag%2Ckeepalive%2Clmt%2Cmime%2Cmm%2Cmn%2Cms%2Cmv%2Cpcm2%2Cpl%2Crequiressl%2Csource%2Cexpire&ms=au&signature=122A6409DB0DB189D5B71F3CB3E32E4F70FB44B9.08EE839016E5EE9566F38C66BD0D951006CB98AB&mt=1496846665&mv=u&mm=31&mn=sn-p5qs7n7e&ratebypass=yes\", \n" +
                                "        \"vcodec\": \"vp9\", \n" +
                                "        \"width\": 1280\n" +
                                "      }, \n" +
                                "      {\n" +
                                "        \"acodec\": \"none\", \n" +
                                "        \"asr\": null, \n" +
                                "        \"ext\": \"mp4\", \n" +
                                "        \"filesize\": 13487751, \n" +
                                "        \"format\": \"136 - 1280x720 (DASH video)\", \n" +
                                "        \"format_id\": \"136\", \n" +
                                "        \"format_note\": \"DASH video\", \n" +
                                "        \"fps\": 30, \n" +
                                "        \"height\": 720, \n" +
                                "        \"http_headers\": {\n" +
                                "          \"Accept\": \"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\", \n" +
                                "          \"Accept-Charset\": \"ISO-8859-1,utf-8;q=0.7,*;q=0.7\", \n" +
                                "          \"Accept-Encoding\": \"gzip, deflate\", \n" +
                                "          \"Accept-Language\": \"en-us,en;q=0.5\", \n" +
                                "          \"User-Agent\": \"Mozilla/5.0 (X11; Linux x86_64; rv:10.0) Gecko/20150101 Firefox/47.0 (Chrome)\"\n" +
                                "        }, \n" +
                                "        \"language\": null, \n" +
                                "        \"manifest_url\": \"https://manifest.googlevideo.com/api/manifest/dash/ip/54.90.126.14/requiressl/yes/playback_host/r1---sn-p5qs7n7e.googlevideo.com/sparams/as%2Cei%2Chfr%2Cid%2Cip%2Cipbits%2Citag%2Cmm%2Cmn%2Cms%2Cmv%2Cpl%2Cplayback_host%2Crequiressl%2Csource%2Cexpire/mv/u/mt/1496846665/signature/9A59EB354CE2F8E4002A803E9A74293D87700392.928FE2E316878C8247A3CC151C422FCFA6C4A890/ms/au/key/yt6/mm/31/ipbits/0/hfr/1/id/ee0eabb9157fa540/pl/21/source/youtube/mn/sn-p5qs7n7e/as/fmp4_audio_clear%2Cfmp4_sd_hd_clear/expire/1496868452/ei/BBI4WeuUDsXA8wTW1q3wAw/itag/0\", \n" +
                                "        \"preference\": -40, \n" +
                                "        \"protocol\": \"https\", \n" +
                                "        \"tbr\": 2321, \n" +
                                "        \"url\": \"https://r1---sn-p5qs7n7e.googlevideo.com/videoplayback?id=ee0eabb9157fa540&itag=136&source=youtube&requiressl=yes&mv=u&ms=au&mm=31&pl=21&mn=sn-p5qs7n7e&ei=BBI4WeuUDsXA8wTW1q3wAw&ratebypass=yes&mime=video/mp4&gir=yes&clen=13487751&lmt=1496819263816634&dur=184.699&mt=1496846665&signature=7C470B5D66225F46810AD19B3CDDDA3CF1323E3C.1BFB4B5A925C21A456AEDC87522227B06EEDF874&key=dg_yt0&ip=54.90.126.14&ipbits=0&expire=1496868452&sparams=ip,ipbits,expire,id,itag,source,requiressl,mv,ms,mm,pl,mn,ei,ratebypass,mime,gir,clen,lmt,dur\", \n" +
                                "        \"vcodec\": \"avc1.4d401f\", \n" +
                                "        \"width\": 1280\n" +
                                "      }, \n" +
                                "      {\n" +
                                "        \"acodec\": \"none\", \n" +
                                "        \"ext\": \"webm\", \n" +
                                "        \"filesize\": 19014533, \n" +
                                "        \"format\": \"248 - 1920x1080 (1080p)\", \n" +
                                "        \"format_id\": \"248\", \n" +
                                "        \"format_note\": \"1080p\", \n" +
                                "        \"fps\": 30, \n" +
                                "        \"height\": 1080, \n" +
                                "        \"http_headers\": {\n" +
                                "          \"Accept\": \"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\", \n" +
                                "          \"Accept-Charset\": \"ISO-8859-1,utf-8;q=0.7,*;q=0.7\", \n" +
                                "          \"Accept-Encoding\": \"gzip, deflate\", \n" +
                                "          \"Accept-Language\": \"en-us,en;q=0.5\", \n" +
                                "          \"User-Agent\": \"Mozilla/5.0 (X11; Linux x86_64; rv:10.0) Gecko/20150101 Firefox/47.0 (Chrome)\"\n" +
                                "        }, \n" +
                                "        \"player_url\": null, \n" +
                                "        \"preference\": -40, \n" +
                                "        \"protocol\": \"https\", \n" +
                                "        \"tbr\": 2750.142, \n" +
                                "        \"url\": \"https://r1---sn-p5qs7n7e.googlevideo.com/videoplayback?mime=video%2Fwebm&key=yt6&expire=1496868451&lmt=1496821083967442&ipbits=0&itag=248&pl=21&dur=184.666&source=youtube&pcm2=yes&requiressl=yes&gir=yes&id=o-AK6oXClKukrGjcel-IjVf80irXZt6hmOUfPtmHSwfnbL&keepalive=yes&clen=19014533&ip=54.90.126.14&ei=AxI4WbGHL8S88wSboJaICw&sparams=clen%2Cdur%2Cei%2Cgir%2Cid%2Cip%2Cipbits%2Citag%2Ckeepalive%2Clmt%2Cmime%2Cmm%2Cmn%2Cms%2Cmv%2Cpcm2%2Cpl%2Crequiressl%2Csource%2Cexpire&ms=au&signature=43EBAA81EE085C136EF67D2A9031EC4AFD92F7F7.D83476DD385F442835E88F18309A8DE1761FB01D&mt=1496846665&mv=u&mm=31&mn=sn-p5qs7n7e&ratebypass=yes\", \n" +
                                "        \"vcodec\": \"vp9\", \n" +
                                "        \"width\": 1920\n" +
                                "      }, \n" +
                                "      {\n" +
                                "        \"acodec\": \"none\", \n" +
                                "        \"asr\": null, \n" +
                                "        \"ext\": \"mp4\", \n" +
                                "        \"filesize\": 39898836, \n" +
                                "        \"format\": \"137 - 1920x1080 (DASH video)\", \n" +
                                "        \"format_id\": \"137\", \n" +
                                "        \"format_note\": \"DASH video\", \n" +
                                "        \"fps\": 30, \n" +
                                "        \"height\": 1080, \n" +
                                "        \"http_headers\": {\n" +
                                "          \"Accept\": \"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\", \n" +
                                "          \"Accept-Charset\": \"ISO-8859-1,utf-8;q=0.7,*;q=0.7\", \n" +
                                "          \"Accept-Encoding\": \"gzip, deflate\", \n" +
                                "          \"Accept-Language\": \"en-us,en;q=0.5\", \n" +
                                "          \"User-Agent\": \"Mozilla/5.0 (X11; Linux x86_64; rv:10.0) Gecko/20150101 Firefox/47.0 (Chrome)\"\n" +
                                "        }, \n" +
                                "        \"language\": null, \n" +
                                "        \"manifest_url\": \"https://manifest.googlevideo.com/api/manifest/dash/ip/54.90.126.14/requiressl/yes/playback_host/r1---sn-p5qs7n7e.googlevideo.com/sparams/as%2Cei%2Chfr%2Cid%2Cip%2Cipbits%2Citag%2Cmm%2Cmn%2Cms%2Cmv%2Cpl%2Cplayback_host%2Crequiressl%2Csource%2Cexpire/mv/u/mt/1496846665/signature/9A59EB354CE2F8E4002A803E9A74293D87700392.928FE2E316878C8247A3CC151C422FCFA6C4A890/ms/au/key/yt6/mm/31/ipbits/0/hfr/1/id/ee0eabb9157fa540/pl/21/source/youtube/mn/sn-p5qs7n7e/as/fmp4_audio_clear%2Cfmp4_sd_hd_clear/expire/1496868452/ei/BBI4WeuUDsXA8wTW1q3wAw/itag/0\", \n" +
                                "        \"preference\": -40, \n" +
                                "        \"protocol\": \"https\", \n" +
                                "        \"tbr\": 4331, \n" +
                                "        \"url\": \"https://r1---sn-p5qs7n7e.googlevideo.com/videoplayback?id=ee0eabb9157fa540&itag=137&source=youtube&requiressl=yes&mv=u&ms=au&mm=31&pl=21&mn=sn-p5qs7n7e&ei=BBI4WeuUDsXA8wTW1q3wAw&ratebypass=yes&mime=video/mp4&gir=yes&clen=39898836&lmt=1496819257287131&dur=184.699&mt=1496846665&signature=4303E70EDF712A08BE07FF6CF90DEAD4653CE8EF.04DD4DF2B97B8B4C1A3FB047099D0D64ECC97A31&key=dg_yt0&ip=54.90.126.14&ipbits=0&expire=1496868452&sparams=ip,ipbits,expire,id,itag,source,requiressl,mv,ms,mm,pl,mn,ei,ratebypass,mime,gir,clen,lmt,dur\", \n" +
                                "        \"vcodec\": \"avc1.640028\", \n" +
                                "        \"width\": 1920\n" +
                                "      }, \n" +
                                "      {\n" +
                                "        \"abr\": 24, \n" +
                                "        \"acodec\": \"mp4a.40.2\", \n" +
                                "        \"ext\": \"3gp\", \n" +
                                "        \"format\": \"17 - 176x144 (small)\", \n" +
                                "        \"format_id\": \"17\", \n" +
                                "        \"format_note\": \"small\", \n" +
                                "        \"height\": 144, \n" +
                                "        \"http_headers\": {\n" +
                                "          \"Accept\": \"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\", \n" +
                                "          \"Accept-Charset\": \"ISO-8859-1,utf-8;q=0.7,*;q=0.7\", \n" +
                                "          \"Accept-Encoding\": \"gzip, deflate\", \n" +
                                "          \"Accept-Language\": \"en-us,en;q=0.5\", \n" +
                                "          \"User-Agent\": \"Mozilla/5.0 (X11; Linux x86_64; rv:10.0) Gecko/20150101 Firefox/47.0 (Chrome)\"\n" +
                                "        }, \n" +
                                "        \"player_url\": null, \n" +
                                "        \"protocol\": \"https\", \n" +
                                "        \"resolution\": \"176x144\", \n" +
                                "        \"url\": \"https://r1---sn-p5qs7n7e.googlevideo.com/videoplayback?mime=video%2F3gpp&key=yt6&expire=1496868451&lmt=1496819262399065&ipbits=0&itag=17&pl=21&dur=184.830&source=youtube&pcm2=yes&requiressl=yes&gir=yes&id=o-AK6oXClKukrGjcel-IjVf80irXZt6hmOUfPtmHSwfnbL&clen=1804289&ip=54.90.126.14&ei=AxI4WbGHL8S88wSboJaICw&sparams=clen%2Cdur%2Cei%2Cgir%2Cid%2Cip%2Cipbits%2Citag%2Clmt%2Cmime%2Cmm%2Cmn%2Cms%2Cmv%2Cpcm2%2Cpl%2Crequiressl%2Csource%2Cexpire&ms=au&signature=762715AE7EEB957599DB729BEC404CC0DF9CE835.0CF1173FE56B89FEDD0CEC7EF3387EA2DBA7DF95&mt=1496846665&mv=u&mm=31&mn=sn-p5qs7n7e&ratebypass=yes\", \n" +
                                "        \"vcodec\": \"mp4v.20.3\", \n" +
                                "        \"width\": 176\n" +
                                "      }, \n" +
                                "      {\n" +
                                "        \"acodec\": \"mp4a.40.2\", \n" +
                                "        \"ext\": \"3gp\", \n" +
                                "        \"format\": \"36 - 320x180 (small)\", \n" +
                                "        \"format_id\": \"36\", \n" +
                                "        \"format_note\": \"small\", \n" +
                                "        \"height\": 180, \n" +
                                "        \"http_headers\": {\n" +
                                "          \"Accept\": \"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\", \n" +
                                "          \"Accept-Charset\": \"ISO-8859-1,utf-8;q=0.7,*;q=0.7\", \n" +
                                "          \"Accept-Encoding\": \"gzip, deflate\", \n" +
                                "          \"Accept-Language\": \"en-us,en;q=0.5\", \n" +
                                "          \"User-Agent\": \"Mozilla/5.0 (X11; Linux x86_64; rv:10.0) Gecko/20150101 Firefox/47.0 (Chrome)\"\n" +
                                "        }, \n" +
                                "        \"player_url\": null, \n" +
                                "        \"protocol\": \"https\", \n" +
                                "        \"resolution\": \"320x180\", \n" +
                                "        \"url\": \"https://r1---sn-p5qs7n7e.googlevideo.com/videoplayback?mime=video%2F3gpp&key=yt6&expire=1496868451&lmt=1496819266042105&ipbits=0&itag=36&pl=21&dur=184.830&source=youtube&pcm2=yes&requiressl=yes&gir=yes&id=o-AK6oXClKukrGjcel-IjVf80irXZt6hmOUfPtmHSwfnbL&clen=5072402&ip=54.90.126.14&ei=AxI4WbGHL8S88wSboJaICw&sparams=clen%2Cdur%2Cei%2Cgir%2Cid%2Cip%2Cipbits%2Citag%2Clmt%2Cmime%2Cmm%2Cmn%2Cms%2Cmv%2Cpcm2%2Cpl%2Crequiressl%2Csource%2Cexpire&ms=au&signature=03A34ABA7D9C7255C89088342570F73280B0E859.6B4E4B9397766DBC43579F1388BADC174604F41A&mt=1496846665&mv=u&mm=31&mn=sn-p5qs7n7e&ratebypass=yes\", \n" +
                                "        \"vcodec\": \"mp4v.20.3\", \n" +
                                "        \"width\": 320\n" +
                                "      }, \n" +
                                "      {\n" +
                                "        \"abr\": 128, \n" +
                                "        \"acodec\": \"vorbis\", \n" +
                                "        \"ext\": \"webm\", \n" +
                                "        \"format\": \"43 - 640x360 (medium)\", \n" +
                                "        \"format_id\": \"43\", \n" +
                                "        \"format_note\": \"medium\", \n" +
                                "        \"height\": 360, \n" +
                                "        \"http_headers\": {\n" +
                                "          \"Accept\": \"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\", \n" +
                                "          \"Accept-Charset\": \"ISO-8859-1,utf-8;q=0.7,*;q=0.7\", \n" +
                                "          \"Accept-Encoding\": \"gzip, deflate\", \n" +
                                "          \"Accept-Language\": \"en-us,en;q=0.5\", \n" +
                                "          \"User-Agent\": \"Mozilla/5.0 (X11; Linux x86_64; rv:10.0) Gecko/20150101 Firefox/47.0 (Chrome)\"\n" +
                                "        }, \n" +
                                "        \"player_url\": null, \n" +
                                "        \"protocol\": \"https\", \n" +
                                "        \"resolution\": \"640x360\", \n" +
                                "        \"url\": \"https://r1---sn-p5qs7n7e.googlevideo.com/videoplayback?mime=video%2Fwebm&key=yt6&expire=1496868451&lmt=1496821061082331&ipbits=0&itag=43&pl=21&dur=0.000&ratebypass=yes&source=youtube&pcm2=yes&requiressl=yes&gir=yes&id=o-AK6oXClKukrGjcel-IjVf80irXZt6hmOUfPtmHSwfnbL&clen=10613842&ip=54.90.126.14&ei=AxI4WbGHL8S88wSboJaICw&sparams=clen%2Cdur%2Cei%2Cgir%2Cid%2Cip%2Cipbits%2Citag%2Clmt%2Cmime%2Cmm%2Cmn%2Cms%2Cmv%2Cpcm2%2Cpl%2Cratebypass%2Crequiressl%2Csource%2Cexpire&ms=au&signature=2523ACEFA8939EACC2FFE1D4C856FD00D36A758B.D05DC5A03EF2E38B519D6196DD40A6F371E44888&mt=1496846665&mv=u&mm=31&mn=sn-p5qs7n7e\", \n" +
                                "        \"vcodec\": \"vp8.0\", \n" +
                                "        \"width\": 640\n" +
                                "      }, \n" +
                                "      {\n" +
                                "        \"abr\": 96, \n" +
                                "        \"acodec\": \"mp4a.40.2\", \n" +
                                "        \"ext\": \"mp4\", \n" +
                                "        \"format\": \"18 - 640x360 (medium)\", \n" +
                                "        \"format_id\": \"18\", \n" +
                                "        \"format_note\": \"medium\", \n" +
                                "        \"height\": 360, \n" +
                                "        \"http_headers\": {\n" +
                                "          \"Accept\": \"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\", \n" +
                                "          \"Accept-Charset\": \"ISO-8859-1,utf-8;q=0.7,*;q=0.7\", \n" +
                                "          \"Accept-Encoding\": \"gzip, deflate\", \n" +
                                "          \"Accept-Language\": \"en-us,en;q=0.5\", \n" +
                                "          \"User-Agent\": \"Mozilla/5.0 (X11; Linux x86_64; rv:10.0) Gecko/20150101 Firefox/47.0 (Chrome)\"\n" +
                                "        }, \n" +
                                "        \"player_url\": null, \n" +
                                "        \"protocol\": \"https\", \n" +
                                "        \"resolution\": \"640x360\", \n" +
                                "        \"url\": \"https://r1---sn-p5qs7n7e.googlevideo.com/videoplayback?mime=video%2Fmp4&key=yt6&expire=1496868451&lmt=1496819269698533&ipbits=0&itag=18&pl=21&dur=184.784&ratebypass=yes&source=youtube&pcm2=yes&requiressl=yes&gir=yes&id=o-AK6oXClKukrGjcel-IjVf80irXZt6hmOUfPtmHSwfnbL&clen=9660276&ip=54.90.126.14&ei=AxI4WbGHL8S88wSboJaICw&sparams=clen%2Cdur%2Cei%2Cgir%2Cid%2Cip%2Cipbits%2Citag%2Clmt%2Cmime%2Cmm%2Cmn%2Cms%2Cmv%2Cpcm2%2Cpl%2Cratebypass%2Crequiressl%2Csource%2Cexpire&ms=au&signature=1E583C7C0195E6EACDD7AD77B6A8E593E1E4FD00.0F24A2625030BE59BDF1DB61C0A607A96892A185&mt=1496846665&mv=u&mm=31&mn=sn-p5qs7n7e\", \n" +
                                "        \"vcodec\": \"avc1.42001E\", \n" +
                                "        \"width\": 640\n" +
                                "      }, \n" +
                                "      {\n" +
                                "        \"abr\": 192, \n" +
                                "        \"acodec\": \"mp4a.40.2\", \n" +
                                "        \"ext\": \"mp4\", \n" +
                                "        \"format\": \"22 - 1280x720 (hd720)\", \n" +
                                "        \"format_id\": \"22\", \n" +
                                "        \"format_note\": \"hd720\", \n" +
                                "        \"height\": 720, \n" +
                                "        \"http_headers\": {\n" +
                                "          \"Accept\": \"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\", \n" +
                                "          \"Accept-Charset\": \"ISO-8859-1,utf-8;q=0.7,*;q=0.7\", \n" +
                                "          \"Accept-Encoding\": \"gzip, deflate\", \n" +
                                "          \"Accept-Language\": \"en-us,en;q=0.5\", \n" +
                                "          \"User-Agent\": \"Mozilla/5.0 (X11; Linux x86_64; rv:10.0) Gecko/20150101 Firefox/47.0 (Chrome)\"\n" +
                                "        }, \n" +
                                "        \"player_url\": null, \n" +
                                "        \"protocol\": \"https\", \n" +
                                "        \"resolution\": \"1280x720\", \n" +
                                "        \"url\": \"https://r1---sn-p5qs7n7e.googlevideo.com/videoplayback?id=o-AK6oXClKukrGjcel-IjVf80irXZt6hmOUfPtmHSwfnbL&mime=video%2Fmp4&key=yt6&mn=sn-p5qs7n7e&expire=1496868451&lmt=1496819318724289&ipbits=0&ei=AxI4WbGHL8S88wSboJaICw&itag=22&sparams=dur%2Cei%2Cid%2Cip%2Cipbits%2Citag%2Clmt%2Cmime%2Cmm%2Cmn%2Cms%2Cmv%2Cpcm2%2Cpl%2Cratebypass%2Crequiressl%2Csource%2Cexpire&pl=21&dur=184.784&ms=au&signature=576D69894756AE18D49983902415462F197DA94F.9C236239B989906DB901536F3842B7E9DD62492E&ratebypass=yes&source=youtube&mv=u&mm=31&pcm2=yes&requiressl=yes&mt=1496846665&ip=54.90.126.14\", \n" +
                                "        \"vcodec\": \"avc1.64001F\", \n" +
                                "        \"width\": 1280\n" +
                                "      }\n" +
                                "    ], \n" +
                                "    \"height\": 720, \n" +
                                "    \"http_headers\": {\n" +
                                "      \"Accept\": \"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\", \n" +
                                "      \"Accept-Charset\": \"ISO-8859-1,utf-8;q=0.7,*;q=0.7\", \n" +
                                "      \"Accept-Encoding\": \"gzip, deflate\", \n" +
                                "      \"Accept-Language\": \"en-us,en;q=0.5\", \n" +
                                "      \"User-Agent\": \"Mozilla/5.0 (X11; Linux x86_64; rv:10.0) Gecko/20150101 Firefox/47.0 (Chrome)\"\n" +
                                "    }, \n" +
                                "    \"id\": \"7g6ruRV_pUA\", \n" +
                                "    \"is_live\": null, \n" +
                                "    \"license\": \"Standard YouTube License\", \n" +
                                "    \"like_count\": 4890, \n" +
                                "    \"player_url\": null, \n" +
                                "    \"playlist\": null, \n" +
                                "    \"playlist_index\": null, \n" +
                                "    \"protocol\": \"https\", \n" +
                                "    \"requested_subtitles\": null, \n" +
                                "    \"resolution\": \"1280x720\", \n" +
                                "    \"season_number\": null, \n" +
                                "    \"series\": null, \n" +
                                "    \"start_time\": null, \n" +
                                "    \"subtitles\": {}, \n" +
                                "    \"tags\": [\n" +
                                "      \"kok\", \n" +
                                "      \"bisa\", \n" +
                                "      \"kokbisa\", \n" +
                                "      \"kokbisa?\", \n" +
                                "      \"bisa?\", \n" +
                                "      \"indonesia\", \n" +
                                "      \"animasi\", \n" +
                                "      \"lucu\", \n" +
                                "      \"kocak\", \n" +
                                "      \"edukasi\", \n" +
                                "      \"belajar\", \n" +
                                "      \"hiburan\", \n" +
                                "      \"menghibur\", \n" +
                                "      \"entertainment\", \n" +
                                "      \"informatif\", \n" +
                                "      \"wawasan\", \n" +
                                "      \"sains\", \n" +
                                "      \"teknologi\", \n" +
                                "      \"technology\", \n" +
                                "      \"science\", \n" +
                                "      \"naga\", \n" +
                                "      \"dinosaurus\", \n" +
                                "      \"dragon\", \n" +
                                "      \"dracorex\", \n" +
                                "      \"hogwarts\", \n" +
                                "      \"komodo\", \n" +
                                "      \"the hobbit\"\n" +
                                "    ], \n" +
                                "    \"thumbnail\": \"https://i.ytimg.com/vi/7g6ruRV_pUA/maxresdefault.jpg\", \n" +
                                "    \"thumbnails\": [\n" +
                                "      {\n" +
                                "        \"id\": \"0\", \n" +
                                "        \"url\": \"https://i.ytimg.com/vi/7g6ruRV_pUA/maxresdefault.jpg\"\n" +
                                "      }\n" +
                                "    ], \n" +
                                "    \"title\": \"Apakah Naga Benar-benar Ada di Zaman Dinosaurus?\", \n" +
                                "    \"upload_date\": \"20170607\", \n" +
                                "    \"uploader\": \"Kok Bisa?\", \n" +
                                "    \"uploader_id\": \"UCu0yQD7NFMyLu_-TmKa4Hqg\", \n" +
                                "    \"uploader_url\": \"http://www.youtube.com/channel/UCu0yQD7NFMyLu_-TmKa4Hqg\", \n" +
                                "    \"url\": \"https://r1---sn-p5qs7n7e.googlevideo.com/videoplayback?id=o-AK6oXClKukrGjcel-IjVf80irXZt6hmOUfPtmHSwfnbL&mime=video%2Fmp4&key=yt6&mn=sn-p5qs7n7e&expire=1496868451&lmt=1496819318724289&ipbits=0&ei=AxI4WbGHL8S88wSboJaICw&itag=22&sparams=dur%2Cei%2Cid%2Cip%2Cipbits%2Citag%2Clmt%2Cmime%2Cmm%2Cmn%2Cms%2Cmv%2Cpcm2%2Cpl%2Cratebypass%2Crequiressl%2Csource%2Cexpire&pl=21&dur=184.784&ms=au&signature=576D69894756AE18D49983902415462F197DA94F.9C236239B989906DB901536F3842B7E9DD62492E&ratebypass=yes&source=youtube&mv=u&mm=31&pcm2=yes&requiressl=yes&mt=1496846665&ip=54.90.126.14\", \n" +
                                "    \"vcodec\": \"avc1.64001F\", \n" +
                                "    \"view_count\": 81232, \n" +
                                "    \"webpage_url\": \"https://www.youtube.com/watch?v=7g6ruRV_pUA\", \n" +
                                "    \"webpage_url_basename\": \"watch\", \n" +
                                "    \"width\": 1280\n" +
                                "  }, \n" +
                                "  \"url\": \"https://www.youtube.com/watch?v=7g6ruRV_pUA\"\n" +
                                "}";
                        JSONObject jsonObject = new JSONObject(string);
                        JSONObject info = new JSONObject(jsonObject.get("info").toString());

                        try {
                            getMessageData(info.get("acodec").toString(),idTarget);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        //getVideoData(idTarget,"https://youtu.be/7g6ruRV_pUA","https://lh4.googleusercontent.com/0MV5E36_Q8vgC6FuuFA83HjqUvvctjgKL4nv0FVtgYdcyDNoWQgkY_fSG_sJtmphrvYjJ969r1CkMaU=w1360-h613");
                        //getVideoData(idTarget,"");
                    }


                    if(msgText.contains("/help"))
                    {
                        try {
                            getMessageData("command list :\n/weather [city name];\n/[osu_mode] [nickname] eg : /mania jakads;\n/puasa [city_name]\n/bukalapak [product_name];\nbot leave for kick out this shit\n\n\nunder development for personal amusement\n-titus efferian",idTarget);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }


                } else {
                    String imagesUrl ="https://lh4.googleusercontent.com/0MV5E36_Q8vgC6FuuFA83HjqUvvctjgKL4nv0FVtgYdcyDNoWQgkY_fSG_sJtmphrvYjJ969r1CkMaU=w1360-h613";
                    if (payload.events[0].source.type.equals("group")){
                        try {
                            getMessageData("my name is Tamachan, i'm the one who is going to beat hibiki!",idTarget);
                            getMessageDataForImage(idTarget,imagesUrl);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        leaveGR(payload.events[0].source.groupId, "group");
                    } else if (payload.events[0].source.type.equals("room")){
                        try {
                            getMessageData("my name is Tamachan, i'm the one who is going to beat hibiki!",idTarget);
                            getMessageDataForImage(idTarget,imagesUrl);
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

    private void getMessageData(String message, String targetID) throws IOException{
        if (message!=null){
            pushMessage(targetID, message);
        }
    }

    private void getMessageDataForImage(String targetId,String string)throws  IOException
    {
        pushImageMessage(targetId,string);
    }
    private void getVideoData(String targetId,String videoString,String imageString)
    {
        pushVideoMessage(targetId,videoString,imageString);
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
    private void pushVideoMessage(String sourceId,String videoString,String imageString)
    {
        VideoMessage videoMessage = new VideoMessage(videoString,imageString);
        PushMessage pushMessage = new PushMessage(sourceId,videoMessage);
        response(pushMessage);
    }
    private void pushImageMessage(String sourceId,String string)
    {
        ImageMessage imageMessage = new ImageMessage(string,string);

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

