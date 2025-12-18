<template>
  <el-dialog
    v-model="visible"
    :title="titleMap[form.orderType] || '业务订单'"
    width="950px"
    :close-on-click-modal="false"
    destroy-on-close
  >
    <el-form :model="form" ref="formRef" label-width="90px">
      <!-- 抬头信息区域 -->
      <div class="header-section">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="订单类型">
              <el-radio-group v-model="form.orderType" @change="onTypeChange">
                <el-radio-button label="PURCHASE">采购入库</el-radio-button>
                <el-radio-button label="SALE">销售出库</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item :label="form.orderType === 'PURCHASE' ? '供应商' : '客户'" required>
              <el-select 
                v-model="form.contactId" 
                filterable 
                placeholder="请选择往来单位"
                style="width: 100%"
              >
                <el-option 
                  v-for="c in contactList" 
                  :key="c.id" 
                  :label="c.name" 
                  :value="c.id" 
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="单据日期" required>
              <el-date-picker 
                v-model="form.orderDate" 
                type="date" 
                value-format="YYYY-MM-DD" 
                style="width: 100%" 
              />
            </el-form-item>
          </el-col>
        </el-row>
      </div>

      <!-- 商品明细表格 -->
      <el-table :data="form.items" border stripe size="small" class="item-table">
        <el-table-column label="#" width="50" type="index" align="center" />
        
        <el-table-column label="商品名称 (Product)" min-width="220">
          <template #default="{ row }">
            <el-select 
              v-model="row.productId" 
              filterable 
              placeholder="搜索商品名称或编码" 
              @change="(val) => onProductSelect(val, row)"
              style="width: 100%"
            >
              <el-option 
                v-for="p in productList" 
                :key="p.id" 
                :label="p.name + ' (' + (p.sku || '-') + ')'" 
                :value="p.id" 
              />
            </el-select>
          </template>
        </el-table-column>

        <el-table-column label="数量" width="130">
          <template #default="{ row }">
            <el-input-number 
              v-model="row.quantity" 
              :min="1" 
              size="small" 
              style="width: 100%" 
            />
          </template>
        </el-table-column>

        <el-table-column label="单价 (¥)" width="150">
          <template #default="{ row }">
            <el-input-number 
              v-model="row.unitPrice" 
              :min="0" 
              :precision="2" 
              size="small"
              style="width: 100%" 
              class="text-right"
            />
          </template>
        </el-table-column>

        <el-table-column label="小计" width="130" align="right">
          <template #default="{ row }">
            <span class="subtotal">{{ formatMoney((row.quantity || 0) * (row.unitPrice || 0)) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="备注" min-width="150">
          <template #default="{ row }">
            <el-input v-model="row.remark" size="small" placeholder="选填" />
          </template>
        </el-table-column>

        <el-table-column width="60" align="center" label="操作">
          <template #default="{ $index }">
            <el-button 
              type="danger" 
              link 
              :icon="Delete" 
              @click="removeItem($index)" 
              v-if="form.items.length > 1"
            />
          </template>
        </el-table-column>
      </el-table>

      <!-- 底部统计栏 -->
      <div class="table-footer">
        <el-button link type="primary" :icon="Plus" @click="addItem">添加商品行</el-button>
        <div class="total-bar">
          <span class="label">订单总金额:</span>
          <span class="total-amount">¥ {{ totalAmount }}</span>
        </div>
      </div>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="submit" :loading="submitting">
        <el-icon class="el-icon--left"><Check /></el-icon> 提交订单
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Plus, Delete, Check } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { createOrder } from '@/api/order'
import { getProductList } from '@/api/product'
import { getContactList } from '@/api/contact'

const props = defineProps(['bookId'])
const emit = defineEmits(['success'])

const visible = ref(false)
const submitting = ref(false)
const productList = ref([])
const contactList = ref([])

const form = ref({
  bookId: null,
  orderType: 'PURCHASE', // PURCHASE | SALE
  contactId: null,
  orderDate: '',
  items: []
})

const titleMap = {
  PURCHASE: '新建采购入库单',
  SALE: '新建销售出库单'
}

// 计算总金额
const totalAmount = computed(() => {
  const sum = form.value.items.reduce((acc, item) => {
    return acc + (item.quantity || 0) * (item.unitPrice || 0)
  }, 0)
  return formatMoney(sum)
})

// 格式化金额
const formatMoney = (val) => {
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

// 打开弹窗
const open = async (type = 'PURCHASE') => {
  visible.value = true
  form.value = {
    bookId: props.bookId,
    orderType: type,
    contactId: null,
    orderDate: new Date().toISOString().split('T')[0],
    items: [{ productId: null, quantity: 1, unitPrice: 0, remark: '' }]
  }
  
  // 并行加载基础数据
  await loadBaseData(type)
}

// 加载商品和往来单位
const loadBaseData = async (orderType) => {
  // 加载商品 (如果还没加载过)
  if (productList.value.length === 0) {
    try {
      const pRes = await getProductList(props.bookId)
      productList.value = pRes
    } catch (e) {
      console.error(e)
    }
  }
  
  // 加载往来单位 (根据类型筛选：采购找供应商，销售找客户)
  await loadContacts(orderType)
}

const loadContacts = async (orderType) => {
  const contactType = orderType === 'PURCHASE' ? 'VENDOR' : 'CUSTOMER'
  try {
    const cRes = await getContactList({ bookId: props.bookId, type: contactType })
    contactList.value = cRes
  } catch (e) {
    console.error(e)
  }
}

// 切换订单类型时，重新加载对应的往来单位
const onTypeChange = (val) => {
  form.value.contactId = null // 清空已选单位
  loadContacts(val)
}

// 选择商品后自动回填价格
const onProductSelect = (productId, row) => {
  const product = productList.value.find(p => p.id === productId)
  if (product) {
    if (form.value.orderType === 'PURCHASE') {
      row.unitPrice = product.purchaseCost || 0 // 回填进价
    } else {
      row.unitPrice = product.salePrice || 0 // 回填售价
    }
  }
}

const addItem = () => {
  form.value.items.push({ productId: null, quantity: 1, unitPrice: 0, remark: '' })
}

const removeItem = (index) => {
  form.value.items.splice(index, 1)
}

const submit = async () => {
  if (!form.value.contactId) return ElMessage.warning('请选择往来单位')
  if (form.value.items.some(i => !i.productId)) return ElMessage.warning('请完善商品信息')

  submitting.value = true
  try {
    await createOrder(form.value)
    ElMessage.success('订单创建成功')
    visible.value = false
    emit('success')
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

defineExpose({ open })
</script>

<style scoped>
.header-section {
  background-color: #f8f9fa;
  padding: 15px 15px 0 15px;
  border-radius: 4px;
  margin-bottom: 15px;
}
.item-table {
  margin-top: 10px;
}
.subtotal {
  font-family: Consolas, Monaco, monospace;
  font-weight: bold;
  color: #606266;
}
.table-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 15px;
  padding: 12px 15px;
  background-color: #fff;
  border: 1px solid #ebeef5;
  border-top: none;
}
.total-bar {
  display: flex;
  align-items: baseline;
}
.total-bar .label {
  font-size: 14px;
  color: #606266;
  margin-right: 10px;
}
.total-bar .total-amount {
  font-size: 20px;
  color: #F56C6C;
  font-family: Consolas, Monaco, monospace;
  font-weight: bold;
}
</style>