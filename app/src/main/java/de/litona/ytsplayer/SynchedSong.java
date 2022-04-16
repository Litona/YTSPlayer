package de.litona.ytsplayer;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.collection.LruCache;

import com.google.android.exoplayer2.MediaItem;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

class SynchedSong extends PreSynchedSong implements Comparable<SynchedSong> {

	@SuppressLint("SimpleDateFormat") private static final SimpleDateFormat yearDisplayFormat = new SimpleDateFormat("yyyy");
	private static final LruCache<File, Bitmap> bitmapCache = new LruCache<File, Bitmap>((int) ((Runtime.getRuntime().maxMemory() / 1024) / 5)) {
		@Override
		protected int sizeOf(@NonNull File key, @NonNull Bitmap value) {
			return value.getByteCount() / 1024;
		}
	};

	private final File file;
	private final long added;
	private final long uploaded;
	private final String simpleTitle;
	private final String interpret;
	private final String year;
	private final boolean hasThumbnail;

	protected SynchedSong(String ytId, String ytTitle, Collection<String> tags, File file, long added, long uploaded, boolean hasThumbnail,
		String simpleTitle, String interpret, String year) {
		super(ytId, ytTitle, tags);
		this.file = file;
		this.added = added;
		this.uploaded = uploaded;
		this.hasThumbnail = hasThumbnail;
		this.simpleTitle = simpleTitle;
		this.interpret = interpret;
		this.year = year;
	}

	public SynchedSong(JSONObject json) throws JSONException {
		super(json.getString("ytId"),
			json.has("ytTitle") ? json.getString("ytTitle") : (json.has("nameCropped") ? json.getString("nameCropped") : cropName(json.getString("file"))),
			json.getJSONArray("tags"));
		file = new File(MainActivity.songsDirectory, json.getString("file"));
		added = json.getLong("added");
		uploaded = json.getLong("uploaded");
		hasThumbnail = json.getBoolean("hasThumbnail");
		simpleTitle = json.has("simpleTitle") ? json.getString("simpleTitle") : "";
		interpret = json.has("interpret") ? json.getString("interpret") : "";
		year = json.has("year") ? json.getString("year") : "";
	}

	public File getFile() {
		return file;
	}

	public long getAdded() {
		return added;
	}

	public long getUploaded() {
		return uploaded;
	}

	public boolean hasSimpleTitle() {
		return simpleTitle != null && !simpleTitle.isEmpty();
	}

	public String getSimpleTitle() {
		return hasSimpleTitle() ? simpleTitle : getYtTitle();
	}

	public String getInterpret() {
		return interpret == null ? "" : interpret;
	}

	public boolean hasYear() {
		return year != null && !year.isEmpty();
	}

	public String getYear() {
		return hasYear() ? year : (uploaded > 0 ? yearDisplayFormat.format(uploaded) : "");
	}

	@Override
	public int compareTo(SynchedSong o) {
		if(ytId.compareTo(o.ytId) == 0)
			return 0;
		else
			return Long.compare(added, o.added);
	}

	public BitmapWorkerTask setThumbnail(ImageView view) {
		if(hasThumbnail) {
			Bitmap t;
			if((t = bitmapCache.get(file)) != null)
				view.setImageBitmap(t);
			else {
				//noinspection deprecation
				BitmapWorkerTask task = new BitmapWorkerTask(view);
				task.execute(file);
				return task;
			}
		}
		return null;
	}

	public Bitmap getThumbnailSync() throws ExecutionException, InterruptedException {
		Bitmap t;
		if((t = bitmapCache.get(file)) != null)
			return t;
		else {
			//noinspection deprecation
			BitmapWorkerTask task = new BitmapWorkerTask(null);
			task.execute(file);
			return task.get();
		}
	}

	public MediaItem getMediaItem() {
		return MediaItem.fromUri(Uri.fromFile(getFile()));
	}

	@SuppressWarnings("deprecation") static class BitmapWorkerTask extends AsyncTask<File, Void, Bitmap> {

		private final WeakReference<ImageView> view;

		BitmapWorkerTask(ImageView view) {
			this.view = new WeakReference<>(view);
		}

		@Override
		protected Bitmap doInBackground(File... files) {
			Bitmap t = null;
			try {
				Mp3File mp3File = new Mp3File(files[0]);
				if(mp3File.hasId3v2Tag()) {
					byte[] imageBytes = mp3File.getId3v2Tag().getAlbumImage();
					if(imageBytes != null) {
						BitmapFactory.Options options = new BitmapFactory.Options();
						options.inJustDecodeBounds = true;
						BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, options);
						options.inJustDecodeBounds = false;
						if(options.outWidth > 0 && options.outHeight > 0) {
							options.inSampleSize = 1;
							while(options.outWidth / (options.inSampleSize + 1) > 112)
								options.inSampleSize = options.inSampleSize * 2;
							bitmapCache.put(files[0], t = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length));
						}
					}
				}
			} catch(IOException | InvalidDataException | UnsupportedTagException e) {
				e.printStackTrace();
			}
			return t;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			ImageView view = this.view.get();
			if(view != null && bitmap != null && !isCancelled())
				view.setImageBitmap(bitmap);
		}
	}

	private static String cropName(String path) {
		String fileName = new File(path).getName();
		System.out.println("Cropped name for " + fileName);
		return fileName.substring(14, fileName.length() - 16);
	}
}