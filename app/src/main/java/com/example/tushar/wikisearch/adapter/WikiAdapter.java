package com.example.tushar.wikisearch.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.tushar.wikisearch.R;
import com.example.tushar.wikisearch.data.WikiItem;

import java.util.List;

public class WikiAdapter extends RecyclerView.Adapter<WikiAdapter.WikiHolder> {

    public List<WikiItem> wikiItems;

    public Context context;

    private final OnItemClickListener clickListener;

    public WikiAdapter(List<WikiItem> wikiItems, OnItemClickListener clickListener, Context context) {

        this.wikiItems = wikiItems;

        this.clickListener = clickListener;

        this.context = context;
    }

    public interface OnItemClickListener {

        void onItemClick(WikiItem item);

    }

    @NonNull
    @Override
    public WikiHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.wiki_item_row, parent, false);

        return new WikiHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull WikiHolder holder, int position) {

        WikiItem wikiItem = wikiItems.get(position);

        holder.bind(wikiItem, clickListener);

    }

    @Override
    public int getItemCount() {

        return wikiItems.size();

    }

    public class WikiHolder extends RecyclerView.ViewHolder {

        public ImageView icon;
        public TextView title, description;

        public WikiHolder(View itemView) {

            super(itemView);

            icon = (ImageView) itemView.findViewById(R.id.icon);
            title = (TextView) itemView.findViewById(R.id.title);
            description = (TextView) itemView.findViewById(R.id.description);

        }

        public void bind(final WikiItem wikiItem, final OnItemClickListener listener) {

            title.setText(wikiItem.getTitle());
            description.setText(wikiItem.getDescription());
            Glide.with(context).load(wikiItem.getThumbnailUrl())
                    .thumbnail(0.5f)
                    .crossFade()
                    .placeholder(R.drawable.no_image_placeholder)
                    .into(icon);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(wikiItem);
                }
            });

        }

    }

}
