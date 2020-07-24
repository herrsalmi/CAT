package com.handlers;

import com.csc.HypergeometricDistribution;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Project checkStatusConcordance
 * Created by ayyoub on 3/6/17.
 */
public class FisherStrandHandler {

    // syncs countReads(int offset) threads
    private volatile boolean stop = false;


    /*****************************************
     * Count reads on call ficherStrand()
     *
     * @param offset index of individual
     *
     * @return list of fisher strand values
     */
    public List<Double> countReads(int offset) {
        LinkedBlockingQueue<ArrayList<Integer>> queue = new LinkedBlockingQueue<>();
        ArrayList<Double> fs = new ArrayList<>(10000);

        CountDownLatch latch = new CountDownLatch(2);

        new Thread(() -> {
            String line;
            String[] info;
            int index = 4 + offset * 3;

            // pattern for parsing pileup reads
            Pattern refFw = Pattern.compile("\\.");
            Pattern refRv = Pattern.compile(",");

            Pattern indelFw = Pattern.compile("[-+][0-9]+[ATGC]+");
            Pattern indelRv = Pattern.compile("[-+][0-9]+[atgc]+");

            Pattern snpFw = Pattern.compile("(?<![0-9])+[atgc]");
            Pattern snpRv = Pattern.compile("(?<![0-9])+[atgc]");
            try {
                BufferedReader br = new BufferedReader(new FileReader("pileup.tmp"));

                while ((line = br.readLine()) != null) {
                    info = line.split("\t");
                    //in case of depth = 0
                    if (info[3].equals("0")) {
                        ArrayList<Integer> tList = new ArrayList<>(4);
                        tList.add(0);
                        tList.add(0);
                        tList.add(0);
                        tList.add(0);
                        queue.offer(tList);
                        continue;
                    }

                    //hashCount.putIfAbsent(info[0], new LinkedHashMap<>(10000));
                    //hashCount.get(info[0]).put(info[1], new ArrayList<>(4));

                    ArrayList<Integer> tList = new ArrayList<>(4);

                    // reference forward and reverse count
                    tList.add(countOccurrences(refFw, info[index]));
                    tList.add(countOccurrences(refRv, info[index]));

                    // indels count
                    if (indelFw.matcher(info[index]).find() || indelRv.matcher(info[index]).find()) {
                        tList.add(countOccurrences(indelFw, info[index]));
                        tList.add(countOccurrences(indelRv, info[index]));
                    } // end of indels count

                    // SNPs count
                    if (snpFw.matcher(info[index]).find() || snpRv.matcher(info[index]).find()) {
                        // no indels found before
                        if (tList.size() == 2) {
                            tList.add(countOccurrences(snpFw, info[index]));
                            tList.add(countOccurrences(snpRv, info[index]));
                        } else {
                            // compare the number of indels and SNPs
                            if (countOccurrences(snpFw, info[index]) + countOccurrences(snpRv, info[index]) >
                                    countOccurrences(indelFw, info[index]) + countOccurrences(indelFw, info[index])) {
                                tList.add(countOccurrences(snpFw, info[index]));
                                tList.add(countOccurrences(snpRv, info[index]));
                            }
                        }
                    } else { // end of SNPs count
                        tList.add(0);
                        tList.add(0);
                    }
                    queue.offer(tList);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            stop = true;
            latch.countDown();
        }).start();

        new Thread(() -> {
            while (!stop || !queue.isEmpty()) {
                try {
                    List<Integer> l = queue.poll(100, TimeUnit.MILLISECONDS);
                    if (l == null)
                        continue;
                    fs.add(fisherStrand(l));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            }
            latch.countDown();
        }).start();


        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }

        return fs;
    }


    /*****************************************************************
     * Count occurrence in a sequence given a pattern
     *
     * @param pattern regex
     *
     * @param sequence sequence to partially match with the pattern
     *
     * @return number of occurrences
     */
    private int countOccurrences(Pattern pattern, String sequence) {
        Matcher matcher = pattern.matcher(sequence);
        ArrayList<String> list = new ArrayList<>();
        while (matcher.find()) {
            list.add(matcher.group());
        }
        TreeSet<String> set = new TreeSet<>(list);
        List<Long> occurrences = set.stream().map(e -> list.stream().filter(el -> el.equals(e)).count()).collect(Collectors.toList());
        // in case of not matches are found
        if (occurrences.isEmpty())
            return 0;

        // System.out.println(new ArrayList<>(set).get(occurrences.indexOf(Collections.max(occurrences))));

        return Integer.parseInt(Collections.max(occurrences).toString());
    }


    /**************************************************************************************************************
     * Calculate fisher strand as given in <a
     * href="http://gatkforums.broadinstitute.org/gatk/discussion/8056/statistical-methods-fisher-s-exact-test">
     * GATK </a>
     *
     * @param list 2x2 matrix as a list
     *
     * @return phred scaled fisher strand value
     */
    // values closer to 0 are good
    private double fisherStrand(List<Integer> list) {
        BigDecimal pvalOriginal = HypergeometricDistribution.getValue(list, 15, RoundingMode.HALF_EVEN);

        int[][] T1 = {{list.get(0), list.get(1)}, {list.get(2), list.get(3)}};
        ArrayList<BigDecimal> pvals = new ArrayList<>();
        while (T1[0][1] > 0 && T1[1][0] > 0) {
            T1[0][0]++;
            T1[0][1]--;
            T1[1][0]--;
            T1[1][1]++;
            pvals.add(HypergeometricDistribution.getValue(T1, 15, RoundingMode.HALF_EVEN));
        }

        int[][] T2 = {{list.get(0), list.get(1)}, {list.get(2), list.get(3)}};
        while (T2[0][0] > 0 && T2[1][1] > 0) {
            T2[0][0]--;
            T2[0][1]++;
            T2[1][0]++;
            T2[1][1]--;
            pvals.add(HypergeometricDistribution.getValue(T2, 15, RoundingMode.HALF_EVEN));
        }

        BigDecimal sum = pvals.stream().filter(e -> e.doubleValue() <= pvalOriginal.doubleValue()).reduce(BigDecimal.ZERO, BigDecimal::add);

        sum = sum.add(pvalOriginal);

        if (sum.doubleValue() > 1)
            return 0.00000;
        else
            return Math.abs(-10 * Math.log10(sum.doubleValue()));
    }

}
