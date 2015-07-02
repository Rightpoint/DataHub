package com.raizlabs.datacontroller;

public class ResultInfo<Data> {
    public static final int MEMORY_DATA = 1;
    public static final int DISK_DATA = 2;
    public static final int WEB_DATA = 4;

    private final Data data;
    private final int dataSourceType;
    private final boolean isFreshDataIncoming;
    private final long lastUpdatedTimestamp;
    private final long dataLifeSpan;

    public ResultInfo(Data data, int dataSourceType, boolean isFreshDataIncoming, long lastUpdatedTimestamp, long dataLifeSpan) {
        this.data = data;
        this.dataSourceType = dataSourceType;
        this.isFreshDataIncoming = isFreshDataIncoming;
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
        this.dataLifeSpan = dataLifeSpan;
    }

    public Data getData() {
        return data;
    }

    public int getDataSourceType() {
        return dataSourceType;
    }

    public boolean isFreshDataIncoming() {
        return isFreshDataIncoming;
    }

    public long getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    public long getDataLifeSpan(){
        return dataLifeSpan;
    }
}
