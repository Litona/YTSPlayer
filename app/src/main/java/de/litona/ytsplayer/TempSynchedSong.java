package de.litona.ytsplayer;

import android.graphics.Bitmap;

import com.google.android.exoplayer2.MediaItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;

public class TempSynchedSong extends SynchedSong {

	private static final SimpleDateFormat ytUploadDateFormat = new SimpleDateFormat("yyyyMMdd");

	private final MediaItem mediaItem;

	public TempSynchedSong(String ytId, String ytTitle, String ytUploaded, MediaItem mediaItem) throws ParseException {
		super(ytId, ytTitle, Collections.emptyList(), null, System.currentTimeMillis(), ytUploadDateFormat.parse(ytUploaded).getTime(), false, ytTitle, "",
			ytUploaded.substring(0, 4));
		this.mediaItem = mediaItem;
	}

	@Override
	public MediaItem getMediaItem() {
		return mediaItem;
	}

	@Override
	public Bitmap getThumbnailSync() {
		return null;
	}
}
