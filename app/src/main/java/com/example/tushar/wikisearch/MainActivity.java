package com.example.tushar.wikisearch;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.example.tushar.wikisearch.adapter.WikiAdapter;
import com.example.tushar.wikisearch.data.WikiItem;
import com.example.tushar.wikisearch.data.WikiSuggestion;
import com.example.tushar.wikisearch.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnKeyListener {

    boolean isConnected ;

    FloatingSearchView floatingSearchView;

    RecyclerView recyclerView;

    TextView pageTitleTextView;

    TextView noResultsTextView;

    private List<WikiItem> wikiItems = new ArrayList<>();

    private WikiAdapter wikiAdapter;

    RequestQueue requestQueue ;

    List<WikiSuggestion> wikiSuggestions ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if(!isConnected) {

            Toast.makeText(this, "You are offline!", Toast.LENGTH_SHORT).show();

        }

        VolleyLog.DEBUG = true;

        requestQueue = Volley.newRequestQueue(this);

        wikiSuggestions = new ArrayList<>();

        floatingSearchView = findViewById(R.id.floating_search_view);

        recyclerView = findViewById(R.id.recycler_view);

        pageTitleTextView = findViewById(R.id.page_title_text_view);

        noResultsTextView = findViewById(R.id.no_results_text_view);

        wikiAdapter = new WikiAdapter(wikiItems, new WikiAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(WikiItem item) {

                Log.i("Item clicked: ", item.getTitle());
                Toast.makeText(MainActivity.this, "Loading wiki page for " + item.getTitle(), Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(MainActivity.this, WikiDetailsActivity.class);
                intent.putExtra(Constants.PAGEID, Integer.toString(item.getPageId()));
                startActivity(intent);

            }
        }, this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(wikiAdapter);

        floatingSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, String newQuery) {

                if (!oldQuery.equals("") && newQuery.equals("")) {

                    floatingSearchView.clearSuggestions();

                } else {

                    requestQueue.cancelAll("req");

                    String queryUrl = generateSearchQuery(newQuery);

                    getSuggestionData(queryUrl);
                }

            }
        });

        floatingSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(SearchSuggestion wikiSuggestion) {

                Log.i("suggestion click", wikiSuggestion.getBody());

                checkNetworkState();

                pageTitleTextView.setText("Wiki search for " + wikiSuggestion.getBody());

                requestQueue.cancelAll("req");

                String queryUrl = generateSearchQuery(wikiSuggestion.getBody());

                prepareWikiData(queryUrl);

                floatingSearchView.clearQuery();

                floatingSearchView.clearSearchFocus();

            }

            @Override
            public void onSearchAction(String currentQuery) {

                checkNetworkState();

                pageTitleTextView.setText("Wiki search for " + currentQuery);

                requestQueue.cancelAll("req");

                String queryUrl = generateSearchQuery(currentQuery);

                prepareWikiData(queryUrl);

                floatingSearchView.clearQuery();

                floatingSearchView.clearSearchFocus();

            }
        });

    }


    private boolean checkNetworkState() {

        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if(isConnected == this.isConnected) {

            // do nothing

        } else {

            if(isConnected) {

                Toast.makeText(this, "You are online!", Toast.LENGTH_SHORT).show();

            } else {

                Toast.makeText(this, "You are offline!", Toast.LENGTH_SHORT).show();

            }

        }

        this.isConnected = isConnected;

        return isConnected;

    }

    private void prepareWikiData(String url) {

        wikiItems.clear();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, new JSONObject(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if(response != null) {
                    try {

                        JSONObject jsonObjectBasic = response.getJSONObject("query");
                        JSONArray jsonArray = jsonObjectBasic.getJSONArray("pages");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            try {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);

                                WikiItem wikiItem = new WikiItem();

                                if (jsonObject.has("title")) {
                                    wikiItem.setTitle(jsonObject.getString("title"));
                                }

                                if (jsonObject.has("pageid")) {
                                    wikiItem.setPageId(jsonObject.getInt("pageid"));
                                }

                                if (jsonObject.has("thumbnail")) {
                                    JSONObject thumbnailObject = jsonObject.getJSONObject("thumbnail");

                                    if (thumbnailObject.has("source")) {
                                        wikiItem.setThumbnailUrl(thumbnailObject.getString("source"));
                                    }

                                }

                                if (jsonObject.has("terms")) {
                                    JSONObject termsObject = jsonObject.getJSONObject("terms");
                                    if (termsObject.has("description")) {
                                        JSONArray descriptionArray = termsObject.getJSONArray("description");

                                        if (descriptionArray != null && descriptionArray.length() != 0) {
                                            wikiItem.setDescription(descriptionArray.getString(0));
                                        } else {
                                            wikiItem.setDescription("Description not available");
                                        }
                                    } else {
                                        wikiItem.setDescription("Description not available");
                                    }
                                } else {
                                    wikiItem.setDescription("Description not available");
                                }

                                wikiItems.add(wikiItem);

                                Log.i("jsonobject", jsonObject.getString("title"));

                            } catch (JSONException e) {

                                e.printStackTrace();

                            }
                        }

                        wikiAdapter.notifyDataSetChanged();

                        noResultsTextView.setVisibility(View.GONE);

                    } catch (JSONException e) {
                        e.printStackTrace();

                        noResultsTextView.setVisibility(View.VISIBLE);

                    }
                } else {

                    noResultsTextView.setVisibility(View.VISIBLE);

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.e("Volley", error.toString());

                noResultsTextView.setVisibility(View.VISIBLE);

            }
        }) {
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    Cache.Entry cacheEntry = HttpHeaderParser.parseCacheHeaders(response);
                    if (cacheEntry == null) {
                        cacheEntry = new Cache.Entry();
                    }
                    final long cacheHitButRefreshed = 3 * 60 * 1000; // in 3 minutes cache will be hit, but also refreshed on background
                    final long cacheExpired = 24 * 60 * 1000; // in 24 hours this cache entry expires completely
                    long now = System.currentTimeMillis();
                    final long softExpire = now + cacheHitButRefreshed;
                    final long ttl = now + cacheExpired;
                    cacheEntry.data = response.data;
                    cacheEntry.softTtl = softExpire;
                    cacheEntry.ttl = ttl;
                    String headerValue;
                    headerValue = response.headers.get("Date");
                    if (headerValue != null) {
                        cacheEntry.serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    headerValue = response.headers.get("Last-Modified");
                    if (headerValue != null) {
                        cacheEntry.lastModified = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    cacheEntry.responseHeaders = response.headers;
                    final String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(new JSONObject(jsonString), cacheEntry);
                } catch (UnsupportedEncodingException | JSONException e) {
                    return Response.error(new ParseError(e));
                }
            }

            @Override
            protected void deliverResponse(JSONObject response) {
                super.deliverResponse(response);
            }

            @Override
            public void deliverError(VolleyError error) {
                super.deliverError(error);
            }

            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {
                return super.parseNetworkError(volleyError);
            }
        };

        jsonObjectRequest.setTag("req");

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);

    }

    private String generateSearchQuery(String query) {

        String[] queryParts = query.split(" ");

        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("https://en.wikipedia.org//w/api.php?action=query&format=json&prop=pageimages%7Cpageterms&generator=prefixsearch&redirects=1&" +
                "formatversion=2&piprop=thumbnail&pithumbsize=50&pilimit=10&wbptterms=description&gpssearch=");

        for (int i = 0; i<queryParts.length; i++) {

            queryBuilder.append(queryParts[i]);

            if(i < queryParts.length - 1) {

                queryBuilder.append("+");

            }

        }

        queryBuilder.append("&gpslimit=10");

        return queryBuilder.toString();

    }

    private void getSuggestionData(String url) {

        wikiSuggestions = new ArrayList<>();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, new JSONObject(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {

                    JSONObject jsonObjectBasic = response.getJSONObject("query");
                    JSONArray jsonArray = jsonObjectBasic.getJSONArray("pages");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            WikiSuggestion wikiSuggestion = new WikiSuggestion(jsonObject.getString("title"));
                            wikiSuggestions.add(wikiSuggestion);

                            Log.i("jsonobject", jsonObject.getString("title"));

                        } catch (JSONException e) {

                            e.printStackTrace();

                        }
                    }

                    floatingSearchView.swapSuggestions(wikiSuggestions);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.e("Volley", error.toString());

            }
        }) {
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    Cache.Entry cacheEntry = HttpHeaderParser.parseCacheHeaders(response);
                    if (cacheEntry == null) {
                        cacheEntry = new Cache.Entry();
                    }
                    final long cacheHitButRefreshed = 3 * 60 * 1000; // in 3 minutes cache will be hit, but also refreshed on background
                    final long cacheExpired = 24 * 60 * 60 * 1000; // in 24 hours this cache entry expires completely
                    long now = System.currentTimeMillis();
                    final long softExpire = now + cacheHitButRefreshed;
                    final long ttl = now + cacheExpired;
                    cacheEntry.data = response.data;
                    cacheEntry.softTtl = softExpire;
                    cacheEntry.ttl = ttl;
                    String headerValue;
                    headerValue = response.headers.get("Date");
                    if (headerValue != null) {
                        cacheEntry.serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    headerValue = response.headers.get("Last-Modified");
                    if (headerValue != null) {
                        cacheEntry.lastModified = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    cacheEntry.responseHeaders = response.headers;
                    final String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(new JSONObject(jsonString), cacheEntry);
                } catch (UnsupportedEncodingException | JSONException e) {
                    return Response.error(new ParseError(e));
                }
            }

            @Override
            protected void deliverResponse(JSONObject response) {
                super.deliverResponse(response);
            }

            @Override
            public void deliverError(VolleyError error) {
                super.deliverError(error);
            }

            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {
                return super.parseNetworkError(volleyError);
            }
        };

        jsonObjectRequest.setTag("req");

        requestQueue.add(jsonObjectRequest);

    }

    @Override
    public boolean onKey(View v, int i, KeyEvent event) {
        if(i == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {

            String queryUrl = generateSearchQuery(floatingSearchView.getQuery());

            prepareWikiData(queryUrl);

            floatingSearchView.clearQuery();

            floatingSearchView.clearSearchFocus();

        }

        return false;
    }
}
