package com.icoder0.gremlinplus.entity.vertex;

import com.icoder0.gremlinplus.annotation.GraphLabel;
import com.icoder0.gremlinplus.annotation.VertexId;
import com.icoder0.gremlinplus.annotation.VertexProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author bofa1ex
 * @since 2020/12/10
 */
@Data
@Builder
@GraphLabel
public class Record {
    @VertexId
    private Object id;
    @VertexProperty("[KEYWORD]")
    private String keyword;
    @VertexProperty("[CONTENT]")
    private String content;
    @VertexProperty("[CREATE_TIME]")
    private LocalDateTime createTime;
}
