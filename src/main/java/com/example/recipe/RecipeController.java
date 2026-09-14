package com.example.recipe;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recipes")
@CrossOrigin(origins = "http://localhost:5173")
public class RecipeController {

    @Autowired
    private RecipeService recipeService;

    // Generate a recipe from a list of ingredients and cuisine
    // Returns the recipe as a RecipeDTO so ingredients and instructions are arrays
    @PostMapping("/generate")
    public RecipeDTO generateRecipe(@RequestBody Map<String, Object> body) {
        List<String> ingredients = (List<String>) body.get("ingredients");
        String cuisine = (String) body.getOrDefault("cuisine", "Any");
        Recipe recipe = recipeService.generateRecipe(ingredients, cuisine);
        return toDTO(recipe);
    }

    // Save a recipe sent from the frontend
    @PostMapping("/save")
    public RecipeDTO saveRecipe(@RequestBody RecipeDTO dto) {
        Recipe recipe = new Recipe();
        recipe.setName(dto.getName());
        recipe.setDescription(dto.getDescription());
        recipe.setCuisine(dto.getCuisine());
        recipe.setIngredients(String.join("||", dto.getIngredients()));
        recipe.setInstructions(String.join("||", dto.getInstructions()));
        Recipe saved = recipeService.saveRecipe(recipe);
        return toDTO(saved);
    }

    // Get all saved recipes
    @GetMapping("/all")
    public List<RecipeDTO> getAllRecipes() {
        return recipeService.getAllRecipes().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Delete a recipe by id
    @DeleteMapping("/delete/{id}")
    public void deleteRecipe(@PathVariable Long id) {
        recipeService.deleteRecipe(id);
    }

    // Converts a Recipe entity to a RecipeDTO
    // Splits the stored string back into arrays for the frontend
    private RecipeDTO toDTO(Recipe recipe) {
        RecipeDTO dto = new RecipeDTO();
        dto.setId(recipe.getId());
        dto.setName(recipe.getName());
        dto.setDescription(recipe.getDescription());
        dto.setCuisine(recipe.getCuisine());
        dto.setIngredients(recipe.getIngredients() != null
                ? Arrays.asList(recipe.getIngredients().split("\\|\\|"))
                : List.of());
        dto.setInstructions(recipe.getInstructions() != null
                ? Arrays.asList(recipe.getInstructions().split("\\|\\|"))
                : List.of());
        return dto;
    }
}
