package com.example.kitbag.effect;

import android.content.Context;
import android.graphics.Color;

import com.facebook.shimmer.Shimmer;
import com.facebook.shimmer.ShimmerDrawable;

public class ShimmerEffect {

    public static ShimmerDrawable get() {
        // Initialize shimmer
        Shimmer shimmer = new Shimmer.ColorHighlightBuilder()
                .setBaseColor(Color.parseColor("#AEADAD"))
                .setBaseAlpha(1)
                .setHighlightColor(Color.parseColor("#E7E7E7"))
                .setHighlightAlpha(1)
                .setDropoff(50)
                .build();
        // Initialize shimmer drawable
        ShimmerDrawable shimmerDrawable = new ShimmerDrawable();
        // Set shimmer
        shimmerDrawable.setShimmer(shimmer);
        return shimmerDrawable;
    }

}
