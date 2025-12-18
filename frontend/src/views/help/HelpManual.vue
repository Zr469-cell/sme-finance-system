<template>
  <div class="manual-wrapper">
    <el-scrollbar>
      <div class="content-container">
        
        <!-- 头部 Header -->
        <header class="manual-header">
          <h1><el-icon><Reading /></el-icon> 简盈财务系统 使用手册</h1>
          <p class="subtitle">为小微企业与个人量身定制的“业务财务一体化”实战指南 | v1.1.0</p>
        </header>

        <el-divider />

        <!-- 1. 会计速成班 -->
        <section class="section-block" id="section-1">
          <h2 class="section-title"><span class="index">01</span> 会计准则速成 (Accounting 101)</h2>
          
          <!-- 1.1 会计等式卡片 -->
          <div class="concept-card">
            <h3>1.1 核心会计等式：复式记账的灵魂</h3>
            <p>复式记账法要求每一笔交易都必须同时记录资金的**来源**和**去向**。无论何时，系统都必须保持这个等式的平衡。</p>
            <div class="equation-viz">
              <div class="block asset">
                <span class="label">资产 (Assets)</span>
                <span class="desc">钱、货、设备</span>
              </div>
              <div class="symbol">=</div>
              <div class="block liability">
                <span class="label">负债 (Liabilities)</span>
                <span class="desc">欠供应商/银行</span>
              </div>
              <div class="symbol">+</div>
              <div class="block equity">
                <span class="label">所有者权益 (Equity)</span>
                <span class="desc">本金 + 利润</span>
              </div>
            </div>
            <div class="equation-note">
              <el-tag type="info">扩展公式</el-tag> 资产 + 费用 = 负债 + 所有者权益 + 收入
            </div>
          </div>

          <!-- 1.2 借贷记账法 -->
          <div class="concept-card" style="margin-top: 20px;">
            <h3>1.2 借贷记账法口诀表 (The Golden Rules)</h3>
            <p>“借” (Debit) 和“贷” (Credit) 只是符号，不代表欠钱或借钱。请死记以下规则：</p>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-card shadow="hover" class="rule-card left">
                  <template #header>
                    <div class="card-head">
                      <span>借方 (Debit) 记什么？</span>
                      <el-tag type="primary">左侧</el-tag>
                    </div>
                  </template>
                  <ul class="rule-list">
                    <li><el-icon color="#67C23A"><Top /></el-icon> <strong>资产增加</strong> (收到钱、买了货)</li>
                    <li><el-icon color="#67C23A"><Top /></el-icon> <strong>费用增加</strong> (花了钱、付房租)</li>
                    <li class="divider"></li>
                    <li><el-icon color="#F56C6C"><Bottom /></el-icon> 负债减少 (还了钱)</li>
                    <li><el-icon color="#F56C6C"><Bottom /></el-icon> 权益减少 (老板撤资)</li>
                    <li><el-icon color="#F56C6C"><Bottom /></el-icon> 收入减少 (退款)</li>
                  </ul>
                </el-card>
              </el-col>
              <el-col :span="12">
                <el-card shadow="hover" class="rule-card right">
                  <template #header>
                    <div class="card-head">
                      <span>贷方 (Credit) 记什么？</span>
                      <el-tag type="warning">右侧</el-tag>
                    </div>
                  </template>
                  <ul class="rule-list">
                    <li><el-icon color="#67C23A"><Top /></el-icon> <strong>负债增加</strong> (借了钱、欠货款)</li>
                    <li><el-icon color="#67C23A"><Top /></el-icon> <strong>权益增加</strong> (注资、盈利)</li>
                    <li><el-icon color="#67C23A"><Top /></el-icon> <strong>收入增加</strong> (卖了货、收利息)</li>
                    <li class="divider"></li>
                    <li><el-icon color="#F56C6C"><Bottom /></el-icon> 资产减少 (付出现金)</li>
                  </ul>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </section>

        <!-- 2. 实战指南 -->
        <section class="section-block" id="section-2">
          <h2 class="section-title"><span class="index">02</span> 实战：如何录入第一笔凭证</h2>
          <p class="desc-text">不要被会计术语吓倒。录入凭证只需两步：1. 确定钱从哪来（贷方）；2. 确定钱到哪去（借方）。</p>

          <el-tabs type="border-card" class="tutorial-tabs">
            <el-tab-pane label="我是企业/个体户">
              <div class="scenario-grid">
                <!-- 场景 1 -->
                <div class="scenario-card">
                  <div class="s-header">场景 A：从银行提现备用</div>
                  <div class="s-body">
                    <p>我去银行取了 5000 元现金放在保险柜。</p>
                    <div class="accounting-entry">
                      <div class="line">
                        <span class="ac debit">[1001] 库存现金</span>
                        <span class="amt">5000</span>
                        <span class="reason">← 钱变多了 (资产增加)</span>
                      </div>
                      <div class="line">
                        <span class="ac credit">[1002] 银行存款</span>
                        <span class="amt">5000</span>
                        <span class="reason">← 卡里钱少了 (资产减少)</span>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 场景 2 -->
                <div class="scenario-card">
                  <div class="s-header">场景 B：报销差旅费</div>
                  <div class="s-body">
                    <p>员工张三报销出差高铁票 300 元，用现金支付。</p>
                    <div class="accounting-entry">
                      <div class="line">
                        <span class="ac debit">[6602] 管理费用-差旅费</span>
                        <span class="amt">300</span>
                        <span class="reason">← 费用发生了 (借方)</span>
                      </div>
                      <div class="line">
                        <span class="ac credit">[1001] 库存现金</span>
                        <span class="amt">300</span>
                        <span class="reason">← 现金付出去 (资产减少)</span>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 场景 3 -->
                <div class="scenario-card">
                  <div class="s-header">场景 C：收到客户货款</div>
                  <div class="s-body">
                    <p>客户李四通过银行转账支付了之前欠的货款 10,000 元。</p>
                    <div class="accounting-entry">
                      <div class="line">
                        <span class="ac debit">[1002] 银行存款</span>
                        <span class="amt">10,000</span>
                        <span class="reason">← 收到钱了 (资产增加)</span>
                      </div>
                      <div class="line">
                        <span class="ac credit">[1122] 应收账款</span>
                        <span class="amt">10,000</span>
                        <span class="reason">← 债权消除了 (资产减少)</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="我是个人/家庭用户">
              <div class="scenario-grid">
                <!-- 场景 1 -->
                <div class="scenario-card">
                  <div class="s-header">场景 A：发工资啦</div>
                  <div class="s-body">
                    <p>公司发工资 8000 元到招商银行卡。</p>
                    <div class="accounting-entry">
                      <div class="line">
                        <span class="ac debit">[资产] 银行存款</span>
                        <span class="amt">8000</span>
                        <span class="reason">← 存款增加</span>
                      </div>
                      <div class="line">
                        <span class="ac credit">[收入] 工资收入</span>
                        <span class="amt">8000</span>
                        <span class="reason">← 收入增加 (贷方)</span>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 场景 2 -->
                <div class="scenario-card">
                  <div class="s-header">场景 B：超市买菜</div>
                  <div class="s-body">
                    <p>去超市买了 200 元的菜和肉，微信支付。</p>
                    <div class="accounting-entry">
                      <div class="line">
                        <span class="ac debit">[费用] 食品烟酒</span>
                        <span class="amt">200</span>
                        <span class="reason">← 花销增加</span>
                      </div>
                      <div class="line">
                        <span class="ac credit">[资产] 微信钱包</span>
                        <span class="amt">200</span>
                        <span class="reason">← 余额减少</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </section>

        <!-- 3. 科目字典 -->
        <section class="section-block" id="section-3">
          <h2 class="section-title"><span class="index">03</span> 标准科目字典</h2>
          <p class="desc-text">系统在一键初始化时会创建以下科目。您可以根据业务需要修改名称或增加子科目。</p>
          
          <el-collapse v-model="activeCollapse">
            <el-collapse-item title="1000 资产类 (Assets)" name="1">
              <el-table :data="assetsData" border size="small">
                <el-table-column prop="code" label="代码" width="100" />
                <el-table-column prop="name" label="科目名称" width="180" />
                <el-table-column prop="desc" label="业务解释" />
              </el-table>
            </el-collapse-item>
            <el-collapse-item title="2000 负债类 (Liabilities)" name="2">
              <el-table :data="liabilitiesData" border size="small">
                <el-table-column prop="code" label="代码" width="100" />
                <el-table-column prop="name" label="科目名称" width="180" />
                <el-table-column prop="desc" label="业务解释" />
              </el-table>
            </el-collapse-item>
            <el-collapse-item title="6000 损益类 (Income & Expenses)" name="3">
              <el-table :data="plData" border size="small">
                <el-table-column prop="code" label="代码" width="100" />
                <el-table-column prop="name" label="科目名称" width="180" />
                <el-table-column prop="desc" label="业务解释" />
              </el-table>
            </el-collapse-item>
          </el-collapse>
        </section>

        <!-- 4. 业务流程 -->
        <section class="section-block" id="section-4">
          <h2 class="section-title"><span class="index">04</span> 进销存业务流程 (ERP)</h2>

          <div class="flow-box">
            <h4><el-icon><ShoppingCart /></el-icon> 采购进货流程 (Procurement)</h4>
            <el-steps :active="3" align-center finish-status="success">
              <el-step title="1. 新建采购单" description="订单中心 -> 新建采购单。此时单据状态为'草稿'，不影响库存和财务。" />
              <el-step title="2. 货到审核" description="确认收货后，点击'审核执行'。系统自动增加实物库存，并生成凭证：借 库存 / 贷 应付账款。" />
              <el-step title="3. 付款结算" description="财务核算 -> 记一笔。录入凭证：借 应付账款 / 贷 银行存款。" />
            </el-steps>
          </div>

          <div class="flow-box" style="margin-top: 20px;">
            <h4><el-icon><Sell /></el-icon> 销售出货流程 (Sales)</h4>
            <el-steps :active="3" align-center finish-status="success">
              <el-step title="1. 新建销售单" description="订单中心 -> 新建销售单。确认客户和价格。" />
              <el-step title="2. 发货审核" description="点击'审核执行'。系统自动校验库存是否充足，扣减库存，生成凭证：借 应收账款 / 贷 主营收入。" />
              <el-step title="3. 收到货款" description="财务核算 -> 记一笔。录入凭证：借 银行存款 / 贷 应收账款。" />
            </el-steps>
          </div>
        </section>

        <!-- 5. 常见问题 -->
        <section class="section-block" id="section-5">
          <h2 class="section-title"><span class="index">05</span> 常见问题 (FAQ)</h2>
          
          <div class="faq-item">
            <div class="question">Q: 为什么新建的账套里没有科目？</div>
            <div class="answer">A: 在创建账套时，请务必勾选 **“自动生成标准会计科目树”**。如果忘记勾选，您可以删除该账套重新创建，或手动添加根科目。</div>
          </div>
          
          <div class="faq-item">
            <div class="question">Q: 什么是“冲销在途物资”？</div>
            <div class="answer">A: 解决跨期采购问题。当您在年底收到发票但货没到时，财务先记一笔“借：在途物资 / 贷：应付账款”。年后货到了，在订单审核时勾选此项，系统生成的凭证贷方就是“在途物资”，避免重复记录负债。</div>
          </div>

          <div class="faq-item">
            <div class="question">Q: 凭证录入时提示“不平衡”怎么办？</div>
            <div class="answer">A: 复式记账要求借贷必相等。您可以点击录入弹窗底部的 **“自动配平”** 按钮，系统会自动计算差额并填入您选中的行。</div>
          </div>
        </section>

        <footer class="manual-footer">
          <p>技术支持：Java Spring Boot + Vue 3 + MySQL</p>
          <p>&copy; 2025 简盈财务系统开发人zrong</p>
        </footer>

      </div>
    </el-scrollbar>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Reading, Top, Bottom, ShoppingCart, Sell, InfoFilled } from '@element-plus/icons-vue'

