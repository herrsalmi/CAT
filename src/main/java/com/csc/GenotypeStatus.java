package com.csc;

public class GenotypeStatus {

    private String geno;
    private String allele1;
    private String allele2;
    private String status;
    private String alt;
    private String ref;


    GenotypeStatus(String geno, String allele1, String allele2) {
        super();
        this.geno = geno;
        this.allele1 = allele1;
        this.allele2 = allele2;
    }

    GenotypeStatus(String geno, String allele1, String allele2, String alt, String ref) {
        super();
        this.geno = geno;
        this.allele1 = allele1;
        this.allele2 = allele2;
        this.alt = alt;
        this.ref = ref;
    }

    String getGeno() {
        return geno;
    }

    String getAllele1() {
        return allele1;
    }

    String getStatus() {
        return status;
    }

    void setStatus(String status) {
        this.status = status;
    }

    String getAlt() {
        return alt;
    }

    String getRef() {
        return ref;
    }

    @Override
    public String toString() {
        return "[Geno : " + geno + "]" + "[Allele1 : " + allele1 + "]" + "[Allele2 : " + allele2 + "]" +
                "[Status : " + status + "]";
    }


}
