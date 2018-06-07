package in.co.jaypatel.githubcommitwatcher;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

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

    final String PREFS_NAME = "MyPrefsFile";
    SharedPreferences shereSharedPreferences;
    TextView tvOwner, tvRepo, tvNoInternet, tvValidRepo;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvOwner = findViewById(R.id.tvOwner);
        tvRepo = findViewById(R.id.tvRepo);
        tvNoInternet = findViewById(R.id.tvNoInternet);
        tvValidRepo = findViewById(R.id.tvValidUrl);
        swipeRefreshLayout = findViewById(R.id.swiperefresh);

        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestCommentJSON();
            }
        });

        shereSharedPreferences = getSharedPreferences(PREFS_NAME, 0);

        if (shereSharedPreferences.getString("url", "empty").equals("empty"))
            shereSharedPreferences.edit().putString("url", "https://api.github.com/repos/rails/rails/commits").apply();

        requestCommentJSON();

    }

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        }
        return false;
    }

    private void setOwnerAndRepo() throws Exception{
        String urlText = shereSharedPreferences.getString("url", "");
        if (urlText.length() > 0) {
            String[] urls = urlText.substring(urlText.indexOf("github."), urlText.length()).split("/");
            String owner = "Owner: " + urls[2];
            String repo = "Repo: " + urls[3];
            tvOwner.setText(owner);
            tvRepo.setText(repo);
        } else {
            tvOwner.setText("Owner: ");
            tvRepo.setText("Repo: ");
        }

    }

    private void requestCommentJSON() {
        if (isNetworkAvailable(this)) {
            tvNoInternet.setVisibility(View.GONE);
            tvValidRepo.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            if(swipeRefreshLayout.isRefreshing())
                swipeRefreshLayout.setRefreshing(false);

            RequestQueue queue = Volley.newRequestQueue(this);
            String url = shereSharedPreferences.getString("url", "https://api.github.com/repos/rails/rails/commits");

            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                parseJSON(response);
                            } catch (Exception e) {
                                e.printStackTrace();
                                tvValidRepo.setVisibility(View.VISIBLE);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    tvValidRepo.setVisibility(View.VISIBLE);
                }
            });

            queue.add(stringRequest);
        } else {
            tvNoInternet.setVisibility(View.VISIBLE);
            tvValidRepo.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_edit) {
            showURLDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showURLDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.new_entry_dialog, null);
        final AlertDialog dialog;
        builder.setView(dialogView);

        final EditText url;
        url = dialogView.findViewById(R.id.et_url);
        Button ok = dialogView.findViewById(R.id.btn_proceed);

        String urlText = shereSharedPreferences.getString("url", "");
        if (urlText.length() > 0) {
            String[] urls = urlText.substring(urlText.indexOf("github."), urlText.length()).split("/");
            urlText = "https://github.com/" + urls[2] + "/" + urls[3];
        }
        url.setText(urlText);

        dialog = builder.create();
        dialog.show();

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Patterns.WEB_URL.matcher(url.getText().toString().toLowerCase()).matches()) {
                    try {
                        dialog.cancel();

                        String urlText = url.getText().toString();
                        String[] urls = urlText.substring(urlText.indexOf("github."), urlText.length()).split("/");

                        String apiURL = "https://api.github.com/repos/" + urls[1] + "/" + urls[2] + "/commits";
                        shereSharedPreferences.edit().putString("url", apiURL).apply();
                        requestCommentJSON();
                    } catch (Exception e) {
                        tvValidRepo.setVisibility(View.VISIBLE);
                    }
                } else {
                    url.setError("Please enter valid URL");
                }
            }
        });

    }


    private void parseJSON(String response) throws Exception {
        JSONArray jsonArray = new JSONArray(response);

        commitList.clear();

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

        if (adapter == null) {
            adapter = new CommitAdapter(this, commitList);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }

        setOwnerAndRepo();
        swipeRefreshLayout.setRefreshing(false);
        progressBar.setVisibility(View.GONE);
    }
}
