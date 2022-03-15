package lielietea.thevaultnorthkinchovsk.vault;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class VaultCodec<C extends VaultCodecComponent<U,V>,U,V extends Valuable> implements JsonDeserializer<VaultCore<U,V>>, JsonSerializer<VaultCore<U,V>> {

    VaultCodecComponent<U,V> codecComponent;

    public VaultCodec(VaultCodecComponent<U,V> codecComponent) {
        this.codecComponent = codecComponent;
    }

    @Override
    public VaultCore<U, V> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        VaultCore<U, V> ret = new VaultCore<>();
        Map<V, Map<U, BigDecimal>> core = new HashMap<>();
        Set<U> hu = new HashSet<>();
        Set<V> hv = new HashSet<>();
        JsonObject src = json.getAsJsonObject();
        if(!src.has("data"))
            return ret;
        JsonObject data = src.get("data").getAsJsonObject();
        for(Map.Entry<String,JsonElement> e: data.entrySet()){
            JsonObject userBalanceData = e.getValue().getAsJsonObject();
            Map<U, BigDecimal> balanceData = new HashMap<>();
            for(String s: userBalanceData.keySet()){
                balanceData.put(codecComponent.userFromJson(s), new BigDecimal(userBalanceData.get(s).getAsString()));
            }
            core.put(codecComponent.valuableFromJson(e.getKey()),balanceData);
        }
        JsonArray jsonHu = src.get("users").getAsJsonArray();
        for(JsonElement jsonElement:jsonHu)
            hu.add(codecComponent.userFromJson(jsonElement.getAsString()));
        JsonArray jsonHv = src.get("valuables").getAsJsonArray();
        for(JsonElement jsonElement:jsonHv)
            hv.add(codecComponent.valuableFromJson(jsonElement.getAsString()));
        ret.setVault(core,hu,hv);
        return ret;
    }

    @Override
    public JsonElement serialize(VaultCore<U, V> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject ret = new JsonObject();
        JsonObject core = new JsonObject();
        for(V valuables: src.getCurrentValuables()){
            JsonObject balanceData = new JsonObject();
            src.getAllBalanceOfValuable(valuables).forEach((user, bigDecimal) -> {
                balanceData.addProperty(codecComponent.userToJson(user), bigDecimal.toPlainString());
            });
            core.add(codecComponent.valuableToJson(valuables), balanceData);
        }
        ret.add("data",core);
        JsonArray hu = new JsonArray();
        for(U user:src.getHistoricalUsers()){
            hu.add(codecComponent.userToJson(user));
        }
        ret.add("users",hu);
        JsonArray hv = new JsonArray();
        for(V valuable:src.getHistoricalValuables())
            hv.add(codecComponent.valuableToJson(valuable));
        ret.add("valuables",hv);
        return ret;
    }
}
