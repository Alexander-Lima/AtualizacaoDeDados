package com.controller.Utils;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Objects;

public class Utils {
    public static String sanitize(String str) {
        if(Objects.isNull(str)) return null;
        return str
                .replaceAll("\\.", "")
                .replaceAll("/", "")
                .replaceAll("-", "");
    }

    public static String getFieldByInnerHtml(String name, Elements data) {
        for(Element element : data) {
            String text = element.text().trim();
            if(!name.equals(text)) continue;

            Element next = element.nextElementSibling();
            if(Objects.nonNull(next)) {
                return next.text();
            }
        }
        return null;
    }
}
