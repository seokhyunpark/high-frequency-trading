package io.github.seokhyunpark.hft;

import io.github.seokhyunpark.hft.controller.HftController;

public class HftApplication {
    public static void main(String[] args) {
        HftController controller = new HftController();
        controller.start();
    }
}
