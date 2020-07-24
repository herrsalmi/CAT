package com.handlers;

import com.csc.BadFileFormatException;
import com.csc.ReadGroupException;
import com.utils.ProgressBar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * project checkStatusConcordance
 * Created by ayyoub on 6/6/17.
 */
public class BamHandler {

    private static final Logger LOGGER = LogManager.getRootLogger();

    // contains samples names from BAM files
    private final ArrayList<String> lSampleName;

    public BamHandler() {
        lSampleName = new ArrayList<>();
    }


    //TODO why I am not using this?
    public List<String> getSamplesNames() {
        return lSampleName.isEmpty() ? null : lSampleName;
    }

    /********************************************
     * Extracts samples names from BAM files
     *
     * @param bamPath List of BAM files paths
     */
    public void extractSamplesNames(List<String> bamPath) throws ReadGroupException, BadFileFormatException {
        StringBuilder sb = new StringBuilder();
        // in case of sample name is not the last tag
        bamPath.stream().map(bam -> new StringBuilder("samtools view -H ").append(bam)).forEach(cmd -> {
            Process p;
            try {
                p = Runtime.getRuntime().exec(cmd.toString());
                p.waitFor();
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(p.getInputStream()));

                String line;
                sb.setLength(0);
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            if (sb.length() == 0) {
                throw new BadFileFormatException("BAM");
            }
            Pattern pattern = Pattern.compile("@RG.*SM:(.+)\t*.*");
            Matcher matcher = pattern.matcher(sb.toString());
            if (!matcher.find()) {
                throw new ReadGroupException();
            }
            String sample = matcher.group(1);
            if (sample.contains("\t"))
                sample = sample.split("\t")[0];
            lSampleName.add(sample);
        });


    }

    /********************************************
     * Calls samtools mpileup from system
     *
     * @param bamPath List of BAM files paths
     */
    public void mpileup(List<String> bamPath, String ref) {
        // faidx indexing of reference genome
        StringBuilder faidxCmd = new StringBuilder("samtools faidx ");
        faidxCmd.append(ref);
        ProgressBar pb = new ProgressBar();
        try {
            Process p = Runtime.getRuntime().exec(faidxCmd.toString());
            //TODO add message output
            LOGGER.info("Indexing reference genome ...");
            pb.start();
            p.waitFor();
            pb.stopAnimation();

        } catch (IOException e) {
            LOGGER.error("samtools not found !");
            System.exit(6);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }

        //TODO add message output, this takes so much time
        // samtools mpileup
        StringBuilder mpileupCmd = new StringBuilder("samtools mpileup -l pos.tmp --reference ");
        mpileupCmd.append(ref).append(" ");
        bamPath.forEach(e -> mpileupCmd.append(e).append(" "));
        mpileupCmd.append("> pileup.tmp");
        pb.reset();
        try {
            Process p = Runtime.getRuntime().exec(mpileupCmd.toString());
            LOGGER.info("Running samtools mpileup ...");
            pb.start();
            p.waitFor();
            pb.stopAnimation();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            System.out.println(sb.toString());

        } catch (IOException e) {
            LOGGER.error("samtools not found !");
            System.exit(6);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }

    }
}
