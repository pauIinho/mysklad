package info.mysklad.mysklad;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import info.mysklad.mysklad.app.AppConfig;
import info.mysklad.mysklad.helper.SQLiteHandler;
import info.mysklad.mysklad.helper.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends ActionBarActivity {

    private EditText search;
    private TextView text;
    private String imei;
    private String login;
    private String mac;
    private String version;
    SQLiteHandler db;
    SessionManager session;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        search = (EditText)findViewById(R.id.search);
        text = (TextView)findViewById(R.id.text_all);
        //SQLite db handler
        db = new SQLiteHandler(getApplicationContext());
        db.getWritableDatabase();
        //Session manager
        session = new SessionManager(getApplicationContext());
        //for logout
        //session.setLogin(false);
        //db.deleteUsers();
        //Check if user is already logged in or not
        new postRequest().execute();
        /*if(session.isLoggedIn()) {
            //User is already logged in. Take him to main activity
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }*/
    }

    /**
     * POST REQUEST
     */
    private class postRequest extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            TelephonyManager mngr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            //get values: login, mac, version
            imei = mngr.getDeviceId().toString();
            mac = imei;
            login = "i" + imei;
            version = "1.0";
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                HttpClient httpclient = new DefaultHttpClient();
                // specify the URL you want to post to
                HttpPost httppost = new HttpPost(AppConfig.URL_LOGIN);
                // create a list to store HTTP variables and their values
                List nameValuePairs = new ArrayList();
                // add an HTTP variable and value pair
                nameValuePairs.add(new BasicNameValuePair("login", login));
                nameValuePairs.add(new BasicNameValuePair("mac", mac));
                nameValuePairs.add(new BasicNameValuePair("version", version));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                // send the variable and value, in other words post, to the URL
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);
                return result;

            } catch (ClientProtocolException e) {
                // process execption
                return null;
            } catch (IOException e) {
                // process execption
                return null;
            }
        }

        protected void onPostExecute(String result) {
            String pass = result.split("~")[0];
            text.setText(result.substring(0,7));
            if(result.substring(0,7).equals("unknown")) {
                text.setText("Ошибка авторизации. Возможно, устройство не зарегистрировано в базе данных");
            }
            else {
                //get current time
                Date d = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy hh:mm");
                String now = dateFormat.toString();
                //user succesfully logged in
                //Create login session
                session.setLogin(true);
                db.addUser(login, pass, now);
            }
        }

    }

}
