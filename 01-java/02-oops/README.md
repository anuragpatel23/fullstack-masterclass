# 02 — OOPs in Java 🟢⭐

## Real-life analogy
A **car**: you use the steering wheel and pedals (**abstraction** — you don't know the engine internals), the engine is sealed under the hood (**encapsulation** — controlled access), an EV and a petrol car both "drive" but differently (**polymorphism**), and both inherit common traits from "Vehicle" (**inheritance**). A car *has an* engine rather than *being* an engine (**composition over inheritance**).

## The 4 pillars

| Pillar | What | How in Java |
|---|---|---|
| **Encapsulation** | Hide state, expose behavior | private fields + getters/setters, records |
| **Abstraction** | Hide *implementation*, show *contract* | interfaces, abstract classes |
| **Inheritance** | IS-A reuse | `extends` (single class), `implements` (multiple interfaces) |
| **Polymorphism** | One interface, many forms | overriding (runtime), overloading (compile-time) |

## Must-know details ⭐

### Overloading vs Overriding
- **Overloading**: same name, different parameter list, resolved at **compile time** (static polymorphism). Return type alone can't differentiate.
- **Overriding**: subclass redefines an inherited method, resolved at **runtime** via dynamic dispatch. Rules: same signature, covariant return allowed, can't reduce visibility, can't throw broader checked exceptions. `private`, `static`, `final` methods can't be overridden (static = *hiding*, not overriding).

### Abstract class vs Interface
| | Abstract class | Interface |
|---|---|---|
| State | instance fields ✔ | only `public static final` constants |
| Constructor | ✔ | ✘ |
| Methods | any | abstract + `default` + `static` + `private` (9+) |
| Inheritance | single | multiple |
| Use when | shared state/skeleton (template) | capability/contract (Comparable, Runnable) |

### Composition vs Inheritance ⭐
Prefer **composition** (HAS-A): looser coupling, testable, no fragile-base-class problem, swap behavior at runtime. Inheritance leaks the parent's implementation to children.

### SOLID (asked at every product company)
- **S**ingle Responsibility — one reason to change.
- **O**pen/Closed — open to extension (new subclass/strategy), closed to modification.
- **L**iskov Substitution — subtype must be usable wherever the parent is (Square-extends-Rectangle violation).
- **I**nterface Segregation — many small interfaces > one fat one.
- **D**ependency Inversion — depend on abstractions; this is what Spring DI implements.

## Top interview questions
1. **Why doesn't Java support multiple inheritance of classes?** Diamond problem — ambiguity of inherited state/behavior. Interfaces with `default` methods reintroduce it, but the compiler forces you to override and resolve.
2. **Can a constructor be overridden?** No — constructors aren't inherited. They can be overloaded.
3. **What is the diamond problem with default methods?** Two interfaces provide same default → class must override and may call `InterfaceA.super.method()`.
4. **Runtime vs compile-time polymorphism?** Overriding (vtable dispatch on actual object type) vs overloading (chosen from reference type at compile time).
5. **Why favor composition?** See above; also mock-friendly and honors LSP more easily.
6. **Is Java 100% object-oriented?** No — primitives are not objects.
7. **Can we instantiate an abstract class?** No, but anonymous subclasses are allowed: `new AbstractType() { ... }`.
8. **What is method hiding?** A static method with the same signature in the subclass hides (doesn't override) the parent's — resolved by reference type.

➡️ Code: [`OopsDemo.java`](./OopsDemo.java)
