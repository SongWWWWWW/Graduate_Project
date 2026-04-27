<template>
  <AuthorHeader />
  <div class="main box_center cf">
    <div class="userBox cf">
      <AuthorSidebar active="income" />
      <div class="my_r">
        <div class="np-page-title">
          <h2>收益管理</h2>
        </div>

        <!-- Tab 切换 -->
        <div class="tab-bar np-animate-slide-up">
          <div
            :class="['tab-item', activeTab === 'month' ? 'active' : '']"
            @click="activeTab = 'month'"
          >
            <el-icon><Calendar /></el-icon>
            月收入
          </div>
          <div
            :class="['tab-item', activeTab === 'detail' ? 'active' : '']"
            @click="activeTab = 'detail'"
          >
            <el-icon><List /></el-icon>
            收入明细
          </div>
        </div>

        <!-- 月收入 -->
        <div v-if="activeTab === 'month'" class="np-animate-slide-up">
          <div class="np-table-wrap">
            <table>
              <thead>
                <tr>
                  <th>收入月份</th>
                  <th>作品</th>
                  <th>税前收入</th>
                  <th>税后收入</th>
                  <th>支付状态</th>
                  <th>确认状态</th>
                  <th>详情</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in incomeList" :key="item.id">
                  <td><strong>{{ item.incomeMonth }}</strong></td>
                  <td>{{ item.bookName || '-' }}</td>
                  <td class="amount">¥{{ item.preTaxIncome }}</td>
                  <td class="amount primary">¥{{ item.afterTaxIncome }}</td>
                  <td>
                    <span :class="['np-status-tag', item.payStatus === 1 ? 'success' : 'warning']">
                      {{ item.payStatus === 1 ? '已支付' : '待支付' }}
                    </span>
                  </td>
                  <td>
                    <span :class="['np-status-tag', item.confirmStatus === 1 ? 'success' : 'info']">
                      {{ item.confirmStatus === 1 ? '已确认' : '待确认' }}
                    </span>
                  </td>
                  <td class="detail-cell">{{ item.detail || '-' }}</td>
                </tr>
              </tbody>
            </table>
            <div class="table-footer">
              <el-pagination
                small
                layout="prev, pager, next"
                :background="true"
                :page-size="pageSize"
                :total="total"
                @current-change="handleCurrentChange"
              />
            </div>
          </div>
          <div v-if="incomeList.length === 0" class="empty-state">
            <el-empty description="暂无收入记录">
              <template #description>
                <div class="empty-text">暂无收入记录</div>
                <div class="empty-tip">坚持创作，收益自然会来</div>
              </template>
            </el-empty>
          </div>
        </div>

        <!-- 收入明细 -->
        <div v-if="activeTab === 'detail'" class="np-animate-slide-up">
          <div class="np-table-wrap">
            <table>
              <thead>
                <tr>
                  <th>日期</th>
                  <th>作品</th>
                  <th>订阅总额</th>
                  <th>订阅次数</th>
                  <th>订阅人数</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in detailList" :key="item.id">
                  <td><strong>{{ item.incomeDate }}</strong></td>
                  <td>{{ item.bookName || '-' }}</td>
                  <td class="amount primary">¥{{ item.incomeAccount }}</td>
                  <td>{{ item.incomeCount }}</td>
                  <td>{{ item.incomeNumber }}</td>
                </tr>
              </tbody>
            </table>
            <div class="table-footer">
              <el-pagination
                small
                layout="prev, pager, next"
                :background="true"
                :page-size="detailPageSize"
                :total="detailTotal"
                @current-change="handleDetailCurrentChange"
              />
            </div>
          </div>
          <div v-if="detailList.length === 0" class="empty-state">
            <el-empty description="暂无收入明细">
              <template #description>
                <div class="empty-text">暂无收入明细</div>
                <div class="empty-tip">读者订阅后会在此展示</div>
              </template>
            </el-empty>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { reactive, toRefs, onMounted, watch } from "vue";
import AuthorHeader from "@/components/author/Header.vue";
import AuthorSidebar from "@/components/author/Sidebar.vue";
import { listIncome, listIncomeDetail } from "@/api/author";
import { Calendar, List } from "@element-plus/icons-vue";

export default {
  name: "authorIncome",
  components: { AuthorHeader, AuthorSidebar, Calendar, List },
  setup() {
    const state = reactive({
      activeTab: "month",
      incomeList: [],
      total: 0,
      pageSize: 10,
      searchCondition: {},
      detailList: [],
      detailTotal: 0,
      detailPageSize: 10,
      detailSearchCondition: {},
    });

    onMounted(() => {
      loadIncome();
    });

    watch(() => state.activeTab, (val) => {
      if (val === "month") {
        loadIncome();
      } else {
        loadDetail();
      }
    });

    const loadIncome = async () => {
      try {
        const { data } = await listIncome(state.searchCondition);
        state.incomeList = data.list;
        state.searchCondition.pageNum = data.pageNum;
        state.searchCondition.pageSize = data.pageSize;
        state.total = Number(data.total);
      } catch (e) {
        console.error(e);
      }
    };

    const loadDetail = async () => {
      try {
        const { data } = await listIncomeDetail(null, state.detailSearchCondition);
        state.detailList = data.list;
        state.detailSearchCondition.pageNum = data.pageNum;
        state.detailSearchCondition.pageSize = data.pageSize;
        state.detailTotal = Number(data.total);
      } catch (e) {
        console.error(e);
      }
    };

    const handleCurrentChange = (pageNum) => {
      state.searchCondition.pageNum = pageNum;
      loadIncome();
    };

    const handleDetailCurrentChange = (pageNum) => {
      state.detailSearchCondition.pageNum = pageNum;
      loadDetail();
    };

    return {
      ...toRefs(state),
      handleCurrentChange,
      handleDetailCurrentChange,
    };
  },
};
</script>

<style scoped>
.tab-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
}
.tab-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 24px;
  border-radius: 12px;
  background: #f5f7fa;
  color: #606266;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}
.tab-item:hover {
  background: #eef1f6;
  color: #667eea;
}
.tab-item.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.amount {
  font-weight: 600;
  color: #1a1a2e;
}
.amount.primary {
  color: #667eea;
}
.detail-cell {
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.table-footer {
  padding: 16px;
  display: flex;
  justify-content: center;
}
.empty-state {
  padding: 60px 0;
}
.empty-text {
  font-size: 15px;
  color: #606266;
  margin-bottom: 6px;
}
.empty-tip {
  font-size: 13px;
  color: #909399;
}
</style>
