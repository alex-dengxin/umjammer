package com.imageresize4j;


final class Class_w {

    private Class_w() {
    }

    static int sub_b9(int i, int j, int k) {
        return sub_db(255, k);
    }

    static int sub_db(int i, int j) {
        if (j < 0) {
            j = 0;
        } else if (j > i) {
            j = i;
        }
        return j;
    }
}
