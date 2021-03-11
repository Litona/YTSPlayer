package de.litona.ytsplayer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TagsFragment extends Fragment {

    TreeMap<String, TextView> tagMap = new TreeMap<String, TextView>(Comparator.comparing(String::toLowerCase));
    ViewGroup allTagsLayout;
    ViewGroup selectedTagsLayout;
    static List<SynchedSong> selectedSongs = MainActivity.songs;
    TextView infoTextView;
    EditText searchField;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tags, container, false);
        allTagsLayout = view.findViewById(R.id.allTagsLayout);
        selectedTagsLayout = view.findViewById(R.id.selectedTagsLayout);
        infoTextView = view.findViewById(R.id.infoTextView);
        searchField = view.findViewById(R.id.searchField);
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateLists();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        MainActivity.songs.stream().map(SynchedSong::getTags).flatMap(Collection::stream).distinct().sorted(Comparator.comparing(String::toLowerCase))
            .forEach(tag -> {
                TextView text = new TextView(view.getContext());
                text.setText(tag);
                text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                text.setPadding(text.getPaddingLeft(), text.getPaddingTop(), text.getPaddingTop(), 10);
                text.setOnTouchListener(new View.OnTouchListener() {
                    private GestureDetector gestureDetector = new GestureDetector(view.getContext(), new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public void onLongPress(MotionEvent e) {
                            if(text.getText().charAt(0) == '✓')
                                text.setText(tag);
                            else if(text.getText().charAt(0) != '-')
                                text.setText("-" + tag);
                            updateLists();
                            super.onLongPress(e);
                        }

                        @Override
                        public boolean onDoubleTap(MotionEvent e) {
                            if(text.getText().charAt(0) == '-')
                                text.setText(tag);
                            else if(text.getText().charAt(0) != '✓')
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
                });
                tagMap.put(tag, text);
                allTagsLayout.addView(text);
            });
        return view;
    }

    void updateLists() {
        selectedSongs = MainActivity.songs.stream().filter(song -> tagMap.values().stream().map(TextView::getText).allMatch(
            predTag -> predTag.charAt(0) == '-' ?
                song.getTags().stream().noneMatch(s -> s.contentEquals(predTag.subSequence(1, predTag.length()))) :
                (predTag.charAt(0) != '✓' || song.getTags().stream().anyMatch(s -> s.contentEquals(predTag.subSequence(1, predTag.length()))))) && (
            searchField.getText().toString().trim().isEmpty() || Stream.of(searchField.getText().toString().trim().toLowerCase().split("\\s+"))
                .allMatch((song.getInterpret() + " - " + song.getSimpleTitle()).toLowerCase()::contains))).collect(Collectors.toList());
        SonglistFragment.adapter.notifyDataSetChanged();
        allTagsLayout.removeAllViews();
        selectedTagsLayout.removeAllViews();
        Collection<String> selectedTags = selectedSongs.stream().map(SynchedSong::getTags).flatMap(Collection::stream).collect(Collectors.toSet());
        tagMap.forEach((tag, text) -> {
            if(text.getText().charAt(0) == '-' || text.getText().charAt(0) == '✓')
                selectedTagsLayout.addView(text);
            else if(selectedTags.contains(tag))
                allTagsLayout.addView(text);
        });
        infoTextView.setText("With above listed additionally available tags, the selection underneath found " + selectedSongs.size() + " songs");
        MainActivity.songlist.showButtons();
    }
}