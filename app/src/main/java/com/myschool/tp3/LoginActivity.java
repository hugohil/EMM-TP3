package com.myschool.tp3;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by perso on 16/06/15.
 */
public class LoginActivity extends ActionBarActivity {
    private SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void onClickLogin(View view) {

        EditText editTextEmail = (EditText) findViewById(R.id.act_register_email);
        final String email = editTextEmail.getText().toString();

        EditText editTextPassword = (EditText) findViewById(R.id.act_register_password);
        final String password = editTextPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {

            Toast toast = Toast.makeText(this, R.string.empty_form, Toast.LENGTH_SHORT);
            toast.show();
        } else {
            String params = "{\"email\": \""+ email +"\", \"password\": \""+ password +"\"}";
            if(hasConnection()){
                RequestQueue requestQueue = Volley.newRequestQueue(this);
                JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                        "http://questioncode.fr:10007/auth/local",
                        params,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                String token;
                                JSONObject json;

                                try {
                                    json = new JSONObject(response.toString());
                                    token = json.getString("token");

                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putString(email + "_password", password);
                                    editor.putString(email + "_token", token);
                                    editor.putString("ACTIVE_USER", email);
                                    editor.apply();

                                    Intent welcomeIntent = new Intent(getApplicationContext(), WelcomeActivity.class);
                                    startActivity(welcomeIntent);
                                    finish();
                                } catch (JSONException e) {

                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                String json;
                                NetworkResponse response = error.networkResponse;
                                if (response.statusCode == 422) {
                                    Toast toast = Toast.makeText(getApplicationContext(), R.string.error_user_exist, Toast.LENGTH_LONG);
                                    toast.show();
                                } else if (response.statusCode == 400) {
                                    Toast toast = Toast.makeText(getApplicationContext(), R.string.error_server_off, Toast.LENGTH_LONG);
                                    toast.show();
                                } else {
                                    json = new String(response.data);
                                    if (json != null) {
                                        Toast toast = Toast.makeText(getApplicationContext(), json, Toast.LENGTH_LONG);
                                        toast.show();
                                    }
                                }
                            }
                        });
                requestQueue.add(jsonObjReq);
            }
        }
    }

    public boolean hasConnection() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
