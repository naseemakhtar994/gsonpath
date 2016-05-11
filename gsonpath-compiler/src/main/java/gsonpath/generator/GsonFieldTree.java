package gsonpath.generator;

import java.util.LinkedHashMap;

public class GsonFieldTree {
    LinkedHashMap<String, Object> fieldMap;

    public GsonFieldTree() {
        fieldMap = new LinkedHashMap<>();
    }

    public void addTreeBranch(String branchName, GsonFieldTree gsonFieldTree) {
        fieldMap.put(branchName, gsonFieldTree);
    }

    public void addField(String branchName, FieldInfo field) throws IllegalArgumentException {
        if (fieldMap.containsKey(branchName)) {
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

}
