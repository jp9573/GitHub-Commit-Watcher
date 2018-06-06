package in.co.jaypatel.githubcommitwatcher;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<Commit> commitList = new ArrayList<>();
    RecyclerView recyclerView;
    CommitAdapter adapter;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        requestCommentJSON();

    }

    private void requestCommentJSON() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://api.github.com/repos/rails/rails/commits";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Toast.makeText(MainActivity.this, response, Toast.LENGTH_LONG).show();
                        try {
                            parseJSON(response);
                        } catch (JSONException | ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        queue.add(stringRequest);
    }

    private void parseJSON(String response) throws JSONException, ParseException {
        JSONArray jsonArray = new JSONArray(response);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject authorJsonObject = jsonArray.getJSONObject(i).getJSONObject("author");
            JSONObject commitJsonObject = jsonArray.getJSONObject(i).getJSONObject("commit");
            JSONObject commitedAutherObject = commitJsonObject.getJSONObject("author");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

            String name = commitedAutherObject.getString("name");
            Date date = sdf.parse(commitedAutherObject.getString("date"));
            String email = commitedAutherObject.getString("email");
            String message = commitJsonObject.getString("message");

            String userName = authorJsonObject.getString("login");
            String avatarURL = authorJsonObject.getString("avatar_url");

            Commit commit = new Commit(name, date, email, message, userName, avatarURL);
            commitList.add(commit);
        }

        adapter = new CommitAdapter(this, commitList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        progressBar.setVisibility(View.GONE);
    }
}
