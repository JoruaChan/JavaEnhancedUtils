package cn.joruachan.utils;

import sun.misc.Unsafe;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * 原子字符类 <br>
 * 原子式更新字符, int类型通过{@link Unsafe}来支持原子式更新；
 * 内部通过char和int互转；
 *
 * @author JoruaChan
 * @phone 16602103479
 * @contact joruachan@gmail.com
 */
public class AtomicCharacter implements Serializable {
    private static final long serialVersionUID = -5080769349245906188L;

    private static final Unsafe unsafe;
    private static final long valueOffset;

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);

            valueOffset = unsafe.objectFieldOffset
                    (AtomicCharacter.class.getDeclaredField("value"));
        } catch (Exception ex) {
            throw new Error(ex);
        }
    }

    private volatile int value;

    private static final int char2Int(final char charValue) {
        return charValue;
    }

    private static final char int2Char(final int intValue) {
        return (char) intValue;
    }

    public AtomicCharacter() {
    }

    public AtomicCharacter(char initChar) {
        this.value = char2Int(initChar);
    }

    public final char get() {
        return int2Char(this.value);
    }

    public final boolean compareAndSet(char expect, char update) {
        return unsafe.compareAndSwapInt(this, valueOffset, char2Int(expect), char2Int(update));
    }

    public final void lazySet(char newValue) {
        unsafe.putOrderedInt(this, valueOffset, char2Int(newValue));
    }

    public final char getAndSet(char newValue) {
        char oldValue;

        do {
            oldValue = get();
        } while (!compareAndSet(oldValue, newValue));
        return oldValue;
    }

    @Override
    public String toString() {
        return Character.toString(int2Char(value));
    }
}
