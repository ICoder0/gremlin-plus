package com.icoder0.gremlinplus.entity.vertex;

import com.icoder0.gremlinplus.annotation.GraphLabel;
import com.icoder0.gremlinplus.annotation.VertexId;
import com.icoder0.gremlinplus.annotation.VertexProperty;
import lombok.Data;

/**
 * @author bofa1ex
 * @since 2021/1/5
 */
@Data
public class Evict {
    @VertexProperty("[EVICT]")
    private boolean evict;
}
