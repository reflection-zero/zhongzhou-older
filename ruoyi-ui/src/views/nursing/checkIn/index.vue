<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="老人姓名" prop="elderName">
        <el-input v-model="queryParams.elderName" placeholder="请输入老人姓名" clearable style="width: 200px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="身份证号" prop="idCardNo">
        <el-input v-model="queryParams.idCardNo" placeholder="请输入身份证号" clearable style="width: 200px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 200px">
          <el-option label="已入住" :value="0" />
          <el-option label="已退住" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['nursing:checkIn:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['nursing:checkIn:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['nursing:checkIn:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="老人姓名" align="center" prop="elderName" />
      <el-table-column label="身份证号" align="center" prop="idCardNo" width="180" />
      <el-table-column label="入住床位" align="center" prop="bedNumber" width="120" />
      <el-table-column label="护理等级" align="center" prop="nursingLevelName" width="120" />
      <el-table-column label="入住开始时间" align="center" prop="startDate" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.startDate) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="入住结束时间" align="center" prop="endDate" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.endDate) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === 0 ? 'success' : 'info'">{{ scope.row.status === 0 ? '已入住' : '已退住' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['nursing:checkIn:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['nursing:checkIn:remove']">删除</el-button>
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
            <el-form-item label="身份证号" prop="idCardNo">
              <el-input v-model="form.idCardNo" placeholder="请输入身份证号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="入住床位" prop="bedNumber">
              <el-input v-model="form.bedNumber" placeholder="请输入入住床位" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="护理等级名称" prop="nursingLevelName">
              <el-input v-model="form.nursingLevelName" placeholder="请输入护理等级名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="入住开始时间" prop="startDate">
              <el-date-picker v-model="form.startDate" type="datetime" placeholder="请选择开始时间" value-format="yyyy-MM-dd HH:mm:ss" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入住结束时间" prop="endDate">
              <el-date-picker v-model="form.endDate" type="datetime" placeholder="请选择结束时间" value-format="yyyy-MM-dd HH:mm:ss" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择状态">
                <el-option label="已入住" :value="0" />
                <el-option label="已退住" :value="1" />
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
import { listCheckIn, getCheckIn, addCheckIn, updateCheckIn, delCheckIn } from "@/api/nursing/checkIn"

export default {
  name: "CheckIn",
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      list: [],
      title: "",
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        elderName: undefined,
        idCardNo: undefined,
        status: undefined
      },
      form: {},
      rules: {
        elderName: [{ required: true, message: "老人姓名不能为空", trigger: "blur" }],
        idCardNo: [{ required: true, message: "身份证号不能为空", trigger: "blur" }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listCheckIn(this.queryParams).then(response => {
        this.list = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        id: undefined,
        elderName: undefined,
        elderId: undefined,
        idCardNo: undefined,
        startDate: undefined,
        endDate: undefined,
        nursingLevelName: undefined,
        bedNumber: undefined,
        status: undefined,
        remark: undefined
      }
      this.resetForm("form")
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加入住记录"
    },
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getCheckIn(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改入住记录"
      })
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != undefined) {
            updateCheckIn(this.form).then(() => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addCheckIn(this.form).then(() => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$modal.confirm('是否确认删除该入住记录？').then(function() {
        return delCheckIn(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    }
  }
}
</script>