const scrollContainer = ref(null)
const activeCollapse = ref(['1'])

const assetsData = [
  { code: '1001', name: '库存现金', desc: '保险柜里的现钞，备用金' },
  { code: '1002', name: '银行存款', desc: '对公账户、支付宝、微信余额' },
  { code: '1122', name: '应收账款', desc: '客户欠我们的货款' },
  { code: '1401', name: '在途物资', desc: '已付款发货但未入库的商品（解决跨期）' },
  { code: '1405', name: '库存商品', desc: '仓库里的实物资产' },
  { code: '1601', name: '固定资产', desc: '设备、电脑、车辆等' },
]

const liabilitiesData = [
  { code: '2001', name: '短期借款', desc: '一年内到期的银行贷款' },
  { code: '2202', name: '应付账款', desc: '欠供应商的货款' },
  { code: '2211', name: '应付职工薪酬', desc: '计提但未发的工资' },
  { code: '2221', name: '应交税费', desc: '欠税务局的税款' },
]

const plData = [
  { code: '6001', name: '主营业务收入', desc: '卖产品赚的钱 (贷方增加)' },
  { code: '6401', name: '主营业务成本', desc: '卖出产品的进货成本 (借方增加)' },
  { code: '6602', name: '管理费用', desc: '房租、水电、行政开支 (借方增加)' },
]
</script>

