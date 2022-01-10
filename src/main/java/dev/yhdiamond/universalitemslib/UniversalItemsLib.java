package dev.yhdiamond.universalitemslib;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public final class UniversalItemsLib {

    public static Set<ItemStack> getAllItemStacksInPluginItemManagers(JavaPlugin plugin) {
        Set<ItemStack> output = new HashSet<>();
        Reflections reflections = new Reflections(plugin.getClass().getPackage().toString().replaceFirst("package ", ""));
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(ItemManager.class);

        for (Class clazz : classes) {
            Field[] fields = clazz.getFields();
            for (Field field : fields) {
                Class fieldClass = field.getDeclaringClass();
                if (!(fieldClass.isAssignableFrom(ItemStack.class) || fieldClass.equals(ItemStack.class))) {
                    ItemStack itemStack;
                    try {
                        itemStack = (ItemStack) field.get(null);
                    } catch (IllegalAccessException e) {
                        System.err.println("Illegal access on " + field);
                        continue;
                    }
                    output.add(itemStack);
                }
            }
        }
        return output;
    }

    public static Set<ItemStack> getAllItemStacksInClass(Class clazz) {
        Set<ItemStack> output = new HashSet<>();
        Field[] fields = clazz.getFields();
        for (Field field : fields) {
            Class fieldClass = field.getDeclaringClass();
            if (!(fieldClass.isAssignableFrom(ItemStack.class) || fieldClass.equals(ItemStack.class))) {
                ItemStack itemStack;
                try {
                    itemStack = (ItemStack) field.get(ItemStack.class);
                    if (itemStack == null) continue;
                } catch (Exception e) {
                    continue;
                }
                output.add(itemStack);
            }
        }
        return output;
    }

    public static Set<ItemStack> getAllItemStacksInPlugin(JavaPlugin plugin, boolean requireItemManagerAnnotation) {
        Set<ItemStack> output = new HashSet<>();
        List<ClassLoader> classLoadersList = new LinkedList<ClassLoader>();
        classLoadersList.add(ClasspathHelper.contextClassLoader());
        classLoadersList.add(ClasspathHelper.staticClassLoader());

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setScanners(new SubTypesScanner(false), new ResourcesScanner())
                .setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0]))));
        Set<Class<?>> classes = requireItemManagerAnnotation ? reflections.getTypesAnnotatedWith(ItemManager.class) : reflections.getSubTypesOf(Object.class);
        for (Class clazz : classes) {
            try {
                if (clazz.getPackage().toString().startsWith(plugin.getClass().getPackage().toString())) {
                    output.addAll(getAllItemStacksInClass(clazz));
                }
            } catch (Throwable e) { continue; }
        }
        System.out.println("Done outputting all classes!");
        return output;
    }

    public static Set<ItemStack> getAllItemStacksInPlugin(JavaPlugin plugin) {
        return getAllItemStacksInPlugin(plugin, false);
    }


}
