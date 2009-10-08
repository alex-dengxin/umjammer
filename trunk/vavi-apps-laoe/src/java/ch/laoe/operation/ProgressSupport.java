/*
 * Copyright (c) 2001 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package ch.laoe.operation;

import java.io.Serializable;

import javax.swing.event.EventListenerList;


/**
 * Editor 機構のの基本実装クラスです．
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 010820 nsano initial version <br>
 *          0.10 020503 nsano use EventListenerList <br>
 *          0.11 020503 nsano repackage <br>
 *          0.12 020510 nsano repackage <br>
 *          0.20 020510 nsano deprecate open/close <br>
 */
public class ProgressSupport implements Serializable {

    /** The editor listeners */
    private EventListenerList listenerList = new EventListenerList();

    /** EditorListener を追加します． */
    public void addEditorListener(ProgressListener l) {
        listenerList.add(ProgressListener.class, l);
    }

    /** EditorListener を削除します． */
    public void removeEditorListener(ProgressListener l) {
        listenerList.remove(ProgressListener.class, l);
    }

    /** エディタがオープンされたイベントを発行します． */
    public void entrySubProgress(ProgressEvent ev) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ProgressListener.class) {
                ((ProgressListener) listeners[i + 1]).entrySubProgress(ev);
            }
        }
    }

    /** エディタがアップデートされたイベントを発行します． */
    public void setProgress(ProgressEvent ev) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ProgressListener.class) {
                ((ProgressListener) listeners[i + 1]).setProgress(ev);
            }
        }
    }

    /** エディタがクローズされたイベントを発行します． */
    public void exitSubProgress(ProgressEvent ev) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ProgressListener.class) {
                ((ProgressListener) listeners[i + 1]).exitSubProgress(ev);
            }
        }
    }
}

/* */
