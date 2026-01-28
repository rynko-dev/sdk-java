package dev.rynko.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Pagination metadata.
 */
public class PaginationMeta {

    @JsonProperty("page")
    private int page;

    @JsonProperty("limit")
    private int limit;

    @JsonProperty("total")
    private int total;

    @JsonProperty("totalPages")
    private int totalPages;

    public PaginationMeta() {
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
