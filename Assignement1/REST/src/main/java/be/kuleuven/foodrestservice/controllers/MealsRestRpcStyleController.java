package be.kuleuven.foodrestservice.controllers;

import be.kuleuven.foodrestservice.domain.Meal;
import be.kuleuven.foodrestservice.domain.MealsRepository;
import be.kuleuven.foodrestservice.exceptions.MealNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.Option;
import java.util.Collection;
import java.util.Optional;

@RestController
public class MealsRestRpcStyleController {

    private final MealsRepository mealsRepository;

    @Autowired
    MealsRestRpcStyleController(MealsRepository mealsRepository) {
        this.mealsRepository = mealsRepository;
    }

    @GetMapping("/restrpc/meals/{id}")
    Meal getMealById(@PathVariable String id) {
        Optional<Meal> meal = mealsRepository.findMeal(id);

        return meal.orElseThrow(() -> new MealNotFoundException(id));
    }

    @GetMapping("/restrpc/meals")
    Collection<Meal> getMeals() {
        return mealsRepository.getAllMeal();
    }

    @GetMapping("/restrpc/meals/cheapest")
    Meal getCheapestMeal(){
        Optional<Meal> meal = mealsRepository.getCheapestMeal();
        return meal.orElseThrow(() -> new MealNotFoundException("cheapest"));
    }

    @GetMapping("/restrpc/meals/largest")
    Meal getLargestMeal(){
        Optional<Meal> meal = mealsRepository.getLargestMeal();
        return meal.orElseThrow(()->new MealNotFoundException("Largest"));
    }

    @PutMapping("/restrpc/meals/{id}")
    void putMeal(@PathVariable String id,@RequestBody Meal updatedMeal){
        Optional<Meal> meal = mealsRepository.findMeal(id);
        meal.orElseThrow(() -> new MealNotFoundException(id));
        mealsRepository.updateMeal(id,updatedMeal);
    }

    @PostMapping("/restrpc/meals")
    void postMeal(@RequestBody Meal newMeal){
        mealsRepository.addNewMeal(newMeal);
    }

    @DeleteMapping("/restrpc/meals/{id}")
    void deleteMeal(@PathVariable String id){
        Optional<Meal> meal = mealsRepository.findMeal(id);
        meal.orElseThrow(() -> new MealNotFoundException(id));
        mealsRepository.deleteMeal(id);
    }
}