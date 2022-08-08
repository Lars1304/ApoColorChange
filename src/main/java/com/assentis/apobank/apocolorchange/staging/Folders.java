package com.assentis.apobank.apocolorchange.staging;

import java.util.HashMap;

public class Folders {
    private static HashMap<String, Folder> folders = null;

    public Folders() {
        this.folders = new HashMap<String, Folder>();
    }

    public static void markChanged(String folderId) {
        folders.get(folderId).markChanged();
    }

    public void add(Folder folder){
        folders.put(folder.getDbKey(), folder);
    }

    public Folder get(String key) {
        return folders.get(key);
    }

    public String getPath(String key){
        Folder folder;
        StringBuilder sb = new StringBuilder();
        do {
            folder = get(key);
            sb.insert(0, "/" + folder.getName());
            key = folder.getParentId();
        } while (key != null);

        return sb.toString();
    }
}
