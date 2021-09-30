package tool.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HashListMap {
    Map<String, List<String>> map;

    public HashListMap() {
        map = new HashMap<>();
    }

    /**
     * Safely appends a value to the list corresponding to the given key.
     * 
     * @param key
     * @param value
     */
    public void addKeyPair(String key, String value) {
        var values = map.containsKey(key) ? map.get(key) : new ArrayList<String>();

        // Append value without duplication
        if (!values.contains(value)) {
            values.add(value);
            map.put(key, values);
        }
    }

    /**
     * Removes a key from the map.
     * 
     * @param key
     */
    public void remove(String key) {
        map.remove(key);
    }

    /**
     * Removes a value from the specified key in the map.
     * 
     * @param key
     */
    public void remove(String key, String value) {
        if (map.containsKey(key)) {
            var values = map.get(key);
            values.remove(value);
            map.put(key, values);
        }
    }
}
