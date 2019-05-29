package com.xkp.attendance.utils.excel;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;


public class ListToMapUtil {

    private static final Logger logger = LoggerFactory.getLogger(ListToMapUtil.class);

    /**
     * 获取属性类型(type)，属性名(name)，属性值(value)的map组成的list
     * */
    public static List objectFieldsToMap(Object o, List<String> keys) {
		Field[] fields = o.getClass().getDeclaredFields();
		List<Map<String, String>> list = new ArrayList<>();
		Map map = new HashedMap();
		for (int i = 0; i < keys.size(); i++) {
			for (int j = 0; j < fields.length; j++) {
				if (fields[j].getName().equals(keys.get(i))) {
					map.put(keys.get(i), getFieldValueByName(fields[j].getName(), o));
				}
			}
		}
		list.add(map);
		return list;
	}

	public static List objectFieldsToMap(Object o,Field[] fields, List<String> keys) {
		List<Map<String, String>> list = new ArrayList<>();
		Map map = new HashedMap();
		for (int i = 0; i < keys.size(); i++) {
			for (int j = 0; j < fields.length; j++) {
				if (fields[j].getName().equals(keys.get(i))) {
					map.put(keys.get(i), getFieldValueByName(fields[j].getName(), o));
				}
			}
		}
		list.add(map);
		return list;
	}

	public static Field[] getAllFields(Object object) {
		Class clazz = object.getClass();
		List<Field> fieldList = new ArrayList<>();
		while (clazz != null) {
			fieldList.addAll(Arrays.asList(clazz.getDeclaredFields()));
			clazz = clazz.getSuperclass();
		}
		Field[] fields = new Field[fieldList.size()];
		fieldList.toArray(fields);
		return fields;
	}



    /**
     * 获取属性类型(type)，属性名(name)，属性值(value)的map组成的list
     * */
    public static List MapListobjectFieldsToMap(  List<Map<String, String>>  o, List<String> keys){

        List<Map<String, String>> list=new ArrayList<>();
        Map map=new HashedMap();
        for ( int  i = 0;i<keys.size();i++){
             int finalI = i;
            o.stream().forEach(stringStringMap -> {
                String keyG =  keys.get(finalI);
                String name = stringStringMap.get(keyG);
                if(StringUtils.isNotBlank(name)){
//                    logger.info("MapListobjectFieldsToMap = key:{},name:{}",keys.get(finalI),name);
                    map.put(keyG,name);
                }
            });
        }
        list.add(map);
        return list;
    }


    public static Object getFieldValueByName(String fieldName, Object o) {
        try {
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            String getter = "get" + firstLetter + fieldName.substring(1);
            Method method = o.getClass().getMethod(getter, new Class[] {});
            Object value = method.invoke(o, new Object[] {});
            if(value!=null && value instanceof Date){
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(value);
            }else{
                return value.toString();
            }
        } catch (Exception e) {
            return null;
        }
    }
}
