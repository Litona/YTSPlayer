package de.litona.ytsplayer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Map;
import java.util.WeakHashMap;

public class SonglistFragment extends Fragment {

    static RecyclerView.Adapter adapter;
    FloatingActionButton appendShuffledButton, clearAndShuffleButton, shuffleInButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_songlist, container, false);
        RecyclerView rv = view.findViewById(R.id.recyclerView);
        rv.setLayoutManager(new GridLayoutManager(getContext(), 1));
        rv.setAdapter(adapter = new RecyclerView.Adapter() {

            Map<RecyclerView.ViewHolder, SynchedSong.BitmapWorkerTask> tasks = new WeakHashMap<>();

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_song, parent, false);
                return new VH(v);
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int i) {
                VH vh = (VH) holder;
                SynchedSong song = TagsFragment.selectedSongs.get(i);
                tasks.put(vh, song.setThumbnail(vh.thumbnailV));
                vh.interpretV.setText(song.getInterpret());
                vh.titleV.setText(song.getSimpleTitle());
                vh.indexV.setText("" + i);
                vh.durationV.setText("" + 0);
                vh.yearV.setText(song.getYear());
                vh.tagsV.setText(String.join(", ", song.getTags()));
            }

            @Override
            public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
                SynchedSong.BitmapWorkerTask t = tasks.get(holder);
                if(t != null)
                    t.cancel(false);
                super.onViewRecycled(holder);
            }

            @Override
            public int getItemCount() {
                return TagsFragment.selectedSongs.size();
            }

            class VH extends RecyclerView.ViewHolder {

                ImageView thumbnailV;
                TextView interpretV;
                TextView titleV;
                TextView indexV;
                TextView durationV;
                TextView yearV;
                TextView tagsV;

                public VH(@NonNull View itemView) {
                    super(itemView);
                    thumbnailV = itemView.findViewById(R.id.thumbnailView);
                    interpretV = itemView.findViewById(R.id.interpretText);
                    titleV = itemView.findViewById(R.id.titleText);
                    indexV = itemView.findViewById(R.id.indexText);
                    durationV = itemView.findViewById(R.id.durationText);
                    yearV = itemView.findViewById(R.id.yearText);
                    tagsV = itemView.findViewById(R.id.tagsText);
                }
            }
        });
        appendShuffledButton = view.findViewById(R.id.appendShuffledButton);
        appendShuffledButton.setOnClickListener(v -> {
            MainActivity.playlist.appendShuffled(TagsFragment.selectedSongs);
            hideButtons();
        });
        shuffleInButton = view.findViewById(R.id.shuffleInButton);
        shuffleInButton.setOnClickListener(v -> {
            MainActivity.playlist.shuffleIn(TagsFragment.selectedSongs);
            hideButtons();
        });
        clearAndShuffleButton = view.findViewById(R.id.clearAndShuffleButton);
        clearAndShuffleButton.setOnClickListener(v -> {
            MainActivity.playlist.clearAndShuffleNew(TagsFragment.selectedSongs);
            hideButtons();
        });
        return view;
    }

    public void showButtons() {
        if (appendShuffledButton != null)
            appendShuffledButton.show();
        if (shuffleInButton != null)
            shuffleInButton.show();
        if (clearAndShuffleButton != null)
            clearAndShuffleButton.show();
    }

    public void hideButtons() {
        if (appendShuffledButton != null)
            appendShuffledButton.hide();
        if (shuffleInButton != null)
            shuffleInButton.hide();
        if (clearAndShuffleButton != null)
            clearAndShuffleButton.hide();
    }
}