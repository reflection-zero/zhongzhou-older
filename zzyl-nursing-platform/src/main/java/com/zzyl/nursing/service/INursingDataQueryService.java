package com.zzyl.nursing.service;

import java.util.Map;

public interface INursingDataQueryService
{
    /**
     * 根据意图查询养老数据，严格按账户隔离
     * @param intent 意图类型 (BED/RESERVATION/CHECKIN/NURSING/PRICE/ROOM/ELDER/HEALTH)
     * @param userId 用户ID
     * @param userType 用户类型 0=admin,1=family_member
     * @return 结构化的养老数据
     */
    Map<String, Object> queryDataByIntent(String intent, Long userId, Integer userType);
}
