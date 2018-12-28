package zwz.core.db;

public class BaseRecord {
    public final static String ID_NAME="base_id";
    public final static String ID_METHOD_NAME="setBaseId";
    private long baseId;

    public long getBaseId() {
        return baseId;
    }

    public void setBaseId(long id) {
        this.baseId = id;
    }
}
