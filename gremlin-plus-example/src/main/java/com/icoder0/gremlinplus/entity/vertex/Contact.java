package com.icoder0.gremlinplus.entity.vertex;

import com.icoder0.gremlinplus.Application;
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
@GraphLabel("<CONTACT>")
public class Contact {
    @VertexId
    private Object id;
    @VertexProperty("[NAME]")
    private String name;
    @VertexProperty("[ACCOUNT]")
    private Long account;
    @VertexProperty("[PWD]")
    private String pwd;
    @VertexProperty("[STATUS]")
    private Byte status;
    @VertexProperty("[IS_DELETE]")
    private Byte isDelete;
    @VertexProperty("[AVATAR_URL]")
    private String avatarUrl;
    @VertexProperty("[PERMISSION]")
    private Byte permission;
    @VertexProperty("[LAST_ONLINE_TIME]")
    private LocalDateTime lastOnlineTime;
    @VertexProperty("[CREATE_TIME]")
    private LocalDateTime createTime;
    @VertexProperty("[UPDATE_TIME]")
    private LocalDateTime updateTime;

    @VertexProperty(value = "[VERIFY_KEY]", serializable = false)
    private String verifyKey;
    @VertexProperty(value = "[PARK_THREAD]", serializable = false)
    private Thread parkThread;
    @VertexProperty(value = "[PROTOTYPE]", serializable = false)
    private Application.Bot prototype;
}
