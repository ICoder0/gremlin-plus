package com.icoder0.gremlinplus.entity.vertex;

import com.icoder0.gremlinplus.annotation.GraphLabel;
import com.icoder0.gremlinplus.annotation.VertexId;
import com.icoder0.gremlinplus.annotation.VertexProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author bofa1ex
 * @since 2020/12/10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@GraphLabel("<SCRIPT>")
public class Script {
    @VertexId
    private Object id;
    @VertexProperty("[CONTENT]")
    private String content;
    @VertexProperty("[ENV]")
    private String env;
    @VertexProperty("[TYPE]")
    private Integer type;
    @VertexProperty("[KEYWORDS]")
    private List<String> keywords;
    @VertexProperty("[IS_DELETE]")
    private Byte isDelete;
    @VertexProperty("[CREATE_TIME]")
    private LocalDateTime createTime;
    @VertexProperty("[UPDATE_TIME]")
    private LocalDateTime updateTime;
}
