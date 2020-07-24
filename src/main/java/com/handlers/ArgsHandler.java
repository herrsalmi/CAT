package com.handlers;

import com.csc.MainEntry;
import com.csc.Mode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * project checkStatusConcordance
 * Created by ayyoub on 6/6/17.
 */
public enum ArgsHandler {

    INSTANCE;

    private static final Logger LOGGER = LogManager.getRootLogger();
    private Mode tMode;
    private final HashMap<String, String> params = new HashMap<>();
    // parameters are declared here
    private int gqThr = 20;
    // now using a phred scaled value, use a threshold of 10 (pval 0.1) or 20 (pval 0.01)
    private int fsThr = 10;
    private String qtlPath;
    private String vcfPath;
    private String outPath;
    private String ref;

    private List<String> bamFiles;
    private List<String> concFiles;
    private int minIndiv = 10;
    private double fraction = 0.9;
    private int methodsNbr = 2;

    ArgsHandler() {
        bamFiles = new ArrayList<>();
    }

    public void parseArgs(String[] args) {
        if (args.length == 0) {
            printHelp();
            System.exit(0);
        }
        switch (args[0]) {
            case "-v":
            case "version":
                System.out.println("Concordance Analysis Toolkit");
                System.out.println("Version: " + MainEntry.VERSION);
                System.exit(0);
                break;
            case "-h":
            case "help":
                printHelp();
                System.exit(0);
                break;
            case "Concordance":
                if (args.length != 8 && args.length != 6 && args.length != 4) {
                    LOGGER.error("ERROR! : incorrect number of arguments !");
                    System.exit(1);
                }
                for (int i = 1; i < args.length; i++) {
                    params.put(args[i].split("=")[0], args[i].split("=")[1]);
                }
                checkParams(Mode.CONCORDANCE);
                tMode = Mode.CONCORDANCE;
                break;
            case "Venn":
                if (args.length != 3 && args.length != 4 && args.length != 5 && args.length != 6) {
                    LOGGER.error("ERROR! : incorrect number of arguments !");
                    System.exit(1);
                }
                for (int i = 1; i < args.length; i++) {
                    params.put(args[i].split("=")[0], args[i].split("=")[1]);
                }
                checkParams(Mode.VENN);
                tMode = Mode.VENN;
                break;
            default:
                printHelp();
                System.err.println("ERROR! : incorrect option !");
                System.exit(1);
        }

        initParams();
    }

    void initParams() {
        switch (tMode) {
            case CONCORDANCE:
                qtlPath = params.get("qtl");
                vcfPath = params.get("vcf");
                outPath = params.get("o");

                gqThr = Integer.parseInt(params.getOrDefault("gq", Integer.toString(gqThr)));
                fsThr = Integer.parseInt(params.getOrDefault("fs", Integer.toString(fsThr)));

                bamFiles = params.containsKey("bam") ? Arrays.asList(params.getOrDefault("bam", null).split(",")) : null;
                ref = params.getOrDefault("ref", null);
                break;
            case VENN:
                concFiles = Arrays.asList(params.get("conc").split(","));
                outPath = params.get("o");
                minIndiv = Integer.parseInt(params.getOrDefault("min-indiv", Integer.toString(minIndiv)));
                fraction = Double.parseDouble(params.getOrDefault("gq", Double.toString(fraction)));
                methodsNbr = Integer.parseInt(params.getOrDefault("m", Integer.toString(methodsNbr)));
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    private void printHelp() {
        System.out.println("Concordance Analysis Toolkit");
        System.out.println("Version: " + MainEntry.VERSION);
        System.out.println("Check concordance between QTL status and genotype");
        System.out.println("usage : CAT [command] [options]");

        System.out.println("\t-v");
        System.out.println("\t\tprint the program version");

        System.out.println("\thelp | -h");
        System.out.println("\t\tprint this help and exit the program");

        System.out.println("\tConcordance");
        System.out.println("\t\tqtl=<file>                file containing QTL status information");
        System.out.println("\t\tvcf=<file>                vcf or gzip compressed vcf file");
        System.out.println("\t\to=<string>                output prefix");
        System.out.println("\t\t[gq=<integer>]            genotype quality threshold (DEFAULT 20)");
        System.out.println("\t\t[fs=<integer>]            fisher strand threshold (DEFAULT 10)");
        System.out.println("\t\t[bam=<file,...>]          comma separated BAM files");
        System.out.println("\t\t[ref=[file]]              reference genome in FASTA format");

        System.out.println("\tVenn");
        System.out.println("\t\tconc=<file,...>           comma separated concordance analysis output files");
        System.out.println("\t\to=<string>                output prefix");
        System.out.println("\t\t[min-indiv=<integer>]     minimum number of supporting individuals (DEFAULT 10)");
        System.out.println("\t\t[frac=<float>]            minimum percentage of concordance (DEFAULT 0.9)");
        System.out.println("\t\t[m=<integer>]             minimum number of methods confirming a SNP (DEFAULT 2)");

    }

    private void checkParams(Mode mode) {
        switch (mode) {
            case CONCORDANCE:
                if (!params.containsKey("qtl")) {
                    System.err.println("ERROR! : QTL file not provided !");
                    System.exit(2);
                }

                if (!params.containsKey("vcf")) {
                    System.err.println("ERROR! : VCF file not provided !");
                    System.exit(2);
                }

                if (!params.containsKey("o")) {
                    System.err.println("ERROR! : output prefix not provided !");
                    System.exit(2);
                }

                if (params.containsKey("bam") && !params.containsKey("ref")) {
                    System.err.println("ERROR! : reference genome should be provided !");
                    System.exit(2);
                }

                break;
            case VENN:
                if (!params.containsKey("conc")) {
                    System.err.println("ERROR! : concordance analysis output files not provided !");
                    System.exit(2);
                }

                if (!params.containsKey("o")) {
                    System.err.println("ERROR! : output prefix not provided !");
                    System.exit(2);
                }
                break;
            default:
                throw new UnsupportedOperationException();
        }

    }

    @Contract(pure = true)
    public Mode getMode() {
        return tMode;
    }

    @Contract(pure = true)
    public int getGqThr() {
        return gqThr;
    }

    @Contract(pure = true)
    public int getFsThr() {
        return fsThr;
    }

    @Contract(pure = true)
    public String getQtlPath() {
        return qtlPath;
    }

    @Contract(pure = true)
    public String getVcfPath() {
        return vcfPath;
    }

    @Contract(pure = true)
    public String getOutPath() {
        return outPath;
    }

    @Contract(pure = true)
    public String getRef() {
        return ref;
    }

    @Contract(pure = true)
    public List<String> getBamFiles() {
        return bamFiles;
    }

    @Contract(pure = true)
    public List<String> getConcFiles() {
        return concFiles;
    }

    @Contract(pure = true)
    public int getMinIndiv() {
        return minIndiv;
    }

    @Contract(pure = true)
    public double getFraction() {
        return fraction;
    }

    @Contract(pure = true)
    public int getMethodsNbr() {
        return methodsNbr;
    }
}
