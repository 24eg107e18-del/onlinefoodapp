package com.example.foodordering.config;

import com.example.foodordering.model.Category;
import com.example.foodordering.model.FoodItem;
import com.example.foodordering.model.Role;
import com.example.foodordering.model.User;
import com.example.foodordering.repository.CategoryRepository;
import com.example.foodordering.repository.FoodItemRepository;
import com.example.foodordering.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final FoodItemRepository foodItemRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Value("${app.admin.name:System Administrator}")
    private String adminName;

    @Value("${app.admin.phone:1234567890}")
    private String adminPhone;

    @Value("${app.admin.address:100 Restaurant Avenue, Food Hub}")
    private String adminAddress;

    public DataInitializer(UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           FoodItemRepository foodItemRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.foodItemRepository = foodItemRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        try {
            seedAdminUser();
            seedSampleCategoriesAndFoods();
        } catch (Exception e) {
            logger.warn("Data initialization skipped or encountered error (e.g. database not reachable yet): {}", e.getMessage());
        }
    }

    private void seedAdminUser() {
        if (StringUtils.hasText(adminEmail) && StringUtils.hasText(adminPassword)) {
            String normalizedEmail = adminEmail.trim().toLowerCase();
            if (!userRepository.existsByEmail(normalizedEmail)) {
                User admin = new User(
                        null,
                        adminName,
                        normalizedEmail,
                        passwordEncoder.encode(adminPassword),
                        adminPhone,
                        adminAddress,
                        Role.ADMIN
                );
                userRepository.save(admin);
                logger.info("Admin bootstrap account successfully initialized for: {}", normalizedEmail);
            } else {
                logger.debug("Admin account already exists for: {}", normalizedEmail);
            }
        } else {
            logger.info("No ADMIN_EMAIL and ADMIN_PASSWORD supplied. Skipping admin account bootstrap.");
        }
    }

    private void seedSampleCategoriesAndFoods() {
        if (categoryRepository.count() == 0) {
            logger.info("Categories collection is empty. Populating default food categories and menu items...");

            Category pizzaCat = categoryRepository.save(new Category(
                    null,
                    "Pizza & Italian",
                    "Artisan wood-fired pizzas, garlic breads and authentic Italian pastas",
                    "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=600&auto=format&fit=crop&q=80",
                    true
            ));

            Category burgerCat = categoryRepository.save(new Category(
                    null,
                    "Burgers & Sandwiches",
                    "Gourmet handcrafted burgers, crispy chicken and artisanal sandwiches",
                    "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=600&auto=format&fit=crop&q=80",
                    true
            ));

            Category biryaniCat = categoryRepository.save(new Category(
                    null,
                    "Biryani & Bowls",
                    "Rich aromatic Dum Biryani, fragrant basmati rice and wholesome curry bowls",
                    "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=600&auto=format&fit=crop&q=80",
                    true
            ));

            Category dessertCat = categoryRepository.save(new Category(
                    null,
                    "Desserts & Shakes",
                    "Decadent chocolate cakes, thick shakes, smoothies and ice creams",
                    "https://images.unsplash.com/photo-1551024709-8f23befc6f87?w=600&auto=format&fit=crop&q=80",
                    true
            ));

            Category beverageCat = categoryRepository.save(new Category(
                    null,
                    "Beverages & Refreshers",
                    "Freshly brewed coffees, mocktails, iced teas and sparkling coolers",
                    "https://images.unsplash.com/photo-1517256064527-09c73fc73e38?w=600&auto=format&fit=crop&q=80",
                    true
            ));

            // Seed food items
            List<FoodItem> items = List.of(
                    new FoodItem(null, "Truffle Mushroom Pizza", "Loaded with sautéed wild mushrooms, white truffle oil, mozzarella and fresh thyme",
                            pizzaCat.getId(), new BigDecimal("14.99"),
                            "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=600&auto=format&fit=crop&q=80", true),
                    new FoodItem(null, "Classic Margherita Pizza", "San Marzano tomatoes, fresh buffalo mozzarella, virgin olive oil and basil leaves",
                            pizzaCat.getId(), new BigDecimal("11.50"),
                            "https://images.unsplash.com/photo-1604382355076-af4b0eb60143?w=600&auto=format&fit=crop&q=80", true),
                    new FoodItem(null, "Spicy Pepperoni Feast", "Double artisan pepperoni slices, crushed red chili flakes, rich tomato marinara",
                            pizzaCat.getId(), new BigDecimal("15.50"),
                            "https://images.unsplash.com/photo-1628840042765-356cda07504e?w=600&auto=format&fit=crop&q=80", true),

                    new FoodItem(null, "Double Angus Smash Burger", "Two juicy smashed beef patties, melted sharp cheddar, caramelized onions and secret sauce",
                            burgerCat.getId(), new BigDecimal("12.99"),
                            "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=600&auto=format&fit=crop&q=80", true),
                    new FoodItem(null, "Crispy Nashville Chicken Burger", "Spicy buttermilk fried chicken breast, dill pickles, cool creamy slaw in a brioche bun",
                            burgerCat.getId(), new BigDecimal("11.99"),
                            "https://images.unsplash.com/photo-1625813506062-0aeb1d7a094b?w=600&auto=format&fit=crop&q=80", true),
                    new FoodItem(null, "Avocado Garden Veggie Burger", "Grilled spiced black bean patty, Hass avocado, tomato relish and garlic aioli",
                            burgerCat.getId(), new BigDecimal("10.50"),
                            "https://images.unsplash.com/photo-1520072959219-c595dc870360?w=600&auto=format&fit=crop&q=80", true),

                    new FoodItem(null, "Royal Hyderabadi Dum Biryani", "Slow-cooked fragrant basmati rice layered with tender spiced meat and saffron aroma",
                            biryaniCat.getId(), new BigDecimal("16.99"),
                            "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=600&auto=format&fit=crop&q=80", true),
                    new FoodItem(null, "Paneer Tikka Biryani Bowl", "Smoked tandoori paneer cubes gently tossed with rich spiced saffron rice and fried onions",
                            biryaniCat.getId(), new BigDecimal("13.50"),
                            "https://images.unsplash.com/photo-1645177628172-a94c1f96e6db?w=600&auto=format&fit=crop&q=80", true),

                    new FoodItem(null, "Warm Molten Lava Cake", "Gooey Belgian chocolate center served with rich cocoa dusting",
                            dessertCat.getId(), new BigDecimal("6.99"),
                            "https://images.unsplash.com/photo-1606313564200-e75d5e30476c?w=600&auto=format&fit=crop&q=80", true),
                    new FoodItem(null, "New York Strawberry Cheesecake", "Creamy traditional cheesecake with fresh strawberry glaze and graham crust",
                            dessertCat.getId(), new BigDecimal("7.50"),
                            "https://images.unsplash.com/photo-1533134242443-d4fd215305ad?w=600&auto=format&fit=crop&q=80", true),

                    new FoodItem(null, "Iced Caramel Macchiato", "Freshly pulled espresso layered over silky chilled milk and buttery caramel drizzle",
                            beverageCat.getId(), new BigDecimal("4.99"),
                            "https://images.unsplash.com/photo-1517256064527-09c73fc73e38?w=600&auto=format&fit=crop&q=80", true),
                    new FoodItem(null, "Mint Lime Mojito Cooler", "Crushed garden mint, freshly squeezed lime, cane sugar and sparkling soda",
                            beverageCat.getId(), new BigDecimal("4.50"),
                            "https://images.unsplash.com/photo-1551024709-8f23befc6f87?w=600&auto=format&fit=crop&q=80", true)
            );

            foodItemRepository.saveAll(items);
            logger.info("Successfully seeded {} default food items.", items.size());
        }
    }
}
