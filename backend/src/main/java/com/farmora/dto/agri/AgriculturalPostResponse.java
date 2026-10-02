package com.farmora.dto.agri;

import com.farmora.dto.auth.UserResponse;
import com.farmora.model.AgriCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgriculturalPostResponse {
    private Long id;
    private UserResponse author;
    private String title;
    private AgriCategory category;
    private String content;
    private String province;
    private String targetCrop;
    private String season;
    private Boolean isPinned;
    private String sourceUrl;
    private LocalDateTime createdAt;
}
