package br.com.luansilveira.todolist.utils;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * <p>Classe utilizada para encapsular as classes JSONObject e JSONArray em um único objeto para facilitar a operação com webservices.</p>
 * <p>Dependendo do tipo de JSON retornado pela requisição, a classe pode encapsular um objeto JSON ({@link JSONObject}) ou um array JSON ({@link JSONArray}).</p>
 * <p>Esta classe também possui funcionalidades para converter objetos para o formato JSON.</p>
 *
 * @author Luan Christian Nascimento da Silveira
 */
public class JSON {

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private final String excecaoJSONArray = "JSONArray não pode ser convertido para JSONObject";
    private final String excecaoJSONObject = "JSONObject não pode ser convertido para JSONArray";
    private JSONObject jsonObject;
    private JSONArray jsonArray;
    private boolean mArray = false;

    public JSON(String s) throws JSONException {
        s = s.trim();
        this.mArray = (s.startsWith("["));
        if (mArray) {
            this.jsonArray = new JSONArray(s);
        } else {
            this.jsonObject = new JSONObject(s);
        }
    }

    public JSON(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public JSON(JSONArray jsonArray) {
        this.jsonArray = jsonArray;
        this.mArray = true;
    }

    public JSON(Map<?, ?> map) {
        this.jsonObject = new JSONObject(map);
    }

    public static <T> List<T> asList(JSONArray array, Class<T> cls) {
        if (array == null) return null;

        List<T> lista = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            try {
                Object obj = array.get(i);
                if (obj.getClass() == cls) {
                    lista.add(cls.cast(obj));
                } else return null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return lista;
    }

    /**
     * Função utilizada para converter um objeto para o formato JSON.
     * A função efetua o parsing de todos os atributos do objeto (que não estejam marcados com  {@link JSONIgnore}) e gera um JSONObject.
     *
     * @param obj Objeto a ser convertido.
     * @return JSONObject
     * @throws JSONException
     */
    public static JSONObject parseObj(Object obj) throws JSONException {
        if (obj == null) return null;
        Class cls = obj.getClass();
        JSONObject json = new JSONObject();

        for (Field campo : cls.getDeclaredFields()) {
            boolean isAcessivel = campo.isAccessible();
            if (!isAcessivel) campo.setAccessible(true);

            if (!campo.isAnnotationPresent(JSONIgnore.class) && !isPublicStaticFinal(campo)) {
                try {
                    String name = campo.isAnnotationPresent(JSONFieldName.class)
                            ? campo.getAnnotation(JSONFieldName.class).value()
                            : campo.getName();
                    Object value = campo.get(obj);
                    if (value != null) {
                        if (value instanceof Date) value = dateFormat.format(value);
                        if (isScalar(value)) {
                            json.put(name, value);
                        } else if (value.getClass().isEnum()) {
                            json.put(name, value.toString());
                        } else if (value instanceof Collection) {
                            json.put(name, JSON.parseArray((Collection<?>) value));
                        } else if (value.getClass().isArray()) {
                            json.put(name, JSON.parseArray(value));
                        } else {
                            json.put(campo.getName(), JSON.parseObj(value));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (!isAcessivel) campo.setAccessible(false);
            }
        }

        return json;
    }

    public static JSONArray parseArray(Object[] array) throws JSONException {
        return JSON.parseArray((Object) array);
    }

    private static JSONArray parseArray(Object array) throws JSONException {
        JSONArray jsonArray = new JSONArray();

        for (int i = 0; i < Array.getLength(array); i++) {
            Object item = Array.get(array, i);
            if (item instanceof Date) item = dateFormat.format(item);
            if (isScalar(item)) {
                jsonArray.put(item);
            } else if (item.getClass().isArray()) {
                jsonArray.put(JSON.parseArray(item));
            } else if (item instanceof Collection) {
                jsonArray.put(JSON.parseArray((Collection<?>) item));
            } else {
                jsonArray.put(JSON.parseObj(item));
            }
        }

        return jsonArray;
    }

    public static JSONArray parseArray(Collection<?> collection) throws JSONException {
        JSONArray jsonArray = new JSONArray();

        for (Object item : collection) {
            if (item instanceof Date) item = dateFormat.format(item);
            if (isScalar(item)) {
                jsonArray.put(item);
            } else if (item instanceof Collection) {
                jsonArray.put(JSON.parseArray((Collection<?>) item));
            } else if (item.getClass().isArray()) {
                jsonArray.put(JSON.parseArray(item));
            } else {
                jsonArray.put(JSON.parseObj(item));
            }
        }

        return jsonArray;
    }

    @SuppressWarnings("unchecked")
    public static <T> T parseJsonToObject(JSONObject json, Class<T> cls) throws JSONException {
        try {
            T obj = cls.newInstance();

            for (Field campo : cls.getDeclaredFields()) {
                boolean isAcessivel = campo.isAccessible();
                if (!isAcessivel) campo.setAccessible(true);

                if (!campo.isAnnotationPresent(JSONIgnore.class) && !isPublicStaticFinal(campo)) {
                    try {
                        String nomeCampo;
                        if (campo.isAnnotationPresent(JSONFieldName.class)) {
                            nomeCampo = campo.getAnnotation(JSONFieldName.class).value();
                        } else nomeCampo = campo.getName();

                        if (json.has(nomeCampo)) {
                            Class<?> clsField = campo.getType();
                            Object value = json.get(nomeCampo);

                            if (value != null && !value.toString().equals("null")) {

                                if (clsField == String.class) {
                                    campo.set(obj, json.getString(nomeCampo));
                                } else if (clsField == Integer.class || clsField == int.class) {
                                    campo.set(obj, json.getInt(nomeCampo));
                                } else if (clsField == Long.class || clsField == long.class) {
                                    campo.set(obj, json.getLong(nomeCampo));
                                } else if (clsField == Double.class || clsField == double.class) {
                                    campo.set(obj, json.getDouble(nomeCampo));
                                } else if (clsField == Boolean.class || clsField == boolean.class) {
                                    campo.set(obj, json.getBoolean(nomeCampo));
                                } else if (clsField.isEnum()) {
                                    campo.set(obj, Enum.valueOf((Class<? extends Enum>) clsField, json.getString(nomeCampo)));
                                } else if (clsField == Date.class) {
                                    try {
                                        campo.set(obj, dateFormat.parse(json.getString(nomeCampo)));
                                    } catch (ParseException ex) {
                                        campo.set(obj, null);
                                    }
                                } else {
                                    if (value instanceof JSONObject)
                                        campo.set(obj, parseJsonToObject((JSONObject) value, clsField));
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (!isAcessivel) campo.setAccessible(false);
                }
            }

            return obj;
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> Iterator<T> iterator(JSONArray array, Class<T> cls) {
        return new JSONArrayIterator<>(array, cls);
    }

    public static Iterator<?> iterator(JSONArray array) {
        return new JSONArrayIterator<>(array, Object.class);
    }

    private static boolean isScalar(Object value) {
        return value instanceof String ||
                value instanceof Boolean ||
                value instanceof Integer ||
                value instanceof Double ||
                value instanceof Float ||
                value instanceof Short ||
                value instanceof Byte ||
                value instanceof Long ||
                value instanceof Void ||
                value instanceof Character;
    }

    private static boolean isScalar(Class<?> cls) {
        return cls == String.class ||
                cls == Boolean.class ||
                cls == Integer.class ||
                cls == Double.class ||
                cls == Float.class ||
                cls == Short.class ||
                cls == Byte.class ||
                cls == Long.class ||
                cls == Void.class ||
                cls == Character.class;
    }

    private static boolean isPublicStaticFinal(Field field) {
        int modifiers = field.getModifiers();
        return (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier
                .isFinal(modifiers));
    }

    public JSONObject toJSONObject() {
        return this.jsonObject;
    }

    public JSONArray toJSONArray() {
        return this.jsonArray;
    }

    public int length() {
        return this.mArray ? jsonArray.length() : jsonObject.length();
    }

    public Object get(String name) throws JSONException {
        if (this.mArray) throw new JSONException(excecaoJSONObject);
        return jsonObject.get(name);
    }

    public String getString(String name) throws JSONException {
        if (this.mArray) throw new JSONException(excecaoJSONObject);
        return jsonObject.getString(name);
    }

    public int getInt(String name) throws JSONException {
        if (this.mArray) throw new JSONException(excecaoJSONObject);
        return jsonObject.getInt(name);
    }

    public long getLong(String name) throws JSONException {
        if (this.mArray) throw new JSONException(excecaoJSONObject);
        return jsonObject.getLong(name);
    }

    public double getDouble(String name) throws JSONException {
        if (this.mArray) throw new JSONException(excecaoJSONObject);
        return jsonObject.getDouble(name);
    }

    public boolean getBoolean(String name) throws JSONException {
        if (this.mArray) throw new JSONException(excecaoJSONObject);
        return jsonObject.getBoolean(name);
    }

    public JSONObject getJSONObject(String name) throws JSONException {
        if (this.mArray) throw new JSONException(excecaoJSONObject);
        return jsonObject.getJSONObject(name);
    }

    public JSONArray getJSONArray(String name) throws JSONException {
        if (this.mArray) throw new JSONException(excecaoJSONObject);
        return jsonObject.getJSONArray(name);
    }

    public Date getDate(String name) throws JSONException, ParseException {
        if (this.mArray) throw new JSONException(excecaoJSONObject);
        return dateFormat.parse(jsonObject.getString(name));
    }

    public Date getDate(String name, String format) throws JSONException, ParseException {
        if (this.mArray) throw new JSONException(excecaoJSONObject);
        return new SimpleDateFormat(format, Locale.getDefault()).parse(jsonObject.getString(name));
    }

    public Object get(int index) throws JSONException {
        if (!this.mArray) throw new JSONException(excecaoJSONArray);
        return jsonArray.get(index);
    }

    public String getString(int index) throws JSONException {
        if (!this.mArray) throw new JSONException(excecaoJSONArray);
        return jsonArray.getString(index);
    }

    public int getInt(int index) throws JSONException {
        if (!this.mArray) throw new JSONException(excecaoJSONArray);
        return jsonArray.getInt(index);
    }

    public long getLong(int index) throws JSONException {
        if (!this.mArray) throw new JSONException(excecaoJSONArray);
        return jsonArray.getLong(index);
    }

    public double getDouble(int index) throws JSONException {
        if (!this.mArray) throw new JSONException(excecaoJSONArray);
        return jsonArray.getDouble(index);
    }

    public boolean getBoolean(int index) throws JSONException {
        if (!this.mArray) throw new JSONException(excecaoJSONArray);
        return jsonArray.getBoolean(index);
    }

    public JSONObject getJSONObject(int index) throws JSONException {
        if (!this.mArray) throw new JSONException(excecaoJSONArray);
        return jsonArray.getJSONObject(index);
    }

    public JSONArray getJSONArray(int index) throws JSONException {
        if (!this.mArray) throw new JSONException(excecaoJSONArray);
        return jsonArray.getJSONArray(index);
    }

    public Date getDate(int index) throws JSONException, ParseException {
        if (!this.mArray) throw new JSONException(excecaoJSONArray);
        return dateFormat.parse(jsonArray.getString(index));
    }

    public Date getDate(int index, String format) throws JSONException, ParseException {
        if (!this.mArray) throw new JSONException(excecaoJSONArray);
        return new SimpleDateFormat(format, Locale.getDefault()).parse(jsonArray.getString(index));
    }

    public boolean isArray() {
        return mArray;
    }

    public boolean has(String name) {
        if (this.mArray) return false;
        return jsonObject.has(name);
    }

    public <T> T fromJSON(Class<T> cls) throws JSONException {
        if (this.mArray) throw new JSONException(excecaoJSONObject);
        return parseJsonToObject(this.jsonObject, cls);
    }

    public <T> Iterator<T> iterator(Class<T> cls) throws JSONException {
        if (!this.mArray) throw new JSONException(excecaoJSONArray);
        return iterator(this.jsonArray, cls);
    }

    public Iterator<?> iterator() throws JSONException {
        if (!this.mArray) throw new JSONException(excecaoJSONArray);
        return iterator(this.jsonArray);
    }

    @NonNull
    @Override
    public String toString() {
        return this.mArray
                ? (this.jsonArray == null ? super.toString() : jsonArray.toString())
                : (this.jsonObject == null ? super.toString() : jsonObject.toString());
    }

    /**
     * Anotação utilizada para marcar um atributo que deve ser ignorado ao fazer parsing do objeto para JSON.
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface JSONIgnore {
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface JSONFieldName {
        String value();
    }

    private static class JSONArrayIterator<T> implements Iterator<T> {

        private JSONArray array;
        private int current = -1;
        private Class<T> cls;

        JSONArrayIterator(JSONArray array, Class<T> cls) {
            this.array = array;
            this.cls = cls;
        }

        @Override
        public boolean hasNext() {
            return (current + 1) < array.length();
        }

        @Override
        public T next() {
            try {
                return cls.cast(array.get(++current));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
