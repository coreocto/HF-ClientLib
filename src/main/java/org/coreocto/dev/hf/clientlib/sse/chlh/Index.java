package org.coreocto.dev.hf.clientlib.sse.chlh;

import java.util.ArrayList;
import java.util.List;

public class Index {
    public String docId = null;
    private List<String> bloomFilters = new ArrayList<>();

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public List<String> getBloomFilters() {
        return bloomFilters;
    }

    public void setBloomFilters(List<String> bloomFilters) {
        this.bloomFilters = bloomFilters;
    }
}
