package latipe.product.utils;

import java.util.List;

/**
 * Utility class for calculating weighted average ratings.
 * This class provides methods to calculate weighted ratings where each position
 * in the ratings list has a different weight based on its index.
 */
public class AvgRating {

    /**
     * Calculates a weighted average of ratings.
     * The weight of each rating is determined by its position in the list (higher index = higher weight).
     *
     * @param ratings A list of integer ratings where index position determines the weight
     * @return The calculated weighted average rating
     */
    public static double calc(List<Integer> ratings) {
        int totalWeight = 0;
        int weightedSum = 0;

        for (int i = 0; i < ratings.size(); i++) {
            int weight = i + 1;
            totalWeight += weight;
            weightedSum += weight * ratings.get(i);
        }

        return (double) weightedSum / totalWeight;
    }
}
