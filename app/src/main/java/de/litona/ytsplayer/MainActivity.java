package de.litona.ytsplayer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.material.tabs.TabLayout;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLException;
import com.yausername.youtubedl_android.YoutubeDLRequest;
import com.yausername.youtubedl_android.mapper.VideoInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainActivity extends AppCompatActivity {

	static File songsDirectory;
	static File songJsonFile;
	static List<SynchedSong> songs = Collections.emptyList();
	static TagsFragment tags = new TagsFragment();
	static SonglistFragment songlist = new SonglistFragment();
	static PlaylistFragment playlistFragment = new PlaylistFragment();
	static PlayerControlView playerControlView;
	static TextView songTitleView;
	static Playlist playlist;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		songsDirectory = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
		songJsonFile = new File(songsDirectory, "songs.json");
		// Deserializing Songs
		try(Stream<String> songsJsonStream = Files.lines(songJsonFile.toPath(), StandardCharsets.UTF_8)) {
			JSONArray array = new JSONObject(songsJsonStream.collect(Collectors.joining(" "))).getJSONArray("songs");
			songs = new ArrayList<>(array.length());
			for(int i = 0; i < array.length(); i++)
				songs.add(new SynchedSong(array.getJSONObject(i)));
		} catch(IOException | JSONException e) {
			System.out.println("No Songs File");
			e.printStackTrace();
		}
		songs = new CopyOnWriteArrayList<>(songs);

		startService(new Intent(this, Playlist.class));

		ViewPager p = findViewById(R.id.viewPager);
		p.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
			private String[] tabTitles = new String[] {"Tags", "Songlist", "Playlist"};

			@Nullable
			@Override
			public CharSequence getPageTitle(int position) {
				return tabTitles[position];
			}

			@NonNull
			@Override
			public Fragment getItem(int position) {
				switch(position) {
					case 0:
						return tags;
					case 1:
						return songlist;
					case 2:
						return playlistFragment;
				}
				return null;
			}

			@Override
			public int getCount() {
				return 3;
			}
		});
		((TabLayout) findViewById(R.id.tabs)).setupWithViewPager(p);

		playerControlView = findViewById(R.id.playerControlView1);
		songTitleView = findViewById(R.id.songTitleView);
		findViewById(R.id.endAppButton).setOnClickListener(v -> {
			playlist.stopSelf();
			finish();
		});
	}

	@SuppressWarnings({"deprecation", "StaticFieldLeak"})
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if(intent.getAction().equals(Intent.ACTION_SEND) && "text/plain".equals(intent.getType())) {
			String ytId = intent.getStringExtra(Intent.EXTRA_TEXT).replace("https://youtu.be/", "");
			new AsyncTask<String, Void, TempSynchedSong>() {

				@Override
				protected TempSynchedSong doInBackground(String... strings) {
					try {
						YoutubeDL.getInstance().init(MainActivity.playlist.getApplication());
						YoutubeDL.getInstance().updateYoutubeDL(MainActivity.playlist.getApplication());
					} catch(YoutubeDLException e) {
						System.out.println("failed to initialize ytdl");
						return null;
					}
					YoutubeDLRequest request = new YoutubeDLRequest("https://youtu.be/" + strings[0]);
					request.addOption("-f", "best");
					try {
						VideoInfo videoInfo = YoutubeDL.getInstance().getInfo(request);
						return new TempSynchedSong(strings[0], videoInfo.getTitle(), videoInfo.getUploadDate(),
							new MediaItem.Builder().setUri(videoInfo.getUrl()).setMimeType(MimeTypes.APPLICATION_MP4).build());
					} catch(YoutubeDLException | InterruptedException | ParseException e) {
						e.printStackTrace();
					}
					return null;
				}

				@Override
				protected void onPostExecute(TempSynchedSong tempSynchedSong) {
					if(tempSynchedSong != null)
						MainActivity.playlist.appendNext(tempSynchedSong);
				}
			}.execute(ytId);
		}
	}
}