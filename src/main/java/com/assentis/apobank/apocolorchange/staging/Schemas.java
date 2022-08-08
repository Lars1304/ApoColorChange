package com.assentis.apobank.apocolorchange.staging;

import java.util.HashMap;

public class Schemas {
    private static HashMap<String, Schema> schemas = null;

    public Schemas() {

        schemas = new HashMap<String, Schema>();
    }

    public static void markChanged(String schemaId) {
        schemas.get(schemaId).markChanged();
    }

    public void add(Schema schema) {
        schemas.put(schema.getDbKey(), schema);
    }

    public Schema get(String key) {

        return schemas.get(key);
    }

}
