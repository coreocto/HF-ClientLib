package org.coreocto.dev.hf.clientlib.sse.mces;

import java.util.ArrayList;
import java.util.List;

public class X {
    private int ind;
    private int leafpos;
    private int num;
    private int len;
    private String f1;
    private List<String> f2;

    public X(){
        f2 = new ArrayList<>();
    }

    public int getInd() {
        return ind;
    }

    public void setInd(int ind) {
        this.ind = ind;
    }

    public int getLeafpos() {
        return leafpos;
    }

    public void setLeafpos(int leafpos) {
        this.leafpos = leafpos;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public String getF1() {
        return f1;
    }

    public void setF1(String f1) {
        this.f1 = f1;
    }

    public List<String> getF2() {
        return f2;
    }

    public void setF2(List<String> f2) {
        this.f2 = f2;
    }

}
