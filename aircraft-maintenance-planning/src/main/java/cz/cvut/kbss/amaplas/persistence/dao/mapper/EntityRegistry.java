package cz.cvut.kbss.amaplas.persistence.dao.mapper;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class EntityRegistry {
    protected Map<String, Object> registry = new HashMap<>();

    public Object put(String key, Object entity){
        return registry.put(key, entity);
    }

    public <T> T getOrCreate(String key, Supplier<T> factory, Class<T> clazz){
        Object val = registry.get(key);
        if(val == null){
            T newVal = factory.get();
            registry.put(key, newVal);
            return newVal;
        }
        if(clazz.isInstance(val))
            return (T)val;
        throw new RuntimeException(String.format("Registry - cannot cast %s to %s", val.getClass().getCanonicalName(), clazz.getCanonicalName()));
    }
}
