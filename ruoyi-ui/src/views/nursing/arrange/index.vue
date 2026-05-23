<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="老人姓名" prop="elderName">
        <el-input v-model="queryParams.elderName" placeholder="请输入老人姓名" clearable style="width: 200px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="项目名称" prop="projectName">
        <el-input v-model="queryParams.projectName" placeholder="请输入项目名称" clearable style="width: 200px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 200px">
          <el-option label="待执行" :value="1" />
          <el-option label="已执行" :value="2" />
          <el-option label="已关闭" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['nursing:nursingTask:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['nursing:nursingTask:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['nursing:nursingTask:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="老人姓名" align="center" prop="elderName" />
      <el-table-column label="项目名称" align="center" prop="projectName" />
      <el-table-column label="床位编号" align="center" prop="bedNumber" />
      <el-table-column label="护理员ID" align="center" prop="nursingId" />
      <el-table-column label="预计服务时间" align="center" prop="estimatedServerTime" width="160">
        <template slot-scope="scope">{{ parseTime(scope.row.estimatedServerTime) }}</template>
      </el-table-column>
      <el-table-column label="实际服务时间" align="center" prop="realServerTime" width="160">
        <template slot-scope="scope">{{ parseTime(scope.row.realServerTime) }}</template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status === 1" type="info">待执行</el-tag>
          <el-tag v-else-if="scope.row.status === 2" type="success">已执行</el-tag>
          <el-tag v-else-if="scope.row.status === 3" type="danger">已关闭</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['nursing:nursingTask:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['nursing:nursingTask:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="650px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="老人姓名" prop="elderName">
              <el-input v-model="form.elderName" placeholder="请输入老人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="项目名称" prop="projectName">
              <el-input v-model="form.projectName" placeholder="请输入项目名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="床位编号" prop="bedNumber">
              <el-input v-model="form.bedNumber" placeholder="请输入床位编号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="护理员ID" prop="nursingId">
              <el-input v-model="form.nursingId" placeholder="请输入护理员ID, 多个以逗号分隔" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="预计服务时间" prop="estimatedServerTime">
              <el-date-picker v-model="form.estimatedServerTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="实际服务时间" prop="realServerTime">
              <el-date-picker v-model="form.realServerTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择状态">
                <el-option label="待执行" :value="1" />
                <el-option label="已执行" :value="2" />
                <el-option label="已关闭" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="执行图片" prop="taskImage">
              <el-input v-model="form.taskImage" placeholder="请输入图片地址" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="执行记录" prop="mark">
              <el-input v-model="form.mark" type="textarea" placeholder="请输入执行记录" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="取消原因" prop="cancelReason">
              <el-input v-model="form.cancelReason" type="textarea" placeholder="请输入取消原因" />
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
import { listNursingTask, getNursingTask, addNursingTask, updateNursingTask, delNursingTask } from "@/api/nursing/nursingTask"

export default {
  name: "Arrange",
  data() {
    return {
      loading: true, ids: [], single: true, multiple: true, showSearch: true, total: 0, list: [], title: "", open: false,
      queryParams: { pageNum: 1, pageSize: 10, elderName: undefined, projectName: undefined, status: undefined },
      form: {},
      rules: {
        elderName: [{ required: true, message: "老人姓名不能为空", trigger: "blur" }],
        projectName: [{ required: true, message: "项目名称不能为空", trigger: "blur" }]
      }
    }
  },
  created() { this.getList() },
  methods: {
    getList() {
      this.loading = true
      listNursingTask(this.queryParams).then(response => { this.list = response.rows; this.total = response.total; this.loading = false })
    },
    cancel() { this.open = false; this.reset() },
    reset() {
      this.form = { id: undefined, elderName: undefined, elderId: undefined, projectId: undefined, projectName: undefined, bedNumber: undefined, nursingId: undefined, estimatedServerTime: undefined, realServerTime: undefined, mark: undefined, cancelReason: undefined, status: 1, taskImage: undefined, remark: undefined }
      this.resetForm("form")
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.resetForm("queryForm"); this.handleQuery() },
    handleSelectionChange(selection) { this.ids = selection.map(item => item.id); this.single = selection.length != 1; this.multiple = !selection.length },
    handleAdd() { this.reset(); this.open = true; this.title = "添加任务安排" },
    handleUpdate(row) {
      this.reset()
      getNursingTask(row.id || this.ids).then(response => { this.form = response.data; this.open = true; this.title = "修改任务安排" })
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != undefined) {
            updateNursingTask(this.form).then(() => { this.$modal.msgSuccess("修改成功"); this.open = false; this.getList() })
          } else {
            addNursingTask(this.form).then(() => { this.$modal.msgSuccess("新增成功"); this.open = false; this.getList() })
          }
        }
      })
    },
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$modal.confirm('是否确认删除该任务？').then(function() { return delNursingTask(ids) }).then(() => { this.getList(); this.$modal.msgSuccess("删除成功") }).catch(() => {})
    }
  }
}
</script>
