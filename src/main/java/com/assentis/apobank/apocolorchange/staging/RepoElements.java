package com.assentis.apobank.apocolorchange.staging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RepoElements {
    private HashMap<String, RepoElement> elements = null;

    public RepoElements() {

        this.elements = new HashMap<String, RepoElement>();
    }

    public void add(RepoElement element) {
        elements.put(element.getDbKey(), element);
    }

    public RepoElement get(String key) {

        return elements.get(key);
    }

    public List<RepoElement> getAll(){
        return new ArrayList<RepoElement>(elements.values());
    }

}
