<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="设备名称" prop="deviceName">
        <el-input v-model="queryParams.deviceName" placeholder="请输入设备名称" clearable style="width: 200px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="请选择类型" clearable style="width: 200px">
          <el-option label="老人异常数据" :value="0" />
          <el-option label="设备异常数据" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 200px">
          <el-option label="待处理" :value="0" />
          <el-option label="已处理" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['nursing:alertData:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="设备名称" align="center" prop="deviceName" :show-overflow-tooltip="true" />
      <el-table-column label="产品名称" align="center" prop="productName" />
      <el-table-column label="功能标识" align="center" prop="functionId" />
      <el-table-column label="数据值" align="center" prop="dataValue" />
      <el-table-column label="报警原因" align="center" prop="alertReason" :show-overflow-tooltip="true" />
      <el-table-column label="类型" align="center" prop="type" width="120">
        <template slot-scope="scope">
          <el-tag :type="scope.row.type === 0 ? 'warning' : 'info'">{{ scope.row.type === 0 ? '老人异常' : '设备异常' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === 0 ? 'danger' : 'success'">{{ scope.row.status === 0 ? '待处理' : '已处理' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="处理人" align="center" prop="processorName" />
      <el-table-column label="处理时间" align="center" prop="processingTime" width="160">
        <template slot-scope="scope">{{ parseTime(scope.row.processingTime) }}</template>
      </el-table-column>
      <el-table-column label="报警时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">{{ parseTime(scope.row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['nursing:alertData:edit']">处理</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['nursing:alertData:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" label-width="100px">
        <el-form-item label="处理结果" prop="processingResult">
          <el-input v-model="form.processingResult" type="textarea" placeholder="请输入处理结果" :rows="4" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态">
            <el-option label="待处理" :value="0" />
            <el-option label="已处理" :value="1" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listAlertData, getAlertData, updateAlertData, delAlertData } from "@/api/nursing/alertData"

export default {
  name: "AlertData",
  data() {
    return {
      loading: true, ids: [], single: true, multiple: true, showSearch: true, total: 0, list: [], title: "", open: false,
      queryParams: { pageNum: 1, pageSize: 10, deviceName: undefined, type: undefined, status: undefined },
      form: {}
    }
  },
  created() { this.getList() },
  methods: {
    getList() {
      this.loading = true
      listAlertData(this.queryParams).then(response => { this.list = response.rows; this.total = response.total; this.loading = false })
    },
    cancel() { this.open = false; this.reset() },
    reset() { this.form = { id: undefined, processingResult: undefined, status: undefined } },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.resetForm("queryForm"); this.handleQuery() },
    handleSelectionChange(selection) { this.ids = selection.map(item => item.id); this.single = selection.length != 1; this.multiple = !selection.length },
    handleUpdate(row) {
      this.reset()
      getAlertData(row.id || this.ids).then(response => { this.form = response.data; this.open = true; this.title = "处理报警" })
    },
    submitForm() {
      updateAlertData(this.form).then(() => { this.$modal.msgSuccess("处理成功"); this.open = false; this.getList() })
    },
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$modal.confirm('是否确认删除该报警数据？').then(function() { return delAlertData(ids) }).then(() => { this.getList(); this.$modal.msgSuccess("删除成功") }).catch(() => {})
    }
  }
}
</script>
