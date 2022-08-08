package com.assentis.apobank.apocolorchange.staging;

public class Schema {

    private String dbKey = null;
    private String name = null;

    private boolean changed = false;

    public Schema() {
    }

    public Schema(String dbKey, String name) {
        this.dbKey = dbKey;
        this.name = name;
    }

    public String getDbKey() {
        return dbKey;
    }

    public void setDbKey(String dbKey) {
        this.dbKey = dbKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isChanged() { return changed; }

    public void markChanged() {
        changed = true;
    }
}
