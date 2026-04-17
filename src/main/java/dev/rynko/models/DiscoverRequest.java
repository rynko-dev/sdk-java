package dev.rynko.models;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Request for discovering extraction schema from sample files.
 */
public class DiscoverRequest {

    private List<File> files;
    private String instructions;

    private DiscoverRequest() {
    }

    public List<File> getFiles() { return files; }
    public String getInstructions() { return instructions; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<File> files = new ArrayList<>();
        private String instructions;

        public Builder file(File file) {
            this.files.add(file);
            return this;
        }

        public Builder files(List<File> files) {
            this.files = files;
            return this;
        }

        public Builder instructions(String instructions) {
            this.instructions = instructions;
            return this;
        }

        public DiscoverRequest build() {
            if (files == null || files.isEmpty()) {
                throw new IllegalArgumentException("At least one file is required");
            }

            DiscoverRequest request = new DiscoverRequest();
            request.files = this.files;
            request.instructions = this.instructions;
            return request;
        }
    }
}
