package be.kuleuven.foodrestservice.controllers;

import be.kuleuven.foodrestservice.domain.Meal;
import be.kuleuven.foodrestservice.domain.MealsRepository;
import be.kuleuven.foodrestservice.exceptions.MealNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
public class MealsRestController {

    private final MealsRepository mealsRepository;

    @Autowired
    MealsRestController(MealsRepository mealsRepository) {
        this.mealsRepository = mealsRepository;
    }

    @Operation(summary = "Get a meal by its id", description = "Get a meal by id description")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the meal",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Meal.class))}),
            @ApiResponse(responseCode = "404", description = "Meal not found", content = @Content)})
    @GetMapping("/rest/meals/{id}")
    ResponseEntity<?> getMealById(
            @Parameter(description = "Id of the meal", schema = @Schema(format = "uuid", type = "string"))
            @PathVariable String id) {
        Meal meal = mealsRepository.findMeal(id).orElseThrow(() -> new MealNotFoundException(id));
        EntityModel<Meal> mealEntityModel = mealToEntityModel(id, meal);
        return ResponseEntity.ok(mealEntityModel);
    }

    @GetMapping("/rest/meals")
    CollectionModel<EntityModel<Meal>> getMeals() {
        Collection<Meal> meals = mealsRepository.getAllMeal();

        List<EntityModel<Meal>> mealEntityModels = new ArrayList<>();
        for (Meal m : meals) {
            EntityModel<Meal> em = mealToEntityModel(m.getId(), m);
            mealEntityModels.add(em);
        }
        return CollectionModel.of(mealEntityModels,
                linkTo(methodOn(MealsRestController.class).getMeals()).withSelfRel());
    }

    @Operation(summary = "Get the cheapest meal", description = "Get the meal with the lowest price")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the cheapest meal",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Meal.class))}),
            @ApiResponse(responseCode = "404", description = "Not a single meal found", content = @Content)})
    @GetMapping("/rest/meals/cheapest")
    ResponseEntity<?> getCheapestMeal() {
        Optional<Meal> optMeal = mealsRepository.getCheapestMeal();
        if(optMeal.isEmpty()){
            throw new MealNotFoundException("Cheapest meal not found");
        }
        Meal meal = optMeal.get();
        EntityModel<Meal> mealEntityModel = mealToEntityModel(meal.getId(), meal);
        return ResponseEntity.ok(mealEntityModel);
    }

    @Operation(summary = "Get the largest meal", description = "Get the meal with the most kcal")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the largest meal",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Meal.class))}),
            @ApiResponse(responseCode = "404", description = "Not a single meal found", content = @Content)})
    @GetMapping("/rest/meals/largest")
    ResponseEntity<?> getLargestMeal() {
        Optional<Meal> optMeal = mealsRepository.getLargestMeal();
        if(optMeal.isEmpty()){
            throw new MealNotFoundException("Largest meal not found");
        }
        Meal meal = optMeal.get();
        EntityModel<Meal> mealEntityModel = mealToEntityModel(meal.getId(), meal);
        return ResponseEntity.ok(mealEntityModel);
    }

    @Operation(summary = "Update meal based on id", description = "Change meal based on id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Meal updated",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Meal.class))}),
            @ApiResponse(responseCode = "404", description = "No meal found based on id", content = @Content)})
    @PutMapping("/rest/meals/{id}")
    ResponseEntity<?> putMeal(@PathVariable String id,@RequestBody Meal updatedMeal){
        Optional<Meal> meal = mealsRepository.findMeal(id);
        meal.orElseThrow(() -> new MealNotFoundException(id));
        mealsRepository.updateMeal(id,updatedMeal);
        EntityModel<Meal> mealEntityModel = mealToEntityModel(id, updatedMeal);
        return ResponseEntity.ok(mealEntityModel);
    }


    @Operation(summary = "Add new meal based", description = "Create a meal based")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Meal created",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Meal.class))}),
            @ApiResponse(responseCode = "404", description = "No meal found based on id", content = @Content)})
    @PostMapping("/rest/meals/")
    ResponseEntity<?> postMeal(@RequestBody Meal newMeal){
        mealsRepository.addNewMeal(newMeal);
        EntityModel<Meal> mealEntityModel = mealToEntityModel(newMeal.getId(), newMeal);
        return ResponseEntity.created(URI.create("/rest/meals/"+newMeal.getId())).body(mealEntityModel);
    }

    @Operation(summary = "Delete meal based on id", description = "Remove meal based on")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Meal deleted",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Meal.class))}),
            @ApiResponse(responseCode = "404", description = "No meal found based on id", content = @Content)})
    @DeleteMapping("/rest/meals/{id}")
    CollectionModel<EntityModel<Meal>> deleteMeal(@PathVariable String id){
        Optional<Meal> meal = mealsRepository.findMeal(id);
        meal.orElseThrow(() -> new MealNotFoundException(id));
        mealsRepository.deleteMeal(id);

        Collection<Meal> meals = mealsRepository.getAllMeal();

        List<EntityModel<Meal>> mealEntityModels = new ArrayList<>();
        for (Meal m : meals) {
            EntityModel<Meal> em = mealToEntityModel(m.getId(), m);
            mealEntityModels.add(em);
        }
        return CollectionModel.of(mealEntityModels,
                linkTo(methodOn(MealsRestController.class).getMeals()).withSelfRel());
    }

    private EntityModel<Meal> mealToEntityModel(String id, Meal meal) {
        return EntityModel.of(meal,
                linkTo(methodOn(MealsRestController.class).getMealById(id)).withSelfRel(),
                linkTo(methodOn(MealsRestController.class).getMeals()).withRel("All Meals"),
                linkTo(methodOn(MealsRestController.class).getCheapestMeal()).withRel("Cheapest Meal"),
                linkTo(methodOn(MealsRestController.class).getLargestMeal()).withRel("Largest Meal"),
                linkTo(methodOn(MealsRestController.class).putMeal(id, meal)).withRel("Put Meal"),
                linkTo(methodOn(MealsRestController.class).postMeal(meal)).withRel("Post Meal"),
                linkTo(methodOn(MealsRestController.class).deleteMeal(id)).withRel("Delete meal"));
    }
}
