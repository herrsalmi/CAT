package com.csc;

import com.handlers.ArgsHandler;
import com.handlers.BamHandler;
import com.handlers.FisherStrandHandler;
import com.handlers.VCFHandler;
import com.utils.SimpsonsRule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * project checkStatusConcordance
 * Created by ayyoub on 6/8/17.
 */
class ConcordanceAnalysis {

    private static final Logger LOGGER = LogManager.getRootLogger();
    private HashMap<String, GenotypeStatus> hashQTLstatus;
    private HashMap<Integer, String> hashAniPosInVCF;
    private HashMap<String, HashMap<String, HashMap<String, GenotypeStatus>>> hashGenotype;
    private HashMap<String, HashMap<String, AllelicInfo>> alleleFrequency;
    private List<String> individuals;
    private ArrayList<String> allAllele;
    private ArrayList<List<Double>> fisherStrand;
    private String statusType;
    private int snpCount;
    private SimpsonsRule simpson = new SimpsonsRule();

    ConcordanceAnalysis() {
        this.snpCount = 0;
        hashQTLstatus = new HashMap<>();
        hashAniPosInVCF = new HashMap<>();
        hashGenotype = new HashMap<>();
        alleleFrequency = new HashMap<>();
        allAllele = new ArrayList<>();

        individuals = new ArrayList<>();
    }


