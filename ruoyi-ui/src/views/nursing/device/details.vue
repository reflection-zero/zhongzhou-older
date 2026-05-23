<template>
  <div class="app-container">
    <el-card v-loading="loading">
      <div slot="header"><span>设备详情</span></div>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="设备名称">{{ form.deviceName }}</el-descriptions-item>
        <el-descriptions-item label="物联网设备ID">{{ form.iotId }}</el-descriptions-item>
        <el-descriptions-item label="产品Key">{{ form.productKey }}</el-descriptions-item>
        <el-descriptions-item label="产品名称">{{ form.productName }}</el-descriptions-item>
        <el-descriptions-item label="绑定位置">{{ form.bindingLocation }}</el-descriptions-item>
        <el-descriptions-item label="位置类型">{{ form.locationType === 0 ? '随身设备' : '固定设备' }}</el-descriptions-item>
        <el-descriptions-item label="物理位置类型">
          <span v-if="form.physicalLocationType === 0">楼层</span>
          <span v-else-if="form.physicalLocationType === 1">房间</span>
          <span v-else-if="form.physicalLocationType === 2">床位</span>
        </el-descriptions-item>
        <el-descriptions-item label="门禁">{{ form.haveEntranceGuard === 1 ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="设备秘钥">{{ form.secret }}</el-descriptions-item>
        <el-descriptions-item label="节点ID">{{ form.nodeId }}</el-descriptions-item>
        <el-descriptions-item label="位置备注">{{ form.deviceDescription }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ form.remark }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script>
import { getDevice } from "@/api/nursing/device"

export default {
  name: "DeviceDetails",
  data() {
    return { loading: false, form: {} }
  },
  created() {
    const id = this.$route.query.id
    if (id) {
      this.loading = true
      getDevice(id).then(response => { this.form = response.data; this.loading = false })
    }
  }
}
</script>
