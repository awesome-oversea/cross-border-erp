package com.aidotnet.erp.sys.domain;

public enum DataTrustLevel {
    A,
    B,
    C,
    D;

    public boolean canOverwrite(DataTrustLevel target) {
        return this.ordinal() <= target.ordinal();
    }
}
