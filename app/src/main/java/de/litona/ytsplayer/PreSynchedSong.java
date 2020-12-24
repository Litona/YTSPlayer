package de.litona.ytsplayer;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeSet;

public class PreSynchedSong {

    protected final String ytId;
    protected String ytTitle;
    protected Collection<String> tags = new TreeSet<>(Comparator.comparing(String::toLowerCase));

    protected PreSynchedSong(String ytId, String ytTitle, Collection<String> tags) {
        this.ytId = ytId;
        this.ytTitle = ytTitle;
        this.tags.addAll(tags);
    }

    protected PreSynchedSong(String ytId, String ytTitle, JSONArray tags) throws JSONException {
        this.ytId = ytId;
        this.ytTitle = ytTitle;
        for (int i = 0; i < tags.length(); i++)
            this.tags.add(tags.getString(i));
    }

    public String getYtId() {
        return ytId;
    }

    public String getYtTitle() {
        return ytTitle;
    }

    public Collection<String> getTags() {
        return Collections.unmodifiableCollection(tags);
    }

    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof PreSynchedSong && ytId.equals(((PreSynchedSong) o).ytId);
    }
}