<style scoped>
.manual-wrapper {
  height: 100%;
  background-color: #f5f7fa;
}

.content-container {
  max-width: 1100px;
  margin: 20px auto;
  background: white;
  padding: 40px 60px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}

/* Header */
.manual-header h1 {
  font-size: 28px;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}
.manual-header .subtitle {
  color: #909399;
  font-size: 14px;
}

/* Section Common */
.section-block {
  margin-bottom: 50px;
}
.section-title {
  font-size: 22px;
  color: #303133;
  margin-bottom: 20px;
  border-bottom: 2px solid #f0f2f5;
  padding-bottom: 10px;
}
.section-title .index {
  color: #409EFF;
  font-family: 'Impact', sans-serif;
  font-size: 28px;
  margin-right: 10px;
  vertical-align: middle;
}
.desc-text {
  color: #606266;
  margin-bottom: 15px;
  line-height: 1.6;
}

/* 1.1 Equation Viz */
.concept-card {
  background: #fcfcfc;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 20px;
}
.concept-card h3 {
  margin-top: 0;
  font-size: 16px;
  border-left: 4px solid #409EFF;
  padding-left: 10px;
}
.equation-viz {
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 30px 0;
  gap: 15px;
}
.block {
  padding: 15px 30px;
  border-radius: 6px;
  text-align: center;
  color: white;
  min-width: 150px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
}
.block .label { display: block; font-weight: bold; font-size: 16px; }
.block .desc { font-size: 12px; opacity: 0.9; }
.asset { background: linear-gradient(135deg, #409EFF, #337ecc); }
.liability { background: linear-gradient(135deg, #E6A23C, #d48806); }
.equity { background: linear-gradient(135deg, #67C23A, #529b2e); }
.symbol { font-size: 24px; font-weight: bold; color: #909399; }
.equation-note { text-align: center; color: #909399; font-size: 13px; margin-top: 15px; }

/* 1.2 Rules */
.rule-card { height: 100%; }
.rule-card .card-head { display: flex; justify-content: space-between; align-items: center; font-weight: bold; }
.rule-list { list-style: none; padding: 0; margin: 0; }
.rule-list li { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; color: #606266; }
.rule-list .divider { height: 1px; background: #eee; margin: 15px 0; }

/* 2. Scenario (New) */
.scenario-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  padding: 10px;
}
.scenario-card {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background: #f9f9f9;
  padding: 15px;
}
.scenario-card .s-header {
  font-weight: bold;
  color: #303133;
  margin-bottom: 10px;
  border-bottom: 1px dashed #dcdfe6;
  padding-bottom: 5px;
}
.scenario-card p {
  font-size: 13px;
  color: #606266;
  margin-bottom: 10px;
}
.accounting-entry {
  background: white;
  padding: 10px;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}
.accounting-entry .line {
  display: flex;
  justify-content: space-between;
  font-family: monospace;
  font-size: 12px;
  margin-bottom: 4px;
}
.accounting-entry .ac.debit { color: #409EFF; }
.accounting-entry .ac.credit { color: #E6A23C; padding-left: 10px; }
.accounting-entry .reason { color: #909399; font-size: 10px; }

/* 3. Flows */
.flow-box {
  background: #f9fafc;
  padding: 25px;
  border-radius: 8px;
  border: 1px dashed #dcdfe6;
}
.flow-box h4 { margin-top: 0; margin-bottom: 20px; color: #303133; display: flex; align-items: center; gap: 8px; }

/* 4. FAQ */
.faq-item {
  margin-bottom: 20px;
  padding-left: 20px;
  border-left: 4px solid #dcdfe6;
}
.faq-item .question { font-weight: bold; color: #303133; margin-bottom: 5px; }
.faq-item .answer { color: #606266; font-size: 14px; line-height: 1.6; }

.manual-footer {
  text-align: center;
  color: #909399;
  font-size: 12px;
  margin-top: 50px;
  border-top: 1px solid #ebeef5;
  padding-top: 20px;
}
</style>