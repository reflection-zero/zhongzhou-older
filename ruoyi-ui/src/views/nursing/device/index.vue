<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="设备名称" prop="deviceName">
        <el-input v-model="queryParams.deviceName" placeholder="请输入设备名称" clearable style="width: 200px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="产品名称" prop="productName">
        <el-input v-model="queryParams.productName" placeholder="请输入产品名称" clearable style="width: 200px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['nursing:device:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['nursing:device:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['nursing:device:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="设备名称" align="center" prop="deviceName" />
      <el-table-column label="物联网设备ID" align="center" prop="iotId" width="200" :show-overflow-tooltip="true" />
      <el-table-column label="产品名称" align="center" prop="productName" />
      <el-table-column label="产品Key" align="center" prop="productKey" width="120" />
      <el-table-column label="绑定位置" align="center" prop="bindingLocation" />
      <el-table-column label="位置类型" align="center" prop="locationType" width="100">
        <template slot-scope="scope">{{ scope.row.locationType === 0 ? '随身设备' : '固定设备' }}</template>
      </el-table-column>
      <el-table-column label="门禁" align="center" prop="haveEntranceGuard" width="80">
        <template slot-scope="scope">{{ scope.row.haveEntranceGuard === 1 ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['nursing:device:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['nursing:device:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="650px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="设备名称" prop="deviceName">
              <el-input v-model="form.deviceName" placeholder="请输入设备名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="物联网设备ID" prop="iotId">
              <el-input v-model="form.iotId" placeholder="请输入物联网设备ID" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="产品Key" prop="productKey">
              <el-input v-model="form.productKey" placeholder="请输入产品Key" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品名称" prop="productName">
              <el-input v-model="form.productName" placeholder="请输入产品名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="绑定位置" prop="bindingLocation">
              <el-input v-model="form.bindingLocation" placeholder="请输入绑定位置" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="位置类型" prop="locationType">
              <el-select v-model="form.locationType" placeholder="请选择位置类型">
                <el-option label="随身设备" :value="0" />
                <el-option label="固定设备" :value="1" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="物理位置类型" prop="physicalLocationType">
              <el-select v-model="form.physicalLocationType" placeholder="请选择物理位置类型">
                <el-option label="楼层" :value="0" />
                <el-option label="房间" :value="1" />
                <el-option label="床位" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="门禁" prop="haveEntranceGuard">
              <el-select v-model="form.haveEntranceGuard" placeholder="是否有门禁">
                <el-option label="否" :value="0" />
                <el-option label="是" :value="1" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="位置备注" prop="deviceDescription">
              <el-input v-model="form.deviceDescription" type="textarea" placeholder="请输入位置备注" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listDevice, getDevice, addDevice, updateDevice, delDevice } from "@/api/nursing/device"

export default {
  name: "Device",
  data() {
    return {
      loading: true, ids: [], single: true, multiple: true, showSearch: true, total: 0, list: [], title: "", open: false,
      queryParams: { pageNum: 1, pageSize: 10, deviceName: undefined, productName: undefined },
      form: {},
      rules: { deviceName: [{ required: true, message: "设备名称不能为空", trigger: "blur" }] }
    }
  },
  created() { this.getList() },
  methods: {
    getList() {
      this.loading = true
      listDevice(this.queryParams).then(response => { this.list = response.rows; this.total = response.total; this.loading = false })
    },
    cancel() { this.open = false; this.reset() },
    reset() {
      this.form = { id: undefined, iotId: undefined, secret: undefined, bindingLocation: undefined, locationType: undefined, physicalLocationType: undefined, deviceName: undefined, productKey: undefined, productName: undefined, deviceDescription: undefined, haveEntranceGuard: 0, nodeId: undefined, remark: undefined }
      this.resetForm("form")
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.resetForm("queryForm"); this.handleQuery() },
    handleSelectionChange(selection) { this.ids = selection.map(item => item.id); this.single = selection.length != 1; this.multiple = !selection.length },
    handleAdd() { this.reset(); this.open = true; this.title = "添加设备" },
    handleUpdate(row) {
      this.reset()
      getDevice(row.id || this.ids).then(response => { this.form = response.data; this.open = true; this.title = "修改设备" })
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != undefined) {
            updateDevice(this.form).then(() => { this.$modal.msgSuccess("修改成功"); this.open = false; this.getList() })
          } else {
            addDevice(this.form).then(() => { this.$modal.msgSuccess("新增成功"); this.open = false; this.getList() })
          }
        }
      })
    },
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$modal.confirm('是否确认删除该设备数据？').then(function() { return delDevice(ids) }).then(() => { this.getList(); this.$modal.msgSuccess("删除成功") }).catch(() => {})
    }
  }
}
</script>
