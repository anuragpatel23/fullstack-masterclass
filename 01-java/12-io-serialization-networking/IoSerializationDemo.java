/**
 * TOPIC: IO & Serialization — transient, serialVersionUID, readResolve singleton
 * protection, deep copy via serialization, NIO.2 file ops, Java 11 HttpClient.
 */
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class IoSerializationDemo {

    public static void main(String[] args) throws Exception {
        serializationBasics();
        singletonReadResolve();
        deepCopyTrick();
        nioFileOps();
        // httpClientDemo();  // uncomment with network access
    }

    // ===== Serializable user with transient secret =====
    static class User implements Serializable {
        @Serial private static final long serialVersionUID = 1L;   // ALWAYS declare explicitly
        String name;
        transient String password;                                  // never persisted
        static String appVersion = "2.0";                           // static: not object state
        User(String name, String password) { this.name = name; this.password = password; }
        @Override public String toString() { return "User[%s, pwd=%s]".formatted(name, password); }
    }

    static byte[] serialize(Object o) throws IOException {
        var bos = new ByteArrayOutputStream();
        try (var oos = new ObjectOutputStream(bos)) { oos.writeObject(o); }
        return bos.toByteArray();
    }
    @SuppressWarnings("unchecked")
    static <T> T deserialize(byte[] bytes) throws Exception {
        try (var ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return (T) ois.readObject();
        }
    }

    static void serializationBasics() throws Exception {
        User u = new User("shilpak", "secret123");
        User restored = deserialize(serialize(u));
        System.out.println("original : " + u);
        System.out.println("restored : " + restored);   // pwd=null — transient skipped ✔
    }

    // ===== readResolve: keep Singleton a singleton through deserialization =====
    static class ConfigManager implements Serializable {
        @Serial private static final long serialVersionUID = 1L;
        private static final ConfigManager INSTANCE = new ConfigManager();
        private ConfigManager() {}
        static ConfigManager getInstance() { return INSTANCE; }
        @Serial private Object readResolve() { return INSTANCE; }  // ⭐ returns canonical instance
    }
    static void singletonReadResolve() throws Exception {
        ConfigManager copy = deserialize(serialize(ConfigManager.getInstance()));
        System.out.println("singleton preserved: " + (copy == ConfigManager.getInstance())); // true
    }

    // ===== Deep copy via serialization =====
    static void deepCopyTrick() throws Exception {
        record Address(String city) implements Serializable {}
        // (record components are final, but list contents show the deep-ness)
        ArrayList<String> tags = new ArrayList<>(List.of("vip"));
        HashMap<String, Object> original = new HashMap<>(Map.of("addr", new Address("Pune"), "tags", tags));
        HashMap<String, Object> deep = deserialize(serialize(original));
        tags.add("mutated-after-copy");
        System.out.println("deep copy unaffected: " + deep.get("tags"));   // [vip]
    }

    // ===== NIO.2: the modern way to do file IO =====
    static void nioFileOps() throws IOException {
        Path dir = Files.createTempDirectory("nio-demo");
        Path file = dir.resolve("notes.txt");

        Files.writeString(file, "line1\nline2\nline3");             // write
        System.out.println("read back: " + Files.readString(file).lines().count() + " lines");

        try (var stream = Files.walk(dir)) {                        // walk a tree
            stream.filter(Files::isRegularFile)
                  .forEach(p -> System.out.println("found: " + p.getFileName()));
        }
        Files.deleteIfExists(file); Files.deleteIfExists(dir);
    }

    // ===== Java 11 HttpClient (sync + async) =====
    static void httpClientDemo() throws Exception {
        var client = java.net.http.HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(5)).build();
        var request = java.net.http.HttpRequest.newBuilder()
            .uri(java.net.URI.create("https://httpbin.org/get")).GET().build();

        // Sync
        var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
        System.out.println("status: " + response.statusCode());

        // Async — returns CompletableFuture (ties into the concurrency topic)
        client.sendAsync(request, java.net.http.HttpResponse.BodyHandlers.ofString())
              .thenApply(java.net.http.HttpResponse::statusCode)
              .thenAccept(code -> System.out.println("async status: " + code))
              .join();
    }
}
