package com.planing.diet_service.contract;

import com.planing.diet.api.DietsApi;
import com.planing.diet.api.FoodsApi;
import com.planing.diet.api.InventoryApi;
import com.planing.diet.api.RecipesApi;
import com.planing.diet.api.ShoppingListsApi;
import com.planing.diet_service.Diet.infrastructure.input.rest.DietRestAdapter;
import com.planing.diet_service.Food.infrastructure.input.rest.FoodRestAdapter;
import com.planing.diet_service.InventoryItem.infrastructure.input.rest.InventoryRestAdapter;
import com.planing.diet_service.Recipe.infrastructure.input.rest.RecipeRestAdapter;
import com.planing.diet_service.ShoppingList.infrastructure.input.rest.ShoppingListRestAdapter;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiWave1ContractRegressionTest {

    private final Map<String, Object> openApi = loadOpenApi();

    @Test
    void openApiKeepsCriticalWave1OperationsAndResponses() {
        assertOperation("/api/v1/diets", "post", "createDiet", "201", "400");
        assertOperation("/api/v1/diets/range", "get", "getDietsByDateRange", "200", "400");

        assertOperation("/api/v1/meal-slots/{slotId}/recipe", "patch", "overrideMealSlotRecipe", "200", "400", "404");

        assertOperation("/api/v1/foods", "get", "getAllFoods", "200");
        assertOperation("/api/v1/foods", "post", "createFood", "201", "400");
        assertOperation("/api/v1/foods/{foodId}", "get", "getFoodById", "200", "404");
        assertOperation("/api/v1/foods/{foodId}", "put", "updateFood", "200", "400", "404");
        assertOperation("/api/v1/foods/{foodId}", "delete", "deleteFood", "204", "404");

        assertOperation("/api/v1/recipes", "get", "getAllRecipes", "200");
        assertOperation("/api/v1/recipes", "post", "createRecipe", "201", "400");
        assertOperation("/api/v1/recipes/{recipeId}", "get", "getRecipeById", "200", "404");
        assertOperation("/api/v1/recipes/{recipeId}", "put", "updateRecipe", "200", "400", "404");
        assertOperation("/api/v1/recipes/{recipeId}", "delete", "deleteRecipe", "204", "404");

        assertOperation("/api/v1/inventory", "get", "getAllInventoryItems", "200", "400");
        assertOperation("/api/v1/inventory", "post", "createInventoryItem", "201", "400");
        assertOperation("/api/v1/inventory/{itemId}", "get", "getInventoryItemById", "200", "404");
        assertOperation("/api/v1/inventory/{itemId}", "put", "updateInventoryItem", "200", "400", "404");
        assertOperation("/api/v1/inventory/{itemId}", "delete", "deleteInventoryItem", "204", "404");

        assertOperation("/api/v1/diets/{dietId}/shopping-lists/generate", "post", "generateWeeklyShoppingList", "200", "404");
        assertOperation("/api/v1/shopping-lists/current", "get", "getCurrentShoppingList", "200", "404");
        assertOperation("/api/v1/shopping-lists/items/{itemId}/purchase", "patch", "purchaseShoppingListItem", "200", "404", "409");
        assertOperation("/api/v1/shopping-lists/purchase-all", "patch", "purchaseAllShoppingListItems", "200", "404");
    }

    @Test
    void openApiKeepsReusableErrorResponsesWithErrorResponseSchema() {
        Map<String, Object> responses = mapAt(mapAt(openApi, "components"), "responses");

        assertErrorResponse(responses, "BadRequest");
        assertErrorResponse(responses, "NotFound");
        assertErrorResponse(responses, "Conflict");

        Map<String, Object> schemas = mapAt(mapAt(openApi, "components"), "schemas");
        Map<String, Object> errorResponse = mapAt(schemas, "ErrorResponse");
        Map<String, Object> properties = mapAt(errorResponse, "properties");

        assertThat(properties.keySet()).contains("status", "error", "message", "timestamp");
    }

    @Test
    void recipeContractExposesMealTypeForApiCreatedGenerationSeedRecipes() {
        Map<String, Object> schemas = mapAt(mapAt(openApi, "components"), "schemas");
        Map<String, Object> mealType = mapAt(schemas, "MealType");
        Map<String, Object> recipeRequestProperties = mapAt(mapAt(schemas, "RecipeRequest"), "properties");
        Map<String, Object> recipeResponseProperties = mapAt(mapAt(schemas, "RecipeResponse"), "properties");

        assertThat(recipeRequestProperties.keySet()).contains("mealType");
        assertThat(recipeResponseProperties.keySet()).contains("mealType");
        assertThat(mapAt(recipeRequestProperties, "mealType").get("$ref")).isEqualTo("#/components/schemas/MealType");
        assertThat(mapAt(recipeResponseProperties, "mealType").get("$ref")).isEqualTo("#/components/schemas/MealType");
        assertThat(stringListAt(mealType, "enum"))
                .contains("BREAKFAST", "LUNCH", "DINNER", "SNACK");
    }

    @Test
    void runtimeAdaptersStillImplementGeneratedOpenApiInterfaces() {
        assertThat(DietsApi.class).isAssignableFrom(DietRestAdapter.class);
        assertThat(FoodsApi.class).isAssignableFrom(FoodRestAdapter.class);
        assertThat(RecipesApi.class).isAssignableFrom(RecipeRestAdapter.class);
        assertThat(InventoryApi.class).isAssignableFrom(InventoryRestAdapter.class);
        assertThat(ShoppingListsApi.class).isAssignableFrom(ShoppingListRestAdapter.class);
    }

    @Test
    void generatedInterfacesStillExposeCriticalOperationMethods() {
        assertMethods(DietsApi.class, "createDiet", "getDietsByDateRange", "overrideMealSlotRecipe");
        assertMethods(FoodsApi.class, "getAllFoods", "createFood", "getFoodById", "updateFood", "deleteFood");
        assertMethods(RecipesApi.class, "getAllRecipes", "createRecipe", "getRecipeById", "updateRecipe", "deleteRecipe");
        assertMethods(InventoryApi.class, "getAllInventoryItems", "createInventoryItem", "getInventoryItemById",
                "updateInventoryItem", "deleteInventoryItem");
        assertMethods(ShoppingListsApi.class, "generateWeeklyShoppingList", "getCurrentShoppingList",
                "purchaseShoppingListItem", "purchaseAllShoppingListItems");
    }

    private void assertOperation(String path, String method, String operationId, String... statuses) {
        Map<String, Object> operation = mapAt(mapAt(paths(), path), method);

        assertThat(operation.get("operationId")).isEqualTo(operationId);
        assertThat(mapAt(operation, "responses").keySet()).contains(statuses);
    }

    private void assertErrorResponse(Map<String, Object> responses, String responseName) {
        Map<String, Object> response = mapAt(responses, responseName);
        Map<String, Object> content = mapAt(response, "content");
        Map<String, Object> json = mapAt(content, "application/json");
        Map<String, Object> schema = mapAt(json, "schema");

        assertThat(schema.get("$ref")).isEqualTo("#/components/schemas/ErrorResponse");
    }

    private void assertMethods(Class<?> apiType, String... methodNames) {
        assertThat(Arrays.stream(apiType.getMethods()).map(Method::getName))
                .contains(methodNames);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> paths() {
        return (Map<String, Object>) openApi.get("paths");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> mapAt(Map<String, Object> source, String key) {
        Object value = source.get(key);

        assertThat(value)
                .as("OpenAPI key '%s' should exist", key)
                .isInstanceOf(Map.class);
        return (Map<String, Object>) value;
    }

    @SuppressWarnings("unchecked")
    private List<String> stringListAt(Map<String, Object> source, String key) {
        Object value = source.get(key);

        assertThat(value)
                .as("OpenAPI key '%s' should exist", key)
                .isInstanceOf(List.class);
        return (List<String>) value;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadOpenApi() {
        try (InputStream input = getClass().getResourceAsStream("/openapi/diet-openapi.yml")) {
            assertThat(input).as("OpenAPI contract resource should be available").isNotNull();
            return new Yaml().load(input);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not load OpenAPI contract", exception);
        }
    }
}
