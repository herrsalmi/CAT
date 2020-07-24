package com.handlers;

import java.io.*;

/**
 * Project checkStatusConcordance
 * Created by ayyoub on 6/6/17.
 */
public class VCFHandler {

    private String path;

    public VCFHandler(String path) {
        this.path = path;
    }

    /***************************************************
     * Extracts variants positions from VCF file
     */
    public void extractPositions() {
        String positionsPath = "pos.tmp";
        StringBuilder sb = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {

            String line;

            while ((line = br.readLine()) != null) {
                if (line.startsWith("#"))
                    continue;
                sb.append(line.split("\t")[0]).append(" ").append(line.split("\t")[1]).append("\n");

            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        try (BufferedWriter bw = new BufferedWriter(new FileWriter(positionsPath))) {

            bw.write(sb.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
