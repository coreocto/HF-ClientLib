package org.coreocto.dev.hf.clientlib.sse.mces;

import java.util.*;

public class SuffixTree {

    int globalNodeIndex = 0;    //this index will increment by 1 every time a node is created under this tree

    class Node extends HashMap<String, Node> {
        /**
         * Follows a link to get a child node.  If no such link
         * exists, then create and attach an empty child node.
         */
        public Node getOrPut(String c) {
            Node child = this.get(c);
            if (child == null) {
                this.put(c, child = new Node(this, c));
            }
            return child;
        }

        private Node parent;
        private String edgeLabel;

        public Node(Node parent, String edgeLabel) {
            this.parent = parent;
            this.edgeLabel = edgeLabel;
            id = globalNodeIndex++;
        }

        public boolean isLeaf() {
            return this.isEmpty();
        }

        public boolean isOnlyChild() {
            return this.parent.size() == 1;
        }

        public boolean isRoot() {
            return this.parent == null;
        }

        private int id = 0;


        public int getId() {
            return id;
        }

        public String toString() {
            String tmp = "node " + id;
            if (this.isLeaf()) {
                tmp += ", leaf " + leafId;
            }
            return tmp;
        }

        private int leafId = -1;

        public int getLeafId() {
            return leafId;
        }

        //TODO path()

        public String initpath() {
            if (this.isRoot()) {
                return "";
            } else {
                StringBuilder str = new StringBuilder();
                Node ref = this;
                while (ref != null) {
                    str.insert(0, ref.edgeLabel);
                    ref = ref.parent;
                }
                return str.toString();
            }
        }

        //leaf() moved to SuffixTree

        public int len() {
            return this.initpath().length();
        }

        //TODO: ind()

        public int leafpos() {
            if (this.isLeaf()) {
                return this.leafId;
            } else {

                int output = -1;

                Stack<Node> s = new Stack<Node>();
                s.add(this);
                while (s.isEmpty() == false) {
                    Node x = s.pop();
                    for (Node n : x.values()) {
                        s.add(n);
                    }
                    if (x.isLeaf()) {
                        output = x.leafId;
                        break;
                    }

                }

                return output;
            }
        }

        public int num() {
            if (this.isRoot()) {
                return 0;
            } else {
                return this.size();
            }
        }
    }

    private Node root;

    public Node getRoot() {
        return root;
    }

    /**
     * Creates the suffix tree from the given string.
     */
    public SuffixTree(String source) {
        this.root = new Node(null, "");
        for (int i = 0; i < source.length(); i++) {
            Node n = this.root.getOrPut(source.charAt(i) + "");
            for (int j = i + 1; j < source.length(); j++) {
                n = n.getOrPut(source.charAt(j) + "");
            }
        }
    }

    public boolean contains(String target) {
        Node n = this.root;
        for (int i = 0; i < target.length(); i++) {
            n = n.get(target.charAt(i) + "");
            if (n == null) {
                return false;
            }
        }
        return true;
    }

    public void dfs(Node rootNode) {
        Stack<Node> s = new Stack<Node>();
        s.add(rootNode);
        while (s.isEmpty() == false) {
            Node x = s.pop();
            for (Node n : x.values()) {
                s.add(n);
            }
            System.out.print(" " + x);
        }
    }

    public List<Node> getChild(Node rootNode) {
        List<Node> result = new ArrayList<>();
        Stack<Node> s = new Stack<Node>();
        s.add(rootNode);
        while (s.isEmpty() == false) {
            Node x = s.pop();
            for (Node n : x.values()) {
                s.add(n);
            }
            result.add(x);
        }
        return result;
    }

    public void assignLeafIdDfs() {
        Stack<Node> s = new Stack<Node>();
        s.add(root);

        int idx = 0;

        while (s.isEmpty() == false) {
            Node x = s.pop();
            for (Node n : x.values()) {
                s.add(n);
            }
            if (x.isLeaf()) {
                x.leafId = idx++;
            }
        }
    }

