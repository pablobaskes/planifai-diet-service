<#
.SYNOPSIS
Runs the Wave 1 critical backend API smoke flow for diet-service.

.DESCRIPTION
This script validates the MVP1 backend flow through public HTTP APIs:
1. create foods
2. create recipes
3. create inventory item
4. create diet
5. edit recipe slot
6. generate shopping list
7. purchase one item
8. purchase all items

Start diet-service first, for example:

  $env:JAVA_HOME='C:\Program Files\Java\jdk-17'
  $env:Path="$env:JAVA_HOME\bin;$env:Path"
  $runArgs = '--server.port=18085 --spring.datasource.url=jdbc:h2:mem:w1smoke;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE --spring.datasource.driver-class-name=org.h2.Driver --spring.datasource.username=sa --spring.datasource.password= --spring.jpa.hibernate.ddl-auto=create-drop --spring.sql.init.mode=never --spring.jpa.show-sql=false'
  mvn spring-boot:run "-Dspring-boot.run.useTestClasspath=true" "-Dspring-boot.run.arguments=$runArgs"

Then run:

  powershell -ExecutionPolicy Bypass -File .\scripts\smoke\wave1-api-smoke.ps1 -BaseUrl http://localhost:18085/api/v1

.PARAMETER BaseUrl
Base API URL. Defaults to PLANIFAI_API_BASE_URL or http://localhost:8083/api/v1.
#>

param(
    [string] $BaseUrl = $(if ($env:PLANIFAI_API_BASE_URL) { $env:PLANIFAI_API_BASE_URL } else { "http://localhost:8083/api/v1" })
)

$ErrorActionPreference = "Stop"

if ($BaseUrl.EndsWith("/")) {
    $BaseUrl = $BaseUrl.TrimEnd("/")
}

function Write-Step {
    param([string] $Message)
    Write-Host "[smoke] $Message"
}

function Assert-Condition {
    param(
        [bool] $Condition,
        [string] $Message
    )

    if (-not $Condition) {
        throw "Assertion failed: $Message"
    }
}

function Read-ErrorBody {
    param($Response)

    if ($null -eq $Response) {
        return ""
    }

    try {
        $stream = $Response.GetResponseStream()
        if ($null -eq $stream) {
            return ""
        }
        $reader = New-Object System.IO.StreamReader($stream)
        return $reader.ReadToEnd()
    } catch {
        return ""
    }
}

function Invoke-SmokeRequest {
    param(
        [string] $Method,
        [string] $Path,
        [object] $Body = $null,
        [int[]] $ExpectedStatus = @(200)
    )

    $uri = "$BaseUrl$Path"
    $parameters = @{
        Uri = $uri
        Method = $Method
        UseBasicParsing = $true
    }

    if ($null -ne $Body) {
        $parameters["ContentType"] = "application/json"
        $parameters["Body"] = ($Body | ConvertTo-Json -Depth 20)
    }

    try {
        $response = Invoke-WebRequest @parameters
        $statusCode = [int] $response.StatusCode
        $content = $response.Content
    } catch {
        $webResponse = $_.Exception.Response
        $statusCode = if ($webResponse) { [int] $webResponse.StatusCode } else { 0 }
        $content = Read-ErrorBody $webResponse
    }

    if ($ExpectedStatus -notcontains $statusCode) {
        throw "$Method $Path returned HTTP $statusCode. Expected $($ExpectedStatus -join ', '). Body: $content"
    }

    $parsed = $null
    if (-not [string]::IsNullOrWhiteSpace($content)) {
        $parsed = $content | ConvertFrom-Json
    }

    return [pscustomobject]@{
        Status = $statusCode
        Body = $parsed
        Raw = $content
    }
}

function New-Food {
    param(
        [string] $Name,
        [string] $Category,
        [double] $Calories,
        [double] $Protein,
        [double] $Carbs,
        [double] $Fat
    )

    $response = Invoke-SmokeRequest POST "/foods" @{
        name = $Name
        category = $Category
        caloriesPer100g = $Calories
        proteinPer100g = $Protein
        carbsPer100g = $Carbs
        fatPer100g = $Fat
    } @(201)

    Assert-Condition ($response.Body.id -gt 0) "food '$Name' was not created with an id"
    return $response.Body
}

function New-Recipe {
    param(
        [string] $Name,
        [string] $MealType,
        [long] $FoodId,
        [double] $Quantity,
        [string] $Unit
    )

    $response = Invoke-SmokeRequest POST "/recipes" @{
        name = $Name
        mealType = $MealType
        ingredients = @(
            @{
                foodId = $FoodId
                quantity = $Quantity
                unit = $Unit
            }
        )
        tags = @("wave1-smoke")
        servings = 1
    } @(201)

    Assert-Condition ($response.Body.id -gt 0) "recipe '$Name' was not created with an id"
    return $response.Body
}

function Get-FirstMealSlot {
    param([object[]] $Diets)

    foreach ($diet in @($Diets)) {
        foreach ($day in @($diet.days)) {
            foreach ($slot in @($day.mealSlots)) {
                if ($null -ne $slot -and $slot.id -gt 0) {
                    return $slot
                }
            }
        }
    }

    return $null
}

function Find-MealSlotById {
    param(
        [object[]] $Diets,
        [long] $SlotId
    )

    foreach ($diet in @($Diets)) {
        foreach ($day in @($diet.days)) {
            foreach ($slot in @($day.mealSlots)) {
                if ($null -ne $slot -and $slot.id -eq $SlotId) {
                    return $slot
                }
            }
        }
    }

    return $null
}

