package com.lims.manage.erp.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author gjl
 */
public class MapSort{


    public static <K extends Comparable<K>,V extends Comparable<V>> List<Map.Entry<K, V>> sortByKey(final Map<K,V> map){
        List<Map.Entry<K, V>> mapList = new ArrayList<>();
        mapList.addAll(map.entrySet());
        Collections.sort(mapList,new MapKeySort<K,V>());
        return mapList;
    }

    public static <K extends Comparable<K>,V extends Comparable<V>> List<Map.Entry<K, V>> sortByValue(final Map<K,V> map){
        List<Map.Entry<K, V>> mapList = new ArrayList<>();
        mapList.addAll(map.entrySet());
        Collections.sort(mapList,new MapValueSort<K,V>());
        return mapList;
    }


    private static class MapKeySort<K extends Comparable<K>,V extends Comparable<V>> implements Comparator<Map.Entry<K, V>>{

        @Override
        public int compare(Entry<K, V> o1, Entry<K, V> o2) {
            // TODO 自动生成的方法存根
            return o1.getKey().compareTo(o2.getKey());
        }

    }

    private static class MapValueSort<K extends Comparable<K>,V extends Comparable<V>> implements Comparator<Map.Entry<K, V>>{

        @Override
        public int compare(Entry<K, V> o1, Entry<K, V> o2) {
            // TODO 自动生成的方法存根
            return o1.getValue().compareTo(o2.getValue());
        }

    }
}