    public List<Node> getAllNodes(boolean leafOnly) {
        List<Node> result = new ArrayList<>();
        Stack<Node> s = new Stack<Node>();
        s.add(root);
        while (s.isEmpty() == false) {
            Node x = s.pop();
            for (Node n : x.values()) {
                s.add(n);
            }
            if (leafOnly && x.isLeaf()) {
                result.add(x);
            } else {
                result.add(x);
            }
        }
        return result;
    }

//    public CT constructDictionary(Node rootNode, IByteCipher byteCipher){
//
//        CT result = new CT();
//
//        Stack<Node> s = new Stack<Node>();
//        s.add(rootNode);
//        while (s.isEmpty() == false) {
//            Node x = s.pop();
//            for (Node n:x.values()){
//                s.add(n);
//            }
//            int j = x.size();
//            Iterator<Node> childIter = x.values().iterator();
//            int i = 0;
//            while (childIter.hasNext()){
//                Node child = childIter.next();
//                String g2 = initpath(child);
//
//                i++;
//            }
//            for (int i=0;i<j;i++){
//                String g2 = initpath(x.)
//            }
//            System.out.print(" " + x);
//        }
//
//        int sourceLen = source.length();
//        Random cRand = new Random(new BigInteger(k3).)
//        for (int i=0;i<sourceLen;i++){
//            result.getC().add()
//        }
//    }

    public void optimize() {
        Node ref = root;

        if (ref.isRoot()) {

        } else if (ref.size() == 1) {
            Node child = ref.values().iterator().next();
            StringBuilder str = new StringBuilder();
            str.append(ref.edgeLabel);
            str.append(child.edgeLabel);
            ref.edgeLabel = str.toString();
            ref.clear();
            ref.putAll(child);
        }

//        List<Node> allLeaves = new ArrayList<>();
//        getLeaves(root, allLeaves);
//        for (Node leaf : allLeaves) {
//            Node ref = leaf;
//            while (ref!=null) {
//
//                if (ref.parent!=null && ref.isOnlyChild()) {
//                    Node parent = ref.parent;
//                    StringBuilder str = new StringBuilder();
//                    str.append(parent.edgeLabel);
//                    str.append(ref.edgeLabel);
//                    parent.remove(ref.edgeLabel);
//                    parent.edgeLabel = str;
//                    parent.put(str,ref);
//                }
//                ref = ref.parent;
//            }
//        }
    }


//    public int len(Node node) {
//        return initpath(node).length();
//    }

//    public int num(Node node) {
//        if (node.isRoot()){
//            return 0;
//        }else{
//            return node.size();
//        }
//    }

//    public String getPath(Node node) {
//        if (node.parent == null) {
//            return "";
//        } else {
//            StringBuilder str = new StringBuilder();
//            str.append(this.initpath(node));
//            Node ref = node;
//            while (true) {
//                if (ref.size() != 1) {
//                    break;
//                } else {
//                    ref = ref.values().iterator().next(); //get the "first" child
//                    str.append(ref.edgeLabel);
//                }
//            }
//            return str.toString();
//        }
//    }

//    public void getLeaves(Node node, List<Node> container) {
//        if (node != null && node.isLeaf()) {
//            container.add(node);
//        } else {
//            for (Map.Entry<String, Node> entry : node.entrySet()) {
//                getLeaves(entry.getValue(), container);
//            }
//        }
//    }

    //ind()
//    public int ind()

    public Set<String> getStrings(Node rootNode, List<String> container) {
        Set<String> strings = new HashSet<>();
        for (Map.Entry<String, Node> entry : rootNode.entrySet()) {
            container.add(entry.getKey().toString());
            strings.addAll(getStrings(entry.getValue(), container));
            if (!container.isEmpty()) {
                strings.add(container.toString());
            }
            container.clear();
        }
        //System.out.println(strings);
        //if (!container.isEmpty()) {
        //strings.add(container.toString());
        //}
        return strings;
    }

//    public static void main(String[] args) {
//        String testStr = "cocoon";
//        SuffixTree sTree = new SuffixTree(testStr);
//        List<String> list = Util.getSubstrings(testStr, false);
//        for (String s : list) {
//            String exists = sTree.contains(s) ? "exists" : "doesn't exist";
//            System.out.printf("Input: %source %source\n", s, exists);
//        }
//
//        sTree.dfs(sTree.root);
//
//
////        if (false){
////            sTree.optimize();
////
////            List<Node> nodes = new ArrayList<Node>();
////            sTree.getLeaves(sTree.root, nodes);
////            System.out.println(nodes);
////
////            for (Node cur : nodes) {
////                System.out.println(sTree.initpath(cur.parent));
////                System.out.println(sTree.getPath(cur.parent));
////            }
////        }
//
//        /* String[] input = new String[] {
//            "ba",
//            "ban",
//            "ana",
//            "anas",
//            "nan",
//            "anans",
//            "ananas",
//            "n",
//            "source",
//            "as",
//            "naab",
//            "baan",
//            "aan",
//        };
//        for (String source : input) {
//            String exists = sTree.contains(source) ? "exists" : "doesn't exist";
//            System.out.printf("Input: %source %source\n", source, exists);
//        } */
//    }
}