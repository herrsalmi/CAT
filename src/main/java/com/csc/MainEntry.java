package com.csc;

import com.handlers.ArgsHandler;
import com.handlers.VennHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainEntry {

    public static final String VERSION = "0.4.3a";
    public static final String EXTENSION = ".tab";
    public static final String CONC_OUT_DIR = "concordance/";
    public static final String VENN_OUT_DIR = "venn/";

    private static final Logger LOGGER = LogManager.getRootLogger();

    private MainEntry(String[] args) {

        ArgsHandler.INSTANCE.parseArgs(args);

        switch (ArgsHandler.INSTANCE.getMode()) {
            case CONCORDANCE:
                ConcordanceAnalysis concordanceHandler = new ConcordanceAnalysis();
                concordanceHandler.run();
                break;
            case VENN:
                VennHandler venn = new VennHandler();
                venn.compFiles(venn.getAllConcordantVars());
                venn.writeToVcf();
                break;
            default:
                throw new UnsupportedOperationException();
        }

        LOGGER.info("DONE !");
    }

    public static void main(String[] args) {

        new MainEntry(args);
    }

}
