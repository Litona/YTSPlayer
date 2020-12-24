package de.litona.ytsplayer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class SonglistFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_songlist, container, false);
        RecyclerView rv = view.findViewById(R.id.recyclerView);
        rv.setLayoutManager(new GridLayoutManager(getContext(), 1));
        rv.setAdapter(new RecyclerView.Adapter() {

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
                SynchedSong song = MainActivity.songs.get(i);
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
                if(t != null)
                    t.cancel(false);
                super.onViewRecycled(holder);
            }

            @Override
            public int getItemCount() {
                return MainActivity.songs.size();
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

        /*for (int i = 0; i < MainActivity.songs.size(); i++) {
            Fragment frag = SongFragment.newInstance(i);
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.layout, frag, "fragment" + i);
            fragmentTransaction.commit();
        }*/
        return view;
    }


}