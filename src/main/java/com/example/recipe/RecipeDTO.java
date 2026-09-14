package com.example.recipe;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecipeDTO {
    private Long id;
    private String name;
    private String description;
    private String cuisine;
    private List<String> ingredients;
    private List<String> instructions;
}
