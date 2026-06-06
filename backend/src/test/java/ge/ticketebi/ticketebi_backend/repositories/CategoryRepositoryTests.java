package ge.ticketebi.ticketebi_backend.repositories;

import ge.ticketebi.ticketebi_backend.config.PostgresTestContainer;
import ge.ticketebi.ticketebi_backend.domain.entities.CategoryEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class CategoryRepositoryTests {

    static {
        PostgresTestContainer.getInstance().start();
    }

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        PostgresTestContainer container = PostgresTestContainer.getInstance();
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
    }

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void whenCategoryExists_findByName_shouldReturnCategory() {
        CategoryEntity category = new CategoryEntity();
        category.setName("Football");
        CategoryEntity savedCategory = categoryRepository.save(category);

        Optional<CategoryEntity> found = categoryRepository.findByName("Football");

        assertTrue(found.isPresent(), "Category should be found by name 'Football'");
        assertEquals(savedCategory.getId(), found.get().getId(), "The IDs should match");
    }

    @Test
    void whenCategoryDoesNotExist_findByName_shouldReturnEmpty() {
        Optional<CategoryEntity> found = categoryRepository.findByName("Music");

        assertFalse(found.isPresent(), "Category should not be found for name 'Music'");
    }
}
