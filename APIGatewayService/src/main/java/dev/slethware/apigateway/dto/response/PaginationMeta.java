package dev.slethware.apigateway.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaginationMeta {

    private int total;
    private int limit;
    private int page;

    @JsonProperty("total_pages")
    private int totalPages;

    @JsonProperty("has_next")
    private boolean hasNext;

    @JsonProperty("has_previous")
    private boolean hasPrevious;
}