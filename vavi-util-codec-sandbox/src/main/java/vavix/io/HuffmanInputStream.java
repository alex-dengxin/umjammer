/*
 * Wavelet Audio Compression
 * 
 * http://www.toblave.org/soundcompression/
 */

package vavix.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;


/** */
class InNode {
    /** */
    public InNode(InNode p, InNode c1, InNode c2, int n, int c, int w) {
        child = new InNode[2];
        parent = p;
        child[0] = c1;
        child[1] = c2;
        value = n;
        count = c;
        which = w;
    }

    /** */
    InNode parent;

    /** */
    InNode child[];

    /** */
    int value;

    /** */
    int count;

    /** */
    int which;
}


/**
 * HuffmanInputStream. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 080516 nsano initial version <br>
 */
public class HuffmanInputStream extends FilterInputStream {

    /** */
    private InNode zeroNode;

    /** */
    private InNode rootNode;

    /** */
    private InNode escapeNode;

    /** */
    private BitInputStream bis;

    /** */
    private int bits;

    /** */
    private InNode addnode() {
        zeroNode.child[0] = new InNode(zeroNode, null, null, 0, 1, 0);
        InNode n = new InNode(zeroNode, null, null, 0, 0, 1);
        zeroNode.child[1] = n;
        zeroNode.count = 1;
        zeroNode = zeroNode.child[0];
        return n;
    }

    /** */
    private void swap(InNode a, InNode b) {
        a.parent.child[a.which] = b;
        b.parent.child[b.which] = a;

        InNode tn = a.parent;
        a.parent = b.parent;
        b.parent = tn;

        int t = a.which;
        a.which = b.which;
        b.which = t;
    }

    /** */
    private void update(InNode n) {
        while (n != null) {
            n.count++;
            if ((n.parent != null) && (n.parent.parent != null)) {
                InNode uncle = n.parent.parent.child[n.parent.which ^ 1];
                if (n.count > uncle.count) {
                    swap(n, uncle);
                }
            }
            n = n.parent;
        }
    }

    /** */
    public int readEscape(int bits) throws IOException {
        return bis.read(bits);
    }

    /** */
    public int readInt() throws IOException {
        InNode n = rootNode;
        int rv;
        while (n.child[0] != null) {
            n = n.child[bis.readBit()];
        }
        if (n == zeroNode) {
            rv = bis.read(bits);
            n = addnode();
            n.value = rv;
        } else {
            rv = n.value;
        }
        n.count++;
        update(n.parent);
        return rv;
    }

    /**
     * @param b bits 
     */
    public HuffmanInputStream(InputStream _in, int b) throws IOException {
        super(_in);
        bits = b;
        bis = new BitInputStream(in);
        rootNode = new InNode(null, null, null, 0, 2, 0);
        zeroNode = new InNode(rootNode, null, null, 0, 1, 0);
        escapeNode = new InNode(rootNode, null, null, -1, 1, 1);
        rootNode.child[0] = zeroNode;
        rootNode.child[1] = escapeNode;
    }
}

/* */