    /***********************************************************
     * ** check if names on VCF file and QTL status file match
     * @throws IOException file not found
     */
    private void checkNames() throws IOException {
        BufferedReader br = readVCF();
        String line;
        List<String> vcfIndiv = new ArrayList<>(20);
        List<String> qtlIndiv = new ArrayList<>(20);
        LOGGER.info("Checking homozygosity from VCF file");
        // store individuals from vcf file
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("##"))
                continue;
            if (line.startsWith("#CHROM")) {
                String[] info = line.split("\t");
                vcfIndiv.addAll(Arrays.asList(info).subList(9, info.length));
                break;
            }
        }
        br.close();

        br = new BufferedReader(new FileReader(ArgsHandler.INSTANCE.getQtlPath()));
        // store individuals from qtl status file
        while ((line = br.readLine()) != null) {
            line = line.trim();
            qtlIndiv.add(line.split("\t")[0]);
        }
        br.close();

        // compare list of individuals
        // TODO more testing, this is a temporary fix
        if (vcfIndiv.size() != qtlIndiv.size()) {
            LOGGER.warn("Number of individuals from VCF file doesn't match the QTL status file !");
            if (vcfIndiv.containsAll(qtlIndiv)) {
                vcfIndiv.retainAll(qtlIndiv);
                individuals.addAll(vcfIndiv);
            }

            if (qtlIndiv.containsAll(vcfIndiv)) {
                qtlIndiv.retainAll(vcfIndiv);
            }

        }

    }


    /****************************************************
     * ** put genotype in a hash and check homozygosity
     * @throws IOException file not found
     */
    private void checkQTL() throws IOException, BadFileFormatException {
        BufferedReader br = new BufferedReader(new FileReader(ArgsHandler.INSTANCE.getQtlPath()));
        String line;
        LOGGER.info("Checking homozygosity from QTL status file");
        while ((line = br.readLine()) != null) {
            line = line.trim();
            String[] info = line.split("\t");
            String[] allele = info[1].split("");
            hashQTLstatus.put(info[0], new GenotypeStatus(info[1], allele[0], allele[1]));

            allAllele.add(allele[0]);
            allAllele.add(allele[1]);
        }

        br.close();

        if (allAllele.contains("P") && allAllele.contains("N")) {
            statusType = "PN";
        } else if (allAllele.contains("P")) {
            statusType = "P";
        } else if (allAllele.contains("N")) {
            statusType = "N";
        }

        //TODO needs optimisation
        for (String ani : hashQTLstatus.keySet()) {
            switch (statusType) {
                case "PN":
                    if (hashQTLstatus.get(ani).getGeno().matches("(.*)p(.*)|(.*)n(.*)|(.*)0(.*)|(.*)na(.*)")) {
                        hashQTLstatus.get(ani).setStatus("NA");
                    } else if (hashQTLstatus.get(ani).getGeno().equals("PP")) {
                        hashQTLstatus.get(ani).setStatus("homoP");
                    } else if (hashQTLstatus.get(ani).getGeno().equals("NN")) {
                        hashQTLstatus.get(ani).setStatus("homoN");
                    } else if (hashQTLstatus.get(ani).getGeno().equals("PN") || hashQTLstatus.get(ani).getGeno().equals("NP")
                            || hashQTLstatus.get(ani).getGeno().equals("Pn") || hashQTLstatus.get(ani).getGeno().equals("nP")
                            || hashQTLstatus.get(ani).getGeno().equals("P0") || hashQTLstatus.get(ani).getGeno().equals("0P")
                            || hashQTLstatus.get(ani).getGeno().equals("Np") || hashQTLstatus.get(ani).getGeno().equals("pN")
                            || hashQTLstatus.get(ani).getGeno().equals("N0") || hashQTLstatus.get(ani).getGeno().equals("0N")
                            || hashQTLstatus.get(ani).getGeno().equals("Pp") || hashQTLstatus.get(ani).getGeno().equals("pP")
                            || hashQTLstatus.get(ani).getGeno().equals("Nn") || hashQTLstatus.get(ani).getGeno().equals("nN")) {
                        hashQTLstatus.get(ani).setStatus("heterozygous");
                    }
                    break;
                case "P":
                    if (hashQTLstatus.get(ani).getGeno().matches("(.*)na(.*)")) {
                        hashQTLstatus.get(ani).setStatus("NA");
                    } else if (hashQTLstatus.get(ani).getGeno().equals("PP")) {
                        hashQTLstatus.get(ani).setStatus("homoP");
                    } else if (hashQTLstatus.get(ani).getGeno().equals("00") || hashQTLstatus.get(ani).getGeno().equals("0n")
                            || hashQTLstatus.get(ani).getGeno().equals("n0") || hashQTLstatus.get(ani).getGeno().equals("nn")
                            || hashQTLstatus.get(ani).getGeno().equals("pp") || hashQTLstatus.get(ani).getGeno().equals("p0")
                            || hashQTLstatus.get(ani).getGeno().equals("0p") || hashQTLstatus.get(ani).getGeno().equals("np")
                            || hashQTLstatus.get(ani).getGeno().equals("pn")) {
                        hashQTLstatus.get(ani).setStatus("homoN");
                    } else if (hashQTLstatus.get(ani).getGeno().equals("P0") || hashQTLstatus.get(ani).getGeno().equals("0P")
                            || hashQTLstatus.get(ani).getGeno().equals("Pn") || hashQTLstatus.get(ani).getGeno().equals("nP")
                            || hashQTLstatus.get(ani).getGeno().equals("Pp") || hashQTLstatus.get(ani).getGeno().equals("pP")) {
                        hashQTLstatus.get(ani).setStatus("heterozygous");
                    }
                    break;
                case "N":
                    if (hashQTLstatus.get(ani).getGeno().matches("(.*)na(.*)")) {
                        hashQTLstatus.get(ani).setStatus("NA");
                    } else if (hashQTLstatus.get(ani).getGeno().equals("NN")) {
                        hashQTLstatus.get(ani).setStatus("homoN");
                    } else if (hashQTLstatus.get(ani).getGeno().equals("00") || hashQTLstatus.get(ani).getGeno().equals("0n")
                            || hashQTLstatus.get(ani).getGeno().equals("n0") || hashQTLstatus.get(ani).getGeno().equals("nn")
                            || hashQTLstatus.get(ani).getGeno().equals("pp") || hashQTLstatus.get(ani).getGeno().equals("p0")
                            || hashQTLstatus.get(ani).getGeno().equals("0p") || hashQTLstatus.get(ani).getGeno().equals("np")
                            || hashQTLstatus.get(ani).getGeno().equals("pn")) {
                        hashQTLstatus.get(ani).setStatus("homoP");
                    } else if (hashQTLstatus.get(ani).getGeno().equals("N0") || hashQTLstatus.get(ani).getGeno().equals("0N")
                            || hashQTLstatus.get(ani).getGeno().equals("Nn") || hashQTLstatus.get(ani).getGeno().equals("nN")
                            || hashQTLstatus.get(ani).getGeno().equals("Np") || hashQTLstatus.get(ani).getGeno().equals("pN")) {
                        hashQTLstatus.get(ani).setStatus("heterozygous");
                    }
                    break;
            }

        }

    }

    /***************************************
     * @throws IOException file not found
     */
    private void checkGenotype() throws IOException {
        BufferedReader vcf = readVCF();
        String line;
        String chr;
        String pos;
        String alt;
        String ref;
        int gqIndex = -1;
        LOGGER.info("Checking homozygosity from VCF file");
        while ((line = vcf.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("##"))
                continue;
            if (line.startsWith("#CHROM")) {
                String[] info = line.split("\t");
                // change this for the general case
                for (int i = 9; i < info.length; i++) {
                    if (individuals.contains(info[i]))
                        hashAniPosInVCF.put(i, info[i]); // added for general case
                } // END of for i
            } // END of if #CHROM
            else {
                if (gqIndex == -1) {
                    String[] info = line.split("\t")[8].split(":");
                    gqIndex = Arrays.asList(info).indexOf("GQ");
                }
                snpCount++;
                String[] info = line.split("\t");
                if (!info[0].startsWith("Chr"))
                    continue;
                if (info[4].contains(","))
                    continue;
                chr = info[0].substring(3);
                pos = info[1];
                alt = info[4];
                ref = info[3];

                for (int i : hashAniPosInVCF.keySet()) {
                    int gq;
                    try {
                        gq = Integer.parseInt(info[i].split(":")[gqIndex]);
                    } catch (NumberFormatException e) {
                        // TODO do something here
                        continue;
                    }
                    // check if genotype quality >= 20
                    if (gq < ArgsHandler.INSTANCE.getGqThr()) {
                        continue;
                    }
                    // check fisher strand < threshold
                    if (fisherStrand != null) {
                        // i starts from 9
                        assert i - 9 >= 0;
                        if (fisherStrand.get(i - 9).get(snpCount - 1) < ArgsHandler.INSTANCE.getFsThr())
                            continue;
                    }

                    String geno = info[i];
                    Pattern p = Pattern.compile("^([01])/([01]):.*$");
                    Matcher m = p.matcher(geno);
                    if (m.find()) {
                        if (hashGenotype.get(chr) == null) {
                            hashGenotype.put(chr, new HashMap<>());
                            alleleFrequency.put(chr, new HashMap<>());
                        }

                        if (hashGenotype.get(chr).get(pos) == null) {
                            hashGenotype.get(chr).put(pos, new HashMap<>());
                            alleleFrequency.get(chr).put(pos, new AllelicInfo());
                        }

                        if (m.group(1).equals(m.group(2))) {
                            hashGenotype.get(chr).get(pos).put(hashAniPosInVCF.get(i),
                                    new GenotypeStatus("homozygous", m.group(1), m.group(2), alt, ref));
                            alleleFrequency.get(chr).get(pos).incrementAlleleCount(m.group(1), m.group(2));
                            alleleFrequency.get(chr).get(pos).incrementHomCount();

                        } else {
                            hashGenotype.get(chr).get(pos).put(hashAniPosInVCF.get(i),
                                    new GenotypeStatus("heterozygous", m.group(1), m.group(2), alt, ref));
                            alleleFrequency.get(chr).get(pos).incrementAlleleCount(m.group(1), m.group(2));
                            alleleFrequency.get(chr).get(pos).incrementHetCount();
                        }
                    } // END of if m.find
                } // END of for i
            }
        } // END of while vcf

        vcf.close();
    }

    private BufferedReader readVCF() throws IOException {
        BufferedReader br;
        if (ArgsHandler.INSTANCE.getVcfPath().endsWith(".gz"))
            br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(ArgsHandler.INSTANCE.getVcfPath()))));
        else
            br = new BufferedReader(new FileReader(ArgsHandler.INSTANCE.getVcfPath()));
        return br;
    }

    /**************************************
     * ** compare genotype and QTL status
     *
     * @throws IOException ***
     */
    private void compareStatus() throws IOException {
        LOGGER.info("Comparing genotype and QTL status");
        // add extension
        if (!Files.isDirectory(Paths.get(MainEntry.CONC_OUT_DIR)))
            Files.createDirectory(Paths.get(MainEntry.CONC_OUT_DIR));
        BufferedWriter bw = new BufferedWriter(new FileWriter(MainEntry.CONC_OUT_DIR + ArgsHandler.INSTANCE.getOutPath() + MainEntry.EXTENSION));
        bw.write("chr\tposition\tnb_concordant\ttotal individuals\tperc_concordance\tconcordant by chance\tref\talt");
        bw.newLine();
        String alt = null;
        String ref = null;
        for (String chr : hashGenotype.keySet()) {
            for (String pos : hashGenotype.get(chr).keySet()) {
                int concordant1 = 0;
                int concordant2 = 0;
                int tot = 0;
                for (String ani : hashGenotype.get(chr).get(pos).keySet()) {
                    alt = hashGenotype.get(chr).get(pos).get(ani).getAlt();
                    ref = hashGenotype.get(chr).get(pos).get(ani).getRef();
                    if (hashQTLstatus.get(ani).getStatus().equals("NA"))
                        continue;

                    if (hashGenotype.get(chr).get(pos).get(ani).getGeno().equals("heterozygous")) {
                        if (hashQTLstatus.get(ani).getStatus().equals("heterozygous")) {
                            concordant1++;
                            concordant2++;
                            tot++;
                            continue;
                        } else {
                            tot++;
                            continue;
                        }
                    }

                    if (hashGenotype.get(chr).get(pos).get(ani).getGeno().equals("homozygous")) {
                        if (hashQTLstatus.get(ani).getStatus().equals("homoP")) {
                            if (hashGenotype.get(chr).get(pos).get(ani).getAllele1().equals("0"))
                                concordant1++;
                            if (hashGenotype.get(chr).get(pos).get(ani).getAllele1().equals("1"))
                                concordant2++;
                            tot++;
                        } else if (hashQTLstatus.get(ani).getStatus().equals("homoN")) {
                            if (hashGenotype.get(chr).get(pos).get(ani).getAllele1().equals("1"))
                                concordant1++;
                            if (hashGenotype.get(chr).get(pos).get(ani).getAllele1().equals("0"))
                                concordant2++;
                            tot++;
                        } else {
                            tot++;
                        }
                    }
                } // END of for ani
                int concordant = concordant1 >= concordant2 ? concordant1 : concordant2;

                StringBuilder buffer = new StringBuilder();
                buffer.append(chr).append("\t");
                buffer.append(pos).append("\t");
                buffer.append(concordant).append("\t");
                buffer.append(tot).append("\t");
                if (tot != 0)
                    buffer.append(String.format("%.2f", ((double) concordant / (double) tot)));
                bw.write(buffer.toString());
                bw.write("\t");
                int n = alleleFrequency.get(chr).get(pos).getHetCount();
                int m = alleleFrequency.get(chr).get(pos).getHomCount();
                double cbc = simpson.integrate(n, m);
                if (cbc <= ((double) 1 / snpCount))
                    bw.write("NO\t");
                else
                    bw.write("YES\t");
                assert ref != null;
                bw.write(ref);
                bw.write("\t");
                bw.write(alt);
                bw.newLine();

            } // END of for pos


        } // END of for chr


        bw.close();
    }


    void run() {
        // call checkNames
        try {
            checkNames();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(6);
        }

        // check if BAM file and ref file are provided, else don't compute fisher strand
        if (ArgsHandler.INSTANCE.getBamFiles() != null && ArgsHandler.INSTANCE.getRef() != null) {
            // call vcfHandler
            VCFHandler vcfHandler = new VCFHandler(ArgsHandler.INSTANCE.getVcfPath());
            // returns the path of the file containing positions, should i use it (PS: hard coded in BamHandler)?
            vcfHandler.extractPositions();
            // call bamHamdler
            BamHandler bamHandler = new BamHandler();
            try {
                bamHandler.extractSamplesNames(ArgsHandler.INSTANCE.getBamFiles());
            } catch (ReadGroupException e1) {
                LOGGER.error("Please add read group information to BAM files or skip fisher strand computation.");
                System.exit(3);
            } catch (BadFileFormatException e2) {
                LOGGER.error("Please check BAM files integrity or skip fisher strand computation.");
            }

            bamHandler.mpileup(ArgsHandler.INSTANCE.getBamFiles(), ArgsHandler.INSTANCE.getRef());
            // call fisherStrandHandler
            FisherStrandHandler fsHandler = new FisherStrandHandler();
            fisherStrand = new ArrayList<>(ArgsHandler.INSTANCE.getBamFiles().size());
            for (int i = 0; i < ArgsHandler.INSTANCE.getBamFiles().size(); i++) {
                fisherStrand.add(fsHandler.countReads(i));
            }
        } else {
            LOGGER.info("BAM files or reference file not provided! Skipping fisher strand computation.");
        }


        try {
            checkQTL();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(6);
        }
        try {
            checkGenotype();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(6);
        }
        try {
            compareStatus();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(6);
        }
    }
}
