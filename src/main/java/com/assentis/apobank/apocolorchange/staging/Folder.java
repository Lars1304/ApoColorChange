package com.assentis.apobank.apocolorchange.staging;

public class Folder {

    private String dbKey = null;
    private String name = null;
    private String parentId = null;
    private String schemaId = null;

    private boolean changed = false;

    public Folder(String dbKey, String name, String parentId, String schemaId) {
        this.dbKey = dbKey;
        this.name = name;
        setParentId(parentId);
        this.schemaId = schemaId;
    }

    public Folder() {

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

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        if (!"-1".equals(parentId))
            this.parentId = parentId;
    }

    public String getSchemaId() {
        return schemaId;
    }

    public void setSchemaId(String schemaId) {
        this.schemaId = schemaId;
    }

    public boolean isChanged() { return changed; }

    public void markChanged() {
        changed = true;
        if (parentId != null) {
            Folders.markChanged(parentId);
        }
        Schemas.markChanged(schemaId);
    }

}
