package de.litona.ytsplayer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class TagsFragment extends Fragment {

    TreeMap<String, TextView> tagMap = new TreeMap<String, TextView>(Comparator.comparing(String::toLowerCase));
    ViewGroup allTagsLayout;
    ViewGroup selectedTagsLayout;
    static List<SynchedSong> selectedSongs = MainActivity.songs;
    TextView infoTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        long timeA = System.currentTimeMillis();
        View view = inflater.inflate(R.layout.fragment_tags, container, false);
        allTagsLayout = view.findViewById(R.id.allTagsLayout);
        selectedTagsLayout = view.findViewById(R.id.selectedTagsLayout);
        infoTextView = view.findViewById(R.id.infoTextView);
        MainActivity.songs.stream().map(SynchedSong::getTags).flatMap(Collection::stream).distinct().sorted(Comparator.comparing(String::toLowerCase)).forEach(tag -> {
            TextView text = new TextView(view.getContext());
            text.setText(tag);
            text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            text.setOnTouchListener(new View.OnTouchListener() {
                private GestureDetector gestureDetector = new GestureDetector(view.getContext(), new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public void onLongPress(MotionEvent e) {
                        if (text.getText().charAt(0) == '✓')
                            text.setText(tag);
                        else if (text.getText().charAt(0) != '-')
                            text.setText("-" + tag);
                        updateLists();
                        super.onLongPress(e);
                    }

                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        if (text.getText().charAt(0) == '-')
                            text.setText(tag);
                        else if (text.getText().charAt(0) != '✓')
                            text.setText("✓" + tag);
                        updateLists();
                        return super.onDoubleTap(e);
                    }
                });

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    gestureDetector.onTouchEvent(event);
                    return true;
                }

                private void updateLists() {
                    selectedSongs = MainActivity.songs.stream().filter(song -> tagMap.values().stream().map(TextView::getText).allMatch(predTag -> predTag.charAt(0) == '-' ?
                            song.getTags().stream().noneMatch(s -> s.contentEquals(predTag.subSequence(1, predTag.length()))) :
                            (predTag.charAt(0) != '✓' || song.getTags().stream().anyMatch(s -> s.contentEquals(predTag.subSequence(1, predTag.length())))))).collect(Collectors.toList());
                    SonglistFragment.adapter.notifyDataSetChanged();
                    allTagsLayout.removeAllViews();
                    selectedTagsLayout.removeAllViews();
                    Collection<String> selectedTags = selectedSongs.stream().map(SynchedSong::getTags).flatMap(Collection::stream).collect(Collectors.toSet());
                    tagMap.forEach((tag, text) -> {
                        if (text.getText().charAt(0) == '-' || text.getText().charAt(0) == '✓')
                            selectedTagsLayout.addView(text);
                        else if (selectedTags.contains(tag))
                            allTagsLayout.addView(text);
                    });
                    infoTextView.setText("With above listed additionally available tags, the selection underneath found " + selectedSongs.size() + " songs");
                }
            });
            tagMap.put(tag, text);
            allTagsLayout.addView(text);
        });
        System.out.println("millis4tags" + (System.currentTimeMillis() - timeA));
        return view;
    }
}