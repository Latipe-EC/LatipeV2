package latipe.product.utils;

import java.util.List;

public class AvgRating {

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
