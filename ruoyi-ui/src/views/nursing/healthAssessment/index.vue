<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="老人姓名" prop="elderName">
        <el-input v-model="queryParams.elderName" placeholder="请输入老人姓名" clearable style="width: 200px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="身份证号" prop="idCard">
        <el-input v-model="queryParams.idCard" placeholder="请输入身份证号" clearable style="width: 200px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['nursing:healthAssessment:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['nursing:healthAssessment:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['nursing:healthAssessment:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="老人姓名" align="center" prop="elderName" />
      <el-table-column label="身份证号" align="center" prop="idCard" width="180" />
      <el-table-column label="年龄" align="center" prop="age" width="80" />
      <el-table-column label="健康评分" align="center" prop="healthScore" width="100" />
      <el-table-column label="危险等级" align="center" prop="riskLevel" width="100" />
      <el-table-column label="推荐护理等级" align="center" prop="nursingLevelName" />
      <el-table-column label="是否建议入住" align="center" prop="suggestionForAdmission" width="120">
        <template slot-scope="scope">{{ scope.row.suggestionForAdmission === 0 ? '建议' : '不建议' }}</template>
      </el-table-column>
      <el-table-column label="入住情况" align="center" prop="admissionStatus" width="100">
        <template slot-scope="scope">{{ scope.row.admissionStatus === 0 ? '已入住' : '未入住' }}</template>
      </el-table-column>
      <el-table-column label="评估时间" align="center" prop="assessmentTime" width="160">
        <template slot-scope="scope">{{ parseTime(scope.row.assessmentTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['nursing:healthAssessment:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['nursing:healthAssessment:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="700px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="老人姓名" prop="elderName">
              <el-input v-model="form.elderName" placeholder="请输入老人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="form.idCard" placeholder="请输入身份证号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="form.gender" placeholder="请选择性别">
                <el-option label="男" :value="0" />
                <el-option label="女" :value="1" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年龄" prop="age">
              <el-input-number v-model="form.age" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="健康评分" prop="healthScore">
              <el-input v-model="form.healthScore" placeholder="请输入健康评分" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="危险等级" prop="riskLevel">
              <el-input v-model="form.riskLevel" placeholder="健康/提示/风险/危险/严重危险" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="是否建议入住" prop="suggestionForAdmission">
              <el-select v-model="form.suggestionForAdmission" placeholder="请选择">
                <el-option label="建议" :value="0" />
                <el-option label="不建议" :value="1" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="推荐护理等级" prop="nursingLevelName">
              <el-input v-model="form.nursingLevelName" placeholder="请输入护理等级" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="入住情况" prop="admissionStatus">
              <el-select v-model="form.admissionStatus" placeholder="请选择">
                <el-option label="已入住" :value="0" />
                <el-option label="未入住" :value="1" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="评估时间" prop="assessmentTime">
              <el-date-picker v-model="form.assessmentTime" type="datetime" placeholder="请选择评估时间" value-format="yyyy-MM-dd HH:mm:ss" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="体检报告URL" prop="physicalReportUrl">
              <el-input v-model="form.physicalReportUrl" placeholder="请输入体检报告URL" />
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
import { listHealthAssessment, getHealthAssessment, addHealthAssessment, updateHealthAssessment, delHealthAssessment } from "@/api/nursing/healthAssessment"

export default {
  name: "HealthAssessment",
  data() {
    return {
      loading: true, ids: [], single: true, multiple: true, showSearch: true, total: 0, list: [], title: "", open: false,
      queryParams: { pageNum: 1, pageSize: 10, elderName: undefined, idCard: undefined },
      form: {},
      rules: { elderName: [{ required: true, message: "老人姓名不能为空", trigger: "blur" }] }
    }
  },
  created() { this.getList() },
  methods: {
    getList() {
      this.loading = true
      listHealthAssessment(this.queryParams).then(response => { this.list = response.rows; this.total = response.total; this.loading = false })
    },
    cancel() { this.open = false; this.reset() },
    reset() {
      this.form = { id: undefined, elderName: undefined, idCard: undefined, gender: undefined, age: undefined, healthScore: undefined, riskLevel: undefined, suggestionForAdmission: undefined, nursingLevelName: undefined, admissionStatus: undefined, physicalReportUrl: undefined, assessmentTime: undefined, remark: undefined }
      this.resetForm("form")
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.resetForm("queryForm"); this.handleQuery() },
    handleSelectionChange(selection) { this.ids = selection.map(item => item.id); this.single = selection.length != 1; this.multiple = !selection.length },
    handleAdd() { this.reset(); this.open = true; this.title = "添加健康评估" },
    handleUpdate(row) {
      this.reset()
      getHealthAssessment(row.id || this.ids).then(response => { this.form = response.data; this.open = true; this.title = "修改健康评估" })
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != undefined) {
            updateHealthAssessment(this.form).then(() => { this.$modal.msgSuccess("修改成功"); this.open = false; this.getList() })
          } else {
            addHealthAssessment(this.form).then(() => { this.$modal.msgSuccess("新增成功"); this.open = false; this.getList() })
          }
        }
      })
    },
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$modal.confirm('是否确认删除该健康评估数据？').then(function() { return delHealthAssessment(ids) }).then(() => { this.getList(); this.$modal.msgSuccess("删除成功") }).catch(() => {})
    }
  }
}
</script>
