<template>
  <div class="app-container">
    <el-card v-loading="loading">
      <div slot="header"><span>告警规则详情</span></div>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="规则名称">{{ form.alertRuleName }}</el-descriptions-item>
        <el-descriptions-item label="产品名称">{{ form.productName }}</el-descriptions-item>
        <el-descriptions-item label="设备名称">{{ form.deviceName }}</el-descriptions-item>
        <el-descriptions-item label="功能名称">{{ form.functionName }}</el-descriptions-item>
        <el-descriptions-item label="运算符">{{ form.operator }}</el-descriptions-item>
        <el-descriptions-item label="阈值">{{ form.value }}</el-descriptions-item>
        <el-descriptions-item label="持续周期">{{ form.duration }}</el-descriptions-item>
        <el-descriptions-item label="沉默周期">{{ form.alertSilentPeriod }}</el-descriptions-item>
        <el-descriptions-item label="报警生效时段">{{ form.alertEffectivePeriod }}</el-descriptions-item>
        <el-descriptions-item label="数据类型">{{ form.alertDataType === 0 ? '老人异常数据' : '设备异常数据' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="form.status === 1 ? 'success' : 'info'">{{ form.status === 1 ? '启用' : '禁用' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备注">{{ form.remark }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script>
import { getAlertRule } from "@/api/nursing/alertRule"

export default {
  name: "AlertRuleDetails",
  data() {
    return { loading: false, form: {} }
  },
  created() {
    const id = this.$route.query.id
    if (id) {
      this.loading = true
      getAlertRule(id).then(response => { this.form = response.data; this.loading = false })
    }
  }
}
</script>
