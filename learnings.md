# 🧠 Interview & Architecture Learnings (Q&A Key Points)

---

### Q1: What if `ObjectStore` is implemented by multiple classes? Which one will Spring inject?
* **Default Behavior:** Spring throws `NoUniqueBeanDefinitionException` at startup because it cannot decide between multiple candidates.
* **Key Solutions:**
  1. **`@Primary`:** Mark one default implementation (e.g., `@Primary @Component public class MinioObjectStore`).
  2. **`@Qualifier("beanName")`:** Explicitly specify the target bean name in constructor injection.
  3. **`@ConditionalOnProperty`:** Conditionally load one implementation based on `application.yaml` properties.
  4. **Registry / Map Injection (Distributed Pattern):** Inject `Map<String, ObjectStore>` or use a `StorageCluster` registry to hold multiple node instances and route dynamically by `nodeId`.

---

### Q2: Can we directly create objects using `new` instead of Spring beans in some cases?
* **Yes:** Direct instantiation using `new` inside a **Factory Pattern** or `@Configuration` class is standard when dealing with dynamic instances.
* **When to use `@Component`:** For static singletons (services, controllers, metadata repositories).
* **When to use `new`:** For dynamic multi-node topologies (e.g., looping through YAML configs to instantiate `new MinioObjectStore(client, bucket)` for Node-1, Node-2, Node-3).
* **Important Rule:** Objects created with `new` are not managed by Spring's container; always pass dependencies explicitly via their constructor.

---

### Q3: What does `@Bean` do? Does it replace a bean created using a default constructor?
* **What `@Bean` does:** Tells Spring to run that method and register whatever object is returned into the ApplicationContext.
* **Constructor control:** Spring does **not** call constructors on its own with `@Bean`—**you** explicitly write the construction logic (`new MyClass(...)` or `.builder()`) inside the method.
* **Overlap:** Do not put `@Component` on a class AND create a `@Bean` method returning that same class (causes duplicate bean conflicts).

---

### Q4: In `@Bean public StorageCluster storageCluster(ClusterProperties properties)`, who provides `properties`?
* **Spring provides it automatically via Dependency Injection.**
* **Mechanism:** When Spring calls the `@Bean` method, it inspects the method parameters, finds the matching `ClusterProperties` bean in the ApplicationContext (populated from `application.yaml` via `@ConfigurationProperties`), and passes it as an argument.

---

### Q5: What happens with circular dependencies (A → B → C → A)? How does DI work or will Spring fail?
* **Default Behavior:** Spring Boot (2.6+ / 3+) **fails at startup** with `BeanCurrentlyInCreationException` (cycles are blocked by default).
* **Why it fails with Constructor Injection:** The JVM cannot physically instantiate any class because each constructor requires the next one to already exist.
* **Key Solutions:**
  1. **Refactor / Extract Shared Logic (Best Practice):** Eliminate the cycle by moving common logic into a third service `D` (Single Responsibility Principle).
  2. **`@Lazy`:** Injects a dynamic runtime proxy placeholder, deferring actual object construction until its first method call.
  3. **Spring Events:** Decouple classes using `ApplicationEventPublisher` and `@EventListener` instead of direct bean injection.

