package dev.rynko.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Extraction usage statistics.
 */
public class ExtractUsage {

    @JsonProperty("used")
    private int used;

    @JsonProperty("limit")
    private int limit;

    @JsonProperty("remaining")
    private int remaining;

    @JsonProperty("periodStart")
    private String periodStart;

    @JsonProperty("periodEnd")
    private String periodEnd;

    public ExtractUsage() {
    }

    public int getUsed() { return used; }
    public void setUsed(int used) { this.used = used; }

    public int getLimit() { return limit; }
    public void setLimit(int limit) { this.limit = limit; }

    public int getRemaining() { return remaining; }
    public void setRemaining(int remaining) { this.remaining = remaining; }

    public String getPeriodStart() { return periodStart; }
    public void setPeriodStart(String periodStart) { this.periodStart = periodStart; }

    public String getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(String periodEnd) { this.periodEnd = periodEnd; }

    @Override
    public String toString() {
        return "ExtractUsage{" +
                "used=" + used +
                ", limit=" + limit +
                ", remaining=" + remaining +
                '}';
    }
}
