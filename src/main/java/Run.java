import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONPath;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.*;

public class Run {

    private static String playAddr;
    private static String videoAddr;
    private static String resourceAddr;

    private static String url;

    public static void main(String[] args) {

        System.out.print("请输入视频地址：");
        Scanner in = new Scanner(System.in);
        url = in.next();

        Properties properties = new Properties();

        try (CloseableHttpClient httpclient = HttpClients.createDefault();
             InputStream is = Thread.currentThread()
                     .getContextClassLoader()
                     .getResource("config.properties")
                     .openStream()) {

            properties.load(is);

            HttpGet httpGet = new HttpGet(url);
            setCustomHttpGet(httpGet);

            try (CloseableHttpResponse response = httpclient.execute(httpGet)) {

                System.out.println(response.getCode() + " " + response.getReasonPhrase());
                HttpEntity entity = response.getEntity();
                playAddr = getPlayAddr(EntityUtils.toString(entity),properties.getProperty("DOMAIN"));

            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            httpGet = new HttpGet(playAddr);
            setCustomHttpGet(httpGet);

            try (CloseableHttpResponse response = httpclient.execute(httpGet)) {

                System.out.println(response.getCode() + " " + response.getReasonPhrase());
                HttpEntity entity = response.getEntity();
                videoAddr = getVideoAddr(EntityUtils.toString(entity));

            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            httpGet = new HttpGet(videoAddr);
            setCustomHttpGet(httpGet,new URL(videoAddr).getHost());

            try (CloseableHttpResponse response = httpclient.execute(httpGet)) {

                System.out.println(response.getCode() + " " + response.getReasonPhrase());
                HttpEntity entity = response.getEntity();
                resourceAddr = getResourceAddr(EntityUtils.toString(entity),new URL(videoAddr).getProtocol() + "://" + new URL(videoAddr).getHost() + "/");

            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            System.out.println(resourceAddr);

        } catch (IOException e) {

            throw new RuntimeException();

        }

    }

    private static String getPlayAddr(String htmlDoc,String domain) {

        var doc = Jsoup.parse(htmlDoc);
        var linkEle = doc.select(".play-list").select("[title='电脑版播放']");
        var link = linkEle.attr("href");
        return domain + link.substring(1);

    }

    private static String getVideoAddr(String htmlDoc) {

        var doc = Jsoup.parse(htmlDoc);
        var scriptEle = doc.select("script");
        var scriptText = scriptEle.get(10).toString();
        var jsonData = scriptText.substring(scriptText.indexOf("{"), scriptText.lastIndexOf("}") + 1);
        var json = JSON.parseObject(jsonData);
        return JSONPath.eval(json, "$.url").toString();

    }

    private static String getResourceAddr(String htmlDoc,String domain) throws MalformedURLException {

        var doc = Jsoup.parse(htmlDoc);
        var htmlText = doc.toString();
        var middleText01 = htmlText.substring(htmlText.indexOf("main"));
        var sourceAuthParam = middleText01.substring(0,middleText01.indexOf("\";") + 1);
        var middleText02 = middleText01.substring(middleText01.indexOf("\"") + 1);
        sourceAuthParam = middleText02.substring(0,middleText02.indexOf("\"")).substring(1);

        URL authURL = new URL(compile("\\s*|\t|\r|\n").matcher(domain + sourceAuthParam).replaceAll(""));

        var authParam = authURL.getQuery();

        middleText01 = htmlText.substring(htmlText.indexOf("mp4"));
        var sourceResourceAddr = middleText01.substring(0,middleText01.indexOf("\";") + 1);
        middleText02 = sourceResourceAddr.substring(sourceResourceAddr.indexOf("\"") + 1);
        sourceResourceAddr = middleText02.substring(0,middleText02.indexOf("\"")).substring(1);

        URL resourceAddr = new URL(compile("\\s*|\t|\r|\n").matcher(domain + sourceResourceAddr).replaceAll(""));

        return resourceAddr + "?" + authParam;

    }

    private static void setCustomHttpGet(HttpGet httpGet) {

        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.132 Safari/537.36");
        httpGet.setHeader("Accept","*/*");
        httpGet.setHeader("Accept-Encoding","gzip, deflate, br");
        httpGet.setHeader("Connection","keep-alive");
        httpGet.setHeader("Host","www.truu196.xyz:1111");
        httpGet.setHeader("If-None-Match","W/\"5185-MK+1lwrSfjYobL8rWJq+iLR2qOI\"");
        httpGet.setHeader("Referer","http://www.truu196.xyz:1111/");

    }

    private static void setCustomHttpGet(HttpGet httpGet,String host) {

        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.132 Safari/537.36");
        httpGet.setHeader("Accept","*/*");
        httpGet.setHeader("Accept-Encoding","gzip, deflate, br");
        httpGet.setHeader("Connection","keep-alive");
        httpGet.setHeader("Host",host);
        httpGet.setHeader("If-None-Match","W/\"5185-MK+1lwrSfjYobL8rWJq+iLR2qOI\"");
        httpGet.setHeader("Referer","http://www.truu196.xyz:1111/");

    }

}
