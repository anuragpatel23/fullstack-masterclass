/**
 * TOPIC: Testing — one example of each layer: pure unit (Mockito),
 * @WebMvcTest + MockMvc, @DataJpaTest, @SpringBootTest + Testcontainers.
 */
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class TestingDemo {

    // (Assume these exist from earlier topics)
    record Product(Long id, String name, double price) {}
    interface ProductRepository { Optional<Product> findById(Long id); Product save(Product p); }
    static class ProductService {
        private final ProductRepository repo;
        ProductService(ProductRepository repo) { this.repo = repo; }   // constructor DI = testable
        Product applyDiscount(Long id, double pct) {
            var p = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("not found"));
            if (pct < 0 || pct > 50) throw new IllegalArgumentException("bad discount");
            var discounted = new Product(p.id(), p.name(), p.price() * (1 - pct / 100));
            return repo.save(discounted);
        }
    }

    // =====================================================================
    // LAYER 1: pure unit test — no Spring, milliseconds ⭐
    // =====================================================================
    @ExtendWith(MockitoExtension.class)
    static class ProductServiceTest {

        @Mock ProductRepository repo;               // fake dependency
        @InjectMocks ProductService service;        // real class under test

        @Test
        void appliesDiscountCorrectly() {
            when(repo.findById(1L)).thenReturn(Optional.of(new Product(1L, "Laptop", 1000)));
            when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));  // echo back

            Product result = service.applyDiscount(1L, 10);

            assertThat(result.price()).isEqualTo(900.0);

            // ArgumentCaptor ⭐: inspect exactly what was saved
            var captor = ArgumentCaptor.forClass(Product.class);
            verify(repo).save(captor.capture());
            assertThat(captor.getValue().name()).isEqualTo("Laptop");
        }

        @Test
        void rejectsInvalidDiscount() {
            when(repo.findById(1L)).thenReturn(Optional.of(new Product(1L, "Laptop", 1000)));
            assertThatThrownBy(() -> service.applyDiscount(1L, 90))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("bad discount");
            verify(repo, never()).save(any());       // side-effect must NOT happen
        }
    }

    // =====================================================================
    // LAYER 2: @WebMvcTest — controller slice with MockMvc ⭐
    // =====================================================================
    // @WebMvcTest(ProductController.class)
    static class ProductControllerTest {

        @Autowired MockMvc mockMvc;                  // simulated servlet layer — no real server
        @MockBean ProductService service;            // service replaced in the slice context

        @Test
        @WithMockUser(roles = "USER")                // security in tests ⭐
        void returnsProductJson() throws Exception {
            when(service.applyDiscount(1L, 10))
                    .thenReturn(new Product(1L, "Laptop", 900));

            mockMvc.perform(post("/api/v1/products/1/discount?pct=10")
                            .with(org.springframework.security.test.web.servlet.request
                                  .SecurityMockMvcRequestPostProcessors.csrf()))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.name").value("Laptop"))
                   .andExpect(jsonPath("$.price").value(900.0));
        }

        @Test
        void validationErrorGives400() throws Exception {
            mockMvc.perform(post("/api/v1/products")
                            .contentType("application/json")
                            .content("{\"name\":\"\",\"price\":-5}"))   // violates @NotBlank/@Positive
                   .andExpect(status().isBadRequest())
                   .andExpect(jsonPath("$.fieldErrors.name").exists());
        }
    }

    // =====================================================================
    // LAYER 3: @DataJpaTest — repository slice, rolls back each test
    // =====================================================================
    // @DataJpaTest  // in-memory DB by default; pair with Testcontainers for realism
    static class ProductRepositoryTest {
        // @Autowired ProductJpaRepository repo;
        // @Autowired TestEntityManager em;
        @Test
        void customQueryWorks() {
            // em.persist(new ProductEntity("Laptop", 1000));
            // assertThat(repo.findByPriceGreaterThan(500)).hasSize(1);
            // Each test runs in a tx that ROLLS BACK -> clean DB between tests ⭐
        }
    }

    // =====================================================================
    // LAYER 4: full integration with Testcontainers ⭐ (the modern answer)
    // =====================================================================
    // @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    // @Testcontainers
    static class CheckoutFlowIT {
        /*
        @Container @ServiceConnection                     // Boot 3.1+: auto-configures datasource URL
        static OracleContainer oracle = new OracleContainer("gvenzl/oracle-free:23-slim");

        @Autowired TestRestTemplate rest;

        @Test
        void placeOrderEndToEnd() {
            var response = rest.postForEntity("/api/v1/orders",
                    new CreateOrderRequest("Laptop", 1, "a@b.com"), OrderResponse.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            // asserts against a REAL Oracle — sequences, dialect, locking all genuine ⭐
        }
        */
    }
}