function Find-MealSlotByType {
    param(
        [object[]] $Diets,
        [string] $Type
    )

    foreach ($diet in @($Diets)) {
        foreach ($day in @($diet.days)) {
            foreach ($slot in @($day.mealSlots)) {
                if ($null -ne $slot -and $slot.type -eq $Type) {
                    return $slot
                }
            }
        }
    }

    return $null
}

Write-Step "Using API base URL: $BaseUrl"
Invoke-SmokeRequest GET "/foods" $null @(200) | Out-Null

$suffix = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
$today = (Get-Date).ToString("yyyy-MM-dd")
$endDate = (Get-Date).AddDays(6).ToString("yyyy-MM-dd")

Write-Step "Creating base foods"
$chicken = New-Food "Smoke Chicken $suffix" "MEAT" 165.0 31.0 0.0 3.6
$rice = New-Food "Smoke Rice $suffix" "GRAIN" 365.0 7.0 80.0 0.7
$egg = New-Food "Smoke Egg $suffix" "DAIRY" 155.0 13.0 1.1 11.0

Write-Step "Creating recipes"
$recipeA = New-Recipe "Smoke Egg Breakfast Recipe $suffix" "BREAKFAST" $egg.id 2.0 "UNIT"
$recipeB = New-Recipe "Smoke Chicken Lunch Recipe $suffix" "LUNCH" $chicken.id 200.0 "G"
$recipeC = New-Recipe "Smoke Rice Dinner Recipe $suffix" "DINNER" $rice.id 500.0 "G"

Write-Step "Creating inventory item"
$inventory = Invoke-SmokeRequest POST "/inventory" @{
    portion = @{
        foodId = $rice.id
        quantity = 100.0
        unit = "G"
    }
    location = "PANTRY"
} @(201)
Assert-Condition ($inventory.Body.id -gt 0) "inventory item was not created"

Write-Step "Creating active diet for $today to $endDate"
$diet = Invoke-SmokeRequest POST "/diets" @{
    name = "Wave 1 Smoke Diet $suffix"
    description = "Generated by Wave 1 API smoke"
    caloriesTarget = 2000
    initDate = $today
    endDate = $endDate
} @(201)
Assert-Condition ($diet.Body.id -gt 0) "diet was not created"

Write-Step "Reading diet range"
$range = Invoke-SmokeRequest GET "/diets/range?from=$today&to=$endDate" $null @(200)
$diets = @($range.Body)
$createdDiet = $diets | Where-Object { $_.id -eq $diet.Body.id } | Select-Object -First 1
Assert-Condition ($null -ne $createdDiet) "created diet was not returned by range endpoint"

$slot = Find-MealSlotByType @($createdDiet) "LUNCH"
if ($null -eq $slot) {
    throw @"
No LUNCH meal slot was generated for the created diet, so the API-only smoke cannot continue to slot override/shopping-list completion.
This is an implementation blocker for W1-E7-T3 when starting from a clean API-created dataset.
Observed constraint: API-created recipes with mealType must be eligible for generated MealType slots.
"@
}

Write-Step "Overriding meal slot $($slot.id) with recipe $($recipeB.id)"
$override = Invoke-SmokeRequest PATCH "/meal-slots/$($slot.id)/recipe" @{
    recipeId = $recipeB.id
} @(200)
Assert-Condition ($override.Body.recipe.id -eq $recipeB.id) "slot override response does not contain recipe $($recipeB.id)"

Write-Step "Verifying range reflects override"
$rangeAfterOverride = Invoke-SmokeRequest GET "/diets/range?from=$today&to=$endDate" $null @(200)
$updatedSlot = Find-MealSlotById @($rangeAfterOverride.Body) $slot.id
Assert-Condition ($null -ne $updatedSlot) "range endpoint did not return overridden slot $($slot.id)"
Assert-Condition ($updatedSlot.recipe.id -eq $recipeB.id) "range endpoint did not reflect overridden recipe"

Write-Step "Generating shopping list"
$shoppingList = Invoke-SmokeRequest POST "/shopping-lists/generate" $null @(200)
Assert-Condition ($shoppingList.Body.id -gt 0) "shopping list was not generated"
$items = @($shoppingList.Body.items)
Assert-Condition ($items.Count -gt 0) "shopping list has no missing items to purchase"

$firstItem = $items | Where-Object { $_.purchased -eq $false } | Select-Object -First 1
Assert-Condition ($null -ne $firstItem) "shopping list has no pending item to purchase"

Write-Step "Purchasing shopping list item $($firstItem.id)"
$purchaseOne = Invoke-SmokeRequest PATCH "/shopping-lists/items/$($firstItem.id)/purchase" $null @(200)
$purchasedItem = @($purchaseOne.Body.items) | Where-Object { $_.id -eq $firstItem.id } | Select-Object -First 1
Assert-Condition ($purchasedItem.purchased -eq $true) "item $($firstItem.id) was not marked as purchased"

Write-Step "Purchasing all remaining items"
$purchaseAll = Invoke-SmokeRequest PATCH "/shopping-lists/purchase-all" $null @(200)
$remaining = @($purchaseAll.Body.items) | Where-Object { $_.purchased -ne $true }
Assert-Condition ($remaining.Count -eq 0) "not all shopping list items were marked as purchased"
Assert-Condition ($purchaseAll.Body.status -eq "COMPLETED") "shopping list status is not COMPLETED"

Write-Step "Wave 1 API smoke passed"
