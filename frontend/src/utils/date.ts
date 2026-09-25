/** 月龄/天数计算与日期展示工具 */

export function today(): string {
  return toDateString(new Date());
}

export function toDateString(d: Date): string {
  const y = d.getFullYear();
  const m = `${d.getMonth() + 1}`.padStart(2, '0');
  const day = `${d.getDate()}`.padStart(2, '0');
  return `${y}-${m}-${day}`;
}

/** 由出生日期算“X岁X个月 / X个月X天”的描述 */
export function describeAge(birthday: string, at: string = today()): string {
  const birth = new Date(birthday + 'T00:00:00');
  const end = new Date(at + 'T00:00:00');
  let months =
    (end.getFullYear() - birth.getFullYear()) * 12 + (end.getMonth() - birth.getMonth());
  if (end.getDate() < birth.getDate()) months -= 1;

  const days = Math.max(0, Math.round((end.getTime() - birth.getTime()) / 86400000));
  if (months < 1) return `${days} 天`;
  const years = Math.floor(months / 12);
  const restMonths = months % 12;
  if (years < 1) return `${months} 个月`;
  return restMonths > 0 ? `${years} 岁 ${restMonths} 个月` : `${years} 岁`;
}

/** 距离今天多少天，用于“最近一次记录” */
export function daysFromToday(date: string): number {
  const target = new Date(date + 'T00:00:00');
  return Math.round((Date.now() - target.getTime()) / 86400000);
}

export function formatDateTime(value?: string | null): string {
  if (!value) return '';
  return value.replace('T', ' ').slice(0, 16);
}
