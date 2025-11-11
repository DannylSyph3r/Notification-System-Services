package dev.slethware.apigateway.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private String error;
    private String message;
    private PaginationMeta meta;

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, null, message, null);
    }

    public static <T> ApiResponse<T> error(String error, String message) {
        return new ApiResponse<>(false, null, error, message, null);
    }
}