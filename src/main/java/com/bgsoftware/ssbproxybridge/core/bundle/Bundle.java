package com.bgsoftware.ssbproxybridge.core.bundle;

import com.bgsoftware.ssbproxybridge.core.JsonUtil;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

public class Bundle {

    private static final List<Class<?>> ALLOWED_TYPES = Arrays.asList(Number.class, String.class, Boolean.class,
            Bundle.class, List.class, Enum.class);

    public static Bundle EMPTY = new Bundle().toImmutable();

    private final JsonObject data = new JsonObject();
    private String channelName;
    private String sender;
    private LinkedList<String> recipients;

    public Bundle() {

    }

    public Bundle(Bundle other) {
        this(other.data);
        this.channelName = other.channelName;
        this.sender = other.sender;
        if (other.recipients != null)
            this.recipients = new LinkedList<>(other.recipients);
    }

    public Bundle(String channelName, String data) throws BundleParseError {
        this.channelName = channelName;
        JsonObject parsed;

        try {
            parsed = BundleSerializer.GSON.fromJson(data, JsonObject.class);
        } catch (JsonSyntaxException error) {
            throw new BundleParseError(error);
        }

        parsed.entrySet().forEach(entry -> {
            if (entry.getKey().startsWith("$")) {
                switch (entry.getKey()) {
                    case "$channel":
                        this.channelName = entry.getValue().getAsString();
                        break;
                    case "$sender":
                        this.sender = entry.getValue().getAsString();
                        break;
                    case "$recipients":
                        this.recipients = new LinkedList<>();
                        entry.getValue().getAsJsonArray().forEach(recipient ->
                                this.recipients.add(recipient.getAsString()));
                        break;
                }
            } else {
                setData(entry.getKey(), entry.getValue());
            }
        });
    }

    public Bundle(JsonObject data) {
        data.deepCopy().entrySet().forEach(entry -> this.data.add(entry.getKey(), entry.getValue()));
    }

    @Nullable
    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    @Nullable
    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setRecipient(String recipient) {
        this.recipients = new LinkedList<>();
        this.recipients.add(recipient);
    }

    @Nullable
    public List<String> getRecipients() {
        return recipients == null ? null : Collections.unmodifiableList(recipients);
    }

    public void setRecipients(List<String> recipients) {
        this.recipients = new LinkedList<>(recipients);
    }

    public int getInt(String key) {
        return getNumber(key).intValue();
    }

    public void setInt(String key, int value) {
        setData(key, new JsonPrimitive(value));
    }

    public byte getByte(String key) {
        return getNumber(key).byteValue();
    }

    public void setByte(String key, byte value) {
        setData(key, new JsonPrimitive(value));
    }

    public long getLong(String key) {
        return getNumber(key).longValue();
    }

    public void setLong(String key, long value) {
        setData(key, new JsonPrimitive(value));
    }

    public long getShort(String key) {
        return getNumber(key).shortValue();
    }

    public void setShort(String key, short value) {
        setData(key, new JsonPrimitive(value));
    }

    public double getDouble(String key) {
        return getNumber(key).doubleValue();
    }

    public void setDouble(String key, double value) {
        setData(key, new JsonPrimitive(value));
    }

    public float getFloat(String key) {
        return getNumber(key).floatValue();
    }

    public void setFloat(String key, float value) {
        setData(key, new JsonPrimitive(value));
    }

    public BigInteger getBigInteger(String key) {
        return getData(key, JsonPrimitive.class, JsonPrimitive::getAsBigInteger);
    }

    public void setBigInteger(String key, BigInteger value) {
        setData(key, new JsonPrimitive(value));
    }

    public BigDecimal getBigDecimal(String key) {
        return getData(key, JsonPrimitive.class, JsonPrimitive::getAsBigDecimal);
    }

    public void setBigDecimal(String key, BigDecimal value) {
        setData(key, new JsonPrimitive(value));
    }

    public String getString(String key) {
        return getData(key, JsonPrimitive.class, JsonPrimitive::getAsString);
    }

