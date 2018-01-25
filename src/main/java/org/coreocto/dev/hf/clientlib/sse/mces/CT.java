package org.coreocto.dev.hf.clientlib.sse.mces;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CT {
    private Map<String, String> C = new HashMap<>();
    private Map<String, String> L = new HashMap<>();
    private Map<String, List<String>> D = new HashMap<>();

    public Map<String, String> getC() {
        return C;
    }

    public Map<String, String> getL() {
        return L;
    }

    public Map<String, List<String>> getD() {
        return D;
    }
}
