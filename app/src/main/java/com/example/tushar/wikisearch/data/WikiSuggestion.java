package com.example.tushar.wikisearch.data;

import android.os.Parcel;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

public class WikiSuggestion implements SearchSuggestion {

    private String wikiName;
    private boolean mIsHistory = false;

    public WikiSuggestion(String suggestion) {
        this.wikiName = suggestion.toLowerCase();
    }

    public WikiSuggestion(Parcel source) {
        this.wikiName = source.readString();
        this.mIsHistory = source.readInt() != 0;
    }


    public void setIsHistory(boolean isHistory) {
        this.mIsHistory = isHistory;
    }

    public boolean getIsHistory() {
        return this.mIsHistory;
    }


    @Override
    public String getBody() {
        return wikiName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(wikiName);
        dest.writeInt(mIsHistory ? 1 : 0);

    }

    public static final Creator<WikiSuggestion> CREATOR = new Creator<WikiSuggestion>() {
        @Override
        public WikiSuggestion createFromParcel(Parcel in) {
            return new WikiSuggestion(in);
        }

        @Override
        public WikiSuggestion[] newArray(int size) {
            return new WikiSuggestion[size];
        }
    };

}
