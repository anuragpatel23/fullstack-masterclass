# 12 — IO, Serialization & Networking 🟡

## Real-life analogy
**Serialization is freeze-drying food**: the object (fresh meal) is converted to a storable/shippable format (bytes) and reconstituted later (deserialization). `transient` fields are the garnish you *don't* freeze-dry (passwords, derived caches) — they come back empty. `serialVersionUID` is the batch label: if the recipe changed since packaging, the label mismatch tells you not to trust the contents.

## IO essentials
- **Byte streams** (`InputStream/OutputStream` — binary) vs **character streams** (`Reader/Writer` — text with charset).
- Decorator stack: `new BufferedReader(new InputStreamReader(socket.getInputStream(), UTF_8))`.
- **Always buffer** file/network streams; always close (try-with-resources).
- **NIO / NIO.2**: `Path`, `Files` (readString, walk, copy) — modern default for file work; `ByteBuffer` + `Channel` + `Selector` for non-blocking IO (one thread, many connections — the idea behind Netty and why it matters vs thread-per-connection). ⭐ conceptual question.

## Serialization ⭐
- `implements Serializable` (marker interface — no methods; JVM checks it via `instanceof`). ⭐ "what's a marker interface?"
- **`serialVersionUID`**: declare it explicitly. If absent, compiler-generated from class structure → any change breaks old data with `InvalidClassException`.
- **`transient`**: skip a field (secrets, caches, non-serializable members) → default value on read.
- `static` fields are never serialized (class-level, not object state).
- Parent not Serializable? Its no-arg constructor runs on deserialization.
- Custom hooks: `writeObject/readObject` (encryption, validation), `readResolve` (protect singletons ⭐), `writeReplace`.
- Deserialization is a **security risk** (gadget chains) — validate, use `ObjectInputFilter` (Java 9+), or prefer JSON/protobuf. Modern take interviewers like: "Java native serialization is effectively legacy; we serialize via Jackson."

## Networking basics
- TCP (`Socket`/`ServerSocket` — reliable, ordered) vs UDP (`DatagramSocket` — fast, lossy).
- HTTP layering: `HttpClient` (Java 11) with sync/async API.
- Know conceptually: blocking IO = thread per connection; NIO multiplexing = event loop; virtual threads (21) make blocking style scalable again.

## Top interview questions
1. **What is a marker interface? Name three.** `Serializable`, `Cloneable`, `RandomAccess` — signal to JVM/libraries via type, no methods.
2. **What happens without serialVersionUID?** Auto-computed; class evolution → InvalidClassException on old data.
3. **transient vs static in serialization?** Both skipped — transient by choice (instance), static because it's not object state.
4. **How to serialize an object with a non-serializable field?** Mark transient + rebuild in `readObject`, or wrap/convert.
5. **How does deserialization break Singleton and how to fix?** New instance created bypassing constructor → implement `readResolve()` returning the singleton.
6. **IO vs NIO?** Stream-blocking-one-thread-per-conn vs buffer-channel-selector multiplexing.
7. **Deep copy via serialization?** Serialize + deserialize = deep clone (slow but easy) — or copy constructors/mapping libs.

➡️ Code: [`IoSerializationDemo.java`](./IoSerializationDemo.java)
