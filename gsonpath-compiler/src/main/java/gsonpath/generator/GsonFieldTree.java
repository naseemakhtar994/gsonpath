package gsonpath.generator;

import java.util.LinkedHashMap;

public class GsonFieldTree {
    private final LinkedHashMap<String, Object> fieldMap;

    public GsonFieldTree() {
        fieldMap = new LinkedHashMap<>();
    }

    public void addTreeBranch(String branchName, GsonFieldTree gsonFieldTree) {
        fieldMap.put(branchName, gsonFieldTree);
    }

    public void addField(String branchName, FieldPathInfo field) throws IllegalArgumentException {
        if (containsKey(branchName)) {
            throw new IllegalArgumentException("Value already exists");
        }
        fieldMap.put(branchName, field);
    }

    public int size() {
        return fieldMap.size();
    }

    public java.util.Set<String> keySet() {
        return fieldMap.keySet();
    }

    public Object get(String key) {
        return fieldMap.get(key);
    }

    public boolean containsKey(String key) {
        return fieldMap.containsKey(key);
    }
}
