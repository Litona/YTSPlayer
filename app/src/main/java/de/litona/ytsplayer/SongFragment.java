package de.litona.ytsplayer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class SongFragment extends Fragment {

	private static final String SONG_ID = "songId";

	int id;
	SynchedSong song;

	public static SongFragment newInstance(int songId) {
		SongFragment fragment = new SongFragment();
		Bundle args = new Bundle();
		args.putInt(SONG_ID, songId);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(getArguments() != null)
			song = MainActivity.songs.get(id = getArguments().getInt(SONG_ID));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_song, container, false);
        /*((TextView) view.findViewById(R.id.interpretText)).setText(song.getInterpret());
        ((TextView) view.findViewById(R.id.titleText)).setText(song.getSimpleTitle());
        ((TextView) view.findViewById(R.id.indexText)).setText("" + (id + 1));
        ((TextView) view.findViewById(R.id.durationText)).setText("" + 0);
        ((TextView) view.findViewById(R.id.yearText)).setText(song.getYear());
        ((TextView) view.findViewById(R.id.tagsText)).setText(String.join(", ", song.getTags()));
        song.setThumbnail(((ImageView) view.findViewById(R.id.thumbnailView)));
        view.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(view.getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public void onLongPress(MotionEvent e) {
                    super.onLongPress(e);
                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    return super.onDoubleTap(e);
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });*/
		return view;
	}
}