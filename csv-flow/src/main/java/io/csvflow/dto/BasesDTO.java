package io.csvflow.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BasesDTO {
    private String name;
    private String description;
    private String nameTableTmp;
    private String nameTableRaw;
}
