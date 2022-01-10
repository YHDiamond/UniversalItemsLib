package dev.yhdiamond.rpmagic;

import com.google.gson.JsonObject;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class RPMagic {

    public static void start(JavaPlugin plugin) throws IllegalAccessException {
        Set<ItemStack> itemStacks = getAllItemStacks(plugin).stream().filter(e -> e.getItemMeta().hasCustomModelData()).collect(Collectors.toSet());
        JsonObject jsonObject = new JsonObject();
        itemStacks.stream().forEach(itemStack -> {
            JsonObject newObj = new JsonObject();
            Material material = itemStack.getType();
            newObj.addProperty("material", material.name());
            String type = null;
            if (material.isBlock()) type = "block";
            if (material.isItem()) type = "item";
        });
    }

    public static Set<ItemStack> getAllItemStacks(JavaPlugin plugin) {
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

}
