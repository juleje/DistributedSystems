/**
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech) (7.0.1).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
package be.kuleuven.foodrestservice.api;

import be.kuleuven.foodrestservice.model.Meal;
import be.kuleuven.foodrestservice.model.MealUpdateRequest;
import java.util.UUID;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.annotation.Generated;

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-10-27T13:25:05.386910300+02:00[Europe/Brussels]")
@Validated
@Tag(name = "meals", description = "the meals API")
public interface MealsApi {

    default Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    /**
     * POST /meals : Add a new meal
     * Add a new meal
     *
     * @param mealUpdateRequest  (required)
     * @return OK (status code 200)
     *         or New Meal created (status code 201)
     */
    @Operation(
        operationId = "addMeal",
        summary = "Add a new meal",
        description = "Add a new meal",
        tags = { "meals" },
        responses = {
            @ApiResponse(responseCode = "200", description = "OK", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))
            }),
            @ApiResponse(responseCode = "201", description = "New Meal created")
        }
    )
    @RequestMapping(
        method = RequestMethod.POST,
        value = "/meals",
        produces = { "application/json" },
        consumes = { "application/json" }
    )
    default ResponseEntity<Object> addMeal(
        @Parameter(name = "MealUpdateRequest", description = "", required = true) @Valid @RequestBody MealUpdateRequest mealUpdateRequest
    ) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }


    /**
     * DELETE /meals/{id} : Remove a meal
     * Remove an existing meal
     *
     * @param id Id of the meal (required)
     * @return OK (status code 200)
     *         or Authentication information is missing or invalid (status code 401)
     *         or Invalid Id Supplied (status code 400)
     *         or Meal not found (status code 404)
     */
    @Operation(
        operationId = "deleteMeal",
        summary = "Remove a meal",
        description = "Remove an existing meal",
        tags = { "meals" },
        responses = {
            @ApiResponse(responseCode = "200", description = "OK", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = Meal.class))
            }),
            @ApiResponse(responseCode = "401", description = "Authentication information is missing or invalid"),
            @ApiResponse(responseCode = "400", description = "Invalid Id Supplied"),
            @ApiResponse(responseCode = "404", description = "Meal not found")
        }
    )
    @RequestMapping(
        method = RequestMethod.DELETE,
        value = "/meals/{id}",
        produces = { "application/json" }
    )
    default ResponseEntity<Meal> deleteMeal(
        @Parameter(name = "id", description = "Id of the meal", required = true, in = ParameterIn.PATH) @PathVariable("id") UUID id
    ) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType: MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "{ \"kcal\" : 0, \"price\" : \"price\", \"name\" : \"name\", \"mealType\" : \"VEGAN\", \"description\" : \"description\", \"id\" : \"046b6c7f-0b8a-43b9-b35d-6489e6daee91\" }";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }


    /**
     * GET /meals/{id} : Get a meal by its id
     * Get a meal by id description
     *
     * @param id Id of the meal (required)
     * @return Found the meal (status code 200)
     *         or Invalid Id Supplied (status code 400)
     *         or Meal not found (status code 404)
     */
    @Operation(
        operationId = "getMealById",
        summary = "Get a meal by its id",
        description = "Get a meal by id description",
        tags = { "meals" },
        responses = {
            @ApiResponse(responseCode = "200", description = "Found the meal", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = Meal.class))
            }),
            @ApiResponse(responseCode = "400", description = "Invalid Id Supplied"),
            @ApiResponse(responseCode = "404", description = "Meal not found")
        }
    )
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/meals/{id}",
        produces = { "application/json" }
    )
    default ResponseEntity<Meal> getMealById(
        @Parameter(name = "id", description = "Id of the meal", required = true, in = ParameterIn.PATH) @PathVariable("id") UUID id
    ) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType: MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "{ \"kcal\" : 0, \"price\" : \"price\", \"name\" : \"name\", \"mealType\" : \"VEGAN\", \"description\" : \"description\", \"id\" : \"046b6c7f-0b8a-43b9-b35d-6489e6daee91\" }";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }


    /**
     * GET /meals : Retrieve all meals
     * Find all meals
     *
     * @return OK (status code 200)
     *         or No Meals found (status code 404)
     */
    @Operation(
        operationId = "getMeals",
        summary = "Retrieve all meals",
        description = "Find all meals",
        tags = { "meals" },
        responses = {
            @ApiResponse(responseCode = "200", description = "OK", content = {
                @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Meal.class)))
            }),
            @ApiResponse(responseCode = "404", description = "No Meals found")
        }
    )
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/meals",
        produces = { "application/json" }
    )
    default ResponseEntity<List<Meal>> getMeals(
        
    ) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType: MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "[ { \"kcal\" : 0, \"price\" : \"price\", \"name\" : \"name\", \"mealType\" : \"VEGAN\", \"description\" : \"description\", \"id\" : \"046b6c7f-0b8a-43b9-b35d-6489e6daee91\" }, { \"kcal\" : 0, \"price\" : \"price\", \"name\" : \"name\", \"mealType\" : \"VEGAN\", \"description\" : \"description\", \"id\" : \"046b6c7f-0b8a-43b9-b35d-6489e6daee91\" } ]";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }


    /**
     * PUT /meals/{id} : Update existing meal
     * Update existing meal
     *
     * @param id Id of the meal (required)
     * @param mealUpdateRequest  (required)
     * @return OK (status code 200)
     *         or Invalid Id Supplied (status code 400)
     *         or Meal not found (status code 404)
     */
    @Operation(
        operationId = "updateMeal",
        summary = "Update existing meal",
        description = "Update existing meal",
        tags = { "meals" },
        responses = {
            @ApiResponse(responseCode = "200", description = "OK", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = Meal.class))
            }),
            @ApiResponse(responseCode = "400", description = "Invalid Id Supplied"),
            @ApiResponse(responseCode = "404", description = "Meal not found")
        }
    )
    @RequestMapping(
        method = RequestMethod.PUT,
        value = "/meals/{id}",
        produces = { "application/json" },
        consumes = { "application/json" }
    )
    default ResponseEntity<Meal> updateMeal(
        @Parameter(name = "id", description = "Id of the meal", required = true, in = ParameterIn.PATH) @PathVariable("id") UUID id,
        @Parameter(name = "MealUpdateRequest", description = "", required = true) @Valid @RequestBody MealUpdateRequest mealUpdateRequest
    ) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType: MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "{ \"kcal\" : 0, \"price\" : \"price\", \"name\" : \"name\", \"mealType\" : \"VEGAN\", \"description\" : \"description\", \"id\" : \"046b6c7f-0b8a-43b9-b35d-6489e6daee91\" }";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }

}
