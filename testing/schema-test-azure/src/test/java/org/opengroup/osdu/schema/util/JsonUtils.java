package org.opengroup.osdu.schema.util;

import com.google.gson.Gson;

import io.restassured.path.json.JsonPath;

public class JsonUtils {
    public static String toJson(Object src) {
        return new Gson().toJson(src);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return new Gson().fromJson(json, classOfT);
    }

    public static JsonPath getAsJsonPath(String src) {
        return JsonPath.with(src);
    }
}
