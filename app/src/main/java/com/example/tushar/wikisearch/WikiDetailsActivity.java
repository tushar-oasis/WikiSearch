package com.example.tushar.wikisearch;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.tushar.wikisearch.data.WikiSuggestion;
import com.example.tushar.wikisearch.utils.Constants;
import com.example.tushar.wikisearch.webviewclient.AppWebViewClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class WikiDetailsActivity extends AppCompatActivity {

    WebView wikiDetailsWebView;

    ProgressBar wikiDetailsProgessBar;

    ImageView wikiDetailsBackImageView;

    String pageUrl;

    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wiki_details);

        activity = this;

        wikiDetailsWebView = findViewById(R.id.wiki_details_web_view);

        wikiDetailsProgessBar = findViewById(R.id.wiki_details_progress_bar);

        wikiDetailsBackImageView = findViewById(R.id.wiki_details_back_image_view);

        String pageId;
        Bundle extras = getIntent().getExtras();
        if(extras == null) {

            pageId= null;

        } else {

            pageId= extras.getString(Constants.PAGEID);

        }

        wikiDetailsWebView.setWebViewClient(new AppWebViewClient(wikiDetailsProgessBar));

        wikiDetailsBackImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activity.finish();

            }
        });

        String queryUrl = generateQueryUrl(pageId);

        String pageUrl = getPageUrl(queryUrl, pageId, this);

    }

    private String getPageUrl(String queryUrl, final String pageId, final WikiDetailsActivity context) {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(queryUrl, new JSONObject(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {

                    JSONObject jsonObjectBasic = response.getJSONObject("query");

                    if(jsonObjectBasic.has("pages")) {

                        JSONObject pageObject = jsonObjectBasic.getJSONObject("pages");

                        if(pageObject.has(pageId)) {

                            JSONObject idObject = pageObject.getJSONObject(pageId);

                            if(idObject.has("fullurl")) {

                                String pageUrl = idObject.getString("fullurl");

                                if(pageUrl != null && !pageUrl.equals("")) {

                                    wikiDetailsWebView.loadUrl(pageUrl);

                                } else {

                                    wikiDetailsWebView.loadUrl("https://www.google.com");

                                }

                            }

                        }

                    }

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

        jsonObjectRequest.setTag("pageUrlReq");

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);

        return null;
    }

    private String generateQueryUrl(String pageId) {

        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("https://en.wikipedia.org/w/api.php?action=query&prop=info&pageids=");

        queryBuilder.append(pageId);

        queryBuilder.append("&inprop=url&format=json");

        return queryBuilder.toString();

    }

}
