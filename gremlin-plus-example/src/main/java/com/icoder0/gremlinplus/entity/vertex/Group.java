package com.icoder0.gremlinplus.entity.vertex;

import com.icoder0.gremlinplus.annotation.GraphLabel;
import com.icoder0.gremlinplus.annotation.VertexId;
import com.icoder0.gremlinplus.annotation.VertexProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author bofa1ex
 * @since 2020/12/10
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@GraphLabel("<GROUP>")
public class Group {
    @VertexId
    private Object id;
    @VertexProperty("[NAME]")
    private String name;
    @VertexProperty("[ACCOUNT]")
    private Long account;
    @VertexProperty("[GROUP_SETTINGS]")
    private GroupSetting groupSetting;
    @VertexProperty("[AVATAR_URL]")
    private String avatarUrl;
    @VertexProperty("[IS_DELETE]")
    private Byte isDelete;
    @VertexProperty("[CREATE_TIME]")
    private LocalDateTime createTime;
    @VertexProperty("[UPDATE_TIME]")
    private LocalDateTime updateTime;
}
