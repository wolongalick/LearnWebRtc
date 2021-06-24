/**
 *
 */
package com.alick.learnwebrtc.utils;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 功能: json解析类
 * 作者: 崔兴旺
 * 日期: 2019/4/9
 */
public class JsonUtils {
    private static Gson gson;

    static class DoubleDefault0Adapter implements JsonSerializer<Double>, JsonDeserializer<Double> {
        @Override
        public Double deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)throws JsonParseException {
            try{
                if(json.getAsString().equals("") || json.getAsString().equals("null")) {//定义为double类型,如果后台返回""或者null,则返回0.00
                    return 0.00;
                }
            }catch (Exception ignore) {
            }
            try{
                return json.getAsDouble();
            }catch (NumberFormatException e) {
                throw new JsonSyntaxException(e);
            }
        }

        @Override
        public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src);
        }
    }

    static class IntegerDefault0Adapter implements JsonSerializer<Integer>, JsonDeserializer<Integer> {
        @Override
        public Integer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext
                context)
                throws JsonParseException {
            try {
                if (json.getAsString().equals("") || json.getAsString().equals("null")) {//定义为int类型,如果后台返回""或者null,则返回0
                    return 0;
                }
            } catch (Exception ignore) {
            }
            try {
                return json.getAsInt();
            } catch (NumberFormatException e) {
                throw new JsonSyntaxException(e);
            }
        }

        @Override
        public JsonElement serialize(Integer src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src);
        }
    }


    static class LongDefault0Adapter implements JsonSerializer<Long>, JsonDeserializer<Long> {
        @Override
        public Long deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            try {
                if (json.getAsString().equals("") || json.getAsString().equals("null")) {//定义为long类型,如果后台返回""或者null,则返回0
                    return 0L;
                }
            } catch (Exception ignore) {
            }
            try {
                return json.getAsLong();
            } catch (NumberFormatException e) {
                throw new JsonSyntaxException(e);
            }
        }

        @Override
        public JsonElement serialize(Long src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src);
        }
    }

    public static Gson getGson() {
        if (gson == null) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.serializeNulls();
            TypeAdapter<String> stringTypeAdapter = new TypeAdapter<String>() {
                public String read(JsonReader reader) throws IOException {
                    if (reader.peek() == JsonToken.NULL) {
                        reader.nextNull();
                        return "";
                    }
                    return reader.nextString();
                }

                public void write(JsonWriter writer, String value) throws IOException {
                    if (value == null) {
                        // 在这里处理null改为空字符串
                        writer.value("");
                        return;
                    }
                    writer.value(value);
                }
            };
            //注册自定义String的适配器
            gsonBuilder.registerTypeAdapter(String.class, stringTypeAdapter)
                       .registerTypeAdapter(Double.class, new DoubleDefault0Adapter())
                       .registerTypeAdapter(double.class, new DoubleDefault0Adapter())
                       .registerTypeAdapter(Integer.class, new IntegerDefault0Adapter())
                       .registerTypeAdapter(int.class, new IntegerDefault0Adapter())
                       .registerTypeAdapter(Long.class, new LongDefault0Adapter())
                       .registerTypeAdapter(long.class, new LongDefault0Adapter())
            ;
            gson = gsonBuilder.create();
        }
        return gson;
    }

    public static Object parseMapIterabletoJSON(Object object) throws JSONException {
        if (object instanceof Map) {
            JSONObject json = new JSONObject();
            Map map  = (Map) object;
            for (Object key : map.keySet()) {
                json.put(key.toString(), parseMapIterabletoJSON(map.get(key)));
            }
            return json;
        } else if (object instanceof Iterable) {
            JSONArray json = new JSONArray();
            for (Object value : ((Iterable) object)) {
                json.put(value);
            }
            return json;
        } else {
            return object;
        }
    }

    public static String parseBean2json(Object obj) {
        return getGson().toJson(obj);
    }

    public static boolean isEmptyObject(JSONObject object) {
        return object.names() == null;
    }

    public static Map<String, Object> getMap(JSONObject object, String key) throws
            JSONException {
        return toMap(object.getJSONObject(key));
    }

    public static Map<String, Object> toMap(JSONObject object)
            throws JSONException {

        Map<String, Object> map  = new HashMap();
        Iterator keys = object.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            map.put(key, fromJson(object.get(key)));
        }
        return map;
    }

    public static Map<String, String> parseMap(JSONObject object) throws JSONException {
        Map<String, String> map  = new HashMap();
        Iterator keys = object.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            map.put(key, fromJson(object.get(key)).toString());
        }
        return map;
    }

    public static List toList(JSONArray array) throws JSONException {
        List list = new ArrayList();
        for (int i = 0; i < array.length(); i++) {
            list.add(fromJson(array.get(i)));
        }
        return list;
    }

    private static Object fromJson(Object json) throws JSONException {
        if (json == JSONObject.NULL) {
            return null;
        } else if (json instanceof JSONObject) {
            return toMap((JSONObject) json);
        } else if (json instanceof JSONArray) {
            return toList((JSONArray) json);
        } else {
            return json;
        }
    }

    public static <
            T> List<T> parseRootJson2List(String json, Class<T> clazz, String listKey) throws
            JSONException {
        return parseJson2List(new JSONObject(json).getJSONObject("data").getJSONArray(listKey).toString(), clazz);
    }

    public static <T> List<T> parseRootJson2List(String json, Class<T> clazz) throws
            JSONException {
        return parseJson2List(new JSONObject(json).getJSONArray("data").toString(), clazz);
    }

    public static <T> List<T> parseJson2List(String json, Class<T> clazz) throws
            JSONException {
        List<T> list = null;
        try {
            list = new ArrayList<>();
            if (TextUtils.isEmpty(json)) {
                return list;
            }

            JsonArray array = new JsonParser().parse(json).getAsJsonArray();
            for (JsonElement element : array) {
                list.add(getGson().fromJson(element, clazz));
            }
        } catch (JsonSyntaxException e) {
            throw new JSONException(e.getMessage());
        }
        return list;
    }

    public static <T> List<T> parseJson2List(String json, Type type) throws JSONException {
        try {
            List<T> list = new ArrayList<>();
            if (TextUtils.isEmpty(json)) {
                return list;
            }

            JsonArray array = new JsonParser().parse(json).getAsJsonArray();
            for (JsonElement element : array) {
                T t = getGson().fromJson(element, type);
                list.add(t);
            }
            return list;
        } catch (JsonSyntaxException e) {
            throw new JSONException(e.getMessage());
        }
    }


    public static <T> T parseJson2Bean(String json, Class<T> clazz) throws JSONException {
        try {
            return getGson().fromJson(json, clazz);
        } catch (JsonSyntaxException e) {
            throw new JSONException(e.getMessage());
        }
    }

    public static <T> T parseRootJson2Bean(String json, Class<T> clazz) throws JSONException {
        try {
            return getGson().fromJson(new JSONObject(json).getJSONObject("data").toString(), clazz);
        } catch (JsonSyntaxException | JSONException e) {
            throw new JSONException(e.getMessage());
        }
    }

    public static <T> T parseRootJson2Bean(String json, Class<T> clazz, String obj_key) throws
            JSONException {
        try {
            return getGson().fromJson(new JSONObject(json).getJSONObject("data").getJSONObject(obj_key).toString(), clazz);
        } catch (JsonSyntaxException | JSONException e) {
            throw new JSONException(e.getMessage());
        }
    }

    public static <T> T parseJson2Bean(String json, Type type) throws JSONException {
        try {
            return getGson().fromJson(json, type);
        } catch (JsonSyntaxException e) {
            throw new JSONException(e.getMessage());
        }
    }

    public static <W> String parseMap2String(Map<String, W> map) throws JSONException {
        try {
            return getGson().toJson(map);
        } catch (Exception e) {
            throw new JSONException(e.getMessage());
        }
    }

    public static <T> T parseMap2Bean(Map<String, ?> map, Class<T> clazz) throws
            JSONException {
        try {
            return getGson().fromJson(getGson().toJson(map), clazz);
        } catch (JsonSyntaxException e) {
            throw new JSONException(e.getMessage());
        }
    }

    /**
     * 用来兼容Android4.4以下版本的remove方法
     *
     * @param jsonArray
     * @param index
     * @return
     */
    public static JSONArray removeCompatibilityKITKAT(JSONArray jsonArray, int index) throws
            JSONException {
        JSONArray mJsonArray = null;
        try {
            mJsonArray = new JSONArray();
            if (index < 0)
                return mJsonArray;
            if (index > jsonArray.length())
                return mJsonArray;
            for (int i = 0; i < jsonArray.length(); i++) {
                if (i != index) {
                    mJsonArray.put(jsonArray.getJSONObject(i));
                }
            }
        } catch (JSONException e) {
            throw new JSONException(e.getMessage());
        }
        return mJsonArray;
    }
}
