import { reactive, computed, type ComputedRef } from 'vue';

type Getter<T> = (state: T) => unknown;
type ActionFn = (...args: any[]) => unknown;

interface StoreOptions<T, G, A> {
  state: T;
  getters?: G;
  actions?: A & ThisType<{ state: T } & A>;
}

/**
 * 轻量响应式 store（避免引入额外依赖）：reactive 提供全局单例状态，
 * getters 以 computed 暴露，actions 自动绑定到 { state } 上下文。
 */
export function defineStore<
  T extends object,
  G extends Record<string, Getter<T>> = Record<string, Getter<T>>,
  A extends Record<string, ActionFn> = Record<string, ActionFn>,
>(options: StoreOptions<T, G, A>) {
  const state = reactive(options.state) as T;
  const api = { state } as { state: T };

  for (const [key, fn] of Object.entries(options.actions ?? {})) {
    (api as Record<string, unknown>)[key] = (fn as ActionFn).bind(api);
  }

  type Store = { state: T } & {
    [K in keyof G]: ComputedRef<ReturnType<G[K]>>;
  } & A;

  return function useStore(): Store {
    const store: Record<string, unknown> = { state };
    for (const [key, fn] of Object.entries(options.getters ?? {})) {
      store[key] = computed(() => (fn as Getter<T>)(state));
    }
    for (const key of Object.keys(options.actions ?? {})) {
      store[key] = (api as Record<string, unknown>)[key];
    }
    return store as Store;
  };
}
