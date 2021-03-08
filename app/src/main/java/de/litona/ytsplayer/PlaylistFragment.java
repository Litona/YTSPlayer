package de.litona.ytsplayer;

import android.app.PendingIntent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class PlaylistFragment extends Fragment implements Player.EventListener, PlayerNotificationManager.MediaDescriptionAdapter {

    private static class Entry {

        private final MediaItem mediaItem;
        private final SynchedSong synchedSong;

        Entry(MediaItem mediaItem, SynchedSong synchedSong) {
            this.mediaItem = mediaItem;
            this.synchedSong = synchedSong;
        }

        Entry(SynchedSong synchedSong) {
            this(MediaItem.fromUri(Uri.fromFile(synchedSong.getFile())), synchedSong);
        }
    }

    private SimpleExoPlayer player;
    private final List<PlaylistFragment.Entry> playlist = new ArrayList<>();
    private int playing;

    static RecyclerView.Adapter adapter;


    public SimpleExoPlayer getPlayer() {
        return player;
    }

    public SynchedSong getPlaying() {
        return playing < playlist.size() ? playlist.get(playing).synchedSong : null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        player = new SimpleExoPlayer.Builder(getContext()).setSkipSilenceEnabled(true).build();
        MainActivity.playerControlView.setPlayer(player);
        player.addListener(this);
        player.prepare();
        PlayerNotificationManager playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(getContext(), "player_controls", R.string.player_controls_notification, 1, this);
        playerNotificationManager.setUseChronometer(true);
        playerNotificationManager.setUseNextActionInCompactView(true);
        playerNotificationManager.setUsePreviousActionInCompactView(true);
        playerNotificationManager.setFastForwardIncrementMs(0);
        playerNotificationManager.setRewindIncrementMs(0);
        playerNotificationManager.setPlayer(player);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);
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
                SynchedSong song = playlist.get(i).synchedSong;
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
                if (t != null)
                    t.cancel(false);
                super.onViewRecycled(holder);
            }

            @Override
            public int getItemCount() {
                return playlist.size();
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
        return view;
    }

    public void appendShuffled(List<SynchedSong> songs) {
        List<Entry> toShuffle = songs.stream().map(Entry::new).collect(Collectors.toList());
        Collections.shuffle(toShuffle);
        toShuffle.forEach(e -> {
            player.addMediaItem(e.mediaItem);
            playlist.add(e);
        });
        adapter.notifyDataSetChanged();
        System.out.println(player.getPlaybackState());
    }

    public void appendNext(SynchedSong song) {
        Entry e = new Entry(song);
        player.addMediaItem(playing + 1, e.mediaItem);
        playlist.add(playing + 1, e);
        adapter.notifyDataSetChanged();
    }

    public void remove(int index) {

    }

    public void jumpTo(int index) {

    }

    // Method for Player.EventListener

    @Override
    public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
        if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO || reason == Player.MEDIA_ITEM_TRANSITION_REASON_SEEK) {
            if (playlist.get(playing).mediaItem == mediaItem)
                System.out.println("Pls optimize! EventReason: " + reason);
            else if (mediaItem == null)
                System.out.println("Null MediaItem; Is this the end?");
            else if (playlist.get(playing + 1).mediaItem == mediaItem)
                playing++;
            else if (playlist.get(playing - 1).mediaItem == mediaItem)
                playing--;
            else {
                for (int i = 0; i < playlist.size(); i++)
                    if (playlist.get(i).mediaItem == mediaItem) {
                        playing = i;
                        return;
                    }
                System.out.println("ERROR: Could not find media! Media unsynched!");
            }
        }
    }


    // Methods for MediaDescriptionAdapter:

    @Override
    public CharSequence getCurrentContentTitle(Player player) {
        SynchedSong song = getPlaying();
        String out = song == null ? "No song" : song.getInterpret() + " - " + song.getSimpleTitle();
        MainActivity.songTitleView.setText(out); // Updating Title in App here!
        return out;
    }

    @Nullable
    @Override
    public PendingIntent createCurrentContentIntent(Player player) {
        return null;
    }

    @Nullable
    @Override
    public CharSequence getCurrentContentText(Player player) {
        SynchedSong song = getPlaying();
        return song == null ? null : String.join(", ", song.getTags());
    }

    @Nullable
    @Override
    public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
        try {
            SynchedSong song = getPlaying();
            return song == null ? null : song.getThumbnailSync();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}