    public void setString(String key, String value) {
        setData(key, new JsonPrimitive(value));
    }

    public boolean getBoolean(String key) {
        return getData(key, JsonPrimitive.class, JsonPrimitive::getAsBoolean);
    }

    public void setBoolean(String key, boolean value) {
        setData(key, new JsonPrimitive(value));
    }

    public Bundle getExtra(String key) {
        return new Bundle(getData(key, JsonObject.class));
    }

    public void setExtra(String key, Bundle value) {
        Preconditions.checkArgument(value != this, "Cannot insert a bundle as an extra to itself.");
        setData(key, value.getJson());
    }

    public List<Object> getList(String key) {
        List<Object> list = new LinkedList<>();
        getData(key, JsonArray.class).forEach(element -> list.add(JsonUtil.getValueFromElement(element)));
        return Collections.unmodifiableList(list);
    }

    public void setList(String key, List<?> value) {
        JsonArray array = new JsonArray();
        value.forEach(element -> array.add(JsonUtil.getJsonFromObject(element)));
        setData(key, array);
    }

    public <T extends Enum<T>> T getEnum(String key, Class<T> enumType) {
        return Enum.valueOf(enumType, getString(key));
    }

    public void setEnum(String key, Enum<?> value) {
        setString(key, value.name());
    }

    public UUID getUUID(String key) {
        return UUID.fromString(getString(key));
    }

    public void setUUID(String key, UUID value) {
        setString(key, value.toString());
    }

    public void setObject(String key, Object value) {
        Preconditions.checkArgument(isAllowedType(value.getClass()), "Value type is not allowed: " + value.getClass());
        setData(key, JsonUtil.getJsonFromObject(value));
    }

    public boolean contains(String key) {
        return this.data.has(key);
    }

    public Bundle toImmutable() {
        return new ImmutableBundle(this);
    }

    public JsonObject getJson() {
        return data.deepCopy();
    }

    private Number getNumber(String key) {
        return getData(key, JsonPrimitive.class, JsonPrimitive::getAsNumber);
    }

    protected <T extends JsonElement> T getData(String key, Class<T> valueType) {
        return getData(key, valueType, result -> result);
    }

    protected <R, T extends JsonElement> R getData(String key, Class<T> valueType, Function<T, R> function) {
        JsonElement value = this.data.get(key);
        if (value != null && valueType.isAssignableFrom(value.getClass())) {
            try {
                return function.apply((T) value);
            } catch (Throwable ignored) {
            }
        }
        throw new NoSuchElementException("Cannot find value for '" + key + "' with type '" + valueType + "'" +
                (value == null ? "" : " (Found '" + value.getClass() + "')"));
    }

    protected void setData(String key, JsonElement value) {
        Preconditions.checkArgument(!key.startsWith("$"), "Keys cannot start with $.");
        this.data.add(key, value);
    }

    private static boolean isAllowedType(Class<?> type) {
        for (Class<?> allowedType : ALLOWED_TYPES) {
            if (allowedType.isAssignableFrom(type))
                return true;
        }

        return false;
    }

    private static class ImmutableBundle extends Bundle {

        ImmutableBundle(Bundle other) {
            super(other);
        }

        @Override
        public void setChannelName(String channelName) {
            throw new UnsupportedOperationException("Cannot set channel-name to immutable bundles.");
        }

        @Override
        public void setSender(String sender) {
            throw new UnsupportedOperationException("Cannot set channel-name to immutable bundles.");
        }

        @Override
        public void setRecipients(List<String> recipients) {
            throw new UnsupportedOperationException("Cannot set channel-name to immutable bundles.");
        }

        @Override
        public void setRecipient(String recipient) {
            throw new UnsupportedOperationException("Cannot set channel-name to immutable bundles.");
        }

        @Override
        public Bundle toImmutable() {
            return this;
        }

        @Override
        protected void setData(String key, JsonElement value) {
            throw new UnsupportedOperationException("Cannot set data to immutable bundles.");
        }
    }

}
