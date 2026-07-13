/**
 * TOPIC: Build-your-own HashMap — the definitive "explain HashMap internals by
 * writing one" answer. Array of buckets + chaining + resize, ~80 lines.
 */
public class MyHashMap<K, V> {

    private static final int INITIAL_CAPACITY = 16;      // power of 2 (like the real one)
    private static final float LOAD_FACTOR = 0.75f;

    /** A chain node — the real HashMap's Node<K,V> looks just like this. */
    private static class Node<K, V> {
        final int hash; final K key; V value; Node<K, V> next;
        Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash; this.key = key; this.value = value; this.next = next;
        }
    }

    private Node<K, V>[] table = newTable(INITIAL_CAPACITY);
    private int size;

    @SuppressWarnings("unchecked")
    private static <K, V> Node<K, V>[] newTable(int cap) { return (Node<K, V>[]) new Node[cap]; }

    /** Same bit-mixing trick as java.util.HashMap: spread high bits into low. */
    private static int hash(Object key) {
        int h;
        return key == null ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    private int indexFor(int hash) { return (table.length - 1) & hash; }  // cheap modulo

    public V put(K key, V value) {
        if (size + 1 > table.length * LOAD_FACTOR) resize();
        int h = hash(key), idx = indexFor(h);
        for (Node<K, V> n = table[idx]; n != null; n = n.next) {
            if (n.hash == h && java.util.Objects.equals(n.key, key)) {   // key exists -> replace
                V old = n.value; n.value = value; return old;
            }
        }
        table[idx] = new Node<>(h, key, value, table[idx]);              // head-insert into chain
        size++;
        return null;
    }

    public V get(K key) {
        int h = hash(key);
        for (Node<K, V> n = table[indexFor(h)]; n != null; n = n.next)
            if (n.hash == h && java.util.Objects.equals(n.key, key)) return n.value;
        return null;
    }

    public V remove(K key) {
        int h = hash(key), idx = indexFor(h);
        Node<K, V> prev = null;
        for (Node<K, V> n = table[idx]; n != null; prev = n, n = n.next) {
            if (n.hash == h && java.util.Objects.equals(n.key, key)) {
                if (prev == null) table[idx] = n.next; else prev.next = n.next;
                size--;
                return n.value;
            }
        }
        return null;
    }

    /** Double capacity and redistribute every node (what makes resize O(n)). */
    private void resize() {
        Node<K, V>[] old = table;
        table = newTable(old.length * 2);
        size = 0;
        for (Node<K, V> head : old)
            for (Node<K, V> n = head; n != null; n = n.next)
                put(n.key, n.value);
    }

    public int size() { return size; }

    // ===== demo =====
    public static void main(String[] args) {
        MyHashMap<String, Integer> map = new MyHashMap<>();
        for (int i = 0; i < 40; i++) map.put("key" + i, i);   // forces multiple resizes
        map.put("key5", 555);                                  // replace
        map.put(null, -1);                                     // null key -> bucket 0

        System.out.println("size      : " + map.size());      // 41
        System.out.println("key5      : " + map.get("key5")); // 555
        System.out.println("null key  : " + map.get(null));   // -1
        System.out.println("removed   : " + map.remove("key7"));
        System.out.println("after rm  : " + map.get("key7")); // null
        // Talking points while writing this in an interview:
        // 1. why power-of-2 capacity  2. hash spreading  3. equals vs == in chain walk
        // 4. resize cost & load factor  5. real HashMap treeifies chains > 8
    }
}
