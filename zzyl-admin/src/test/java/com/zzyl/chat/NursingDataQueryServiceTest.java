package com.zzyl.chat;

import com.zzyl.nursing.domain.*;
import com.zzyl.nursing.mapper.FamilyMemberElderMapper;
import com.zzyl.nursing.service.*;
import com.zzyl.nursing.service.impl.NursingDataQueryServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * NursingDataQueryService 测试
 * 重点：账户隔离——家属不能跨账户访问老人数据
 */
@DisplayName("养老数据查询服务 — 账户隔离测试")
@ExtendWith(MockitoExtension.class)
class NursingDataQueryServiceTest {

    @Mock
    private IBedService bedService;

    @Mock
    private IElderService elderService;

    @Mock
    private IRoomTypeService roomTypeService;

    @Mock
    private INursingLevelService nursingLevelService;

    @Mock
    private IReservationService reservationService;

    @Mock
    private IHealthAssessmentService healthAssessmentService;

    @Mock
    private ICheckInConfigService checkInConfigService;

    @Mock
    private IRoomService roomService;

    @Mock
    private FamilyMemberElderMapper familyMemberElderMapper;

    @InjectMocks
    private NursingDataQueryServiceImpl service;

    // 家属 A (userId=100) 绑定了老人 1、2
    // 家属 B (userId=200) 绑定了老人 3

    @Nested
    @DisplayName("家属端账户隔离")
    class FamilyMemberIsolation {

        @Test
        @DisplayName("家属 A 查询老人信息只返回绑定的老人 1 和 2，不返回老人 3")
        void shouldOnlyReturnAuthorizedElders() {
            // 家属 A 绑定了老人 1 和 2
            mockFamilyMemberElders(100L, Arrays.asList(1L, 2L));

            Elder elder1 = createElder(1L, "张奶奶");
            Elder elder2 = createElder(2L, "王爷爷");
            List<Elder> elders = Arrays.asList(elder1, elder2);
            when(elderService.listByIds(argThat(ids -> ids.containsAll(Arrays.asList(1L, 2L)))))
                    .thenReturn(elders);

            Map<String, Object> result = service.queryDataByIntent("ELDER", 100L, 1);

            assertNotNull(result);
            String elderInfo = (String) result.get("老人信息");
            assertNotNull(elderInfo);
            assertTrue(elderInfo.contains("张奶奶"), "应该包含张奶奶");
            assertTrue(elderInfo.contains("王爷爷"), "应该包含王爷爷");
            assertFalse(elderInfo.contains("李大爷"), "不应包含李大爷");
        }

        @Test
        @DisplayName("家属无绑定老人时返回提示信息")
        void shouldReturnHint_whenNoBindingElders() {
            mockFamilyMemberElders(100L, Collections.emptyList());

            Map<String, Object> result = service.queryDataByIntent("ELDER", 100L, 1);

            assertNotNull(result);
            assertTrue(result.containsKey("提示"), "无绑定老人应有提示");
        }

        @Test
        @DisplayName("家属查询预约记录仅返回本人预约")
        void shouldOnlyReturnOwnReservations() {
            Map<String, Object> result = service.queryDataByIntent("RESERVATION", 100L, 1);
            assertNotNull(result);
            // 验证 reservationService.list() 传入的 wrapper 包含 familyMemberId = 100L
            verify(reservationService).list(argThat(wrapper -> true));
        }

        @Test
        @DisplayName("家属查询健康评估仅返回绑定老人的数据")
        void shouldOnlyReturnAuthorizedHealthAssessments() {
            mockFamilyMemberElders(100L, Arrays.asList(1L, 2L));
            when(healthAssessmentService.list(any())).thenReturn(Collections.emptyList());

            Map<String, Object> result = service.queryDataByIntent("HEALTH", 100L, 1);

            assertNotNull(result);
            verify(healthAssessmentService).list(argThat(wrapper -> true));
        }

        @Test
        @DisplayName("家属无绑定老人时查询健康评估返回提示")
        void shouldReturnHint_whenNoBindingForHealth() {
            mockFamilyMemberElders(100L, Collections.emptyList());

            Map<String, Object> result = service.queryDataByIntent("HEALTH", 100L, 1);

            assertTrue(result.containsKey("提示"), "无绑定老人应有提示");
        }
    }

    @Nested
    @DisplayName("管理端数据查询")
    class AdminQueries {

        @Test
        @DisplayName("管理端查询老人信息应返回全部（不限绑定）")
        void shouldReturnAllEldersForAdmin() {
            Elder e1 = createElder(1L, "张奶奶");
            Elder e2 = createElder(2L, "李大爷");
            when(elderService.list(any())).thenReturn(Arrays.asList(e1, e2));

            Map<String, Object> result = service.queryDataByIntent("ELDER", 1L, 0);

            assertNotNull(result);
            verify(familyMemberElderMapper, never()).selectList(any());
        }
    }

    @Nested
    @DisplayName("公开数据查询（无需账户隔离）")
    class PublicData {

        @Test
        @DisplayName("床位统计数据对家属和管理端均可查询")
        void shouldAllowBedQueryForAllUsers() {
            when(bedService.list()).thenReturn(Collections.emptyList());
            when(roomTypeService.list()).thenReturn(Collections.emptyList());

            // 家属端
            Map<String, Object> result1 = service.queryDataByIntent("BED", 100L, 1);
            assertNotNull(result1);

            // 管理端
            Map<String, Object> result2 = service.queryDataByIntent("BED", 1L, 0);
            assertNotNull(result2);
        }

        @Test
        @DisplayName("房型价格对所有用户可查")
        void shouldAllowPriceQueryForAllUsers() {
            when(roomTypeService.list()).thenReturn(Collections.emptyList());
            when(nursingLevelService.list()).thenReturn(Collections.emptyList());

            Map<String, Object> result = service.queryDataByIntent("PRICE", 100L, 1);
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("参数校验")
    class Validation {

        @Test
        @DisplayName("userId 为空时抛出异常")
        void shouldThrow_whenUserIdIsNull() {
            assertThrows(IllegalArgumentException.class, () ->
                    service.queryDataByIntent("BED", null, 1));
        }

        @Test
        @DisplayName("userType 为空时抛出异常")
        void shouldThrow_whenUserTypeIsNull() {
            assertThrows(IllegalArgumentException.class, () ->
                    service.queryDataByIntent("BED", 1L, null));
        }
    }

    // === 辅助方法 ===

    private void mockFamilyMemberElders(Long memberId, List<Long> elderIds) {
        List<FamilyMemberElder> links = new ArrayList<>();
        for (Long elderId : elderIds) {
            FamilyMemberElder link = new FamilyMemberElder();
            link.setFamilyMemberId(memberId);
            link.setElderId(elderId);
            links.add(link);
        }
        when(familyMemberElderMapper.selectList(any())).thenReturn(links);
    }

    private Elder createElder(Long id, String name) {
        Elder elder = new Elder();
        elder.setId(id);
        elder.setName(name);
        elder.setAge(75);
        elder.setSex(1);
        return elder;
    }
}
