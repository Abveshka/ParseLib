package org.example;

public abstract class ParserSettings {
    public String baseUrl;
    public String prefix;
    protected int startPoint;
    protected int endPoint;

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getPrefix() {
        return prefix;
    }

    public int getStartPoint() {
        return startPoint;
    }

    public int getEndPoint() {
        return endPoint;
    }
}