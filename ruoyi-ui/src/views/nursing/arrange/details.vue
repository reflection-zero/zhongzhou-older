<template>
  <div class="app-container">
    <el-card v-loading="loading">
      <div slot="header"><span>任务详情</span></div>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="老人姓名">{{ form.elderName }}</el-descriptions-item>
        <el-descriptions-item label="项目名称">{{ form.projectName }}</el-descriptions-item>
        <el-descriptions-item label="床位编号">{{ form.bedNumber }}</el-descriptions-item>
        <el-descriptions-item label="护理员ID">{{ form.nursingId }}</el-descriptions-item>
        <el-descriptions-item label="预计服务时间">{{ parseTime(form.estimatedServerTime) }}</el-descriptions-item>
        <el-descriptions-item label="实际服务时间">{{ parseTime(form.realServerTime) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="form.status === 1" type="info">待执行</el-tag>
          <el-tag v-else-if="form.status === 2" type="success">已执行</el-tag>
          <el-tag v-else-if="form.status === 3" type="danger">已关闭</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="执行图片">{{ form.taskImage }}</el-descriptions-item>
        <el-descriptions-item label="执行记录" :span="2">{{ form.mark }}</el-descriptions-item>
        <el-descriptions-item label="取消原因" :span="2">{{ form.cancelReason }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ form.remark }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script>
import { getNursingTask } from "@/api/nursing/nursingTask"

export default {
  name: "ArrangeDetails",
  data() {
    return { loading: false, form: {} }
  },
  created() {
    const id = this.$route.query.id
    if (id) {
      this.loading = true
      getNursingTask(id).then(response => { this.form = response.data; this.loading = false })
    }
  }
}
</script>
