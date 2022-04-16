package de.litona.ytsplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class Playlist extends Service
	implements Player.EventListener, PlayerNotificationManager.MediaDescriptionAdapter, PlayerNotificationManager.NotificationListener {

	private static class Entry {

		private final MediaItem mediaItem;
		private final SynchedSong synchedSong;

		Entry(MediaItem mediaItem, SynchedSong synchedSong) {
			this.mediaItem = mediaItem;
			this.synchedSong = synchedSong;
		}

		Entry(SynchedSong synchedSong) {
			this(synchedSong.getMediaItem(), synchedSong);
		}
	}

	private SimpleExoPlayer player;
	private PlayerNotificationManager playerNotificationManager;
	private final List<Entry> playlist = new ArrayList<>();
	private int playing;

	@Override
	public void onCreate() {
		super.onCreate();
		MainActivity.playlist = this;
		player = new SimpleExoPlayer.Builder(this).build();
		MainActivity.playerControlView.setPlayer(player);
		player.addListener(this);
		player.prepare();
		playerNotificationManager = PlayerNotificationManager
			.createWithNotificationChannel(this, "player_controls", R.string.player_controls_notification, 1, this, this);
		playerNotificationManager.setUseChronometer(true);
		playerNotificationManager.setUseNextActionInCompactView(true);
		playerNotificationManager.setUsePreviousActionInCompactView(true);
		playerNotificationManager.setFastForwardIncrementMs(0);
		playerNotificationManager.setRewindIncrementMs(0);
		playerNotificationManager.setPlayer(player);
	}

	@Override
	public void onDestroy() {
		playerNotificationManager.setPlayer(null);
		player.release();
		player = null;
		super.onDestroy();
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public SimpleExoPlayer getPlayer() {
		return player;
	}

	public SynchedSong getPlaying() {
		return playing < playlist.size() ? playlist.get(playing).synchedSong : null;
	}

	public int getPlayingIndex() {
		return playing;
	}

	public int getSize() {
		return playlist.size();
	}

	public SynchedSong get(int index) {
		return playlist.get(index).synchedSong;
	}

	public void appendShuffled(List<SynchedSong> songs) {
		List<Entry> toShuffle = songs.stream().map(Entry::new).collect(Collectors.toList());
		Collections.shuffle(toShuffle);
		toShuffle.forEach(e -> {
			player.addMediaItem(e.mediaItem);
			playlist.add(e);
		});
		MainActivity.playlistFragment.adapter.notifyDataSetChanged();
	}

	public void shuffleIn(List<SynchedSong> songs) {
		if(playing + 1 < playlist.size()) {
			Random random = new Random();
			songs.forEach(s -> addAtIndex(s, playing + 2 + random.nextInt(playlist.size() - playing - 1), false));
			MainActivity.playlistFragment.adapter.notifyDataSetChanged();
		} else
			appendShuffled(songs);
	}

	public void addAtIndex(SynchedSong song, int index, boolean notify) {
		if(index <= playing) // pointless..
			return;
		Entry e = new Entry(song);
		player.addMediaItem(index, e.mediaItem);
		playlist.add(index, e);
		if(notify)
			MainActivity.playlistFragment.adapter.notifyDataSetChanged();
	}

	public void appendNext(SynchedSong song) {
		addAtIndex(song, playing + 1, true);
	}

	public void clearAndShuffleNew(List<SynchedSong> songs) {
		player.clearMediaItems();
		playlist.clear();
		playing = 0;
		appendShuffled(songs);
	}

	public void remove(int index, boolean notify) {
		if(index <= playing) // pointless..
			return;
		player.removeMediaItem(index);
		playlist.remove(index);
		if(notify)
			MainActivity.playlistFragment.adapter.notifyDataSetChanged();
	}

	public void remove(List<SynchedSong> songs) {
		int index = playing + 1;
		while(index < getSize())
			if(songs.contains(get(index)))
				remove(index, false);
			else
				index++;
		MainActivity.playlistFragment.adapter.notifyDataSetChanged();
	}

	public void jumpTo(int index) {
		while(playing != index)
			if(playing > index)
				player.previous();
			else
				player.next();
	}

	// Method for Player.EventListener

	@Override
	public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
		if(reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO || reason == Player.MEDIA_ITEM_TRANSITION_REASON_SEEK) {
			if(playlist.get(playing).mediaItem == mediaItem)
				System.out.println("Pls optimize! EventReason: " + reason);
			else if(mediaItem == null)
				System.out.println("Null MediaItem; Is this the end?");
			else if(playlist.get(playing + 1).mediaItem == mediaItem)
				MainActivity.playlistFragment.adapter.notifyItemChanged(playing++);
			else if(playlist.get(playing - 1).mediaItem == mediaItem)
				MainActivity.playlistFragment.adapter.notifyItemChanged(--playing);
			else {
				for(int i = 0; i < playlist.size(); i++)
					if(playlist.get(i).mediaItem == mediaItem) {
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
		} catch(ExecutionException | InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	// Methods for NotificationListener:

	@Override
	public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
		startForeground(notificationId, notification);
	}

	@Override
	public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
		stopSelf();
	}
}