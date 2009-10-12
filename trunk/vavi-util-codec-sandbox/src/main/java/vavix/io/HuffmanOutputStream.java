/*
 * Wavelet Audio Compression
 * 
 * http://www.toblave.org/soundcompression/
 */

package vavix.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;


/** */
class Symbol {
    /** */
    public Symbol(int sym, int len) {
        symbol = sym;
        length = len;
    }

    /** */
    int symbol;

    /** */
    int length;
}


/** */
class Node {
    /** */
    public Node(Node p, Node c1, Node c2, int n, int d, int c, int w) {
        child = new Node[2];
        parent = p;
        child[0] = c1;
        child[1] = c2;
        code = n;
        depth = d;
        count = c;
        which = w;
    }

    /** */
    Node parent;

    /** */
    Node child[];

    /** */
    int code;

    /** */
    int depth;

    /** */
    int count;

    /** */
    int which;
}


/**
 * HuffmanOutputStream. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 080516 nsano initial version <br>
 */
public class HuffmanOutputStream extends FilterOutputStream {
    /** */
    private Map<Integer, Node> table;

    /** */
    private Node rootNode;

    /** */
    private Node zeroNode;

    /** */
    private Node escapeNode;

    /** */
    private BitOutputStream bos;

    /** */
    int bits;

    /** */
    private Node addnode() {
        zeroNode.child[0] = new Node(zeroNode, null, null, zeroNode.code << 1, zeroNode.depth + 1, 1, 0);
        Node node = new Node(zeroNode, null, null, (zeroNode.code << 1) + 1, zeroNode.depth + 1, 0, 1);
        zeroNode.child[1] = node;
        zeroNode = zeroNode.child[0];
        return node;
    }

    /** */
    private void docodes(Node n) {
        if (n != null) {
            n.depth = n.parent.depth + 1;
            n.code = ((n.parent.code) << 1) + n.which;
            docodes(n.child[0]);
            docodes(n.child[1]);
        }
    }

    /** */
    private void swap(Node a, Node b) {
        a.parent.child[a.which] = b;
        b.parent.child[b.which] = a;

        Node tn = a.parent;
        a.parent = b.parent;
        b.parent = tn;

        int t = a.which;
        a.which = b.which;
        b.which = t;

        docodes(a);
        docodes(b);
    }

    /** */
    private void update(Node n) {
        while (n != null) {
            n.count++;
            if ((n.parent != null) && (n.parent.parent != null)) {
                Node uncle = n.parent.parent.child[n.parent.which ^ 1];
                if (n.count > uncle.count) {
                    swap(n, uncle);
                }
            }
            n = n.parent;
        }
    }

    /** */
    public void flush() throws IOException {
        bos.flush();
    }

    // pass straight to bit writer
    public void writeEscape(int actual, int bits) throws IOException {
        bos.write(escapeNode.code, escapeNode.depth);
        escapeNode.count++;
        update(escapeNode.parent);
        bos.write(actual, bits);
    }

    /** */
    public void writeInt(int actual) throws IOException {
        Node n = table.get(actual);
        if (n == null) {
            bos.write(zeroNode.code, zeroNode.depth);
            bos.write(actual, bits);
            n = addnode();
            table.put(actual, n);
        } else {
            bos.write(n.code, n.depth);
        }
        n.count++;
        update(n.parent);
    }

    /** */
    public HuffmanOutputStream(OutputStream _out, int b) throws IOException {
        super(_out);
        bits = b;
        bos = new BitOutputStream(out);
        table = new HashMap<Integer, Node>(257);
        rootNode = new Node(null, null, null, 0, 0, 2, 0);
        zeroNode = new Node(rootNode, null, null, 0, 1, 1, 0);
        escapeNode = new Node(rootNode, null, null, 1, 1, 1, 1);
        rootNode.child[0] = zeroNode;
        rootNode.child[1] = escapeNode;
    }
}

/* */
