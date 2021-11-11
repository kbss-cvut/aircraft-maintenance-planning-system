package cz.cvut.kbss.amaplas.exp.common;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.connectivity.KosarajuStrongConnectivityInspector;
import org.jgrapht.alg.cycle.QueueBFSFundamentalCycleBasis;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.AttributeType;
import org.jgrapht.nio.DefaultAttribute;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;

public class JGraphTUtils {

    /**
     * Construct attributes for the input element based on it's bean properties and public fields who's type is
     * primitive or String.
     *
     * @param t
     * @param <T>
     * @return
     */
    public static <T> Map<String, Attribute> toMap(T t) {
        Map<String, Attribute> ret = new HashMap<>();
        try {
            BeanInfo bi = Introspector.getBeanInfo(t.getClass());
            for (PropertyDescriptor pd : bi.getPropertyDescriptors()) {
                String name = pd.getName();
                Object val = pd.getReadMethod().invoke(t);
                DefaultAttribute a = new DefaultAttribute(val, getType(pd.getPropertyType()));
                ret.put(name, a);
            }
            for(Field f : t.getClass().getDeclaredFields()){
                if(Modifier.isPublic(f.getModifiers())){
                    String name = f.getName();
                    if(!ret.containsKey(name)) {
                        Object val = f.get(t);
                        if(val == null)
                            continue;
                        AttributeType type = getType(f.getType());
                        if(!type.equals(AttributeType.UNKNOWN)) {
                            DefaultAttribute a = new DefaultAttribute(val, type);
                            ret.put(name, a);
                        }
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IntrospectionException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static <T> Map<String, AttributeType> attributeDefinitions(Class<T> cls) {
        Map<String, AttributeType> ret = new HashMap<>();
        try {
            BeanInfo bi = Introspector.getBeanInfo(cls);
            for (PropertyDescriptor pd : bi.getPropertyDescriptors()) {
                String name = pd.getName();
                AttributeType type = getType(pd.getPropertyType());
                if(!type.equals(AttributeType.UNKNOWN)) {
                    ret.put(name, type);
                }
            }
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }

        for(Field f : cls.getDeclaredFields()){
            if(Modifier.isPublic(f.getModifiers())){
                String name = f.getName();
                if(!ret.containsKey(name)) {
                    AttributeType type = getType(f.getType());
                    if(!type.equals(AttributeType.UNKNOWN)) {
                        ret.put(name, type);
                    }
                }
            }
        }

        return ret;
    }


//    public static AttributeType getType(Object val)
//    }

    public static AttributeType getType(Class cls){
        if(boolean.class.isAssignableFrom(cls) || Boolean.class.isAssignableFrom(cls)){
            return AttributeType.BOOLEAN;
        }else if(double.class.isAssignableFrom(cls) || Double.class.isAssignableFrom(cls)) {
            return AttributeType.DOUBLE;
        }else if(float.class.isAssignableFrom(cls) || Float.class.isAssignableFrom(cls)){
            return AttributeType.FLOAT;
        }else if(int.class.isAssignableFrom(cls) || Integer.class.isAssignableFrom(cls)){
            return AttributeType.INT;
        }else if(long.class.isAssignableFrom(cls) || Long.class.isAssignableFrom(cls)){
            return AttributeType.LONG;
        }else if(cls.isEnum()){
            return AttributeType.STRING;
        }else if(String.class.isAssignableFrom(cls)){
            return AttributeType.STRING;
        }
        return AttributeType.UNKNOWN;
    }

    public static <V, E> List<GraphPath<V, E>> findDirectedCycles(Graph<V, E> g){
        QueueBFSFundamentalCycleBasis<V, E> alg = new QueueBFSFundamentalCycleBasis<>(g);
        List<GraphPath<V, E>> ret = new ArrayList<>();
        for( GraphPath<V, E> gp : alg.getCycleBasis().getCyclesAsGraphPaths()){
            AsSubgraph<V, E> sg = new AsSubgraph<>(g, new HashSet<>(gp.getVertexList()), new HashSet<>(gp.getEdgeList()));
            KosarajuStrongConnectivityInspector<V, E> alg2 = new KosarajuStrongConnectivityInspector<>(sg);
            if(alg2.isStronglyConnected())
                ret.add(gp);
        }
        return ret;
//        return alg.getCycleBasis().getCyclesAsGraphPaths().iterator().next().getCycles().stream().map(l -> new HashSet<>(l)).collect(Collectors.toSet());
    }
}
