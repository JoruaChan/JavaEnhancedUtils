package cn.joruachan.utils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 支持<strong>动态扩缩容</strong>的HashMap
 * <p>
 * 由于HashMap原生已支持动态扩容，但是未支持动态缩容;
 * 笔者认为: 有许多需要支持动态扩缩容的场景;
 * <p>
 * 比如：一次处理中峰值有个1w条，但大部分时间只会10条。
 * 这点似乎违背了HashMap设计的初衷：既能减少哈希冲突，又能节省空间；
 *
 * @author JoruaChan
 * @contact joruachan@gmail.com
 */
public class AutoScaleHashMap<K, V> extends HashMap<K, V> {
    // 获取HashMap的table参数
    public static Field TABLE_FIELD;
    public static Field THRESHOLD_FIELD;

    static {
        try {
            Field tableField = HashMap.class.getDeclaredField("table");
            tableField.setAccessible(true);

            TABLE_FIELD = tableField;

            Field thresholdField = HashMap.class.getDeclaredField("threshold");
            thresholdField.setAccessible(true);

            THRESHOLD_FIELD = thresholdField;
        } catch (NoSuchFieldException e) {
            throw new Error("AutoScaleHashMap load failed！");
        }
    }

    // 用于当前的线程
    private Thread thread;

    long removeCount;
    long timestamp;

    long removeThreshold;
    long removeTimeThreshold;

    public AutoScaleHashMap(int initialCapacity, float loadFactor) {
        this(initialCapacity, loadFactor, -1, 0);
    }

    public AutoScaleHashMap(int initialCapacity) {
        this(initialCapacity, -1, 0);
    }

    public AutoScaleHashMap() {
        this(-1L, 0L);
    }

    public AutoScaleHashMap(Map<? extends K, ? extends V> m) {
        super(m);
        this.removeThreshold = removeThreshold;
        this.removeTimeThreshold = removeTimeThreshold;
    }

    public AutoScaleHashMap(int initialCapacity, float loadFactor, long removeThreshold, long removeTimeThreshold) {
        super(initialCapacity, loadFactor);
        this.removeThreshold = removeThreshold;
        this.removeTimeThreshold = removeTimeThreshold;
    }

    public AutoScaleHashMap(int initialCapacity, long removeThreshold, long removeTimeThreshold) {
        super(initialCapacity);
        this.removeThreshold = removeThreshold;
        this.removeTimeThreshold = removeTimeThreshold;
    }

    public AutoScaleHashMap(long removeThreshold, long removeTimeThreshold) {
        this.removeThreshold = removeThreshold;
        this.removeTimeThreshold = removeTimeThreshold;
    }

    public AutoScaleHashMap(Map<? extends K, ? extends V> m, long removeThreshold, long removeTimeThreshold) {
        super(m);
        this.removeThreshold = removeThreshold;
        this.removeTimeThreshold = removeTimeThreshold;
    }

    @Override
    public V put(K key, V value) {
        V v = super.put(key, value);
        removeCount = 0;
        return v;
    }

    @Override
    public V remove(Object key) {
        V value = super.remove(key);

        if (removeThreshold > 0 && ++removeCount > removeThreshold &&
                System.currentTimeMillis() - timestamp >= removeTimeThreshold) {
            // TODO: 指定时间内remove，并达到阈值，则调整。但是调整成多少？？？
            // 看看Guava如何实现的
            removeCount = 0;
            timestamp = 0;
        }
        return value;
    }

    /**
     * 通过反射获取哈希桶的长度
     *
     * @return
     * @throws IllegalAccessException
     */
    public int getTableLength() throws IllegalAccessException {
        Object[] objects = (Object[]) TABLE_FIELD.get(this);
        if (objects == null) return 0;
        return objects.length;
    }

    public static void main(String[] args) throws IllegalAccessException {
        AutoScaleHashMap<String, Integer> hashMap = new AutoScaleHashMap<>();
        System.out.println("哈希桶长度: " + hashMap.getTableLength() + ", size: " + hashMap.size());

        for (int i = 0; i < 10000; i++) {
            Integer integer = Integer.valueOf(i);
            hashMap.put(integer.toString(), integer);
        }

        // 获取hashMap的哈希桶长度
        System.out.println("哈希桶长度: " + hashMap.getTableLength() + ", size: " + hashMap.size());

        for (int i = 9999; i > 10; i--) {
            Integer integer = Integer.valueOf(i);
            hashMap.remove(integer.toString());
        }
        System.out.println("哈希桶长度: " + hashMap.getTableLength() + ", size: " + hashMap.size());


        AutoScaleHashMap<String, Integer> hashMap2 = new AutoScaleHashMap<>(5L, 2000L);
        System.out.println("哈希桶长度: " + hashMap2.getTableLength() + ", size: " + hashMap2.size());

        for (int i = 0; i < 10000; i++) {
            Integer integer = Integer.valueOf(i);
            hashMap2.put(integer.toString(), integer);
        }

        // 获取hashMap的哈希桶长度
        System.out.println("哈希桶长度: " + hashMap2.getTableLength() + ", size: " + hashMap2.size());

        for (int i = 9999; i > 10; i--) {
            Integer integer = Integer.valueOf(i);
            hashMap2.remove(integer.toString());
        }
        System.out.println("哈希桶长度: " + hashMap2.getTableLength() + ", size: " + hashMap2.size());
    }
}
