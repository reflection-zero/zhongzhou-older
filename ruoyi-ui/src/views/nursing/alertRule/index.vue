<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="规则名称" prop="alertRuleName">
        <el-input v-model="queryParams.alertRuleName" placeholder="请输入规则名称" clearable style="width: 200px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 200px">
          <el-option label="禁用" :value="0" />
          <el-option label="启用" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['nursing:alertRule:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['nursing:alertRule:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['nursing:alertRule:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="规则名称" align="center" prop="alertRuleName" />
      <el-table-column label="产品名称" align="center" prop="productName" />
      <el-table-column label="设备名称" align="center" prop="deviceName" />
      <el-table-column label="功能名称" align="center" prop="functionName" />
      <el-table-column label="运算符" align="center" prop="operator" width="80" />
      <el-table-column label="阈值" align="center" prop="value" width="80" />
      <el-table-column label="持续周期" align="center" prop="duration" width="80" />
      <el-table-column label="数据类型" align="center" prop="alertDataType" width="120">
        <template slot-scope="scope">{{ scope.row.alertDataType === 0 ? '老人异常数据' : '设备异常数据' }}</template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template slot-scope="scope">
          <el-switch v-model="scope.row.status" active-value="1" inactive-value="0" @change="handleStatusChange(scope.row)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['nursing:alertRule:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['nursing:alertRule:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="650px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="规则名称" prop="alertRuleName">
              <el-input v-model="form.alertRuleName" placeholder="请输入规则名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备名称" prop="deviceName">
              <el-input v-model="form.deviceName" placeholder="请输入设备名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="运算符" prop="operator">
              <el-select v-model="form.operator" placeholder="请选择运算符">
                <el-option label=">" value=">" />
                <el-option label=">=" value=">=" />
                <el-option label="<" value="<" />
                <el-option label="<=" value="<=" />
                <el-option label="=" value="=" />
                <el-option label="!=" value="!=" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="阈值" prop="value">
              <el-input-number v-model="form.value" placeholder="请输入阈值" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="持续周期" prop="duration">
              <el-input-number v-model="form.duration" :min="0" placeholder="请输入持续周期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="沉默周期" prop="alertSilentPeriod">
              <el-input-number v-model="form.alertSilentPeriod" :min="0" placeholder="请输入沉默周期" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="报警数据类型" prop="alertDataType">
              <el-select v-model="form.alertDataType" placeholder="请选择">
                <el-option label="老人异常数据" :value="0" />
                <el-option label="设备异常数据" :value="1" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :label="0">禁用</el-radio>
                <el-radio :label="1">启用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="生效时段" prop="alertEffectivePeriod">
              <el-input v-model="form.alertEffectivePeriod" placeholder="请输入报警生效时段" />
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
import { listAlertRule, getAlertRule, addAlertRule, updateAlertRule, delAlertRule } from "@/api/nursing/alertRule"

export default {
  name: "AlertRule",
  data() {
    return {
      loading: true, ids: [], single: true, multiple: true, showSearch: true, total: 0, list: [], title: "", open: false,
      queryParams: { pageNum: 1, pageSize: 10, alertRuleName: undefined, status: undefined },
      form: {},
      rules: { alertRuleName: [{ required: true, message: "规则名称不能为空", trigger: "blur" }] }
    }
  },
  created() { this.getList() },
  methods: {
    getList() {
      this.loading = true
      listAlertRule(this.queryParams).then(response => { this.list = response.rows; this.total = response.total; this.loading = false })
    },
    handleStatusChange(row) { updateAlertRule(row).then(() => { this.$modal.msgSuccess("状态更新成功") }) },
    cancel() { this.open = false; this.reset() },
    reset() {
      this.form = { id: undefined, alertRuleName: undefined, productKey: undefined, productName: undefined, deviceName: undefined, iotId: undefined, functionName: undefined, functionId: undefined, operator: undefined, value: undefined, duration: undefined, alertEffectivePeriod: undefined, alertSilentPeriod: undefined, alertDataType: undefined, status: 1, remark: undefined }
      this.resetForm("form")
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.resetForm("queryForm"); this.handleQuery() },
    handleSelectionChange(selection) { this.ids = selection.map(item => item.id); this.single = selection.length != 1; this.multiple = !selection.length },
    handleAdd() { this.reset(); this.open = true; this.title = "添加报警规则" },
    handleUpdate(row) {
      this.reset()
      getAlertRule(row.id || this.ids).then(response => { this.form = response.data; this.open = true; this.title = "修改报警规则" })
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != undefined) {
            updateAlertRule(this.form).then(() => { this.$modal.msgSuccess("修改成功"); this.open = false; this.getList() })
          } else {
            addAlertRule(this.form).then(() => { this.$modal.msgSuccess("新增成功"); this.open = false; this.getList() })
          }
        }
      })
    },
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$modal.confirm('是否确认删除该报警规则？').then(function() { return delAlertRule(ids) }).then(() => { this.getList(); this.$modal.msgSuccess("删除成功") }).catch(() => {})
    }
  }
}
</script>
