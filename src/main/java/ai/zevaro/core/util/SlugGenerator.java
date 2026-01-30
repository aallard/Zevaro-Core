package ai.zevaro.core.util;

import org.springframework.stereotype.Component;

import java.util.function.Predicate;

@Component
public class SlugGenerator {

    public String generateSlug(String input) {
        if (input == null) {
            return "";
        }
        return input.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    public String generateUniqueSlug(String input, Predicate<String> existsCheck) {
        String baseSlug = generateSlug(input);
        String slug = baseSlug;
        int counter = 1;
        while (existsCheck.test(slug)) {
            slug = baseSlug + "-" + counter++;
        }
        return slug;
    }
}
