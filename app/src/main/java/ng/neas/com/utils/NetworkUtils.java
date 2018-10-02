package ng.neas.com.utils;



import android.app.Activity;

import com.elkanahtech.widerpay.myutils.listeners.MyCallBackListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import ng.neas.com.R;
import ng.neas.com.model.AlertEntity;


/**
 * Created by ADEOLU on 1/28/2018.
 */

public class NetworkUtils {
    public static void postRequest(final Activity activity, final AlertEntity pushEntity , final MyCallBackListener listener, final int callCode ){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(activity.getString(R.string.address_path));
                    HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setUseCaches(false);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Authorization",  "key=" + activity.getString(R.string.server_key));
                    conn.setConnectTimeout((int) TimeUnit.MINUTES.toMillis(3));
                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

                    JSONObject json = new JSONObject();

                    json.put("to", "/topics/Alert");
                    JSONObject info = new JSONObject();
                    info.put("title", alertType( pushEntity.getFaculty(), pushEntity.getDepartment()) + " - " + pushEntity.getTitle());
                    info.put("message", pushEntity.getDetail());
                    json.put("notification", info);
                    String postData = json.toString();
                    if(postData != null){
                        writer.write(postData);
                        writer.flush();
                        writer.close();
                        os.close();
                    }

                    BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                    StringBuilder sb = new StringBuilder();
                    String output;
                    while ((output = br.readLine()) != null) {
                        sb.append(output);
                    }
                    final String response = sb.toString();
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onProcessComplete(response, callCode);
                        }
                    });
                }catch (Exception e){
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onErrorOccur("Could not connect to the internet.\nPlease check your connection", callCode);
                        }
                    });
                }
            }
        }).start();
    }

    private static String alertType(String faculty, String department) {
        switch (faculty){
            case "General":
                return "General Alert";
            default:
                if(department.equals("General"))
                    return "Faculty Alert";
                else
                    return "Departmental Alert";
        }
    }

}