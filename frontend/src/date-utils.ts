/** 把 ISO(yyyy-MM-dd) 格式化为中文日期。 */
export function formatDate(iso?: string | null): string {
  if (!iso) return '';
  const [y, m, d] = iso.split('T')[0].split('-');
  return `${y}年${Number(m)}月${Number(d)}日`;
}

/** 从出生日到指定日期的月龄描述，如“11个月28天 / 出生第 5 天”。 */
export function ageText(birthday?: string | null, target?: string | null): string {
  if (!birthday || !target) return '';
  const birth = new Date(birthday + 'T00:00:00');
  const day = new Date(target.split('T')[0] + 'T00:00:00');
  let months =
    (day.getFullYear() - birth.getFullYear()) * 12 +
    (day.getMonth() - birth.getMonth());
  const anchor = new Date(birth);
  anchor.setMonth(anchor.getMonth() + months);
  if (anchor.getTime() > day.getTime()) {
    months -= 1;
    anchor.setMonth(anchor.getMonth() - 1);
  }
  const days = Math.round((day.getTime() - anchor.getTime()) / 86400000);
  if (months <= 0) return `出生第 ${days} 天`;
  return `${months}个月${days > 0 ? `${days}天` : ''}`;
}

export function todayISO(): string {
  const d = new Date();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${m}-${day}`;
}

export function createdAtText(s?: string | null): string {
  if (!s) return '';
  return s.replace('T', ' ').slice(0, 16);
}
