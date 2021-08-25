package com.androidcat.catlibs.utils;

import android.content.Context;

import java.lang.reflect.Field;

public class Res {

    /**
     * 获取id
     *
     * @param resName 资源名称
     * @return 资源id
     */
    public static int id(Context context,String resName){
        return context.getResources().getIdentifier(resName, "id", context.getPackageName());
    }

    /**
     * 获取xml类型资源id
     *
     * @param resName 资源名称
     * @return 资源id
     */
    public static int xml(Context context,String resName){
        return context.getResources().getIdentifier(resName, "xml", context.getPackageName());
    }

    /**
     * 获取anim类型资源id
     *
     * @param resName 资源名称
     * @return 资源id
     */
    public static int anim(Context context,String resName){
        return context.getResources().getIdentifier(resName, "anim", context.getPackageName());
    }

    /**
     * 获取layout类型资源id
     *
     * @param resName 资源名称
     * @return 资源id
     */
    public static int layout(Context context,String resName){
        return  context.getResources().getIdentifier(resName, "layout", context.getPackageName());
    }

    /**
     * 获取drawable类型资源id
     *
     * @param resName 资源名称
     * @return 资源id
     */
    public static int drawable(Context context,String resName){
        return context.getResources().getIdentifier(resName, "drawable", context.getPackageName());
    }

    public static int mipmap(Context context,String resName){
        return context.getResources().getIdentifier(resName, "mipmap", context.getPackageName());
    }

    /**
     * 获取string类型资源id
     *
     * @param resName 资源名称
     * @return 资源id
     */
    public static int string(Context context,String resName){
        return context.getResources().getIdentifier(resName, "string", context.getPackageName());
    }

    /**
     * 获取raw类型资源id
     *
     * @param resName 资源名称
     * @return 资源id
     */
    public static int raw(Context context,String resName){
        return context.getResources().getIdentifier(resName, "raw", context.getPackageName());
    }

    public static int getColor(Context context,String resName){
        return context.getResources().getIdentifier(resName, "color", context.getPackageName());
    }


    /**
     * 获取style类型资源id
     *
     * @param resName 资源名称
     * @return 资源id
     */
    public static int style(Context context,String resName) {
        return context.getResources().getIdentifier(resName, "style", context.getPackageName());
    }

    public static int[] getIdsByName(Context context, String className, String name) {
        String packageName = context.getPackageName();
        Class r = null;
        int[] ids = null;
        try {
            r = Class.forName(packageName + ".R");

            Class[] classes = r.getClasses();
            Class desireClass = null;

            for (int i = 0; i < classes.length; ++i) {
                if (classes[i].getName().split("\\$")[1].equals(className)) {
                    desireClass = classes[i];
                    break;
                }
            }

            if ((desireClass != null) && (desireClass.getField(name).get(desireClass) != null) && (desireClass.getField(name).get(desireClass).getClass().isArray()))
                ids = (int[])desireClass.getField(name).get(desireClass);
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        return ids;
    }

    public static int getStyleableFieldId(Context context, String styleableName, String styleableFieldName) {
        String className = context.getPackageName() + ".R";
        String type = "styleable";
        String name = styleableName + "_" + styleableFieldName;
        try {
            Class<?> cla = Class.forName(className);
            for (Class<?> childClass : cla.getClasses()) {
                String simpleName = childClass.getSimpleName();
                if (simpleName.equals(type)) {
                    for (Field field : childClass.getFields()) {
                        String fieldName = field.getName();
                        if (fieldName.equals(name)) {
                            return (int) field.get(null);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return 0;
    }

}
