import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;


/**
 * Send your CAPTCHAs to 2Captcha.com and get them solved automatically by humans. 
 * @author Samuel Facchinello
 */
public class TwoCaptchaDotComSolver {

//    public static void main(String[] args) throws Exception {
//        String key = ""; //get your key on http://2captcha.com/
//        File file = new File("C:/image.jpg"); //image
//        //System.out.println(solveCaptcha(key, file));
//    }

    /**
     * https://2captcha.com/setting
     * @param key is your key of 32 symbols length.
     * @param file is your Image (Captcha) File 
     * @return the letters and numbers of your captcha
     * @throws Exception
     */
    public static String solveCaptcha(String key, File file) throws Exception{
        String ret = getId(key, file);

        if(ret!=null){
            if(ret.startsWith("OK")){
                String id = ret.split("[|]")[1];
                String captchaSolved = getCaptcha(key, id);
                if(captchaSolved.startsWith("OK")){
                    return captchaSolved.split("[|]")[1];
                }else{
                    throw new Exception(captchaSolved);
                }
            }else{
                throw new Exception(ret);
            }
        }else{
            throw new Exception("ID NULL");
        }
    }

    private static String getId(String key, File file) throws Exception {
        //System.out.println("sending image, key:" + key);
        HttpClient httpclient =  HttpClientBuilder.create().build();

        HttpPost httppost = new HttpPost("http://2captcha.com/in.php");

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        
        builder.addTextBody("method", "post");
        builder.addTextBody("key", key);
        //TODO: Additional CAPTCHA parameters (https://2captcha.com/setting)
//        builder.addTextBody("min_len", "4");
//        builder.addTextBody("max_len", "10");
//        builder.addTextBody("phrase", "0");
//        builder.addTextBody("regsense", "0");
//        builder.addTextBody("language", "2");
//        builder.addTextBody("calc", "0");
//        builder.addTextBody("numeric", "3");
        builder.addBinaryBody("file", file, ContentType.create("image/png"), "img.png");

        HttpEntity entity = builder.build();
        httppost.setEntity(entity);
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity resEntity = response.getEntity();

        String ret = "";
        if (resEntity != null) {
            ret = EntityUtils.toString(resEntity);
        }
        if (resEntity != null) {
            EntityUtils.consume(resEntity);
        }
        

        return ret;
    }

    private static String getCaptcha(String key, String id) throws Exception {
        //System.out.println("solving captcha " + id);
        URL url = new URL("http://2captcha.com/res.php?key="+key+"&action=get&id="+id);
        HttpURLConnection conn= (HttpURLConnection) url.openConnection();           
        conn.setRequestMethod( "GET" );
        conn.setRequestProperty( "charset", "utf-8");
        conn.setUseCaches( false );

        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

        String ret = "";
        for (int c; (c = in.read()) >= 0;){
            ret += (char)c;
        }
        if(ret.equals("CAPCHA_NOT_READY")){
            Thread.sleep(500);
            return getCaptcha(key, id);
        }
        return ret;
    }


}
