<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="等级名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入等级名称" clearable style="width: 200px" @keyup.enter.native="handleQuery" />
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
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['nursing:nursingLevel:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['nursing:nursingLevel:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['nursing:nursingLevel:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="等级名称" align="center" prop="name" />
      <el-table-column label="护理费用(元/月)" align="center" prop="fee" />
      <el-table-column label="护理计划ID" align="center" prop="lplanId" />
      <el-table-column label="等级说明" align="center" prop="description" :show-overflow-tooltip="true" />
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
          <el-switch v-model="scope.row.status" active-value="1" inactive-value="0" @change="handleStatusChange(scope.row)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['nursing:nursingLevel:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['nursing:nursingLevel:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="等级名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入等级名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="护理费用" prop="fee">
              <el-input-number v-model="form.fee" :min="0" :precision="2" placeholder="请输入护理费用" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="护理计划ID" prop="lplanId">
              <el-input-number v-model="form.lplanId" :min="0" style="width: 100%" />
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
            <el-form-item label="等级说明" prop="description">
              <el-input v-model="form.description" type="textarea" placeholder="请输入等级说明" />
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
import { listNursingLevel, getNursingLevel, addNursingLevel, updateNursingLevel, delNursingLevel } from "@/api/nursing/nursingLevel"

export default {
  name: "NursingLevel",
  data() {
    return {
      loading: true, ids: [], single: true, multiple: true, showSearch: true, total: 0, list: [], title: "", open: false,
      queryParams: { pageNum: 1, pageSize: 10, name: undefined, status: undefined },
      form: {},
      rules: { name: [{ required: true, message: "等级名称不能为空", trigger: "blur" }] }
    }
  },
  created() { this.getList() },
  methods: {
    getList() {
      this.loading = true
      listNursingLevel(this.queryParams).then(response => { this.list = response.rows; this.total = response.total; this.loading = false })
    },
    handleStatusChange(row) { updateNursingLevel(row).then(() => { this.$modal.msgSuccess("状态更新成功") }) },
    cancel() { this.open = false; this.reset() },
    reset() { this.form = { id: undefined, name: undefined, fee: undefined, lplanId: undefined, description: undefined, status: 1, remark: undefined }; this.resetForm("form") },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.resetForm("queryForm"); this.handleQuery() },
    handleSelectionChange(selection) { this.ids = selection.map(item => item.id); this.single = selection.length != 1; this.multiple = !selection.length },
    handleAdd() { this.reset(); this.open = true; this.title = "添加护理等级" },
    handleUpdate(row) {
      this.reset()
      getNursingLevel(row.id || this.ids).then(response => { this.form = response.data; this.open = true; this.title = "修改护理等级" })
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != undefined) {
            updateNursingLevel(this.form).then(() => { this.$modal.msgSuccess("修改成功"); this.open = false; this.getList() })
          } else {
            addNursingLevel(this.form).then(() => { this.$modal.msgSuccess("新增成功"); this.open = false; this.getList() })
          }
        }
      })
    },
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$modal.confirm('是否确认删除该护理等级？').then(function() { return delNursingLevel(ids) }).then(() => { this.getList(); this.$modal.msgSuccess("删除成功") }).catch(() => {})
    }
  }
}
</script>
