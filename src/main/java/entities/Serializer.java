package entities;

import com.google.gson.Gson;

public abstract class Serializer {
    public static String serialize(Object o) {
        return new Gson().toJson(o);
    }

    public static <T> T deserialize(String json, Class<T> cl) {
        return new Gson().fromJson(json, cl);
    }
}
