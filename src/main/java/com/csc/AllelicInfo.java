package com.csc;

class AllelicInfo {

    private int homCount;
    private int hetCount;
    private int refAlleleCount;
    private int altAlleleCount;


    AllelicInfo() {
        homCount = 0;
        hetCount = 0;
        refAlleleCount = 0;
        altAlleleCount = 0;
    }

    int getHomCount() {
        return homCount;
    }

    int getHetCount() {
        return hetCount;
    }

    void incrementHomCount() {
        this.homCount++;
    }

    void incrementHetCount() {
        this.hetCount++;
    }

    void incrementAlleleCount(String allele1, String allele2) {
        if (allele1.equals("0"))
            this.refAlleleCount++;
        else
            this.altAlleleCount++;

        if (allele2.equals("0"))
            this.refAlleleCount++;
        else
            this.altAlleleCount++;
    }


    double getRefAlleleFrequency() {
        return (double)refAlleleCount / (refAlleleCount + altAlleleCount);
    }

}
