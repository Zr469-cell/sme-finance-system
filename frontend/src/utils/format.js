/**
 * 金额格式化 (Financial Number Formatting)
 * 将数字格式化为千分位货币形式，例如：1234.5 => "1,234.50"
 * * @param {number|string} amount - 金额
 * @param {number} decimals - 保留小数位 (默认2位)
 * @param {string} currencySymbol - 货币符号 (如 ¥, $)
 * @returns {string} 格式化后的字符串
 */
export function formatMoney(amount, decimals = 2, currencySymbol = '') {
  if (amount === undefined || amount === null || amount === '') return '-'
  
  const num = Number(amount)
  if (isNaN(num)) return '-'

  // 使用 Intl.NumberFormat 进行标准千分位格式化
  const options = {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  }
  
  const formatted = new Intl.NumberFormat('zh-CN', options).format(num)
  return currencySymbol ? `${currencySymbol} ${formatted}` : formatted
}

/**
 * 简单的日期格式化
 * 将 Date 对象或时间戳转换为 YYYY-MM-DD 或 YYYY-MM-DD HH:mm:ss
 * * @param {Date|string|number} dateInput - 日期
 * @param {boolean} withTime - 是否包含时间
 * @returns {string}
 */
export function formatDate(dateInput, withTime = false) {
  if (!dateInput) return ''
  
  const date = new Date(dateInput)
  if (isNaN(date.getTime())) return ''

  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  
  let str = `${y}-${m}-${d}`
  
  if (withTime) {
    const hh = String(date.getHours()).padStart(2, '0')
    const mm = String(date.getMinutes()).padStart(2, '0')
    const ss = String(date.getSeconds()).padStart(2, '0')
    str += ` ${hh}:${mm}:${ss}`
  }
  
  return str
}

/**
 * 解析后端传来的百分比 (小数转百分数)
 * 0.05 -> "5%"
 */
export function formatPercent(val) {
  if (!val && val !== 0) return '-'
  return (Number(val) * 100).toFixed(0) + '%'
}