package de.litona.ytsplayer;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Map;
import java.util.WeakHashMap;

public class PlaylistFragment extends Fragment {

	static RecyclerView.Adapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
				SynchedSong song = MainActivity.playlist.get(i);
				tasks.put(vh, song.setThumbnail(vh.thumbnailV));
				vh.interpretV.setText(song.getInterpret());
				vh.titleV.setText(song.getSimpleTitle());
				vh.indexV.setText("" + i);
				vh.durationV.setText("" + 0);
				vh.yearV.setText(song.getYear());
				vh.tagsV.setText(String.join(", ", song.getTags()));
				if(i < MainActivity.playlist.getPlayingIndex())
					vh.background.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
				else
					vh.background.setBackgroundColor(getResources().getColor(android.R.color.black));
				vh.itemView.setOnTouchListener(new View.OnTouchListener() {
					private GestureDetector gestureDetector = new GestureDetector(view.getContext(), new GestureDetector.SimpleOnGestureListener() {
						@Override
						public boolean onSingleTapConfirmed(MotionEvent e) {
							MainActivity.playlist.jumpTo(i);
							return super.onSingleTapConfirmed(e);
						}

						@Override
						public void onLongPress(MotionEvent e) {
							MainActivity.playlist.remove(i, true);
							super.onLongPress(e);
						}

						@Override
						public boolean onDoubleTap(MotionEvent e) {
							MainActivity.playlist.appendNext(MainActivity.playlist.get(i));
							return super.onDoubleTap(e);
						}
					});

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						gestureDetector.onTouchEvent(event);
						return true;
					}
				});
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
				System.out.println(MainActivity.playlist);
				return MainActivity.playlist.getSize();
			}

			class VH extends RecyclerView.ViewHolder {

				ImageView thumbnailV;
				TextView interpretV;
				TextView titleV;
				TextView indexV;
				TextView durationV;
				TextView yearV;
				TextView tagsV;
				View background;

				public VH(@NonNull View itemView) {
					super(itemView);
					thumbnailV = itemView.findViewById(R.id.thumbnailView);
					interpretV = itemView.findViewById(R.id.interpretText);
					titleV = itemView.findViewById(R.id.titleText);
					indexV = itemView.findViewById(R.id.indexText);
					durationV = itemView.findViewById(R.id.durationText);
					yearV = itemView.findViewById(R.id.yearText);
					tagsV = itemView.findViewById(R.id.tagsText);
					background = itemView.findViewById(R.id.background);
				}
			}
		});
		return view;
	}
}