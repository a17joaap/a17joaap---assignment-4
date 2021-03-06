package com.example.brom.listviewjsonapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ArrayList<Mountain> mountainList = new ArrayList<>();
    MountainAdapter adapter;
    ListView listView;
    public static final String mName = "Mountain Name";
    public static final String mLoc = "Mountain Location";
    public static final String mHeight = "Mountain Height";
    public static final String mURL = "Picture URL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FetchData getJson = new FetchData();
        getJson.execute();

        listView = (ListView) findViewById(R.id.mtnList);

        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        String mountain = mountainList.get(i).name;
                        String location = mountainList.get(i).location;
                        String height = mountainList.get(i).height;
                        String url = mountainList.get(i).pictureURL;

                        Intent intent = new Intent(getApplicationContext(),MountainActivity.class);
                        intent.putExtra(mName, mountain);
                        intent.putExtra(mLoc, location);
                        intent.putExtra(mHeight, height);
                        intent.putExtra(mURL, url);

                        startActivity(intent);
                    }
                }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            mountainList.clear();
            //mountainNames.clear();
            new FetchData().execute();
            Toast refreshed = Toast.makeText(this, "List refreshed", Toast.LENGTH_SHORT);
            refreshed.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class FetchData extends AsyncTask<Void,Void,String>{
        @Override
        protected String doInBackground(Void... params) {
            // These two variables need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a Java string.
            String jsonStr = null;

            try {
                // Construct the URL for the Internet service
                URL url = new URL("http://wwwlab.iit.his.se/brom/kurser/mobilprog/dbservice/admin/getdataasjson.php?type=brom");

                // Create the request to the PHP-service, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                jsonStr = buffer.toString();
                return jsonStr;
            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in
                // attempting to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("Network error", "Error closing stream", e);
                    }
                }
            }
        }
        @Override
        protected void onPostExecute(String o) {
            super.onPostExecute(o);
            // This code executes after we have received our data. The String object o holds
            // the un-parsed JSON string or is null if we had an IOException during the fetch.

            // Implement a parsing code that loops through the entire JSON and creates objects
            // of our newly created Mountain class.

            try {
                JSONArray mountains = new JSONArray(o);
                for (int i = 0; i < mountains.length(); i++) {
                    JSONObject mountain = mountains.getJSONObject(i);
                    // mountain.getJSONObject("auxdata") threw an error saying it was a string, so this was the solution..
                    String auxdata = mountain.getString("auxdata");
                    JSONObject seemsLikeThereShouldBeAnEasierWayToDoThis = new JSONObject(auxdata);

                    String name = mountain.getString("name");
                    String location = mountain.getString("location");
                    String height = mountain.getString("size");
                    String picUrl = seemsLikeThereShouldBeAnEasierWayToDoThis.getString("img");

                    Mountain m = new Mountain(name,location,height,picUrl);
                    mountainList.add(m);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            adapter = new MountainAdapter(getApplicationContext(), mountainList);
            listView.setAdapter(adapter);
        }
    }
}



