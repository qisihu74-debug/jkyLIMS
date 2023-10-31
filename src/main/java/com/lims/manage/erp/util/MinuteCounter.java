package com.lims.manage.erp.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.util
 * @desc
 * @date 2023-10-31 13:21
 * @Copyright © 河南交科院
 */
public class MinuteCounter {
    private static final int MASK = 0x7FFFFFFF;
    private final AtomicInteger atom;

    public MinuteCounter() {
        atom = new AtomicInteger(0);
    }

    public final int incrementAndGet() {
        return atom.incrementAndGet() & MASK;
    }

    public int get() {
        return atom.get() & MASK;
    }

    public void set(int newValue) {
        atom.set(newValue & MASK);
    }
}

