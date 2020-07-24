package com.utils;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class SubInformation implements Comparable<SubInformation> {

    private int totalIndiv;
    private final double percentConc;
    private final String alt;
    private final String ref;
    private final long position;
    private final HashSet<String> supportingMethods;
    private boolean identicalAlt;


    public SubInformation(long position, int totalIndiv, double percentConc, String ref, String alt, String method) {
        this.supportingMethods = new HashSet<>();
        this.position = position;
        this.totalIndiv = totalIndiv;
        this.percentConc = percentConc;
        this.alt = alt;
        this.ref = ref;
        supportingMethods.add(method);
    }

    public int getTotalIndiv() {
        return totalIndiv;
    }

    public double getPercentConc() {
        return percentConc;
    }

    public String getAlt() {
        return alt;
    }

    public String getRef() {
        return ref;
    }

    public long getPosition() {
        return position;
    }

    public boolean isIdenticalAlt() {
        return identicalAlt;
    }

    public Set<String> getSupportingMethods() {
        return supportingMethods;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(position);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubInformation that = (SubInformation) o;
        return position == that.getPosition();
    }

    @Override
    public int compareTo(@NotNull SubInformation o) {
        if (this.getPosition() == o.getPosition()) {
            o.supportingMethods.addAll(this.getSupportingMethods());
            o.identicalAlt = o.alt.equals(this.getAlt());
            o.totalIndiv = Integer.min(o.totalIndiv, this.getTotalIndiv());
            return 0;
        } else if (this.getPosition() > o.getPosition())
            return 1;
        else
            return -1;
    }
}
