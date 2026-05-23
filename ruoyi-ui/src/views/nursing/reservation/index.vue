<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="预约人" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入预约人姓名" clearable style="width: 200px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="手机号" prop="mobile">
        <el-input v-model="queryParams.mobile" placeholder="请输入手机号" clearable style="width: 200px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="请选择类型" clearable style="width: 200px">
          <el-option label="参观预约" :value="0" />
          <el-option label="探访预约" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 200px">
          <el-option label="待报道" :value="0" />
          <el-option label="已完成" :value="1" />
          <el-option label="取消" :value="2" />
          <el-option label="过期" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['nursing:reservation:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['nursing:reservation:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['nursing:reservation:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="预约人" align="center" prop="name" />
      <el-table-column label="手机号" align="center" prop="mobile" width="120" />
      <el-table-column label="探访人" align="center" prop="visitor" />
      <el-table-column label="预约时间" align="center" prop="time" width="160">
        <template slot-scope="scope">{{ parseTime(scope.row.time) }}</template>
      </el-table-column>
      <el-table-column label="预约类型" align="center" prop="type" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.type === 0 ? 'primary' : 'warning'">{{ scope.row.type === 0 ? '参观预约' : '探访预约' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status === 0">待报道</el-tag>
          <el-tag v-else-if="scope.row.status === 1" type="success">已完成</el-tag>
          <el-tag v-else-if="scope.row.status === 2" type="info">取消</el-tag>
          <el-tag v-else-if="scope.row.status === 3" type="danger">过期</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['nursing:reservation:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['nursing:reservation:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="预约人姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入预约人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="mobile">
              <el-input v-model="form.mobile" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="探访人" prop="visitor">
              <el-input v-model="form.visitor" placeholder="请输入探访人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预约时间" prop="time">
              <el-date-picker v-model="form.time" type="datetime" placeholder="请选择预约时间" value-format="yyyy-MM-dd HH:mm:ss" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="预约类型" prop="type">
              <el-select v-model="form.type" placeholder="请选择类型">
                <el-option label="参观预约" :value="0" />
                <el-option label="探访预约" :value="1" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择状态">
                <el-option label="待报道" :value="0" />
                <el-option label="已完成" :value="1" />
                <el-option label="取消" :value="2" />
                <el-option label="过期" :value="3" />
              </el-select>
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
import { listReservation, getReservation, addReservation, updateReservation, delReservation } from "@/api/nursing/reservation"

export default {
  name: "Reservation",
  data() {
    return {
      loading: true, ids: [], single: true, multiple: true, showSearch: true, total: 0, list: [], title: "", open: false,
      queryParams: { pageNum: 1, pageSize: 10, name: undefined, mobile: undefined, type: undefined, status: undefined },
      form: {},
      rules: {
        name: [{ required: true, message: "预约人姓名不能为空", trigger: "blur" }],
        mobile: [{ required: true, message: "手机号不能为空", trigger: "blur" }]
      }
    }
  },
  created() { this.getList() },
  methods: {
    getList() {
      this.loading = true
      listReservation(this.queryParams).then(response => { this.list = response.rows; this.total = response.total; this.loading = false })
    },
    cancel() { this.open = false; this.reset() },
    reset() {
      this.form = { id: undefined, name: undefined, mobile: undefined, visitor: undefined, time: undefined, type: undefined, status: undefined, remark: undefined }
      this.resetForm("form")
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.resetForm("queryForm"); this.handleQuery() },
    handleSelectionChange(selection) { this.ids = selection.map(item => item.id); this.single = selection.length != 1; this.multiple = !selection.length },
    handleAdd() { this.reset(); this.open = true; this.title = "添加预约" },
    handleUpdate(row) {
      this.reset()
      getReservation(row.id || this.ids).then(response => { this.form = response.data; this.open = true; this.title = "修改预约" })
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != undefined) {
            updateReservation(this.form).then(() => { this.$modal.msgSuccess("修改成功"); this.open = false; this.getList() })
          } else {
            addReservation(this.form).then(() => { this.$modal.msgSuccess("新增成功"); this.open = false; this.getList() })
          }
        }
      })
    },
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$modal.confirm('是否确认删除该预约数据？').then(function() { return delReservation(ids) }).then(() => { this.getList(); this.$modal.msgSuccess("删除成功") }).catch(() => {})
    }
  }
}
</script>
