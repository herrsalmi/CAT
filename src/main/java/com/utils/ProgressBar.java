package com.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * project checkStatusConcordance
 * Created by ayyoub on 7/13/17.
 */

public class ProgressBar extends Thread {

    private volatile boolean showProgress = true;

    @Override
    public void run() {
        List<String> animation = new ArrayList<>();

        animation.add("[*---------]");
        animation.add("[-*--------]");
        animation.add("[--*-------]");
        animation.add("[---*------]");
        animation.add("[----*-----]");
        animation.add("[-----*----]");
        animation.add("[------*---]");
        animation.add("[-------*--]");
        animation.add("[--------*-]");
        animation.add("[---------*]");
        animation.add("[--------*-]");
        animation.add("[-------*--]");
        animation.add("[------*---]");
        animation.add("[-----*----]");
        animation.add("[----*-----]");
        animation.add("[---*------]");
        animation.add("[--*-------]");
        animation.add("[-*--------]");

        int x = 0;

        while (showProgress) {
            System.out.print("\rProcessing " + animation.get(x++ % animation.size()));
            try {
                Thread.sleep(400);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("Done!");
    }

    public synchronized void stopAnimation() {
        this.showProgress = false;
    }

    public synchronized void reset() {
        this.showProgress = true;
    }
}
