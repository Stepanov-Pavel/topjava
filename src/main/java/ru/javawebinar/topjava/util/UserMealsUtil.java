package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExcess;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

public class UserMealsUtil {
    public static void main(String[] args) {
        List<UserMeal> meals = Arrays.asList(
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 10, 0), "Завтрак", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 13, 0), "Обед", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 20, 0), "Ужин", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 0, 0), "Еда на граничное значение", 100),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 10, 0), "Завтрак", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 13, 0), "Обед", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 20, 0), "Ужин", 410)
        );

        List<UserMealWithExcess> mealsTo = filteredByCycles(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
        mealsTo.forEach(System.out::println);

        System.out.println(filteredByStreams(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000));
    }

    public static List<UserMealWithExcess> filteredByCycles(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        List<UserMealWithExcess> result = new ArrayList<>();
        Map<String, Integer> totalCaloriesPerDay = new HashMap<>();

        for (UserMeal meal : meals) {
            String mealDayOfMonth = String.valueOf(meal.getDateTime().toLocalDate().getDayOfMonth());
            if (totalCaloriesPerDay.containsKey(mealDayOfMonth)) {
                Integer dayCaloriesValue = totalCaloriesPerDay.get(mealDayOfMonth);
                dayCaloriesValue += meal.getCalories();
                totalCaloriesPerDay.put(mealDayOfMonth, dayCaloriesValue);
            } else {
                totalCaloriesPerDay.put(mealDayOfMonth, meal.getCalories());
            }
        }

        for (UserMeal meal : meals) {
            if (TimeUtil.isBetweenHalfOpen(meal.getDateTime().toLocalTime(), startTime, endTime)) {
                String mealDayOfMonth = String.valueOf(meal.getDateTime().toLocalDate().getDayOfMonth());
                result.add(new UserMealWithExcess(meal.getDateTime(), meal.getDescription(), meal.getCalories(), (totalCaloriesPerDay.get(mealDayOfMonth) > caloriesPerDay)));
            }
        }

        return result;
    }

    public static List<UserMealWithExcess> filteredByStreams(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        Map<String, Integer> totalCaloriesPerDay = meals.stream()
                .collect(Collectors.groupingBy(meal -> String.valueOf(meal.getDateTime().toLocalDate().getDayOfMonth()),
                        Collectors.summingInt(UserMeal::getCalories)
                ));

        return meals.stream()
                .filter(meal -> TimeUtil.isBetweenHalfOpen(meal.getDateTime().toLocalTime(), startTime, endTime))
                .map(meal -> {
                    String mealDayOfMonth = String.valueOf(meal.getDateTime().toLocalDate().getDayOfMonth());
                    return new UserMealWithExcess(meal.getDateTime(), meal.getDescription(), meal.getCalories(), (totalCaloriesPerDay.get(mealDayOfMonth) > caloriesPerDay));
                })
                .collect(Collectors.toList());
    }
}
