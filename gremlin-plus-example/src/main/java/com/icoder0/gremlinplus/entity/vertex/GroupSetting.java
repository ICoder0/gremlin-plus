package com.icoder0.gremlinplus.entity.vertex;

import lombok.Builder;
import lombok.Data;

/**
 * @author bofa1ex
 * @since 2020/12/10
 */
@Data
@Builder
public class GroupSetting {
    private String entranceAnnouncement;
    private boolean isMuteAll;
    private boolean isAllowMemberInvite;
    private boolean isAutoApproveEnabled;
    private boolean isAnonymousChatEnabled;
}
