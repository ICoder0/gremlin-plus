package com.icoder0.gremlinplus.entity.vertex;

import com.icoder0.gremlinplus.annotation.VertexId;
import com.icoder0.gremlinplus.annotation.VertexLabel;
import com.icoder0.gremlinplus.annotation.VertexProperty;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * @author bofa1ex
 * @since 2020/12/4
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@VertexLabel("<USER>")
public class User {
    @VertexId
    private Object id;
    @VertexProperty("[NAME]")
    private String name;
    @VertexProperty("[PWD]")
    private String pwd;
    @VertexProperty("[PHONE_NUM]")
    private String phoneNum;
    @VertexProperty("[MAIL]")
    private String mail;
    @VertexProperty("[IS_DELETE]")
    private Byte isDelete;
    @VertexProperty("[STATUS]")
    private Byte status;
    @VertexProperty("[CREATE_TIME]")
    private LocalDateTime createTime;
    @VertexProperty("[UPDATE_TIME]")
    private LocalDateTime updateTime;
}
