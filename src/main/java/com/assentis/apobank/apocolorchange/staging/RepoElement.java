package com.assentis.apobank.apocolorchange.staging;

public class RepoElement {

    private String dbKey = null;
    private String name = null;
    private String elementId = null;
    private String elementPath = null;
    private String elementType = null;
    private String folderId = null;
    private boolean changed = false;

    public RepoElement(String dbKey, String name, String elementId, String elementPath, String elementType, String folderId) {
        this.dbKey = dbKey;
        this.name = name;
        this.elementId = elementId;
        this.elementPath = elementPath;
        this.elementType = elementType;
        this.folderId = folderId;
    }

    public RepoElement() {

    }

    public String getElementPath() {
        return elementPath;
    }

    public void setElementPath(String elementPath) {
        this.elementPath = elementPath;
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

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public String getElementType() {
        return elementType;
    }

    public void setElementType(String elementType) {
        this.elementType = elementType;
    }

    public String getFolderId() {
        return folderId;
    }

    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    public String getFilename () {
        return getElementId() + "." + getElementType();
    }

    public boolean isChanged() { return changed; }

    public void markChanged() {
        changed = true;
        Folders.markChanged(folderId);
    }
}
