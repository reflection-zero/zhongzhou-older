package com.zzyl.nursing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzyl.common.utils.SecurityUtils;
import com.zzyl.nursing.domain.*;
import com.zzyl.nursing.mapper.FamilyMemberElderMapper;
import com.zzyl.nursing.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NursingDataQueryServiceImpl implements INursingDataQueryService {

    @Autowired
    private IBedService bedService;

    @Autowired
    private IElderService elderService;

    @Autowired
    private IRoomTypeService roomTypeService;

    @Autowired
    private INursingLevelService nursingLevelService;

    @Autowired
    private IReservationService reservationService;

    @Autowired
    private IHealthAssessmentService healthAssessmentService;

    @Autowired
    private ICheckInConfigService checkInConfigService;

    @Autowired
    private IRoomService roomService;

    @Autowired
    private FamilyMemberElderMapper familyMemberElderMapper;

    @Override
    public Map<String, Object> queryDataByIntent(String intent, Long userId, Integer userType) {
        validateUser(userId, userType);

        switch (intent) {
            case "BED":
                return queryBedAvailability();
            case "RESERVATION":
                return queryReservationStatus(userId, userType);
            case "CHECKIN":
                return queryCheckInConfig();
            case "NURSING":
                return queryNursingLevels();
            case "PRICE":
                return queryPricing();
            case "ROOM":
                return queryRoomInfo();
            case "ELDER":
                return queryElderInfo(userId, userType);
            case "HEALTH":
                return queryHealthAssessment(userId, userType);
            default:
                return Collections.emptyMap();
        }
    }

    // === 账户隔离校验 ===

    private void validateUser(Long userId, Integer userType) {
        if (userId == null || userType == null) {
            throw new IllegalArgumentException("用户ID和类型不能为空");
        }
    }

    private List<Long> getAuthorizedElderIds(Long userId, Integer userType) {
        if (userType == 0) {
            return null; // 管理端返回 null 表示查全部
        }
        // 家属端：仅查绑定的老人
        List<FamilyMemberElder> links = familyMemberElderMapper.selectList(
                new LambdaQueryWrapper<FamilyMemberElder>()
                        .eq(FamilyMemberElder::getFamilyMemberId, userId));
        List<Long> ids = links.stream().map(FamilyMemberElder::getElderId).collect(Collectors.toList());
        if (ids.isEmpty()) {
            log.warn("家属 userId={} 没有绑定任何老人，拒绝查询", userId);
        }
        return ids;
    }

    // === 各类数据查询 ===

    private Map<String, Object> queryBedAvailability() {
        Map<String, Object> data = new LinkedHashMap<>();

        List<Bed> allBeds = bedService.list();
        long total = allBeds.size();
        long occupied = allBeds.stream().filter(b -> b.getBedStatus() != null && b.getBedStatus() == 1).count();
        long available = total - occupied;

        data.put("床位总数", total + "张");
        data.put("已入住", occupied + "张");
        data.put("空闲", available + "张");

        // 按房型统计空闲床位（Room 通过 typeName 关联 RoomType）
        List<RoomType> roomTypes = roomTypeService.list();
        if (roomTypes != null && !roomTypes.isEmpty()) {
            StringBuilder roomBreakdown = new StringBuilder();
            for (RoomType rt : roomTypes) {
                List<Room> rooms = roomService.list(
                        new LambdaQueryWrapper<Room>().eq(Room::getTypeName, rt.getName()));
                List<Long> roomIds = rooms.stream().map(Room::getId).collect(Collectors.toList());
                if (!roomIds.isEmpty()) {
                    long freeInType = allBeds.stream()
                            .filter(b -> b.getBedStatus() != null && b.getBedStatus() == 0
                                    && roomIds.contains(b.getRoomId()))
                            .count();
                    if (freeInType > 0) {
                        roomBreakdown.append(rt.getName()).append("：空闲").append(freeInType).append("张（月费")
                                .append(rt.getPrice() != null ? rt.getPrice() + "元" : "未定价").append("）、");
                    }
                }
            }
            if (roomBreakdown.length() > 0) {
                roomBreakdown.setLength(roomBreakdown.length() - 1);
                data.put("房型分布", roomBreakdown.toString());
            }
        }

        return data;
    }

    private Map<String, Object> queryReservationStatus(Long userId, Integer userType) {
        Map<String, Object> data = new LinkedHashMap<>();

        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        if (userType == 1) {
            // 家属仅查本人（familyMemberId 存储在 BaseEntity.createBy）
            wrapper.eq(Reservation::getCreateBy, userId.toString());
        }
        wrapper.orderByDesc(Reservation::getCreateTime).last("LIMIT 10");
        List<Reservation> reservations = reservationService.list(wrapper);

        long pending = reservations.stream().filter(r -> r.getStatus() != null && r.getStatus() == 0).count();
        long approved = reservations.stream().filter(r -> r.getStatus() != null && r.getStatus() == 1).count();

        data.put("预约总数(最近)", reservations.size() + "条");
        data.put("待处理", pending + "条");
        data.put("已确认", approved + "条");

        if (!reservations.isEmpty()) {
            StringBuilder detail = new StringBuilder();
            for (Reservation r : reservations) {
                detail.append("[").append(r.getName()).append("] ")
                        .append(r.getMobile()).append(" - ")
                        .append(getReservationStatusName(r.getStatus()))
                        .append(" (").append(r.getCreateTime()).append("); ");
            }
            data.put("详情", detail.toString());
        }

        return data;
    }

    private Map<String, Object> queryCheckInConfig() {
        Map<String, Object> data = new LinkedHashMap<>();
        List<CheckInConfig> configs = checkInConfigService.list();
        if (configs != null && !configs.isEmpty()) {
            CheckInConfig config = configs.get(0);
            String nursing = config.getNursingLevelName() != null ? config.getNursingLevelName() : "未指定";
            String bedFee = config.getBedFee() != null ? config.getBedFee() + "元/月" : "未定";
            String deposit = config.getDeposit() != null ? config.getDeposit() + "元" : "未定";
            data.put("入住配置", "护理等级：" + nursing + "，床位费：" + bedFee + "，押金：" + deposit);
        } else {
            data.put("入住流程", "暂未配置入住流程，请联系工作人员咨询");
        }
        return data;
    }

    private Map<String, Object> queryNursingLevels() {
        Map<String, Object> data = new LinkedHashMap<>();
        List<NursingLevel> levels = nursingLevelService.list();
        if (levels != null && !levels.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (NursingLevel nl : levels) {
                sb.append(nl.getName()).append("：").append(nl.getDescription() != null ? nl.getDescription() : "暂无描述")
                        .append("（月费").append(nl.getFee() != null ? nl.getFee() + "元" : "未定价").append("）; ");
            }
            data.put("护理等级", sb.toString());
        } else {
            data.put("护理等级", "暂未配置护理等级信息");
        }
        return data;
    }

    private Map<String, Object> queryPricing() {
        Map<String, Object> data = new LinkedHashMap<>();
        List<RoomType> roomTypes = roomTypeService.list();
        if (roomTypes != null && !roomTypes.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (RoomType rt : roomTypes) {
                sb.append(rt.getName()).append("：月费")
                        .append(rt.getPrice() != null ? rt.getPrice() + "元" : "未定价")
                        .append("、床位").append(rt.getBedCount()).append("个; ");
            }
            data.put("房型价格", sb.toString());
        }
        List<NursingLevel> levels = nursingLevelService.list();
        if (levels != null && !levels.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (NursingLevel nl : levels) {
                sb.append(nl.getName()).append("：月费")
                        .append(nl.getFee() != null ? nl.getFee() + "元" : "未定价").append("; ");
            }
            data.put("护理费用", sb.toString());
        }
        return data;
    }

    private Map<String, Object> queryRoomInfo() {
        Map<String, Object> data = new LinkedHashMap<>();
        List<RoomType> roomTypes = roomTypeService.list();
        if (roomTypes != null) {
            for (RoomType rt : roomTypes) {
                List<Room> rooms = roomService.list(
                        new LambdaQueryWrapper<Room>().eq(Room::getTypeName, rt.getName()));
                data.put(rt.getName(), "共" + rooms.size() + "间（" + rt.getBedCount() + "床/间）月费"
                        + (rt.getPrice() != null ? rt.getPrice() + "元" : "未定价")
                        + "，" + (rt.getIntroduction() != null ? rt.getIntroduction() : "暂无介绍"));
            }
        }
        return data;
    }

    private Map<String, Object> queryElderInfo(Long userId, Integer userType) {
        Map<String, Object> data = new LinkedHashMap<>();

        List<Long> authorizedIds = getAuthorizedElderIds(userId, userType);
        if (userType == 1 && (authorizedIds == null || authorizedIds.isEmpty())) {
            data.put("提示", "您暂未绑定任何老人信息");
            return data;
        }

        List<Elder> elders;
        if (userType == 0) {
            elders = elderService.list(new LambdaQueryWrapper<Elder>().orderByDesc(Elder::getCreateTime).last("LIMIT 10"));
        } else {
            elders = elderService.listByIds(authorizedIds);
        }

        if (elders == null || elders.isEmpty()) {
            data.put("提示", "未找到关联的老人信息");
            return data;
        }

        StringBuilder sb = new StringBuilder();
        for (Elder e : elders) {
            String sexName = (e.getSex() != null && e.getSex() == 1) ? "男" : "女";
            sb.append(e.getName()).append("（").append(sexName).append("、")
                    .append(e.getAge() != null ? e.getAge() + "岁" : "未知年龄").append("）- ");
            if (e.getBedNumber() != null) {
                sb.append("床位：").append(e.getBedNumber()).append("; ");
            } else {
                sb.append("暂未入住; ");
            }
        }
        data.put("老人信息", sb.toString());
        return data;
    }

    private Map<String, Object> queryHealthAssessment(Long userId, Integer userType) {
        Map<String, Object> data = new LinkedHashMap<>();

        List<Long> authorizedIds = getAuthorizedElderIds(userId, userType);
        if (userType == 1 && (authorizedIds == null || authorizedIds.isEmpty())) {
            data.put("提示", "您暂未绑定任何老人，无法查询健康评估");
            return data;
        }

        LambdaQueryWrapper<HealthAssessment> wrapper = new LambdaQueryWrapper<>();
        if (userType == 1) {
            // HealthAssessment 无 elderId 字段，通过 elderName 匹配
            List<Elder> authorizedElders = elderService.listByIds(authorizedIds);
            List<String> authorizedNames = authorizedElders.stream()
                    .map(Elder::getName).collect(Collectors.toList());
            if (authorizedNames.isEmpty()) {
                data.put("提示", "未找到关联老人的健康评估");
                return data;
            }
            wrapper.in(HealthAssessment::getElderName, authorizedNames);
        }
        wrapper.orderByDesc(HealthAssessment::getCreateTime).last("LIMIT 5");
        List<HealthAssessment> assessments = healthAssessmentService.list(wrapper);

        if (assessments == null || assessments.isEmpty()) {
            data.put("提示", "暂未找到相关健康评估数据");
            return data;
        }

        StringBuilder sb = new StringBuilder();
        for (HealthAssessment a : assessments) {
            sb.append("评分").append(a.getHealthScore() != null ? a.getHealthScore() : "未评")
                    .append("（风险等级：").append(a.getRiskLevel() != null ? a.getRiskLevel() : "未知").append("）- ")
                    .append(a.getAssessmentTime() != null ? a.getAssessmentTime() : "未知时间").append("; ");
        }
        data.put("健康评估", sb.toString());
        return data;
    }

    private String getReservationStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待处理";
            case 1: return "已确认";
            case 2: return "已取消";
            case 3: return "已完成";
            default: return "状态" + status;
        }
    }
}
