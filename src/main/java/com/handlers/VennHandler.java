package com.handlers;

import com.csc.MainEntry;
import com.utils.SubInformation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

/**
 * project checkStatusConcordance
 * Created by ayyoub on 7/10/17.
 */
public class VennHandler {

    private static final Logger LOGGER = LogManager.getRootLogger();

    private String chr;
    private final Set<SubInformation> variants = new TreeSet<>();


    public void compFiles(Map<String, List<SubInformation>> methods)  {

        LOGGER.info("Comparing files ...");

        if (!Files.isDirectory(Paths.get(MainEntry.VENN_OUT_DIR))) {
            try {
                Files.createDirectory(Paths.get(MainEntry.VENN_OUT_DIR));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try(BufferedWriter bw = new BufferedWriter(new FileWriter(
                MainEntry.VENN_OUT_DIR + ArgsHandler.INSTANCE.getOutPath() + MainEntry.EXTENSION))) {
            String sb = "#chr\tposition\tsupporting individuals\tconcordance\ttype\tconfirmed\talt identical?";
            bw.write(sb);
            bw.newLine();

            methods.forEach((k, v) -> variants.addAll(v));

            variants.forEach(e -> {
                try {
                    bw.write(chr + "\t");
                    bw.write(e.getPosition() + "\t");
                    bw.write(e.getTotalIndiv() + "\t");
                    bw.write(e.getPercentConc() + "\t");
                    bw.write(String.join("_", e.getSupportingMethods()) + "\t");
                    bw.write(e.getSupportingMethods().size() + "\t");
                    if (e.getSupportingMethods().size() == 1)
                        bw.write("-");
                    else
                        bw.write(e.isIdenticalAlt() ? "YES" : "NO");
                    bw.newLine();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeToVcf() {
        LOGGER.info("Writing results ...");
        StringBuilder sb = new StringBuilder();

        sb.append("##fileformat=VCFv4.1\n");
        sb.append("##fileDate=");
        sb.append(LocalDateTime.now().toLocalDate().toString());
        sb.append("\n");
        sb.append("##source=ConcordanceAnalysisToolkit\n");
        sb.append("##FORMAT=<ID=GT,Number=1,Type=String,Description=\"Genotype\">\n");
        sb.append("#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\tFORMAT\n");

        variants.stream().filter(e -> e.getSupportingMethods().size() >= ArgsHandler.INSTANCE.getMethodsNbr())
                .forEach(e -> sb.append(chr).append("\t").append(e.getPosition()).append("\t.\t").append(e.getRef())
                        .append("\t").append(e.getAlt()).append("\t.\t.\t.\t.\n"));

        try (BufferedWriter br = new BufferedWriter(new FileWriter(MainEntry.VENN_OUT_DIR + ArgsHandler.INSTANCE.getOutPath() + ".vcf"))) {
            br.write(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<SubInformation> extractConcVar(String file, int indivThreshold, double minFraction, String method) {
        List<SubInformation> listExtract = new ArrayList<>(100);
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            String alt;
            String ref;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("chr"))
                    continue;

                String[] info = line.split("\t");
                if (chr == null)
                    chr = info[0];
                ref = info[6];
                alt = info[7];
                if (Integer.parseInt(info[3]) >= indivThreshold && Double.parseDouble(info[4]) >= minFraction && info[5].equals("NO")) {
                    listExtract.add(new SubInformation(Long.parseLong(info[1]), Integer.parseInt(info[3]),
                            Double.parseDouble(info[4]), ref, alt, method));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return listExtract;
    }

    public Map<String, List<SubInformation>> getAllConcordantVars() {
        HashMap<String, List<SubInformation>> object = new HashMap<>();

        ArgsHandler.INSTANCE.getConcFiles().forEach(e -> object.putIfAbsent(e.substring(e.lastIndexOf("/") + 1, e.lastIndexOf(".")),
                extractConcVar(e, ArgsHandler.INSTANCE.getMinIndiv(),
                        ArgsHandler.INSTANCE.getFraction(), e.substring(e.lastIndexOf("/") + 1, e.lastIndexOf(".")))));
        return object;
    }
}
