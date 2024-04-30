package com.controller.Classes;

import java.util.HashMap;
import java.util.Map;

public class FormDocType {
    private static final Map<Integer, String> map = new HashMap<>();
    static {
        map.put(9, "1");
        map.put(14, "2");
        map.put(11, "3");
    }
    public static String geDocType(int size) {
        return map.getOrDefault(size, "14");
    }
}
