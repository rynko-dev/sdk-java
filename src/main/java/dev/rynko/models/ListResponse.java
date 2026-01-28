package dev.rynko.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Paginated list response.
 *
 * @param <T> The type of items in the list
 */
public class ListResponse<T> {

    @JsonProperty("data")
    private List<T> data;

    @JsonProperty("meta")
    private PaginationMeta meta;

    public ListResponse() {
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public PaginationMeta getMeta() {
        return meta;
    }

    public void setMeta(PaginationMeta meta) {
        this.meta = meta;
    }

    /**
     * Returns whether there are more pages.
     */
    public boolean hasMore() {
        return meta != null && meta.getPage() < meta.getTotalPages();
    }
}
