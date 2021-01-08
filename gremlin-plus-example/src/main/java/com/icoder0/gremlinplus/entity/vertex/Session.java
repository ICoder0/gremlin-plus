package com.icoder0.gremlinplus.entity.vertex;

import com.icoder0.gremlinplus.annotation.GraphLabel;
import com.icoder0.gremlinplus.annotation.VertexId;
import com.icoder0.gremlinplus.annotation.VertexProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author bofa1ex
 * @since 2020/12/10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@GraphLabel("<SESSION>")
public class Session extends Evict {
    @VertexId
    private Object id;
    @VertexProperty(value = "[PROTOTYPE]", serializable = false)
    private Object prototype;
}
