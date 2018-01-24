package org.coreocto.dev.hf.clientlib.sse.mces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CT {
    private List<String> C = new ArrayList<>();
    private List<String> L = new ArrayList<>();
    private Map<String, List<String>> D = new HashMap<>();

    public List<String> getC() {
        return C;
    }

    public List<String> getL() {
        return L;
    }

    public Map<String, List<String>> getD() {
        return D;
    }
}
