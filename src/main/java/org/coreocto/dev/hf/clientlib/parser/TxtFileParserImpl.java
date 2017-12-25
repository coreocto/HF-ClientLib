package org.coreocto.dev.hf.clientlib.parser;

import org.coreocto.dev.hf.clientlib.Constants;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TxtFileParserImpl implements IFileParser {

    private static final String TAG = "TxtFileParserImpl";

    @Override
    public List<String> getText(File file) {
        List<String> result = null;
        try {
            result = getText(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public List<String> getText(InputStream inputStream) {
        List<String> result = new ArrayList<>();

        BufferedReader in = null;

        try {
            in = new BufferedReader(new InputStreamReader(inputStream, Constants.ENCODING_UTF8));
            String tempStr = null;

            while ((tempStr = in.readLine()) != null) {
                tempStr = tempStr.toLowerCase();
                result.addAll(Arrays.asList(tempStr.split(Constants.REGEX_SPACE)));
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
