<template>
  <div class="app-container">
    <el-card v-loading="loading">
      <div slot="header"><span>在住管理详情</span></div>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="老人姓名">{{ form.elderName }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ form.idCardNo }}</el-descriptions-item>
        <el-descriptions-item label="入住床位">{{ form.bedNumber }}</el-descriptions-item>
        <el-descriptions-item label="护理等级">{{ form.nursingLevelName }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="form.status === 0 ? 'success' : 'info'">{{ form.status === 0 ? '已入住' : '已退住' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="入住开始时间">{{ parseTime(form.startDate) }}</el-descriptions-item>
        <el-descriptions-item label="入住结束时间">{{ parseTime(form.endDate) }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ form.remark }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script>
import { getCheckIn } from "@/api/nursing/checkIn"

export default {
  name: "CheckInDetails",
  data() {
    return { loading: false, form: {} }
  },
  created() {
    const id = this.$route.query.id
    if (id) {
      this.loading = true
      getCheckIn(id).then(response => { this.form = response.data; this.loading = false })
    }
  }
}
</script>
