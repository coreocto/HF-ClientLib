package org.coreocto.dev.hf.clientlib.parser;

import org.coreocto.dev.hf.clientlib.Constants;
import sun.rmi.runtime.Log;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TxtFileParserImpl implements IFileParser {

    private static final String TAG = "TxtFileParserImpl";

    @Override
    public List<String> getText(File file) {
        List<String> result = new ArrayList<>();

        BufferedReader in = null;

        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(file), Constants.UTF8));
            String tempStr = null;

            while ((tempStr = in.readLine()) != null) {
                tempStr = tempStr.toLowerCase();
                result.addAll(Arrays.asList(tempStr.split(Constants.SPACE)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
            }
        }

        return result;
    }
}
