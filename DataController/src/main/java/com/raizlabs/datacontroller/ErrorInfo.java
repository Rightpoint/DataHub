package com.raizlabs.datacontroller;

public class ErrorInfo extends ResultInfo{
    private final String errorTitle;
    private final String errorDescription;

    public ErrorInfo(String errorTitle, String errorDescription, int dataSourceType, boolean isFreshDataIncoming) {
        this(errorTitle, errorDescription, dataSourceType, isFreshDataIncoming, 0, 0);
    }

    public ErrorInfo(String errorTitle, String errorDescription, int dataSourceType, boolean isFreshDataIncoming, long lastUpdatedTimestamp, long dataLifeSpan) {
        super(null, dataSourceType, isFreshDataIncoming, lastUpdatedTimestamp, dataLifeSpan);
        this.errorTitle = errorTitle;
        this.errorDescription = errorDescription;
    }

    public String getErrorTitle() {
        return errorTitle;
    }

    public String getErrorDescription() {
        return errorDescription;
    }
}
