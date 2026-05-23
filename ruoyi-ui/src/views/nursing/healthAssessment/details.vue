<template>
  <div class="app-container">
    <el-card v-loading="loading">
      <div slot="header"><span>健康评估详情</span></div>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="老人姓名">{{ form.elderName }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ form.idCard }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ form.gender === 0 ? '男' : '女' }}</el-descriptions-item>
        <el-descriptions-item label="年龄">{{ form.age }}</el-descriptions-item>
        <el-descriptions-item label="健康评分">{{ form.healthScore }}</el-descriptions-item>
        <el-descriptions-item label="危险等级">{{ form.riskLevel }}</el-descriptions-item>
        <el-descriptions-item label="是否建议入住">{{ form.suggestionForAdmission === 0 ? '建议' : '不建议' }}</el-descriptions-item>
        <el-descriptions-item label="推荐护理等级">{{ form.nursingLevelName }}</el-descriptions-item>
        <el-descriptions-item label="入住情况">{{ form.admissionStatus === 0 ? '已入住' : '未入住' }}</el-descriptions-item>
        <el-descriptions-item label="评估时间">{{ parseTime(form.assessmentTime) }}</el-descriptions-item>
        <el-descriptions-item label="体检机构">{{ form.physicalExamInstitution }}</el-descriptions-item>
        <el-descriptions-item label="体检报告URL">{{ form.physicalReportUrl }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ form.remark }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script>
import { getHealthAssessment } from "@/api/nursing/healthAssessment"

export default {
  name: "HealthAssessmentDetails",
  data() {
    return { loading: false, form: {} }
  },
  created() {
    const id = this.$route.query.id
    if (id) {
      this.loading = true
      getHealthAssessment(id).then(response => { this.form = response.data; this.loading = false })
    }
  }
}
</script>
