package Configuration;

import java.util.ArrayList;

/**
 * Created by Robin on 2016-05-05.
 */
public class PatchNotes {
    private String version;
    private String name;
    private String date;
    private ArrayList<PatchChange> changes;

    public String getVersion() {
        return version;
    }

    protected void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    protected void setDate(String date) {
        this.date = date;
    }

    public ArrayList<PatchChange> getChanges() {
        return changes;
    }

    public void setChanges(ArrayList<PatchChange> changes) {
        this.changes = changes;
    }
}