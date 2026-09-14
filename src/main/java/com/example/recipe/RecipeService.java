package com.example.recipe;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RecipeService {

    @Autowired
    private RecipeRepository recipeRepository;

    public Recipe generateRecipe(List<String> ingredients, String cuisine) {

        String ingredientList = String.join(", ", ingredients);

        String prompt = "I have these ingredients: " + ingredientList + ". " +
                        "Suggest a recipe. Cuisine: " + cuisine + ". " +
                        "Respond ONLY with a JSON object in this exact format with no extra text, no markdown, no code blocks: " +
                        "{\"name\": \"Recipe Name\", " +
                        "\"description\": \"One sentence description\", " +
                        "\"ingredients\": [\"ingredient 1\", \"ingredient 2\"], " +
                        "\"instructions\": [\"Step 1\", \"Step 2\"]}";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Map.of(
            "model", "gemma-2b-instruct",
            "messages", List.of(
                Map.of("role", "system", "content", "You are a helpful chef. Always respond with valid JSON only, no extra text."),
                Map.of("role", "user", "content", prompt)
            )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        Map response = restTemplate.postForObject(
            "http://localhost:1234/v1/chat/completions",
            request,
            Map.class
        );

        List<Map> choices = (List<Map>) response.get("choices");
        Map message = (Map) choices.get(0).get("message");
        String content = (String) message.get("content");

        // Parse the JSON string returned by the AI into a Recipe object
        try {
            // Parse the JSON response manually without ObjectMapper
            Recipe recipe = new Recipe();
            recipe.setCuisine(cuisine);

            // Extract name
            String name = content.split("\"name\":\\s*\"")[1].split("\"")[0];
            recipe.setName(name);

            // Extract description
            String description = content.split("\"description\":\\s*\"")[1].split("\"")[0];
            recipe.setDescription(description);

            // Extract ingredients array
            String ingredientsRaw = content.split("\"ingredients\":\\s*\\[")[1].split("\\]")[0];
            List<String> ingredientsList = Arrays.stream(ingredientsRaw.split(","))
                .map(s -> s.trim().replace("\"", ""))
                .collect(java.util.stream.Collectors.toList());
            recipe.setIngredients(String.join("||", ingredientsList));

            // Extract instructions array
            String instructionsRaw = content.split("\"instructions\":\\s*\\[")[1].split("\\]")[0];
            List<String> instructionsList = Arrays.stream(instructionsRaw.split(","))
                .map(s -> s.trim().replace("\"", ""))
                .collect(java.util.stream.Collectors.toList());
            recipe.setInstructions(String.join("||", instructionsList));

            return recipe;

        } catch (Exception e) {
            // If parsing fails, return a basic recipe with the raw content
            Recipe recipe = new Recipe();
            recipe.setName("Generated Recipe");
            recipe.setDescription(content);
            recipe.setIngredients(ingredientList);
            recipe.setInstructions("See description");
            recipe.setCuisine(cuisine);
            return recipe;
        }
    }

    public Recipe saveRecipe(Recipe recipe) {
        return recipeRepository.save(recipe);
    }

    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }

    public void deleteRecipe(Long id) {
        recipeRepository.deleteById(id);
    }
}
