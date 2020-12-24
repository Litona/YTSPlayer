package de.litona.ytsplayer;

import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainActivity extends AppCompatActivity {

    static File songsDirectory;
    static File songJsonFile;
    static List<SynchedSong> songs = new ArrayList<>(1500);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        songsDirectory = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        songJsonFile = new File(songsDirectory, "songs.json");
        // Deserializing Songs
        try (Stream<String> songsJsonStream = Files.lines(songJsonFile.toPath(), StandardCharsets.UTF_8)) {
            JSONArray array = new JSONObject(songsJsonStream.collect(Collectors.joining(" "))).getJSONArray("songs");
            for(int i = 0; i< array.length(); i++)
                songs.add(new SynchedSong(array.getJSONObject(i)));
        } catch (IOException | JSONException e) {
            System.out.println("No Songs File");
            e.printStackTrace();
        }
        songs = new CopyOnWriteArrayList<>(songs);


        ViewPager p = findViewById(R.id.viewPager);
        p.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            private String[] tabTitles = new String[]{"Tags", "Songlist", "Playlist"};

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
                        return new TagsFragment();
                    case 1:
                        return new SonglistFragment();
                    case 2:
                        return new TagsFragment();
                }
                return null;
            }

            @Override
            public int getCount() {
                return 3;
            }
        });
        ((TabLayout) findViewById(R.id.tabs)).setupWithViewPager(p);
    }
}